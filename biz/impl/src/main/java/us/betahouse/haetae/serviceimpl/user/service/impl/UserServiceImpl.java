/*
  betahouse.us
  CopyRight (c) 2012 - 2018
 */
package us.betahouse.haetae.serviceimpl.user.service.impl;

import cn.hutool.poi.excel.ExcelReader;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import us.betahouse.haetae.serviceimpl.activity.constant.ActivityPermType;
import us.betahouse.haetae.serviceimpl.activity.enums.ActivityPermTypeEnum;
import us.betahouse.haetae.serviceimpl.common.OperateContext;
import us.betahouse.haetae.serviceimpl.common.constant.UserRequestExtInfoKey;
import us.betahouse.haetae.serviceimpl.common.verify.VerifyPerm;
import us.betahouse.haetae.serviceimpl.common.verify.VerifyRole;
import us.betahouse.haetae.serviceimpl.user.constant.GeneralPermType;
import us.betahouse.haetae.serviceimpl.user.constant.UserPermType;
import us.betahouse.haetae.serviceimpl.user.enums.GeneralManagerPermTypeEnum;
import us.betahouse.haetae.serviceimpl.user.enums.UserRoleCode;
import us.betahouse.haetae.serviceimpl.user.request.CommonUserRequest;
import us.betahouse.haetae.serviceimpl.user.request.UploadUserExcelRequest;
import us.betahouse.haetae.serviceimpl.user.routingtable.UserRoutingTable;
import us.betahouse.haetae.serviceimpl.user.service.UserService;
import us.betahouse.haetae.user.dal.service.PermRepoService;
import us.betahouse.haetae.user.dal.service.UserInfoRepoService;
import us.betahouse.haetae.user.dal.service.UserRepoService;
import us.betahouse.haetae.user.enums.RoleCode;
import us.betahouse.haetae.user.enums.UserErrorCode;
import us.betahouse.haetae.user.manager.UserManager;
import us.betahouse.haetae.user.model.CommonUser;
import us.betahouse.haetae.user.model.basic.UserInfoBO;
import us.betahouse.haetae.user.model.basic.perm.PermBO;
import us.betahouse.haetae.user.model.basic.perm.RoleBO;
import us.betahouse.haetae.user.model.basic.perm.UserBO;
import us.betahouse.haetae.user.request.UserManageRequest;
import us.betahouse.haetae.user.user.service.UserBasicService;
import us.betahouse.haetae.user.utils.EncryptUtil;
import us.betahouse.util.enums.CommonResultCode;
import us.betahouse.util.exceptions.BetahouseException;
import us.betahouse.util.template.ExcelTemplate;
import us.betahouse.util.utils.*;
import us.betahouse.util.validator.MultiValidator;
import us.betahouse.util.wechat.WeChatLoginUtil;
import us.betahouse.util.yiban.YibanUtil;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ??????????????????
 *
 * @author dango.yxm
 * @version : UserServiceImpl.java 2018/11/21 6:39 PM dango.yxm
 */
@Service
public class UserServiceImpl implements UserService {

    @Value("${wechat.appId}")
    private String APP_ID;

    @Value("${wechat.secret}")
    private String SECRET;

    @Autowired
    private UserRepoService userRepoService;

    @Autowired
    private UserInfoRepoService userInfoRepoService;

    @Autowired
    private UserManager userManager;

    @Autowired
    private UserBasicService userBasicService;

    @Autowired
    private YibanUtil yibanUtil;

    @Autowired
    private PermRepoService permRepoService;

    /**
     * ?????????????????????
     */
    @Autowired
    private MultiValidator<UserManageRequest> passwordValidator;


    @Override
    public CommonUser register(CommonUserRequest request, OperateContext context) {
        return userManager.create(request);
    }


    @Override
    public CommonUser login(CommonUserRequest request, OperateContext context) {
        // ??????openId
        String openId = null;
        if (StringUtils.isNotBlank(request.getCode())) {
            openId = WeChatLoginUtil.fetchOpenId(request.getCode(), APP_ID, SECRET);
        }
        return userBasicService.login(request.getUsername(), request.getPassword(), request.getAvatarUrl(), openId, context.getOperateIP());
    }

    @Override
    public CommonUser yiLogin(CommonUserRequest request, OperateContext context) {
        String yiStuId=yibanUtil.getStuId(yibanUtil.getAccessToken(request.getCode()));
        AssertUtil.assertNotNull(yiStuId, UserErrorCode.USER_NOT_EXIST);
        CommonUser commonUser=userBasicService.getByStuId(yiStuId);
        AssertUtil.assertNotNull(commonUser, UserErrorCode.USER_NOT_EXIST);
        return userBasicService.setToken(commonUser);
    }

    @Override
    public CommonUser fetchUser(CommonUserRequest request, OperateContext context) {
        return userBasicService.getByUserId(request.getUserId());
    }

    @Override
    public void logout(CommonUserRequest request, OperateContext context) {
        userBasicService.loginOut(request.getUserId());
    }

    @Override
    public void wxLogout(CommonUserRequest request, OperateContext context) {
        userBasicService.wxLoginOut(request.getUserId());
    }

    @Override
    public Map<String, PermBO> fetchUserPerms(CommonUserRequest request, OperateContext context) {
        return userBasicService.fetchUserPerms(request.getUserId());
    }

    @Override
    public Map<String, RoleBO> fetchUserRoles(CommonUserRequest request, OperateContext context) {
        return userBasicService.fetchUserRoles(request.getUserId());
    }

    @Override
    public void modifyUser(CommonUserRequest request, OperateContext context) {

        UserBO userBO = userRepoService.queryByUserId(request.getUserId());
        AssertUtil.assertNotNull(userBO, "???????????????");

        // ?????????????????? ???????????????????????????
        if (StringUtils.isNotBlank(request.fetchExtInfo(UserRequestExtInfoKey.USER_NEW_PASSWORD))) {
            // ????????????????????????????????? ???????????????
            if (StringUtils.isNotBlank(request.getPassword())) {
                String oldEncodePwd = EncryptUtil.encryptPassword(request.getPassword(), userBO.getSalt());
                AssertUtil.assertTrue(StringUtils.equals(userBO.getPassword(), oldEncodePwd), "???????????????");
            }

            // ?????????????????????????????????
            request.setPassword(request.fetchExtInfo(UserRequestExtInfoKey.USER_NEW_PASSWORD));
            // ??????????????????????????? ????????????
            passwordValidator.validate(request);

            userBO.setSalt(UUID.randomUUID().toString());
            userBO.setPassword(EncryptUtil.encryptPassword(request.getPassword(), userBO.getSalt()));
        }
        userRepoService.updateUserByUserId(userBO);
    }

    @Override
    @VerifyPerm(permType = UserPermType.USER_PASSWORD_RESET)
    public void modifyPwdByStuId(CommonUserRequest request, OperateContext context) {

        UserInfoBO userInfoBO = userInfoRepoService.queryUserInfoByStuId(request.getStuId());
        AssertUtil.assertNotNull(userInfoBO, "???????????????");
        UserBO userBO = userRepoService.queryByUserId(userInfoBO.getUserId());
        AssertUtil.assertNotNull(userBO, "???????????????");

        // ?????????????????????????????????
        request.setPassword(request.fetchExtInfo(UserRequestExtInfoKey.USER_NEW_PASSWORD));
        // ??????????????????????????? ????????????
        passwordValidator.validate(request);

        userBO.setSalt(UUID.randomUUID().toString());
        userBO.setPassword(EncryptUtil.encryptPassword(request.getPassword(), userBO.getSalt()));

        userRepoService.updateUserByUserId(userBO);
    }

    @Override
    public void modifyUserMajorAndClassAndGrade(CommonUserRequest request, OperateContext context) {
        userBasicService.modifyMajorAndClassAndGrade(request.getUserId(), request.getUserInfoBO().getMajor(), request.getUserInfoBO().getClassId(), request.getUserInfoBO().getGrade());
    }

    @Override
    public UserBO queryByUserId(String userId, OperateContext context) {
      return userRepoService.queryByUserId(userId);
    }

    @Override
    public CommonUser queryCommonByUserId(String userId, OperateContext context) {
        CommonUser commonUser = userBasicService.getByUserId(userId);
        commonUser.setAvatarUrl(queryByUserId(userId,context).getAvatarUrl());
       return commonUser;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @VerifyPerm(permType = GeneralPermType.PERM_OPERATOR)
    public List<String> saveUserByExcel(UploadUserExcelRequest request, MultipartFile file, OperateContext context) {
        int index=file.getOriginalFilename().indexOf(".");
        String fileType=file.getOriginalFilename().substring(index+1);
        Workbook workbook;
        List<String> titles=new ArrayList<>();
        try {
            if(fileType.equalsIgnoreCase("xls")){
                workbook=new HSSFWorkbook(file.getInputStream());
            }else if(fileType.equalsIgnoreCase("xlsx")){
                workbook=new XSSFWorkbook(file.getInputStream());
            }else {
                throw new BetahouseException(CommonResultCode.SYSTEM_ERROR,"??????????????????");
            }
        } catch (IOException e) {
            throw new BetahouseException(CommonResultCode.SYSTEM_ERROR,"??????????????????");
        }
        List<ExcelTemplate> excelTemplates = ExcelUtil.parseExcel(workbook);

        for (ExcelTemplate excelTemplate : excelTemplates) {
            Map<String, List<String>> data = excelTemplate.getDate();
            for (int i = 0; i < excelTemplate.getRowNum(); i++) {
                List<String> list;
                CommonUserRequest commonUserRequest=new CommonUserRequest();
                UserInfoBO userInfoBO=new UserInfoBO();
                if(data.get("??????")!=null){
                    list=data.get("??????");
                    userInfoBO.setRealName(list.get(i));
                }
                if(data.get("??????")!=null){
                    list=data.get("??????");
                    userInfoBO.setStuId(list.get(i));
                    commonUserRequest.setStuId(list.get(i));
                    commonUserRequest.setUsername(list.get(i));
                }
                if(data.get("????????????")!=null){
                    list=data.get("????????????");
                    commonUserRequest.setPassword(list.get(i));
                }
                if(data.get("??????")!=null){
                    list=data.get("??????");
                    userInfoBO.setSex(list.get(i));
                }
                if(data.get("??????")!=null){
                    list=data.get("??????");
                    userInfoBO.setMajor(list.get(i));
                }
                if(data.get("??????")!=null){
                    list=data.get("??????");
                    userInfoBO.setGrade(list.get(i));
                }
                if(data.get("??????")!=null){
                    list=data.get("??????");
                    userInfoBO.setClassId(list.get(i));
                }
                commonUserRequest.setSalt(UUID.randomUUID().toString());
                userInfoBO.setEnrollDate(new Date());
                commonUserRequest.setUserInfoBO(userInfoBO);
                userManager.create(commonUserRequest);
            }
            if(StringUtils.isNotBlank(excelTemplate.getTitle())){
                titles.add(excelTemplate.getTitle());
            }
        }
        return titles;
    }

    /**
     * ???????????????
     * @param userId
     * @return
     */
    @Override
    public List<UserRoutingTable> getRoutingTable(String userId) {
        //?????????
        List<UserRoutingTable> userRoutingTable=new ArrayList<>();
        Map<String, PermBO> permBOMap = userBasicService.fetchUserPerms(userId);
        List<String> permTypes = CollectionUtils.toStream(permBOMap.values()).filter(Objects::nonNull).map(PermBO::getPermType).collect(Collectors.toList());

        boolean isManager=userBasicService.verifyPermissionByRoleCode(userId,Collections.singletonList(UserRoleCode.GENERAL_MANAGER));

        //????????????????????????
        List<UserRoutingTable> activityListA=new ArrayList<>();

        //???????????????
        if(userBasicService.verifyPermissionByRoleCode(userId,Collections.singletonList(UserRoleCode.ACTIVITY_MANAGER))){

            UserRoutingTable inquiry=new UserRoutingTable("/inquiry","?????????????????????","inquiry",false,null);
            activityListA.add(inquiry);

            UserRoutingTable youthLearn=new UserRoutingTable("/youthquery","?????????????????????","youthquery",false,null);
            activityListA.add(youthLearn);

            //??????????????????
            UserRoutingTable activity=new UserRoutingTable("/activity","????????????",null,true,activityListA);
            if(!isManager){
                userRoutingTable.add(activity);
            }
            if(permTypes.contains(ActivityPermType.STAMPER_MANAGE)){
                List<UserRoutingTable> chapterList=new ArrayList<>();
                UserRoutingTable importTable=new UserRoutingTable("/import","?????????","importChapter",false,null);
                chapterList.add(importTable);
                //?????????????????????
                UserRoutingTable chapter=new UserRoutingTable("/chapter","???????????????",null,true,chapterList);
                UserRoutingTable youth=new UserRoutingTable("/youthLearn","?????????????????????","importYouthLearn",false,null);
                if(!isManager){
                    chapterList.add(youth);
                    userRoutingTable.add(chapter);
                }
            }
        }

        //?????????
        if(isManager){
            //?????????????????????
            UserRoutingTable overview=new UserRoutingTable("/overview","???????????????","overview",false,null);
            userRoutingTable.add(overview);

            List<UserRoutingTable> activityList=new ArrayList<>();
            //??????????????????????????????
            if(userBasicService.verifyPermissionByRoleCode(userId,Collections.singletonList(UserRoleCode.ACTIVITY_MANAGER))){
                activityList.addAll(activityListA);
            }
            UserRoutingTable approval=new UserRoutingTable("/approval","????????????","approval",false,null);
            activityList.add(approval);
            UserRoutingTable approveDetail=new UserRoutingTable("/approvedetail","??????????????????","approvedetail",false,null);
            activityList.add(approveDetail);
            UserRoutingTable authority=new UserRoutingTable("/authority","????????????","authority",false,null);
            activityList.add(authority);
            //??????????????????
            UserRoutingTable activity=new UserRoutingTable("/activity","????????????",null,true,activityList);
            userRoutingTable.add(activity);

            List<UserRoutingTable> chapterList=new ArrayList<>();
            UserRoutingTable manage=new UserRoutingTable("/manage","??????/?????????","manageChapter",false,null);
            chapterList.add(manage);
            UserRoutingTable youth=new UserRoutingTable("/youthLearn","?????????????????????","importYouthLearn",false,null);
            chapterList.add(youth);
            //?????????????????????
            UserRoutingTable chapter=new UserRoutingTable("/chapter","???????????????",null,true,chapterList);
            userRoutingTable.add(chapter);

            //????????? <- ????????????
            List<UserRoutingTable> officeList=new ArrayList<>();
            UserRoutingTable infoEntry=new UserRoutingTable("/infoentry","??????????????????","infoEntry",false,null);
            officeList.add(infoEntry);
            //??????????????????
            UserRoutingTable office=new UserRoutingTable("/office","????????????",null,true,officeList);
            userRoutingTable.add(office);
        }
        return userRoutingTable;
    }

    @Override
    public CommonUser loginProxy(CommonUserRequest request, OperateContext context){
        CommonUser commonUser = login(request, context);
        List<RoleCode> list=new ArrayList(2);
        list.add(UserRoleCode.ACTIVITY_MANAGER);
        list.add(UserRoleCode.GENERAL_MANAGER);
        boolean b = userBasicService.verifyPermissionByRoleCode(commonUser.getUserId(), list);
        AssertUtil.assertTrue(b,CommonResultCode.FORBIDDEN,"??????????????????????????????");
        return commonUser;
    }

    @Override
//    @VerifyPerm(permType = {GeneralPermType.PERM_OPERATOR})
    @VerifyRole(roleCodes = {UserRoleCode.GENERAL_MANAGER})
    public void giveStamperPerm(CommonUserRequest request, OperateContext context) {
        AssertUtil.assertTrue(!permRepoService.verifyRolePermRelationByPermType(request.getOperateId(),Collections.singletonList(ActivityPermType.STAMP_IMPORTER)),"??????????????????????????????????????????");
        PermBO permBO = permRepoService.queryPermByPermType(ActivityPermType.STAMP_IMPORTER);
        AssertUtil.assertNotNull(permBO,CommonResultCode.ILLEGAL_PARAMETERS.getCode(),"?????????????????????????????????????????????");
        UserManageRequest userManageRequest=new UserManageRequest();
        userManageRequest.setUserId(request.getOperateId());
        userManageRequest.setPermIds(Collections.singletonList(permBO.getPermId()));
        userManager.batchBindPerm(userManageRequest);
    }

    @Override
    public CommonUser findByStuid(String stuid) {
        return userBasicService.getByStuId(stuid);
    }

    @Override
//    @VerifyPerm(permType = {GeneralPermType.PERM_OPERATOR})
    @VerifyRole(roleCodes = {UserRoleCode.GENERAL_MANAGER})
    public void unBindStamperPerm(CommonUserRequest request, OperateContext context) {
        AssertUtil.assertTrue(!userBasicService.verifyPermissionByRoleCode(request.getOperateId(),Collections.singletonList(UserRoleCode.GENERAL_MANAGER)),"?????????????????????????????????");
        AssertUtil.assertTrue(!permRepoService.verifyRolePermRelationByPermType(request.getOperateId(),Collections.singletonList(ActivityPermType.STAMP_IMPORTER)),"???????????????????????????,????????????");
        PermBO permBO = permRepoService.queryPermByPermType(ActivityPermTypeEnum.STAMP_IMPORTER.getCode());
        AssertUtil.assertNotNull(permBO,CommonResultCode.SYSTEM_ERROR.getCode(),"?????????????????????????????????????????????");
        permRepoService.userUnbindPerms(request.getOperateId(),Collections.singletonList(permBO.getPermId()));
    }

}
