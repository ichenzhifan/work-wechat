package com.linkwechat.wecom.service;


import com.linkwechat.wecom.domain.WeTaskFissionCompleteRecord;

import java.util.List;

/**
 * 裂变任务完成记录Service接口
 *
 * @author ruoyi
 * @date 2021-01-27
 */
public interface IWeTaskFissionCompleteRecordService {
    /**
     * 查询裂变任务完成记录
     *
     * @param id 裂变任务完成记录ID
     * @return 裂变任务完成记录
     */
    public WeTaskFissionCompleteRecord selectWeTaskFissionCompleteRecordById(Long id);

    /**
     * 查询裂变任务完成记录列表
     *
     * @param weTaskFissionCompleteRecord 裂变任务完成记录
     * @return 裂变任务完成记录集合
     */
    public List<WeTaskFissionCompleteRecord> selectWeTaskFissionCompleteRecordList(WeTaskFissionCompleteRecord weTaskFissionCompleteRecord);

    /**
     * 新增裂变任务完成记录
     *
     * @param weTaskFissionCompleteRecord 裂变任务完成记录
     * @return 结果
     */
    public int insertWeTaskFissionCompleteRecord(WeTaskFissionCompleteRecord weTaskFissionCompleteRecord);

    /**
     * 修改裂变任务完成记录
     *
     * @param weTaskFissionCompleteRecord 裂变任务完成记录
     * @return 结果
     */
    public int updateWeTaskFissionCompleteRecord(WeTaskFissionCompleteRecord weTaskFissionCompleteRecord);

    /**
     * 批量删除裂变任务完成记录
     *
     * @param ids 需要删除的裂变任务完成记录ID
     * @return 结果
     */
    public int deleteWeTaskFissionCompleteRecordByIds(Long[] ids);

    /**
     * 删除裂变任务完成记录信息
     *
     * @param id 裂变任务完成记录ID
     * @return 结果
     */
    public int deleteWeTaskFissionCompleteRecordById(Long id);
}
