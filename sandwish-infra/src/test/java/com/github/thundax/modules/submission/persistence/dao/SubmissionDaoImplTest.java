package com.github.thundax.modules.submission.persistence.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.modules.submission.persistence.dataobject.SubmissionDO;
import com.github.thundax.modules.submission.persistence.mapper.SubmissionMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class SubmissionDaoImplTest {

    @BeforeClass
    public static void setUpTableInfo() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, SubmissionDO.class);
    }

    @Test
    public void shouldBuildListQueryWithFilters() {
        SubmissionMapper mapper = mock(SubmissionMapper.class);
        SubmissionDaoImpl dao = new SubmissionDaoImpl(mapper);
        when(mapper.selectList(any())).thenReturn(Collections.<SubmissionDO>emptyList());
        Date begin = new Date(1000L);
        Date end = new Date(2000L);

        dao.list("SUBMITTED", "client-1", begin, end, SortDirection.ASC);

        Wrapper<SubmissionDO> wrapper = captureListWrapper(mapper);
        assertSqlContains(wrapper, "status");
        assertSqlContains(wrapper, "source_client_id");
        assertSqlContains(wrapper, "submitted_at");
        assertSqlContains(wrapper, "priority asc");
        assertSqlContains(wrapper, "id asc");
        assertParamsContain(wrapper, "SUBMITTED", "client-1", begin, end);
    }

    @Test
    public void shouldPassPageArgumentsAndReuseListQueryRules() {
        SubmissionMapper mapper = mock(SubmissionMapper.class);
        SubmissionDaoImpl dao = new SubmissionDaoImpl(mapper);
        Page<SubmissionDO> dataObjectPage = new Page<>(3, 25);
        dataObjectPage.setTotal(0);
        when(mapper.selectPage(any(), any())).thenReturn(dataObjectPage);

        Page<?> page = dao.page("APPROVED", "client-1", null, null, SortDirection.DESC, 3, 25);

        ArgumentCaptor<Page> pageCaptor = ArgumentCaptor.forClass(Page.class);
        ArgumentCaptor<Wrapper> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(mapper).selectPage(pageCaptor.capture(), wrapperCaptor.capture());
        assertEquals(3L, pageCaptor.getValue().getCurrent());
        assertEquals(25L, pageCaptor.getValue().getSize());
        assertEquals(3L, page.getCurrent());
        assertEquals(25L, page.getSize());
        assertSqlContains(wrapperCaptor.getValue(), "priority desc");
        assertParamsContain(wrapperCaptor.getValue(), "APPROVED", "client-1");
    }

    @Test
    public void shouldReturnZeroWhenMaxPriorityResultIsNull() {
        SubmissionMapper mapper = mock(SubmissionMapper.class);
        SubmissionDaoImpl dao = new SubmissionDaoImpl(mapper);
        when(mapper.selectObjs(any())).thenReturn(Collections.singletonList(null));

        assertEquals(0, dao.maxPriority());
    }

    @Test
    public void shouldDeleteById() {
        SubmissionMapper mapper = mock(SubmissionMapper.class);
        SubmissionDaoImpl dao = new SubmissionDaoImpl(mapper);
        when(mapper.deleteById(9001L)).thenReturn(1);

        assertEquals(
                1, dao.deleteById(com.github.thundax.modules.submission.entity.valueobject.SubmissionId.of(9001L)));

        verify(mapper).deleteById(9001L);
    }

    private Wrapper<SubmissionDO> captureListWrapper(SubmissionMapper mapper) {
        ArgumentCaptor<Wrapper> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(mapper).selectList(wrapperCaptor.capture());
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
