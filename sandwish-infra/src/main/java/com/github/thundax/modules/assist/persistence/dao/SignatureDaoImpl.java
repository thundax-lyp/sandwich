package com.github.thundax.modules.assist.persistence.dao;

import com.github.thundax.modules.assist.dao.SignatureDao;
import com.github.thundax.modules.assist.entity.Signature;
import com.github.thundax.modules.assist.persistence.assembler.SignaturePersistenceAssembler;
import com.github.thundax.modules.assist.persistence.dataobject.SignatureDO;
import com.github.thundax.modules.assist.persistence.mapper.SignatureMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 签名 DAO 实现。
 */
@Repository
public class SignatureDaoImpl implements SignatureDao {

    private final SignatureMapper mapper;

    public SignatureDaoImpl(SignatureMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Signature get(Signature entity) {
        return SignaturePersistenceAssembler.toEntity(mapper.get(SignaturePersistenceAssembler.toDataObject(entity)));
    }

    @Override
    public List<Signature> getMany(List<String> idList) {
        return SignaturePersistenceAssembler.toEntityList(mapper.getMany(idList));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Signature> findList(Signature entity) {
        List<SignatureDO> dataObjects = mapper.findList(SignaturePersistenceAssembler.toDataObject(entity));
        List<Signature> entities = SignaturePersistenceAssembler.toEntityList(dataObjects);
        if (dataObjects instanceof com.github.pagehelper.Page) {
            List rawPage = (List) dataObjects;
            rawPage.clear();
            rawPage.addAll(entities);
            return rawPage;
        }
        return entities;
    }

    @Override
    public int insert(Signature entity) {
        return mapper.insert(SignaturePersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int update(Signature entity) {
        return mapper.update(SignaturePersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int updatePriority(Signature entity) {
        return 0;
    }

    public int updateStatus(Signature entity) {
        return 0;
    }

    @Override
    public int updateDelFlag(Signature entity) {
        return 0;
    }

    @Override
    public int delete(Signature entity) {
        return mapper.delete(SignaturePersistenceAssembler.toDataObject(entity));
    }

    @Override
    public int insertOrUpdate(Signature entity) {
        return mapper.insertOrUpdate(SignaturePersistenceAssembler.toDataObject(entity));
    }
}
