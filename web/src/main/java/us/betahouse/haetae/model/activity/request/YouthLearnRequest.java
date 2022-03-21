package us.betahouse.haetae.model.activity.request;

import us.betahouse.haetae.common.RestRequest;

import java.util.Date;

public class YouthLearnRequest extends RestRequest {
    private static final long serialVersionUID = 6611470606473009600L;

    /**
     * 活动记录id
     */
    private String activityRecordId;

    /**
     * 活动id
     */
    private String activityId;

    /**
     * 记录者id
     */
    private String scannerUserId;


    /**
     * 完成时间
     */
    private Long finishTime;

    /**
     * 青年大学习名称
     */
    private String ActivityName;

    /**
     * 学号
     */
    private String stuId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 第几页
     */
    private Integer page;

    /**
     * 每页条数
     */
    private Integer size;

    /**
     * 班级号
     */
    private String classId;

    public void setActivityRecordId(String activityRecordId) {
        this.activityRecordId = activityRecordId;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getScannerUserId() {
        return scannerUserId;
    }

    public void setScannerUserId(String scannerUserId) {
        this.scannerUserId = scannerUserId;
    }

    public Long getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Long finishTime) {
        this.finishTime = finishTime;
    }

    public String getActivityName() {
        return ActivityName;
    }

    public void setActivityName(String activityName) {
        ActivityName = activityName;
    }

    public String getStuId() {
        return stuId;
    }

    public void setStuId(String stuId) {
        this.stuId = stuId;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getActivityRecordId() {
        return activityRecordId;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }
}
