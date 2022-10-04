package us.betahouse.haetae.activity.model.basic;

import java.util.List;
import java.util.Set;

public class YouthLearnBatchBO {

    /**
     * 青年大学习期数名称
     */
    List<String> activityName;

    /**
     * 未做的期数
     */
    List<String> undo;

    /**
     * 青年大学习排序后(含做的和未做的)
     */
    List<String> sortedActivityName;

    /**
     * 未做的期数在排序后的位置
     */
    List<Integer> undoLocation;

    /**
     * 已做大学习信息
     */
    List<YouthLearningBO> youthLearn;

    /**
     * 学期
     */
    String term;

    public List<String> getActivityName() {
        return activityName;
    }

    public void setActivityName(List<String> activityName) {
        this.activityName = activityName;
    }

    public List<String> getUndo() {
        return undo;
    }

    public void setUndo(List<String> undo) {
        this.undo = undo;
    }

    public List<String> getSortedActivityName() {
        return sortedActivityName;
    }

    public List<Integer> getUndoLocation() {
        return undoLocation;
    }

    public void setUndoLocation(List<Integer> undoLocation) {
        this.undoLocation = undoLocation;
    }

    public void setSortedActivityName(List<String> sortedActivityName) {
        this.sortedActivityName = sortedActivityName;
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
