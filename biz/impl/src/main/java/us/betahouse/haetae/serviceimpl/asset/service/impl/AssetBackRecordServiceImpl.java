/*
 * betahouse.us
 * CopyRight (c) 2012 - 2019
 */
package us.betahouse.haetae.serviceimpl.asset.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import us.betahouse.haetae.asset.dal.repo.AssetDORepo;
import us.betahouse.haetae.asset.manager.AssetBackRecordManager;
import us.betahouse.haetae.asset.manager.AssetLoanRecordManager;
import us.betahouse.haetae.asset.manager.AssetManager;
import us.betahouse.haetae.asset.model.basic.AssetBO;
import us.betahouse.haetae.asset.model.basic.AssetBackRecordBO;
import us.betahouse.haetae.asset.model.basic.AssetLoanRecordBO;
import us.betahouse.haetae.asset.request.AssetBackRecordRequest;
import us.betahouse.haetae.organization.dal.service.OrganizationRepoService;
import us.betahouse.haetae.serviceimpl.asset.service.AssetBackRecordService;
import us.betahouse.haetae.serviceimpl.common.OperateContext;
import us.betahouse.util.enums.RestResultCode;
import us.betahouse.util.utils.AssertUtil;

import java.util.List;

/**
 * @author yiyuk.hxy
 * @version : AssetBackRecordServiceImpl.java 2019/02/12 17:43 yiyuk.hxy
 */
@Service
public class AssetBackRecordServiceImpl implements AssetBackRecordService {
    private final Logger LOGGER = LoggerFactory.getLogger(AssetBackRecordServiceImpl.class);

    @Autowired
    private AssetManager assetManager;

    @Autowired
    private AssetLoanRecordManager assetLoanRecordManager;

    @Autowired
    private AssetBackRecordManager assetBackRecordManager;

    @Autowired
    private AssetDORepo assetDORepo;

    @Autowired
    private OrganizationRepoService organizationRepoService;

    @Override
    @Transactional
    public AssetBackRecordBO create(AssetBackRecordRequest request, OperateContext context) {
        AssetLoanRecordBO assetLoanRecordBO = assetLoanRecordManager.findAssetLoanRecordByLoanRecordId(request.getLoanRecoedId());
        AssertUtil.assertNotNull(assetLoanRecordBO, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "?????????????????????");
        AssetBO assetBO = assetManager.findAssetByAssetID(assetLoanRecordBO.getAssetId());
        AssertUtil.assertNotNull(assetBO, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "??????????????????");
        AssertUtil.assertTrue(assetLoanRecordBO.getRemain() != 0, "?????????????????????");
        AssertUtil.assertTrue(request.getAmount() <= assetLoanRecordBO.getRemain(), "???????????????????????????????????????");
        request.setAssetType(assetBO.getAssetType());
        AssetBackRecordBO assetBackRecordBO = assetBackRecordManager.create(request);
        addOrganizationName(assetBackRecordBO);
        return assetBackRecordBO;
    }

    @Override
    public List<AssetBackRecordBO> findAllAssetBackRecordByAssetId(AssetBackRecordRequest request, OperateContext context) {
        AssetBO assetBO = assetManager.findAssetByAssetID(request.getAssetId());
        AssertUtil.assertNotNull(assetBO, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "???????????????");
        List<AssetBackRecordBO> assetBackRecordBOS = assetBackRecordManager.findAllAssetBackRecordByAssetId(request.getAssetId());
        assetBackRecordBOS.forEach(this::addOrganizationName);
        return assetBackRecordBOS;
    }

    @Override
    public List<AssetBackRecordBO> findAllAssetBackRecordByUserId(AssetBackRecordRequest request, OperateContext context) {
        List<AssetBackRecordBO> assetBackRecordBOS = assetBackRecordManager.findAllAssetBackRecordByUserId(request.getUserId());
        assetBackRecordBOS.forEach(this::addOrganizationName);
        return assetBackRecordBOS;
    }

    @Override
    public List<AssetBackRecordBO> findAllAssetBackRecordByLoanRecordId(AssetBackRecordRequest request, OperateContext context) {
        List<AssetBackRecordBO> assetBackRecordBOS = assetBackRecordManager.findAllAssetBackRecordByLoanRecordId(request.getLoanRecoedId());
        AssertUtil.assertNotNull(assetBackRecordBOS, RestResultCode.ILLEGAL_PARAMETERS.getCode(), "????????????ID?????????");
        AssertUtil.assertTrue(assetBackRecordBOS.size()!=0,"?????????????????????");
        AssetBO assetBO = assetManager.findAssetByAssetID(assetBackRecordBOS.get(0).getAssetId());
        for (AssetBackRecordBO assetBackRecordBO : assetBackRecordBOS) {
            assetBackRecordBO.setAssetName(assetBO.getAssetName());
        }
        assetBackRecordBOS.forEach(this::addOrganizationName);
        return assetBackRecordBOS;
    }

    //??????????????????????????????????????????
    void addOrganizationName(AssetBackRecordBO assetBackRecordBO) {
        String assetId = assetBackRecordBO.getAssetId();
        String organizationId=assetDORepo.findByAssetId(assetId).getOrginazationId();
        String organizationName = organizationRepoService.queryByOrganizationId(organizationId).getOrganizationName();
        assetBackRecordBO.setOrganizationName(organizationName);
    }
}
