package us.betahouse.haetae.serviceimpl.activity.service;

import org.springframework.data.domain.Page;
import us.betahouse.haetae.activity.model.basic.YouthLearnBatchBO;
import us.betahouse.haetae.activity.model.basic.YouthLearningBO;
import us.betahouse.haetae.activity.model.common.PageList;
import us.betahouse.haetae.serviceimpl.activity.request.YouthLearningRequest;
import us.betahouse.haetae.serviceimpl.activity.service.impl.YouthLearningServiceImpl;

import java.util.List;

public interface YouthLearningService {

    boolean saveRecord(YouthLearningRequest request);

    /**
     * 批量保存
     *
     * @return
     */
    YouthLearningServiceImpl.Info batchSaveRecord(YouthLearningRequest request);

    /**
     * 批量删除记录
     *
     * @return
     */
    List<YouthLearningBO> batchDeleteRecord(YouthLearningRequest request);

    /**
     * 通过学生id得到记录
     *
     * @return
     */
    @Deprecated
    List<YouthLearningBO> getRecordByUserId(YouthLearningRequest request);

    /**
     * 通过学生id得到记录数量
     *
     * @return
     */
    @Deprecated
    Integer getRecordNumByUserId(YouthLearningRequest request);

    /**
     * 分页模糊查找
     * 目前仅支持按照学号和活动全称查找
     *
     *
     * @param request
     * @return
     */
    PageList<YouthLearningBO> getByActivityNameAndUserName(YouthLearningRequest request);

    /**
     * 按学期分类获得用户的青年大学习记录
     *
     * @param request
     * @return
     */
    List<YouthLearnBatchBO> getTermedRecordByUserId(YouthLearningRequest request);
}
