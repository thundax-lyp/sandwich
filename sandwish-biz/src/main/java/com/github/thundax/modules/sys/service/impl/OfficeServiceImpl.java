package com.github.thundax.modules.sys.service.impl;

import com.github.thundax.common.Constants;
import com.github.thundax.common.persistence.TreeEntity;
import com.github.thundax.common.service.impl.CrudServiceImpl;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.utils.redis.RedisClient;
import com.github.thundax.modules.sys.dao.OfficeDao;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.service.OfficeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wdit
 */
@Service
@Transactional(readOnly = true)
public class OfficeServiceImpl extends CrudServiceImpl<OfficeDao, Office> implements OfficeService {

    public OfficeServiceImpl(OfficeDao dao, RedisClient redisClient) {
        super(dao, redisClient);
    }

    @Override
    protected boolean isRedisCacheEnabled() {
        return true;
    }

    @Override
    protected String getCacheSection() {
        return Constants.CACHE_PREFIX + "SYS_OFFICE_";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(Office entity) {
        if (entity.getIsNewRecord()) {
            entity.preInsert();

            Integer newPosition;
            if (StringUtils.isNotBlank(entity.getParentId())
                    && !StringUtils.equals(entity.getParentId(), TreeEntity.ROOT_ID)) {
                Office parent = dao.getTreeNode(entity.getParentId());
                newPosition = parent.getRgt();
            } else {
                entity.setParentId(null);
                Integer maxRgt = dao.getMaxPosition();
                if (maxRgt == null) {
                    maxRgt = 0;
                }
                newPosition = maxRgt + 1;
            }
            entity.setLft(newPosition);
            entity.setRgt(newPosition + 1);
            dao.moveTreeRgts(newPosition, 2);
            dao.moveTreeLfts(newPosition, 2);
            dao.insert(entity);

        } else {
            entity.preUpdate();
            Office oldBean = dao.getTreeNode(entity.getId());
            if (!StringUtils.equals(oldBean.getParentId(), entity.getParentId())) {
                Integer newPosition;
                if (StringUtils.isNotBlank(entity.getParentId())) {
                    Office parent = dao.getTreeNode(entity.getParentId());
                    newPosition = parent.getRgt();
                } else {
                    newPosition = dao.getMaxPosition() + 1;
                }
                dao.moveTreeRgts(newPosition, oldBean.treeSpan());
                dao.moveTreeLfts(newPosition, oldBean.treeSpan());

                oldBean = dao.getTreeNode(entity.getId());
                int offset = newPosition - oldBean.getLft();
                dao.moveTreeNodes(oldBean.getLft(), oldBean.getRgt(), offset);

                dao.moveTreeRgts(oldBean.getLft(), -oldBean.treeSpan());
                dao.moveTreeLfts(oldBean.getLft(), -oldBean.treeSpan());
            }
            dao.update(entity);
        }

        removeAllCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Office entity) {
        Office bean = this.get(entity.getId());
        if (bean == null) {
            return 0;
        }
        dao.moveTreeRgts(bean.getLft(), -bean.treeSpan());
        dao.moveTreeLfts(bean.getLft(), -bean.treeSpan());

        int count = dao.delete(bean);

        removeAllCache();

        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveTreeNode(Office from, Office to, MoveTreeNodeType moveType) {
        Office fromBean = dao.getTreeNode(from.getId());
        Office toBean = dao.getTreeNode(to.getId());

        int newPosition;
        String newParentId;
        if (moveType == MoveTreeNodeType.AFTER) {
            newPosition = toBean.getRgt() + 1;
            newParentId = toBean.getParentId();

        } else if (moveType == MoveTreeNodeType.BEFORE) {
            newPosition = toBean.getLft();
            newParentId = toBean.getParentId();

        } else if (moveType == MoveTreeNodeType.INSIDE) {
            newPosition = toBean.getLft() + 1;
            newParentId = to.getId();

        } else {
            newPosition = toBean.getRgt();
            newParentId = to.getId();
        }

        dao.moveTreeLfts(newPosition, fromBean.treeSpan());
        dao.moveTreeRgts(newPosition, fromBean.treeSpan());

        fromBean = dao.getTreeNode(from.getId());
        int offset = newPosition - fromBean.getLft();
        dao.moveTreeNodes(fromBean.getLft(), fromBean.getRgt(), offset);

        dao.moveTreeLfts(fromBean.getLft(), -fromBean.treeSpan());
        dao.moveTreeRgts(fromBean.getLft(), -fromBean.treeSpan());

        from.setParentId(newParentId);
        dao.updateParent(from);

        removeAllCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLftRgt(Office entity) {
        dao.updateLftRgt(entity);
        removeAllCache();
    }

}
