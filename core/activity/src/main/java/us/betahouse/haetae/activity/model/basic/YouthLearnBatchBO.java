package us.betahouse.haetae.activity.model.basic;

import java.util.List;
import java.util.Set;

public class YouthLearnBatchBO {

    /**
     * 青年大学习期数名称
     */
    Set<String> activityName;

    /**
     * 未做的期数
     */
    Set<String> undo;

    /**
     * 一批青年大学习
     */
    List<YouthLearningBO> youthLearn;

    /**
     * 学期
     */
    String term;

    public Set<String> getActivityName() {
        return activityName;
    }

    public void setActivityName(Set<String> activityName) {
        this.activityName = activityName;
    }

    public Set<String> getUndo() {
        return undo;
    }

    public void setUndo(Set<String> undo) {
        this.undo = undo;
    }

    public List<YouthLearningBO> getYouthLearn() {
        return youthLearn;
    }

    public void setYouthLearn(List<YouthLearningBO> youthLearn) {
        this.youthLearn = youthLearn;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }
}
