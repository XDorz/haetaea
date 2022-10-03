package us.betahouse.haetae.user.dal.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import us.betahouse.haetae.user.dal.convert.EntityConverter;
import us.betahouse.haetae.user.dal.model.UserFeedBackDO;
import us.betahouse.haetae.user.dal.repo.UserFeedBackDORepo;
import us.betahouse.haetae.user.dal.service.UserFeedBackRepoService;
import us.betahouse.haetae.user.idfactory.BizIdFactory;
import us.betahouse.haetae.user.model.basic.UserFeedBackBO;
import us.betahouse.haetae.user.model.common.PageList;
import us.betahouse.util.utils.AssertUtil;


@Service
public class UserFeedBackRepoServiceImpl implements UserFeedBackRepoService {

    @Autowired
    UserFeedBackDORepo userFeedBackDORepo;

    @Autowired
    EntityConverter converter;

    @Autowired
    BizIdFactory idFactory;


    @Override
    public PageList<UserFeedBackBO> getUserFeedBackByUserId(String userId, Pageable pageable) {
        Page<UserFeedBackDO> feedBackDOS = userFeedBackDORepo.findAllByUserId(userId, pageable);
        return new PageList<>(feedBackDOS,converter::convert,null);
    }

    @Override
    public PageList<UserFeedBackBO> getAllFeedBack(Pageable pageable) {
        Page<UserFeedBackDO> feedBackDOS = userFeedBackDORepo.findAllFeedBack(pageable);
        return new PageList<>(feedBackDOS,converter::convert,null);
    }

    @Override
    public PageList<UserFeedBackBO> getAllFeedBackByVersion(String version, Pageable pageable) {
        Page<UserFeedBackDO> feedBackDOS = userFeedBackDORepo.findAllFeedBackByVersion(version,pageable);
        return new PageList<>(feedBackDOS,converter::convert,null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(UserFeedBackDO userFeedBackDO) {
        String feedBackId=userFeedBackDO.getFeedBackId()==null?idFactory.getFeedBackId():userFeedBackDO.getFeedBackId();
        userFeedBackDO.setFeedBackId(feedBackId);
        if(userFeedBackDO.getFeedBackNext()!=null){
            String frontId = userFeedBackDO.getFeedBackNext();
            userFeedBackDORepo.updateNextIdByFeedBackId(frontId,feedBackId);
            userFeedBackDO.setFeedBackNext(null);
        }else if(userFeedBackDO.getFeedBackHead()==null){
            //是楼主无需任何处理
        }else {
            //从头开始找起，找到最后一个接上去
            UserFeedBackDO feedBackDO = userFeedBackDORepo.findAllByFeedBackId(userFeedBackDO.getFeedBackHead());
            while (feedBackDO.getFeedBackNext()!=null){
                feedBackDO=userFeedBackDORepo.findAllByFeedBackId(feedBackDO.getFeedBackNext());
                AssertUtil.assertNotNull(feedBackDO,"数据错误，未找到该有的续接回馈，请联系数据管理员反馈");
            }
            userFeedBackDORepo.updateNextIdByFeedBackId(feedBackDO.getFeedBackId(),feedBackId);
        }
        userFeedBackDORepo.save(userFeedBackDO);
    }
}
