package us.betahouse.haetae.serviceimpl.activity.request;

import us.betahouse.haetae.activity.model.basic.YouthLearningBO;
import us.betahouse.haetae.serviceimpl.common.verify.VerifyRequest;
import us.betahouse.haetae.serviceimpl.user.request.PermRequest;

import java.util.Date;
import java.util.List;


public class YouthLearningRequest implements VerifyRequest {

    /**
     * 活动记录id
     */
    private String activityRecordId;

    /**
     * 活动id
     */
    private String activityId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 记录者id
     */
    private String scannerUserId;

    /**
     * 类型
     */
    private String type;

    /**
     * 状态
     */
    private String status;

    /**
     * 完成时间
     */
    private Date finishTime;

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
     * 页数
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 学号
     */
    private List<String> stuIds;

    /**
     * 班级号
     */
    private String classId;

    /**
     * 单个青年大学习导入
     */
    private YouthLearningBO youthLearningBO;

    /**
     * 多个青年大学习导入
     */
    private List<YouthLearningBO> youthLearningBOList;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
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

    public YouthLearningBO getYouthLearningBO() {
        return youthLearningBO;
    }

    public void setYouthLearningBO(YouthLearningBO youthLearningBO) {
        this.youthLearningBO = youthLearningBO;
    }

    public List<YouthLearningBO> getYouthLearningBOList() {
        return youthLearningBOList;
    }

    public void setYouthLearningBOList(List<YouthLearningBO> youthLearningBOList) {
        this.youthLearningBOList = youthLearningBOList;
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

    public List<String> getStuIds() {
        return stuIds;
    }

    public void setStuIds(List<String> stuIds) {
        this.stuIds = stuIds;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    @Override
    public String getVerifyUserId() {
        return getUserId();
    }
}
