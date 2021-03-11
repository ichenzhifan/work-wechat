package com.linkwechat.wecom.domain;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @description: 客户画像实体VO
 * @author: HaoN
 * @create: 2021-03-03 11:49
 **/
@Data
public class WeCustomerPortrait {
    //外部联系人id
    private String externalUserid;
    //企业员工id
    private String userId;
    //客户与企业员工关系id
    private String flowerCustomerRelId;
    //客户昵称
    private String name;
    //客户备注
    private String remark;
    //备注客户手机号
    private String remarkMobiles;
    //客户生日
    private Date birthday;
    //邮箱
    private String email;
    //地址
    private String address;
    //qq
    private String qq;
    //职业
    private String position;
    //公司
    private String remarkCorpName;
    //描述
    private String description;

    //客户标签
    private List<WeTagGroup> weTagGroupList;

    //客户社交关系
    private WeCustomerSocialConn socialConn;

}
