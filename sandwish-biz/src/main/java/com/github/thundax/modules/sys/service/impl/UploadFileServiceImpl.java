package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.page.PageDTO;
import com.github.thundax.common.page.PageRules;
import com.github.thundax.modules.sys.dao.UploadFileDao;
import com.github.thundax.modules.sys.entity.UploadFile;
import com.github.thundax.modules.sys.service.UploadFileService;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UploadFileServiceImpl implements UploadFileService {

    private final UploadFileDao dao;

    public UploadFileServiceImpl(UploadFileDao dao) {
        this.dao = dao;
    }

    public Class<UploadFile> getElementType() {
        return UploadFile.class;
    }

    public UploadFile newEntity(String id) {
        UploadFile uploadFile = new UploadFile();
        uploadFile.setId(EntityIdCodec.toDomain(id));
        return uploadFile;
    }

    public UploadFile getById(UploadFile entity) {
        return entity == null ? null : getById(entity.getId());
    }

    public UploadFile getById(EntityId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    public List<UploadFile> batchGetByIds(List<EntityId> ids) {
        return dao.batchGetByIds(EntityIdCodec.toValues(ids));
    }

    public List<UploadFile> list(UploadFile entity) {
        return dao.list();
    }

    public UploadFile getOne(UploadFile entity) {
        List<UploadFile> files = list(entity);
        return files == null || files.isEmpty() ? null : files.get(0);
    }

    public PageDTO<UploadFile> page(UploadFile entity, PageDTO<UploadFile> page) {
        PageDTO<UploadFile> normalizedPage = normalizePage(page);
        IPage<UploadFile> dataPage = dao.page(normalizedPage.getPageNo(), normalizedPage.getPageSize());
        normalizedPage.setPageNo((int) dataPage.getCurrent());
        normalizedPage.setPageSize((int) dataPage.getSize());
        normalizedPage.setCount(dataPage.getTotal());
        normalizedPage.setList(dataPage.getRecords());
        return normalizedPage;
    }

    public long count(UploadFile entity) {
        List<UploadFile> files = list(entity);
        return files == null ? 0 : files.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(UploadFile entity) {
        entity.setId(EntityIdCodec.toDomain(dao.insert(entity)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UploadFile entity) {
        dao.update(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteById(EntityId id) {
        return id == null ? 0 : dao.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteById(List<EntityId> ids) {
        return batchOperate(ids, this::deleteById);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updatePriority(UploadFile entity) {
        return dao.updatePriority(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updatePriority(List<UploadFile> list) {
        return batchOperate(list, this::updatePriority);
    }

    @Override
    public UploadFile getContent(UploadFile uploadFile) {
        return uploadFile == null ? null : dao.getContentById(uploadFile.getId());
    }

    @Override
    public List<UploadFile> batchGetByFileIds(String[] fileId) {
        return dao.batchGetByFileIds(Arrays.asList(fileId));
    }

    private <T> int batchOperate(Collection<T> collection, Function<T, Integer> operator) {
        int count = 0;
        if (collection != null && !collection.isEmpty()) {
            for (T entity : collection) {
                count += operator.apply(entity);
            }
        }
        return count;
    }

    private PageDTO<UploadFile> normalizePage(PageDTO<UploadFile> page) {
        PageDTO<UploadFile> normalizedPage = page == null ? new PageDTO<>() : page;
        if (normalizedPage.getPageNo() < PageRules.firstPageIndex()) {
            normalizedPage.setPageNo(PageRules.firstPageIndex());
        }
        if (normalizedPage.getPageSize() <= 0) {
            normalizedPage.setPageSize(PageRules.defaultPageSize());
        }
        return normalizedPage;
    }
}
