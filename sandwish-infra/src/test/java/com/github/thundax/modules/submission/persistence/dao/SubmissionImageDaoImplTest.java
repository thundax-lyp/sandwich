package com.github.thundax.modules.submission.persistence.dao;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.github.thundax.modules.submission.entity.valueobject.SubmissionId;
import com.github.thundax.modules.submission.persistence.dataobject.SubmissionImageDO;
import com.github.thundax.modules.submission.persistence.mapper.SubmissionImageMapper;
import java.util.Collections;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class SubmissionImageDaoImplTest {

    @BeforeClass
    public static void setUpTableInfo() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, SubmissionImageDO.class);
    }

    @Test
    public void shouldLoadImagesBySubmissionIdOrderedBySortOrder() {
        SubmissionImageMapper mapper = mock(SubmissionImageMapper.class);
        SubmissionImageDaoImpl dao = new SubmissionImageDaoImpl(mapper);
        when(mapper.selectList(any())).thenReturn(Collections.<SubmissionImageDO>emptyList());

        dao.listBySubmissionId(SubmissionId.of(9001L));

        ArgumentCaptor<Wrapper> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(mapper).selectList(wrapperCaptor.capture());
        String sql =
                wrapperCaptor.getValue().getSqlSegment().replaceAll("\\s+", " ").toLowerCase();
        assertTrue(sql.contains("submission_id"));
        assertTrue(sql.contains("sort_order asc"));
    }

    @Test
    public void shouldDeleteImagesBySubmissionId() {
        SubmissionImageMapper mapper = mock(SubmissionImageMapper.class);
        SubmissionImageDaoImpl dao = new SubmissionImageDaoImpl(mapper);

        dao.deleteBySubmissionId(SubmissionId.of(9001L));

        ArgumentCaptor<Wrapper> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(mapper).delete(wrapperCaptor.capture());
        String sql =
                wrapperCaptor.getValue().getSqlSegment().replaceAll("\\s+", " ").toLowerCase();
        assertTrue(sql.contains("submission_id"));
    }
}
