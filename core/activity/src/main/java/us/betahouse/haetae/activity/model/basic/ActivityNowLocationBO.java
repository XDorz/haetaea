package us.betahouse.haetae.activity.model.basic;

import cn.hutool.core.date.DateTime;
import us.betahouse.util.common.ToString;

import java.util.Date;

public class ActivityNowLocationBO extends ToString {
    /**
     * 活动名称
     */

    private String activity_name;

    /**
     * 活动时间
     */
    private Date start;

    /**
     * 活动地点
     */
    private String location;

    public ActivityNowLocationBO() {

    }

    public String getActivity_name() {
        return activity_name;
    }

    public void setActivity_name(String activity_name) {
        this.activity_name = activity_name;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
