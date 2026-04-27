package com.github.thundax.modules.assist.assembler;

import com.github.thundax.modules.assist.response.KeypairPublicKeyResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class KeypairInterfaceAssembler {

    @NonNull
    public KeypairPublicKeyResponse toPublicKeyResponse(String publicKey) {
        KeypairPublicKeyResponse response = new KeypairPublicKeyResponse();
        response.setPublicKey(publicKey);
        return response;
    }
}
