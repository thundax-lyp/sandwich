package com.github.thundax.modules.sys.service.impl;

import com.github.thundax.common.service.impl.CrudServiceImpl;
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

    public OfficeServiceImpl(OfficeDao dao) {
        super(dao);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(Office entity) {
        if (entity.getIsNewRecord()) {
            entity.preInsert();
            dao.insert(entity);
        } else {
            entity.preUpdate();
            dao.update(entity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Office entity) {
        Office bean = this.get(entity.getId());
        if (bean == null) {
            return 0;
        }

        int count = dao.delete(bean);

        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void moveTreeNode(Office from, Office to, MoveTreeNodeType moveType) {
        dao.moveTreeNode(from.getId(), to.getId(), moveType);
    }

    @Override
    public boolean isChildOf(Office child, Office parent) {
        return child != null && parent != null && dao.isChildOf(child.getId(), parent.getId());
    }

}
