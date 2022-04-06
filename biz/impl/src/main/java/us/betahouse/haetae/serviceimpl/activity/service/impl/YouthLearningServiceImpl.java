package us.betahouse.haetae.serviceimpl.activity.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import us.betahouse.haetae.activity.dal.model.ActivityDO;
import us.betahouse.haetae.activity.dal.repo.ActivityDORepo;
import us.betahouse.haetae.activity.dal.service.YouthLearningRepoService;
import us.betahouse.haetae.activity.enums.ActivityStateEnum;
import us.betahouse.haetae.activity.model.basic.YouthLearningBO;
import us.betahouse.haetae.activity.model.common.PageList;
import us.betahouse.haetae.serviceimpl.activity.constant.ActivityPermType;
import us.betahouse.haetae.serviceimpl.activity.request.YouthLearningRequest;
import us.betahouse.haetae.serviceimpl.activity.service.YouthLearningService;
import us.betahouse.haetae.serviceimpl.common.verify.VerifyPerm;
import us.betahouse.haetae.user.dal.model.UserInfoDO;
import us.betahouse.haetae.user.dal.repo.UserInfoDORepo;
import us.betahouse.util.utils.CollectionUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class YouthLearningServiceImpl implements YouthLearningService {

    @Autowired
    YouthLearningRepoService youthLearningRepoService;

    @Autowired
    ActivityDORepo activityDORepo;

    @Autowired
    UserInfoDORepo userInfoDORepo;

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

    @Override
    public List<YouthLearningBO> getRecordByUserId(YouthLearningRequest request) {
        return youthLearningRepoService.getRecordByUserId(request.getUserId());
    }

    @Override
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

    private YouthLearningBO fill(YouthLearningBO youthLearningBO){
        UserInfoDO userInfoDO = userInfoDORepo.findByUserId(youthLearningBO.getUserId());
        youthLearningBO.setRealName(userInfoDO.getRealName());
        youthLearningBO.setStuId(userInfoDO.getStuId());
        youthLearningBO.setClassId(userInfoDO.getClassId());
        return youthLearningBO;
    }


    private Info remove(List<YouthLearningBO> list){
        ActivityDO activityDO = activityDORepo.findAllByActivityNameAndStateNot(list.get(0).getActivityName(), ActivityStateEnum.CANCELED.getCode());
        list.forEach(youthLearningBO -> youthLearningBO.setActivityId(activityDO.getActivityId()));
        Info info=new Info();
        int i=2;
        Iterator<YouthLearningBO> iterator = list.iterator();
        while (iterator.hasNext()){
            YouthLearningBO youthLearningBO = iterator.next();
            if(!verify(youthLearningBO,info,i)){
                info.getRepeat().add(youthLearningBO);
                iterator.remove();
                i++;
            }
        }
        List<YouthLearningBO> fails = youthLearningRepoService.removeRepeat(list);
        info.setInfo(info.getInfo().append(MessageFormat.format("此次共有【{0}】条项目重复，以略过",String.valueOf(fails.size()))));
        return info;
    }

    private Info remove(List<YouthLearningBO> list,int delNum){
        Info info=new Info();
        int i=2;
        Iterator<YouthLearningBO> iterator = list.iterator();
        while (iterator.hasNext()){
            YouthLearningBO youthLearningBO = iterator.next();
            if(!verify(youthLearningBO,info,i)){
                info.getRepeat().add(youthLearningBO);
                iterator.remove();
                i++;
            }
        }
        info.setInfo(info.getInfo().append(MessageFormat.format("此次共有【{0}】条项目重复，以略过",String.valueOf(delNum))));
        return info;
    }

    private boolean verify(YouthLearningBO youthLearningBO,Info info,int j){
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
            userInfoDO=userInfoDORepo.findByStuId(sid);
            if(userInfoDO==null){
                info.setInfo(info.getInfo().append(MessageFormat.format("第【{0}】行 学号不存在，查无此学号，可在另一文件中查看\n",String.valueOf(j))));
                return false;
            }
        }
        youthLearningBO.setUserId(userInfoDO.getUserId());
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
