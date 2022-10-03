package us.betahouse.haetae.activity.dal.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import us.betahouse.haetae.activity.dal.model.YouthLearningDO;

import java.util.List;

@Repository
public interface YouthLearningDORepo extends JpaRepository<YouthLearningDO,Long> {

    /**
     * 根据学生id查找该生做了那几期
     *
     * @param userId
     * @return
     */
    List<YouthLearningDO> findAllByUserIdAndStatusOrderByFinishTimeDesc(String userId,String status);

    /**
     * 根据活动id查找
     *
     * @param activityId
     * @return
     */
    List<YouthLearningDO> findAllByActivityId(String activityId);

    /**
     * 根据学生id查找该生做了那几期
     *
     * @param userId
     * @return
     */
    List<YouthLearningDO> findAllByUserIdAndStatusAndTermOrderByFinishTimeDesc(String userId,String status,String term);

    /**
     * 根据学生id查找该生做了那几期
     *
     * @param userId
     * @return
     */
    List<YouthLearningDO> findAllByUserIdAndStatusAndTermOrderByFinishTimeAsc(String userId,String status,String term);

    /**
     * 根据活动查找有哪些学生做了该期
     *
     * @param activityId
     * @return
     */
    List<YouthLearningDO> findAllByActivityIdAndStatusOrderByFinishTimeDesc(String activityId,String status);

    /**
     * 根据活动id和学生id查找
     *
     * @param activityId
     * @param userId
     * @return
     */
    List<YouthLearningDO> findAllByActivityIdAndUserId(String activityId,String userId);

    /**
     * 根据id查找活动记录
     *
     * @param activityRecordId
     * @return
     */
    YouthLearningDO findAllByActivityRecordId(String activityRecordId);

    /**
     * 通过活动和用户id查找
     *
     * @param activityId
     * @param userId
     * @return
     */
    List<YouthLearningDO> findAllByActivityIdAndUserIdAndStatus(String activityId,String userId,String status);

    /**
     * 查询是否存在记录
     *
     * @param activityId
     * @param userId
     * @return
     */
    boolean existsByActivityIdAndUserId(String activityId,String userId);

    /**
     * 按学期查询青年大学习活动
     *
     * @param term
     * @return
     */
    boolean findAllByTermAndStatus(String term,String status);

    /**
     * 根据学生id查找该生做了多少期
     *
     * @param userId
     * @return
     */
    @Query(value = "select count(1) from younth_learning_record where user_id=?1 and status=?2",nativeQuery = true)
    Integer getNumByUserId(String userId,String status);

    /**
     * 删除一条记录
     *
     * @param recordId
     * @param status
     * @return
     */
    @Modifying
    @Query(value = "update younth_learning_record set status=?2 where activity_record_id=?1",nativeQuery = true)
    Integer deleteByActivityRecordId(String recordId,String status);

    /**
     * 根据用户id和活动id模糊查找
     *
     * @param pageable
     * @param activityId
     * @param userId
     * @return
     */
    @Query(value = "select * from younth_learning_record where activity_id like concat('%',?1,'%') and user_id like concat('%',?2,'%')",nativeQuery = true)
    Page<YouthLearningDO> findAllByActivityIdContainsAndUserIdContains(Pageable pageable,String activityId,String userId);

    /**
     * 根据用户id和活动id模糊查找
     *
     * @param pageable
     * @param activityId
     * @param userId
     * @return
     */
    @Query(value = "select * from younth_learning_record where activity_id like concat('%',?1,'%') and user_id like concat('%',?2,'%') and user_id in (?3)",nativeQuery = true)
    Page<YouthLearningDO> findAllByActivityIdContainsAndUserIdContainsAndClassIdIn(Pageable pageable,String activityId,String userId,List<String> userIds);

    @Query(value = "select user_id from younth_learning_record group by user_id",nativeQuery = true)
    List<String> getAllUserId();
}
