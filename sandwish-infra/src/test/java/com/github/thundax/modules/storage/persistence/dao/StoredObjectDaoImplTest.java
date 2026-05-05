package com.github.thundax.modules.storage.persistence.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.modules.storage.persistence.cache.StorageCacheSupport;
import com.github.thundax.modules.storage.persistence.dataobject.StoredObjectDO;
import com.github.thundax.modules.storage.persistence.dataobject.StoredObjectReferenceDO;
import com.github.thundax.modules.storage.persistence.mapper.StoredObjectMapper;
import com.github.thundax.modules.storage.persistence.mapper.StoredObjectReferenceMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class StoredObjectDaoImplTest {

    @BeforeClass
    public static void setUpTableInfo() {
        initTableInfo(StoredObjectDO.class);
        initTableInfo(StoredObjectReferenceDO.class);
    }

    @Test
    public void shouldBuildListQueryWithReferenceFilters() {
        StoredObjectMapper mapper = mock(StoredObjectMapper.class);
        StoredObjectReferenceMapper referenceMapper = mock(StoredObjectReferenceMapper.class);
        StoredObjectDaoImpl dao = dao(mapper, referenceMapper);
        when(referenceMapper.selectObjs(any())).thenReturn(Arrays.<Object>asList("s1", "s2"));
        when(mapper.selectList(any())).thenReturn(Collections.<StoredObjectDO>emptyList());

        dao.list("image/png", "owner-1", "USER", "ACTIVE", "REFERENCED", "biz-1", "ARTICLE", "avatar", "profile");

        Wrapper<StoredObjectReferenceDO> referenceWrapper = captureReferenceWrapper(referenceMapper);
        assertSqlContains(referenceWrapper, "reference_owner_id");
        assertSqlContains(referenceWrapper, "reference_owner_type");
        assertParamsContain(referenceWrapper, "biz-1", "ARTICLE");

        Wrapper<StoredObjectDO> storageWrapper = captureStorageListWrapper(mapper);
        assertSqlContains(storageWrapper, "del_flag");
        assertSqlContains(storageWrapper, "id");
        assertSqlContains(storageWrapper, "mime_type");
        assertSqlContains(storageWrapper, "owner_id");
        assertSqlContains(storageWrapper, "owner_type");
        assertSqlContains(storageWrapper, "object_status");
        assertSqlContains(storageWrapper, "reference_status");
        assertSqlContains(storageWrapper, "name");
        assertSqlContains(storageWrapper, "remarks");
        assertSqlContains(storageWrapper, "order by create_date desc");
        assertSqlContains(storageWrapper, "priority asc");
        assertParamsContain(
                storageWrapper,
                "0",
                "s1",
                "s2",
                "image/png",
                "owner-1",
                "USER",
                "ACTIVE",
                "REFERENCED",
                "%avatar%",
                "%profile%");
    }

    @Test
    public void shouldUseNoMatchConditionWhenReferenceFilterHasNoStorage() {
        StoredObjectMapper mapper = mock(StoredObjectMapper.class);
        StoredObjectReferenceMapper referenceMapper = mock(StoredObjectReferenceMapper.class);
        StoredObjectDaoImpl dao = dao(mapper, referenceMapper);
        when(referenceMapper.selectObjs(any())).thenReturn(Collections.emptyList());
        when(mapper.selectList(any())).thenReturn(Collections.<StoredObjectDO>emptyList());

        dao.list(null, null, null, null, null, "biz-1", null, null, null);

        Wrapper<StoredObjectDO> storageWrapper = captureStorageListWrapper(mapper);
        assertSqlContains(storageWrapper, "id");
        assertParamsContain(storageWrapper, "__no_matching_storage__");
    }

    @Test
    public void shouldPassPageArgumentsAndReuseListQueryRules() {
        StoredObjectMapper mapper = mock(StoredObjectMapper.class);
        StoredObjectReferenceMapper referenceMapper = mock(StoredObjectReferenceMapper.class);
        StoredObjectDaoImpl dao = dao(mapper, referenceMapper);
        Page<StoredObjectDO> dataObjectPage = new Page<>(3, 25);
        dataObjectPage.setTotal(0);
        when(mapper.selectPage(any(), any())).thenReturn(dataObjectPage);

        Page<?> page = dao.page("text/plain", null, null, "ACTIVE", null, null, null, null, null, 3, 25);

        ArgumentCaptor<Page> pageCaptor = ArgumentCaptor.forClass(Page.class);
        ArgumentCaptor<Wrapper> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(mapper).selectPage(pageCaptor.capture(), wrapperCaptor.capture());
        assertEquals(3L, pageCaptor.getValue().getCurrent());
        assertEquals(25L, pageCaptor.getValue().getSize());
        assertEquals(3L, page.getCurrent());
        assertEquals(25L, page.getSize());
        assertSqlContains(wrapperCaptor.getValue(), "del_flag");
        assertSqlContains(wrapperCaptor.getValue(), "mime_type");
        assertSqlContains(wrapperCaptor.getValue(), "object_status");
        assertParamsContain(wrapperCaptor.getValue(), "0", "text/plain", "ACTIVE");
    }

    private StoredObjectDaoImpl dao(StoredObjectMapper mapper, StoredObjectReferenceMapper referenceMapper) {
        return new StoredObjectDaoImpl(mapper, referenceMapper, mock(StorageCacheSupport.class));
    }

    private static void initTableInfo(Class<?> entityClass) {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, entityClass);
    }

    private Wrapper<StoredObjectDO> captureStorageListWrapper(StoredObjectMapper mapper) {
        ArgumentCaptor<Wrapper> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(mapper).selectList(wrapperCaptor.capture());
        return wrapperCaptor.getValue();
    }

    private Wrapper<StoredObjectReferenceDO> captureReferenceWrapper(StoredObjectReferenceMapper referenceMapper) {
        ArgumentCaptor<Wrapper> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(referenceMapper).selectObjs(wrapperCaptor.capture());
        return wrapperCaptor.getValue();
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
