package com.github.thundax.modules.assist.assembler;

import com.github.thundax.common.domain.Signable;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.modules.assist.entity.Signature;
import com.github.thundax.modules.assist.request.SignaturePageRequest;
import com.github.thundax.modules.assist.response.SignatureResponse;
import com.github.thundax.modules.assist.response.SignatureVerifyResponse;
import com.github.thundax.modules.assist.service.query.SignatureQuery;
import org.springframework.lang.NonNull;

public final class SignatureInterfaceAssembler {
    private SignatureInterfaceAssembler() {}

    @NonNull
    public static SignatureResponse toResponse(Signature entity, Signable signable) {
        if (entity == null) {
            return new SignatureResponse();
        }
        SignatureResponse response = new SignatureResponse();
        response.setId(EntityIdCodec.toValue(entity.getId()));
        response.setRemarks(entity.getRemarks());
        response.setCreateDate(entity.getCreateDate());
        response.setUpdateDate(entity.getUpdateDate());
        response.setPriority(entity.getPriority());
        response.setBusinessType(entity.getBusinessType());
        response.setBusinessId(entity.getBusinessId());
        response.setSignature(entity.getSignature());
        if (signable != null) {
            response.setBodyParams(signable.getSignBody());
        }
        return response;
    }

    @NonNull
    public static SignatureVerifyResponse toVerifyResponse(Boolean verified) {
        SignatureVerifyResponse response = new SignatureVerifyResponse();
        response.setVerified(Boolean.TRUE.equals(verified));
        return response;
    }

    @NonNull
    public static SignatureQuery toQuery(@NonNull SignaturePageRequest request) {
        SignatureQuery query = new SignatureQuery();
        query.setBusinessType(request.getBusinessType());
        return query;
    }
}
