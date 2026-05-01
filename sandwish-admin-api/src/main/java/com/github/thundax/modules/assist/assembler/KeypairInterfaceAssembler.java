package com.github.thundax.modules.assist.assembler;

import com.github.thundax.modules.assist.response.KeypairPublicKeyResponse;
import org.springframework.lang.NonNull;

public final class KeypairInterfaceAssembler {
    private KeypairInterfaceAssembler() {}

    @NonNull
    public static KeypairPublicKeyResponse toPublicKeyResponse(String publicKey) {
        KeypairPublicKeyResponse response = new KeypairPublicKeyResponse();
        response.setPublicKey(publicKey);
        return response;
    }
}
