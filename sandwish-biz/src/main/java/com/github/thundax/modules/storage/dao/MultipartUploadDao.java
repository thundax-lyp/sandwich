package com.github.thundax.modules.storage.dao;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.storage.entity.MultipartUploadPart;
import com.github.thundax.modules.storage.entity.MultipartUploadSession;
import java.util.List;

public interface MultipartUploadDao {

    EntityId insertMultipartSession(MultipartUploadSession session);

    MultipartUploadSession getMultipartSessionByUploadId(String uploadId);

    int updateMultipartSession(MultipartUploadSession session);

    EntityId insertMultipartPart(MultipartUploadPart part);

    MultipartUploadPart getMultipartPart(String uploadId, Integer partNumber);

    List<MultipartUploadPart> listMultipartParts(String uploadId);

    int countMultipartParts(String uploadId);
}
