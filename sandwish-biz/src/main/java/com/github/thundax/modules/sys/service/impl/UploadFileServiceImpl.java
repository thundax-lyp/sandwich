package com.github.thundax.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.modules.sys.dao.UploadFileDao;
import com.github.thundax.modules.sys.entity.UploadFile;
import com.github.thundax.modules.sys.service.UploadFileService;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
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
        return new UploadFile(id);
    }

    @Override
    public UploadFile get(UploadFile entity) {
        return entity == null ? null : get(entity.getId());
    }

    @Override
    public UploadFile get(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return dao.get(id);
    }

    @Override
    public List<UploadFile> getMany(List<String> ids) {
        return dao.getMany(ids);
    }

    @Override
    public List<UploadFile> findList(UploadFile entity) {
        return dao.findList();
    }

    @Override
    public UploadFile findOne(UploadFile entity) {
        List<UploadFile> files = findList(entity);
        return files == null || files.isEmpty() ? null : files.get(0);
    }

    @Override
    public Page<UploadFile> findPage(UploadFile entity, Page<UploadFile> page) {
        Page<UploadFile> normalizedPage = normalizePage(page);
        IPage<UploadFile> dataPage = dao.findPage(normalizedPage.getPageNo(), normalizedPage.getPageSize());
        normalizedPage.setPageNo((int) dataPage.getCurrent());
        normalizedPage.setPageSize((int) dataPage.getSize());
        normalizedPage.setCount(dataPage.getTotal());
        normalizedPage.setList(dataPage.getRecords());
        return normalizedPage;
    }

    @Override
    public long count(UploadFile entity) {
        List<UploadFile> files = findList(entity);
        return files == null ? 0 : files.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(UploadFile entity) {
        entity.preInsert();
        entity.setId(dao.insert(entity));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UploadFile entity) {
        entity.preUpdate();
        dao.update(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(UploadFile entity) {
        return entity == null ? 0 : dao.delete(entity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(List<UploadFile> list) {
        return batchOperate(list, this::delete);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePriority(UploadFile entity) {
        entity.preUpdate();
        return dao.updatePriority(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePriority(List<UploadFile> list) {
        return batchOperate(list, this::updatePriority);
    }

    @Override
    public UploadFile getContent(UploadFile uploadFile) {
        return uploadFile == null ? null : dao.getContent(uploadFile.getId());
    }

    @Override
    public List<UploadFile> findByFileIds(String[] fileId) {
        return dao.findByFileIds(Arrays.asList(fileId));
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
