package com.github.thundax.modules.sys.persistence.assembler;

import static org.junit.Assert.*;

import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.enums.MenuVisibility;
import com.github.thundax.modules.sys.entity.valueobject.AccessRank;
import com.github.thundax.modules.sys.persistence.dataobject.MenuDO;
import org.junit.Test;

public class MenuPersistenceAssemblerTest {

    @Test
    public void shouldReadVisibilityAsDomainValue() {
        MenuDO dataObject = new MenuDO();
        dataObject.setVisibility("VISIBLE");

        Menu entity = MenuPersistenceAssembler.toEntity(dataObject);

        assertSame(MenuVisibility.VISIBLE, entity.getVisibility());
    }

    @Test
    public void shouldRejectLegacyVisibilityValue() {
        MenuDO dataObject = new MenuDO();
        dataObject.setVisibility("1");

        try {
            MenuPersistenceAssembler.toEntity(dataObject);
            fail("Legacy visibility value must be rejected");
        } catch (RuntimeException expected) {
            assertEquals("Unknown menu visibility: 1", expected.getMessage());
        }
    }

    @Test
    public void shouldWriteDomainValueToVisibility() {
        Menu entity = new Menu();
        entity.setVisibility(MenuVisibility.HIDDEN);

        MenuDO dataObject = MenuPersistenceAssembler.toDataObject(entity);

        assertEquals("HIDDEN", dataObject.getVisibility());
    }

    @Test
    public void shouldMapDisplayFieldsAtPersistenceBoundary() {
        Menu entity = new Menu();
        entity.setName("用户管理");
        entity.setPerms("sys:user:view");
        MenuDO dataObject = new MenuDO();
        dataObject.setName("角色管理");
        dataObject.setPerms("sys:role:view");

        assertEquals("用户管理", MenuPersistenceAssembler.toDataObject(entity).getName());
        assertEquals(
                "sys:user:view", MenuPersistenceAssembler.toDataObject(entity).getPerms());
        assertEquals("角色管理", MenuPersistenceAssembler.toEntity(dataObject).getName());
        assertEquals(
                "sys:role:view", MenuPersistenceAssembler.toEntity(dataObject).getPerms());
    }

    @Test
    public void shouldMapRankValueObjectToRanksColumn() {
        Menu entity = new Menu();
        entity.setRank(AccessRank.of(12));

        MenuDO dataObject = MenuPersistenceAssembler.toDataObject(entity);
        assertEquals(Integer.valueOf(9), dataObject.getRanks());

        dataObject.setRanks(-1);
        Menu restored = MenuPersistenceAssembler.toEntity(dataObject);
        assertEquals(AccessRank.of(0), restored.getRank());
    }
}
