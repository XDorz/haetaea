/*
  betahouse.us
  CopyRight (c) 2012 - 2019
 */
package us.betahouse.haetae.serviceimpl.finance.service;

import org.springframework.stereotype.Service;
import us.betahouse.haetae.finance.model.basic.FinanceMessageBO;
import us.betahouse.haetae.finance.model.basic.FinanceTotalBO;
import us.betahouse.haetae.finance.model.common.PageList;
import us.betahouse.haetae.serviceimpl.common.OperateContext;
import us.betahouse.haetae.serviceimpl.finance.request.FinanceManagerRequest;

/**
 * @author MessiahJK
 * @version : FinanceService.java 2019/02/22 0:20 MessiahJK
 */
@Service
public interface FinanceService {

    /**
     * 查询信息
     *
     * @param request
     * @param context
     * @return
     */
    PageList<FinanceMessageBO> findMessage(FinanceManagerRequest request,OperateContext context);

    /**
     * 提交预算
     *
     * @param request
     * @param context
     * @return
     */
    FinanceMessageBO submitBudget(FinanceManagerRequest request,OperateContext context);

    /**
     * 审核
     *
     * @param request
     * @param context
     * @return
     */
    FinanceMessageBO audite(FinanceManagerRequest request,OperateContext context);

    /**
     * 核算信息
     *
     * @param request
     * @param context
     * @return
     */
    FinanceMessageBO checkMessage(FinanceManagerRequest request,OperateContext context);

    /**
     * 记账
     *
     * @param request
     * @param context
     * @return
     */
    FinanceMessageBO tally(FinanceManagerRequest request,OperateContext context);

    /**
     * 获取总金额
     *
     * @param request
     * @param context
     * @return
     */
    FinanceTotalBO total(FinanceManagerRequest request,OperateContext context);
}
