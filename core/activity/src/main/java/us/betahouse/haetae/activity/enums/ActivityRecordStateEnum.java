package us.betahouse.haetae.activity.enums;

import org.apache.commons.lang.StringUtils;

public enum ActivityRecordStateEnum {

    ENABLE("ENABLE","通过"),

    DELETE("DELETE","删除");

    /**
     * 状态id
     */
    private final String code;

    /**
     * 描述
     */
    private final String desc;

    public static ActivityRecordStateEnum getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (ActivityRecordStateEnum activityRecordStateEnum : values()) {
            if (StringUtils.equals(activityRecordStateEnum.getCode(), code)) {
                return activityRecordStateEnum;
            }
        }
        return null;
    }

    private ActivityRecordStateEnum(String code,String desc){
        this.code=code;
        this.desc=desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
