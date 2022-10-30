package us.betahouse.haetae.activity.dal.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import us.betahouse.haetae.activity.dal.model.ActivityDO;
import us.betahouse.haetae.activity.dal.model.YouthLearningDO;
import us.betahouse.haetae.activity.dal.repo.ActivityDORepo;
import us.betahouse.haetae.activity.dal.repo.YouthLearningDORepo;
import us.betahouse.haetae.activity.dal.service.YouthLearningRepoService;
import us.betahouse.haetae.activity.enums.ActivityRecordStateEnum;
import us.betahouse.haetae.activity.enums.ActivityStateEnum;
import us.betahouse.haetae.activity.idfactory.BizIdFactory;
import us.betahouse.haetae.activity.model.basic.ActivityRecordBO;
import us.betahouse.haetae.activity.model.basic.YouthLearningBO;
import us.betahouse.haetae.activity.model.common.PageList;
import us.betahouse.util.utils.CollectionUtils;
import us.betahouse.util.utils.DateUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class YouthLearningRepoServiceImpl implements YouthLearningRepoService {

    @Autowired
    private ActivityDORepo activityDORepo;

    @Autowired
    private BizIdFactory idFactory;

    @Autowired
    private YouthLearningDORepo youthLearningDORepo;

    private SimpleDateFormat sdf=new SimpleDateFormat("");

    private YouthLearningBO convert(YouthLearningDO youthLearningDO){
        YouthLearningBO youthLearningBO=new YouthLearningBO();
        youthLearningBO.setActivityId(youthLearningDO.getActivityId());
        ActivityDO activityDO = activityDORepo.findByActivityId(youthLearningDO.getActivityId());
        youthLearningBO.setActivityName(activityDO.getActivityName());
        youthLearningBO.setOrganization(activityDO.getOrganizationMessage());
        youthLearningBO.setActivityRecordId(youthLearningDO.getActivityRecordId());
        youthLearningBO.setFinishTime(youthLearningDO.getFinishTime());
        youthLearningBO.setScannerUserId(youthLearningDO.getScannerUserId());
        youthLearningBO.setTerm(youthLearningDO.getTerm());
        youthLearningBO.setType(youthLearningDO.getType());
        youthLearningBO.setUserId(youthLearningDO.getUserId());
        youthLearningBO.setStatus(youthLearningDO.getStatus());
        youthLearningBO.setTime(DateUtil.getOutputDateStr(youthLearningDO.getFinishTime()));
        return youthLearningBO;
    }

    private YouthLearningDO convertDO(YouthLearningBO youthLearningBO){
        YouthLearningDO youthLearningDO=new YouthLearningDO();
        String activityId=youthLearningBO.getActivityId()==null?activityDORepo.findAllByActivityNameAndStateNot(youthLearningBO.getActivityName(), ActivityStateEnum.CANCELED.getCode()).getActivityId():youthLearningBO.getActivityId();
        youthLearningDO.setActivityId(activityId);
        if(youthLearningBO.getActivityRecordId()==null||youthLearningBO.getActivityRecordId().equals("")) youthLearningBO.setActivityRecordId(idFactory.getYouthLearningRecordId());
        youthLearningDO.setActivityRecordId(youthLearningBO.getActivityRecordId()==null?idFactory.getYouthLearningRecordId():youthLearningBO.getActivityRecordId());
        youthLearningDO.setFinishTime(youthLearningBO.getFinishTime());
        youthLearningDO.setScannerUserId(youthLearningBO.getScannerUserId());
        youthLearningDO.setTerm(youthLearningBO.getTerm());
        youthLearningDO.setType(youthLearningBO.getType());
        youthLearningDO.setStatus(youthLearningBO.getStatus());
        youthLearningDO.setUserId(youthLearningBO.getUserId());
        return youthLearningDO;
    }

    @Override
    public boolean saveRecord(YouthLearningBO youthLearningBO) {
//        List<YouthLearningDO> list = youthLearningDORepo.findAllByActivityIdAndUserIdAndStatus(youthLearningBO.getActivityId(), youthLearningBO.getUserId(),ActivityRecordStateEnum.ENABLE.getCode());
//        if(list.size()!=0) return false;
        youthLearningDORepo.save(convertDO(youthLearningBO));
        return true;
    }

    @Override
    public List<YouthLearningBO> batchSaveRecord(List<YouthLearningBO> list) {
        List<YouthLearningDO> lists = CollectionUtils.toStream(list).filter(Objects::nonNull).map(this::convertDO).collect(Collectors.toList());
        youthLearningDORepo.saveAll(lists);
        return null;
    }

    @Override
    public List<YouthLearningBO> batchDeleteRecord(List<YouthLearningBO> list) {
        for (YouthLearningBO youthLearningBO : list) {
            List<YouthLearningDO> lists = youthLearningDORepo.findAllByActivityIdAndUserIdAndStatus(youthLearningBO.getActivityId(), youthLearningBO.getUserId(), ActivityRecordStateEnum.ENABLE.getCode());
            youthLearningDORepo.deleteByActivityRecordId(lists.get(0).getActivityRecordId(), ActivityRecordStateEnum.DELETE.getCode());
        }
        return list;
    }

    @Override
    public List<YouthLearningBO> getRecordByUserId(String userId) {
        return CollectionUtils.toStream(youthLearningDORepo.findAllByUserIdAndStatusOrderByFinishTimeDesc(userId,ActivityRecordStateEnum.ENABLE.getCode()))
                .filter(Objects::nonNull)
                .map(this::convert)
                .collect(Collectors.toList());
    }

    @Override
    public List<YouthLearningBO> getRecordByUserIdAndTermAsc(String userId, String term) {
        return CollectionUtils.toStream(youthLearningDORepo.findAllByUserIdAndStatusAndTermOrderByFinishTimeAsc(userId,ActivityRecordStateEnum.ENABLE.getCode(),term))
                .filter(Objects::nonNull)
                .map(this::convert)
                .collect(Collectors.toList());
    }

    @Override
    public List<YouthLearningBO> getRecordByUserIdAndTerm(String userId, String term) {
        return CollectionUtils.toStream(youthLearningDORepo.findAllByUserIdAndStatusAndTermOrderByFinishTimeDesc(userId,ActivityRecordStateEnum.ENABLE.getCode(),term))
                .filter(Objects::nonNull)
                .map(this::convert)
                .collect(Collectors.toList());
    }

    @Override
    public Integer getRecordNumByUserId(String userId) {
        return youthLearningDORepo.getNumByUserId(userId,ActivityRecordStateEnum.ENABLE.getCode());
    }

    /**
     * 去重
     * @return
     */
    @Override
    public List<YouthLearningBO> removeRepeat(List<YouthLearningBO> original){

//        List<String> userIds=new ArrayList<>();

        Iterator<YouthLearningBO> iterator = original.iterator();
        List<YouthLearningBO> list=new ArrayList<>();
        while (iterator.hasNext()){
            YouthLearningBO next = iterator.next();
            if(next.getActivityId()==null) next.setActivityId(activityDORepo.findAllByActivityNameAndStateNot(next.getActivityName(),ActivityStateEnum.CANCELED.getCode()).getActivityId());
//            if(userIds.contains(next.getUserId())){
//                list.add(next);
//                iterator.remove();
//                continue;
//            }else {
//                userIds.add(next.getUserId());
//            }
            if(youthLearningDORepo.findAllByActivityIdAndUserIdAndStatus(next.getActivityId(),next.getUserId(),ActivityRecordStateEnum.ENABLE.getCode()).size()!=0){
                list.add(next);
                iterator.remove();
            }
        }
        return list;
    }

    @Override
    public List<YouthLearningBO> getRecordByActivityId(String activityId) {
        return CollectionUtils.toStream(youthLearningDORepo.findAllByActivityId(activityId))
                .filter(Objects::nonNull)
                .map(this::convert)
                .collect(Collectors.toList());
    }

    @Override
    public PageList<YouthLearningBO> getByActivityNameAndRealName(Pageable pageable,String activityId, String userId) {
        Page<YouthLearningDO> page = youthLearningDORepo.findAllByActivityIdContainsAndUserIdContains(pageable, activityId, userId);
        return new PageList<YouthLearningBO>(this::convert,page);
    }

    @Override
    public PageList<YouthLearningBO> getByActivityNameAndRealNameAndClassId(Pageable pageable,String activityId, String userId,List<String> userIds) {
        Page<YouthLearningDO> page = youthLearningDORepo.findAllByActivityIdContainsAndUserIdContainsAndClassIdIn(pageable, activityId, userId,userIds);
        return new PageList<YouthLearningBO>(this::convert,page);
    }

    @Override
    public boolean exitByActivityNameAndUserId(String activityId,String userId) {
        return youthLearningDORepo.existsByActivityIdAndUserId(activityId,userId);
    }

    @Override
    public ActivityRecordBO convertARB(YouthLearningBO youthLearningBO) {
        ActivityRecordBO activityRecordBO=new ActivityRecordBO();
        activityRecordBO.setActivityId(youthLearningBO.getActivityId());
        activityRecordBO.setActivityRecordId(youthLearningBO.getActivityRecordId());
        activityRecordBO.setScannerUserId(youthLearningBO.getScannerUserId());
        activityRecordBO.setStatus(youthLearningBO.getStatus());
        activityRecordBO.setTerm(youthLearningBO.getTerm());
        activityRecordBO.setType(youthLearningBO.getType());
        activityRecordBO.setCreateTime(youthLearningBO.getFinishTime());
        return activityRecordBO;
    }
}
