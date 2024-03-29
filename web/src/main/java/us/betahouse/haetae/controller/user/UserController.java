/*
  betahouse.us
  CopyRight (c) 2012 - 2018
 */
package us.betahouse.haetae.controller.user;

import com.alibaba.fastjson.JSONObject;
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
import us.betahouse.haetae.model.user.request.UserFeedBackRestRequest;
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
import us.betahouse.haetae.serviceimpl.user.request.UserFeedBackRequest;
import us.betahouse.haetae.serviceimpl.user.routingtable.UserRoutingTable;
import us.betahouse.haetae.serviceimpl.user.service.UserFeedBackService;
import us.betahouse.haetae.serviceimpl.user.service.UserService;
import us.betahouse.haetae.user.dal.model.UserFeedBackDO;
import us.betahouse.haetae.user.dal.service.PermRepoService;
import us.betahouse.haetae.user.dal.service.RoleRepoService;
import us.betahouse.haetae.user.dal.service.UserInfoRepoService;
import us.betahouse.haetae.user.model.CommonUser;
import us.betahouse.haetae.user.model.basic.UserFeedBackBO;
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
 * 用户接口
 *
 * @author dango.yxm
 * @version : UserController.java 2018/11/21 6:19 PM dango.yxm
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/user")
public class UserController {

    /**
     * 日志
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

    @Autowired
    private UserFeedBackService userFeedBackService;

    /**
     * 登陆
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @PostMapping(value = "/openId")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<UserVO> wxLogin(UserRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "用户微信登录", null, new RestOperateCallBack<UserVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUsername(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户名不能为空");
                AssertUtil.assertStringNotBlank(request.getPassword(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "密码不能为空");
                AssertUtil.assertStringNotBlank(request.getCode(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "微信code不能为空");
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

                return RestResultUtil.buildSuccessResult(userVO, "登陆成功");
            }
        });
    }

    /**
     * 易班登陆
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @PostMapping(value = "/yiLogin")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<UserVO> yiLogin(UserRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "用户易班登录", null, new RestOperateCallBack<UserVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getCode(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "易班code不能为空");
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

                return RestResultUtil.buildSuccessResult(userVO, "登陆成功");
            }
        });
    }
    @DeleteMapping(value = "/openId")
    @CheckLogin
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<UserVO> wxLogout(UserRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "用户微信登出", request, new RestOperateCallBack<UserVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
            }

            @Override
            public Result<UserVO> execute() {
                CommonUserRequestBuilder builder = CommonUserRequestBuilder.getInstance()
                        .withRequestId(request.getRequestId())
                        .withUserId(request.getUserId());
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                userService.wxLogout(builder.build(), context);
                return RestResultUtil.buildSuccessResult("微信登出成功");
            }
        });
    }

    /**
     * 登陆
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CrossOrigin
    @PostMapping(value = "/token")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<UserVO> normalLogin(UserRequest request, HttpServletRequest httpServletRequest , HttpServletResponse response) {
        return RestOperateTemplate.operate(LOGGER, "用户普通登录", null, new RestOperateCallBack<UserVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUsername(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户名不能为空");
                AssertUtil.assertStringNotBlank(request.getPassword(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "密码不能为空");
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
                return RestResultUtil.buildSuccessResult(userVO, "登陆成功");
            }
        });
    }

    /**
     * 登陆
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @GetMapping
//    @CheckLogin
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<UserVO> checkLogin(UserRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "用户登录维持", request, new RestOperateCallBack<UserVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
            }

            @Override
            public Result<UserVO> execute() {
                String referer = httpServletRequest.getHeader("haha");
                String header = httpServletRequest.getHeader("ka");
                Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
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

                return RestResultUtil.buildSuccessResult(userVO, "登陆成功");
            }
        });
    }

    /**
     * 根据token获取用户信息
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
        return RestOperateTemplate.operate(LOGGER, "用户普通登录", null, new RestOperateCallBack<UserVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户id不能为空");
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
                return RestResultUtil.buildSuccessResult(userVO, "获取信息成功");
            }
        });
    }



    /**
     * 获取信用分
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @GetMapping("/creditScore")
    @CheckLogin
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<Long> getCreditScore(UserRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "获取信用分", request, new RestOperateCallBack<Long>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
            }

            @Override
            public Result<Long> execute() {
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                return RestResultUtil.buildSuccessResult(activityBlacklistService.getCreditScoreByUserIdAndTerm(request.getUserId(),request.getTerm() == null ? TermUtil.getNowTerm() : request.getTerm()), "查询信用分成功");
            }
        });
    }

    /**
     * 用户登出
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
        return RestOperateTemplate.operate(LOGGER, "用户普通登出", request, new RestOperateCallBack<UserVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
            }

            @Override
            public Result<UserVO> execute() {
                CommonUserRequestBuilder builder = CommonUserRequestBuilder.getInstance()
                        .withRequestId(request.getRequestId())
                        .withUserId(request.getUserId());
                OperateContext context = new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                userService.logout(builder.build(), context);
                return RestResultUtil.buildSuccessResult("登出成功");
            }
        });
    }

    /**
     * 修改密码
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
        return RestOperateTemplate.operate(LOGGER, "修改密码", null, new RestOperateCallBack<UserVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
                AssertUtil.assertStringNotBlank(request.getPassword(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "原密码不能为空");
                AssertUtil.assertStringNotBlank(request.getNewPassword(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "新密码不能为空");
                boolean oldNewNotEqual = !StringUtils.equals(request.getPassword(), request.getNewPassword());
                AssertUtil.assertTrue(oldNewNotEqual, "新旧密码不能一致");
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
                return RestResultUtil.buildSuccessResult("密码修改成功");
            }
        });
    }

    /**
     * 根据学号修改密码
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
        return RestOperateTemplate.operate(LOGGER, "根据学号修改密码", null, new RestOperateCallBack<UserVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getStuId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "学号不能为空");
                AssertUtil.assertStringNotBlank(request.getNewPassword(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "新密码不能为空");
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
                return RestResultUtil.buildSuccessResult("密码修改成功");
            }
        });
    }

    /**
     * 修改班级、年级、专业
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @PutMapping(value = "/message")
    @CheckLogin
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<UserVO> modifyMessage(UserRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "修改班级、年级、专业", null, new RestOperateCallBack<UserVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户不能为空");
                AssertUtil.assertNotNull(request.getGrade(), "年级不能为空");
                AssertUtil.assertNotNull(request.getClassId(), "班级不能为空");
                AssertUtil.assertNotNull(request.getMajor(), "专业不能为空");
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
                return RestResultUtil.buildSuccessResult("信息登记成功");
            }
        });
    }

    @PostMapping("/uploaduserexcel")
    @CheckLogin
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<List<String>> uploadUserExcel(@PathVariable("file") MultipartFile file,UserRequest request,HttpServletRequest httpServletRequest){
        return RestOperateTemplate.operate(LOGGER, "根据excel批量创建用户", request, new RestOperateCallBack<List<String>>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(file,CommonResultCode.ILLEGAL_PARAMETERS.getCode(),"上传文件不能为空");
            }

            @Override
            public Result<List<String>> execute(){
                UploadUserExcelRequest uploadUserExcelRequest=new UploadUserExcelRequest();
                uploadUserExcelRequest.setUserId(request.getUserId());
                OperateContext context=new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                List<String> titles=userService.saveUserByExcel(uploadUserExcelRequest,file,context);
                return RestResultUtil.buildSuccessResult(titles,"导入excel成功，返回标题");
            }
        });
    }

    @GetMapping("/downloadtemplate")
    public Result<String> download(UserRequest request,HttpServletResponse response) {
        return RestOperateTemplate.operate(LOGGER, "下载新生模板", request, new RestOperateCallBack<String>() {
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
                    // 清空response
                    response.reset();
// 设置response的Header
                    response.setCharacterEncoding("UTF-8");
//Content-Disposition的作用：告知浏览器以何种方式显示响应返回的文件，用浏览器打开还是以附件的形式下载到本地保存
//attachment表示以附件方式下载 inline表示在线打开 "Content-Disposition: inline; filename=文件名.mp3"
// filename表示文件的默认名称，因为网络传输只支持URL编码的相关支付，因此需要将文件名URL编码后进行传输,前端收到后需要反编码才能获取到真正的名称
                    response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
// 告知浏览器文件的大小
//                    response.addHeader("Content-Length", "" + file.length());
                    response.setContentType("application/octet-stream");
                    while (fis.read(buffer)!=-1){
                        outputStream.write(buffer);
                    }
                    outputStream.flush();
                } catch (Exception e) {
                    throw new BetahouseException(CommonResultCode.SYSTEM_ERROR.getCode(),"下载文件失败，资源不存在或未找到");
                }finally {
//                    fileInputStream.close();
                    fis.close();
                    outputStream.flush();
                    outputStream.close();
                }
                return RestResultUtil.buildSuccessResult("下载成功");
            }
        });
    }

    /**
     * 管理员/活动负责人 登陆
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @PostMapping(value = "/managetoken")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<UserVO> normalManagerLogin(UserRequest request, HttpServletRequest httpServletRequest , HttpServletResponse response) {
        return RestOperateTemplate.operate(LOGGER, "管理员登录", null, new RestOperateCallBack<UserVO>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUsername(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户名不能为空");
                AssertUtil.assertStringNotBlank(request.getPassword(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "密码不能为空");
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
                return RestResultUtil.buildSuccessResult(userVO, "登陆成功");
            }
        });
    }

    /**
     * 给予一位用户导章权限
     *
     * @param request
     * @param httpServletRequest
     * @return
     */
    @CheckLogin
    @PutMapping(value = "/stampmanager")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<String> giveStamperPerm(UserRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "给予一位用户导章权限", request, new RestOperateCallBack<String>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "操作员id不能为空");
                AssertUtil.assertNotNull(request.isCanStamp(),RestResultCode.ILLEGAL_PARAMETERS.getCode(), "操作员id不能为空");
            }

            @Override
            public Result<String> execute() {
                if(request.getOperatedId()==null||request.getOperatedId().equals("")){
                    AssertUtil.assertStringNotBlank(request.getStuId(),"id和学号请至少含有一个参数");
                    CommonUser commonUser;
                    AssertUtil.assertNotNull((commonUser=userService.findByStuid(request.getStuId())),CommonResultCode.ILLEGAL_PARAMETERS.getCode(),"通过学号查无此用户");
                    request.setOperatedId(commonUser.getUserId());
                }
                CommonUserRequestBuilder builder=CommonUserRequestBuilder.getInstance()
                        .withUserId(request.getUserId())
                        .withOperateId(request.getOperatedId());
                OperateContext context=new OperateContext();
                context.setOperateIP(IPUtil.getIpAddr(httpServletRequest));
                if(request.isCanStamp()){
                    userService.giveStamperPerm(builder.build(),context);
                    return RestResultUtil.buildSuccessResult("给予权限成功，学号："+request.getStuId());
                }else {
                    userService.unBindStamperPerm(builder.build(),context);
                    return RestResultUtil.buildSuccessResult("解绑权限成功,学号："+request.getStuId());
                }
            }
        });
    }

    @CheckLogin
    @GetMapping(value = "/routingtable")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<List<UserRoutingTable>> getRoutingTable(UserRequest request,HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "拉取路由表", request, new RestOperateCallBack<List<UserRoutingTable>>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertStringNotBlank(request.getUserId(), RestResultCode.ILLEGAL_PARAMETERS.getCode(), "用户id不能为空");
            }

            @Override
            public Result<List<UserRoutingTable>> execute() {
                List<UserRoutingTable> routingTable = userService.getRoutingTable(request.getUserId());
                return RestResultUtil.buildSuccessResult(routingTable,"拉取路由表成功");
            }
        });
    }

    @CheckLogin
    @GetMapping(value = "/unQualified/undergraduate")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<UserInfoBO>> getUnQualified1(UserRequest request,HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "查询大四普通本科未达毕业要求学生信息", request, new RestOperateCallBack<PageList<UserInfoBO>>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
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
                return RestResultUtil.buildSuccessResult(userInfoRepoService.findUnQualifiedUndergraduate(page,limit),"查询大四普通本科未达毕业要求学生成功");
            }
        });
    }

    @CheckLogin
    @GetMapping(value = "/myfeedback")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<UserFeedBackBO>> getUserFeedBack(UserFeedBackRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "查询自己的反馈记录", request, new RestOperateCallBack<PageList<UserFeedBackBO>>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertNotNull(request.getUserId(),RestResultCode.UNAUTHORIZED);
            }
            @Override
            public Result<PageList<UserFeedBackBO>> execute() {
                UserFeedBackRequest feedBackRequest=new UserFeedBackRequest();
                feedBackRequest.setPageable(request.getPageable());
                feedBackRequest.setUserId(request.getUserId());
                return RestResultUtil.buildSuccessResult(userFeedBackService.getUserFeedBack(feedBackRequest),"用户查询反馈记录成功");
            }
        });
    }

    @CheckLogin
    @GetMapping(value = "/userfeedback")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<UserFeedBackBO>> getFeedBackByUserId(UserFeedBackRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "查询某一用户的反馈记录", request, new RestOperateCallBack<PageList<UserFeedBackBO>>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertNotNull(request.getUserId(),RestResultCode.UNAUTHORIZED);
                AssertUtil.assertNotNull(request.getTargetId(),RestResultCode.ILLEGAL_PARAMETERS.getCode(),"查询用户的id不能为空");
            }
            @Override
            public Result<PageList<UserFeedBackBO>> execute() {
                UserFeedBackRequest feedBackRequest=new UserFeedBackRequest();
                feedBackRequest.setPageable(request.getPageable());
                feedBackRequest.setUserId(request.getUserId());
                feedBackRequest.setTargetId(request.getTargetId());
                return RestResultUtil.buildSuccessResult(userFeedBackService.getFeedBackByUserId(feedBackRequest),"查询用户反馈记录成功");
            }
        });
    }

    @CheckLogin
    @GetMapping(value = "/feedback")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<UserFeedBackBO>> getFeedBack(UserFeedBackRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "查询所有反馈记录", request, new RestOperateCallBack<PageList<UserFeedBackBO>>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertNotNull(request.getUserId(),RestResultCode.UNAUTHORIZED);
            }
            @Override
            public Result<PageList<UserFeedBackBO>> execute() {
                UserFeedBackRequest feedBackRequest=new UserFeedBackRequest();
                feedBackRequest.setPageable(request.getPageable());
                feedBackRequest.setUserId(request.getUserId());
                return RestResultUtil.buildSuccessResult(userFeedBackService.getAllFeedBack(feedBackRequest),"查询所有反馈记录成功");
            }
        });
    }

    @CheckLogin
    @GetMapping(value = "/versionfeedback")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<PageList<UserFeedBackBO>> getVersionFeedBack(UserFeedBackRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "根据版本查询反馈记录", request, new RestOperateCallBack<PageList<UserFeedBackBO>>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertNotNull(request.getUserId(),RestResultCode.UNAUTHORIZED);
                AssertUtil.assertNotNull(request.getVersion(),RestResultCode.ILLEGAL_PARAMETERS.getCode(),"版本号不能为空");
            }
            @Override
            public Result<PageList<UserFeedBackBO>> execute() {
                UserFeedBackRequest feedBackRequest=new UserFeedBackRequest();
                feedBackRequest.setPageable(request.getPageable());
                feedBackRequest.setVersion(request.getVersion());
                feedBackRequest.setUserId(request.getUserId());
                return RestResultUtil.buildSuccessResult(userFeedBackService.getAllFeedBackByVersion(feedBackRequest),"用户查询反馈记录成功");
            }
        });
    }

    @CheckLogin
    @PostMapping(value = "/feedback")
    @Log(loggerName = LoggerName.WEB_DIGEST)
    public Result<Void> saveFeedBack(UserFeedBackRestRequest request, HttpServletRequest httpServletRequest) {
        return RestOperateTemplate.operate(LOGGER, "提交反馈记录", request, new RestOperateCallBack<Void>() {
            @Override
            public void before() {
                AssertUtil.assertNotNull(request, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "请求体不能为空");
                AssertUtil.assertNotNull(request.getUserId(),RestResultCode.UNAUTHORIZED);
                AssertUtil.assertNotNull(request.getContext(),RestResultCode.ILLEGAL_PARAMETERS.getCode(),"反馈内容不能为空");
                AssertUtil.assertNotNull(request.getVersion(),RestResultCode.ILLEGAL_PARAMETERS.getCode(),"反馈的版本信息不能为空");
                AssertUtil.assertNotNull(request.getContext(),RestResultCode.ILLEGAL_PARAMETERS.getCode(),"反馈内容不能为空");
            }
            @Override
            public Result<Void> execute() {
                UserFeedBackDO userFeedBackDO=new UserFeedBackDO();
                userFeedBackDO.setVersion(request.getVersion());
                userFeedBackDO.setExtInfo(JSONObject.toJSONString(request.getExtInfo()));
                userFeedBackDO.setFeedBackHead(request.getFeedBackHead());
                userFeedBackDO.setFeedBackNext(request.getFeedBackNext());
                userFeedBackDO.setUserId(request.getUserId());
                userFeedBackDO.setTitle(request.getTitle());
                userFeedBackDO.setContext(request.getContext());
                UserFeedBackRequest feedBackRequest=new UserFeedBackRequest();
                feedBackRequest.setUserFeedBackDO(userFeedBackDO);
                userFeedBackService.save(feedBackRequest);
                return RestResultUtil.buildSuccessResult(null,"保存反馈成功");
            }
        });
    }


    //每天凌晨两点半刷新未合格标志
    @Scheduled(cron = "0 30 2 * * ?")
    @PutMapping("update/unqualified")
    public void updateUnqualified(){
        userInfoRepoService.updateUndergraduateState();
        userInfoRepoService.updateCollegeUpgradeState();
    }
}


