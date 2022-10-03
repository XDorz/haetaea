package us.betahouse.haetae.serviceimpl.user.service;

import org.springframework.data.domain.Pageable;
import us.betahouse.haetae.serviceimpl.user.request.UserFeedBackRequest;
import us.betahouse.haetae.user.dal.model.UserFeedBackDO;
import us.betahouse.haetae.user.model.basic.UserFeedBackBO;
import us.betahouse.haetae.user.model.common.PageList;

public interface UserFeedBackService {

    /**
     * 查找该用户的所有反馈
     */
    PageList<UserFeedBackBO> getUserFeedBack(UserFeedBackRequest request);

    /**
     * 查找用户的所有反馈(鉴权)
     */
    PageList<UserFeedBackBO> getFeedBackByUserId(UserFeedBackRequest request);

    /**
     * 查看所有反馈
     */
    PageList<UserFeedBackBO> getAllFeedBack(UserFeedBackRequest request);

    /**
     * 查看某个版本的所有反馈
     */
    PageList<UserFeedBackBO> getAllFeedBackByVersion(UserFeedBackRequest request);

    /**
     * 保存反馈
     */
    void save(UserFeedBackRequest request);

}
