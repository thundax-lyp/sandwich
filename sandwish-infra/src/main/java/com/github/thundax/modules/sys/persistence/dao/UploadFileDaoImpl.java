package com.github.thundax.modules.sys.persistence.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.thundax.modules.sys.dao.UploadFileDao;
import com.github.thundax.modules.sys.entity.UploadFile;
import com.github.thundax.modules.sys.persistence.assembler.UploadFilePersistenceAssembler;
import com.github.thundax.modules.sys.persistence.dataobject.UploadFileDO;
import com.github.thundax.modules.sys.persistence.mapper.UploadFileMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class UploadFileDaoImpl implements UploadFileDao {

    private final UploadFileMapper mapper;

    public UploadFileDaoImpl(UploadFileMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public UploadFile get(String id) {
        return UploadFilePersistenceAssembler.toEntity(mapper.selectById(id));
    }

    @Override
    public List<UploadFile> getMany(List<String> idList) {
        return UploadFilePersistenceAssembler.toEntityList(mapper.selectBatchIds(idList));
    }

    @Override
    public List<UploadFile> findList() {
        return UploadFilePersistenceAssembler.toEntityList(mapper.selectList(buildListWrapper()));
    }

    @Override
    public Page<UploadFile> findPage(int pageNo, int pageSize) {
        Page<UploadFileDO> dataObjectPage = mapper.selectPage(new Page<>(pageNo, pageSize), buildListWrapper());
        Page<UploadFile> entityPage = new Page<>(dataObjectPage.getCurrent(), dataObjectPage.getSize());
        entityPage.setTotal(dataObjectPage.getTotal());
        entityPage.setRecords(UploadFilePersistenceAssembler.toEntityList(dataObjectPage.getRecords()));
        return entityPage;
    }

    @Override
    public int insert(UploadFile entity) {
        return mapper.insert(UploadFilePersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int update(UploadFile entity) {
        UploadFileDO dataObject = UploadFilePersistenceAssembler.toDataObject(entity);
        LambdaUpdateWrapper<UploadFileDO> wrapper = buildIdUpdateWrapper(dataObject);
        wrapper.set(UploadFileDO::getName, dataObject.getName())
                .set(UploadFileDO::getExtendName, dataObject.getExtendName())
                .set(UploadFileDO::getMimeType, dataObject.getMimeType())
                .set(UploadFileDO::getSize, dataObject.getSize())
                .set(UploadFileDO::getPath, dataObject.getPath());
        return mapper.update(null, wrapper);
    }

    @Override
    public int updatePriority(UploadFile entity) {
        return 0;
    }

    @Override
    public int updateDelFlag(UploadFile entity) {
        return 0;
    }

    @Override
    public int delete(String id) {
        return mapper.deleteById(id);
    }

    @Override
    public UploadFile getContent(String id) {
        return get(id);
    }

    @Override
    public List<UploadFile> findByFileIds(List<String> fileIds) {
        return UploadFilePersistenceAssembler.toEntityList(mapper.selectBatchIds(fileIds));
    }

    private LambdaQueryWrapper<UploadFileDO> buildListWrapper() {
        LambdaQueryWrapper<UploadFileDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(UploadFileDO::getCreateDate);
        return wrapper;
    }

    private LambdaUpdateWrapper<UploadFileDO> buildIdUpdateWrapper(UploadFileDO dataObject) {
        LambdaUpdateWrapper<UploadFileDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UploadFileDO::getId, dataObject.getId());
        return wrapper;
    }
}
