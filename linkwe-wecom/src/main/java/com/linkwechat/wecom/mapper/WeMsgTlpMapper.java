package com.linkwechat.wecom.mapper;

import java.util.List;
import com.linkwechat.wecom.domain.WeMsgTlp;
import org.apache.ibatis.annotations.Param;

/**
 * 欢迎语模板Mapper接口
 * 
 * @author ruoyi
 * @date 2020-10-04
 */
public interface WeMsgTlpMapper 
{
    /**
     * 查询欢迎语模板
     * 
     * @param id 欢迎语模板ID
     * @return 欢迎语模板
     */
    public WeMsgTlp selectWeMsgTlpById(Long id);

    /**
     * 查询欢迎语模板列表
     * 
     * @param weMsgTlp 欢迎语模板
     * @return 欢迎语模板集合
     */
    public List<WeMsgTlp> selectWeMsgTlpList(WeMsgTlp weMsgTlp);

    /**
     * 新增欢迎语模板
     * 
     * @param weMsgTlp 欢迎语模板
     * @return 结果
     */
    public int insertWeMsgTlp(WeMsgTlp weMsgTlp);

    /**
     * 修改欢迎语模板
     * 
     * @param weMsgTlp 欢迎语模板
     * @return 结果
     */
    public int updateWeMsgTlp(WeMsgTlp weMsgTlp);

    /**
     * 删除欢迎语模板
     * 
     * @param id 欢迎语模板ID
     * @return 结果
     */
    public int deleteWeMsgTlpById(Long id);

    /**
     * 批量删除欢迎语模板
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteWeMsgTlpByIds(Long[] ids);


    /**
     * 通过模板id删除
     * @param msgTlpIds
     * @return
     */
    public int batchRemoveByids(@Param("msgTlpIds") List<Long> msgTlpIds);
}
