package com.linkwechat.wecom.factory.impl;

import com.linkwechat.wecom.domain.vo.WxCpXmlMessageVO;
import com.linkwechat.wecom.factory.WeCallBackEventFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author danmo
 * @description 异步任务完成通知
 * @date 2020/11/9 14:40
 **/
@Service
@Slf4j
public class WeEventBatchJobResultImpl implements WeCallBackEventFactory {

    @Override
    public void eventHandle(WxCpXmlMessageVO message) {
        String jobType = message.getBatchJob().getJobType();
        switch (jobType){
            case "sync_user"://增量更新成员)
                break;
            case "replace_user"://全量覆盖成员
                break;
            case "invite_user"://邀请成员关注
                break;
            case "replace_party"://全量覆盖部门
                break;
        }
    }
}
