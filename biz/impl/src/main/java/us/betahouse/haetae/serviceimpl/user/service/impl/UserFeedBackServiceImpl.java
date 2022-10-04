package us.betahouse.haetae.serviceimpl.user.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import us.betahouse.haetae.serviceimpl.common.verify.VerifyPerm;
import us.betahouse.haetae.serviceimpl.user.constant.UserPermType;
import us.betahouse.haetae.serviceimpl.user.request.UserFeedBackRequest;
import us.betahouse.haetae.serviceimpl.user.service.UserFeedBackService;
import us.betahouse.haetae.user.dal.model.UserFeedBackDO;
import us.betahouse.haetae.user.dal.service.UserFeedBackRepoService;
import us.betahouse.haetae.user.model.basic.UserFeedBackBO;
import us.betahouse.haetae.user.model.common.PageList;

@Service
public class UserFeedBackServiceImpl implements UserFeedBackService {

    @Autowired
    UserFeedBackRepoService userFeedBackRepoService;

    @Override
    public PageList<UserFeedBackBO> getUserFeedBack(UserFeedBackRequest request) {
        return userFeedBackRepoService.getUserFeedBackByUserId(request.getUserId(),request.getPageable());
    }

    @Override
    @VerifyPerm(permType = UserPermType.USER_FEEDBACK_MANAGER)
    public PageList<UserFeedBackBO> getFeedBackByUserId(UserFeedBackRequest request) {
        return userFeedBackRepoService.getUserFeedBackByUserId(request.getUserId(),request.getPageable());
    }

    @Override
    @VerifyPerm(permType = UserPermType.USER_FEEDBACK_MANAGER)
    public PageList<UserFeedBackBO> getAllFeedBack(UserFeedBackRequest request) {
        return userFeedBackRepoService.getAllFeedBack(request.getPageable());
    }

    @Override
    @VerifyPerm(permType = UserPermType.USER_FEEDBACK_MANAGER)
    public PageList<UserFeedBackBO> getAllFeedBackByVersion(UserFeedBackRequest request) {
        return userFeedBackRepoService.getAllFeedBackByVersion(request.getVersion(),request.getPageable());
    }

    @Override
    public void save(UserFeedBackRequest request) {
        userFeedBackRepoService.save(request.getUserFeedBackDO());
    }
}
