package com.github.thundax.modules.storage.service;

import com.github.thundax.common.arch.LayerPublicApi;
import com.github.thundax.modules.storage.entity.MultipartUploadPart;
import com.github.thundax.modules.storage.entity.MultipartUploadSession;
import com.github.thundax.modules.storage.entity.StoredObject;
import com.github.thundax.modules.storage.service.command.AbortMultipartUploadCommand;
import com.github.thundax.modules.storage.service.command.CompleteMultipartUploadCommand;
import com.github.thundax.modules.storage.service.command.InitMultipartUploadCommand;
import com.github.thundax.modules.storage.service.command.UploadMultipartPartCommand;

public interface MultipartUploadService {

    @LayerPublicApi(reason = "分片上传流程初始化会话的业务入口")
    MultipartUploadSession init(InitMultipartUploadCommand command);

    @LayerPublicApi(reason = "分片上传流程写入单个分片的业务入口")
    MultipartUploadPart uploadPart(UploadMultipartPartCommand command);

    @LayerPublicApi(reason = "分片上传流程合并并生成存储对象的业务入口")
    StoredObject complete(CompleteMultipartUploadCommand command);

    @LayerPublicApi(reason = "分片上传流程取消会话的业务入口")
    int abort(AbortMultipartUploadCommand command);
}
