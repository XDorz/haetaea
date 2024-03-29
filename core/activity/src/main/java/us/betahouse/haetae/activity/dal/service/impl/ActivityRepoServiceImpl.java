/*
  betahouse.us
  CopyRight (c) 2012 - 2018
 */
package us.betahouse.haetae.activity.dal.service.impl;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import us.betahouse.haetae.activity.dal.model.ActivityDO;
import us.betahouse.haetae.activity.dal.model.PastActivityDO;
import us.betahouse.haetae.activity.dal.repo.ActivityDORepo;
import us.betahouse.haetae.activity.dal.repo.PastActivityDORepo;
import us.betahouse.haetae.activity.dal.service.ActivityRepoService;
import us.betahouse.haetae.activity.idfactory.BizIdFactory;
import us.betahouse.haetae.activity.model.basic.ActivityBO;
import us.betahouse.haetae.activity.model.basic.PastActivityBO;
import us.betahouse.haetae.activity.model.common.PageList;
import us.betahouse.util.enums.CommonResultCode;
import us.betahouse.util.exceptions.BetahouseException;
import us.betahouse.util.utils.AssertUtil;
import us.betahouse.util.utils.CollectionUtils;
import us.betahouse.util.utils.LoggerUtil;

import java.io.InputStream;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 活动仓储服务实现
 *
 * @author MessiahJK
 * @version : ActivityRepoServiceImpl.java 2018/11/17 20:28 MessiahJK
 */
@Service
public class ActivityRepoServiceImpl implements ActivityRepoService {

    private final Logger LOGGER = LoggerFactory.getLogger(ActivityRepoServiceImpl.class);
    @Autowired
    private ActivityDORepo activityDORepo;

    @Autowired
    private PastActivityDORepo pastActivityDORepo;

    /**
     * id工厂
     */
    @Autowired
//    @Qualifier("activityBizFactory")
    private BizIdFactory activityBizFactory;

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * 查询所有活动
     *
     * @return
     */
    @Override
    public List<ActivityBO> queryAllActivity() {
        List<ActivityDO> activityDOList = activityDORepo.findAll();
        return CollectionUtils.toStream(activityDOList)
                .filter(Objects::nonNull)
                .map(this::convert)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> queryAllOrganization() {
        InputStream fis=getClass().getClassLoader().getResourceAsStream("download/organization.xlsx");
        ExcelReader excelReader;
        try {
            excelReader = ExcelUtil.getReader(fis);
        } catch (Exception e) {
            throw new BetahouseException(CommonResultCode.FORBIDDEN,"文件读取错误");
        }
        AssertUtil.assertBigger(2, excelReader.getRowCount(), CommonResultCode.FORBIDDEN.getCode(), "文件不得少于两行信息，本次批量导入未进行");
        List<List<Object>> read = excelReader.read(0, excelReader.getRowCount());
        List<String> header = new ArrayList<>(Arrays.asList("组织名称"));
        AssertUtil.assertTrue(header.equals(read.get(0)), CommonResultCode.FORBIDDEN, MessageFormat.format("表头格式错误，正确模板为{0}", header));
        read.remove(0);

        List<String> notStampStuIds = new ArrayList<>();
        for (List<Object> objects : read) {
            notStampStuIds.add(String.valueOf(objects.get(0)));
        }
        return notStampStuIds;
    }

    @Override
    public List<ActivityBO> queryActivitiesByState(String state) {
        List<ActivityDO> activityDOList = activityDORepo.findAllByState(state);
        return CollectionUtils.toStream(activityDOList)
                .filter(Objects::nonNull)
                .map(this::convert)
                .collect(Collectors.toList());
    }

    @Override
    public List<ActivityBO> findFirst10OrOrderByStart() {
        List<ActivityDO> activityDOList =activityDORepo.findFirst10OrOrderByStart();
        return CollectionUtils.toStream(activityDOList)
                .filter(Objects::nonNull)
                .map(this::convert)
                .collect(Collectors.toList());
    }

    /**
     * 通过类型查询活动
     *
     * @param type
     * @return
     */
    @Override
    public List<ActivityBO> queryActivityByType(String type) {
        List<ActivityDO> activityDOList = activityDORepo.findAllByType(type);
        return CollectionUtils.toStream(activityDOList)
                .filter(Objects::nonNull)
                .map(this::convert)
                .collect(Collectors.toList());
    }

    /**
     * 新增活动
     *
     * @param activityBO
     * @return
     */
    @Override
    public ActivityBO createActivity(ActivityBO activityBO) {
        if (StringUtils.isBlank(activityBO.getActivityId())) {
            activityBO.setActivityId(activityBizFactory.getActivityId());
        }
        ActivityDO convert = convert(activityBO);
        return convert(activityDORepo.save(convert));
    }

    /**
     * 更新活动
     *
     * @param activityBO
     * @return
     */
    @Override
    public ActivityBO updateActivity(ActivityBO activityBO) {
        if (StringUtils.isBlank(activityBO.getActivityId()) && !activityDORepo.existsActivityDOByActivityId(activityBO.getActivityId())) {
            LoggerUtil.error(LOGGER, "更新的活动不存在 ActivityBO={0}", activityBO);
            throw new BetahouseException(CommonResultCode.ILLEGAL_PARAMETERS.getCode(), "更新的活动不存在");
        }
        ActivityDO activityDO = activityDORepo.findByActivityId(activityBO.getActivityId());
        ActivityDO newActivityDO = convert(activityBO);
        if (newActivityDO.getActivityName() != null) {
            activityDO.setActivityName(newActivityDO.getActivityName());
        }
        if (newActivityDO.getDescription() != null) {
            activityDO.setDescription(newActivityDO.getDescription());
        }
        if (newActivityDO.getEnd() != null) {
            activityDO.setEnd(newActivityDO.getEnd());
        }
        if (newActivityDO.getLocation() != null) {
            activityDO.setLocation(newActivityDO.getLocation());
        }
        if (newActivityDO.getOrganizationMessage() != null) {
            activityDO.setOrganizationMessage(newActivityDO.getOrganizationMessage());
        }
        if (newActivityDO.getScore() != null) {
            activityDO.setScore(newActivityDO.getScore());
        }
        if (newActivityDO.getStart() != null) {
            activityDO.setStart(newActivityDO.getStart());
        }
        if (newActivityDO.getState() != null) {
            activityDO.setState(newActivityDO.getState());
        }
        if (newActivityDO.getTerm() != null) {
            activityDO.setTerm(newActivityDO.getTerm());
        }
        if (newActivityDO.getType() != null) {
            activityDO.setType(newActivityDO.getType());
        }
        if (newActivityDO.getUserId() != null) {
            activityDO.setUserId(newActivityDO.getUserId());
        }
        if (newActivityDO.getExtInfo() != null) {
            activityDO.setExtInfo(newActivityDO.getExtInfo());
        }
        if(newActivityDO.getApplicationStamper()!=0){
            activityDO.setApplicationStamper(newActivityDO.getApplicationStamper());
        }
        if(newActivityDO.getPictureUrl()!=null){
            activityDO.setPictureUrl(newActivityDO.getPictureUrl());
        }
        activityDO.setModified(true);
        return convert(activityDORepo.save(activityDO));
    }

    @Override
    public ActivityBO queryActivityByActivityId(String activityId) {
        return convert(activityDORepo.findByActivityId(activityId));
    }

    @Override
    public List<ActivityBO> queryActivityByActivityIds(List<String> activityIds) {
        List<ActivityDO> activityDOList = activityDORepo.findAllByActivityIdIn(activityIds);
        return CollectionUtils.toStream(activityDOList)
                .filter(Objects::nonNull)
                .map(this::convert)
                .collect(Collectors.toList());
    }

    @Override
    public ActivityBO queryActivityByActivityName(String name) {
        return convert(activityDORepo.findByActivityName(name));
    }

    @Override
    public PageList<ActivityBO> queryActivityByTermAndStateAndTypePagerDESC(String term, String status, String type, Integer page, Integer limit) {
        Pageable pageable = PageRequest.of(page, limit);
        return new PageList<>(activityDORepo.findAllByTermContainsAndStateContainsAndTypeContainsOrderByActivityIdDesc(pageable, term, status, type), this::convert);
    }

    @Override
    public PageList<ActivityBO> queryActivityByTermAndStateAndTypePagerASC(String term, String status, String type, Integer page, Integer limit) {
        Pageable pageable = PageRequest.of(page, limit);
        return new PageList<>(activityDORepo.findAllByTermContainsAndStateContainsAndTypeContains(pageable, term, status, type), this::convert);
    }

    @Override
    public PastActivityBO getPastByUserId(String userId) {
        return convert(pastActivityDORepo.findByUserId(userId));
    }

    @Override
    public PastActivityBO getPastByStuId(String stuId) {
        return convert(pastActivityDORepo.findByStuId(stuId));
    }

    @Override
    public PastActivityBO updatePastActivity(String userId, PastActivityBO pastActivityBO) {
        PastActivityDO pastActivityDO=pastActivityDORepo.findByUserId(userId);
        if(pastActivityDO==null){
            return null;
        }
        if (Objects.nonNull((pastActivityBO.getPastLectureActivity()))) {
            pastActivityDO.setPastLectureActivity(pastActivityBO.getPastLectureActivity());
        }
        if (Objects.nonNull(pastActivityBO.getPastPracticeActivity())){
            pastActivityDO.setPastPracticeActivity(pastActivityBO.getPastPracticeActivity());
        }
        if(Objects.nonNull(pastActivityBO.getPastSchoolActivity())){
            pastActivityDO.setPastSchoolActivity(pastActivityBO.getPastSchoolActivity());
        }
        if(Objects.nonNull(pastActivityBO.getPastVolunteerActivityTime())){
            pastActivityDO.setPastVolunteerActivityTime(pastActivityBO.getPastVolunteerActivityTime());
        }
        if(Objects.nonNull(pastActivityBO.getUndistributedStamp())){
            pastActivityDO.setUndistributedStamp(pastActivityBO.getUndistributedStamp());
        }
        return convert(pastActivityDORepo.save(pastActivityDO));
    }

    @Override
    public PastActivityBO createPastActivity(PastActivityBO pastActivityBO) {
        PastActivityDO pastActivityDO=convert(pastActivityBO);
        if(StringUtils.isBlank(pastActivityDO.getPastActivityId())){
            pastActivityDO.setPastActivityId(activityBizFactory.getPastActivityId());
        }
        return convert(pastActivityDORepo.save(pastActivityDO));
    }

    @Override
    public PageList<ActivityBO> queryActivityByUserId(String userId, Integer page, Integer limit) {
        Pageable pageable = PageRequest.of(page, limit);
        return new PageList<>(activityDORepo.findByUserId(pageable,userId), this::convert);
    }

    @Override
    public PageList<ActivityBO> queryApproved(String state, String stuId, String activityName, String organizationMessage, Integer page, Integer limit) {
        Pageable pageable = PageRequest.of(page, limit);
        return new PageList<>(activityDORepo.findApproved(pageable,state,stuId,activityName,organizationMessage), this::convert);
    }

    @Override
    public PageList<ActivityBO> queryApprovedAddTime(String state, String stuId, String activityName, String organizationMessage, Long activityStampedStart, Long activityStampedEnd, Integer page, Integer limit) throws ParseException {
        Pageable pageable = PageRequest.of(page, limit);
        String Syear = String.valueOf(activityStampedStart).substring(0,4);
        String Smonth = String.valueOf(activityStampedStart).substring(4,6);
        String Sdate = String.valueOf(activityStampedStart).substring(6,8);
        String Shour = String.valueOf(activityStampedStart).substring(8,10);
        String Sminute = String.valueOf(activityStampedStart).substring(10,12);
        String Ssecond = String.valueOf(activityStampedStart).substring(12,14);

        String Stime = Syear+"-"+Smonth+"-"+Sdate+" "+Shour+":"+Sminute+":"+Ssecond;
        Date sTime = SIMPLE_DATE_FORMAT.parse(Stime);

        String Eyear = String.valueOf(activityStampedEnd).substring(0,4);
        String Emonth = String.valueOf(activityStampedEnd).substring(4,6);
        String Edate = String.valueOf(activityStampedEnd).substring(6,8);
        String Ehour = String.valueOf(activityStampedEnd).substring(8,10);
        String Eminute = String.valueOf(activityStampedEnd).substring(10,12);
        String Esecond = String.valueOf(activityStampedEnd).substring(12,14);
        String Etime = Eyear+"-"+Emonth+"-"+Edate+" "+Ehour+":"+Eminute+":"+Esecond;

        Date eTime = SIMPLE_DATE_FORMAT.parse(Etime);

        return new PageList<>(activityDORepo.findApprovedAddTime(pageable,state,stuId,activityName,organizationMessage,sTime,eTime), this::convert);
    }

    @Override
    public void updateActivityStampedTimeByActivityId(Date activityStampedStart, Date activityStampedEnd, String activityId) {
        int i=activityDORepo.updateActivityStampedTimeByActivityId(activityStampedStart,activityStampedEnd,activityId);
        if(i==0){
            throw new BetahouseException(CommonResultCode.SYSTEM_ERROR,"修改未成功");
        }
    }

    @Override
    public PageList<ActivityBO> queryApprovedBy(String stuId, String activityName, String organizationMessage, Long start, Long end, Integer page, Integer limit) throws ParseException {
        Pageable pageable = PageRequest.of(page, limit);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1 = new Date(start);
        Date sTime = SIMPLE_DATE_FORMAT.parse(sdf.format(d1));
        Date d2 = new Date(end);
        Date eTime = SIMPLE_DATE_FORMAT.parse(sdf.format(d2));
        return new PageList<>(activityDORepo.findApprovedBy(pageable,stuId,activityName,organizationMessage,sTime,eTime), this::convert);
    }

    @Override
    public PageList<ActivityBO> findCanceledBy(String stuId, String activityName, String organizationMessage, Long start, Long end, Integer page, Integer limit) throws ParseException {
        Pageable pageable = PageRequest.of(page, limit);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1 = new Date(start);
        Date sTime = SIMPLE_DATE_FORMAT.parse(sdf.format(d1));
        Date d2 = new Date(end);
        Date eTime = SIMPLE_DATE_FORMAT.parse(sdf.format(d2));
        return new PageList<>(activityDORepo.findCanceledBy(pageable,stuId,activityName,organizationMessage,sTime,eTime), this::convert);
    }

    @Override
    public PageList<ActivityBO> findCreatedThisWeek(Integer page, Integer limit,String activityName) {
        Pageable pageable = PageRequest.of(page, limit);
        return new PageList<>(activityDORepo.findCreatedThisWeek(pageable,activityName), this::convert);
    }

    @Override
    public PageList<ActivityBO> findApprovedThisWeek(Integer page, Integer limit,String activityName) {
        Pageable pageable = PageRequest.of(page, limit);
        return new PageList<>(activityDORepo.findApprovedThisWeek(pageable,activityName), this::convert);
    }

    /**
     * 活动审批通过
     *
     * @param activityBO
     * @return
     */
    @Override
    public ActivityBO publishActivity(ActivityBO activityBO) {
        if (StringUtils.isBlank(activityBO.getActivityId()) && !activityDORepo.existsActivityDOByActivityId(activityBO.getActivityId())) {
            LoggerUtil.error(LOGGER, "审批通过的活动不存在", activityBO);
            throw new BetahouseException(CommonResultCode.ILLEGAL_PARAMETERS.getCode(), "审批通过的活动不存在");
        }
        ActivityDO activityDO = activityDORepo.findByActivityId(activityBO.getActivityId());
        activityDO.setState("PUBLISHED");
        activityDO.setApprovedTime(new Date());
        return convert(activityDORepo.save(activityDO));
    }
    /**
     * 活动驳回
     *
     * @param activityBO
     * @return
     */
    @Override
    public ActivityBO cancelActivity(ActivityBO activityBO) {
        if (StringUtils.isBlank(activityBO.getActivityId()) && !activityDORepo.existsActivityDOByActivityId(activityBO.getActivityId())) {
            LoggerUtil.error(LOGGER, "取消的活动不存在", activityBO);
            throw new BetahouseException(CommonResultCode.ILLEGAL_PARAMETERS.getCode(), "取消的活动不存在");
        }
        ActivityDO activityDO = activityDORepo.findByActivityId(activityBO.getActivityId());
        activityDO.setState("CANCELED");
        activityDO.setCancelReason(activityBO.getCancelReason());
        activityDO.setModified(false);
        return convert(activityDORepo.save(activityDO));
    }


    @Override
    public PageList<ActivityBO> queryApprovedActivityByUserId(String userId, Integer page, Integer limit) {
        Pageable pageable = PageRequest.of(page, limit);
        return new PageList<>(activityDORepo.findApprovedByUserId(pageable,userId), this::convert);
    }
    @Override
    public PageList<ActivityBO> queryCanceledActivityByUserId(String userId, Integer page, Integer limit) {
        Pageable pageable = PageRequest.of(page, limit);
        return new PageList<>(activityDORepo.findCanceledByUserId(pageable,userId), this::convert);
    }

    @Override
    public List<ActivityBO> convert(List<ActivityDO> activityDOs) {
        return CollectionUtils.toStream(activityDOs).filter(Objects::nonNull).map(this::convert).collect(Collectors.toList());
    }

    private Object convert(Object o) {
        if(o instanceof ActivityDO){
            return convert((ActivityDO)o);
        }else if(o instanceof ActivityBO){
            return convert((ActivityBO)o);
        }else if(o instanceof PastActivityBO) {
            return convert((PastActivityBO) o);
        }else if(o instanceof PastActivityDO) {
            return convert((PastActivityDO) o);
        }else{
            return null;
        }
    }

    /**
     * 活动DO2BO
     *
     * @param activityDO
     * @return
     */
    @SuppressWarnings("unchecked")
    private ActivityBO convert(ActivityDO activityDO) {
        if (activityDO == null) {
            return null;
        }
        ActivityBO activityBO = new ActivityBO();
        activityBO.setActivityId(activityDO.getActivityId());
        activityBO.setActivityName(activityDO.getActivityName());
        activityBO.setType(activityDO.getType());
        activityBO.setOrganizationMessage(activityDO.getOrganizationMessage());
        activityBO.setLocation(activityDO.getLocation());
        activityBO.setStart(activityDO.getStart());
        activityBO.setEnd(activityDO.getEnd());
        activityBO.setScore(activityDO.getScore());
        activityBO.setApplicationStamper(activityDO.getApplicationStamper());
        activityBO.setDescription(activityDO.getDescription());
        activityBO.setCreatorId(activityDO.getUserId());
        activityBO.setState(activityDO.getState());
        activityBO.setTerm(activityDO.getTerm());
        activityBO.setActivityStampedStart(activityDO.getActivityStampedStart());
        activityBO.setActivityStampedEnd(activityDO.getActivityStampedEnd());
        activityBO.setCancelReason(activityDO.getCancelReason());
        activityBO.setApprovedTime(activityDO.getApprovedTime());
        activityBO.setModified(activityDO.getModified());
        activityBO.setPictureUrl(activityDO.getPictureUrl());
        activityBO.setExtInfo(JSON.parseObject(activityDO.getExtInfo(), Map.class));
        return activityBO;
    }

    /**
     * 活动BO2DO
     *
     * @param activityBO
     * @return
     */
    private ActivityDO convert(ActivityBO activityBO) {
        if (activityBO == null) {
            return null;
        }
        ActivityDO activityDO = new ActivityDO();
        activityDO.setActivityId(activityBO.getActivityId());
        activityDO.setActivityName(activityBO.getActivityName());
        activityDO.setType(activityBO.getType());
        activityDO.setOrganizationMessage(activityBO.getOrganizationMessage());
        activityDO.setLocation(activityBO.getLocation());
        activityDO.setStart(activityBO.getStart());
        activityDO.setEnd(activityBO.getEnd());
        activityDO.setScore(activityBO.getScore());
        activityDO.setApplicationStamper(activityBO.getApplicationStamper());
        activityDO.setDescription(activityBO.getDescription());
        activityDO.setUserId(activityBO.getCreatorId());
        activityDO.setState(activityBO.getState());
        activityDO.setTerm(activityBO.getTerm());
        activityDO.setActivityStampedStart(activityBO.getActivityStampedStart());
        activityDO.setActivityStampedEnd(activityBO.getActivityStampedEnd());
        activityDO.setCancelReason(activityBO.getCancelReason());
        activityDO.setApprovedTime(activityBO.getApprovedTime());
        activityDO.setModified(activityBO.getModified());
        activityDO.setPictureUrl(activityBO.getPictureUrl());
        activityDO.setExtInfo(JSON.toJSONString(activityBO.getExtInfo()));
        return activityDO;
    }

    private PastActivityBO convert(PastActivityDO pastActivityDO){
        if(pastActivityDO==null){
            return null;
        }
        PastActivityBO pastActivityBO = new PastActivityBO();
        pastActivityBO.setPastActivityId(pastActivityDO.getPastActivityId());
        pastActivityBO.setUserId(pastActivityDO.getUserId());
        pastActivityBO.setStuId(pastActivityDO.getStuId());
        pastActivityBO.setUndistributedStamp(pastActivityDO.getUndistributedStamp());
        pastActivityBO.setPastSchoolActivity(pastActivityDO.getPastSchoolActivity());
        pastActivityBO.setPastLectureActivity(pastActivityDO.getPastLectureActivity());
        pastActivityBO.setPastVolunteerActivityTime(pastActivityDO.getPastVolunteerActivityTime());
        pastActivityBO.setPastPracticeActivity(pastActivityDO.getPastPracticeActivity());
        return pastActivityBO;
    }
    private PastActivityDO convert(PastActivityBO pastActivityBO){
        if(pastActivityBO==null){
            return null;
        }
        PastActivityDO pastActivityDO = new PastActivityDO();
        pastActivityDO.setPastActivityId(pastActivityBO.getPastActivityId());
        pastActivityDO.setUserId(pastActivityBO.getUserId());
        pastActivityDO.setStuId(pastActivityBO.getStuId());
        pastActivityDO.setUndistributedStamp(pastActivityBO.getUndistributedStamp());
        pastActivityDO.setPastSchoolActivity(pastActivityBO.getPastSchoolActivity());
        pastActivityDO.setPastLectureActivity(pastActivityBO.getPastLectureActivity());
        pastActivityDO.setPastVolunteerActivityTime(pastActivityBO.getPastVolunteerActivityTime());
        pastActivityDO.setPastPracticeActivity(pastActivityBO.getPastPracticeActivity());
        return pastActivityDO;

    }

    @Override
    public List<ActivityBO> findCreatedThisWeekNotPage(String activityName) {
        List<ActivityBO> activityBOList = new ArrayList<>();
        List<ActivityDO> createdThisWeekNotPage = (activityDORepo.findCreatedThisWeekNotPage(activityName));
        for (ActivityDO activityDO : createdThisWeekNotPage) {
            ActivityBO activityBO = convert(activityDO);
            activityBOList.add(activityBO);
        }
        return activityBOList;
    }

    @Override
    public Integer queryActualNumPastMonthByOrganizationMessage(String organizationMessage) {
        return activityDORepo.queryActualNumPastMonthByOrganizationMessage(organizationMessage);
    }

    @Override
    public Integer querySignNumPastMonthByOrganizationMessage(String organizationMessage) {
        return activityDORepo.querySignNumPastMonthByOrganizationMessage(organizationMessage);
    }

    @Override
    public Integer findLectureActivityNum(String term) {
        return activityDORepo.findLectureActivityNum(term);
    }

    @Override
    public Integer findSchoolActivityNum(String term) {
        return activityDORepo.findSchoolActivityNum(term);
    }

    @Override
    public Integer findAllActivityNum(String term) {
        return activityDORepo.findAllActivityNum(term);
    }

    @Override
    public List<String> findActivityName() {
        return activityDORepo.findActivityName();
    }

    @Override
    public List<Date> findActivityTime() {
        return activityDORepo.findActivityTime();
    }

    @Override
    public List<String> findActivityLocation() {
        return activityDORepo.findActivityLocation();
    }

    @Override
    public List<String> findActivityType() {
        return activityDORepo.findActivityType();
    }
}
