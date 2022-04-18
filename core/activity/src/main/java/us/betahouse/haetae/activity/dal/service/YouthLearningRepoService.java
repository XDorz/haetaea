package us.betahouse.haetae.activity.dal.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.betahouse.haetae.activity.model.basic.YouthLearningBO;
import us.betahouse.haetae.activity.model.common.PageList;

import java.util.List;

public interface YouthLearningRepoService {

    boolean saveRecord(YouthLearningBO youthLearningBO);

    /**
     * 批量保存
     *
     * @return
     */
    List<YouthLearningBO> batchSaveRecord(List<YouthLearningBO> list);

    /**
     * 批量删除记录
     *
     * @return
     */
    List<YouthLearningBO> batchDeleteRecord(List<YouthLearningBO> list);

    /**
     * 通过学生id得到记录
     *
     * @return
     */
    List<YouthLearningBO> getRecordByUserId(String userId);

    /**
     * 通过学生id得到记录数量
     *
     * @return
     */
    Integer getRecordNumByUserId(String userId);

    /**
     * 去重
     *
     * @param original
     * @return
     */
    List<YouthLearningBO> removeRepeat(List<YouthLearningBO> original);

    /**
     * 活动名和姓名模糊查找
     *
     * @return
     */
    PageList<YouthLearningBO> getByActivityNameAndRealName(Pageable pageable, String activityId, String userId);

    /**
     * 活动名和姓名班级模糊查找
     *
     * @param pageable
     * @param activityId
     * @param userId
     * @return
     */
    PageList<YouthLearningBO> getByActivityNameAndRealNameAndClassId(Pageable pageable, String activityId, String userId,List<String> userIds);

    /**
     * 判断是否存在记录
     *
     *
     * @return
     */
    boolean exitByActivityNameAndUserId(String activityId,String userId);
}
