/**
 * betahouse.us
 * CopyRight (c) 2012 - 2018
 */
package us.betahouse.haetae.user.dal.repo.perm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import us.betahouse.haetae.user.dal.model.perm.PermDO;

import java.util.List;

/**
 * 权限仓储
 *
 * @author dango.yxm
 * @version : PermDORepo.java 2018/11/16 下午7:07 dango.yxm
 */
@Repository
public interface PermDORepo extends JpaRepository<PermDO, Long> {

    /**
     * 通过权限id获取权限
     *
     * @param perms
     * @return
     */
    List<PermDO> findAllByPermIdIn(List<String> perms);

    /**
     * 查询权限
     * @param permId
     * @return
     */
    PermDO findByPermId(String permId);
}
