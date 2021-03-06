/*
  betahouse.us
  CopyRight (c) 2012 - 2018
 */
package us.betahouse.haetae.user.model.basic;

import org.hibernate.validator.constraints.NotBlank;
import us.betahouse.util.common.ToString;

import javax.persistence.Column;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dango.yxm
 * @version : UserInfoBO.java 2018/11/17 下午8:10 dango.yxm
 */
public class UserInfoBO extends ToString {

    private static final long serialVersionUID = 835342972654081721L;

    /**
     * 用户信息id
     */
    private String userInfoId;

    /**
     * 用户id
     */
    @NotBlank
    private String userId;
    
    /**
     * 学号
     */
    private String stuId;

    /**
     * 姓名
     */
    private String realName;

    /**
     * 性别
     */
    private String sex;

    /**
     * 专业号
     */
    private String major;

    /**
     * 班级号
     */
    private String classId;

    /**
     * 年级
     */
    private String grade;
    /**
     * 入学年份
     */
    private Date enrollDate;
    
    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 活动章数
     */
    private int activityChapterNum;

    /**
     * 讲座章数
     */
    private int lectureChapterNum;

    /**
     * 实践次数
     */
    private int practiceTimes;

    /**
     * 证书数
     */
    private int certificatesNum;


    /**
     * 拓展信息
     */
    private Map<String, String> extInfo = new HashMap<>();

    public int getActivityChapterNum() {
        return activityChapterNum;
    }

    public void setActivityChapterNum(int activityChapterNum) {
        this.activityChapterNum = activityChapterNum;
    }

    public int getLectureChapterNum() {
        return lectureChapterNum;
    }

    public void setLectureChapterNum(int lectureChapterNum) {
        this.lectureChapterNum = lectureChapterNum;
    }

    public int getPracticeTimes() {
        return practiceTimes;
    }

    public void setPracticeTimes(int practiceTimes) {
        this.practiceTimes = practiceTimes;
    }

    public int getCertificatesNum() {
        return certificatesNum;
    }

    public void setCertificatesNum(int certificatesNum) {
        this.certificatesNum = certificatesNum;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public String getUserInfoId() {
        return userInfoId;
    }

    public void setUserInfoId(String userInfoId) {
        this.userInfoId = userInfoId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public Date getEnrollDate() {
        return enrollDate;
    }

    public void setEnrollDate(Date enrollDate) {
        this.enrollDate = enrollDate;
    }

    public Map<String, String> getExtInfo() {
        return extInfo;
    }

    public void setExtInfo(Map<String, String> extInfo) {
        this.extInfo = extInfo;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }
}
