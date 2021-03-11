package com.linkwechat.wecom.client;

import com.dtflys.forest.annotation.DataObject;
import com.dtflys.forest.annotation.Header;
import com.dtflys.forest.annotation.Query;
import com.dtflys.forest.annotation.Request;
import com.linkwechat.wecom.domain.dto.*;

/**
 * @description: 企业微信通讯录成员
 * @author: HaoN
 * @create: 2020-08-27 16:42
 **/
public interface WeUserClient {

    /**
     * 创建用户
     * @param weUserDto
     * @return
     */
    @Request(url="/user/create",
            type = "POST"
    )
    WeResultDto createUser(@DataObject WeUserDto weUserDto);


    /**
     * 根据用户账号,获取用户详情信息
     * @param userid
     * @return
     */
    @Request(url = "/user/get")
    WeUserDto getUserByUserId(@Query("userid") String userid);


    /**
     * 更新通讯录用户
     * @param weUserDto
     * @return
     */
    @Request(url="/user/update",
            type = "POST"
    )
    WeResultDto updateUser(@DataObject WeUserDto weUserDto);


    /**
     * 根据账号删除指定用户
     * @param userid
     * @return
     */
    @Request(url = "/user/delete")
    WeResultDto deleteUserByUserId(@Query("userid") String userid);


    /**
     *  获取部门成员
     * @param departmentId
     * @param fetchChild
     * @return
     */
    @Request(url="/user/list")
    WeUserListDto  list(@Query("department_id") Long departmentId,@Query("fetch_child") Integer fetchChild);


    /**
     * 分配客户
     * @return
     */
    @Request(url="/externalcontact/transfer",
            type = "POST"
    )
    WeResultDto allocateCustomer(@DataObject AllocateWeCustomerDto allocateWeCustomerDto);


    /**
     * 分配成员群
     * @return
     */
    @Request(url="/externalcontact/groupchat/transfer",
            type = "POST"
    )
    WeResultDto allocateGroup(@DataObject AllocateWeGroupDto allocateWeGroupDto);


    /**
     * 获取离职员工列表
     * @return
     */
    @Request(
            url = "/externalcontact/get_unassigned_list",
            type = "POST"
    )
    LeaveWeUserListsDto  leaveWeUsers();


    /**
     * 获取访问用户身份(内部应用)
     * @param code
     * @param agentId 应用的id,请求头中
     * @return
     */
    @Request(url = "/user/getuserinfo")
    WeUserInfoDto  getuserinfo(@Query("code")String code,@Header("agentId")String agentId);
}
