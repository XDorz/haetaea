/*
  betahouse.us
  CopyRight (c) 2012 - 2018
 */
package us.betahouse.haetae.activity.dal.model;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

/**
 * 活动实体
 *
 * @author MessiahJK
 * @version : ActivityDO.java 2018/11/17 1:43 MessiahJK
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "activity",
        indexes = {
                @Index(name = "uk_activity_id", columnList = "activity_id", unique = true)
        })
public class ActivityDO extends BaseDO {

    private static final long serialVersionUID = -6722889286261263968L;
    /**
     * 活动id
     */
    @Column(name = "activity_id", length = 32, updatable = false)
    private String activityId;

    /**
     * 活动名
     */
    @Column(name = "activity_name", length = 64)
    private String activityName;

    /**
     * 活动类型
     */
    @Column(length = 32)
    private String type;

    /**
     * 单位信息
     */
    @Column(name = "organization_message", length = 256)
    private String organizationMessage;

    /**
     * 活动地点
     */
    private String location;

    /**
     * 活动开始时间
     */
    private Date start;

    /**
     * 活动结束时间
     */
    private Date end;

    /**
     * 活动分数
     */
    private Long score;

    /**
     * 活动申请章数
     */
    @Column(name = "application_stamper")
    private Integer applicationStamper;

    /**
     * 活动描述
     */
    private String description;

    /**
     * 活动创建者
     */
    @Column(name = "user_id")
    private String userId;

    /**
     * 活动状态
     */
    private String state;

    /**
     * 活动学期
     */
    private String term;

    /**
     * 活动扫章时间开始
     */
    @Column(name = "activity_stamped_start")
    private Date activityStampedStart;

    /**
     * 活动扫章时间结束
     */
    @Column(name = "activity_stamped_end")
    private Date activityStampedEnd;

    /**
     * 审批通过的时间
     */
    private Date approvedTime;

    /**
     * 驳回原因
     */
    private String cancelReason;

    /**
     * 审批修改记录
     * 未修改为false
     * 修改为true
     */
    private Boolean modified;

    /**
     * 钉钉审批截图
     */
    @Column(name = "picture_url")
    private String pictureUrl;

    public Date getApprovedTime() {
        return approvedTime;
    }

    public void setApprovedTime(Date approvedTime) {
        this.approvedTime = approvedTime;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public Boolean getModified() {
        return modified;
    }

    public void setModified(Boolean modified) {
        this.modified = modified;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public Date getActivityStampedStart() {
        return activityStampedStart;
    }

    public void setActivityStampedStart(Date activityStampedStart) {
        this.activityStampedStart = activityStampedStart;
    }

    public Date getActivityStampedEnd() {
        return activityStampedEnd;
    }

    public void setActivityStampedEnd(Date activityStampedEnd) {
        this.activityStampedEnd = activityStampedEnd;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOrganizationMessage() {
        return organizationMessage;
    }

    public void setOrganizationMessage(String organizationMessage) {
        this.organizationMessage = organizationMessage;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public Long getScore() {
        return score;
    }

    public void setScore(Long score) {
        this.score = score;
    }

    public Integer getApplicationStamper() {
        return applicationStamper;
    }

    public void setApplicationStamper(Integer applicationStamper) {
        this.applicationStamper = applicationStamper;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }
}
