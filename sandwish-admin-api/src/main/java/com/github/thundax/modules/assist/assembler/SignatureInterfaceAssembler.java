package com.github.thundax.modules.assist.assembler;

import com.github.thundax.common.persistence.DataEntity;
import com.github.thundax.common.persistence.Signable;
import com.github.thundax.modules.assist.entity.Signature;
import com.github.thundax.modules.assist.response.SignatureResponse;
import com.github.thundax.modules.assist.response.SignatureVerifyResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class SignatureInterfaceAssembler {

    @NonNull
    public SignatureResponse toResponse(Signature entity, Signable signable) {
        if (entity == null) {
            return new SignatureResponse();
        }

        SignatureResponse response = baseEntityToResponse(new SignatureResponse(), entity);
        response.setBusinessType(entity.getBusinessType());
        response.setBusinessId(entity.getBusinessId());
        response.setSignature(entity.getSignature());
        if (signable != null) {
            response.setBodyParams(signable.getSignBody());
        }
        return response;
    }

    @NonNull
    public SignatureVerifyResponse toVerifyResponse(Boolean verified) {
        SignatureVerifyResponse response = new SignatureVerifyResponse();
        response.setVerified(Boolean.TRUE.equals(verified));
        return response;
    }

    private static SignatureResponse baseEntityToResponse(SignatureResponse response, DataEntity entity) {
        response.setId(entity.getId());
        response.setRemarks(entity.getRemarks());
        response.setCreateDate(entity.getCreateDate());
        response.setUpdateDate(entity.getUpdateDate());
        response.setPriority(entity.getPriority());
        return response;
    }
}
