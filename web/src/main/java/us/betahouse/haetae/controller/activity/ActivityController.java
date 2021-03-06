/*
  betahouse.us
  CopyRight (c) 2012 - 2018
 */
package us.betahouse.haetae.controller.activity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.csvreader.CsvWriter;
import com.mysql.cj.xdevapi.JsonArray;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import us.betahouse.haetae.activity.dal.service.ActivityRepoService;
import us.betahouse.haetae.activity.enums.ActivityRecordStateEnum;
import us.betahouse.haetae.activity.enums.ActivityStateEnum;
import us.betahouse.haetae.activity.enums.ActivityTypeEnum;
import us.betahouse.haetae.activity.manager.ActivityManager;
import us.betahouse.haetae.activity.model.basic.ActivityBO;
import us.betahouse.haetae.activity.model.basic.YouthLearningBO;
import us.betahouse.haetae.activity.model.common.PageList;
import us.betahouse.haetae.common.log.LoggerName;
import us.betahouse.haetae.common.session.CheckLogin;
import us.betahouse.haetae.common.template.RestOperateCallBack;
import us.betahouse.haetae.common.template.RestOperateTemplate;
import us.betahouse.haetae.model.activity.PastActivityVO;
import us.betahouse.haetae.model.activity.request.ActivityRestRequest;
import us.betahouse.haetae.model.activity.request.AuditRestRequest;
import us.betahouse.haetae.model.activity.request.YouthLearnRequest;
import us.betahouse.haetae.organization.dal.model.OrganizationDO;
import us.betahouse.haetae.organization.dal.repo.OrganizationRepo;
import us.betahouse.haetae.serviceimpl.activity.constant.ActivityCreatorId;
import us.betahouse.haetae.serviceimpl.activity.constant.ActivityExtInfoKey;
import us.betahouse.haetae.serviceimpl.activity.enums.ActivityOperationEnum;
import us.betahouse.haetae.serviceimpl.activity.model.AuditMessage;
import us.betahouse.haetae.serviceimpl.activity.request.ActivityManagerRequest;
import us.betahouse.haetae.serviceimpl.activity.request.YouthLearningRequest;
import us.betahouse.haetae.serviceimpl.activity.request.builder.ActivityManagerRequestBuilder;
import us.betahouse.haetae.serviceimpl.activity.service.ActivityService;
import us.betahouse.haetae.serviceimpl.activity.service.YouthLearningService;
import us.betahouse.haetae.serviceimpl.activity.service.impl.YouthLearningServiceImpl;
import us.betahouse.haetae.serviceimpl.common.OperateContext;
import us.betahouse.haetae.serviceimpl.common.utils.AuditUtil;
import us.betahouse.haetae.serviceimpl.common.utils.TermUtil;
import us.betahouse.haetae.serviceimpl.organization.service.OrganizationService;
import us.betahouse.haetae.serviceimpl.schedule.manager.AccessTokenManage;
import us.betahouse.haetae.serviceimpl.user.request.PermRequest;
import us.betahouse.haetae.serviceimpl.user.service.PermService;
import us.betahouse.haetae.serviceimpl.user.service.UserService;
import us.betahouse.haetae.user.dal.service.PermRepoService;
import us.betahouse.haetae.user.model.basic.perm.PermBO;
import us.betahouse.haetae.utils.IPUtil;
import us.betahouse.haetae.utils.RestResultUtil;
import us.betahouse.util.common.Result;
import us.betahouse.util.enums.CommonResultCode;
import us.betahouse.util.enums.RestResultCode;
import us.betahouse.util.exceptions.BetahouseException;
import us.betahouse.util.log.Log;
import us.betahouse.util.template.OperateCallBack;
import us.betahouse.util.template.OperateTemplate;
import us.betahouse.util.utils.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ????????????
 *
 * @author MessiahJK
 * @version : ActivityController.java 2018/11/25 13:16 MessiahJK
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/activity")
public class ActivityController {
    /**
     * ????????????
     */
    private final Logger LOGGER = LoggerFactory.getLogger(ActivityController.class);

    @Autowired
    private ActivityService activityService;
    
    @Autowired
    private ActivityRepoService activityRepoService;

    @Autowired
    private UserService userService;

    @Autowired
    private ActivityManager activityManager;

    @Autowired
    private PermService permService;

    @Autowired
    private PermRepoService permRepoService;

    @Autowired
    private YouthLearningService youthLearningService;

    @Autowired
    private OrganizationRepo organizationRepo;
    /**
     * ????????????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @PostMapping
    //?????????????????????modified?????????false
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<ActivityBO> add(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "????????????", request, new RestOperateCallBack<ActivityBO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
                AssertUtil.assertStringNotBlank(request.getActivityName(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getActivityType(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "????????????????????????");
                AssertUtil.assertNotNull(request.getActivityStartTime(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????????????????");
                AssertUtil.assertNotNull(request.getActivityEndTime(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????????????????");
                boolean validateTime = new Date(request.getActivityStartTime()).before(new Date(request.getActivityEndTime()));
                AssertUtil.assertTrue(validateTime, "??????????????????????????????????????????");
                AssertUtil.assertNotNull(request.getOrganizationMessage(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "????????????????????????");
                AssertUtil.assertNotNull(request.getActivityStampedStart(),RestResultCode.ILLEGAL_PARAMETERS.getCode(),"????????????????????????????????????");
                AssertUtil.assertNotNull(request.getActivityStampedEnd(),RestResultCode.ILLEGAL_PARAMETERS.getCode(),"????????????????????????????????????");
                boolean validateStampTime=new Date(request.getActivityStampedStart()).before(new Date(request.getActivityStampedEnd()));
                AssertUtil.assertTrue(validateStampTime,RestResultCode.ILLEGAL_PARAMETERS,"??????????????????????????????????????????");
            }

            @Override
            public Result<ActivityBO> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequest activityManagerRequest = ActivityManagerRequestBuilder.getInstance()
                        .withUserId(request.getUserId())
                        .withActivityName(request.getActivityName())
                        .withType(request.getActivityType())
                        .withOrganizationMessage(request.getOrganizationMessage())
                        .withStart(request.getActivityStartTime())
                        .withEnd(request.getActivityEndTime())
                        .withTerm(request.getTerm() == null ? TermUtil.getNowTerm() : request.getTerm())
                        .withActivityStampedTimeStart(request.getActivityStampedStart())
                        .withActivityStampedTimeEnd(request.getActivityStampedEnd())
                        .withApplicationStamper(request.getApplicationStamper())
                        .withPictureUrl(request.getPictureUrl())
                        // ?????????????????????
                        // ??????
                        .withDescription(request.getDescription())
                        // ??????
                        .withLocation(request.getLocation())
                        // ??????
                        .withScore(request.getScore())
                        .build();
                ActivityBO activityBO = activityService.create(activityManagerRequest, context);
                return RestResultUtil.buildSuccessResult(activityBO, "??????????????????");
            }
        });
    }

    /**
     * ????????????????????????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<ActivityBO>> getActivityList(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "??????????????????", request, new RestOperateCallBack<PageList<ActivityBO>>() {

            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
            }

            @Override
            public Result<PageList<ActivityBO>> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));

                ActivityManagerRequestBuilder builder = ActivityManagerRequestBuilder.getInstance();

                // ????????????????????????
                if (StringUtils.isNotBlank(request.getState())) {
                    builder.withState(request.getState());
                }

                // ????????????????????????
                if (StringUtils.isNotBlank(request.getTerm())) {
                    builder.withTerm(request.getTerm());
                }

                // ????????????????????????
                if(StringUtils.isNotBlank(request.getActivityType())){
                    builder.withType(request.getActivityType());
                }

                //????????????
                if(request.getPage()!=null&&request.getPage()!=0){
                    builder.withPage(request.getPage());
                }

                //??????????????????
                if(request.getLimit()!=null&&request.getLimit()!=0){
                    builder.withLimit(request.getLimit());
                }

                //??????????????????
                if(StringUtils.isBlank(request.getOrderRule())){
                    builder.withOrderRule(request.getOrderRule());
                }
                return RestResultUtil.buildSuccessResult(activityService.findAll(builder.build(), context), "????????????????????????");
            }
        });
    }
    
    /**
     * ??????????????????????????????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/organizers")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<JSONArray> getOrganizers(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "??????????????????????????????", request, new RestOperateCallBack<JSONArray>() {
            
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
            }
            
            @Override
            public Result<JSONArray> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                
                List<String> organizers = new ArrayList<>();
                activityRepoService.queryAllActivity().forEach(n -> organizers.add(n.getOrganizationMessage()));
                // ??????
                List<String> out = organizers.stream().distinct().collect(Collectors.toList());
                return RestResultUtil.buildSuccessResult(JSONArray.parseArray(JSON.toJSONString(out)), "????????????????????????????????????");
            }
        });
    }

    /**
     * ??????????????????????????????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/organizers/all")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<JSONArray> getOrganizer(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "??????????????????????????????", request, new RestOperateCallBack<JSONArray>() {

            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
            }

            @Override
            public Result<JSONArray> execute() {
                List<OrganizationDO> all = organizationRepo.findAll();
                List<String> list=new ArrayList<>();
                for (int i = 0; i < all.size(); i++) {
                    list.add(all.get(i).getOrganizationName());
                }
                return RestResultUtil.buildSuccessResult(JSONArray.parseArray(JSON.toJSONString(list)),"????????????????????????????????????");
            }
        });
    }

    /**
     * (???)??????????????????????????????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/neworganizers")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<List<String>> newGetOrganizers(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "(???)??????????????????????????????", request, new RestOperateCallBack<List<String>>() {

            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
            }

            @Override
            public Result<List<String>> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                return RestResultUtil.buildSuccessResult(activityRepoService.queryAllOrganization(), "(???)????????????????????????????????????");
            }
        });
    }

    /**
     * ????????????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @PutMapping
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<ActivityBO> operate(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "????????????", request, new RestOperateCallBack<ActivityBO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getActivityId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????id????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
                AssertUtil.assertStringNotBlank(request.getOperation(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
            }

            @Override
            public Result<ActivityBO> execute() {
                // ?????????????????????
                ActivityOperationEnum operation = ActivityOperationEnum.getByCode(request.getOperation());
                AssertUtil.assertNotNull(operation, "?????????????????????");
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequest activityManagerRequest = ActivityManagerRequestBuilder.getInstance()
                        .withActivityId(request.getActivityId())
                        .withUserId(request.getUserId())
                        .withOperation(request.getOperation())
                        .build();
                ActivityBO activityBO = activityService.operate(activityManagerRequest, context);
                return RestResultUtil.buildSuccessResult(activityBO, MessageFormat.format("??????{0}??????", operation.getDesc()));
            }
        });
    }
    /**
     * ??????????????????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping("/past")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PastActivityVO> getPastActivity(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "????????????", request, new RestOperateCallBack<PastActivityVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
            }

            @Override
            public Result<PastActivityVO> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequest activityManagerRequest=new ActivityManagerRequest();
                activityManagerRequest.setUserId(request.getUserId());
                return RestResultUtil.buildSuccessResult(PastActivityVO.valueOf(activityService.getPastActivity(activityManagerRequest, context)));
            }
        });
    }
    /**
     * ????????????????????????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @PostMapping("/past")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PastActivityVO> assignPastActivity(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "????????????", request, new RestOperateCallBack<PastActivityVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
                AssertUtil.assertNotNull(request.getUndistributedStamp(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????????????????");
                AssertUtil.assertNotNull(request.getPastSchoolActivity(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????????????????");
                AssertUtil.assertNotNull(request.getPastLectureActivity(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????????????????");
            }

            @Override
            public Result<PastActivityVO> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequest activityManagerRequest=new ActivityManagerRequest();
                activityManagerRequest.setUserId(request.getUserId());
                activityManagerRequest.setUndistributedStamp(request.getUndistributedStamp());
                activityManagerRequest.setPastSchoolActivity(request.getPastSchoolActivity());
                activityManagerRequest.setPastLectureActivity(request.getPastLectureActivity());
                activityService.assignPastRecord(activityManagerRequest, context);
                return RestResultUtil.buildSuccessResult(PastActivityVO.valueOf(activityService.getPastActivity(activityManagerRequest, context)));
            }
        });
    }

    @CheckLogin
    @PostMapping("/audit")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<AuditRestRequest> auditActivity(AuditRestRequest request , HttpServletRequest httpServletRequest){
        return RestOperateTemplate.operate(LOGGER, "??????????????????", request, new RestOperateCallBack<AuditRestRequest>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
                AssertUtil.assertStringNotBlank(request.getAuditId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "????????????id????????????");
                AssertUtil.assertStringNotBlank(request.getResult(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "????????????????????????");
                AssertUtil.assertStringNotBlank(request.getDetail(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "????????????????????????");
                AssertUtil.assertStringNotBlank(request.getAuditTime(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "????????????????????????");
                AssertUtil.assertStringNotBlank(request.getApplicant(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
            }

            @Override
            public Result<AuditRestRequest> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                AuditMessage message = new AuditMessage();
                BeanUtils.copyProperties(request,message);
                String openid =  userService.queryByUserId(request.getAuditId(),context).getOpenId();
                if (StringUtils.isEmpty(openid))
                    return RestResultUtil.buildSuccessResult(request , "??????????????????");
                String token = AccessTokenManage.GetToken();
                String result = AuditUtil.publishAuditByOpenId(request.getPage(),openid,token,message);
                if (StringUtils.equals(CommonResultCode.FORBIDDEN.getCode(),result)){
                    return  RestResultUtil.buildSuccessResult(request , "??????????????????????????????");
                }
                return RestResultUtil.buildSuccessResult(request , "?????????????????????");
            }

        });
    }

    @CheckLogin
    @GetMapping("/approved")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<ActivityBO>> getApprovedActivity(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "??????????????????????????????", request, new RestOperateCallBack<PageList<ActivityBO>>() {

            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
            }

            @Override
            public Result<PageList<ActivityBO>> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));

                //??????????????????????????????
                int page=0;
                int limit=10;

                //????????????
                if(request.getPage()!=null&&request.getPage()>0){
                    page=request.getPage()-1;
                }

                //??????????????????
                if(request.getLimit()!=null&&request.getLimit()>0){
                    limit=request.getLimit();
                }
                ActivityManagerRequest activityManagerRequest=new ActivityManagerRequest();
                activityManagerRequest.setUserId(request.getUserId());
                activityManagerRequest.setPage(page);
                activityManagerRequest.setLimit(limit);
                if(request.getActivityName()!=null&&!request.getActivityName().equals("")){
                    activityManagerRequest.setActivityName(request.getActivityName());
                }
                if(request.getOrganizationMessage()!=null&&!request.getOrganizationMessage().equals("")){
                    activityManagerRequest.setOrganizationMessage(request.getOrganizationMessage());
                }
                if(request.getActivityStampedStart()!=null&&request.getActivityStampedEnd()!=null){
                    activityManagerRequest.setActivityStampedTimeStart(request.getActivityStampedStart());
                    activityManagerRequest.setActivityStampedTimeEnd(request.getActivityStampedEnd());
                }
                if(request.getSearchCreatorStuId()!=null&&!request.getSearchCreatorStuId().equals("")){
                    activityManagerRequest.setStuId(request.getSearchCreatorStuId());
                }
                return RestResultUtil.buildSuccessResult(activityService.findApprovedActivity(activityManagerRequest,context),"??????????????????????????????");
            }
        });
    }

    @CheckLogin
    @Log(loggerName = LoggerName.WEB_DIGEST)
    @PutMapping("/updatestampedtime")
    public Result<ActivityBO> updateStampedTime(ActivityRestRequest request,HttpServletRequest httpServletRequest){
        return RestOperateTemplate.operate(LOGGER, "????????????????????????", request, new RestOperateCallBack<ActivityBO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
                AssertUtil.assertStringNotBlank(request.getActivityStampedStart().toString(),"????????????????????????????????????");
                AssertUtil.assertStringNotBlank(request.getActivityStampedEnd().toString(),"????????????????????????????????????");
                boolean validateStampTime=new Date(request.getActivityStampedStart()).before(new Date(request.getActivityStampedEnd()));
                AssertUtil.assertTrue(validateStampTime,RestResultCode.ILLEGAL_PARAMETERS,"??????????????????????????????????????????");
                AssertUtil.assertStringNotBlank(request.getActivityId(),"??????id????????????");
            }

            @Override
            public Result<ActivityBO> execute() {
                ActivityManagerRequest activityManagerRequest=new ActivityManagerRequest();
                activityManagerRequest.setUserId(request.getUserId());
                activityManagerRequest.setActivityId(request.getActivityId());
                activityManagerRequest.setActivityStampedTimeStart(request.getActivityStampedStart());
                activityManagerRequest.setActivityStampedTimeEnd(request.getActivityStampedEnd());
                OperateContext context=new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                activityService.updateActivityStampedTimeByActivityId(activityManagerRequest,context);
                return RestResultUtil.buildSuccessResult(activityRepoService.queryActivityByActivityId(request.getActivityId()),"????????????????????????");
            }
        });
    }

    /**
     * ?????????????????????userId??????????????????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/getActivityListByUserID")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<ActivityBO>> getActivityListByUserID(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "?????????????????????userId??????????????????", request, new RestOperateCallBack<PageList<ActivityBO>>() {

            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
            }

            @Override
            public Result<PageList<ActivityBO>> execute() {
                //???????????????status???Finish?????????????????????
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));

                ActivityManagerRequestBuilder builder = ActivityManagerRequestBuilder.getInstance();

                //????????????
                if(request.getPage()!=null&&request.getPage()!=0){
                    builder.withPage(request.getPage());
                }

                //??????????????????
                if(request.getLimit()!=null&&request.getLimit()!=0){
                    builder.withLimit(request.getLimit());
                }

                //??????????????????
                if(StringUtils.isBlank(request.getOrderRule())){
                    builder.withOrderRule(request.getOrderRule());
                }

                builder.withUserId(request.getUserId());

                return RestResultUtil.buildSuccessResult(activityService.findByUserId(builder.build(), context), "?????????????????????userId??????????????????");
            }
        });
    }

    /**
     * ????????????????????????????????????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/getApprovedActivityList")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<ActivityBO>> getApprovedActivityList(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "????????????????????????????????????", request, new RestOperateCallBack<PageList<ActivityBO>>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
//                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
            }

            @Override
            public Result<PageList<ActivityBO>> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));

                //??????????????????????????????
                int page=1;
                int limit=10;

                //????????????
                if(request.getPage()!=null&&request.getPage()>0){
                    page=request.getPage();
                }

                //??????????????????
                if(request.getLimit()!=null&&request.getLimit()>0){
                    limit=request.getLimit();
                }
                ActivityManagerRequest activityManagerRequest=new ActivityManagerRequest();
                activityManagerRequest.setUserId(request.getUserId());
                activityManagerRequest.setPage(page);
                activityManagerRequest.setLimit(limit);
                if(request.getActivityName()!=null&&!request.getActivityName().equals("")){
                    activityManagerRequest.setActivityName(request.getActivityName());
                }
                if(request.getOrganizationMessage()!=null&&!request.getOrganizationMessage().equals("")){
                    activityManagerRequest.setOrganizationMessage(request.getOrganizationMessage());
                }
                if(request.getActivityStampedStart()!=null&&request.getActivityStampedEnd()!=null){
                    activityManagerRequest.setActivityStampedTimeStart(request.getActivityStampedStart());
                    activityManagerRequest.setActivityStampedTimeEnd(request.getActivityStampedEnd());
                }
                if(request.getSearchCreatorStuId()!=null&&!request.getSearchCreatorStuId().equals("")){
                    activityManagerRequest.setStuId(request.getSearchCreatorStuId());
                }
                return RestResultUtil.buildSuccessResult(activityService.findApproved(activityManagerRequest,context),"????????????????????????????????????");
            }
        });
    }

    /**
     * ???????????????????????????????????? ?????????????????????????????????????????????????????????????????????????????????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/approved/by")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<ActivityBO>> getApprovedActivityBy(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "????????????????????????????????????", request, new RestOperateCallBack<PageList<ActivityBO>>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
            }
            @Override
            public Result<PageList<ActivityBO>> execute() throws ParseException {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequestBuilder builder = ActivityManagerRequestBuilder.getInstance();
                if(request.getPage()!=null&&request.getPage()!=0){
                    builder.withPage(request.getPage());
                }
                if(request.getLimit()!=null&&request.getLimit()!=0){
                    builder.withLimit(request.getLimit());
                }
                if(StringUtils.isBlank(request.getOrderRule())){
                    builder.withOrderRule(request.getOrderRule());
                }
                if (StringUtils.isNotBlank(request.getSearchCreatorStuId())) {//???????????????StuId?????????StuId???UserId
                    builder.withUserId(request.getSearchCreatorStuId());
                }
                if (StringUtils.isNotBlank(request.getActivityName())) {
                    builder.withActivityName(request.getActivityName());
                }
                if (StringUtils.isNotBlank(request.getOrganizationMessage())) {
                    builder.withOrganizationMessage(request.getOrganizationMessage());
                }
                if (StringUtils.isNotBlank(String.valueOf(request.getActivityStampedEnd()))) {
                    builder.withActivityStampedTimeEnd( request.getActivityStampedEnd());
                }
                if (StringUtils.isNotBlank(String.valueOf(request.getActivityStampedStart()))) {
                    builder.withActivityStampedTimeStart(request.getActivityStampedStart());
                }
                return RestResultUtil.buildSuccessResult(activityService.findApprovedBy(builder.build(), context), "??????????????????????????????????????????");
            }
        });
    }


    /**
     * ???????????????????????????????????? ?????????????????????????????????????????????????????????????????????????????????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/canceled/by")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<ActivityBO>> getCanceledActivityBy(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "????????????????????????????????????", request, new RestOperateCallBack<PageList<ActivityBO>>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
            }
            @Override
            public Result<PageList<ActivityBO>> execute() throws ParseException {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequestBuilder builder = ActivityManagerRequestBuilder.getInstance();
                if(request.getPage()!=null&&request.getPage()!=0){
                    builder.withPage(request.getPage());
                }
                if(request.getLimit()!=null&&request.getLimit()!=0){
                    builder.withLimit(request.getLimit());
                }
                if(StringUtils.isBlank(request.getOrderRule())){
                    builder.withOrderRule(request.getOrderRule());
                }
                if (StringUtils.isNotBlank(request.getSearchCreatorStuId())) {//???????????????StuId?????????StuId???UserId
                    builder.withUserId(request.getSearchCreatorStuId());
                }
                if (StringUtils.isNotBlank(request.getActivityName())) {
                    builder.withActivityName(request.getActivityName());
                }
                if (StringUtils.isNotBlank(request.getOrganizationMessage())) {
                    builder.withOrganizationMessage(request.getOrganizationMessage());
                }
                if (StringUtils.isNotBlank(String.valueOf(request.getActivityStampedEnd()))) {
                    builder.withActivityStampedTimeEnd( request.getActivityStampedEnd());
                }
                if (StringUtils.isNotBlank(String.valueOf(request.getActivityStampedStart()))) {
                    builder.withActivityStampedTimeStart(request.getActivityStampedStart());
                }
                return RestResultUtil.buildSuccessResult(activityService.findCanceledBy(builder.build(), context), "??????????????????????????????????????????");
            }
        });
    }


    /**
     * ?????????????????????????????????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/week/created")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<ActivityBO>> getCreatedActivityListThisWeek(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "?????????????????????????????????", request, new RestOperateCallBack<PageList<ActivityBO>>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
            }
            @Override
            public Result<PageList<ActivityBO>> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequestBuilder builder = ActivityManagerRequestBuilder.getInstance();
                if(request.getPage()!=null&&request.getPage()!=0){
                    builder.withPage(request.getPage());
                }
                if(request.getLimit()!=null&&request.getLimit()!=0){
                    builder.withLimit(request.getLimit());
                }
                if(StringUtils.isBlank(request.getOrderRule())){
                    builder.withOrderRule(request.getOrderRule());
                }
                //??????????????????
                if (StringUtils.isNotBlank(request.getActivityName())) {
                    builder.withActivityName(request.getActivityName());
                }
                return RestResultUtil.buildSuccessResult(activityService.findCreatedThisWeek(builder.build(), context), "???????????????????????????????????????");
            }
        });
    }
    /**
     * ???????????????????????????????????????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/week/approved")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<ActivityBO>> getApprovedActivityListThisWeek(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "???????????????????????????????????????", request, new RestOperateCallBack<PageList<ActivityBO>>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
            }
            @Override
            public Result<PageList<ActivityBO>> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequestBuilder builder = ActivityManagerRequestBuilder.getInstance();
                if(request.getPage()!=null&&request.getPage()!=0){
                    builder.withPage(request.getPage());
                }
                if(request.getLimit()!=null&&request.getLimit()!=0){
                    builder.withLimit(request.getLimit());
                }
                if(StringUtils.isBlank(request.getOrderRule())){
                    builder.withOrderRule(request.getOrderRule());
                }
                //??????????????????
                if (StringUtils.isNotBlank(request.getActivityName())) {
                    builder.withActivityName(request.getActivityName());
                }
                return RestResultUtil.buildSuccessResult(activityService.findApprovedThisWeek(builder.build(), context), "?????????????????????????????????????????????");
            }
        });
    }

    //??????????????????????????????id???????????????id?????????????????????????????????????????????????????????ok

    /**
     * ????????????????????????????????????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/week/unQualified")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<List<ActivityBO>> getUnQualifiedActivityListThisWeek(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "????????????????????????????????????", request, new RestOperateCallBack<List<ActivityBO>>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
            }
            @Override
            public Result<List<ActivityBO>> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequestBuilder builder = ActivityManagerRequestBuilder.getInstance();
                //??????????????????
                if (StringUtils.isNotBlank(request.getActivityName())) {
                    builder.withActivityName(request.getActivityName());
                }
                return RestResultUtil.buildSuccessResult(activityService.findUnQualifiedThisWeek(builder.build(), context), "??????????????????????????????????????????");
            }
        });
    }

    /**
     * ????????????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @PutMapping(value = "/cancel")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<ActivityBO> cancel(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "????????????", request, new RestOperateCallBack<ActivityBO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getActivityId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????id????????????");
            }
            @Override
            public Result<ActivityBO> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequest activityManagerRequest = ActivityManagerRequestBuilder.getInstance()
                        .withActivityId(request.getActivityId())
                        .withCancelReason(request.getCancelReason())
                        .build();
                ActivityBO activityBO = activityService.cancel(activityManagerRequest, context);
                return RestResultUtil.buildSuccessResult(activityBO, "??????????????????");
            }
        });
    }
    /**
     * ????????????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @PutMapping(value = "/publish")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<ActivityBO> publish(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "????????????", request, new RestOperateCallBack<ActivityBO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getActivityId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????id????????????");
            }

            @Override
            public Result<ActivityBO> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequest activityManagerRequest = ActivityManagerRequestBuilder.getInstance()
                        .withActivityId(request.getActivityId())
                        .build();
                ActivityBO activityBO = activityService.publish(activityManagerRequest, context);
                return RestResultUtil.buildSuccessResult(activityBO, "??????????????????");
            }
        });
    }

    /**
     * ????????????id????????????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/ByActivityId")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<ActivityBO> getActivityByActivityID(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "????????????id????????????", request, new RestOperateCallBack<ActivityBO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getActivityId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????id????????????");
            }
            @Override
            public Result<ActivityBO> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequestBuilder builder = ActivityManagerRequestBuilder.getInstance();
                builder.withActivityId(request.getActivityId());
                return RestResultUtil.buildSuccessResult(activityService.findByActivityId(builder.build(), context), "????????????id????????????");
            }
        });
    }
    /**
     * ?????????????????? ??????????????????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @PutMapping(value = "/modify")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<ActivityBO> modify(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "??????????????????", request, new RestOperateCallBack<ActivityBO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
                AssertUtil.assertNotNull(request.getActivityId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????id????????????");
                AssertUtil.assertStringNotBlank(request.getActivityName(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getActivityType(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "????????????????????????");
                AssertUtil.assertNotNull(request.getActivityStartTime(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????????????????");
                AssertUtil.assertNotNull(request.getActivityEndTime(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????????????????");
                boolean validateTime = new Date(request.getActivityStartTime()).before(new Date(request.getActivityEndTime()));
                //false?????????????????????
                AssertUtil.assertTrue(!request.getModified(),"????????????????????????");
                AssertUtil.assertTrue(validateTime, "??????????????????????????????????????????");
                AssertUtil.assertNotNull(request.getOrganizationMessage(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "????????????????????????");
                AssertUtil.assertNotNull(request.getActivityStampedStart(),RestResultCode.ILLEGAL_PARAMETERS.getCode(),"????????????????????????????????????");
                AssertUtil.assertNotNull(request.getActivityStampedEnd(),RestResultCode.ILLEGAL_PARAMETERS.getCode(),"????????????????????????????????????");
                boolean validateStampTime=new Date(request.getActivityStampedStart()).before(new Date(request.getActivityStampedEnd()));
                AssertUtil.assertTrue(validateStampTime,RestResultCode.ILLEGAL_PARAMETERS,"??????????????????????????????????????????");
            }

            @Override
            public Result<ActivityBO> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequest activityManagerRequest = ActivityManagerRequestBuilder.getInstance()
                        .withActivityId(request.getActivityId())
                        .withUserId(request.getUserId())
                        .withActivityName(request.getActivityName())
                        .withType(request.getActivityType())
                        .withOrganizationMessage(request.getOrganizationMessage())
                        .withStart(request.getActivityStartTime())
                        .withEnd(request.getActivityEndTime())
                        .withTerm(request.getTerm() == null ? TermUtil.getNowTerm() : request.getTerm())
                        .withActivityStampedTimeStart(request.getActivityStampedStart())
                        .withActivityStampedTimeEnd(request.getActivityStampedEnd())
                        .withApplicationStamper(request.getApplicationStamper())
                        .withLocation(request.getLocation())
                        .withPictureUrl(request.getPictureUrl())
                        .build();
                ActivityBO activityBO = activityService.modify(activityManagerRequest, context);
                return RestResultUtil.buildSuccessResult(activityBO, "????????????????????????");
            }
        });
    }


    /**
     * ?????????????????????Id????????????????????????????????????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/approved/byUserID")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<ActivityBO>> getApprovedListByUserID(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "?????????????????????Id?????????????????????????????????", request, new RestOperateCallBack<PageList<ActivityBO>>() {

            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
            }

            @Override
            public Result<PageList<ActivityBO>> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequestBuilder builder = ActivityManagerRequestBuilder.getInstance();
                if(request.getPage()!=null&&request.getPage()!=0){
                    builder.withPage(request.getPage());
                }
                if(request.getLimit()!=null&&request.getLimit()!=0){
                    builder.withLimit(request.getLimit());
                }
                if(StringUtils.isBlank(request.getOrderRule())){
                    builder.withOrderRule(request.getOrderRule());
                }
                builder.withUserId(request.getUserId());

                return RestResultUtil.buildSuccessResult(activityService.findApprovedByUserId(builder.build(), context), "?????????????????????Id???????????????????????????????????????");
            }
        });
    }

    /**
     * ?????????????????????Id????????????????????????????????????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/canceled/byUserID")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<ActivityBO>> getCanceledListByUserID(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "?????????????????????Id????????????????????????????????????", request, new RestOperateCallBack<PageList<ActivityBO>>() {

            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
            }

            @Override
            public Result<PageList<ActivityBO>> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequestBuilder builder = ActivityManagerRequestBuilder.getInstance();
                if(request.getPage()!=null&&request.getPage()!=0){
                    builder.withPage(request.getPage());
                }
                if(request.getLimit()!=null&&request.getLimit()!=0){
                    builder.withLimit(request.getLimit());
                }
                if(StringUtils.isBlank(request.getOrderRule())){
                    builder.withOrderRule(request.getOrderRule());
                }
                builder.withUserId(request.getUserId());

                return RestResultUtil.buildSuccessResult(activityService.findCanceledByUserId(builder.build(), context), "?????????????????????Id??????????????????????????????????????????");
            }
        });
    }

    @CheckLogin
    @PostMapping(value = "/youthlearning/batchsave")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<Void> batchSave(YouthLearnRequest request, @PathVariable("file") MultipartFile file, HttpServletRequest httpServletRequest, HttpServletResponse response){
        return RestOperateTemplate.operate(LOGGER, "?????????????????????????????????", httpServletRequest, new RestOperateCallBack<Void>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(file,RestResultCode.ILLEGAL_PARAMETERS.getCode(),"???????????????csv??????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
            }

            @Override
            public Result<Void> execute(){
                try {
                    String[][] values = CsvUtil.getWithHeader(file.getInputStream(),StandardCharsets.UTF_8);
                    int stuClass=-1,stuId=-1,finishTime=-1,activityName=-1;
                    for (int i = 0; i < values[0].length; i++) {
                        switch (values[0][i]){
                            case "??????/??????/???????????????":
                                stuClass=i;
                                break;
                            case "??????/??????/??????":
                                stuId=i;
                                break;
                            case "????????????":
                                finishTime=i;
                                break;
                            case "??????":
                                activityName=i;
                                break;
                        }
                    }
                    if(stuId==-1||finishTime==-1||activityName==-1||stuClass==-1){
                        HttpDownloadUtil.downloadByValue("result.txt","?????????????????????????????????,?????????????????????????????????/??????/??????,??????/??????/???????????????,????????????,??????",response);
                        return RestResultUtil.buildFailResult();
                    }
                    List<YouthLearningBO> youthLearningBOS=new ArrayList<>();
                    SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy/MM/dd HH:mm");
                    for (int i = 1; i < values.length; i++) {
                        String stuid="";
                        for (int j = 0; j < values[i][stuId].length(); j++) {
                            char c = values[i][stuId].charAt(j);
                            if(c>='0'&&c<='9') stuid+=String.valueOf(c);
                        }
                        YouthLearningBO youthLearningBO=new YouthLearningBO();
                        youthLearningBO.setScannerUserId(request.getUserId());
                        youthLearningBO.setType(ActivityTypeEnum.YOUTH_LEARNING_ACTIVITY.getCode());
                        youthLearningBO.setTerm(TermUtil.getNowTerm());
                        youthLearningBO.setStatus(ActivityRecordStateEnum.ENABLE.getCode());
                        youthLearningBO.setActivityName(values[i][activityName]);
                        youthLearningBO.setFinishTime(simpleDateFormat.parse(values[i][finishTime]));
                        youthLearningBO.setClassId(values[i][stuClass]);
                        youthLearningBO.setStuId(values[i][stuId]);
                        youthLearningBOS.add(youthLearningBO);
                    }
                    List<YouthLearningBO> fails=null;
                    YouthLearningServiceImpl.Info info=null;
                    if(youthLearningBOS.size()>0){
                        YouthLearningRequest youthLearningRequest=new YouthLearningRequest();
                        youthLearningRequest.setYouthLearningBOList(youthLearningBOS);
                        youthLearningRequest.setUserId(request.getUserId());
                        info = youthLearningService.batchSaveRecord(youthLearningRequest);
                    }else {
                        HttpDownloadUtil.downloadByValue("result.txt","????????????????????????????????????",response);
                        return RestResultUtil.buildFailResult();
                    }
                    if(info.getRepeat().size()==0&&info.getInfo().length()==0){
                        HttpDownloadUtil.downloadByValue("result.txt","???????????? :) \n"+info.getInfo(),response);
                        return RestResultUtil.buildSuccessResult();
                    }
                    ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
                    CsvWriter csvWriter=new CsvWriter(outputStream,',', StandardCharsets.UTF_8);
                    csvWriter.writeRecord(new String[]{"??????/??????/??????","??????/??????/???????????????", "??????","????????????"});
                    for (int i = 0; i < info.getRepeat().size(); i++) {
                        YouthLearningBO youthLearningBO = info.getRepeat().get(i);
                        String sId=youthLearningBO.getStuId()==null?"":youthLearningBO.getStuId();
                        String name=youthLearningBO.getRealName()==null?"":youthLearningBO.getRealName();
                        String clsid=youthLearningBO.getClassId()==null?"":youthLearningBO.getClassId();
                        csvWriter.writeRecord(new String[]{sId+" "+name,clsid,youthLearningBO.getActivityName(),simpleDateFormat.format(youthLearningBO.getFinishTime())});
                    }
                    csvWriter.flush();
                    csvWriter.close();
                    ByteArrayInputStream infoStream=new ByteArrayInputStream(info.getInfo().toString().getBytes());
                    ByteArrayInputStream resultStream=new ByteArrayInputStream(outputStream.toByteArray());

//                    FileOutputStream fileOutputStream=new FileOutputStream(System.getProperty("user.home")+"/desktop/123.csv");
//                    fileOutputStream.write(outputStream.toByteArray());


                    HttpDownloadUtil.downloadInputStreamZIP("result.zip",response,"sheet.csv",resultStream,"info.txt",infoStream);
                    return RestResultUtil.buildFailResult();
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
                return RestResultUtil.buildFailResult();
            }
        });
    }

    @CheckLogin
    @GetMapping(value = "/youthlearning/byuserid")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<List<YouthLearningBO>> youthLearningByUserId(YouthLearnRequest request,HttpServletRequest httpServletRequest){
        return OperateTemplate.operate(LOGGER, "???????????????????????????????????????????????????", request, new OperateCallBack<List<YouthLearningBO>>() {

            @Override
            public void before() {
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
            }

            @Override
            public Result<List<YouthLearningBO>> execute() {
                YouthLearningRequest youthLearningRequest=new YouthLearningRequest();
                youthLearningRequest.setUserId(request.getUserId());
                return RestResultUtil.buildSuccessResult(youthLearningService.getRecordByUserId(youthLearningRequest));
            }
        });
    }

    @CheckLogin
    @GetMapping(value = "/youthlearning/byuseridnum")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<Integer> youthLearningByUserIdNum(YouthLearnRequest request,HttpServletRequest httpServletRequest){
        return OperateTemplate.operate(LOGGER, "????????????????????????????????????????????????????????????", request, new OperateCallBack<Integer>() {

            @Override
            public void before() {
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
            }

            @Override
            public Result<Integer> execute() {
                YouthLearningRequest youthLearningRequest=new YouthLearningRequest();
                youthLearningRequest.setUserId(request.getUserId());
                return RestResultUtil.buildSuccessResult(youthLearningService.getRecordNumByUserId(youthLearningRequest));
            }
        });
    }

    @CheckLogin
    @PostMapping(value = "/youthlearning/delete")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<Void> deleteYouthLearning(YouthLearnRequest request,HttpServletRequest httpServletRequest,MultipartFile file,HttpServletResponse response){
        return OperateTemplate.operate(LOGGER, "????????????????????????????????????", request, new OperateCallBack<Void>() {

            @Override
            public void before() {
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
            }

            @Override
            public Result<Void> execute() throws IOException {
                YouthLearningRequest youthLearningRequest=new YouthLearningRequest();
                youthLearningRequest.setUserId(request.getUserId());
                if(request.getStuId()!=null&&request.getActivityName()!=null){
                    YouthLearningBO youthLearningBO=new YouthLearningBO();
                    youthLearningBO.setActivityName(request.getActivityName());
                    youthLearningBO.setStuId(request.getStuId());
                    youthLearningRequest.setYouthLearningBOList(new ArrayList<>(Collections.singletonList(youthLearningBO)));
                }else if(file!=null){
                    String[][] values = CsvUtil.getWithHeader(file.getInputStream(),StandardCharsets.UTF_8);
                    int stuId=-1,activityName=-1;
                    for (int i = 0; i < values[0].length; i++) {
                        switch (values[0][i]){
                            case "??????":
                                stuId=i;
                                break;
                            case "????????????":
                                activityName=i;
                                break;
                        }
                    }
                    if(stuId==-1||activityName==-1){
                        HttpDownloadUtil.downloadByValue("result.txt","?????????????????????????????????,?????????????????????????????????,????????????",response);
                        return RestResultUtil.buildFailResult();
                    }
                    List<YouthLearningBO> youthLearningBOList=new ArrayList<>();
                    for (int i = 1; i < values.length; i++) {
                        YouthLearningBO youthLearningBO=new YouthLearningBO();
                        youthLearningBO.setStuId(values[i][stuId]);
                        youthLearningBO.setActivityName(values[i][activityName]);
                        youthLearningBOList.add(youthLearningBO);
                    }
                    youthLearningRequest.setYouthLearningBOList(youthLearningBOList);
                }else {
                    HttpDownloadUtil.downloadByValue("result.txt","??????????????????????????? ??? ??????????????????????????????",response);
                    return RestResultUtil.buildFailResult();
                }
                List<YouthLearningBO> fails = youthLearningService.batchDeleteRecord(youthLearningRequest);
                if(fails.size()==0){
                    HttpDownloadUtil.downloadByValue("success.txt","??????????????? :) ",response);
                    return RestResultUtil.buildSuccessResult();
                }else {
                    ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
                    CsvWriter writer=new CsvWriter(outputStream,',',StandardCharsets.UTF_8);
                    writer.writeRecord(new String[]{"??????","????????????"});
                    for (YouthLearningBO fail : fails) {
                        writer.writeRecord(new String[]{fail.getStuId(),fail.getActivityName()});
                    }
                    writer.write("????????????????????????????????????????????????????????????????????????????????????????????????????????????",true);
                    writer.flush();
                    writer.close();
                    ByteArrayInputStream inputStream=new ByteArrayInputStream(outputStream.toByteArray());
                    HttpDownloadUtil.downloadByInputStream("result.csv",inputStream,response);
                    return RestResultUtil.buildSuccessResult();
                }
            }
        });
    }

    @CheckLogin
    @PostMapping(value = "/youthlearning/add")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<String> addYouthLearnRecord(YouthLearnRequest request,HttpServletRequest httpServletRequest){
        return OperateTemplate.operate(LOGGER, "???????????????????????????", request, new OperateCallBack<String>() {
            @Override
            public void before() {
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
                AssertUtil.assertNotNull(request.getActivityName(),"?????????????????????");
                AssertUtil.assertNotNull(request.getStuId(),"??????????????????????????????");
                AssertUtil.assertNotNull(request.getFinishTime(),"?????????????????????");
            }

            @Override
            public Result<String> execute() {
                YouthLearningBO youthLearningBO=new YouthLearningBO();
                youthLearningBO.setStuId(request.getStuId());
                youthLearningBO.setActivityName(request.getActivityName());
                youthLearningBO.setStatus(ActivityRecordStateEnum.ENABLE.getCode());
                youthLearningBO.setTerm(TermUtil.getNowTerm());
                youthLearningBO.setType(ActivityTypeEnum.YOUTH_LEARNING_ACTIVITY.getCode());
                youthLearningBO.setScannerUserId(request.getUserId());
                youthLearningBO.setFinishTime(new Date(request.getFinishTime()));
                YouthLearningRequest youthLearningRequest=new YouthLearningRequest();
                youthLearningRequest.setYouthLearningBO(youthLearningBO);
                youthLearningRequest.setUserId(request.getUserId());
                youthLearningService.saveRecord(youthLearningRequest);
                return RestResultUtil.buildSuccessResult("????????????");
            }
        });
    }

    @CheckLogin
    @PostMapping(value = "/youthlearning/search")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<YouthLearningBO>> youthLearningSearch(YouthLearnRequest request,HttpServletRequest httpServletRequest){
        return OperateTemplate.operate(LOGGER, "???????????????????????????????????????????????????", request, new OperateCallBack<PageList<YouthLearningBO>>() {
            @Override
            public void before() {
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
            }

            @Override
            public Result<PageList<YouthLearningBO>> execute() {
                YouthLearningRequest youthLearningRequest=new YouthLearningRequest();
                youthLearningRequest.setUserId(request.getUserId());
                youthLearningRequest.setStuId(request.getStuId());
                youthLearningRequest.setActivityName(request.getActivityName());
                youthLearningRequest.setPage(request.getPage());
                youthLearningRequest.setSize(request.getSize());
                youthLearningRequest.setClassId(request.getClassId());
                return RestResultUtil.buildSuccessResult(youthLearningService.getByActivityNameAndUserName(youthLearningRequest));
            }
        });
    }
}
