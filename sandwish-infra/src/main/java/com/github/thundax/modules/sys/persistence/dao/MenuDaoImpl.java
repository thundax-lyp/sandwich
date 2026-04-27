package com.github.thundax.modules.sys.persistence.dao;

import com.github.thundax.modules.sys.dao.MenuDao;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.persistence.assembler.MenuPersistenceAssembler;
import com.github.thundax.modules.sys.persistence.dataobject.MenuDO;
import com.github.thundax.modules.sys.persistence.mapper.MenuMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 菜单 DAO 实现。
 */
@Repository
public class MenuDaoImpl implements MenuDao {

    private final MenuMapper mapper;

    public MenuDaoImpl(MenuMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Menu get(Menu entity) {
        return MenuPersistenceAssembler.toEntity(mapper.get(MenuPersistenceAssembler.toDataObject(entity)));
    }

    @Override
    public List<Menu> getMany(List<String> idList) {
        return MenuPersistenceAssembler.toEntityList(mapper.getMany(idList));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Menu> findList(Menu entity) {
        List<MenuDO> dataObjects = mapper.findList(MenuPersistenceAssembler.toDataObject(entity));
        List<Menu> entities = MenuPersistenceAssembler.toEntityList(dataObjects);
        if (dataObjects instanceof com.github.pagehelper.Page) {
            List rawPage = (List) dataObjects;
            rawPage.clear();
            rawPage.addAll(entities);
            return rawPage;
        }
        return entities;
    }

    @Override
    public int insert(Menu entity) {
        return mapper.insert(MenuPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int update(Menu entity) {
        return mapper.update(MenuPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int updatePriority(Menu entity) {
        return mapper.updatePriority(MenuPersistenceAssembler.toDataObject(entity));
    }

    public int updateStatus(Menu entity) {
        return mapper.updateStatus(MenuPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int updateDelFlag(Menu entity) {
        return mapper.updateDelFlag(MenuPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int delete(Menu entity) {
        return mapper.delete(MenuPersistenceAssembler.toDataObject(entity));
    }

    @Override
    public Menu getTreeNode(String id) {
        return MenuPersistenceAssembler.toEntity(mapper.getTreeNode(id));
    }

    @Override
    public void updateLftRgt(Menu node) {
        mapper.updateLftRgt(MenuPersistenceAssembler.toDataObject(node));
    }

    @Override
    public void updateParent(Menu node) {
        mapper.updateParent(MenuPersistenceAssembler.toDataObject(node));
    }

    @Override
    public Integer getMaxPosition() {
        return mapper.getMaxPosition();
    }

    @Override
    public void moveTreeRgts(Integer from, Integer offset) {
        mapper.moveTreeRgts(from, offset);
    }

    @Override
    public void moveTreeLfts(Integer from, Integer offset) {
        mapper.moveTreeLfts(from, offset);
    }

    @Override
    public void moveTreeNodes(Integer from, Integer to, Integer offset) {
        mapper.moveTreeNodes(from, to, offset);
    }

    @Override
    public int updateDisplayFlag(Menu menu) {
        return mapper.updateDisplayFlag(MenuPersistenceAssembler.toDataObject(menu));
    }

    @Override
    public void deleteMenuRole(Menu menu) {
        mapper.deleteMenuRole(MenuPersistenceAssembler.toDataObject(menu));
    }
}
