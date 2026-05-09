package com.github.thundax.modules.storage.dao;

import com.github.thundax.modules.storage.entity.MultipartUploadPart;
import com.github.thundax.modules.storage.entity.MultipartUploadSession;
import com.github.thundax.modules.storage.entity.valueobject.MultipartUploadPartId;
import com.github.thundax.modules.storage.entity.valueobject.MultipartUploadSessionId;
import java.util.List;

public interface MultipartUploadDao {

    MultipartUploadSessionId insertMultipartSession(MultipartUploadSession session);

    MultipartUploadSession getMultipartSessionByUploadId(String uploadId);

    int updateMultipartSession(MultipartUploadSession session);

    MultipartUploadPartId insertMultipartPart(MultipartUploadPart part);

    MultipartUploadPart getMultipartPart(String uploadId, Integer partNumber);

    List<MultipartUploadPart> listMultipartParts(String uploadId);

    int countMultipartParts(String uploadId);
}
