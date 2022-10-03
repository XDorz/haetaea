package us.betahouse.haetae.user.model.basic;

import us.betahouse.haetae.user.dal.model.UserFeedBackDO;
import us.betahouse.haetae.user.dal.model.UserInfoDO;
import us.betahouse.util.common.ToString;

import java.util.List;

public class UserFeedBackBO extends ToString {

    private static final long serialVersionUID = -5436507905336231963L;

    /**
     * 反馈的id
     */
    private String feedBackId;

    /**
     * 反馈标题，回评的话为null
     */
    private String title;

    /**
     * 反馈内容
     */
    private String context;

    /**
     * 反馈的用户信息
     */
    private UserInfoDO userInfo;

    /**
     * 下一个评论的id(暂定？)
     */
    private String feedBackNext;

    /**
     * 反馈的头id(暂定？)
     */
    private String feedBackHead;

    /**
     * 反馈时所用的版本
     */
    private String version;

    /**
     * 完整回馈链(暂定？)
     */
    private List<UserFeedBackDO> feedBackChain;

    /**
     * 额外信息
     */
    private String extInfo;

    public String getFeedBackId() {
        return feedBackId;
    }

    public void setFeedBackId(String feedBackId) {
        this.feedBackId = feedBackId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public UserInfoDO getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfoDO userInfo) {
        this.userInfo = userInfo;
    }

    public String getFeedBackNext() {
        return feedBackNext;
    }

    public void setFeedBackNext(String feedBackNext) {
        this.feedBackNext = feedBackNext;
    }

    public String getFeedBackHead() {
        return feedBackHead;
    }

    public void setFeedBackHead(String feedBackHead) {
        this.feedBackHead = feedBackHead;
    }

    public List<UserFeedBackDO> getFeedBackChain() {
        return feedBackChain;
    }

    public void setFeedBackChain(List<UserFeedBackDO> feedBackChain) {
        this.feedBackChain = feedBackChain;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getExtInfo() {
        return extInfo;
    }

    public void setExtInfo(String extInfo) {
        this.extInfo = extInfo;
    }
}
