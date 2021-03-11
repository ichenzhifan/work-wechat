package com.linkwechat.common.utils.wecom;

import com.google.common.base.CharMatcher;
import com.google.common.io.BaseEncoding;
import com.linkwechat.common.exception.wecom.WeComException;
import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

/**
 * <pre>
 * 对公众平台发送给公众账号的消息加解密示例代码.
 * Copyright (c) 1998-2014 Tencent Inc.
 * 针对org.apache.commons.codec.binary.Base64，
 * 需要导入架包commons-codec-1.9（或commons-codec-1.8等其他版本）
 * 官方下载地址：http://commons.apache.org/proper/commons-codec/download_codec.cgi
 * </pre>
 *
 * @author Tencent
 */
public class WxCryptUtil {

    private static final Base64 BASE64 = new Base64();
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final ThreadLocal<DocumentBuilder> BUILDER_LOCAL = new ThreadLocal<DocumentBuilder>() {
        @Override
        protected DocumentBuilder initialValue() {
            try {
                final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setExpandEntityReferences(false);
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                return factory.newDocumentBuilder();
            } catch (ParserConfigurationException exc) {
                throw new IllegalArgumentException(exc);
            }
        }
    };

    protected byte[] aesKey;
    protected String token;
    protected String appidOrCorpid;

    public WxCryptUtil() {
    }

    /**
     * 构造函数.
     *
     * @param token          公众平台上，开发者设置的token
     * @param encodingAesKey 公众平台上，开发者设置的EncodingAESKey
     * @param appidOrCorpid  公众平台appid/corpid
     */
    public WxCryptUtil(String token, String encodingAesKey, String appidOrCorpid) {
        this.token = token;
        this.appidOrCorpid = appidOrCorpid;
        this.aesKey = BaseEncoding.base64().decode(CharMatcher.whitespace().removeFrom(encodingAesKey));
    }

    private static String extractEncryptPart(String xml) {
        try {
            DocumentBuilder db = BUILDER_LOCAL.get();
            Document document = db.parse(new InputSource(new StringReader(xml)));

            Element root = document.getDocumentElement();
            return root.getElementsByTagName("Encrypt").item(0).getTextContent();
        } catch (Exception e) {
            throw new WeComException(e.getMessage());
        }
    }

    /**
     * 将一个数字转换成生成4个字节的网络字节序bytes数组.
     */
    private static byte[] number2BytesInNetworkOrder(int number) {
        byte[] orderBytes = new byte[4];
        orderBytes[3] = (byte) (number & 0xFF);
        orderBytes[2] = (byte) (number >> 8 & 0xFF);
        orderBytes[1] = (byte) (number >> 16 & 0xFF);
        orderBytes[0] = (byte) (number >> 24 & 0xFF);
        return orderBytes;
    }

    /**
     * 4个字节的网络字节序bytes数组还原成一个数字.
     */
    private static int bytesNetworkOrder2Number(byte[] bytesInNetworkOrder) {
        int sourceNumber = 0;
        for (int i = 0; i < 4; i++) {
            sourceNumber <<= 8;
            sourceNumber |= bytesInNetworkOrder[i] & 0xff;
        }
        return sourceNumber;
    }

    /**
     * 随机生成16位字符串.
     */
    private static String genRandomStr() {
        String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 生成xml消息.
     *
     * @param encrypt   加密后的消息密文
     * @param signature 安全签名
     * @param timestamp 时间戳
     * @param nonce     随机字符串
     * @return 生成的xml字符串
     */
    private static String generateXml(String encrypt, String signature, String timestamp, String nonce) {
        String format = "<xml>\n" + "<Encrypt><![CDATA[%1$s]]></Encrypt>\n"
                + "<MsgSignature><![CDATA[%2$s]]></MsgSignature>\n"
                + "<TimeStamp>%3$s</TimeStamp>\n" + "<Nonce><![CDATA[%4$s]]></Nonce>\n"
                + "</xml>";
        return String.format(format, encrypt, signature, timestamp, nonce);
    }

    /**
     * 发送文字消息格式封装
     *
     * @param content
     * @return
     */
    public static String getTextRespData(String content) {
        return "<xml><MsgType><![CDATA[text]]></MsgType><Content><![CDATA[" + content + "]]></Content></xml>";
    }

    public static String getReqData(String toUserName, String encrypt, String agentId) {
        return "<xml><ToUserName><![CDATA[" + toUserName + "]]></ToUserName><Encrypt><![CDATA[" + encrypt +
                "]]></Encrypt><AgentID><![CDATA[" + agentId + "]]></AgentID></xml>";
    }

    /**
     * 将公众平台回复用户的消息加密打包.
     * <ol>
     * <li>对要发送的消息进行AES-CBC加密</li>
     * <li>生成安全签名</li>
     * <li>将消息密文和安全签名打包成xml格式</li>
     * </ol>
     *
     * @param plainText 公众平台待回复用户的消息，xml格式的字符串
     * @return 加密后的可以直接回复用户的密文，包括msg_signature, timestamp, nonce, encrypt的xml格式的字符串
     */
    public String encrypt(String plainText) {
        // 加密
        String encryptedXml = encrypt(genRandomStr(), plainText);

        // 生成安全签名
        String timeStamp = Long.toString(System.currentTimeMillis() / 1000L);
        String nonce = genRandomStr();

        String signature = SHA1.gen(this.token, timeStamp, nonce, encryptedXml);
        return generateXml(encryptedXml, signature, timeStamp, nonce);
    }

    /**
     * 对明文进行加密.
     *
     * @param plainText 需要加密的明文
     * @return 加密后base64编码的字符串
     */
    protected String encrypt(String randomStr, String plainText) {
        ByteGroup byteCollector = new ByteGroup();
        byte[] randomStringBytes = randomStr.getBytes(CHARSET);
        byte[] plainTextBytes = plainText.getBytes(CHARSET);
        byte[] bytesOfSizeInNetworkOrder = number2BytesInNetworkOrder(plainTextBytes.length);
        byte[] appIdBytes = this.appidOrCorpid.getBytes(CHARSET);

        // randomStr + networkBytesOrder + text + appid
        byteCollector.addBytes(randomStringBytes);
        byteCollector.addBytes(bytesOfSizeInNetworkOrder);
        byteCollector.addBytes(plainTextBytes);
        byteCollector.addBytes(appIdBytes);

        // ... + pad: 使用自定义的填充方式对明文进行补位填充
        byte[] padBytes = PKCS7Encoder.encode(byteCollector.size());
        byteCollector.addBytes(padBytes);

        // 获得最终的字节流, 未加密
        byte[] unencrypted = byteCollector.toBytes();

        try {
            // 设置加密模式为AES的CBC模式
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(this.aesKey, "AES");
            IvParameterSpec iv = new IvParameterSpec(this.aesKey, 0, 16);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);

            // 加密
            byte[] encrypted = cipher.doFinal(unencrypted);

            // 使用BASE64对加密后的字符串进行编码
            return BASE64.encodeToString(encrypted);
        } catch (Exception e) {
            throw new WeComException(e.getMessage());
        }
    }

    /**
     * 检验消息的真实性，并且获取解密后的明文.
     * <ol>
     * <li>利用收到的密文生成安全签名，进行签名验证</li>
     * <li>若验证通过，则提取xml中的加密消息</li>
     * <li>对消息进行解密</li>
     * </ol>
     *
     * @param msgSignature 签名串，对应URL参数的msg_signature
     * @param timeStamp    时间戳，对应URL参数的timestamp
     * @param nonce        随机串，对应URL参数的nonce
     * @param encryptedXml 密文，对应POST请求的数据
     * @return 解密后的原文
     */
    public String decrypt(String msgSignature, String timeStamp, String nonce, String encryptedXml) {
        // 密钥，公众账号的app corpSecret
        // 提取密文
        String cipherText = extractEncryptPart(encryptedXml);

        // 验证安全签名
        String signature = SHA1.gen(this.token, timeStamp, nonce, cipherText);
        if (!signature.equals(msgSignature)) {
            throw new WeComException("加密消息签名校验失败");
        }

        // 解密
        return decrypt(cipherText);
    }

    /**
     * 对密文进行解密.
     *
     * @param cipherText 需要解密的密文
     * @return 解密得到的明文
     */
    public String decrypt(String cipherText) {
        byte[] original;
        try {
            // 设置解密模式为AES的CBC模式
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(this.aesKey, "AES");
            IvParameterSpec iv = new IvParameterSpec(Arrays.copyOfRange(this.aesKey, 0, 16));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);

            // 使用BASE64对密文进行解码
            byte[] encrypted = Base64.decodeBase64(cipherText);

            // 解密
            original = cipher.doFinal(encrypted);
        } catch (Exception e) {
            throw new WeComException(e.getMessage());
        }

        String xmlContent;
        String fromAppid;
        try {
            // 去除补位字符
            byte[] bytes = PKCS7Encoder.decode(original);

            // 分离16位随机字符串,网络字节序和AppId
            byte[] networkOrder = Arrays.copyOfRange(bytes, 16, 20);

            int xmlLength = bytesNetworkOrder2Number(networkOrder);

            xmlContent = new String(Arrays.copyOfRange(bytes, 20, 20 + xmlLength), CHARSET);
            fromAppid = new String(Arrays.copyOfRange(bytes, 20 + xmlLength, bytes.length), CHARSET);
        } catch (Exception e) {
            throw new WeComException(e.getMessage());
        }

        // appid不相同的情况 暂时忽略这段判断
//    if (!fromAppid.equals(this.appidOrCorpid)) {
//      throw new WxRuntimeException("AppID不正确，请核实！");
//    }

        return xmlContent;

    }

    public String verifyURL(String msgSignature, String timeStamp, String nonce, String echoStr) throws WeComException {
        String signature = SHA1.getSHA1(this.token, timeStamp, nonce, echoStr);
        if (!signature.equals(msgSignature)) {
            throw new WeComException("签名验证错误");
        } else {
            return this.decrypt(echoStr);
        }
    }
}
