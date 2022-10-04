/*
  betahouse.us
  CopyRight (c) 2012 - 2018
 */
package us.betahouse.haetae.controller.activity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.csvreader.CsvWriter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import us.betahouse.haetae.activity.dal.service.ActivityRecordRepoService;
import us.betahouse.haetae.activity.dal.service.ActivityRepoService;
import us.betahouse.haetae.activity.enums.ActivityRecordStateEnum;
import us.betahouse.haetae.activity.enums.ActivityTypeEnum;
import us.betahouse.haetae.activity.manager.ActivityManager;
import us.betahouse.haetae.activity.model.basic.ActivityBO;
import us.betahouse.haetae.activity.model.basic.ActivityNowLocationBO;
import us.betahouse.haetae.activity.model.basic.ActivitySignBO;
import us.betahouse.haetae.activity.model.basic.YouthLearnBatchBO;
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
import us.betahouse.haetae.serviceimpl.schedule.manager.AccessTokenManage;
import us.betahouse.haetae.serviceimpl.user.service.PermService;
import us.betahouse.haetae.serviceimpl.user.service.UserService;
import us.betahouse.haetae.user.dal.service.PermRepoService;
import us.betahouse.haetae.utils.IPUtil;
import us.betahouse.haetae.utils.RestResultUtil;
import us.betahouse.util.common.Result;
import us.betahouse.util.enums.CommonResultCode;
import us.betahouse.util.enums.RestResultCode;
import us.betahouse.util.log.Log;
import us.betahouse.util.template.OperateCallBack;
import us.betahouse.util.template.OperateTemplate;
import us.betahouse.util.utils.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
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

    @Autowired
    private YouthLearningService youthLearningService;

    @Autowired
    private OrganizationRepo organizationRepo;

    @Autowired
    private ActivityRecordRepoService activityRecordRepoService;

    /**
     * 添加活动
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
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
     * 获取所有活动举办单位
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/organizers/all")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<JSONArray> getOrganizer(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "获取所有活动举办单位", request, new RestOperateCallBack<JSONArray>() {

            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
            }

            @Override
            public Result<JSONArray> execute() {
                List<OrganizationDO> all = organizationRepo.findAll();
                List<String> list=new ArrayList<>();
                for (int i = 0; i < all.size(); i++) {
                    list.add(all.get(i).getOrganizationName());
                }
                return RestResultUtil.buildSuccessResult(JSONArray.parseArray(JSON.toJSONString(list)),"获取所有活动举办单位成功");
            }
        });
    }

    /**
     * (新)获取所有活动举办单位
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/neworganizers")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<List<String>> newGetOrganizers(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "(新)获取所有活动举办单位", request, new RestOperateCallBack<List<String>>() {

            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
            }

            @Override
            public Result<List<String>> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                return RestResultUtil.buildSuccessResult(activityRepoService.queryAllOrganization(), "(新)获取所有活动举办单位成功");
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
     * 获取以往活动
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping("/past")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PastActivityVO> getPastActivity(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "获取以往活动", request, new RestOperateCallBack<PastActivityVO>() {
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
                String token = AccessTokenManage.GetToken();
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
                int page=0;
                int limit=10;

                //添加页码
                if(request.getPage()!=null&&request.getPage()>0){
                    page=request.getPage()-1;
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
//                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
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
                return RestResultUtil.buildSuccessResult(activityService.findApproved(activityManagerRequest,context),"获取已审批通过的活动列表");
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
    public Result<List<ActivityBO>> getUnQualifiedActivityListThisWeek(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "获取本周不合格的活动列表", request, new RestOperateCallBack<List<ActivityBO>>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
            }
            @Override
            public Result<List<ActivityBO>> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequestBuilder builder = ActivityManagerRequestBuilder.getInstance();
                //活动名称可选
                if (StringUtils.isNotBlank(request.getActivityName())) {
                    builder.withActivityName(request.getActivityName());
                }
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
                AssertUtil.assertNotNull(request.getActivityId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "活动id不能为空");
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

    @CheckLogin
    @PostMapping(value = "/youthlearning/batchsave")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<Void> batchSave(YouthLearnRequest request, @PathVariable("file") MultipartFile file, HttpServletRequest httpServletRequest, HttpServletResponse response){
        return RestOperateTemplate.operate(LOGGER, "批量记录青年大学习情况", httpServletRequest, new RestOperateCallBack<Void>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(file,RestResultCode.ILLEGAL_PARAMETERS.getCode(),"请上传相应csv文件");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
            }

            @Override
            public Result<Void> execute(){
                try {
                    String[][] values = CsvUtil.getWithHeader(file.getInputStream(),StandardCharsets.UTF_8);
                    int stuClass=-1,stuId=-1,finishTime=-1,activityName=-1;
                    for (int i = 0; i < values[0].length; i++) {
                        switch (values[0][i]){
                            case "单位/班级/社区（村）":
                                stuClass=i;
                                break;
                            case "学号/卡号/工号":
                                stuId=i;
                                break;
                            case "学习时间":
                                finishTime=i;
                                break;
                            case "课程":
                                activityName=i;
                                break;
                        }
                    }
                    if(stuId==-1||finishTime==-1||activityName==-1||stuClass==-1){
                        HttpDownloadUtil.downloadByValue("result.txt","您上传的表格数据不完整,正确格式包含以下：学号/卡号/工号,单位/班级/社区（村）,学习时间,课程",response);
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
                        youthLearningBO.setStatus(ActivityRecordStateEnum.ENABLE.getCode());
                        youthLearningBO.setActivityName(values[i][activityName]);
                        youthLearningBO.setFinishTime(simpleDateFormat.parse(values[i][finishTime]));
                        youthLearningBO.setTerm(TermUtil.getTerm(youthLearningBO.getFinishTime()));
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
                        HttpDownloadUtil.downloadByValue("result.txt","您上传的表格没有任何数据",response);
                        return RestResultUtil.buildFailResult();
                    }
                    if(info.getRepeat().size()==0&&info.getInfo().length()==0){
                        HttpDownloadUtil.downloadByValue("result.txt","导入成功 :) \n"+info.getInfo(),response);
                        return RestResultUtil.buildSuccessResult();
                    }
                    ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
                    CsvWriter csvWriter=new CsvWriter(outputStream,',', StandardCharsets.UTF_8);
                    csvWriter.writeRecord(new String[]{"学号/卡号/工号","单位/班级/社区（村）", "课程","学习时间"});
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

    //该功能以由其他接口替代
    @Deprecated
    @CheckLogin
    @GetMapping(value = "/youthlearning/byuserid")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<List<YouthLearningBO>> youthLearningByUserId(YouthLearnRequest request,HttpServletRequest httpServletRequest){
        return OperateTemplate.operate(LOGGER, "获取该用户参加的所有青年大学习活动", request, new OperateCallBack<List<YouthLearningBO>>() {

            @Override
            public void before() {
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
            }

            @Override
            public Result<List<YouthLearningBO>> execute() {
                YouthLearningRequest youthLearningRequest=new YouthLearningRequest();
                youthLearningRequest.setUserId(request.getUserId());
                return RestResultUtil.buildSuccessResult(youthLearningService.getRecordByUserId(youthLearningRequest));
            }
        });
    }

    //该功能以由其他接口替代
    @Deprecated
    @CheckLogin
    @GetMapping(value = "/youthlearning/byuseridnum")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<Integer> youthLearningByUserIdNum(YouthLearnRequest request,HttpServletRequest httpServletRequest){
        return OperateTemplate.operate(LOGGER, "获取该用户参加的所有青年大学习活动的数量", request, new OperateCallBack<Integer>() {

            @Override
            public void before() {
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
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
        return OperateTemplate.operate(LOGGER, "盖章员删除某人的学习记录", request, new OperateCallBack<Void>() {

            @Override
            public void before() {
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
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
                            case "学号":
                                stuId=i;
                                break;
                            case "活动名称":
                                activityName=i;
                                break;
                        }
                    }
                    if(stuId==-1||activityName==-1){
                        HttpDownloadUtil.downloadByValue("result.txt","您上传的表格数据不完整,正确格式包含以下：学号,活动名称",response);
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
                    HttpDownloadUtil.downloadByValue("result.txt","请在活动名称，学号 和 表格之间至少上传一个",response);
                    return RestResultUtil.buildFailResult();
                }
                List<YouthLearningBO> fails = youthLearningService.batchDeleteRecord(youthLearningRequest);
                if(fails.size()==0){
                    HttpDownloadUtil.downloadByValue("success.txt","删除章成功 :) ",response);
                    return RestResultUtil.buildSuccessResult();
                }else {
                    ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
                    CsvWriter writer=new CsvWriter(outputStream,',',StandardCharsets.UTF_8);
                    writer.writeRecord(new String[]{"学号","活动名称"});
                    for (YouthLearningBO fail : fails) {
                        writer.writeRecord(new String[]{fail.getStuId(),fail.getActivityName()});
                    }
                    writer.write("以上为查找不到大学习记录或者已经被删除该记录的名单，重新导入请将此行删除",true);
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
        return OperateTemplate.operate(LOGGER, "管理员单独添加记录", request, new OperateCallBack<String>() {
            @Override
            public void before() {
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
                AssertUtil.assertNotNull(request.getActivityName(),"请填入活动名称");
                AssertUtil.assertNotNull(request.getStuId(),"请填入被加入学生学号");
                AssertUtil.assertNotNull(request.getFinishTime(),"请填入完成时间");
            }

            @Override
            public Result<String> execute() {
                YouthLearningBO youthLearningBO=new YouthLearningBO();
                youthLearningBO.setStuId(request.getStuId());
                youthLearningBO.setActivityName(request.getActivityName());
                youthLearningBO.setStatus(ActivityRecordStateEnum.ENABLE.getCode());
                youthLearningBO.setType(ActivityTypeEnum.YOUTH_LEARNING_ACTIVITY.getCode());
                youthLearningBO.setScannerUserId(request.getUserId());
                youthLearningBO.setFinishTime(new Date(request.getFinishTime()));
                youthLearningBO.setTerm(TermUtil.getTerm(youthLearningBO.getFinishTime()));
                YouthLearningRequest youthLearningRequest=new YouthLearningRequest();
                youthLearningRequest.setYouthLearningBO(youthLearningBO);
                youthLearningRequest.setUserId(request.getUserId());
                youthLearningService.saveRecord(youthLearningRequest);
                return RestResultUtil.buildSuccessResult("加入成功");
            }
        });
    }

    @CheckLogin
    @PostMapping(value = "/youthlearning/search")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<YouthLearningBO>> youthLearningSearch(YouthLearnRequest request,HttpServletRequest httpServletRequest){
        return OperateTemplate.operate(LOGGER, "盖章员查询某期大学习和某人学习情况", request, new OperateCallBack<PageList<YouthLearningBO>>() {
            @Override
            public void before() {
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
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


    @CheckLogin
    @GetMapping(value = "/youthlearning/selflearn")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<List<YouthLearnBatchBO>> getSelfYouthLearnRecord(YouthLearnRequest request, HttpServletRequest httpServletRequest){
        return OperateTemplate.operate(LOGGER, "个人获取大学习记录情况", request, new OperateCallBack<List<YouthLearnBatchBO>>() {
            @Override
            public void before() {
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
            }

            @Override
            public Result<List<YouthLearnBatchBO>> execute() {
                YouthLearningRequest youthLearningRequest=new YouthLearningRequest();
                youthLearningRequest.setUserId(request.getUserId());
                return RestResultUtil.buildSuccessResult(youthLearningService.getTermedRecordByUserId(youthLearningRequest));
            }
        });
    }

    /**
     * 根据单位信息查询过去一个月内所有发起了报名的活动的实际参与的人数
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/queryActualNumPastMonthByOrganizationMessage")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<List<ActivitySignBO>> querySignNumPastMonthByOrganizationMessage(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return OperateTemplate.operate(LOGGER, "获取过去一个月内所有发起了报名的活动的报名总人数与实际参与的人数，按发起的社团分类，一个月以活动开始时间为准", request, new OperateCallBack<List<ActivitySignBO>>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
            }
            @Override
            public Result<List<ActivitySignBO>> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequestBuilder builder = ActivityManagerRequestBuilder.getInstance();
                List<ActivitySignBO> activitySignBOS = new ArrayList<>();
                List<OrganizationDO> all = organizationRepo.findAll();
                for (int i = 0; i < all.size(); i++) {
                    ActivitySignBO activitySignBO = new ActivitySignBO();
                    activitySignBO.setOrganizationId(all.get(i).getOrganizationId());
                    activitySignBO.setOrganizationMessage(all.get(i).getOrganizationName());
                    builder.withOrganizationMessage(all.get(i).getOrganizationName());
                    activitySignBO.setSignNumPastMonth(activityService.querySignNumPastMonthByOrganizationMessage(builder.build(), context));
                    activitySignBO.setActualNumPastMonth(activityService.queryActualNumPastMonthByOrganizationMessage(builder.build(), context));
                    activitySignBOS.add(activitySignBO);
                }

                return RestResultUtil.buildSuccessResult(activitySignBOS, "获取过去一个月内所有发起了报名的活动的报名总人数与实际参与的人数，按发起的社团分类，一个月以活动开始时间为准");
            }
        });
    }

    /**
     * 查询当前学期校园活动数量和讲座活动数量和总活动数量
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/findSchoolAndLectureActivityNumAndAllNum")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<List<Integer>> findSchoolAndLectureActivityNumAndAllNum(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return OperateTemplate.operate(LOGGER, "查询当前学期校园活动数量和讲座活动数量和总活动数量", request, new OperateCallBack<List<Integer>>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
            }
            @Override
            public Result<List<Integer>> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequestBuilder builder = ActivityManagerRequestBuilder.getInstance();
                List<Integer> integers = new ArrayList<>();
                String term = TermUtil.getNowTerm();
                builder.withTerm(term);
                integers.add(activityService.findSchoolActivityNum(builder.build(), context));
                integers.add(activityService.findLectureActivityNum(builder.build(), context));
                integers.add(activityService.findAllActivityNum(builder.build(), context));
                return RestResultUtil.buildSuccessResult(integers, "查询当前学期校园活动数量和讲座活动数量和总活动数量");
            }
        });
    }

    /**
     * 查询活动名称，活动时间和活动地点
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @GetMapping(value = "/findActivityTimeAndPosition")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<List<ActivityNowLocationBO>> findActivityTimeAndPosition(ActivityRestRequest request, HttpServletRequest httpServletRequest) {
        return OperateTemplate.operate(LOGGER, "查询活动名称，活动时间和活动地点", request, new OperateCallBack<List<ActivityNowLocationBO>>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
            }
            @Override
            public Result<List<ActivityNowLocationBO>> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityManagerRequestBuilder builder = ActivityManagerRequestBuilder.getInstance();
                List<ActivityNowLocationBO> activityNowLocationBOS = new ArrayList<>();
                int size = activityService.findActivityName(builder.build(), context).size();
                for(int i = 0; i < size; i ++ ){
                    ActivityNowLocationBO activityNowLocationBO = new ActivityNowLocationBO();
                    activityNowLocationBO.setActivity_name(activityService.findActivityName(builder.build(), context).get(i));
                    activityNowLocationBO.setStart(activityService.findActivityTime(builder.build(), context).get(i));
                    activityNowLocationBO.setLocation(activityService.findActivityLocation(builder.build(), context).get(i));
                    activityNowLocationBOS.add(activityNowLocationBO);
                }
                return RestResultUtil.buildSuccessResult(activityNowLocationBOS, "查询活动名称，活动时间和活动地点");
            }
        });
    }
}
