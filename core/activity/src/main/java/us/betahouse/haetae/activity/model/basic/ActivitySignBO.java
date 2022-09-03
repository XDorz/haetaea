package us.betahouse.haetae.activity.model.basic;

public class ActivitySignBO {
    /**
     * 单位信息
     */

    private String organizationId;

    /**
     * 单位信息
     */
    private String organizationMessage;

    /**
     * 过去一个月内所有发起了报名的活动的实际参与的人数
     */
    private Integer ActualNumPastMonth;

    /**
     * 过去一个月内所有发起了报名的活动的报名总人数
     */
    private Integer SignNumPastMonth;

    public ActivitySignBO() {
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationMessage() {
        return organizationMessage;
    }

    public void setOrganizationMessage(String organizationMessage) {
        this.organizationMessage = organizationMessage;
    }

    public Integer getActualNumPastMonth() {
        return ActualNumPastMonth;
    }

    public void setActualNumPastMonth(Integer actualNumPastMonth) {
        ActualNumPastMonth = actualNumPastMonth;
    }

    public Integer getSignNumPastMonth() {
        return SignNumPastMonth;
    }

    public void setSignNumPastMonth(Integer signNumPastMonth) {
        SignNumPastMonth = signNumPastMonth;
    }
}
