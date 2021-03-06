/*
  betahouse.us
  CopyRight (c) 2012 - 2018
 */
package us.betahouse.haetae.activity.model.basic;

import org.apache.commons.lang.StringUtils;
import us.betahouse.haetae.activity.enums.ActivityStateEnum;
import us.betahouse.haetae.activity.enums.ActivityTypeEnum;
import us.betahouse.util.common.ToString;
import us.betahouse.util.utils.DateUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 活动领域对象
 *
 * @author dango.yxm
 * @version : ActivityBO.java 2018/11/15 下午2:16 dango.yxm
 */
public class ActivityBO extends ToString {

    private static final long serialVersionUID = 6803113655032865227L;
    /**
     * 活动id
     */
    private String activityId;

    /**
     * 活动名
     */

    private String activityName;

    /**
     * 活动类型
     */

    private String type;

    /**
     * 单位信息
     */

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
    private Integer applicationStamper;

    /**
     * 活动描述
     */
    private String description;

    /**
     * 活动创建者
     */
    private String creatorId;

    /**
     * 活动状态
     *
     * @see ActivityStateEnum
     */
    private String state;

    /**
     * 活动学期
     */
    private String term;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 学号
     */
    private String stuId;

    /**
     * 活动盖章开始时间
     */
    private Date activityStampedStart;

    /**
     * 活动盖章结束时间
     */
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
     */
    private Boolean modified;

    /**
     * 实际扫章数
     */
    private int actualStamper;

    /**
     * 扫章偏差百分比
     */
    private double stamperPercentageDeviation;


    /**
     * 钉钉审批截图
     */
    private String pictureUrl;

    /**
     * 该活动的负责人是否可以导章
     */
    private boolean canStamp;

    /**
     * 拓展信息
     */
    private Map<String, String> extInfo = new HashMap<>();

    public String fetchExtInfo(String key) {
        if (extInfo == null) {
            return null;
        }
        return extInfo.get(key);
    }

    public void putExtInfo(String key, String value) {
        if (extInfo == null) {
            extInfo = new HashMap<>();
        }
        extInfo.put(key, value);
    }

    /**
     * 判断是否能盖章
     *
     * @return
     */
    public boolean canStamp() {
        // 活动重启 直接认为可以盖章
        if (StringUtils.equals(state, ActivityStateEnum.RESTARTED.getCode())) {
            return true;
        }
        ActivityTypeEnum activityTypeEnum = ActivityTypeEnum.getByCode(type);
        if (activityTypeEnum == null) {
            return false;
        }
        Date actStart= start;
        Date actEnd=end;
        if(activityStampedStart!=null&&activityStampedEnd!=null){
            actStart=activityStampedStart;
            actEnd=activityStampedEnd;
        }
        switch (activityTypeEnum) {
            case VOLUNTEER_WORK:
                return true;
            case PRACTICE_ACTIVITY:
                return true;
            case VOLUNTEER_ACTIVITY:
                return StringUtils.equals(state, ActivityStateEnum.PUBLISHED.getCode()) && DateUtil.nowIsBetween(actStart,actEnd);
            case SCHOOL_ACTIVITY:
                return StringUtils.equals(state, ActivityStateEnum.PUBLISHED.getCode()) && DateUtil.nowIsBetween(actStart,actEnd);
            case LECTURE_ACTIVITY:
                return StringUtils.equals(state, ActivityStateEnum.PUBLISHED.getCode()) && DateUtil.nowIsBetween(actStart,actEnd);
            default:
                return false;
        }
    }

    /**
     * 判断是否可以结束
     *
     * @return
     */
    public boolean canFinish() {
        // 活动手动重启 系统不会去结束
        if (StringUtils.equals(state, ActivityStateEnum.RESTARTED.getCode())) {
            return false;
        }
        ActivityTypeEnum activityTypeEnum = ActivityTypeEnum.getByCode(type);
        if (activityTypeEnum == null) {
            return false;
        }
        switch (activityTypeEnum) {
            case VOLUNTEER_WORK:
                // 义工都不要结束
                return false;
            case PRACTICE_ACTIVITY:
                // 实践不要结束
                return false;
            case VOLUNTEER_ACTIVITY:
                return StringUtils.equals(state, ActivityStateEnum.PUBLISHED.getCode()) && end.before(new Date());
            case SCHOOL_ACTIVITY:
                return StringUtils.equals(state, ActivityStateEnum.PUBLISHED.getCode()) && end.before(new Date());
            case LECTURE_ACTIVITY:
                return StringUtils.equals(state, ActivityStateEnum.PUBLISHED.getCode()) && end.before(new Date());
            default:
                return false;
        }
    }

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

    public int getActualStamper() {
        return actualStamper;
    }

    public void setActualStamper(int actualStamper) {
        this.actualStamper = actualStamper;
    }

    public double getStamperPercentageDeviation() {
        return stamperPercentageDeviation;
    }

    public void setStamperPercentageDeviation(double stamperPercentageDeviation) {
        this.stamperPercentageDeviation = stamperPercentageDeviation;
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

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
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

    public String getStuId() {
        return stuId;
    }

    public void setStuId(String stuId) {
        this.stuId = stuId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map<String, String> getExtInfo() {
        return extInfo;
    }

    public boolean isCanStamp() {
        return canStamp;
    }

    public void setCanStamp(boolean canStamp) {
        this.canStamp = canStamp;
    }

    public void setExtInfo(Map<String, String> extInfo) {
        this.extInfo = extInfo;
    }
}
