package com.github.thundax.modules.sys.persistence.dao;

import com.github.pagehelper.Page;
import com.github.thundax.modules.sys.dao.UploadFileDao;
import com.github.thundax.modules.sys.entity.UploadFile;
import com.github.thundax.modules.sys.persistence.assembler.UploadFilePersistenceAssembler;
import com.github.thundax.modules.sys.persistence.dataobject.UploadFileDO;
import com.github.thundax.modules.sys.persistence.mapper.UploadFileMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

/** 上传文件 DAO 实现。 */
@Repository
public class UploadFileDaoImpl implements UploadFileDao {

    private final UploadFileMapper mapper;

    public UploadFileDaoImpl(UploadFileMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public UploadFile get(UploadFile entity) {
        return UploadFilePersistenceAssembler.toEntity(
                mapper.get(UploadFilePersistenceAssembler.toDataObject(entity)));
    }

    @Override
    public List<UploadFile> getMany(List<String> idList) {
        return UploadFilePersistenceAssembler.toEntityList(mapper.getMany(idList));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<UploadFile> findList(UploadFile entity) {
        List<UploadFileDO> dataObjects =
                mapper.findList(UploadFilePersistenceAssembler.toDataObject(entity));
        List<UploadFile> entities = UploadFilePersistenceAssembler.toEntityList(dataObjects);
        if (dataObjects instanceof Page) {
            List rawPage = (List) dataObjects;
            rawPage.clear();
            rawPage.addAll(entities);
            return rawPage;
        }
        return entities;
    }

    @Override
    public int insert(UploadFile entity) {
        return mapper.insert(UploadFilePersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int update(UploadFile entity) {
        return mapper.update(UploadFilePersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int updatePriority(UploadFile entity) {
        return 0;
    }

    public int updateStatus(UploadFile entity) {
        return 0;
    }

    @Override
    public int updateDelFlag(UploadFile entity) {
        return 0;
    }

    @Override
    public int delete(UploadFile entity) {
        return mapper.delete(UploadFilePersistenceAssembler.toDataObject(entity));
    }

    @Override
    public UploadFile getContent(UploadFile uploadFile) {
        return UploadFilePersistenceAssembler.toEntity(
                mapper.getContent(UploadFilePersistenceAssembler.toDataObject(uploadFile)));
    }

    @Override
    public List<UploadFile> findByFileIds(String[] fileId) {
        return UploadFilePersistenceAssembler.toEntityList(mapper.findByFileIds(fileId));
    }
}
