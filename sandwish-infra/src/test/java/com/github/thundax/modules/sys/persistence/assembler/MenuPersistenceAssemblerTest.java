package com.github.thundax.modules.sys.persistence.assembler;

import static org.junit.Assert.*;

import com.github.thundax.common.exception.BizException;
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
        } catch (BizException expected) {
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
    public void shouldNormalizeNegativePriorityAtPersistenceBoundary() {
        Menu entity = new Menu();
        entity.setPriority(-1);
        MenuDO dataObject = new MenuDO();
        dataObject.setPriority(-1);

        assertEquals(
                Integer.valueOf(0),
                MenuPersistenceAssembler.toDataObject(entity).getPriority());
        assertEquals(0, MenuPersistenceAssembler.toEntity(dataObject).getPriority());
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
