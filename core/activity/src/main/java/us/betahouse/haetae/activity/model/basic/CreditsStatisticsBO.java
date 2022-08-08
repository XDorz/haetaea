package us.betahouse.haetae.activity.model.basic;

import javax.persistence.Entity;
import java.util.HashMap;
import java.util.HashSet;

/**
 * 存储学分 年级 专业的实体
 *
 * @author lzm
 * @version : ClassDO.java
 */
public class CreditsStatisticsBO {
    private static final long serialVersionUID = -8735096497842710304L;
    /**
     * 用来存储学分对应的数量
     */
    private HashSet<Integer> creditAndNums = new HashSet<>();
    /**
     * 年级
     */
    private Integer grade;
    /**
     *  专业id
     */
    private Integer MajorId;

}
