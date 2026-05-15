package com.github.thundax.modules.open.persistence.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.modules.open.entity.OpenClientPermission;
import com.github.thundax.modules.open.entity.enums.OpenClientStatus;
import com.github.thundax.modules.open.entity.valueobject.OpenClientId;
import com.github.thundax.modules.open.persistence.dataobject.OpenClientDO;
import com.github.thundax.modules.open.persistence.dataobject.OpenClientPermissionDO;
import com.github.thundax.modules.open.persistence.mapper.OpenClientMapper;
import com.github.thundax.modules.open.persistence.mapper.OpenClientPermissionMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class OpenClientDaoImplTest {

    @BeforeClass
    public static void setUpTableInfo() {
        initTableInfo(OpenClientDO.class);
        initTableInfo(OpenClientPermissionDO.class);
    }

    @Test
    public void shouldBuildPageQueryWithNameAndStatus() {
        OpenClientMapper mapper = mock(OpenClientMapper.class);
        OpenClientPermissionMapper permissionMapper = mock(OpenClientPermissionMapper.class);
        OpenClientDaoImpl dao = new OpenClientDaoImpl(mapper, permissionMapper);
        Page<OpenClientDO> dataObjectPage = new Page<>(3, 25);
        dataObjectPage.setTotal(0);
        when(mapper.selectPage(any(), any())).thenReturn(dataObjectPage);

        Page<?> page = dao.page("partner", OpenClientStatus.ENABLED.value(), 3, 25);

        ArgumentCaptor<Page> pageCaptor = ArgumentCaptor.forClass(Page.class);
        ArgumentCaptor<Wrapper> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(mapper).selectPage(pageCaptor.capture(), wrapperCaptor.capture());
        assertEquals(3L, pageCaptor.getValue().getCurrent());
        assertEquals(25L, pageCaptor.getValue().getSize());
        assertEquals(3L, page.getCurrent());
        assertEquals(25L, page.getSize());
        assertSqlContains(wrapperCaptor.getValue(), "name");
        assertSqlContains(wrapperCaptor.getValue(), "status");
        assertSqlContains(wrapperCaptor.getValue(), "id desc");
        assertParamsContain(wrapperCaptor.getValue(), "%partner%", "ENABLED");
    }

    @Test
    public void shouldListPermissionsByClientId() {
        OpenClientMapper mapper = mock(OpenClientMapper.class);
        OpenClientPermissionMapper permissionMapper = mock(OpenClientPermissionMapper.class);
        OpenClientDaoImpl dao = new OpenClientDaoImpl(mapper, permissionMapper);
        when(permissionMapper.selectList(any())).thenReturn(Collections.<OpenClientPermissionDO>emptyList());

        dao.listPermissionsByClientId(OpenClientId.of(9001L));

        Wrapper<OpenClientPermissionDO> wrapper = capturePermissionListWrapper(permissionMapper);
        assertSqlContains(wrapper, "client_id");
        assertSqlContains(wrapper, "permission asc");
        assertParamsContain(wrapper, 9001L);
    }

    @Test
    public void shouldDeletePermissionsByClientId() {
        OpenClientMapper mapper = mock(OpenClientMapper.class);
        OpenClientPermissionMapper permissionMapper = mock(OpenClientPermissionMapper.class);
        OpenClientDaoImpl dao = new OpenClientDaoImpl(mapper, permissionMapper);
        when(permissionMapper.delete(any())).thenReturn(2);

        assertEquals(2, dao.deleteByClientId(OpenClientId.of(9001L)));

        Wrapper<OpenClientPermissionDO> wrapper = capturePermissionDeleteWrapper(permissionMapper);
        assertSqlContains(wrapper, "client_id");
        assertParamsContain(wrapper, 9001L);
    }

    @Test
    public void shouldGeneratePermissionIdsWhenBatchInsert() {
        OpenClientMapper mapper = mock(OpenClientMapper.class);
        OpenClientPermissionMapper permissionMapper = mock(OpenClientPermissionMapper.class);
        OpenClientDaoImpl dao = new OpenClientDaoImpl(mapper, permissionMapper);
        when(permissionMapper.insert(any())).thenReturn(1);
        OpenClientPermission permission = new OpenClientPermission();
        permission.setClientId(OpenClientId.of(9001L));
        permission.setPermission("submission:submission:create");

        assertEquals(1, dao.batchInsertPermissions(Arrays.asList(permission)));

        ArgumentCaptor<OpenClientPermissionDO> captor = ArgumentCaptor.forClass(OpenClientPermissionDO.class);
        verify(permissionMapper).insert(captor.capture());
        assertTrue(captor.getValue().getId() > 0);
        assertEquals(Long.valueOf(9001L), captor.getValue().getClientId());
    }

    private Wrapper<OpenClientPermissionDO> capturePermissionListWrapper(OpenClientPermissionMapper permissionMapper) {
        ArgumentCaptor<Wrapper> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(permissionMapper).selectList(wrapperCaptor.capture());
        return wrapperCaptor.getValue();
    }

    private Wrapper<OpenClientPermissionDO> capturePermissionDeleteWrapper(
            OpenClientPermissionMapper permissionMapper) {
        ArgumentCaptor<Wrapper> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(permissionMapper).delete(wrapperCaptor.capture());
        return wrapperCaptor.getValue();
    }

    private static void initTableInfo(Class<?> entityClass) {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, entityClass);
    }

    private void assertSqlContains(Wrapper<?> wrapper, String expected) {
        assertTrue(normalizedSql(wrapper).contains(expected));
    }

    private String normalizedSql(Wrapper<?> wrapper) {
        return wrapper.getSqlSegment().replaceAll("\\s+", " ").toLowerCase();
    }

    private void assertParamsContain(Wrapper<?> wrapper, Object... values) {
        List<Object> params = Arrays.asList(values);
        List<Object> actual = Arrays.asList(((AbstractWrapper<?, ?, ?>) wrapper)
                .getParamNameValuePairs()
                .values()
                .toArray());
        assertTrue("expected " + params + " in " + actual, actual.containsAll(params));
    }
}
