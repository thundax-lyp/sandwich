package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.persistence.Page;
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

    @Override
    public Class<UploadFile> getElementType() {
        return UploadFile.class;
    }

    @Override
    public UploadFile newEntity(String id) {
        UploadFile uploadFile = new UploadFile();
        uploadFile.setId(EntityIdCodec.toDomain(id));
        return uploadFile;
    }

    @Override
    public UploadFile getById(UploadFile entity) {
        return entity == null ? null : getById(entity.getId());
    }

    @Override
    public UploadFile getById(EntityId id) {
        if (id == null) {
            return null;
        }
        return dao.getById(id);
    }

    @Override
    public List<UploadFile> batchGetByIds(List<String> ids) {
        return dao.batchGetByIds(ids);
    }

    @Override
    public List<UploadFile> list(UploadFile entity) {
        return dao.list();
    }

    @Override
    public UploadFile getOne(UploadFile entity) {
        List<UploadFile> files = list(entity);
        return files == null || files.isEmpty() ? null : files.get(0);
    }

    @Override
    public Page<UploadFile> page(UploadFile entity, Page<UploadFile> page) {
        Page<UploadFile> normalizedPage = normalizePage(page);
        IPage<UploadFile> dataPage = dao.page(normalizedPage.getPageNo(), normalizedPage.getPageSize());
        normalizedPage.setPageNo((int) dataPage.getCurrent());
        normalizedPage.setPageSize((int) dataPage.getSize());
        normalizedPage.setCount(dataPage.getTotal());
        normalizedPage.setList(dataPage.getRecords());
        return normalizedPage;
    }

    @Override
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(UploadFile entity) {
        return entity == null ? 0 : dao.deleteById(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteById(List<UploadFile> list) {
        return batchOperate(list, this::deleteById);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePriority(UploadFile entity) {
        return dao.updatePriority(entity);
    }

    @Override
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

    private int batchOperate(Collection<UploadFile> collection, Function<UploadFile, Integer> operator) {
        int count = 0;
        if (collection != null && !collection.isEmpty()) {
            for (UploadFile uploadFile : collection) {
                count += operator.apply(uploadFile);
            }
        }
        return count;
    }

    private Page<UploadFile> normalizePage(Page<UploadFile> page) {
        Page<UploadFile> normalizedPage = page == null ? new Page<>() : page;
        if (normalizedPage.getPageNo() < Page.FIRST_PAGE_INDEX) {
            normalizedPage.setPageNo(Page.FIRST_PAGE_INDEX);
        }
        if (normalizedPage.getPageSize() <= 0) {
            normalizedPage.setPageSize(Page.DEFAULT_PAGE_SIZE);
        }
        return normalizedPage;
    }
}
