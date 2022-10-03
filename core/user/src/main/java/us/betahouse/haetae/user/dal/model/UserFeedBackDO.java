package us.betahouse.haetae.user.dal.model;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_feedback",
        indexes = {
                @Index(name = "uk_feedback_id", columnList = "feedback_id", unique = true),
                @Index(name = "uk_feedback_next_id", columnList = "feedback_next_id"),
                @Index(name = "uk_user_id", columnList = "user_id"),
        }
)

public class UserFeedBackDO extends BaseDO{

    /**
     * 用户反馈id
     */
    @Column(name = "feedback_id", length = 32, updatable = false, nullable = false)
    private String feedBackId;

    /**
     * 反馈标题，回评的话为null
     */
    @Column(name = "title", length = 32)
    private String title;

    /**
     * 反馈内容
     */
    private String context;

    /**
     * 反馈的用户id
     */
    @Column(name = "user_id", length = 32, updatable = false, nullable = false)
    private String userId;

    /**
     * 下一个评论的id(暂定？)
     */
    @Column(name = "feedback_next_id", length = 32, updatable = false, nullable = false)
    private String feedBackNext;

    /**
     * 反馈的头id(暂定？)
     */
    @Column(name = "feedback_head_id", length = 32, updatable = false)
    private String feedBackHead;

    /**
     * 反馈时的小程序版本
     */
    @Column(name = "app_version", length = 10, updatable = false,nullable = false)
    private String version;

    /**
     * 额外信息
     */
    @Column(name = "ext_info", length = 2000)
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
