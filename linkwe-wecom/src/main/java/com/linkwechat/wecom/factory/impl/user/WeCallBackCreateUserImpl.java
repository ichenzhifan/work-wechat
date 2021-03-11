package com.linkwechat.wecom.factory.impl.user;

import com.linkwechat.wecom.domain.vo.WxCpXmlMessageVO;
import com.linkwechat.wecom.factory.WeEventStrategy;
import com.linkwechat.wecom.service.IWeUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author danmo
 * @description 新建外部联系人事件
 * @date 2021/1/20 22:19
 **/
@Slf4j
@Component("create_user")
public class WeCallBackCreateUserImpl extends WeEventStrategy {
    @Autowired
    private IWeUserService weUserService;

    @Override
    public void eventHandle(WxCpXmlMessageVO message) {
        try {
            weUserService.insertWeUserNoToWeCom(setWeUserData(message));
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }
}
