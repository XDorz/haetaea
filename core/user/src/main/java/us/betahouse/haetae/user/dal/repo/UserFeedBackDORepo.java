package us.betahouse.haetae.user.dal.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import us.betahouse.haetae.user.dal.model.UserFeedBackDO;

@Repository
public interface UserFeedBackDORepo extends JpaRepository<UserFeedBackDO, Long> {

    //由于评论反馈一般不会太长，故不再做递归查找完整评论链

    /**
     * 通过id查找
     */
    UserFeedBackDO findAllByFeedBackId(String feedId);

    /**
     * 查找某个用户的所有反馈
     */
    Page<UserFeedBackDO> findAllByUserId(String userId,Pageable pageable);

    /**
     * 评论更新nextId
     */
    @Modifying
    @Query(value = "update user_feedback set feedback_next_id=?2 where feedback_id=?1",nativeQuery = true)
    void  updateNextIdByFeedBackId(String feedBackId,String nextId);

    /**
     * 查找所有回馈
     */
    @Query(value = "select * from user_feedback where feedback_head_id=null",nativeQuery = true)
    Page<UserFeedBackDO> findAllFeedBack(Pageable pageable);

    /**
     * 通过当前版本查找
     */
    @Query(value = "select * from user_feedback where feedback_head_id=null and app_version=?1",nativeQuery = true)
    Page<UserFeedBackDO> findAllFeedBackByVersion(String version,Pageable pageable);

}
