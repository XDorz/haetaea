package us.betahouse.haetae.serviceimpl.activity.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import us.betahouse.haetae.activity.dal.model.ActivityDO;
import us.betahouse.haetae.activity.dal.model.YouthLearningDO;
import us.betahouse.haetae.activity.dal.repo.ActivityDORepo;
import us.betahouse.haetae.activity.dal.repo.YouthLearningDORepo;
import us.betahouse.haetae.activity.dal.service.YouthLearningRepoService;
import us.betahouse.haetae.activity.enums.ActivityStateEnum;
import us.betahouse.haetae.activity.enums.ActivityTypeEnum;
import us.betahouse.haetae.activity.model.basic.YouthLearnBatchBO;
import us.betahouse.haetae.activity.model.basic.YouthLearningBO;
import us.betahouse.haetae.activity.model.common.PageList;
import us.betahouse.haetae.serviceimpl.activity.constant.ActivityPermType;
import us.betahouse.haetae.serviceimpl.activity.request.YouthLearningRequest;
import us.betahouse.haetae.serviceimpl.activity.service.YouthLearningService;
import us.betahouse.haetae.serviceimpl.common.verify.VerifyPerm;
import us.betahouse.haetae.user.dal.model.UserInfoDO;
import us.betahouse.haetae.user.dal.repo.UserInfoDORepo;
import us.betahouse.util.enums.CommonResultCode;
import us.betahouse.util.utils.AssertUtil;
import us.betahouse.util.utils.CollectionUtils;

import javax.annotation.PostConstruct;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class YouthLearningServiceImpl implements YouthLearningService {

    @Autowired
    YouthLearningRepoService youthLearningRepoService;

    @Autowired
    YouthLearningDORepo youthLearningDORepo;

    @Autowired
    ActivityDORepo activityDORepo;

    @Autowired
    UserInfoDORepo userInfoDORepo;

    private Comparator<String> youthLearnComparator;

    private Comparator<YouthLearningBO> youthLearnBOComparator;

    @Override
    @VerifyPerm(permType={ActivityPermType.ACTIVITY_CREATE})
    public boolean saveRecord(YouthLearningRequest request) {
        YouthLearningBO youthLearningBO=request.getYouthLearningBO();
        youthLearningBO.setUserId(userInfoDORepo.findByStuId(youthLearningBO.getStuId()).getUserId());
        youthLearningBO.setActivityId(activityDORepo.findAllByActivityNameAndStateNot(youthLearningBO.getActivityName(),ActivityStateEnum.CANCELED.getCode()).getActivityId());
        return youthLearningRepoService.saveRecord(youthLearningBO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @VerifyPerm(permType={ActivityPermType.ACTIVITY_CREATE})
    public Info batchSaveRecord(YouthLearningRequest request) {
        List<YouthLearningBO> youthLearningBOList = request.getYouthLearningBOList();
        Info info = remove(youthLearningBOList);
        youthLearningRepoService.batchSaveRecord(youthLearningBOList);
        info.setInfo(info.getInfo().append(MessageFormat.format("本次共计导入【{0}】条数据",youthLearningBOList.size())));
        return info;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @VerifyPerm(permType={ActivityPermType.ACTIVITY_CREATE})
    public List<YouthLearningBO> batchDeleteRecord(YouthLearningRequest request) {
        List<YouthLearningBO> fail = request.getYouthLearningBOList();
        for (YouthLearningBO youthLearningBO : fail) {
//            youthLearningBO.setActivityId(activityDORepo.findByActivityName(youthLearningBO.getActivityName()).getActivityId());
            youthLearningBO.setUserId(userInfoDORepo.findByStuId(youthLearningBO.getStuId()).getUserId());
        }
        List<YouthLearningBO> target=youthLearningRepoService.removeRepeat(fail);
        youthLearningRepoService.batchDeleteRecord(target);
        return fail;
    }

    @Deprecated
    @Override
    public List<YouthLearningBO> getRecordByUserId(YouthLearningRequest request) {
        return youthLearningRepoService.getRecordByUserId(request.getUserId());
    }

    @Override
    @Deprecated
    public Integer getRecordNumByUserId(YouthLearningRequest request) {
        return youthLearningRepoService.getRecordNumByUserId(request.getUserId());
    }

    @Override
    @VerifyPerm(permType={ActivityPermType.ACTIVITY_CREATE})
    public PageList<YouthLearningBO> getByActivityNameAndUserName(YouthLearningRequest request) {
        Integer page=request.getPage()==null?0:request.getPage();
        Integer size=request.getSize()==null?10:request.getSize();
        Pageable pageable= PageRequest.of(page,size);
        ActivityDO activityDO = activityDORepo.findAllByActivityNameAndStateNot(request.getActivityName(),ActivityStateEnum.CANCELED.getCode());
        UserInfoDO userInfoDO = userInfoDORepo.findByStuId(request.getStuId());
        String activityId=activityDO==null?"":activityDO.getActivityId();
        String userId=userInfoDO==null?"":userInfoDO.getUserId();
        PageList<YouthLearningBO> pageList=null;
        if(request.getClassId()==null||request.getClassId().equals("")){
            pageList = youthLearningRepoService.getByActivityNameAndRealName(pageable, activityId, userId);
        }else {
            List<UserInfoDO> infoDOS = userInfoDORepo.findAllByClassId(request.getClassId());
            List<String> userIdList = CollectionUtils.toStream(infoDOS).filter(Objects::nonNull).map(UserInfoDO::getUserId).collect(Collectors.toList());
            pageList = youthLearningRepoService.getByActivityNameAndRealNameAndClassId(pageable, activityId, userId,userIdList);
        }
        if(pageList==null) return null;
        pageList.setContent(CollectionUtils.toStream(pageList.getContent()).filter(Objects::nonNull).map(this::fill).collect(Collectors.toList()));
        return pageList;
    }

    @Override
    public List<YouthLearnBatchBO> getTermedRecordByUserId(YouthLearningRequest request) {
        Map<String,Set<String>> map=new HashMap<>();
        List<ActivityDO> activityDOS = activityDORepo.findAllByTypeAndStateNot(
                ActivityTypeEnum.YOUTH_LEARNING_ACTIVITY.getCode(),ActivityStateEnum.CANCELED.getCode());
        Set<String> termSet=new HashSet<>();
        for (ActivityDO activityDO : activityDOS) {
            String term = activityDO.getTerm();
            termSet.add(term);
            if(map.containsKey(term)){
                map.get(term).add(activityDO.getActivityName());
            }else {
                Set<String> set=new HashSet<>();
                set.add(activityDO.getActivityName());
                map.put(term,set);
            }
        }
        List<YouthLearnBatchBO> list=new ArrayList<>();
        for (String s : termSet) {
            Set<String> activityNameSet=new HashSet<>();
            List<YouthLearningBO> youthLearningBOS = youthLearningRepoService.getRecordByUserIdAndTermAsc(request.getUserId(), s);
            for (YouthLearningBO youthLearningBO : youthLearningBOS) {
                activityNameSet.add(youthLearningBO.getActivityName());
            }
            Set<String> set = map.get(s);
            set.removeAll(activityNameSet);
            YouthLearnBatchBO youthLearnBatchBO=new YouthLearnBatchBO();
            List<String> undo=new ArrayList<>(set);
            undo.sort(youthLearnComparator);
            youthLearnBatchBO.setUndo(undo);
            youthLearnBatchBO.setTerm(s);
            List<String> nameList=new ArrayList<>(activityNameSet);
            nameList.sort(youthLearnComparator);
            youthLearnBatchBO.setActivityName(nameList);
            List<String> allName=new ArrayList<>(nameList);
            allName.addAll(undo);
            allName.sort(youthLearnComparator);
            youthLearnBatchBO.setSortedActivityName(allName);
            List<Integer> location=new ArrayList<>();
            if(undo.size()!=0){
                int i=0;
                int j=0;
                String undoName=undo.get(j);
                for (String name : allName) {
                    if(name.equals(undoName)){
                        j++;
                        location.add(i);
                        if(j==undo.size()) break;
                        undoName=undo.get(j);
                    }
                    i++;
                }
            }
            youthLearnBatchBO.setUndoLocation(location);
            youthLearningBOS.sort(youthLearnBOComparator);
            youthLearnBatchBO.setYouthLearn(youthLearningBOS);
            list.add(youthLearnBatchBO);
        }
        return list;
    }

    @PostConstruct
    private void initComparator(){

        this.youthLearnComparator=new Comparator<String>() {
            Pattern pattern=Pattern.compile("第[ ]?(\\d+)[ ]?期");
            Pattern yearPattern=Pattern.compile("^(\\d+)年");

            @Override
            public int compare(String o1, String o2) {
                Matcher matcher1 = pattern.matcher(o1);
                Matcher matcher2 = pattern.matcher(o2);
                //年份检测
                Matcher matcher3=yearPattern.matcher(o1);
                Matcher matcher4=yearPattern.matcher(o2);
                //默认年份一定都有
                matcher3.find();
                matcher4.find();
                int y1=Integer.parseInt(matcher3.group(1));
                int y2=Integer.parseInt(matcher4.group(1));
                if(y1>y2) return y1-y2;
                //期数检测
                boolean m1=matcher1.find();
                boolean m2=matcher2.find();
                if(m1&&m2){
                    int num1=Integer.parseInt(matcher1.group(1));
                    int num2=Integer.parseInt(matcher2.group(1));
                    return num1-num2;
                }else if(!m1&&m2){
                    return 1;
                }else if(m1){
                    return -1;
                }else {
                    ActivityDO activity1 = activityDORepo.findAllByActivityNameAndStateNot(o1, ActivityStateEnum.CANCELED.getCode());
                    ActivityDO activity2 = activityDORepo.findAllByActivityNameAndStateNot(o2,ActivityStateEnum.CANCELED.getCode());
                    return (int)(activity1.getStart().getTime()-activity2.getStart().getTime());
                }
            }
        };

        this.youthLearnBOComparator=new Comparator<YouthLearningBO>() {
            Pattern pattern=Pattern.compile("第[ ]?(\\d+)[ ]?期");
            Pattern yearPattern=Pattern.compile("^(\\d+)年");

            @Override
            public int compare(YouthLearningBO bo1, YouthLearningBO bo2) {
                String o1=bo1.getActivityName();
                String o2=bo2.getActivityName();
                Matcher matcher1 = pattern.matcher(o1);
                Matcher matcher2 = pattern.matcher(o2);
                //年份检测
                Matcher matcher3=yearPattern.matcher(o1);
                Matcher matcher4=yearPattern.matcher(o2);
                //默认年份一定都有
                matcher3.find();
                matcher4.find();
                int y1=Integer.parseInt(matcher3.group(1));
                int y2=Integer.parseInt(matcher4.group(1));
                if(y1>y2) return y1-y2;
                //期数检测
                boolean m1=matcher1.find();
                boolean m2=matcher2.find();
                if(m1&&m2){
                    int num1=Integer.parseInt(matcher1.group(1));
                    int num2=Integer.parseInt(matcher2.group(1));
                    return num1-num2;
                }else if(!m1&&m2){
                    return 1;
                }else if(m1){
                    return -1;
                }else {
                    ActivityDO activity1 = activityDORepo.findAllByActivityNameAndStateNot(o1, ActivityStateEnum.CANCELED.getCode());
                    ActivityDO activity2 = activityDORepo.findAllByActivityNameAndStateNot(o2,ActivityStateEnum.CANCELED.getCode());
                    return (int)(activity1.getStart().getTime()-activity2.getStart().getTime());
                }
            }
        };
    }

    private YouthLearningBO fill(YouthLearningBO youthLearningBO){
        UserInfoDO userInfoDO = userInfoDORepo.findByUserId(youthLearningBO.getUserId());
        youthLearningBO.setRealName(userInfoDO.getRealName());
        youthLearningBO.setStuId(userInfoDO.getStuId());
        youthLearningBO.setClassId(userInfoDO.getClassId());
        return youthLearningBO;
    }


//    private Info remove(List<YouthLearningBO> list){
//        for (YouthLearningBO youthLearningBO : list) {
//            ActivityDO activityDO = activityDORepo.findAllByActivityNameAndStateNot(youthLearningBO.getActivityName(), ActivityStateEnum.CANCELED.getCode());
//            AssertUtil.assertNotNull(activityDO, CommonResultCode.ILLEGAL_PARAMETERS.getCode(),"查无此活动");
//            youthLearningBO.setActivityId(activityDO.getActivityId());
//        }
//        Info info=new Info();
//        int i=2;
//        Iterator<YouthLearningBO> iterator = list.iterator();
//        while (iterator.hasNext()){
//            YouthLearningBO youthLearningBO = iterator.next();
//            if(!verify(youthLearningBO,info,i)){
//                info.getRepeat().add(youthLearningBO);
//                iterator.remove();
//                i++;
//            }
//        }
//        List<YouthLearningBO> fails = youthLearningRepoService.removeRepeat(list);
//        info.setInfo(info.getInfo().append(MessageFormat.format("此次共有【{0}】条项目重复，以略过",String.valueOf(fails.size()))));
//        return info;
//    }

    private Info remove(List<YouthLearningBO> list){

        String name = list.get(0).getActivityName();
        ActivityDO activity = activityDORepo.findAllByActivityNameAndStateNot(name, ActivityStateEnum.CANCELED.getCode());
        AssertUtil.assertNotNull(activity, CommonResultCode.ILLEGAL_PARAMETERS.getCode(),"查无此活动");

        Calendar calendar=Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        List<UserInfoDO> stuInfoOriginal = userInfoDORepo.findByGradeRegex(year + "|" + (year - 1) + "|" + (year - 2) + "|" + (year - 3) + "|" + (year - 4));
        List<String> stuInfo=new ArrayList<>();
        Map<String,String> usmap=new HashMap<>();
        Map<String,String> sumap=new HashMap<>();
        stuInfoOriginal.forEach(sinfo->{
            stuInfo.add(sinfo.getStuId());
            usmap.put(sinfo.getUserId(),sinfo.getStuId());
            sumap.put(sinfo.getStuId(),sinfo.getUserId());
        });
        String activityId = activity.getActivityId();
        List<YouthLearningDO> records = youthLearningDORepo.findAllByActivityId(activityId);
        Set<String> set=new HashSet<>();
        records.forEach(record -> {
            String stuId = usmap.getOrDefault(record.getUserId(), null);
            if(stuId==null||stuId.equals("")){
                stuId=userInfoDORepo.findByUserId(record.getUserId()).getStuId();
                sumap.put(stuId,record.getUserId());
            }
            set.add(stuId);
        });

        Info info=new Info();
        int i=2;

        List<YouthLearningBO> fails = new ArrayList<>();
        Iterator<YouthLearningBO> iterator = list.iterator();
        while (iterator.hasNext()){
            YouthLearningBO youthLearningBO = iterator.next();
            youthLearningBO.setActivityId(activityId);


            if(!verify(youthLearningBO,info,i,stuInfo,sumap)){
                info.getRepeat().add(youthLearningBO);
                iterator.remove();
                i++;
            }else {
                boolean remove = set.remove(youthLearningBO.getStuId());
                if(remove){
                    fails.add(youthLearningBO);
                    iterator.remove();
                }
            }
        }
        info.setInfo(info.getInfo().append(MessageFormat.format("此次共有【{0}】条项目重复，以略过,",String.valueOf(fails.size()))));
        return info;
    }

//    private Info remove(List<YouthLearningBO> list,int delNum){
//        Info info=new Info();
//        int i=2;
//        Calendar calendar=Calendar.getInstance();
//        int year = calendar.get(Calendar.YEAR);
//        List<UserInfoDO> stuInfoOriginal = userInfoDORepo.findByGradeRegex(year + "|" + (year - 1) + "|" + (year - 2) + "|" + (year - 3) + "|" + (year - 4));
//        List<String> stuInfo=new ArrayList<>();
//        stuInfoOriginal.forEach(sinfo->{
//            stuInfo.add(sinfo.getStuId()+"_"+sinfo.getRealName());
//        });
//        Iterator<YouthLearningBO> iterator = list.iterator();
//        while (iterator.hasNext()){
//            YouthLearningBO youthLearningBO = iterator.next();
//            if(!verify(youthLearningBO,info,i,stuInfo)){
//                info.getRepeat().add(youthLearningBO);
//                iterator.remove();
//                i++;
//            }
//        }
//        info.setInfo(info.getInfo().append(MessageFormat.format("此次共有【{0}】条项目重复，以略过",String.valueOf(delNum))));
//        return info;
//    }

    private boolean verify(YouthLearningBO youthLearningBO,Info info,int j,List<String> stuInfo,Map<String,String> suMap){
        String classId = youthLearningBO.getClassId();
        String stuId = youthLearningBO.getStuId();
        if(classId==null||stuId==null){
            info.setInfo(info.getInfo().append(MessageFormat.format("第【{0}】行 未曾找到学号或者姓名，无法比较,可在另一文件中查看\n",String.valueOf(j))));
            return false;
        }
        String sid="",name="";
        for (int i = 0; i < stuId.length(); i++) {
            char c=stuId.charAt(i);
            if(c<='9'&&c>='0'){
                sid+=String.valueOf(c);
            } else {
                name+=String.valueOf(c);
            }
            name=name.trim();
        }
        youthLearningBO.setStuId(sid);
        youthLearningBO.setRealName(name);
        UserInfoDO userInfoDO=null;
//        if(name.equals("")&&sid.equals("")){
//            info.setInfo(info.getInfo().append(MessageFormat.format("第【{0}】行 未曾找到学号或者姓名，无法比较,可在另一文件中查看\n",String.valueOf(j))));
//            return false;
//        }else if(classId.equals("")){
//            info.setInfo(info.getInfo().append(MessageFormat.format("第【{0}】行 未填写班级,无法比较,可在另一文件中查看\n",String.valueOf(j))));
//            return false;
//        }else if(!name.equals("")&&!sid.equals("")){
//            userInfoDO = userInfoDORepo.findByStuId(sid);
//            if(userInfoDO==null){
//                info.setInfo(info.getInfo().append(MessageFormat.format("第【{0}】行 学号与姓名不匹配，查无此记录,可在另一文件中查看\n",String.valueOf(j))));
//                return false;
//            }
//            if(!name.equals(userInfoDO.getRealName())){
//                info.setInfo(info.getInfo().append(MessageFormat.format("第【{0}】行 学号与姓名不匹配，填写姓名【{1}】 实际姓名可能为【{2}】,可在另一文件中查看\n",String.valueOf(j),name,userInfoDO.getRealName())));
//                return false;
//            }
//        }else if(!sid.equals("")){
//            userInfoDO = userInfoDORepo.findByStuId(sid);
//            if(userInfoDO==null){
//                info.setInfo(info.getInfo().append(MessageFormat.format("第【{0}】行 学号不存在，查无此记录,可在另一文件中查看\n",String.valueOf(j))));
//                return false;
//            }
//            if(!classId.equals(userInfoDO.getClassId())){
//                info.setInfo(info.getInfo().append(MessageFormat.format("第【{0}】行 学号与班级不匹配，填写班级【{1}】 实际班级可能为【{2}】,可在另一文件中查看\n",String.valueOf(j),classId,userInfoDO.getClassId())));
//                return false;
//            }
//        }else {
//            List<UserInfoDO> userInfoDOs = userInfoDORepo.findAllByRealNameAndClassId(name, classId);
//            if(userInfoDOs.size()>1){
//                info.setInfo(info.getInfo().append(MessageFormat.format("第【{0}】行 姓名与班级有多行匹配，可在另一文件中查看\n",String.valueOf(j))));
//                return false;
//            }else if(userInfoDOs.size()==0){
//                info.setInfo(info.getInfo().append(MessageFormat.format("第【{0}】行 姓名与班级不匹配，查无此记录，可在另一文件中查看\n",String.valueOf(j))));
//                return false;
//            }
//            userInfoDO=userInfoDOs.get(0);
//        }
        if(sid.equals("")){
            info.setInfo(info.getInfo().append(MessageFormat.format("第【{0}】行 学号未填入，可在另一文件中查看\n",String.valueOf(j))));
            return false;
        }else {
            if(!stuInfo.remove(sid)){
                userInfoDO=userInfoDORepo.findByStuId(sid);
                if(userInfoDO==null){
                    info.setInfo(info.getInfo().append(MessageFormat.format("第【{0}】行 学号不存在，查无此学号，可在另一文件中查看\n",String.valueOf(j))));
                    return false;
                }
                youthLearningBO.setUserId(userInfoDO.getUserId());
                return true;
            }
        }
        youthLearningBO.setUserId(suMap.get(sid));
        return true;
    }

    public class Info{
        /**
         * 重复的记录
         */
        private List<YouthLearningBO> repeat;

        /**
         * 返回消息
         */
        private StringBuffer info;

        private boolean flag;

        public Info(){
            this.repeat=new ArrayList<>();
            this.info=new StringBuffer();
            this.flag=true;
        }

        public List<YouthLearningBO> getRepeat() {
            return repeat;
        }

        public void setRepeat(List<YouthLearningBO> repeat) {
            this.repeat = repeat;
        }

        public StringBuffer getInfo() {
            return info;
        }

        public void setInfo(StringBuffer info) {
            this.info = info;
        }

        public boolean isFlag() {
            return flag;
        }

        public void setFlag(boolean flag) {
            this.flag = flag;
        }
    }
}
