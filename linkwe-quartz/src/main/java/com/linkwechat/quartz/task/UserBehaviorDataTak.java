package com.linkwechat.quartz.task;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dtflys.forest.exceptions.ForestRuntimeException;
import com.linkwechat.common.constant.WeConstans;
import com.linkwechat.common.utils.bean.BeanUtils;
import com.linkwechat.wecom.client.WeCustomerClient;
import com.linkwechat.wecom.domain.WeUser;
import com.linkwechat.wecom.domain.WeUserBehaviorData;
import com.linkwechat.wecom.domain.dto.UserBehaviorDataDto;
import com.linkwechat.wecom.domain.query.UserBehaviorDataQuery;
import com.linkwechat.wecom.service.IWeUserBehaviorDataService;
import com.linkwechat.wecom.service.IWeUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author sxw
 * @description 联系客户统计
 * @date 2021/2/24 0:41
 **/
@Slf4j
@Component("UserBehaviorDataTak")
public class UserBehaviorDataTak {
    @Autowired
    private WeCustomerClient weCustomerClient;
    @Autowired
    private IWeUserService weUserService;
    @Autowired
    private IWeUserBehaviorDataService weUserBehaviorDataService;

    private final int offset = 500;

    public void getUserBehaviorData() {
        log.info("联系客户统计>>>>>>>>>>>>>>>>>>>启动");
        LambdaQueryWrapper<WeUser> wrapper = new LambdaQueryWrapper<WeUser>().eq(WeUser::getIsActivate, WeConstans.WE_USER_IS_ACTIVATE);
        int userCount = weUserService.count(wrapper);
        log.info("联系客户统计>>>>>>>>>>>>>>>>>>>userCount：{}",userCount);
        double num = 1;
        if (userCount > offset) {
            num = Math.ceil((double) userCount / offset);
        }
        int temp = 0;
        for (int i = 0; i < num; i++) {
            wrapper.last("limit " + temp + "," + offset);
            List<WeUser> list = weUserService.list(wrapper);
            if (CollectionUtil.isNotEmpty(list)) {
                List<WeUserBehaviorData> dataList = new ArrayList<>();
                UserBehaviorDataQuery query = new UserBehaviorDataQuery();
                //前一天的数据
                Long startTime = strToDate(-1, 0);
                Long endTime = strToDate(-1, 1);
                query.setStart_time(startTime);
                query.setEnd_time(endTime);
                list.forEach(weUser -> {
                    List<String> idList = new ArrayList<>();
                    idList.add(weUser.getUserId());
                    query.setUserid(idList);
                    try {
                        UserBehaviorDataDto userBehaviorData = weCustomerClient.getUserBehaviorData(query);
                        List<UserBehaviorDataDto.BehaviorData> behaviorDataList = userBehaviorData.getBehaviorData();
                        for (UserBehaviorDataDto.BehaviorData data : behaviorDataList) {
                            WeUserBehaviorData weUserBehaviorData = new WeUserBehaviorData();
                            BeanUtils.copyPropertiesignoreOther(data, weUserBehaviorData);
                            weUserBehaviorData.setUserId(weUser.getUserId());
                            dataList.add(weUserBehaviorData);
                        }
                    } catch (ForestRuntimeException e) {
                        e.printStackTrace();
                        log.error("员工数据拉取失败: userId:【{}】,ex:【{}】",weUser.getUserId(),e.getStackTrace());
                    }
                });
                weUserBehaviorDataService.saveBatch(dataList);
            }
            temp += offset;
        }

    }

    private Long strToDate(int days, Integer type) {
        Long time = null;
        DateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        Calendar cale = Calendar.getInstance();
        cale.add(Calendar.DATE, days);
        String tarday = new SimpleDateFormat("yyyy-MM-dd").format(cale.getTime());
        if (type.equals(0)) {
            tarday += " 00:00:00";
        } else {
            tarday += " 23:59:59";
        }
        // String转Date
        try {
            date = format2.parse(tarday);
            System.out.println(date.getTime());
            time = date.getTime() / 1000;
            System.out.println(time.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }
}
