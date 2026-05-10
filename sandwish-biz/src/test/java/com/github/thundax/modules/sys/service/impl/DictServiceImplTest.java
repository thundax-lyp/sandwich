package com.github.thundax.modules.sys.service.impl;

import static org.junit.Assert.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.modules.sys.dao.DictDao;
import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.entity.valueobject.DictId;
import com.github.thundax.modules.sys.entity.valueobject.DictIdCodec;
import com.github.thundax.modules.sys.service.command.ChangeDictInfoCommand;
import com.github.thundax.modules.sys.service.command.CreateDictCommand;
import com.github.thundax.modules.sys.service.command.DeleteDictCommand;
import com.github.thundax.modules.sys.service.query.DictQuery;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class DictServiceImplTest {

    @Test
    public void shouldReadByQueryId() {
        RecordingDictDao dao = new RecordingDictDao();
        dao.getResult = dict(1001L);
        DictServiceImpl service = new DictServiceImpl(dao);

        Dict result = service.get(DictId.of(1001L));

        assertSame(dao.getResult, result);
        assertEquals(Long.valueOf(1001L), dao.getId);
    }

    @Test
    public void shouldExpandFindListQuery() {
        RecordingDictDao dao = new RecordingDictDao();
        DictQuery query = new DictQuery();
        query.setType("status");
        query.setLabel("启用");
        query.setRemarks("system");
        DictServiceImpl service = new DictServiceImpl(dao);

        service.list(query);

        assertEquals("status", dao.type);
        assertEquals("启用", dao.label);
        assertEquals("system", dao.remarks);
    }

    @Test
    public void shouldNormalizeInvalidPageBeforeQuery() {
        RecordingDictDao dao = new RecordingDictDao();
        PageQuery page = new PageQuery();
        page.setPageNo(0);
        page.setPageSize(0);
        DictServiceImpl service = new DictServiceImpl(dao);

        PageResult<Dict> result = service.page(new DictQuery(), page);

        assertEquals(PageRules.firstPageIndex(), dao.pageNo);
        assertEquals(PageRules.defaultPageSize(), dao.pageSize);
        assertEquals(1L, result.getTotalCount());
    }

    @Test
    public void shouldCreateDictFromCommand() {
        RecordingDictDao dao = new RecordingDictDao();
        CreateDictCommand command = new CreateDictCommand("status", "启用", "ENABLED", "system");
        DictServiceImpl service = new DictServiceImpl(dao);

        DictId id = service.create(command);

        assertEquals(DictId.of(9001L), id);
        assertNotNull(dao.inserted);
        assertEquals("status", dao.inserted.getType());
        assertEquals("启用", dao.inserted.getLabel());
        assertEquals("ENABLED", dao.inserted.getValue());
        assertEquals("system", dao.inserted.getRemarks());
    }

    @Test
    public void shouldChangeDictInfoFromCommand() {
        RecordingDictDao dao = new RecordingDictDao();
        ChangeDictInfoCommand command =
                new ChangeDictInfoCommand(DictId.of(1001L), "status", "禁用", "DISABLED", "system");
        DictServiceImpl service = new DictServiceImpl(dao);

        service.changeInfo(command);

        assertNotNull(dao.updated);
        assertEquals(Long.valueOf(1001L), dao.updated.getId().value());
        assertEquals("status", dao.updated.getType());
        assertEquals("禁用", dao.updated.getLabel());
        assertEquals("DISABLED", dao.updated.getValue());
        assertEquals("system", dao.updated.getRemarks());
    }

    @Test
    public void shouldRemoveDictByCommand() {
        RecordingDictDao dao = new RecordingDictDao();
        DictServiceImpl service = new DictServiceImpl(dao);

        service.remove(new DeleteDictCommand(DictId.of(1001L)));

        assertEquals(Long.valueOf(1001L), dao.deletedId);
    }

    private static Dict dict(Long id) {
        Dict dict = new Dict();
        dict.setId(DictIdCodec.toDomain(id));
        return dict;
    }

    private static class RecordingDictDao implements DictDao {

        private Dict getResult;
        private Long getId;
        private String type;
        private String label;
        private String remarks;
        private int pageNo;
        private int pageSize;
        private Dict inserted;
        private Dict updated;
        private Long deletedId;

        @Override
        public Dict getById(DictId id) {
            this.getId = id.value();
            return getResult;
        }

        @Override
        public List<Dict> listByIds(List<Long> idList) {
            return null;
        }

        @Override
        public List<Dict> list(String type, String label, String remarks) {
            this.type = type;
            this.label = label;
            this.remarks = remarks;
            return Collections.singletonList(dict(1001L));
        }

        @Override
        public Page<Dict> page(String type, String label, String remarks, int pageNo, int pageSize) {
            this.type = type;
            this.label = label;
            this.remarks = remarks;
            this.pageNo = pageNo;
            this.pageSize = pageSize;
            Page<Dict> dataPage = new Page<>(pageNo, pageSize);
            dataPage.setTotal(1);
            return dataPage;
        }

        @Override
        public DictId insert(Dict dict) {
            this.inserted = dict;
            return DictId.of(9001L);
        }

        @Override
        public int update(Dict dict) {
            this.updated = dict;
            return 1;
        }

        @Override
        public int updatePriority(Dict dict) {
            return 1;
        }

        @Override
        public int deleteById(DictId id) {
            this.deletedId = id.value();
            return 1;
        }

        @Override
        public List<String> listTypes() {
            return Collections.singletonList("status");
        }
    }
}
