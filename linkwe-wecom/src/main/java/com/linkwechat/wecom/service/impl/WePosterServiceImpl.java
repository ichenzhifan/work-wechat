package com.linkwechat.wecom.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linkwechat.common.config.CosConfig;
import com.linkwechat.common.config.RuoYiConfig;
import com.linkwechat.common.config.ServerConfig;
import com.linkwechat.common.utils.file.FileUploadUtils;
import com.linkwechat.common.utils.file.FileUtils;
import com.linkwechat.common.utils.img.ImageUtils;
import com.linkwechat.common.utils.img.NetFileUtils;
import com.linkwechat.wecom.domain.WeCategory;
import com.linkwechat.wecom.domain.WePoster;
import com.linkwechat.wecom.domain.WePosterFont;
import com.linkwechat.wecom.domain.WePosterSubassembly;
import com.linkwechat.wecom.mapper.WePosterMapper;
import com.linkwechat.wecom.service.IWeCategoryService;
import com.linkwechat.wecom.service.IWePosterFontService;
import com.linkwechat.wecom.service.IWePosterService;
import com.linkwechat.wecom.service.IWePosterSubassemblyService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 海报字体
 *
 * @author ws
 */
@Service
public class WePosterServiceImpl extends ServiceImpl<WePosterMapper, WePoster> implements IWePosterService {

    @Resource
    private WePosterMapper wePosterMapper;

    @Resource
    private IWePosterSubassemblyService posterSubassemblyService;

    @Resource
    private IWePosterFontService posterFontService;

    @Resource
    private ServerConfig serverConfig;

    @Resource
    private IWeCategoryService weCategoryService;

    @Resource
    private CosConfig cosConfig;

    /**
     * 查询一条
     *
     * @param id
     * @return
     */
    @Override
    public WePoster selectOne(Long id) {
        WePoster poster = this.lambdaQuery()
                .eq(WePoster::getId, id)
                .eq(WePoster::getDelFlag, 0)
                .list().get(0);
        if (poster == null) {
            return null;
        }
        poster.setPosterSubassemblyList(posterSubassemblyService.lambdaQuery().eq(WePosterSubassembly::getPosterId, poster.getId()).list());
        if (!CollectionUtils.isEmpty(poster.getPosterSubassemblyList())) {

            List<Long> fontIdList = poster.getPosterSubassemblyList().stream().filter(wePosterSubassembly -> wePosterSubassembly.getFontId() != null).map(WePosterSubassembly::getFontId).distinct().collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(fontIdList)) {
                List<WePosterFont> fontList = posterFontService.lambdaQuery().in(WePosterFont::getId, fontIdList).list();
                if (!CollectionUtils.isEmpty(fontList)) {
                    Map<Long, WePosterFont> fontMap = fontList.stream().collect(Collectors.toMap(WePosterFont::getId, f -> f));
                    poster.getPosterSubassemblyList().stream().filter(wePosterSubassembly -> wePosterSubassembly.getFontId() != null).forEach(wePosterSubassembly -> {
                        WePosterFont font = fontMap.get(wePosterSubassembly.getFontId());
                        if (font != null) {
                            wePosterSubassembly.setFont(font);
                        }
                    });
                }
            }
        }
        /*if(poster.getCategoryId() != null){
            weCategoryService.findWeCategoryById(poster.getId());
        }*/

        return poster;
    }


    @Override
    public List<WePoster> list(Long categoryId, String name) {
        List<WePoster> fontList = this.lambdaQuery()
                .eq(WePoster::getDelFlag,0)
                .eq(categoryId != null,WePoster::getCategoryId,categoryId)
                .like(com.linkwechat.common.utils.StringUtils.isNotBlank(name),WePoster::getTitle,name)
                .orderByDesc(WePoster::getCreateTime)
                .list();
        return fontList;
    }

    /**
     * 生成海报图片地址
     *
     * @param poster
     * @return
     */
    @Override
    public String generateSimpleImg(WePoster poster) {
        if(CollectionUtils.isEmpty(poster.getPosterSubassemblyList())){
            poster.setSampleImgPath(poster.getBackgroundImgPath());
            return poster.getBackgroundImgPath();
        }
        Set<String> existFontId = new HashSet<>();
        Map<String, Font> fontMap = poster.getPosterSubassemblyList().stream().filter(wePosterSubassembly -> wePosterSubassembly.getType().equals(1))
                .peek(wePosterSubassembly -> {
                    if (wePosterSubassembly.getFontId() == null) {
                        wePosterSubassembly.setFontId(0L);
                    }
                })
                .filter(wePosterSubassembly -> {
                    if(existFontId.contains(wePosterSubassembly.getFontId()+"_"+wePosterSubassembly.getFontSize()+"_"+wePosterSubassembly.getFontStyle())){
                        return false;
                    }else {
                        existFontId.add(wePosterSubassembly.getFontId()+"_"+wePosterSubassembly.getFontSize()+"_"+wePosterSubassembly.getFontStyle());
                        return true;
                    }
                })
                .collect(Collectors.toMap(wePosterSubassembly -> {
                    return wePosterSubassembly.getFontId()+"_"+wePosterSubassembly.getFontSize()+"_"+wePosterSubassembly.getFontStyle();
                }, wePosterSubassembly -> posterFontService.getFont(wePosterSubassembly.getFontId(), wePosterSubassembly.getFontSize(),wePosterSubassembly.getFontStyle())));
        Map<String, NetFileUtils.FileCallable> fileCallableMap = poster.getPosterSubassemblyList().stream().map(WePosterSubassembly::getImgPath).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toMap(s->s, NetFileUtils::getNetFile));
        if (CollectionUtils.isEmpty(fileCallableMap)) {
            fileCallableMap = new HashMap<>();
        }
        fileCallableMap.put(poster.getBackgroundImgPath(), NetFileUtils.getNetFile(poster.getBackgroundImgPath()));
        Map<String, BufferedImage> bufferedImageMap = fileCallableMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, stringFileCallableEntry -> {
            try {
                return ImageUtils.copyBufferedImage(ImageIO.read(new ByteArrayInputStream(Objects.requireNonNull(NetFileUtils.getByteArrayOutputStream(stringFileCallableEntry.getValue(), false)).toByteArray())),BufferedImage.TYPE_INT_ARGB);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("图片读取错误");
            }
        }));
        BufferedImage backgroundImg = bufferedImageMap.get(poster.getBackgroundImgPath());
        poster.setWidth(backgroundImg.getWidth());
        poster.setHeight(backgroundImg.getHeight());
        poster.getPosterSubassemblyList().forEach(wePosterSubassembly -> {
            if (wePosterSubassembly.getType().equals(1)) {
                Font font = fontMap.get(wePosterSubassembly.getFontId()+"_"+wePosterSubassembly.getFontSize()+"_"+wePosterSubassembly.getFontStyle());
                FontMetrics fontMetrics = ImageUtils.getFontMetrics(font);
                Color color;
                if (StringUtils.isNotBlank(wePosterSubassembly.getFontColor())) {
                    color = ImageUtils.getColor(wePosterSubassembly.getFontColor(),wePosterSubassembly.getAlpha());
                } else {
                    color = Color.BLACK;
                }
                List<ImageUtils.LineText> lineTextList = ImageUtils.splitContext(wePosterSubassembly.getContent(), fontMetrics, wePosterSubassembly.getLeft(), wePosterSubassembly.getTop(), wePosterSubassembly.getWidth(), wePosterSubassembly.getHeight(),wePosterSubassembly.getWordSpace(),wePosterSubassembly.getLineSpace(), wePosterSubassembly.getFontTextAlign(),wePosterSubassembly.getVerticalType());
                lineTextList.forEach(lineText -> {
                    lineText.getCharTextList().forEach(charText -> {
                        ImageUtils.writeFontBufferedImage(backgroundImg, charText.getValue().toString(), charText.getPointX(), charText.getPointY(), font, color);
                    });

                });
            } else {
                BufferedImage bufferedImage = bufferedImageMap.get(wePosterSubassembly.getImgPath());
                if (wePosterSubassembly.getAlpha() != null && wePosterSubassembly.getAlpha() >= 0){
                    bufferedImage = ImageUtils.setBufferedImageAlpha(bufferedImage,wePosterSubassembly.getAlpha(),BufferedImage.TYPE_INT_ARGB);
                }
                bufferedImage = ImageUtils.fixedDimensionBufferedImage(bufferedImage, BufferedImage.TYPE_INT_ARGB, wePosterSubassembly.getWidth(), wePosterSubassembly.getHeight());
                if(wePosterSubassembly.getRotate() == null){
                    ImageUtils.mergeBufferedImage(backgroundImg, bufferedImage, wePosterSubassembly.getLeft(), wePosterSubassembly.getTop());
                }else {
                    int x = wePosterSubassembly.getLeft() + bufferedImage.getWidth()/2;
                    int y = wePosterSubassembly.getTop() + bufferedImage.getHeight()/2;
                    bufferedImage = ImageUtils.rotateImage(bufferedImage,wePosterSubassembly.getRotate());
                    x = x - bufferedImage.getWidth() / 2;
                    y = y - bufferedImage.getHeight() / 2;
                    ImageUtils.mergeBufferedImage(backgroundImg, bufferedImage, x, y);
                }
            }

        });
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(backgroundImg, "png", byteArrayOutputStream);
            NetFileUtils.StreamMultipartFile streamMultipartFile = new NetFileUtils.StreamMultipartFile(System.currentTimeMillis()+".jpg",byteArrayOutputStream.toByteArray());
            byteArrayOutputStream.close();
            String path = FileUploadUtils.upload2Cos(streamMultipartFile,cosConfig);
            poster.setSampleImgPath(cosConfig.getImgUrlPrefix() + path);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("图片生成错误");
        }

        return null;
    }
}
