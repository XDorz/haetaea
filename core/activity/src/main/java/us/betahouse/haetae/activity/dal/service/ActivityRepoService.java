/*
  betahouse.us
  CopyRight (c) 2012 - 2018
 */
package us.betahouse.haetae.activity.dal.service;

import cn.hutool.core.date.DateTime;
import org.springframework.data.jpa.repository.Query;
import us.betahouse.haetae.activity.dal.model.ActivityDO;
import us.betahouse.haetae.activity.model.basic.ActivityBO;
import us.betahouse.haetae.activity.model.basic.ActivityNowLocationBO;
import us.betahouse.haetae.activity.model.basic.PastActivityBO;
import us.betahouse.haetae.activity.model.common.PageList;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * 活动仓储服务
 *
 * @author MessiahJK
 * @version : ActivityRepoService.java 2018/11/17 20:00 MessiahJK
 */
public interface ActivityRepoService {
    /**
     * 查询所有活动
     *
     * @return
     */
    List<ActivityBO> queryAllActivity();

    /**
     * 查询所有活动举办单位
     *
     * @return
     */
    List<String> queryAllOrganization();

    /**
     * 通过状态查询活动
     *
     * @param state 狀態
     * @return
     */
    List<ActivityBO> queryActivitiesByState(String state);

    /**
     * 查询最近十个活动
     *
     * @return
     */
    List<ActivityBO> findFirst10OrOrderByStart();
    /**
     * 通过类型查询活动
     *
     * @param type 类型
     * @return
     */
    List<ActivityBO> queryActivityByType(String type);

    /**
     * 新增活动
     *
     * @param activityBO
     * @return
     */
    ActivityBO createActivity(ActivityBO activityBO);



    /**
     * 通过活动id查询活动
     *
     * @param activityId
     * @return
     */
    ActivityBO queryActivityByActivityId(String activityId);

    /**
     * 通过活动ids查询活动
     *
     * @param activityIds
     * @return
     */
    List<ActivityBO> queryActivityByActivityIds(List<String> activityIds);

    /**
     * 通过活动名查询活动
     *
     * @param name
     * @return
     */
    ActivityBO queryActivityByActivityName(String name);

    /**
     * 通过学期、状态、类型分页查询 分页
     *
     * @param term 学期
     * @param status 状态
     * @param type 类型
     * @param page 页面
     * @param limit 每页行数
     * @return PageList<ActivityBO>
     */
    PageList<ActivityBO> queryActivityByTermAndStateAndTypePagerDESC(String term,String status,String type,Integer page,Integer limit);


    /**
     * 通过学期、状态、类型分页查询 分页
     *
     * @param term 学期
     * @param status 状态
     * @param type 类型
     * @param page 页面
     * @param limit 每页行数
     * @return PageList<ActivityBO>
     */
    PageList<ActivityBO> queryActivityByTermAndStateAndTypePagerASC(String term,String status,String type,Integer page,Integer limit);


    /**
     * 通过用户id获取以往活动记录
     *
     * @param userId
     * @return
     */
    PastActivityBO getPastByUserId(String userId);

    /**
     * 通过学号获取以往活动记录
     *
     * @param stuId
     * @return
     */
    PastActivityBO getPastByStuId(String stuId);

    /**
     * 更新过去活动记录
     *
     * @param userId
     * @param pastActivityBO
     * @return
     */
    PastActivityBO updatePastActivity(String userId,PastActivityBO pastActivityBO);


    /**
     * 创建过去活动记录
     *
     * @param pastActivityBO
     * @return
     */
    PastActivityBO createPastActivity(PastActivityBO pastActivityBO);

    /**
     * 通过用户Id分页查询 分页
     *
     * @param userId
     * @return PageList<ActivityBO>
     */
    PageList<ActivityBO> queryActivityByUserId(String userId,Integer page,Integer limit);

    /**
     * 查询已通过审批分页查询 分页
     *
     * @param state 状态
     * @param stuId 学号
     * @param activityName 活动名
     * @param organizationMessage 组织单位
     * @param page 页面
     * @param limit 每页行数
     * @return PageList<ActivityBO>
     */
    PageList<ActivityBO> queryApproved(String state,String stuId,String activityName,String organizationMessage,
                                       Integer page,Integer limit);

    /**
     * 查询已通过审批（添加时间）分页查询 分页
     *
     * @param state 状态
     * @param stuId 学号
     * @param activityName 活动名
     * @param organizationMessage 组织单位
     * @param page 页面
     * @param limit 每页行数
     * @return PageList<ActivityBO>
     */
    PageList<ActivityBO> queryApprovedAddTime(String state,String stuId,String activityName,String organizationMessage
            ,Long activityStampedStart,Long activityStampedEnd,Integer page,Integer limit) throws ParseException;

    /**
     *根据activityId修改活动扫章时间
     * @param activityStampedStart
     * @param activityStampedEnd
     * @param activityId
     */
    void updateActivityStampedTimeByActivityId(Date activityStampedStart,Date activityStampedEnd,String activityId);

    /**
     * List<ActivityBO> 2 List<ActivityDO>
     * @param activityDOs
     * @return
     */
    List<ActivityBO> convert(List<ActivityDO> activityDOs);

    /**
     * 查询已通过审批的活动 分页
     * @param stuId
     * @param activityName
     * @param organizationMessage
     * @param start
     * @param end
     * @param page
     * @param limit
     * @return
     * @throws ParseException
     */
    PageList<ActivityBO> queryApprovedBy(String stuId,String activityName,String organizationMessage
            ,Long start,Long end,Integer page,Integer limit) throws ParseException;

    /**
     * 查询未通过审批的活动 分页
     * @param stuId
     * @param activityName
     * @param organizationMessage
     * @param start
     * @param end
     * @param page
     * @param limit
     * @return
     * @throws ParseException
     */
    PageList<ActivityBO> findCanceledBy(String stuId,String activityName,String organizationMessage
            ,Long start,Long end,Integer page,Integer limit) throws ParseException;

    /**
     * 查找本周创建的活动
     * @param page
     * @param limit
     * @param activityName
     * @return
     */
    PageList<ActivityBO> findCreatedThisWeek(Integer page, Integer limit,String activityName);


    /**
     * 查找本周审批通过的活动
     * @param page
     * @param limit
     * @param activityName
     * @return
     */
    PageList<ActivityBO> findApprovedThisWeek(Integer page, Integer limit,String activityName);


    /**
     * 活动审批通过
     *
     * @param activityBO
     * @return
     */
    ActivityBO publishActivity(ActivityBO activityBO);
    /**
     * 活动申请驳回
     *
     * @param activityBO
     * @return
     */
    ActivityBO cancelActivity(ActivityBO activityBO);

    /**
     * 更新活动
     *
     * @param activityBO
     * @return
     */
    ActivityBO updateActivity(ActivityBO activityBO);

    /**
     * 通过活动负责人Id查询已审批通过的活动 分页
     *
     * @param userId
     * @return PageList<ActivityBO>
     */
    PageList<ActivityBO> queryApprovedActivityByUserId(String userId,Integer page,Integer limit);

    /**
     * 通过活动负责人Id查询未审批通过的活动 分页
     *
     * @param userId
     * @return PageList<ActivityBO>
     */
    PageList<ActivityBO> queryCanceledActivityByUserId(String userId,Integer page,Integer limit);

    /**
     * 查找本周创建的活动 不分页
     * @param activityName
     * @return
     */
    List<ActivityBO> findCreatedThisWeekNotPage(String activityName);

    /**
     * 根据单位信息查询过去一个月内所有发起了报名的活动的实际参与的人数
     * @param organizationMessage
     * @return
     */
    Integer queryActualNumPastMonthByOrganizationMessage(String organizationMessage);

    /**
     * 根据单位信息查询过去一个月内所有发起了报名的活动的报名总人数
     * @param organizationMessage
     * @return
     */
    Integer querySignNumPastMonthByOrganizationMessage(String organizationMessage);

    /**
     * 查找本学期的讲座活动数量
     * @param term
     * @return
     */
    Integer findLectureActivityNum(String term);

    /**
     * 查找本学期的校园活动数量
     * @param term
     * @return
     */
    Integer findSchoolActivityNum(String term);

    /**
     * 查找本学期的总活动数量
     * @param term
     * @return
     */
    Integer findAllActivityNum(String term);

    /**
     * 查询活动名称
     *
     * @return
     */
    List<String> findActivityName();

    /**
     * 查询活动时间
     *
     * @return
     */
    List<Date> findActivityTime();

    /**
     * 查询活动地点
     *
     * @return
     */
    List<String> findActivityLocation();

    /**
     * 查询活动类型
     *
     * @return
     */
    List<String> findActivityType();
}
