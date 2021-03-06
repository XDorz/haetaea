/*
  betahouse.us
  CopyRight (c) 2012 - 2018
 */
package us.betahouse.haetae.controller.user;

import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import us.betahouse.haetae.common.log.LoggerName;
import us.betahouse.haetae.common.session.CheckLogin;
import us.betahouse.haetae.common.template.RestOperateCallBack;
import us.betahouse.haetae.common.template.RestOperateTemplate;
import us.betahouse.haetae.converter.UserVOConverter;
import us.betahouse.haetae.model.user.request.UserRequest;
import us.betahouse.haetae.model.user.vo.UserVO;
import us.betahouse.haetae.organization.model.OrganizationMemberBO;
import us.betahouse.haetae.serviceimpl.activity.service.ActivityBlacklistService;
import us.betahouse.haetae.serviceimpl.common.OperateContext;
import us.betahouse.haetae.serviceimpl.common.constant.UserRequestExtInfoKey;
import us.betahouse.haetae.serviceimpl.common.utils.TermUtil;
import us.betahouse.haetae.serviceimpl.organization.request.OrganizationRequest;
import us.betahouse.haetae.serviceimpl.organization.service.OrganizationService;
import us.betahouse.haetae.serviceimpl.user.builder.CommonUserRequestBuilder;
import us.betahouse.haetae.serviceimpl.user.request.CommonUserRequest;
import us.betahouse.haetae.serviceimpl.user.request.UploadUserExcelRequest;
import us.betahouse.haetae.serviceimpl.user.routingtable.UserRoutingTable;
import us.betahouse.haetae.serviceimpl.user.service.RoleService;
import us.betahouse.haetae.serviceimpl.user.service.UserService;
import us.betahouse.haetae.user.dal.service.PermRepoService;
import us.betahouse.haetae.user.dal.service.RoleRepoService;
import us.betahouse.haetae.user.dal.service.UserInfoRepoService;
import us.betahouse.haetae.user.model.CommonUser;
import us.betahouse.haetae.user.model.basic.UserInfoBO;
import us.betahouse.haetae.user.model.basic.perm.PermBO;
import us.betahouse.haetae.user.model.basic.perm.RoleBO;
import us.betahouse.haetae.user.model.common.PageList;
import us.betahouse.haetae.utils.IPUtil;
import us.betahouse.haetae.utils.RestResultUtil;
import us.betahouse.util.common.Result;
import us.betahouse.util.enums.CommonResultCode;
import us.betahouse.util.enums.RestResultCode;
import us.betahouse.util.exceptions.BetahouseException;
import us.betahouse.util.log.Log;
import us.betahouse.util.utils.AssertUtil;
import us.betahouse.util.utils.CollectionUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ????????????
 *
 * @author dango.yxm
 * @version : UserController.java 2018/11/21 6:19 PM dango.yxm
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/user")
public class UserController {

    /**
     * ??????
     */
    private final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;
  
    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private ActivityBlacklistService activityBlacklistService;

    @Autowired
    private PermRepoService permRepoService;

    @Autowired
    private RoleRepoService roleRepoService;

    @Autowired
    private UserInfoRepoService userInfoRepoService;

    /**
     * ??????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @PostMapping(value = "/openId")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<UserVO> wxLogin(UserRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "??????????????????", null, new RestOperateCallBack<UserVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUsername(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getPassword(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
                AssertUtil.assertStringNotBlank(request.getCode(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????code????????????");
            }

            @Override
            public Result<UserVO> execute() {
                CommonUserRequestBuilder builder = CommonUserRequestBuilder.getInstance()
                        .withRequestId(request.getRequestId())
                        .withUsername(request.getUsername()).withPassword(request.getPassword())
                        .withCode(request.getCode())
                        .withAvatarUrl(request.getAvatarUrl());
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                UserVO userVO = UserVOConverter.convert(userService.login(builder.build(), context));
                OrganizationRequest organizationRequest=new OrganizationRequest();
                organizationRequest.setMemberId(userVO.getUserId());
                List<String> list= CollectionUtils.toStream(organizationService.queryOrganizationMemberByMemberId(organizationRequest)).map(OrganizationMemberBO::findJob).distinct().collect(Collectors.toList());
                userVO.setJobInfo(list);

                CommonUserRequest commonUserRequest=new CommonUserRequest();
                commonUserRequest.setUserId(userVO.getUserId());
                List<String> roleIds=CollectionUtils.toStream(userService.fetchUserRoles(commonUserRequest,context).values()).map(RoleBO::getRoleCode).collect(Collectors.toList());
//                List<String> rolePermNames = CollectionUtils.toStream(permRepoService.batchQueryPermByRoleId(roleIds)).map(PermBO::getPermType).collect(Collectors.toList());
                userVO.setRoleInfo(roleIds);

                return RestResultUtil.buildSuccessResult(userVO, "????????????");
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
    @PostMapping(value = "/yiLogin")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<UserVO> yiLogin(UserRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "??????????????????", null, new RestOperateCallBack<UserVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getCode(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????code????????????");
            }
            @Override
            public Result<UserVO> execute() {
                CommonUserRequestBuilder builder = CommonUserRequestBuilder.getInstance()
                        .withCode(request.getCode());
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                UserVO userVO = UserVOConverter.convert(userService.yiLogin(builder.build(), context));
                OrganizationRequest organizationRequest=new OrganizationRequest();
                organizationRequest.setMemberId(userVO.getUserId());
                List<String> list= CollectionUtils.toStream(organizationService.queryOrganizationMemberByMemberId(organizationRequest)).map(OrganizationMemberBO::findJob).distinct().collect(Collectors.toList());
                userVO.setJobInfo(list);

                CommonUserRequest commonUserRequest=new CommonUserRequest();
                commonUserRequest.setUserId(userVO.getUserId());
                List<String> roleIds=CollectionUtils.toStream(userService.fetchUserRoles(commonUserRequest,context).values()).map(RoleBO::getRoleId).collect(Collectors.toList());
                List<String> rolePermNames = CollectionUtils.toStream(permRepoService.batchQueryPermByRoleId(roleIds)).map(PermBO::getPermType).collect(Collectors.toList());
                userVO.setRoleInfo(rolePermNames);

                return RestResultUtil.buildSuccessResult(userVO, "????????????");
            }
        });
    }
    @DeleteMapping(value = "/openId")
    @CheckLogin
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<UserVO> wxLogout(UserRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "??????????????????", request, new RestOperateCallBack<UserVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
            }

            @Override
            public Result<UserVO> execute() {
                CommonUserRequestBuilder builder = CommonUserRequestBuilder.getInstance()
                        .withRequestId(request.getRequestId())
                        .withUserId(request.getUserId());
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                userService.wxLogout(builder.build(), context);
                return RestResultUtil.buildSuccessResult("??????????????????");
            }
        });
    }

    /**
     * ??????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CrossOrigin
    @PostMapping(value = "/token")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<UserVO> normalLogin(UserRequest request, HttpServletRequest httpServletRequest , HttpServletResponse response) {
        return RestOperateTemplate.operate(LOGGER, "??????????????????", null, new RestOperateCallBack<UserVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUsername(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getPassword(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
            }

            @Override
            public Result<UserVO> execute() {
                CommonUserRequestBuilder builder = CommonUserRequestBuilder.getInstance()
                        .withRequestId(request.getRequestId())
                        .withUsername(request.getUsername()).withPassword(request.getPassword());
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));

                UserVO userVO = UserVOConverter.convert(userService.login(builder.build(), context));
                OrganizationRequest organizationRequest=new OrganizationRequest();
                organizationRequest.setMemberId(userVO.getUserId());

                List<String> list= CollectionUtils.toStream(organizationService.queryOrganizationMemberByMemberId(organizationRequest)).map(OrganizationMemberBO::findJob).distinct().collect(Collectors.toList());
                userVO.setJobInfo(list);

                CommonUserRequest commonUserRequest=new CommonUserRequest();
                commonUserRequest.setUserId(userVO.getUserId());
                List<String> roleIds=CollectionUtils.toStream(userService.fetchUserRoles(commonUserRequest,context).values()).map(RoleBO::getRoleId).collect(Collectors.toList());
                List<String> rolePermNames = CollectionUtils.toStream(permRepoService.batchQueryPermByRoleId(roleIds)).map(PermBO::getPermType).collect(Collectors.toList());
                userVO.setRoleInfo(rolePermNames);


                Cookie cookie = new Cookie("UserToken", userVO.getToken());

                response.addCookie(cookie);
                return RestResultUtil.buildSuccessResult(userVO, "????????????");
            }
        });
    }

    /**
     * ??????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @GetMapping
    @CheckLogin
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<UserVO> checkLogin(UserRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "??????????????????", request, new RestOperateCallBack<UserVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
            }

            @Override
            public Result<UserVO> execute() {
                CommonUserRequestBuilder builder = CommonUserRequestBuilder.getInstance()
                        .withRequestId(request.getRequestId())
                        .withUserId(request.getUserId());
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                UserVO userVO = UserVOConverter.convert(userService.fetchUser(builder.build(), context));
                OrganizationRequest organizationRequest=new OrganizationRequest();
                organizationRequest.setMemberId(userVO.getUserId());
                List<String> list= CollectionUtils.toStream(organizationService.queryOrganizationMemberByMemberId(organizationRequest)).map(OrganizationMemberBO::findJob).distinct().collect(Collectors.toList());
                userVO.setJobInfo(list);

                CommonUserRequest commonUserRequest=new CommonUserRequest();
                commonUserRequest.setUserId(userVO.getUserId());
                List<String> roleIds=CollectionUtils.toStream(userService.fetchUserRoles(commonUserRequest,context).values()).map(RoleBO::getRoleCode).collect(Collectors.toList());
//                List<String> rolePermNames = CollectionUtils.toStream(permRepoService.batchQueryPermByRoleId(roleIds)).map(PermBO::getPermType).collect(Collectors.toList());
                userVO.setRoleInfo(roleIds);

                return RestResultUtil.buildSuccessResult(userVO, "????????????");
            }
        });
    }

    /**
     * ??????token??????????????????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CrossOrigin
    @GetMapping(value = "/token")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    @CheckLogin
    public Result<UserVO> getUserInfoByToken(UserRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "??????????????????", null, new RestOperateCallBack<UserVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????id????????????");
            }

            @Override
            public Result<UserVO> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                CommonUser commonUser =  userService.queryCommonByUserId(request.getUserId(), context);
                UserVO userVO = UserVOConverter.convert(commonUser);
                OrganizationRequest organizationRequest=new OrganizationRequest();
                organizationRequest.setMemberId(request.getUserId());

                List<String> list= CollectionUtils.toStream(organizationService.queryOrganizationMemberByMemberId(organizationRequest)).map(OrganizationMemberBO::findJob).distinct().collect(Collectors.toList());
                userVO.setJobInfo(list);

                CommonUserRequest commonUserRequest=new CommonUserRequest();
                commonUserRequest.setUserId(userVO.getUserId());
                List<String> roleIds=CollectionUtils.toStream(userService.fetchUserRoles(commonUserRequest,context).values()).map(RoleBO::getRoleCode).collect(Collectors.toList());
//                List<String> rolePermNames = CollectionUtils.toStream(permRepoService.batchQueryPermByRoleId(roleIds)).map(PermBO::getPermType).collect(Collectors.toList());
                userVO.setRoleInfo(roleIds);
                return RestResultUtil.buildSuccessResult(userVO, "??????????????????");
            }
        });
    }



    /**
     * ???????????????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @GetMapping("/creditScore")
    @CheckLogin
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<Long> getCreditScore(UserRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "???????????????", request, new RestOperateCallBack<Long>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
            }

            @Override
            public Result<Long> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                return RestResultUtil.buildSuccessResult(activityBlacklistService.getCreditScoreByUserIdAndTerm(request.getUserId(),request.getTerm() == null ? TermUtil.getNowTerm() : request.getTerm()), "?????????????????????");
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
    @CrossOrigin
    @DeleteMapping(value = "/token")
    @CheckLogin
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<UserVO> logout(UserRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "??????????????????", request, new RestOperateCallBack<UserVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
            }

            @Override
            public Result<UserVO> execute() {
                CommonUserRequestBuilder builder = CommonUserRequestBuilder.getInstance()
                        .withRequestId(request.getRequestId())
                        .withUserId(request.getUserId());
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                userService.logout(builder.build(), context);
                return RestResultUtil.buildSuccessResult("????????????");
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
    @CrossOrigin
    @PutMapping(value = "/pwd")
    @CheckLogin
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<UserVO> modifyPassword(UserRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "????????????", null, new RestOperateCallBack<UserVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
                AssertUtil.assertStringNotBlank(request.getPassword(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getNewPassword(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                boolean oldNewNotEqual = !StringUtils.equals(request.getPassword(), request.getNewPassword());
                AssertUtil.assertTrue(oldNewNotEqual, "????????????????????????");
            }

            @Override
            public Result<UserVO> execute() {
                CommonUserRequestBuilder builder = CommonUserRequestBuilder.getInstance()
                        .withRequestId(request.getRequestId())
                        .withPassword(request.getPassword())
                        .withUserId(request.getUserId());
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));

                CommonUserRequest commonUserRequest = builder.build();
                commonUserRequest.putExtInfo(UserRequestExtInfoKey.USER_NEW_PASSWORD, request.getNewPassword());

                userService.modifyUser(commonUserRequest, context);
                return RestResultUtil.buildSuccessResult("??????????????????");
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
    @CrossOrigin
    @PutMapping(value = "/pwdByStuId")
    @CheckLogin
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<UserVO> modifyPasswordByStuId(UserRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "????????????????????????", null, new RestOperateCallBack<UserVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getStuId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
                AssertUtil.assertStringNotBlank(request.getNewPassword(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
            }

            @Override
            public Result<UserVO> execute() {
                CommonUserRequestBuilder builder = CommonUserRequestBuilder.getInstance()
                        .withStuId(request.getStuId())
                        .withPassword(request.getPassword())
                        .withUserId(request.getUserId());
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));

                CommonUserRequest commonUserRequest = builder.build();
                commonUserRequest.putExtInfo(UserRequestExtInfoKey.USER_NEW_PASSWORD, request.getNewPassword());

                userService.modifyPwdByStuId(commonUserRequest, context);
                return RestResultUtil.buildSuccessResult("??????????????????");
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
    @PutMapping(value = "/message")
    @CheckLogin
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<UserVO> modifyMessage(UserRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "??????????????????????????????", null, new RestOperateCallBack<UserVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
                AssertUtil.assertNotNull(request.getGrade(), "??????????????????");
                AssertUtil.assertNotNull(request.getClassId(), "??????????????????");
                AssertUtil.assertNotNull(request.getMajor(), "??????????????????");
            }

            @Override
            public Result<UserVO> execute() {
                UserInfoBO userInfoBO = new UserInfoBO();
                userInfoBO.setGrade(request.getGrade());
                userInfoBO.setClassId(request.getClassId());
                userInfoBO.setMajor(request.getMajor());
                userInfoBO.setUserId(request.getUserId());
                CommonUserRequestBuilder builder = CommonUserRequestBuilder.getInstance()
                        .withUserInfoBO(userInfoBO)
                        .withUserId(request.getUserId());
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));

                CommonUserRequest commonUserRequest = builder.build();
                userService.modifyUserMajorAndClassAndGrade(commonUserRequest, context);
                return RestResultUtil.buildSuccessResult("??????????????????");
            }
        });
    }

    @PostMapping("/uploaduserexcel")
    @CheckLogin
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<List<String>> uploadUserExcel(@PathVariable("file") MultipartFile file,UserRequest request,HttpServletRequest httpServletRequest){
        return RestOperateTemplate.operate(LOGGER, "??????excel??????????????????", request, new RestOperateCallBack<List<String>>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(file,CommonResultCode.ILLEGAL_PARAMETERS.getCode(),"????????????????????????");
            }

            @Override
            public Result<List<String>> execute(){
                UploadUserExcelRequest uploadUserExcelRequest=new UploadUserExcelRequest();
                uploadUserExcelRequest.setUserId(request.getUserId());
                OperateContext context=new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                List<String> titles=userService.saveUserByExcel(uploadUserExcelRequest,file,context);
                return RestResultUtil.buildSuccessResult(titles,"??????excel?????????????????????");
            }
        });
    }

    @GetMapping("/downloadtemplate")
    public Result<String> download(UserRequest request,HttpServletResponse response) {
        return RestOperateTemplate.operate(LOGGER, "??????????????????", request, new RestOperateCallBack<String>() {
            @Override
            public void before() {
                RestOperateCallBack.super.before();
            }
            @Override
            public Result<String> execute() throws IOException {
//                String filePath = "classpath:download";
                String fileName = "template.xlsx";
//                File file = new File(filePath + File.separator + fileName);
//                File file= ResourceUtils.getFile(filePath);
//                FileInputStream fileInputStream = new FileInputStream(file);
//                InputStream fis = new BufferedInputStream(fileInputStream);
                InputStream fis=getClass().getClassLoader().getResourceAsStream("download/template.xlsx");
                OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
                try {
                    byte[] buffer = new byte[1024];
                    // ??????response
                    response.reset();
// ??????response???Header
                    response.setCharacterEncoding("UTF-8");
//Content-Disposition???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
//attachment??????????????????????????? inline?????????????????? "Content-Disposition: inline; filename=?????????.mp3"
// filename?????????????????????????????????????????????????????????URL????????????????????????????????????????????????URL?????????????????????,????????????????????????????????????????????????????????????
                    response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
// ??????????????????????????????
//                    response.addHeader("Content-Length", "" + file.length());
                    response.setContentType("application/octet-stream");
                    while (fis.read(buffer)!=-1){
                        outputStream.write(buffer);
                    }
                    outputStream.flush();
                } catch (Exception e) {
                    throw new BetahouseException(CommonResultCode.SYSTEM_ERROR.getCode(),"????????????????????????????????????????????????");
                }finally {
//                    fileInputStream.close();
                    fis.close();
                    outputStream.flush();
                    outputStream.close();
                }
                return RestResultUtil.buildSuccessResult("????????????");
            }
        });
    }

    /**
     * ?????????/??????????????? ??????
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @PostMapping(value = "/managetoken")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<UserVO> normalManagerLogin(UserRequest request, HttpServletRequest httpServletRequest , HttpServletResponse response) {
        return RestOperateTemplate.operate(LOGGER, "???????????????", null, new RestOperateCallBack<UserVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUsername(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getPassword(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
            }

            @Override
            public Result<UserVO> execute() {
                CommonUserRequestBuilder builder = CommonUserRequestBuilder.getInstance()
                        .withRequestId(request.getRequestId())
                        .withUsername(request.getUsername()).withPassword(request.getPassword());
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));

                UserVO userVO = UserVOConverter.convert(userService.loginProxy(builder.build(), context));
                OrganizationRequest organizationRequest=new OrganizationRequest();
                organizationRequest.setMemberId(userVO.getUserId());

                List<String> list= CollectionUtils.toStream(organizationService.queryOrganizationMemberByMemberId(organizationRequest)).map(OrganizationMemberBO::findJob).distinct().collect(Collectors.toList());
                userVO.setJobInfo(list);

                Cookie cookie = new Cookie("UserToken", userVO.getToken());

                response.addCookie(cookie);
                return RestResultUtil.buildSuccessResult(userVO, "????????????");
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
    @PutMapping(value = "/stampmanager")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<String> giveStamperPerm(UserRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "??????????????????????????????", request, new RestOperateCallBack<String>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????id????????????");
                AssertUtil.assertNotNull(request.isCanStamp(),RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????id????????????");
            }

            @Override
            public Result<String> execute() {
                if(request.getOperatedId()==null||request.getOperatedId().equals("")){
                    AssertUtil.assertStringNotBlank(request.getStuId(),"id????????????????????????????????????");
                    CommonUser commonUser;
                    AssertUtil.assertNotNull((commonUser=userService.findByStuid(request.getStuId())),CommonResultCode.ILLEGAL_PARAMETERS.getCode(),"???????????????????????????");
                    request.setOperatedId(commonUser.getUserId());
                }
                CommonUserRequestBuilder builder=CommonUserRequestBuilder.getInstance()
                        .withUserId(request.getUserId())
                        .withOperateId(request.getOperatedId());
                OperateContext context=new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                if(request.isCanStamp()){
                    userService.giveStamperPerm(builder.build(),context);
                    return RestResultUtil.buildSuccessResult("??????????????????????????????"+request.getStuId());
                }else {
                    userService.unBindStamperPerm(builder.build(),context);
                    return RestResultUtil.buildSuccessResult("??????????????????,?????????"+request.getStuId());
                }
            }
        });
    }

    @CheckLogin
    @GetMapping(value = "/routingtable")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<List<UserRoutingTable>> getRoutingTable(UserRequest request,HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "???????????????", request, new RestOperateCallBack<List<UserRoutingTable>>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????id????????????");
            }

            @Override
            public Result<List<UserRoutingTable>> execute() {
                List<UserRoutingTable> routingTable = userService.getRoutingTable(request.getUserId());
                return RestResultUtil.buildSuccessResult(routingTable,"?????????????????????");
            }
        });
    }

    @CheckLogin
    @GetMapping(value = "/unQualified/undergraduate")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<UserInfoBO>> getUnQualified1(UserRequest request,HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "??????????????????????????????????????????????????????", request, new RestOperateCallBack<PageList<UserInfoBO>>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
            }
            @Override
            public Result<PageList<UserInfoBO>> execute() {
                Integer limit = request.getLimit();
                Integer page = request.getPage();
                if(limit==null){
                    limit=5;
                }
                if (page==null){
                    page=0;
                }
                return RestResultUtil.buildSuccessResult(userInfoRepoService.findUnQualifiedUndergraduate(page,limit),"??????????????????????????????????????????????????????");
            }
        });
    }

    @CheckLogin
    @GetMapping(value = "/unQualified/collegeUpgrade")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<UserInfoBO>> getUnQualified2(UserRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "???????????????????????????????????????????????????", request, new RestOperateCallBack<PageList<UserInfoBO>>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
            }
            @Override
            public Result<PageList<UserInfoBO>> execute() {
                Integer limit = request.getLimit();
                Integer page = request.getPage();
                if(limit==null){
                    limit=5;
                }
                if (page==null){
                    page=0;
                }
                return RestResultUtil.buildSuccessResult(userInfoRepoService.findUnQualifiedCollegeUpgrade(page,limit),"???????????????????????????????????????????????????");
            }
        });
    }

    //??????????????????????????????????????????
    @Scheduled(cron = "0 30 2 * * ?")
    @PutMapping("update/unqualified")
    public void updateUnqualified(){
        userInfoRepoService.updateUndergraduateState();
        userInfoRepoService.updateCollegeUpgradeState();
    }
}


