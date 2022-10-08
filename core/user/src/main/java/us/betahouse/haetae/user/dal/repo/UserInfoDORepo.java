/*
  betahouse.us
  CopyRight (c) 2012 - 2018
 */
package us.betahouse.haetae.user.dal.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import us.betahouse.haetae.user.dal.model.UserInfoDO;
import us.betahouse.haetae.user.model.basic.UserInfoBO;

import java.util.List;

/**
 * 用户信息仓储
 *
 * @author dango.yxm
 * @version : UserInfoDORepo.java 2018/11/16 下午7:15 dango.yxm
 */
@Repository
public interface UserInfoDORepo extends JpaRepository<UserInfoDO, Long> {

    /**
     * 获取用户信息
     *
     * @param userId
     * @return
     */
    UserInfoDO findByUserId(String userId);

    /**
     * 根据班级和姓名查找
     *
     * @param realName
     * @param classId
     * @return
     */
    List<UserInfoDO> findAllByRealNameAndClassId(String realName,String classId);

    /**
     * 通过过学号获取用户信息实体
     *
     * @param stuId
     * @return
     */
    UserInfoDO findByStuId(String stuId);

    /**
     * 通过过班级号获取用户信息实体
     *
     * @param
     * @return
     */
    List<UserInfoDO> findAllByClassId(String classId);

    /**
     * 批量获取用户信息
     *
     * @param userIds
     * @return
     */
    List<UserInfoDO> findAllByUserIdIn(List<String> userIds);

    /**
     * 普通本科的8种更新
     */
    //活动章-讲座章-实践-证书
    @Transactional
    @Modifying
    @Query(value = "update common_user_info set qualified='1' where user_id in ( select user_id from (select user_id,count(type) as total from activity_record where user_id in (select user_id from common_user_info where grade='2018' and major_id not like '%专升本%' and major_id !='教职工' and qualified !='1') and type in ('schoolActivity') group by user_id) temp where temp.total>=2) and user_id in (select user_id from(select user_id,count(type) as total from activity_record where user_id in (select user_id from common_user_info where grade='2018' and major_id not like '%专升本%' and major_id !='教职工' and qualified !='1')and type in ('lectureActivity')group by user_id) temp where temp.total>=16)",nativeQuery = true)
    void update21600();

    @Transactional
    @Modifying
    @Query(value = "update common_user_info\n" +
            "set  qualified='1'\n" +
            "where user_id in (\n" +
            "    select user_id\n" +
            "    from\n" +
            "        (\n" +
            "            select user_id,count(type) as total\n" +
            "            from activity_record\n" +
            "            where user_id in (\n" +
            "                select user_id\n" +
            "                from common_user_info\n" +
            "                where grade='2018' and major_id not like '%专升本%' and major_id !='教职工' and qualified !='1'\n" +
            "            )\n" +
            "              and type in ('schoolActivity')\n" +
            "            group by user_id\n" +
            "        ) temp\n" +
            "    where temp.total>=16\n" +
            ")",nativeQuery = true)
    void update16000();

    @Transactional
    @Modifying
    @Query(value = "update common_user_info\n" +
            "set  qualified='1'\n" +
            "where user_id in (\n" +
            "    select user_id\n" +
            "    from\n" +
            "        (\n" +
            "            select user_id,count(type) as total\n" +
            "            from activity_record\n" +
            "            where user_id in (\n" +
            "                select user_id\n" +
            "                from common_user_info\n" +
            "                where grade='2018' and major_id not like '%专升本%' and major_id !='教职工' and qualified !='1'\n" +
            "            )\n" +
            "              and type in ('lectureActivity')\n" +
            "            group by user_id\n" +
            "        ) temp\n" +
            "    where temp.total>=8\n" +
            ") and user_id in (\n" +
            "    select user_id\n" +
            "    from\n" +
            "        (\n" +
            "            select user_id,count(*) as total\n" +
            "            from certificate_qualifications_record\n" +
            "            where user_id in (\n" +
            "                select user_id\n" +
            "                from common_user_info\n" +
            "                where grade='2018' and major_id not like '%专升本%' and major_id !='教职工' and qualified !='1'\n" +
            "            ) and status = 'APPROVED' and certificate_name not in ('英语四六级证书','浙江省高等学校计算机等级考试证书','浙江省高等学校计算机等级考试')\n" +
            "            group by user_id\n" +
            "        ) temp\n" +
            "    where temp.total>=1\n" +
            ")\n" +
            "\n",nativeQuery = true)
    void update0801();

    @Transactional
    @Modifying
    @Query(value = "update common_user_info\n" +
            "set  qualified='1'\n" +
            "where user_id in (\n" +
            "    select user_id\n" +
            "    from\n" +
            "        (\n" +
            "            select user_id,count(type) as total\n" +
            "            from activity_record\n" +
            "            where user_id in (\n" +
            "                select user_id\n" +
            "                from common_user_info\n" +
            "                where grade='2018' and major_id not like '%专升本%' and major_id !='教职工' and qualified !='1'\n" +
            "            )\n" +
            "              and type in ('lectureActivity')\n" +
            "            group by user_id\n" +
            "        ) temp\n" +
            "    where temp.total>=8\n" +
            ") and user_id in (\n" +
            "    select user_id\n" +
            "    from\n" +
            "        (\n" +
            "            select user_id,count(type) as total\n" +
            "            from activity_record\n" +
            "            where user_id in (\n" +
            "                select user_id\n" +
            "                from common_user_info\n" +
            "                where grade='2018' and major_id not like '%专升本%' and major_id !='教职工' and qualified = '1'\n" +
            "            )\n" +
            "              and type in ('practiceActivity')\n" +
            "            group by user_id\n" +
            "        ) temp\n" +
            "    where temp.total>=1\n" +
            ")\n" +
            "\n",nativeQuery = true)
    void update0810();

    @Transactional
    @Modifying
    @Query(value = "update common_user_info\n" +
            "set  qualified='1'\n" +
            "where user_id in (\n" +
            "    select user_id\n" +
            "    from\n" +
            "        (\n" +
            "            select user_id,count(type) as total\n" +
            "            from activity_record\n" +
            "            where user_id in (\n" +
            "                select user_id\n" +
            "                from common_user_info\n" +
            "                where grade='2018' and major_id not like '%专升本%' and major_id !='教职工' and qualified !='1'\n" +
            "            )\n" +
            "              and type in ('schoolActivity')\n" +
            "            group by user_id\n" +
            "        ) temp\n" +
            "    where temp.total>=2\n" +
            ") and user_id in (\n" +
            "    select user_id\n" +
            "    from\n" +
            "        (\n" +
            "            select user_id,count(type) as total\n" +
            "            from activity_record\n" +
            "            where user_id in (\n" +
            "                select user_id\n" +
            "                from common_user_info\n" +
            "                where grade='2018' and major_id not like '%专升本%' and major_id !='教职工' and qualified = '1'\n" +
            "            )\n" +
            "              and type in ('practiceActivity')\n" +
            "            group by user_id\n" +
            "        ) temp\n" +
            "    where temp.total>=1\n" +
            ") and user_id in (\n" +
            "    select user_id\n" +
            "    from\n" +
            "        (\n" +
            "            select user_id,count(*) as total\n" +
            "            from certificate_qualifications_record\n" +
            "            where user_id in (\n" +
            "                select user_id\n" +
            "                from common_user_info\n" +
            "                where grade='2018' and major_id not like '%专升本%' and major_id !='教职工' and qualified !='1'\n" +
            "            ) and status = 'APPROVED' and certificate_name not in ('英语四六级证书','浙江省高等学校计算机等级考试证书','浙江省高等学校计算机等级考试')\n" +
            "            group by user_id\n" +
            "        ) temp\n" +
            "    where temp.total>=1\n" +
            ")\n" +
            "\n",nativeQuery = true)
    void update2011();

    @Transactional
    @Modifying
    @Query(value = "update common_user_info\n" +
            "set  qualified='1'\n" +
            "where user_id in (\n" +
            "    select user_id\n" +
            "    from\n" +
            "        (\n" +
            "            select user_id,count(type) as total\n" +
            "            from activity_record\n" +
            "            where user_id in (\n" +
            "                select user_id\n" +
            "                from common_user_info\n" +
            "                where grade='2018' and major_id not like '%专升本%' and major_id !='教职工' and qualified !='1'\n" +
            "            )\n" +
            "              and type in ('schoolActivity')\n" +
            "            group by user_id\n" +
            "        ) temp\n" +
            "    where temp.total>=8\n" +
            ") and user_id in (\n" +
            "    select user_id\n" +
            "    from\n" +
            "        (\n" +
            "            select user_id,count(*) as total\n" +
            "            from certificate_qualifications_record\n" +
            "            where user_id in (\n" +
            "                select user_id\n" +
            "                from common_user_info\n" +
            "                where grade='2018' and major_id not like '%专升本%' and major_id !='教职工' and qualified !='1'\n" +
            "            ) and status = 'APPROVED' and certificate_name not in ('英语四六级证书','浙江省高等学校计算机等级考试证书','浙江省高等学校计算机等级考试')\n" +
            "            group by user_id\n" +
            "        ) temp\n" +
            "    where temp.total>=1\n" +
            ")\n" +
            "\n",nativeQuery = true)
    void update8001();

    @Transactional
    @Modifying
    @Query(value = "update common_user_info\n" +
            "set  qualified='1'\n" +
            "where user_id in (\n" +
            "    select user_id\n" +
            "    from\n" +
            "        (\n" +
            "            select user_id,count(type) as total\n" +
            "            from activity_record\n" +
            "            where user_id in (\n" +
            "                select user_id\n" +
            "                from common_user_info\n" +
            "                where grade='2018' and major_id not like '%专升本%' and major_id !='教职工' and qualified !='1'\n" +
            "            )\n" +
            "              and type in ('schoolActivity')\n" +
            "            group by user_id\n" +
            "        ) temp\n" +
            "    where temp.total>=8\n" +
            ") and user_id in (\n" +
            "    select user_id\n" +
            "    from\n" +
            "        (\n" +
            "            select user_id,count(type) as total\n" +
            "            from activity_record\n" +
            "            where user_id in (\n" +
            "                select user_id\n" +
            "                from common_user_info\n" +
            "                where grade='2018' and major_id not like '%专升本%' and major_id !='教职工' and qualified = '1'\n" +
            "            )\n" +
            "              and type in ('practiceActivity')\n" +
            "            group by user_id\n" +
            "        ) temp\n" +
            "    where temp.total>=1\n" +
            ")\n" +
            "\n",nativeQuery = true)
    void update8010();

    @Transactional
    @Modifying
    @Query(value = "update common_user_info\n" +
            "set  qualified='1'\n" +
            "where user_id in (\n" +
            "    select user_id\n" +
            "    from\n" +
            "        (\n" +
            "            select user_id,count(type) as total\n" +
            "            from activity_record\n" +
            "            where user_id in (\n" +
            "                select user_id\n" +
            "                from common_user_info\n" +
            "                where grade='2018' and major_id not like '%专升本%' and major_id !='教职工' and qualified !='1'\n" +
            "            )\n" +
            "              and type in ('schoolActivity')\n" +
            "            group by user_id\n" +
            "        ) temp\n" +
            "    where temp.total>=8\n" +
            "    ) and user_id in (\n" +
            "    select user_id\n" +
            "    from\n" +
            "        (\n" +
            "            select user_id,count(type) as total\n" +
            "            from activity_record\n" +
            "            where user_id in (\n" +
            "                select user_id\n" +
            "                from common_user_info\n" +
            "                where grade='2018' and major_id not like '%专升本%' and major_id !='教职工' and qualified !='1'\n" +
            "            )\n" +
            "              and type in ('lectureActivity')\n" +
            "            group by user_id\n" +
            "        ) temp\n" +
            "    where temp.total>=8\n" +
            "    )\n" +
            "\n",nativeQuery = true)
    void update8800();

    /**
     * 更新大四专升本未达标情况
     */
    @Transactional
    @Modifying
    @Query(value = "update common_user_info\n" +
            "set  qualified='2'\n" +
            "where user_id in (\n" +
            "    select user_id\n" +
            "    from\n" +
            "        (\n" +
            "            select user_id,count(type) as total\n" +
            "            from activity_record\n" +
            "            where user_id in (\n" +
            "                select user_id\n" +
            "                from common_user_info\n" +
            "                where grade='2018' and major_id like '%专升本%' and qualified !='2'\n" +
            "            )\n" +
            "              and type in ('practiceActivity')\n" +
            "            group by user_id\n" +
            "        ) temp\n" +
            "    where temp.total>=1\n" +
            ")  or user_id in (\n" +
            "    select user_id\n" +
            "    from\n" +
            "        (\n" +
            "            select user_id,count(*) as total\n" +
            "            from certificate_qualifications_record\n" +
            "            where user_id in (\n" +
            "                select user_id\n" +
            "                from common_user_info\n" +
            "                where grade='2018' and major_id like '%专升本%' and qualified !='2'\n" +
            "            ) and status = 'APPROVED' and certificate_name not in ('英语四六级证书','浙江省高等学校计算机等级考试证书','浙江省高等学校计算机等级考试')\n" +
            "            group by user_id\n" +
            "        ) temp\n" +
            "    where temp.total>=1\n" +
            ")  or user_id in (\n" +
            "    select user_id\n" +
            "    from\n" +
            "        (\n" +
            "            select user_id,count(type) as total\n" +
            "            from activity_record\n" +
            "            where user_id in (\n" +
            "                select user_id\n" +
            "                from common_user_info\n" +
            "                where grade='2018' and major_id like '%专升本%' and qualified !='2'\n" +
            "            )\n" +
            "              and type in ('lectureActivity','schoolActivity')\n" +
            "            group by user_id\n" +
            "        ) temp\n" +
            "    where temp.total>=4\n" +
            ")\n" +
            "\n",nativeQuery = true)
    void update411();


    /**
     * 查询普通本科大四未达标
     */
    @Query(value = "select * from common_user_info where grade='2018' and major_id not like '%专升本%' and major_id !='教职工' and qualified !='1'",nativeQuery = true)
    Page<UserInfoDO> findUnQualifiedUndergraduate(Pageable pageable);

    /**
     * 查询专升本大四未达标
     */
    @Query(value = "select * from common_user_info where grade='2018' and major_id like '%专升本%' and qualified !='2'",nativeQuery = true)
    Page<UserInfoDO> findUnQualifiedCollegeUpgrade(Pageable pageable);

    /**
     * 查询指定userId学生的讲座章数
     * @param userId
     * @return
     */
    @Query(value = "select count(*) from activity_record where user_id=? and type='lectureActivity'",nativeQuery = true)
    int getLectureStampNumByUserId(String userId);

    /**
     * 查询指定userId学生的活动章数
     * @param userId
     * @return
     */
    @Query(value = "select count(*) from activity_record where user_id=? and type='schoolActivity'",nativeQuery = true)
    int getActivityStampNumByUserId(String userId);

    /**
     * 查询指定userId学生的实践次数
     * @param userId
     * @return
     */
    @Query(value = "select count(*) from activity_record where user_id=? and type='practiceActivity'",nativeQuery = true)
    int getPracticeTimesByUserId(String userId);


    /**
     * 查询指定userId学生的证书数量
     * @param userId
     * @return
     */
    @Query(value = "select count(*) from certificate_qualifications_record where status = 'APPROVED' and certificate_name not in ('英语四六级证书','浙江省高等学校计算机等级考试证书','浙江省高等学校计算机等级考试') and user_id = ?",nativeQuery = true)
    int getCertificateNumByUserId(String userId);

    @Query(value = "select * from common_user_info where grade in ('2018','2019','2020','2021') GROUP BY major_id,grade,user_id", nativeQuery = true)
    List<UserInfoDO> getUserInfoDOByGrade();
}
