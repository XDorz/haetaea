package us.betahouse.haetae.serviceimpl.activity.manager.stamp;

import org.springframework.beans.factory.annotation.Autowired;
import us.betahouse.haetae.activity.dal.model.ActivityDO;
import us.betahouse.haetae.activity.dal.repo.ActivityDORepo;
import us.betahouse.haetae.activity.dal.service.YouthLearningRepoService;
import us.betahouse.haetae.activity.enums.ActivityRecordStateEnum;
import us.betahouse.haetae.activity.enums.ActivityTypeEnum;
import us.betahouse.haetae.activity.model.basic.ActivityBO;
import us.betahouse.haetae.activity.model.basic.YouthLearningBO;
import us.betahouse.haetae.serviceimpl.activity.request.ActivityStampRequest;
import us.betahouse.haetae.serviceimpl.common.utils.TermUtil;
import us.betahouse.util.enums.CommonResultCode;
import us.betahouse.util.utils.AssertUtil;

import java.util.Date;
import java.util.List;


public class YouthLearnActivityStampService implements StampService {

    @Autowired
    YouthLearningRepoService youthLearningRepoService;

    @Autowired
    ActivityDORepo activityDORepo;

    @Override
    public void batchStamp(ActivityStampRequest request, List<String> userIds, ActivityBO activityBO) {
        String activityId=request.getActivityId();
        ActivityDO activityDO = activityDORepo.findByActivityId(activityId);
        AssertUtil.assertNotNull(activityDO, CommonResultCode.ILLEGAL_PARAMETERS.getCode(),"查无此活动");
        AssertUtil.assertEquals(activityDO.getType(), ActivityTypeEnum.YOUTH_LEARNING_ACTIVITY.getCode(),"活动类型不符合");
        for (String userId : userIds) {
            if(!youthLearningRepoService.exitByActivityNameAndUserId(activityId,userId)){
                YouthLearningBO youthLearningBO=new YouthLearningBO();
                youthLearningBO.setActivityId(activityId);
                youthLearningBO.setUserId(userId);
                youthLearningBO.setStatus(ActivityRecordStateEnum.ENABLE.getCode());
                youthLearningBO.setType(ActivityTypeEnum.YOUTH_LEARNING_ACTIVITY.getCode());
                youthLearningBO.setTerm(TermUtil.getTerm(request.getFinishTime()));

                youthLearningBO.setFinishTime(request.getFinishTime()==null?new Date():request.getFinishTime());
                youthLearningBO.setScannerUserId(request.getScannerUserId());

                youthLearningRepoService.saveRecord(youthLearningBO);
            }
        }
    }
}
