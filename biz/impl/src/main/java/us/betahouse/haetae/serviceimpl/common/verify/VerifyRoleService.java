package us.betahouse.haetae.serviceimpl.common.verify;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.betahouse.haetae.serviceimpl.user.enums.UserRoleCode;
import us.betahouse.haetae.user.enums.RoleCode;
import us.betahouse.haetae.user.user.service.UserBasicService;
import us.betahouse.util.enums.CommonResultCode;
import us.betahouse.util.exceptions.BetahouseException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Aspect
@Component
public class VerifyRoleService {
    final Logger LOGGER= LoggerFactory.getLogger(VerifyRoleService.class);

    @Autowired
    UserBasicService userBasicService;

    @Pointcut("execution(* us.betahouse.haetae.serviceimpl..*(..)) && @annotation(us.betahouse.haetae.serviceimpl.common.verify.VerifyRole)")
    public void targ(){

    }

    @Before("targ() && @annotation(verifyRole)")
    public void verify(JoinPoint joinPoint,VerifyRole verifyRole){
        Object[] args = joinPoint.getArgs();
        VerifyRequest request=null;
        for (Object arg : args) {
            if(arg instanceof VerifyRequest){
                request=(VerifyRequest) arg;
            }
        }
        if(request==null){
            LOGGER.info("方法传参错误，没有鉴权对象");
            throw new BetahouseException(CommonResultCode.ILLEGAL_PARAMETERS,"方法传参错误，没有鉴权对象");
        }
        if(request.getVerifyUserId()==null||request.getVerifyUserId().length()==0){
            LOGGER.warn("请求未带用户id参数，请留意");
            throw new BetahouseException(CommonResultCode.ILLEGAL_PARAMETERS,"请求未带用户id参数，请留意");
        }
        List<RoleCode> roleCode;
        if(verifyRole.roleCodes().length==0){
            LOGGER.info("参数错误，请填写rolecode参数");
            throw new BetahouseException(CommonResultCode.ILLEGAL_PARAMETERS,"参数错误，请填写rolecode参数");
        }else {
            roleCode=Arrays.asList(verifyRole.roleCodes());
        }
        boolean flag=userBasicService.verifyPermissionByRoleCode(request.getVerifyUserId(),roleCode);
        if(!flag){
            LOGGER.warn(MessageFormat.format("存在越权操作，越权用户id:{0}",request.getVerifyUserId()));
            throw new BetahouseException(CommonResultCode.FORBIDDEN,MessageFormat.format("存在越权操作，越权用户id{0}",request.getVerifyUserId()));
        }
    }
}
