package us.betahouse.haetae.activity.dal.model;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

/**
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "younth_learning_record",
        indexes = {
            @Index(name = "uk_activity_record_id", columnList = "activity_record_id", unique = true)
        })
public class YouthLearningDO extends BaseDO {

    private static final long serialVersionUID = -3807346465128804876L;
    /**
     * 活动记录id
     */
    @Column(name = "activity_record_id", length = 32, updatable = false)
    private String activityRecordId;

    /**
     * 活动id
     */
    @Column(name = "activity_id", length = 32, nullable = false,updatable = false)
    private String activityId;

    /**
     * 状态
     */
    private String status;

    /**
     * 用户id
     */
    @Column(name = "user_id", length = 32, nullable = false,updatable = false)
    private String userId;

    /**
     * 记录者id
     */
    @Column(name = "scanner_user_id", length = 32, nullable = false)
    private String scannerUserId;

    /**
     * 类型
     */
    private String type;

    /**
     * 学期
     */
    private String term;

    /**
     * 完成时间
     */
    @Column(name = "finish_time", nullable = false,updatable = false)
    private Date finishTime;

    public String getActivityRecordId() {
        return activityRecordId;
    }

    public void setActivityRecordId(String activityRecordId) {
        this.activityRecordId = activityRecordId;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getScannerUserId() {
        return scannerUserId;
    }

    public void setScannerUserId(String scannerUserId) {
        this.scannerUserId = scannerUserId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }
}
