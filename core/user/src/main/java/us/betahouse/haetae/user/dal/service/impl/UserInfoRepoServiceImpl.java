/*
  betahouse.us
  CopyRight (c) 2012 - 2018
 */
package us.betahouse.haetae.user.dal.service.impl;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import us.betahouse.haetae.user.dal.convert.EntityConverter;
import us.betahouse.haetae.user.dal.model.UserInfoDO;
import us.betahouse.haetae.user.dal.model.perm.UserDO;
import us.betahouse.haetae.user.dal.repo.UserInfoDORepo;
import us.betahouse.haetae.user.dal.repo.perm.UserDORepo;
import us.betahouse.haetae.user.dal.service.UserInfoRepoService;
import us.betahouse.haetae.user.idfactory.BizIdFactory;
import us.betahouse.haetae.user.model.basic.UserInfoBO;
import us.betahouse.haetae.user.model.common.PageList;
import us.betahouse.util.utils.AssertUtil;
import us.betahouse.util.utils.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户信息仓储服务实现
 *
 * @author dango.yxm
 * @version : UserInfoRepoServiceImpl.java 2018/11/17 下午8:37 dango.yxm
 */
@Service
public class UserInfoRepoServiceImpl implements UserInfoRepoService {

    private final Logger LOGGER = LoggerFactory.getLogger(UserInfoRepoServiceImpl.class);

    @Autowired
    private UserInfoDORepo userInfoDORepo;

    @Autowired
    private UserDORepo userDORepo;

    /**
     * id工厂
     */
    @Autowired
    private BizIdFactory userBizIdFactory;

    @Override
    public void bindUserInfo(String userId, UserInfoBO userInfoBO) {
        // 获取用户
        UserDO userDO = userDORepo.findByUserId(userId);
        AssertUtil.assertNotNull(userDO, "用户不存在");
        userInfoBO.setUserId(userId);

        // 获取用户信息
        UserInfoDO userInfoDO = userInfoDORepo.findByUserId(userId);
        AssertUtil.assertNull(userInfoDO, "用户已经绑定过用户信息，只能修改");

        //填充用户信息id
        if (StringUtils.isBlank(userInfoBO.getUserInfoId())) {
            userInfoBO.setUserInfoId(userBizIdFactory.getUserInfoId(userId));
        }
        userInfoDORepo.save(EntityConverter.convert(userInfoBO));
    }

    @Override
    public UserInfoBO queryUserInfoByUserId(String userId) {
        return EntityConverter.convert(userInfoDORepo.findByUserId(userId));
    }

    @Override
    public UserInfoBO queryUserInfoByStuId(String stuId) {
        return EntityConverter.convert(userInfoDORepo.findByStuId(stuId));
    }

    @Override
    public UserInfoBO modifyUserInfoByUserId(String userId, UserInfoBO userInfoBO) {
        UserInfoDO userInfoDO = userInfoDORepo.findByUserId(userId);
        AssertUtil.assertNotNull(userInfoDO, "修改的用户信息不存在");

        UserInfoDO newUserInfoDO = EntityConverter.convert(userInfoBO);
        if (newUserInfoDO.getClassId() != null) {
            userInfoDO.setClassId(newUserInfoDO.getClassId());
        }
        if (newUserInfoDO.getMajorId() != null) {
            userInfoDO.setMajorId(newUserInfoDO.getMajorId());
        }
        if (newUserInfoDO.getRealName() != null) {
            userInfoDO.setRealName(newUserInfoDO.getRealName());
        }
        if (newUserInfoDO.getEnrollDate() != null) {
            userInfoDO.setEnrollDate(newUserInfoDO.getEnrollDate());
        }
        if (newUserInfoDO.getSex() != null) {
            userInfoDO.setSex(newUserInfoDO.getSex());
        }
        if (newUserInfoDO.getStuId() != null) {
            userInfoDO.setStuId(newUserInfoDO.getStuId());
        }
        if (newUserInfoDO.getGrade() != null) {
            userInfoDO.setGrade(newUserInfoDO.getGrade());
        }
        // 特殊逻辑 拓展信息为null 时认为是主动清空
        if (userInfoBO.getExtInfo() == null) {
            userInfoDO.setExtInfo(JSON.toJSONString(new HashMap<>()));
        } else if (!userInfoBO.getExtInfo().isEmpty()) {
            // 只有当拓展信息不为空时才会更新
            userInfoDO.setExtInfo(newUserInfoDO.getExtInfo());
        }
        return EntityConverter.convert(userInfoDORepo.save(userInfoDO));
    }

    @Override
    public List<UserInfoBO> batchQueryByUserIds(List<String> userIds) {
        List<UserInfoBO> userInfoBOS = new ArrayList<>();
        List<UserDO> userDOS = userDORepo.findAllByUserIdIn(userIds);
        List<UserInfoDO> userInfoDOS = userInfoDORepo.findAllByUserIdIn(userIds);
        for (int i = 0;i < userInfoDOS.size();i ++ ) {
            UserInfoBO userInfoBO = EntityConverter.convert(userInfoDOS.get(i));
            userInfoBO.setAvatarUrl(userDOS.get(i).getAvatarUrl());
            userInfoBOS.add(userInfoBO);
        }
        return userInfoBOS;
    }

    @Override
    public List<UserInfoBO> queryAllUser() {
        return CollectionUtils.toStream(userInfoDORepo.findAll())
                .filter(Objects::nonNull)
                .map(EntityConverter::convert)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserInfoBO> queryAllMajorAndGrade() {
        List<UserInfoBO> userInfoBOS = CollectionUtils.toStream(userInfoDORepo.findAll())
                .filter(Objects::nonNull)
                .map(EntityConverter::majorConvert)
                .collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(o -> o.getGrade() + ";" + o.getMajor()))), ArrayList::new));
        return userInfoBOS;
    }

    @Override
    public void updateUndergraduateState() {
        userInfoDORepo.update21600();
        userInfoDORepo.update16000();
        userInfoDORepo.update0801();
        userInfoDORepo.update0810();
        userInfoDORepo.update2011();
        userInfoDORepo.update8001();
        userInfoDORepo.update8010();
        userInfoDORepo.update8800();
    }

    @Override
    public void updateCollegeUpgradeState() {
        userInfoDORepo.update411();
    }

    @Override
    public PageList<UserInfoBO> findUnQualifiedUndergraduate(Integer page, Integer limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<UserInfoDO> list = userInfoDORepo.findUnQualifiedUndergraduate(pageable);
        List<UserInfoBO> userInfoBOS = convert(list.getContent());
        for (UserInfoBO userInfoBO : userInfoBOS) {
            final String userId = userInfoBO.getUserId();
            userInfoBO.setLectureChapterNum(userInfoDORepo.getLectureStampNumByUserId(userId));
            userInfoBO.setActivityChapterNum(userInfoDORepo.getActivityStampNumByUserId(userId));
            userInfoBO.setPracticeTimes(userInfoDORepo.getPracticeTimesByUserId(userId));
            userInfoBO.setCertificatesNum(userInfoDORepo.getCertificateNumByUserId(userId));
        }
        return new PageList<>(userInfoBOS,list.getTotalElements(),list.getPageable().getPageNumber(),list.getPageable().getPageSize());
    }

    @Override
    public PageList<UserInfoBO> findUnQualifiedCollegeUpgrade(Integer page, Integer limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<UserInfoDO> list = userInfoDORepo.findUnQualifiedCollegeUpgrade(pageable);
        List<UserInfoBO> userInfoBOS = convert(list.getContent());
        for (UserInfoBO userInfoBO : userInfoBOS) {
            final String userId = userInfoBO.getUserId();
            userInfoBO.setLectureChapterNum(userInfoDORepo.getLectureStampNumByUserId(userId));
            userInfoBO.setActivityChapterNum(userInfoDORepo.getActivityStampNumByUserId(userId));
            userInfoBO.setPracticeTimes(userInfoDORepo.getPracticeTimesByUserId(userId));
            userInfoBO.setCertificatesNum(userInfoDORepo.getCertificateNumByUserId(userId));
        }
        return new PageList<>(userInfoBOS,list.getTotalElements(),list.getPageable().getPageNumber(),list.getPageable().getPageSize());
    }


    public List<UserInfoBO> convert(List<UserInfoDO> userInfoDOS){
        return CollectionUtils.toStream(userInfoDOS).filter(Objects::nonNull).map(this::convert).collect(Collectors.toList());
    }

    private UserInfoBO convert(UserInfoDO userInfoDO){
        if (userInfoDO == null){
            return null;
        }
        UserInfoBO userInfoBO = new UserInfoBO();
        userInfoBO.setUserId(userInfoDO.getUserId());
        userInfoBO.setStuId(userInfoDO.getStuId());
        userInfoBO.setRealName(userInfoDO.getRealName());
        userInfoBO.setMajor(userInfoDO.getMajorId());
        userInfoBO.setClassId(userInfoDO.getClassId());
        return userInfoBO;
    }
}
