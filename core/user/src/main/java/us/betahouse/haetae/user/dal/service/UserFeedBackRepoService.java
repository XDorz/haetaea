package us.betahouse.haetae.user.dal.service;

import org.springframework.data.domain.Pageable;
import us.betahouse.haetae.user.dal.model.UserFeedBackDO;
import us.betahouse.haetae.user.model.basic.UserFeedBackBO;
import us.betahouse.haetae.user.model.common.PageList;

public interface UserFeedBackRepoService {

    /**
     * 查找用户的所有反馈
     */
    PageList<UserFeedBackBO> getUserFeedBackByUserId(String userId,Pageable pageable);

    /**
     * 查看所有反馈
     */
    PageList<UserFeedBackBO> getAllFeedBack(Pageable pageable);

    /**
     * 查看某个版本的所有反馈
     */
    PageList<UserFeedBackBO> getAllFeedBackByVersion(String version,Pageable pageable);

    /**
     * 保存反馈
     */
    void save(UserFeedBackDO userFeedBackDO);
}
