package us.betahouse.haetae.controller.activity;

import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import us.betahouse.haetae.activity.dal.service.ActivityEntryRepoService;
import us.betahouse.haetae.activity.dal.service.ActivityRepoService;
import us.betahouse.haetae.activity.model.basic.ActivityBO;
import us.betahouse.haetae.activity.model.basic.ActivityEntryBO;
import us.betahouse.haetae.activity.request.ActivityEntryRequest;
import us.betahouse.haetae.common.log.LoggerName;
import us.betahouse.haetae.common.session.CheckLogin;
import us.betahouse.haetae.common.template.RestOperateCallBack;
import us.betahouse.haetae.common.template.RestOperateTemplate;
import us.betahouse.haetae.model.activity.request.ActivityEntryRestRequest;
import us.betahouse.haetae.model.activity.request.ActivityRestRequest;
import us.betahouse.haetae.serviceimpl.activity.builder.ActivityEntryRequestBuilder;
import us.betahouse.haetae.serviceimpl.activity.model.ActivityEntry;
import us.betahouse.haetae.serviceimpl.activity.model.ActivityEntryList;
import us.betahouse.haetae.serviceimpl.activity.service.ActivityEntryService;
import us.betahouse.haetae.serviceimpl.common.OperateContext;
import us.betahouse.haetae.user.model.basic.UserInfoBO;
import us.betahouse.haetae.utils.IPUtil;
import us.betahouse.haetae.utils.RestResultUtil;
import us.betahouse.util.common.Result;
import us.betahouse.util.enums.RestResultCode;
import us.betahouse.util.log.Log;
import us.betahouse.util.utils.AssertUtil;
import us.betahouse.util.utils.DateUtil;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

/**
 * ????????????????????????
 *
 * @author zjb
 * @version : ActivityEntryController.java 2019/7/8 1:27 zjb
 */
@RestController
@RequestMapping(value = "/activityEntry")
public class ActivityEntryController {
    /**
     * ????????????
     */
    private final Logger LOGGER = LoggerFactory.getLogger(ActivityController.class);

    @Autowired
    private ActivityEntryService activityEntryService;
    
    @Autowired
    private ActivityEntryRepoService activityEntryRepoService;
    
    @Autowired
    private ActivityRepoService activityRepoService;

    @CheckLogin
    @GetMapping
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<ActivityEntryList> queryActivityEntry(ActivityEntryRestRequest request, HttpServletRequest httpServletRequest) {
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
                if (request.getActivityId()!="" && request.getActivityId()!= null)
                    return RestResultUtil.buildSuccessResult(activityEntryService.activityEntryQueryByActivityId(request.getActivityId()), "??????????????????????????????");;
                ActivityEntryRequest activityEntryRequest = ActivityEntryRequestBuilder.anActivityEntryRequest()
                        .withState(request.getState())
                        .withTerm(request.getTerm())
                        .withType(request.getActivityType())
                        .withPage(request.getPage())
                        .withLimit(request.getLimit())
                        .build();
                    return RestResultUtil.buildSuccessResult(activityEntryService.activityEntryQuery(activityEntryRequest, request.getUserId()), "??????????????????????????????");
            }
        });
    }

    @CheckLogin
    @GetMapping("/singleActivityEntry")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<ActivityEntry> singleActivityEntry(ActivityEntryRestRequest activityEntryRestRequest, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "????????????????????????", activityEntryRestRequest, new RestOperateCallBack<ActivityEntry>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(activityEntryRestRequest, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertNotNull(activityEntryRestRequest.getActivityEntryId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????id??????");
                AssertUtil.assertStringNotBlank(activityEntryRestRequest.getActivityEntryId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "????????????id????????????");
            }
    
            @Override
            public Result<ActivityEntry> execute() throws IOException {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityEntryBO activityEntryBO = activityEntryRepoService.findByActivityEntryId(activityEntryRestRequest.getActivityEntryId());
                ActivityBO activityBO = activityRepoService.queryActivityByActivityId(activityEntryBO.getActivityId());
                ActivityEntry activityEntry = new ActivityEntry();
                activityEntry.setActivityId(activityEntryBO.getActivityId());
                activityEntry.setActivityEntryId(activityEntryBO.getActivityEntryId());
                activityEntry.setTitle(activityEntryBO.getTitle());
                activityEntry.setActivityEntryStart(DateUtil.format(activityEntryBO.getStart(), "yyyy???MM???dd??? HH:mm:ss"));
                activityEntry.setActivityEntryEnd(DateUtil.format(activityEntryBO.getEnd(), "yyyy???MM???dd??? HH:mm:ss"));
                activityEntry.setActivityName(activityBO.getActivityName());
                activityEntry.setActivityType(activityEntryBO.getType());
                activityEntry.setDescription(activityEntryBO.getNote());
                activityEntry.setLocation(activityBO.getLocation());
                activityEntry.setSecond((activityEntryBO.getStart().getTime() - System.currentTimeMillis())/1000);
                activityEntry.setNumber(activityEntryBO.getNumber());
                activityEntry.setLinkman(activityEntryBO.getLinkman());
                activityEntry.setContact(activityEntryBO.getContact());
                activityEntry.setChoose(activityEntryBO.getChoose());
                activityEntry.setTop(activityEntryBO.getTop());
                activityEntry.setStart(DateUtil.format(activityBO.getStart(), "yyyy???MM???dd??? HH:mm:ss"));
                activityEntry.setEnd(DateUtil.format(activityBO.getEnd(), "yyyy???MM???dd??? HH:mm:ss"));
                activityEntry.setStatus(activityEntryBO.getState());
                return RestResultUtil.buildSuccessResult(activityEntry, "??????????????????????????????");
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
    @PostMapping
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<ActivityEntryBO> creatActivityEntry(ActivityEntryRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "??????????????????????????????", request, new RestOperateCallBack<ActivityEntryBO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
                AssertUtil.assertStringNotBlank(request.getActivityId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????id????????????");
                AssertUtil.assertStringNotBlank(request.getTitle(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "????????????????????????");
                AssertUtil.assertTrue(request.getNumber()>0,  "???????????????????????????");
                AssertUtil.assertStringNotBlank(request.getLinkman(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getContact(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertTrue(request.getContact().matches("^(13[0-9]|14[5-9]|15[0-3,5-9]|16[2,5,6,7]|17[0-8]|18[0-9]|19[1,3,5,8,9])\\d{8}$"),"????????????????????????");
                AssertUtil.assertNotNull(request.getStart(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????????????????");
                AssertUtil.assertNotNull(request.getEnd(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????????????????");
                boolean validateTime = DateUtil.parse(request.getStart(),"yyyy-MM-dd hh:mm:ss").before(DateUtil.parse(request.getEnd(),"yyyy-MM-dd hh:mm:ss"));
                AssertUtil.assertTrue(validateTime, "????????????????????????????????????????????????");
                AssertUtil.assertStringNotBlank(request.getNote(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "????????????????????????");
            }

            @Override
            public Result<ActivityEntryBO> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityEntryRequest activityEntryRequest = ActivityEntryRequestBuilder.anActivityEntryRequest()
                        .withActivityId(request.getActivityId())
                        .withTitle(request.getTitle())
                        .withNumber(request.getNumber())
                        .withLinkman(request.getLinkman())
                        .withContact(request.getContact())
                        .withStart(DateUtil.parse(request.getStart(),"yyyy-MM-dd hh:mm:ss").getTime())
                        .withEnd(DateUtil.parse(request.getEnd(),"yyyy-MM-dd hh:mm:ss").getTime())
                        .withChoose(request.getChoose())
                        .withTop(request.getTop())
                        .withNote(request.getNote())
                        .build();
                ActivityEntryBO activityEntryBO = activityEntryService.createActivityEntry(activityEntryRequest);
                return RestResultUtil.buildSuccessResult(activityEntryBO, "??????????????????");
            }
        });
    }

    /**
     * ??????????????????
     * @param request
     * @param httpServletRequest
     * @param response
     * @return
     */
    @CheckLogin
    @GetMapping(value = "activityEntryRecordFile",produces = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public void getActivityEntryRecordFileByActivityEntryId(ActivityEntryRestRequest request, HttpServletRequest httpServletRequest, HttpServletResponse response) throws IOException {

        AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
            AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
        AssertUtil.assertStringNotBlank(request.getActivityEntryId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "????????????id????????????");

        OperateContext context = new OperateContext();
        context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
        List<UserInfoBO> UserInfoBOLists=  activityEntryService.getActivityEntryRecordUserInfoFileByActivityEntryId(request.getActivityEntryId());
        String title = activityEntryService.getActivityEntryTitle(request.getActivityEntryId());
        ExcelWriter writer = ExcelUtil.getWriter(true);

        writer.addHeaderAlias("userInfoId", "????????????id");
        writer.addHeaderAlias("userId", "??????id");
        writer.addHeaderAlias("stuId", "??????");
        writer.addHeaderAlias("realName", "??????");
        writer.addHeaderAlias("sex", "??????");
        writer.addHeaderAlias("major", "??????");
        writer.addHeaderAlias("classId", "??????");
        writer.addHeaderAlias("grade", "??????");
        writer.addHeaderAlias("enrollDate", "????????????");
        writer.addHeaderAlias("extInfo", "????????????");
        writer.merge(9, title+"--????????????");
        writer.write(UserInfoBOLists, true);
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(title,"UTF-8") +".xls");
        ServletOutputStream out=response.getOutputStream();

        writer.flush(out, true);
        writer.close();
        IoUtil.close(out);
    }


    /**
     * ??????????????????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @PutMapping
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<ActivityEntryBO> update(ActivityEntryRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "????????????", request, new RestOperateCallBack<ActivityEntryBO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getActivityEntryId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "????????????id????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
            }

            @Override
            public Result<ActivityEntryBO> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                ActivityEntryRequest activityEntryRequest = ActivityEntryRequestBuilder.anActivityEntryRequest()
                        .withActivityEntryId(request.getActivityEntryId())
                        .withState(request.getState())
                        .withTitle(request.getTitle())
                        .withNumber(request.getNumber())
                        .withLinkman(request.getLinkman())
                        .withContact(request.getContact())
                        .withStart(DateUtil.parse(request.getStart(),"yyyy-MM-dd hh:mm:ss").getTime())
                        .withEnd(DateUtil.parse(request.getEnd(),"yyyy-MM-dd hh:mm:ss").getTime())
                        .withChoose(request.getChoose())
                        .withTop(request.getTop())
                        .withNote(request.getNote())
                        .build();
                ActivityEntryBO activityEntryBO = activityEntryService.updateActivityEntry(activityEntryRequest);
                return RestResultUtil.buildSuccessResult(activityEntryBO, "????????????????????????");
            }
        });
    }

}
