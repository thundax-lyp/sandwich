package com.github.thundax.modules.sys.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.common.exception.BizException;
import com.github.thundax.common.exception.ErrorCode;
import com.github.thundax.modules.sys.dao.DictDao;
import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.entity.valueobject.DictId;
import com.github.thundax.modules.sys.entity.valueobject.DictIdCodec;
import com.github.thundax.modules.sys.service.command.CreateDictCommand;
import com.github.thundax.modules.sys.service.command.DictSortCommand;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class DictServiceImplTest {

    @Test
    public void shouldCreatePriorityFromGlobalMaxWithStep() {
        RecordingDictDao dao = new RecordingDictDao();
        dao.setMaxPriority(30);
        DictServiceImpl service = new DictServiceImpl(dao);

        DictId id = service.create(new CreateDictCommand("status", null, null, null));

        assertEquals(Long.valueOf(8001L), id.value());
        assertEquals("status", dao.inserted.getType());
        assertEquals(40, dao.inserted.getPriority());
    }

    @Test
    public void shouldSortByAscendingDirectionBySwap() {
        RecordingDictDao dao = new RecordingDictDao();
        List<Dict> currentList =
                Arrays.asList(dict(1001L, "status", 10), dict(1002L, "status", 20), dict(1003L, "status", 30));
        dao.setTypeList(currentList);
        dao.setListByIdsResult(currentList);
        dao.setMaxPriority(30);
        DictServiceImpl service = new DictServiceImpl(dao);

        service.sort(new DictSortCommand(
                Arrays.asList(DictIdCodec.toDomain(1003L), DictIdCodec.toDomain(1001L), DictIdCodec.toDomain(1002L)),
                SortDirection.ASC));

        assertEquals(Arrays.asList(1003L, 1001L, 1003L, 1001L, 1002L, 1001L), dao.getOrderedIdsAfterSort());
        assertEquals(Arrays.asList(40, 30, 10, 41, 30, 20), dao.getUpdatedPriorities());
    }

    @Test
    public void shouldRejectEmptySortInputBySortCode() {
        DictServiceImpl service = new DictServiceImpl(new RecordingDictDao());

        try {
            service.sort(new DictSortCommand(new ArrayList<>(), SortDirection.ASC));
            fail("expect BizException");
        } catch (BizException e) {
            assertEquals(ErrorCode.SORT_EMPTY_INPUT.getCode(), e.getCode());
            assertEquals(ErrorCode.SORT_EMPTY_INPUT.getMessage(), e.getMessage());
        }
    }

    @Test
    public void shouldRejectSortDuplicateIdsBySortCode() {
        RecordingDictDao dao = new RecordingDictDao();
        List<Dict> currentList = Arrays.asList(dict(1001L, "status", 10), dict(1002L, "status", 20));
        dao.setTypeList(currentList);
        dao.setListByIdsResult(currentList);
        DictServiceImpl service = new DictServiceImpl(dao);

        try {
            service.sort(new DictSortCommand(
                    Arrays.asList(DictIdCodec.toDomain(1001L), DictIdCodec.toDomain(1001L)), SortDirection.ASC));
            fail("expect BizException");
        } catch (BizException e) {
            assertEquals(ErrorCode.SORT_DUPLICATE_ID.getCode(), e.getCode());
            assertEquals(ErrorCode.SORT_DUPLICATE_ID.getMessage(), e.getMessage());
        }
    }

    private Dict dict(Long id, String type, int priority) {
        Dict dict = new Dict();
        dict.setId(DictIdCodec.toDomain(id));
        dict.setType(type);
        dict.setPriority(priority);
        return dict;
    }

    private static class RecordingDictDao implements DictDao {

        private int maxPriority;
        private List<Dict> typeList = new ArrayList<>();
        private List<Dict> listByIdsResult = new ArrayList<>();
        private final List<Integer> updatedPriorities = new ArrayList<>();
        private final List<Long> orderedIdsAfterSort = new ArrayList<>();
        private Dict inserted;

        void setMaxPriority(int maxPriority) {
            this.maxPriority = maxPriority;
        }

        void setTypeList(List<Dict> typeList) {
            this.typeList = typeList;
        }

        void setListByIdsResult(List<Dict> listByIdsResult) {
            this.listByIdsResult = listByIdsResult;
        }

        List<Integer> getUpdatedPriorities() {
            return updatedPriorities;
        }

        List<Long> getOrderedIdsAfterSort() {
            return orderedIdsAfterSort;
        }

        @Override
        public Dict getById(DictId id) {
            return null;
        }

        @Override
        public List<Dict> listByIds(List<Long> idList) {
            List<Dict> result = new ArrayList<>();
            for (Dict dict : listByIdsResult) {
                if (idList.contains(DictIdCodec.toValue(dict.getId()))) {
                    result.add(dict);
                }
            }
            return result;
        }

        @Override
        public List<Dict> list(String type, String label, String remarks) {
            return null;
        }

        @Override
        public com.baomidou.mybatisplus.extension.plugins.pagination.Page<Dict> page(
                String type, String label, String remarks, int pageNo, int pageSize) {
            return null;
        }

        @Override
        public int maxPriority() {
            return maxPriority;
        }

        @Override
        public List<Dict> listByType(String type, SortDirection sortDirection) {
            return typeList;
        }

        @Override
        public DictId insert(Dict entity) {
            inserted = entity;
            inserted.setId(DictIdCodec.toDomain(8001L));
            return inserted.getId();
        }

        @Override
        public int update(Dict dict) {
            return 1;
        }

        @Override
        public int updatePriority(Dict dict) {
            updatedPriorities.add(dict.getPriority());
            orderedIdsAfterSort.add(DictIdCodec.toValue(dict.getId()));
            return 1;
        }

        @Override
        public int deleteById(DictId id) {
            return 1;
        }

        @Override
        public List<String> listTypes() {
            return new ArrayList<>();
        }
    }
}
