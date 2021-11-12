package us.betahouse.haetae.user.dal.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import us.betahouse.haetae.serviceimpl.user.enums.UserRoleCode;
import us.betahouse.haetae.user.model.basic.UserInfoBO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RoleRepoServiceTest {

    @Autowired
    private RoleRepoService roleRepoService;
    @Autowired
    private UserInfoRepoService userInfoRepoService;


    @Test
    public void addCertificateManager(){
        UserInfoBO userInfoBO = userInfoRepoService.queryUserInfoByStuId("17909227");
        if(null==userInfoBO){
            System.out.println("账号不存在");
            return ;
        }
        else{
            roleRepoService.userBindRolesByCode(userInfoBO.getUserId(), UserRoleCode.LOCALE_MEMBER);
            System.out.println("绑定成功");
        }
    }


   @Test
    public void queryRolesByRoleIds() {
    }

    @Test
    public void queryRolesByUserId() {
    }

    @Test
    public void userBindRoles() {
        List list=new ArrayList();
        list.add("202110030319567600554800021003");
        list.add("201811302151309605429200021130");
        roleRepoService.userBindRoles("201811302141073197820001201810", list);

    }

    @Test
    public void userBindRolesByCode() {

    }

    @Test
    public void userUnbindRoles() {
    }

    @Test
    public void usersBindRole() {
    }

    @Test
    public void usersUnbindRole() {
    }

    @Test
    public void detachAllUser() {
    }

    @Test
    public void initRole() {
    }
}