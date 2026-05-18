package com.github.thundax.modules.storage.persistence.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.github.thundax.modules.storage.persistence.dataobject.StoredObjectReferenceDO;
import com.github.thundax.modules.storage.persistence.mapper.StoredObjectReferenceMapper;
import java.util.Arrays;
import java.util.List;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class StoredObjectReferenceDaoImplTest {

    @BeforeClass
    public static void setUpTableInfo() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, StoredObjectReferenceDO.class);
    }

    @Test
    public void shouldListReferenceOwnerTypesFromReferenceOwnerTypeColumn() {
        StoredObjectReferenceMapper mapper = mock(StoredObjectReferenceMapper.class);
        StoredObjectReferenceDaoImpl dao = new StoredObjectReferenceDaoImpl(mapper);
        when(mapper.selectObjs(any())).thenReturn(Arrays.<Object>asList("USER", null, "MEMBER"));

        List<String> result = dao.listReferenceOwnerTypes();

        assertEquals(Arrays.asList("USER", "MEMBER"), result);
        Wrapper<StoredObjectReferenceDO> wrapper = captureWrapper(mapper);
        String sql = normalizedSql(wrapper);
        assertTrue(sql.contains("reference_owner_type"));
        assertTrue(sql.contains("group by reference_owner_type"));
        assertTrue(sql.contains("order by reference_owner_type asc"));
        assertTrue(!sql.contains("business_type"));
    }

    private Wrapper<StoredObjectReferenceDO> captureWrapper(StoredObjectReferenceMapper mapper) {
        ArgumentCaptor<Wrapper> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(mapper).selectObjs(wrapperCaptor.capture());
        return wrapperCaptor.getValue();
    }

    private String normalizedSql(Wrapper<?> wrapper) {
        return wrapper.getSqlSegment().replaceAll("\\s+", " ").toLowerCase();
    }
}
