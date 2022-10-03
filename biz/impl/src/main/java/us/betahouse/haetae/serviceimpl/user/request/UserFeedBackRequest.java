package us.betahouse.haetae.serviceimpl.user.request;

import org.springframework.data.domain.Pageable;
import us.betahouse.haetae.serviceimpl.common.verify.VerifyRequest;
import us.betahouse.haetae.user.dal.model.UserFeedBackDO;

public class UserFeedBackRequest implements VerifyRequest {

    private String userId;

    private Pageable pageable;

    private UserFeedBackDO userFeedBackDO;

    private String version;

    private String targetId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVerifyUserId() {
        return userId;
    }

    public Pageable getPageable() {
        return pageable;
    }

    public void setPageable(Pageable pageable) {
        this.pageable = pageable;
    }

    public UserFeedBackDO getUserFeedBackDO() {
        return userFeedBackDO;
    }

    public void setUserFeedBackDO(UserFeedBackDO userFeedBackDO) {
        this.userFeedBackDO = userFeedBackDO;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }
}
