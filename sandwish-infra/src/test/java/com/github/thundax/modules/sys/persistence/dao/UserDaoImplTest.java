package com.github.thundax.modules.sys.persistence.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.github.thundax.modules.sys.entity.valueobject.UserId;
import com.github.thundax.modules.sys.persistence.cache.RoleCacheSupport;
import com.github.thundax.modules.sys.persistence.cache.UserCacheSupport;
import com.github.thundax.modules.sys.persistence.dataobject.UserDO;
import com.github.thundax.modules.sys.persistence.mapper.UserMapper;
import com.github.thundax.modules.sys.persistence.mapper.UserRoleMapper;
import java.util.Arrays;
import java.util.List;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class UserDaoImplTest {

    @BeforeClass
    public static void setUpTableInfo() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, UserDO.class);
    }

    @Test
    public void shouldCountEmailExcludingCurrentUser() {
        UserMapper mapper = mock(UserMapper.class);
        UserDaoImpl dao = dao(mapper);
        when(mapper.selectCount(any())).thenReturn(1L);

        assertEquals(1, dao.countByEmail("exists@example.com", UserId.of(1001L)));

        Wrapper<UserDO> wrapper = captureCountWrapper(mapper);
        assertSqlContains(wrapper, "email");
        assertSqlContains(wrapper, "id <>");
        assertParamsContain(wrapper, "exists@example.com", 1001L);
    }

    @Test
    public void shouldIgnoreBlankMobileWhenCounting() {
        UserMapper mapper = mock(UserMapper.class);
        UserDaoImpl dao = dao(mapper);

        assertEquals(0, dao.countByMobile(" ", UserId.of(1001L)));

        verify(mapper, never()).selectCount(any());
    }

    private UserDaoImpl dao(UserMapper mapper) {
        return new UserDaoImpl(
                mapper, mock(UserRoleMapper.class), mock(UserCacheSupport.class), mock(RoleCacheSupport.class));
    }

    private Wrapper<UserDO> captureCountWrapper(UserMapper mapper) {
        ArgumentCaptor<Wrapper> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(mapper).selectCount(wrapperCaptor.capture());
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
