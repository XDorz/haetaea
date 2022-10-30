package us.betahouse.haetae.activity.dal.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import us.betahouse.haetae.activity.model.basic.ActivityRecordBO;
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
     * 通过学生id得到记录
     *
     * @return
     */
    List<YouthLearningBO> getRecordByUserIdAndTermAsc(String userId,String term);

    /**
     * 通过学生id与学期得到记录
     *
     * @param userId
     * @param term
     * @return
     */
    List<YouthLearningBO> getRecordByUserIdAndTerm(String userId,String term);

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
     * 获取某一青年大学习的学习信息
     */
    List<YouthLearningBO> getRecordByActivityId(String activityId);

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

    /**
     * 转换为ActivityRecordBO
     *
     * @param youthLearningBO
     * @return
     */
    ActivityRecordBO convertARB(YouthLearningBO youthLearningBO);
}
