/*
  betahouse.us
  CopyRight (c) 2012 - 2018
 */
package us.betahouse.haetae.controller.activity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import us.betahouse.haetae.activity.dal.service.ActivityRepoService;
import us.betahouse.haetae.activity.enums.ActivityStateEnum;
import us.betahouse.haetae.activity.manager.ActivityManager;
import us.betahouse.haetae.activity.model.basic.ActivityBO;
import us.betahouse.haetae.activity.model.common.PageList;
import us.betahouse.haetae.common.log.LoggerName;
import us.betahouse.haetae.common.session.CheckLogin;
import us.betahouse.haetae.common.template.RestOperateCallBack;
import us.betahouse.haetae.common.template.RestOperateTemplate;
import us.betahouse.haetae.model.activity.PastActivityVO;
import us.betahouse.haetae.model.activity.request.ActivityRestRequest;
import us.betahouse.haetae.model.activity.request.AuditRestRequest;
import us.betahouse.haetae.serviceimpl.activity.constant.ActivityCreatorId;
import us.betahouse.haetae.serviceimpl.activity.constant.ActivityExtInfoKey;
import us.betahouse.haetae.serviceimpl.activity.enums.ActivityOperationEnum;
import us.betahouse.haetae.serviceimpl.activity.model.AuditMessage;
import us.betahouse.haetae.serviceimpl.activity.request.ActivityManagerRequest;
import us.betahouse.haetae.serviceimpl.activity.request.builder.ActivityManagerRequestBuilder;
import us.betahouse.haetae.serviceimpl.activity.service.ActivityService;
import us.betahouse.haetae.serviceimpl.common.OperateContext;
import us.betahouse.haetae.serviceimpl.common.utils.AuditUtil;
import us.betahouse.haetae.serviceimpl.common.utils.TermUtil;
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
import us.betahouse.util.log.Log;
import us.betahouse.util.utils.AssertUtil;
import us.betahouse.util.utils.CollectionUtils;
import us.betahouse.util.utils.DateUtil;
import us.betahouse.util.utils.PageUtil;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 活动接口
 *
 * @author MessiahJK
 * @version : ActivityController.java 2018/11/25 13:16 MessiahJK
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/activity")
public class ActivityController {
    /**
     * 日志实体
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
    /**
     * 添加活动
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    //@CheckLogin
    @PostMapping
    //添加活动时设置modified默认为false
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<ActivityBO> add(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "新增活动", request, new RestOperateCallBack<ActivityBO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
                AssertUtil.assertStringNotBlank(request.getActivityName(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "活动名不能为空");
                AssertUtil.assertStringNotBlank(request.getActivityType(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "活动类型不能为空");
                AssertUtil.assertNotNull(request.getActivityStartTime(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "活动开始时间不能为空");
                AssertUtil.assertNotNull(request.getActivityEndTime(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "活动结束时间不能为空");
                boolean validateTime = new Date(request.getActivityStartTime()).before(new Date(request.getActivityEndTime()));
                AssertUtil.assertTrue(validateTime, "活动开始时间必须早于结束时间");
                AssertUtil.assertNotNull(request.getOrganizationMessage(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "举办单位不能为空");
                AssertUtil.assertNotNull(request.getActivityStampedStart(),RestResultCode.ILLEGAL_PARAMETERS.getCode(),"活动盖章开始时间不能为空");
                AssertUtil.assertNotNull(request.getActivityStampedEnd(),RestResultCode.ILLEGAL_PARAMETERS.getCode(),"活动盖章结束时间不能为空");
                boolean validateStampTime=new Date(request.getActivityStampedStart()).before(new Date(request.getActivityStampedEnd()));
                AssertUtil.assertTrue(validateStampTime,RestResultCode.ILLEGAL_PARAMETERS,"扫章开始时间必须早于结束时间");
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
                        // 以下是可选参数
                        // 描述
                        .withDescription(request.getDescription())
                        // 地点
                        .withLocation(request.getLocation())
                        // 分数
                        .withScore(request.getScore())
                        .build();
                ActivityBO activityBO = activityService.create(activityManagerRequest, context);
                return RestResultUtil.buildSuccessResult(activityBO, "创建活动成功");
            }
        });
    }

    /**
     * 获取所有活动列表
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<ActivityBO>> getActivityList(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "获取活动列表", request, new RestOperateCallBack<PageList<ActivityBO>>() {

            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
            }

            @Override
            public Result<PageList<ActivityBO>> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));

                ActivityManagerRequestBuilder builder = ActivityManagerRequestBuilder.getInstance();

                // 填充状态选择条件
                if (StringUtils.isNotBlank(request.getState())) {
                    builder.withState(request.getState());
                }

                // 添加学期选择条件
                if (StringUtils.isNotBlank(request.getTerm())) {
                    builder.withTerm(request.getTerm());
                }

                // 添加类型选择条件
                if(StringUtils.isNotBlank(request.getActivityType())){
                    builder.withType(request.getActivityType());
                }

                //添加页码
                if(request.getPage()!=null&&request.getPage()!=0){
                    builder.withPage(request.getPage());
                }

                //添加每页条数
                if(request.getLimit()!=null&&request.getLimit()!=0){
                    builder.withLimit(request.getLimit());
                }

                //添加排序規則
                if(StringUtils.isBlank(request.getOrderRule())){
                    builder.withOrderRule(request.getOrderRule());
                }
                return RestResultUtil.buildSuccessResult(activityService.findAll(builder.build(), context), "获取活动列表成功");
            }
        });
    }
    
    /**
     * 获取所有活动举办单位
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/organizers")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<JSONArray> getOrganizers(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "获取所有活动举办单位", request, new RestOperateCallBack<JSONArray>() {
            
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
            }
            
            @Override
            public Result<JSONArray> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                
                List<String> organizers = new ArrayList<>();
                activityRepoService.queryAllActivity().forEach(n -> organizers.add(n.getOrganizationMessage()));
                // 去重
                List<String> out = organizers.stream().distinct().collect(Collectors.toList());
                return RestResultUtil.buildSuccessResult(JSONArray.parseArray(JSON.toJSONString(out)), "获取所有活动举办单位成功");
            }
        });
    }

    /**
     * 操作活动
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @PutMapping
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<ActivityBO> operate(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "操作活动", request, new RestOperateCallBack<ActivityBO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getActivityId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "活动id不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
                AssertUtil.assertStringNotBlank(request.getOperation(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "操作不能为空");
            }

            @Override
            public Result<ActivityBO> execute() {
                // 强校验操作类型
                ActivityOperationEnum operation = ActivityOperationEnum.getByCode(request.getOperation());
                AssertUtil.assertNotNull(operation, "操作类型不存在");
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequest activityManagerRequest = ActivityManagerRequestBuilder.getInstance()
                        .withActivityId(request.getActivityId())
                        .withUserId(request.getUserId())
                        .withOperation(request.getOperation())
                        .build();
                ActivityBO activityBO = activityService.operate(activityManagerRequest, context);
                return RestResultUtil.buildSuccessResult(activityBO, MessageFormat.format("活动{0}成功", operation.getDesc()));
            }
        });
    }
    /**
     * 获取以往活动记录
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping("/past")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PastActivityVO> getPastActivity(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "操作活动", request, new RestOperateCallBack<PastActivityVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
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
     * 分配未分配活动章
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @PostMapping("/past")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PastActivityVO> assignPastActivity(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "操作活动", request, new RestOperateCallBack<PastActivityVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
                AssertUtil.assertNotNull(request.getUndistributedStamp(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "未分配活动章数不能为空");
                AssertUtil.assertNotNull(request.getPastSchoolActivity(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "校园活动章数不能为空");
                AssertUtil.assertNotNull(request.getPastLectureActivity(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "讲座活动章数不能为空");
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
        return RestOperateTemplate.operate(LOGGER, "审核结果发布", request, new RestOperateCallBack<AuditRestRequest>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
                AssertUtil.assertStringNotBlank(request.getAuditId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "订阅用户id不能为空");
                AssertUtil.assertStringNotBlank(request.getResult(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "审核结果不能为空");
                AssertUtil.assertStringNotBlank(request.getDetail(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "审核内容不能为空");
                AssertUtil.assertStringNotBlank(request.getAuditTime(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "审核时间不能为空");
                AssertUtil.assertStringNotBlank(request.getApplicant(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "申请人不能为空");
            }

            @Override
            public Result<AuditRestRequest> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                AuditMessage message = new AuditMessage();
                BeanUtils.copyProperties(request,message);
                String openid =  userService.queryByUserId(request.getAuditId(),context).getOpenId();
                if (StringUtils.isEmpty(openid))
                    return RestResultUtil.buildSuccessResult(request , "该用户不存在");
                String token = AccessTokenManage.GetToken();;
                String result = AuditUtil.publishAuditByOpenId(request.getPage(),openid,token,message);
                if (StringUtils.equals(CommonResultCode.FORBIDDEN.getCode(),result)){
                    return  RestResultUtil.buildSuccessResult(request , "用户未允许订阅该消息");
                }
                return RestResultUtil.buildSuccessResult(request , "订阅信息已发布");
            }

        });
    }

    @CheckLogin
    @GetMapping("/approved")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<ActivityBO>> getApprovedActivity(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "获取已通过的活动列表", request, new RestOperateCallBack<PageList<ActivityBO>>() {

            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
            }

            @Override
            public Result<PageList<ActivityBO>> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));

                //默认第一页，每页十条
                int page=1;
                int limit=10;

                //添加页码
                if(request.getPage()!=null&&request.getPage()>0){
                    page=request.getPage();
                }

                //添加每页条数
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
                return RestResultUtil.buildSuccessResult(activityService.findApprovedActivity(activityManagerRequest,context),"获取通过活动列表成功");
            }
        });
    }

    @CheckLogin
    @Log(loggerName = LoggerName.WEB_DIGEST)
    @PutMapping("/updatestampedtime")
    public Result<ActivityBO> updateStampedTime(ActivityRestRequest request,HttpServletRequest httpServletRequest){
        return RestOperateTemplate.operate(LOGGER, "修改活动扫章时间", request, new RestOperateCallBack<ActivityBO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
                AssertUtil.assertStringNotBlank(request.getActivityStampedStart().toString(),"更新扫章开始时间不能为空");
                AssertUtil.assertStringNotBlank(request.getActivityStampedEnd().toString(),"更新扫章结束时间不能为空");
                boolean validateStampTime=new Date(request.getActivityStampedStart()).before(new Date(request.getActivityStampedEnd()));
                AssertUtil.assertTrue(validateStampTime,RestResultCode.ILLEGAL_PARAMETERS,"盖章开始时间必须早于结束时间");
                AssertUtil.assertStringNotBlank(request.getActivityId(),"活动id不能为空");
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
                return RestResultUtil.buildSuccessResult(activityRepoService.queryActivityByActivityId(request.getActivityId()),"更新扫章时间成功");
            }
        });
    }

    /**
     * 根据活动负责人userId获取活动列表
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/getActivityListByUserID")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<ActivityBO>> getActivityListByUserID(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "根据活动负责人userId获取活动列表", request, new RestOperateCallBack<PageList<ActivityBO>>() {

            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
            }

            @Override
            public Result<PageList<ActivityBO>> execute() {
                //根据返回的status为Finish为不可导章行为
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));

                ActivityManagerRequestBuilder builder = ActivityManagerRequestBuilder.getInstance();

                //添加页码
                if(request.getPage()!=null&&request.getPage()!=0){
                    builder.withPage(request.getPage());
                }

                //添加每页条数
                if(request.getLimit()!=null&&request.getLimit()!=0){
                    builder.withLimit(request.getLimit());
                }

                //添加排序規則
                if(StringUtils.isBlank(request.getOrderRule())){
                    builder.withOrderRule(request.getOrderRule());
                }

                builder.withUserId(request.getUserId());

                return RestResultUtil.buildSuccessResult(activityService.findByUserId(builder.build(), context), "根据活动负责人userId获取活动列表");
            }
        });
    }

    /**
     * 获取已审批通过的活动列表
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/getApprovedActivityList")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<ActivityBO>> getApprovedActivityList(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "获取已审批通过的活动列表", request, new RestOperateCallBack<PageList<ActivityBO>>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
            }
            @Override
            public Result<PageList<ActivityBO>> execute() throws ParseException {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequestBuilder builder = ActivityManagerRequestBuilder.getInstance();
                builder.withState("APPROVED");//approve 表示待审核，sql语句自带cancel
                //添加页码
                if(request.getPage()!=null&&request.getPage()!=0){
                    builder.withPage(request.getPage());
                }
                //添加每页条数
                if(request.getLimit()!=null&&request.getLimit()!=0){
                    builder.withLimit(request.getLimit());
                }
                //添加排序規則
                if(StringUtils.isBlank(request.getOrderRule())){
                    builder.withOrderRule(request.getOrderRule());
                }
                //条件查询（可选）
                if (StringUtils.isNotBlank(request.getUserId())) {//获取到的是StuId。通过StuId找UserId
                    builder.withUserId(request.getSearchCreatorStuId());
                }
                // 添加活动名称选择条件
                if (StringUtils.isNotBlank(request.getActivityName())) {
                    builder.withActivityName(request.getActivityName());
                }
                // 添加举办单位选择条件
                if (StringUtils.isNotBlank(request.getOrganizationMessage())) {
                    builder.withOrganizationMessage(request.getOrganizationMessage());
                }
                // 添加开始时间选择条件
                if (StringUtils.isNotBlank(String.valueOf(request.getActivityStartTime()))) {
                    builder.withStart(request.getActivityStartTime());
                }
                // 添加结束时间选择条件
                if (StringUtils.isNotBlank(String.valueOf(request.getActivityEndTime()))) {
                    builder.withEnd( request.getActivityEndTime());
                }
                return RestResultUtil.buildSuccessResult(activityService.findApproved(builder.build(), context), "获取已审批通过的活动列表");
            }
        });
    }
    /**
     * 获取已审批通过的活动列表 可以按负责人学号、活动名称、举办单位、扫章时间模糊查询
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/approved/by")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<ActivityBO>> getApprovedActivityBy(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "获取已审批通过的活动列表", request, new RestOperateCallBack<PageList<ActivityBO>>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
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
                if (StringUtils.isNotBlank(request.getSearchCreatorStuId())) {//获取到的是StuId。通过StuId找UserId
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
                return RestResultUtil.buildSuccessResult(activityService.findApprovedBy(builder.build(), context), "获取已审批通过的活动列表成功");
            }
        });
    }


    /**
     * 获取未审批通过的活动列表 可以按负责人学号、活动名称、举办单位、扫章时间模糊查询
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/canceled/by")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<ActivityBO>> getCanceledActivityBy(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "获取未审批通过的活动列表", request, new RestOperateCallBack<PageList<ActivityBO>>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
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
                if (StringUtils.isNotBlank(request.getSearchCreatorStuId())) {//获取到的是StuId。通过StuId找UserId
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
                return RestResultUtil.buildSuccessResult(activityService.findCanceledBy(builder.build(), context), "获取未审批通过的活动列表成功");
            }
        });
    }


    /**
     * 获取本周创建的活动列表
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/week/created")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<ActivityBO>> getCreatedActivityListThisWeek(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "获取本周创建的活动列表", request, new RestOperateCallBack<PageList<ActivityBO>>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
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
                //活动名称可选
                if (StringUtils.isNotBlank(request.getActivityName())) {
                    builder.withActivityName(request.getActivityName());
                }
                return RestResultUtil.buildSuccessResult(activityService.findCreatedThisWeek(builder.build(), context), "获取本周创建的活动列表成功");
            }
        });
    }
    /**
     * 获取本周审批通过的活动列表
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/week/approved")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<ActivityBO>> getApprovedActivityListThisWeek(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "获取本周审批通过的活动列表", request, new RestOperateCallBack<PageList<ActivityBO>>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
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
                //活动名称可选
                if (StringUtils.isNotBlank(request.getActivityName())) {
                    builder.withActivityName(request.getActivityName());
                }
                return RestResultUtil.buildSuccessResult(activityService.findApprovedThisWeek(builder.build(), context), "获取本周审批通过的活动列表成功");
            }
        });
    }

    //先查询本周的所有活动id，然后根据id查对应的扫章数，将查询结果添加到对象，ok

    /**
     * 获取本周不合格的活动列表
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/week/unQualified")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<ActivityBO>> getUnQualifiedActivityListThisWeek(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "获取本周不合格的活动列表", request, new RestOperateCallBack<PageList<ActivityBO>>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
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
                //活动名称可选
                if (StringUtils.isNotBlank(request.getActivityName())) {
                    builder.withActivityName(request.getActivityName());
                }
                //测试时将时间限制删除
                return RestResultUtil.buildSuccessResult(activityService.findUnQualifiedThisWeek(builder.build(), context), "获取本周不合格的活动列表成功");
            }
        });
    }


    /**
     * 驳回审批
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @PutMapping(value = "/cancel")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<ActivityBO> cancel(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "驳回申请", request, new RestOperateCallBack<ActivityBO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getActivityId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "活动id不能为空");
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
                return RestResultUtil.buildSuccessResult(activityBO, "申请驳回成功");
            }
        });
    }
    /**
     * 审批通过
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @PutMapping(value = "/publish")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<ActivityBO> publish(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "审批通过", request, new RestOperateCallBack<ActivityBO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getActivityId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "活动id不能为空");
            }

            @Override
            public Result<ActivityBO> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequest activityManagerRequest = ActivityManagerRequestBuilder.getInstance()
                        .withActivityId(request.getActivityId())
                        .build();
                ActivityBO activityBO = activityService.publish(activityManagerRequest, context);
                return RestResultUtil.buildSuccessResult(activityBO, "审批通过成功");
            }
        });
    }

    /**
     * 根据活动id查询活动
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/ByActivityId")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<ActivityBO> getActivityByActivityID(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "根据活动id查询活动", request, new RestOperateCallBack<ActivityBO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getActivityId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "活动id不能为空");
            }
            @Override
            public Result<ActivityBO> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequestBuilder builder = ActivityManagerRequestBuilder.getInstance();
                builder.withActivityId(request.getActivityId());
                return RestResultUtil.buildSuccessResult(activityService.findByActivityId(builder.build(), context), "根据活动id查询活动");
            }
        });
    }
    /**
     * 修改活动申请 只能修改一次
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @PutMapping(value = "/modify")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<ActivityBO> modify(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "修改活动申请", request, new RestOperateCallBack<ActivityBO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
                AssertUtil.assertStringNotBlank(request.getActivityName(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "活动名不能为空");
                AssertUtil.assertStringNotBlank(request.getActivityType(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "活动类型不能为空");
                AssertUtil.assertNotNull(request.getActivityStartTime(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "活动开始时间不能为空");
                AssertUtil.assertNotNull(request.getActivityEndTime(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "活动结束时间不能为空");
                boolean validateTime = new Date(request.getActivityStartTime()).before(new Date(request.getActivityEndTime()));
                //false未修改才能继续
                AssertUtil.assertTrue(!request.getModified(),"申请只能修改一次");
                AssertUtil.assertTrue(validateTime, "活动开始时间必须早于结束时间");
                AssertUtil.assertNotNull(request.getOrganizationMessage(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "举办单位不能为空");
                AssertUtil.assertNotNull(request.getActivityStampedStart(),RestResultCode.ILLEGAL_PARAMETERS.getCode(),"活动盖章开始时间不能为空");
                AssertUtil.assertNotNull(request.getActivityStampedEnd(),RestResultCode.ILLEGAL_PARAMETERS.getCode(),"活动盖章结束时间不能为空");
                boolean validateStampTime=new Date(request.getActivityStampedStart()).before(new Date(request.getActivityStampedEnd()));
                AssertUtil.assertTrue(validateStampTime,RestResultCode.ILLEGAL_PARAMETERS,"扫章开始时间必须早于结束时间");
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
                return RestResultUtil.buildSuccessResult(activityBO, "修改活动申请成功");
            }
        });
    }


    /**
     * 根据活动负责人Id获取已审批通过的活动列表
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/approved/byUserID")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<ActivityBO>> getApprovedListByUserID(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "根据活动负责人Id获取审批通过的活动列表", request, new RestOperateCallBack<PageList<ActivityBO>>() {

            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
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

                return RestResultUtil.buildSuccessResult(activityService.findApprovedByUserId(builder.build(), context), "根据活动负责人Id获取审批通过的活动列表成功");
            }
        });
    }

    /**
     * 根据活动负责人Id获取未审批通过的活动列表
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/canceled/byUserID")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<ActivityBO>> getCanceledListByUserID(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "根据活动负责人Id获取未审批通过的活动列表", request, new RestOperateCallBack<PageList<ActivityBO>>() {

            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
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

                return RestResultUtil.buildSuccessResult(activityService.findCanceledByUserId(builder.build(), context), "根据活动负责人Id获取未审批通过的活动列表成功");
            }
        });
    }

}
