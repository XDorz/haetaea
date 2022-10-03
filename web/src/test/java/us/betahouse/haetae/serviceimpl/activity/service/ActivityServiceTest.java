package us.betahouse.haetae.serviceimpl.activity.service;


import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.text.csv.CsvWriter;
import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import us.betahouse.haetae.activity.dal.model.ActivityDO;
import us.betahouse.haetae.activity.dal.repo.ActivityDORepo;
import us.betahouse.haetae.activity.dal.repo.YouthLearningDORepo;
import us.betahouse.haetae.activity.dal.service.ActivityRepoService;
import us.betahouse.haetae.activity.enums.ActivityTypeEnum;
import us.betahouse.haetae.activity.manager.ActivityManager;
import us.betahouse.haetae.activity.model.basic.PastActivityBO;
import us.betahouse.haetae.activity.request.ActivityRequest;
import us.betahouse.haetae.serviceimpl.activity.request.ActivityManagerRequest;
import us.betahouse.haetae.serviceimpl.common.OperateContext;
import us.betahouse.haetae.user.dal.model.UserInfoDO;
import us.betahouse.haetae.user.dal.repo.UserInfoDORepo;
import us.betahouse.haetae.user.dal.service.UserInfoRepoService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ActivityServiceTest {

    @Autowired
    ActivityService activityService;
    @Autowired
    private UserInfoRepoService userInfoRepoService;
    @Autowired
    ActivityManager activityManager;
    @Autowired
    ActivityRepoService activityRepoService;
    @Autowired
    YouthLearningDORepo youthLearningDORepo;
    @Autowired
    ActivityDORepo activityDORepo;
    @Autowired
    UserInfoDORepo userInfoDORepo;

    @Test
    public void findAllTest(){
        System.out.println(JSON.toJSONString(activityService.findAll(new ActivityManagerRequest(), new OperateContext())));
    }

    @Test
    public void initPastActivity() {
        activityService.initPastActivity();
    }

    //@Transactional
    @Test
    public void initPastActivityRecord(){
        String url = "C:\\Users\\86181\\Desktop\\17.csv";
        String[][] csv = us.betahouse.util.utils.CsvUtil.getWithHeader(url);
        PastActivityBO pastActivityBO;
        ActivityRequest activityRequest=new ActivityRequest();
        for (int i = 1; i < csv.length; i++) {
            String[] acsv = csv[i];
            System.out.println(acsv[0]);
            activityRequest.setStuId(acsv[0]);
            pastActivityBO = activityManager.findPast(activityRequest);
            pastActivityBO.setPastLectureActivity(0L);
            pastActivityBO.setPastVolunteerActivityTime(Long.valueOf(acsv[2]));
            pastActivityBO.setPastPracticeActivity(Long.valueOf(acsv[3]));
            pastActivityBO.setPastSchoolActivity(0L);
            pastActivityBO.setUndistributedStamp(Long.valueOf(acsv[1]));
            //System.out.println(pastActivityBO);
            activityRepoService.updatePastActivity(pastActivityBO.getUserId(), pastActivityBO);
        }
    }

    @Test
    public void getYouthLearnRecord(){
        List<String> users = youthLearningDORepo.getAllUserId();
        List<ActivityDO> activityDOS = activityDORepo.findAllByTypeAndTerm(ActivityTypeEnum.YOUTH_LEARNING_ACTIVITY.getCode(), "2021B");
        CsvWriter writer = CsvUtil.getWriter(System.getProperty("user.home") + "/desktop/result.csv", StandardCharsets.UTF_8);
        String[] param=new String[activityDOS.size()+2];
        param[0]="姓名";
        param[1]="学号";
        for (int i = 2; i < param.length; i++) {
            param[i]=activityDOS.get(i-2).getActivityName();
        }
        writer.write(param);
        for (String user : users) {
            UserInfoDO userInfoDO = userInfoDORepo.findByUserId(user);
            param[0]=userInfoDO.getRealName();
            param[1]=userInfoDO.getStuId();
            for (int i = 2; i < param.length; i++) {
                param[i]=youthLearningDORepo.existsByActivityIdAndUserId(
                        activityDOS.get(i-2).getActivityId(),userInfoDO.getUserId())?"√":"×";
            }
            writer.write(param);
        }
        writer.flush();
        writer.close();
    }

    @Test
    public void getYouthLearnRecord2(){
        List<String> users = youthLearningDORepo.getAllUserId();
        List<ActivityDO> activityDOS = new ArrayList<>();
        activityDOS.add(activityDORepo.findByActivityName("2022年第16期"));
        CsvWriter writer = CsvUtil.getWriter(System.getProperty("user.home") + "/desktop/result.csv", StandardCharsets.UTF_8);
        String[] param=new String[activityDOS.size()+2];
        param[0]="姓名";
        param[0]="学号";
        for (int i = 2; i < param.length; i++) {
            param[i]=activityDOS.get(i-2).getActivityName();
        }
        writer.write(param);
        for (String user : users) {
            UserInfoDO userInfoDO = userInfoDORepo.findByUserId(user);
            param[0]=userInfoDO.getRealName();
            param[1]=userInfoDO.getStuId();
            for (int i = 2; i < param.length; i++) {
                param[i]=youthLearningDORepo.existsByActivityIdAndUserId(
                        activityDOS.get(i-2).getActivityId(),userInfoDO.getUserId())?"√":"×";
            }
            writer.write(param);
        }
        writer.flush();
        writer.close();
    }
}
