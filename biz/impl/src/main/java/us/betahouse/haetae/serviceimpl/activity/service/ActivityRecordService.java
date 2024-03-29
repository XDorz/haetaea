/*
  betahouse.us
  CopyRight (c) 2012 - 2018
 */
package us.betahouse.haetae.serviceimpl.activity.service;

import org.springframework.web.multipart.MultipartFile;
import us.betahouse.haetae.activity.dal.model.ActivityDO;
import us.betahouse.haetae.activity.model.basic.ActivityBO;
import us.betahouse.haetae.activity.model.basic.ActivityCreditsStatisticsBO;
import us.betahouse.haetae.activity.model.basic.importModel;
import us.betahouse.haetae.serviceimpl.activity.model.ActivityRecordStatistics;
import us.betahouse.haetae.serviceimpl.activity.model.StampRecord;
import us.betahouse.haetae.serviceimpl.activity.request.ActivityStampRequest;
import us.betahouse.haetae.serviceimpl.common.OperateContext;
import us.betahouse.haetae.user.model.basic.UserInfoBO;


import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 活动记录服务
 *
 * @author MessiahJK
 * @version : ActivityRecordService.java 2018/11/22 20:31 MessiahJK
 */
public interface ActivityRecordService {

    /**
     * 批量盖章
     *
     * @param request
     * @param context
     * @return 未盖上章的id
     */
    List<String> batchStamp(ActivityStampRequest request, OperateContext context);
    List<ActivityCreditsStatisticsBO> CreditsstatisticsPutCache();
    /**
     * 批量盖章（导入json）
     *
     * @param context
     * @return
     */
    List<String> batchStampJson(importModel[] importModels,ActivityStampRequest request, OperateContext context);

    /**
     * 获取用户活动章
     *
     * @param request
     * @param context
     * @return
     */
    StampRecord getUserStamps(ActivityStampRequest request, OperateContext context);

    /**
     * 通过活动id统计活动记录条数
     *
     * @param request
     * @param context
     * @return
     */
    Long countByActivityId(ActivityStampRequest request, OperateContext context);


    /**
     * 获取盖章任务
     *
     * @param request
     * @param context
     * @return
     */
    List<ActivityBO> fetchStampMission(ActivityStampRequest request, OperateContext context);

    /**
     * 导入盖章
     *
     * @param url
     * @return 未盖上章的id
     */
    List<String> importStamp(String url);

    /**
     * 统计用户活动记录
     *
     * @param userId
     * @param term
     * @return
     */
    ActivityRecordStatistics fetchUserRecordStatistics(String userId, String term);


    /**
     * 统计用户活动记录
     *
     * @param userId
     * @return
     */
    default ActivityRecordStatistics fetchUserRecordStatistics(String userId) {
        return fetchUserRecordStatistics(userId, null);
    }

    /**
     * （excel）批量导出活动章名单
     *
     * @param request
     * @param context
     * @return
     */
    List<String> exportExcel(ActivityStampRequest request, HttpServletResponse response, OperateContext context) throws IOException;


    /**
     * （excel）批量导入活动章
     *
     * @param file
     * @param request
     * @param context
     * @return
     */
    List<String> importExcel(MultipartFile file, ActivityStampRequest request, OperateContext context);

    /**
     * 填写excel数据
     *
     * @param userInfoBOList 用户信息
     * @return
     */
    List<Map<String, Object>> createExcelRecord(List<UserInfoBO> userInfoBOList);

    /**
     * 获取学分
     *
     * @param userid
     * @return
     */
    Integer getCreditByUserId(String userId);

    /**
     * 统计各个年级专业的学分
     *
     *
     * @return List<ActivityCreditsStatisticsBO>
     */
    public List<ActivityCreditsStatisticsBO> Creditsstatistics();
}
