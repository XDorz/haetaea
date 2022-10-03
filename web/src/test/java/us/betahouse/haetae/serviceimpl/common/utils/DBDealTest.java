package us.betahouse.haetae.serviceimpl.common.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import us.betahouse.haetae.activity.dal.model.ActivityDO;
import us.betahouse.haetae.activity.dal.model.ActivityRecordDO;
import us.betahouse.haetae.activity.dal.repo.ActivityDORepo;
import us.betahouse.haetae.activity.dal.repo.ActivityRecordDORepo;
import us.betahouse.haetae.certificate.dal.model.CompetitionDO;
import us.betahouse.haetae.certificate.dal.model.QualificationsDO;
import us.betahouse.haetae.certificate.dal.model.SkillDO;
import us.betahouse.haetae.certificate.dal.repo.CompetitionDORepo;
import us.betahouse.haetae.certificate.dal.repo.QualificationsDORepo;
import us.betahouse.haetae.certificate.dal.repo.SkillDORepo;
import us.betahouse.haetae.user.dal.model.UserInfoDO;
import us.betahouse.haetae.user.dal.model.perm.UserDO;
import us.betahouse.haetae.user.dal.repo.UserInfoDORepo;
import us.betahouse.haetae.user.dal.repo.perm.UserDORepo;
import us.betahouse.haetae.user.utils.EncryptUtil;
import us.betahouse.util.utils.MD5Util;

import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DBDealTest {
    @Autowired
    UserInfoDORepo userInfoDORepo;

    @Autowired
    UserDORepo userDORepo;

    @Autowired
    CompetitionDORepo competitionDORepo;

    @Autowired
    QualificationsDORepo qualificationsDORepo;

    @Autowired
    SkillDORepo skillDORepo;

    @Autowired
    ActivityRecordDORepo activityRecordDORepo;

    @Autowired
    ActivityDORepo activityDORepo;

    //数据库脱敏，执行前务必做个备份！
    @Test
//    @Transactional(rollbackFor = Exception.class)
    public void initDB(){
        try{
        int i=0;
        List<UserDO> all = userDORepo.findAll();
        UUID uuid=UUID.randomUUID();
        String pwd="a00001";
        for (UserDO userDO : all) {
            userDO.setOpenId(UUID.randomUUID().toString().substring(10));
            userDO.setLastLoginIP("127.0.0.1");
            userDO.setAvatarUrl("");
            userDO.setSessionId(UUID.randomUUID().toString().substring(10));
            userDO.setPassword(EncryptUtil.encryptPassword(pwd,uuid.toString()));
            userDO.setSalt(uuid.toString());
            userDORepo.save(userDO);
            i++;
            System.out.println("保存第"+i+"条");
        }
        List<UserInfoDO> userInfoDOList = userInfoDORepo.findAll();
        for (UserInfoDO userInfoDO : userInfoDOList) {
            userInfoDO.setRealName(userInfoDO.getRealName().substring(0,1)+Math.round(Math.random()*1000));
            userInfoDO.setExtInfo("{}");
            userInfoDORepo.save(userInfoDO);
            i++;
            System.out.println("保存第"+i+"条");
        }
        List<CompetitionDO> competitionDOList = competitionDORepo.findAll();
        for (CompetitionDO competitionDO : competitionDOList) {
            competitionDO.setExtInfo("{}");
            competitionDO.setTeamName("{}");
            competitionDO.setPictureUrl("");
            competitionDORepo.save(competitionDO);
            i++;
            System.out.println("保存第"+i+"条");
        }
        List<QualificationsDO> qualificationsDOList = qualificationsDORepo.findAll();
        for (QualificationsDO qualificationsDO : qualificationsDOList) {
            qualificationsDO.setExtInfo("{}");
            qualificationsDO.setPictureUrl("");
            qualificationsDORepo.save(qualificationsDO);
            i++;
            System.out.println("保存第"+i+"条");
        }
        List<SkillDO> skillDOList = skillDORepo.findAll();
        for (SkillDO skillDO : skillDOList) {
            skillDO.setExtInfo("{}");
            skillDO.setPictureUrl("");
            skillDORepo.save(skillDO);
            i++;
            System.out.println("保存第"+i+"条");
        }
        List<ActivityRecordDO> recordDOList = activityRecordDORepo.findAll();
        for (ActivityRecordDO activityRecordDO : recordDOList) {
            activityRecordDO.setExtInfo("{}");
            activityRecordDORepo.save(activityRecordDO);
            i++;
            System.out.println("保存第"+i+"条");
        }
        List<ActivityDO> activityDOList = activityDORepo.findAll();
        for (ActivityDO activityDO : activityDOList) {
            activityDO.setPictureUrl("");
            activityDORepo.save(activityDO);
            i++;
            System.out.println("保存第"+i+"条");
        }
        }catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
    }
}
