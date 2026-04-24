package com.github.thundax.common.service.impl;

import com.github.thundax.common.persistence.TreeDao;
import com.github.thundax.common.persistence.TreeEntity;
import com.github.thundax.common.service.TreeService;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.utils.redis.RedisClient;
import org.springframework.transaction.annotation.Transactional;

/**
 * @param <D> dao
 * @param <T> entity
 * @author thundax
 */
@Transactional(readOnly = true)
public abstract class TreeServiceImpl<D extends TreeDao<T>, T extends TreeEntity<T>> extends CrudServiceImpl<D, T> implements TreeService<T> {

    public TreeServiceImpl(D dao, RedisClient redisClient) {
        super(dao, redisClient);
    }

    /**
     * 树形结构更新说明
     * <p>
     * 1) 新增节点
     * A) 获取新位置 newPosition,
     * 如果parent != null, 则newPosition = parent.rgt
     * 如果parent == null, 则newPosition = max(rgt)
     * 如果max(rgt) == null，则newPosition = 1
     * B) update set rgt = rgt + 2 where rgt >= newPosition;
     * update set lft = lft + 2 where lft >= newPosition;
     * C) 更新当前节点 item.lft = newPosition; item.rgt = newPosition + 1;
     * <p>
     * 2) 更新节点（父节点变化, newParent, oldParent）
     * A) 获取节点当前跨度span = me.rgt - me.lft + 1
     * B) 获取节点当前位置oldLft = me.lft, oldRgt = me.rgt
     * C) 获取新位置 newPosition
     * 如果newParent != null, 则newPosition = parent.rgt
     * 如果newParent == null, 则newPosition = max(rgt)
     * 如果max(rgt) == null，则newPosition = 1
     * D) update set rgt = rgt + span where rgt >= newParent.rgt
     * update set lft = lft + span where lft >= newParent.rgt
     * E) 更新当前节点 me.lft = newPosition; me.rgt = newPosition + span - 1;
     * F) update set rgt = rgt - span where rgt > oldRgt
     * update set lft = lft - span where lft > oldRgt
     * <p>
     * 3) 删除（必须无子节点）
     * A) 获取当前位置position = me.lft;
     * update set rgt = rgt - 2 where rgt > position
     * update set lft = lft - 2 where lft > position
     *
     * @param entity 对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(T entity) {
        if (entity.getIsNewRecord()) {
            entity.preInsert();

            Integer newPosition;
            if (StringUtils.isNotBlank(entity.getParentId()) && !StringUtils.equals(entity.getParentId(), TreeEntity.ROOT_ID)) {
                T parent = dao.getTreeNode(entity.getParentId());
                newPosition = parent.getRgt();
            } else {
                entity.setParentId(null);
                Integer maxRgt = dao.getMaxPosition(entity);
                if (maxRgt == null) {
                    maxRgt = 0;
                }
                newPosition = maxRgt + 1;
            }
            entity.setLft(newPosition);
            entity.setRgt(newPosition + 1);
            dao.moveTreeRgts(entity, newPosition, 2);
            dao.moveTreeLfts(entity, newPosition, 2);
            dao.insert(entity);

        } else {
            entity.preUpdate();
            T oldBean = dao.getTreeNode(entity.getId());
            if (!StringUtils.equals(oldBean.getParentId(), entity.getParentId())) {
                Integer newPosition;
                if (StringUtils.isNotBlank(entity.getParentId())) {
                    T parent = dao.getTreeNode(entity.getParentId());
                    newPosition = parent.getRgt();
                } else {
                    newPosition = dao.getMaxPosition(entity) + 1;
                }
                dao.moveTreeRgts(oldBean, newPosition, oldBean.treeSpan());
                dao.moveTreeLfts(oldBean, newPosition, oldBean.treeSpan());

                // 调整自己
                oldBean = dao.getTreeNode(entity.getId());
                int offset = newPosition - oldBean.getLft();
                dao.moveTreeNodes(oldBean, oldBean.getLft(), oldBean.getRgt(), offset);

                // 删除空位
                dao.moveTreeRgts(oldBean, oldBean.getLft(), -oldBean.treeSpan());
                dao.moveTreeLfts(oldBean, oldBean.getLft(), -oldBean.treeSpan());
            }
            dao.update(entity);
        }

        removeAllCache();
    }

    /**
     * 删除
     * 获取当前位置position = me.lft;
     * update set rgt = rgt - 2 where rgt > position
     * update set lft = lft - 2 where lft > position
     *
     * @param entity 对象
     * @return 影响记录数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(T entity) {
        T bean = this.get(entity.getId());
        if (bean == null) {
            return 0;
        }
        dao.moveTreeRgts(bean, bean.getLft(), -bean.treeSpan());
        dao.moveTreeLfts(bean, bean.getLft(), -bean.treeSpan());

        int count = dao.delete(bean);

        removeAllCache();

        return count;
    }

    /**
     * 移动节点
     *
     * @param from     源节点
     * @param to       目标节点
     * @param moveType 移动类型:after, before, inside
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveTreeNode(T from, T to, MoveTreeNodeType moveType) {
        T fromBean = dao.getTreeNode(from.getId());
        T toBean = dao.getTreeNode(to.getId());

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

        // 空出位置
        dao.moveTreeLfts(toBean, newPosition, fromBean.treeSpan());
        dao.moveTreeRgts(toBean, newPosition, fromBean.treeSpan());

        // 调整 from 位置
        fromBean = dao.getTreeNode(from.getId());
        int offset = newPosition - fromBean.getLft();
        dao.moveTreeNodes(fromBean, fromBean.getLft(), fromBean.getRgt(), offset);

        // 删除空位
        dao.moveTreeLfts(fromBean, fromBean.getLft(), -fromBean.treeSpan());
        dao.moveTreeRgts(fromBean, fromBean.getLft(), -fromBean.treeSpan());

        //更新父节
        from.setParentId(newParentId);
        dao.updateParent(from);

        removeAllCache();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLftRgt(T entity) {
        dao.updateLftRgt(entity);
        removeAllCache();
    }

}
