package us.betahouse.haetae.activity.model.basic;

import java.util.HashMap;

/**
 * 存储学分 年级 专业的实体
 *
 * @author lzm
 *
 */
public class ActivityCreditsStatisticsBO {
    private static final long serialVersionUID = -8735096497842710304L;
    /**
     * 用来存储学分对应的数量
     */
    private HashMap<Integer,Integer> creditStatistic;
    /**
     * 年级
     */
    private String grade;
    /**
     *  专业
     */
    private String MajorId;

    public ActivityCreditsStatisticsBO() {

    }

    public ActivityCreditsStatisticsBO(String grade, String majorId) {
        this.grade = grade;
        MajorId = majorId;
    }

    public void addCreditStatistic(Integer credit) {
        if (creditStatistic==null){
            creditStatistic = new HashMap<>();
        }
        if (creditStatistic.putIfAbsent(credit, 1)==null){
            return;
        };
        int nums = creditStatistic.get(credit);
        creditStatistic.put(credit,nums+1);
    }


    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getMajorId() {
        return MajorId;
    }

    public void setMajorId(String majorId) {
        MajorId = majorId;
    }

    @Override
    public String toString() {
        return "ActivityCreditsStatisticsBO{" +
                "creditStatistic=" + creditStatistic +
                ", grade='" + grade + '\'' +
                ", MajorId='" + MajorId + '\'' +
                '}';
    }
}
