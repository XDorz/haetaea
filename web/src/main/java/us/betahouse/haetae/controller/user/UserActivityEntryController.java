package us.betahouse.haetae.controller.user;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import us.betahouse.haetae.activity.model.basic.ActivityEntryRecordBO;
import us.betahouse.haetae.activity.request.ActivityEntryRecordRequest;
import us.betahouse.haetae.activity.request.ActivityEntryRequest;
import us.betahouse.haetae.common.log.LoggerName;
import us.betahouse.haetae.common.session.CheckLogin;
import us.betahouse.haetae.common.template.RestOperateCallBack;
import us.betahouse.haetae.common.template.RestOperateTemplate;
import us.betahouse.haetae.controller.activity.ActivityController;
import us.betahouse.haetae.model.activity.request.ActivityEntryRestRequest;
import us.betahouse.haetae.model.activity.request.ActivitySubscribeRestRequest;
import us.betahouse.haetae.serviceimpl.activity.builder.ActivityEntryRecordRequestBuilder;
import us.betahouse.haetae.serviceimpl.activity.builder.ActivityEntryRequestBuilder;
import us.betahouse.haetae.serviceimpl.activity.model.ActivityEntryList;
import us.betahouse.haetae.serviceimpl.activity.model.ActivityEntryPublish;
import us.betahouse.haetae.serviceimpl.activity.service.ActivityBlacklistService;
import us.betahouse.haetae.serviceimpl.activity.service.ActivityEntryService;
import us.betahouse.haetae.serviceimpl.activity.service.ActivityRecordService;
import us.betahouse.haetae.serviceimpl.common.OperateContext;
import us.betahouse.haetae.serviceimpl.common.utils.SubscribeUtil;
import us.betahouse.haetae.serviceimpl.common.utils.TermUtil;
import us.betahouse.haetae.serviceimpl.schedule.ScheduleTaskMap;
import us.betahouse.haetae.serviceimpl.schedule.dto.RealTask;
import us.betahouse.haetae.serviceimpl.schedule.manager.AccessTokenManage;
import us.betahouse.haetae.serviceimpl.user.service.UserService;
import us.betahouse.haetae.user.model.basic.perm.UserBO;
import us.betahouse.haetae.utils.IPUtil;
import us.betahouse.haetae.utils.RestResultUtil;
import us.betahouse.util.common.Result;
import us.betahouse.util.enums.CommonResultCode;
import us.betahouse.util.enums.RestResultCode;
import us.betahouse.util.log.Log;
import us.betahouse.util.utils.AssertUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * ???????????????????????????????????????
 *
 * @author zjb
 * @version : UserActivityEntryController.java 2019/7/8 23:18 zjb
 */
@RestController
@RequestMapping(value = "/user")
@CrossOrigin
public class UserActivityEntryController {
    /**
     * ????????????
     */
    private final Logger LOGGER = LoggerFactory.getLogger(ActivityController.class);

    @Autowired
    private ActivityEntryService activityEntryService;

    @Autowired
    private ActivityBlacklistService activityBlacklistService;

    @Autowired
    private UserService userService;


    @CheckLogin
    @GetMapping("/registeredActivityEntry")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<ActivityEntryList> queryUserRegisteredActivityEntry(ActivityEntryRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "??????????????????????????????", request, new RestOperateCallBack<ActivityEntryList>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
                AssertUtil.assertNotNull(request.getPage(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "page??????????????????");
                AssertUtil.assertNotNull(request.getLimit(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "limit??????????????????");
            }

            @Override
            public Result<ActivityEntryList> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityEntryRequest activityEntryRequest = ActivityEntryRequestBuilder.anActivityEntryRequest()
                        .withActivityId(request.getActivityId())
                        .withState(request.getState())
                        .withTerm(request.getTerm())
                        .withType(request.getActivityType())
                        .withPage(request.getPage())
                        .withLimit(request.getLimit())
                        .build();
                return RestResultUtil.buildSuccessResult(activityEntryService.registeredActivityEntryQuery(activityEntryRequest, request.getUserId()), "????????????????????????????????????");
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
    @PostMapping("/signUp")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<ActivityEntryRecordBO> signUp(ActivityEntryRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "????????????", request, new RestOperateCallBack<ActivityEntryRecordBO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
                AssertUtil.assertStringNotBlank(request.getActivityEntryId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "????????????id????????????");
            }

            @Override
            public Result<ActivityEntryRecordBO> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                if( 0 >= activityBlacklistService.getCreditScoreByUserIdAndTerm(request.getUserId(),TermUtil.getNowTerm())) {
                    return RestResultUtil.buildSuccessResult(null, "?????????????????????????????????");
                }
                ActivityEntryRecordRequest activityEntryRecordRequest = ActivityEntryRecordRequestBuilder.anActivityEntryRecordRequest()
                        .withActivityEntryId(request.getActivityEntryId())
                        .withActivityEntryRecordId(request.getActivityEntryRecordId())
                        .withUserId(request.getUserId())
                        .withNote(request.getRecordNote())
                        .withChoose(request.getRecordChoose())
                        .build();
                ActivityEntryRecordBO activityEntryRecordBO = activityEntryService.createActivityEntryRecord(activityEntryRecordRequest);
                if(activityEntryRecordBO == null){
                    return RestResultUtil.buildSuccessResult(null, "?????????");
                }else{
                    return RestResultUtil.buildSuccessResult(activityEntryRecordBO, "????????????");
                }
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
    @DeleteMapping("/undoSignUp")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<ActivityEntryRecordBO> undoSignUp(ActivityEntryRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "??????????????????", request, new RestOperateCallBack<ActivityEntryRecordBO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
                AssertUtil.assertStringNotBlank(request.getActivityEntryId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "????????????id????????????");
            }

            @Override
            public Result<ActivityEntryRecordBO> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityEntryRecordRequest activityEntryRecordRequest = ActivityEntryRecordRequestBuilder.anActivityEntryRecordRequest()
                        .withActivityEntryId(request.getActivityEntryId())
                        .withActivityEntryRecordId(request.getActivityEntryRecordId())
                        .withUserId(request.getUserId())
                        .withNote(request.getRecordNote())
                        .withChoose(request.getRecordChoose())
                        .build();
                activityEntryService.undoSignUp(activityEntryRecordRequest);
                return RestResultUtil.buildSuccessResult(null, "??????????????????");
            }
        });
    }

    /**
     * ??????????????????
     * @param request
     * @param httpServletRequest
     * @return
     */
       @CheckLogin
       @PostMapping("/Subscribe")
       @Log(loggerName = LoggerName.WEB_DIGEST)
      public Result<ActivityEntryPublish> ActivitySubscribe(ActivitySubscribeRestRequest request , HttpServletRequest httpServletRequest) {
           return RestOperateTemplate.operate(LOGGER, "????????????????????????", request, new RestOperateCallBack<ActivityEntryPublish>() {
               @Override
               public void before() {
                   AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                   AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
                   AssertUtil.assertStringNotBlank(request.getSubscribeId(),RestResultCode.ILLEGAL_PARAMETERS.getCode(), "????????????id????????????");
                   AssertUtil.assertStringNotBlank(request.getActivityName(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                   AssertUtil.assertStringNotBlank(request.getStart(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????????????????");
                   AssertUtil.assertStringNotBlank(request.getActivityTime(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "????????????????????????");
                   AssertUtil.assertStringNotBlank(request.getLocation(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "????????????????????????");
               }

               @Override
               public Result<ActivityEntryPublish> execute() {
                   OperateContext context = new OperateContext();
                   context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                   if (request.getAdvanceTime()==null || !StringUtils.isNumeric(request.getAdvanceTime())){
                       request.setAdvanceTime("0");
                   }
                    UserBO userBO =  userService.queryByUserId(request.getUserId(),context);
                   if (userBO==null){
                       return RestResultUtil.buildSuccessResult("??????id??????");
                   }
                   String openid = userBO.getOpenId();
                   ActivityEntryPublish activityEntryPublish = new ActivityEntryPublish();
                   BeanUtils.copyProperties(request,activityEntryPublish);
                   ScheduleTaskMap.getInstance().putMap(Integer.parseInt(request.getAdvanceTime()),request.getPage(),activityEntryPublish,openid);
                   return RestResultUtil.buildSuccessResult(activityEntryPublish, "????????????????????????");
               }
           });
       }

    /**
     * ?????????????????????
     */
    @CheckLogin
    @GetMapping("/Subscribe")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<ActivityEntryPublish> ActivitySubscribeFind(ActivitySubscribeRestRequest request , HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "??????????????????", request, new RestOperateCallBack<ActivityEntryPublish>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
                AssertUtil.assertStringNotBlank(request.getSubscribeId(),RestResultCode.ILLEGAL_PARAMETERS.getCode(), "????????????id????????????");
            }

            @Override
            public Result<ActivityEntryPublish> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                return RestResultUtil.buildSuccessResult(ScheduleTaskMap.getInstance().ifExist(request.getSubscribeId()) , "????????????");
            }
        });
    }
    /**
     * ????????????
     */
    @CheckLogin
    @DeleteMapping("/Subscribe")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<ActivityEntryPublish> ActivitySubscribeDel(ActivitySubscribeRestRequest request , HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "??????????????????", request, new RestOperateCallBack<ActivityEntryPublish>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
                AssertUtil.assertStringNotBlank(request.getSubscribeId(),RestResultCode.ILLEGAL_PARAMETERS.getCode(), "????????????id????????????");
            }

            @Override
            public Result<ActivityEntryPublish> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                return RestResultUtil.buildSuccessResult(ScheduleTaskMap.getInstance().delMap(request.getSubscribeId()) , "??????????????????");
            }
        });
    }

    /**
     *
     * ??????????????????
     * @param request
     * @param httpServletRequest
     * @return
     */
       @CheckLogin
       @GetMapping("/getActivityEntryRecord")
       @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<ActivityEntryRecordBO> getRecord(ActivityEntryRestRequest request, HttpServletRequest httpServletRequest) {
           return RestOperateTemplate.operate(LOGGER, "??????????????????", request, new RestOperateCallBack<ActivityEntryRecordBO>() {
               @Override
               public void before() {
                   AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                   AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
                   AssertUtil.assertStringNotBlank(request.getActivityEntryId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "????????????id????????????");
               }

               @Override
               public Result<ActivityEntryRecordBO> execute() {
                   OperateContext context = new OperateContext();
                   context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                   ActivityEntryRecordRequest activityEntryRecordRequest = ActivityEntryRecordRequestBuilder.anActivityEntryRecordRequest()
                           .withActivityEntryId(request.getActivityEntryId())
                           .withUserId(request.getUserId())
                           .build();
                   ActivityEntryRecordBO result =  activityEntryService.findByActivityEntryIdAndUserId(activityEntryRecordRequest);
                   return RestResultUtil.buildSuccessResult(result, "????????????");
               }
           });
       }



}
