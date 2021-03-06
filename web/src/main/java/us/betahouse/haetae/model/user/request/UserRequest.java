/*
  betahouse.us
  CopyRight (c) 2012 - 2018
 */
package us.betahouse.haetae.model.user.request;


import us.betahouse.haetae.common.RestRequest;

/**
 * 用户请求
 *
 * @author dango.yxm
 * @version : UserRequest.java 2018/11/21 8:45 PM dango.yxm
 */
public class UserRequest extends RestRequest {

    private static final long serialVersionUID = 4621666229903071688L;

    /**
     * 用户名称
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 学号
     */
    private String stuId;

    /**
     * 微信code
     */
    private String code;
    
    /**
     * 微信头像
     */
    private String avatarUrl;

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
     * 学期
     */
    private String term;

    /**
     * b
     */
    private String operatedId;

    /**
     * 每页条数
     */
    private Integer limit;

    /**
     * 页数
     */
    private Integer page;

    /**
     * 排序规则
     */
    private String orderRule;

    private boolean canStamp;

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public String getOrderRule() {
        return orderRule;
    }

    public void setOrderRule(String orderRule) {
        this.orderRule = orderRule;
    }

    public boolean isCanStamp() {
        return canStamp;
    }

    public void setCanStamp(boolean canStamp) {
        this.canStamp = canStamp;
    }

    public String getOperatedId() {
        return operatedId;
    }

    public void setOperatedId(String operatedId) {
        this.operatedId = operatedId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStuId() {
        return stuId;
    }

    public void setStuId(String stuId) {
        this.stuId = stuId;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }
}
