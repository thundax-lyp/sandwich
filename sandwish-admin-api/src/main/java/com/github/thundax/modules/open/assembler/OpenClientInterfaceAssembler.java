package com.github.thundax.modules.open.assembler;

import com.github.thundax.modules.open.controller.request.OpenClientPageRequest;
import com.github.thundax.modules.open.controller.request.OpenClientSaveRequest;
import com.github.thundax.modules.open.controller.request.OpenClientStatusRequest;
import com.github.thundax.modules.open.controller.response.OpenClientResponse;
import com.github.thundax.modules.open.controller.response.OpenClientSecretResponse;
import com.github.thundax.modules.open.entity.enums.OpenClientStatus;
import com.github.thundax.modules.open.entity.valueobject.OpenClientIdCodec;
import com.github.thundax.modules.open.service.command.ChangeOpenClientStatusCommand;
import com.github.thundax.modules.open.service.command.CreateOpenClientCommand;
import com.github.thundax.modules.open.service.command.UpdateOpenClientCommand;
import com.github.thundax.modules.open.service.dto.OpenClientDTO;
import com.github.thundax.modules.open.service.query.OpenClientQuery;
import java.util.Collections;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class OpenClientInterfaceAssembler {

    private OpenClientInterfaceAssembler() {}

    @NonNull
    public static OpenClientQuery toQuery(@NonNull OpenClientPageRequest request) {
        OpenClientQuery query = new OpenClientQuery();
        query.setName(request.getName());
        query.setStatus(StringUtils.isBlank(request.getStatus()) ? null : OpenClientStatus.from(request.getStatus()));
        return query;
    }

    @NonNull
    public static CreateOpenClientCommand toCreateCommand(@NonNull OpenClientSaveRequest request) {
        CreateOpenClientCommand command = new CreateOpenClientCommand();
        command.setName(request.getName());
        command.setIpWhitelist(request.getIpWhitelist());
        command.setExpiredAt(request.getExpiredAt());
        command.setRemarks(request.getRemarks());
        command.setPermissions(request.getPermissions());
        return command;
    }

    @NonNull
    public static UpdateOpenClientCommand toUpdateCommand(@NonNull OpenClientSaveRequest request) {
        UpdateOpenClientCommand command = new UpdateOpenClientCommand();
        command.setId(OpenClientIdCodec.toDomain(request.getId()));
        command.setName(request.getName());
        command.setIpWhitelist(request.getIpWhitelist());
        command.setExpiredAt(request.getExpiredAt());
        command.setRemarks(request.getRemarks());
        command.setPermissions(request.getPermissions());
        return command;
    }

    @NonNull
    public static ChangeOpenClientStatusCommand toChangeStatusCommand(@NonNull OpenClientStatusRequest request) {
        ChangeOpenClientStatusCommand command = new ChangeOpenClientStatusCommand();
        command.setId(OpenClientIdCodec.toDomain(request.getId()));
        command.setStatus(OpenClientStatus.from(request.getStatus()));
        return command;
    }

    @NonNull
    public static OpenClientResponse toResponse(OpenClientDTO dto) {
        if (dto == null) {
            return OpenClientResponse.builder()
                    .permissions(Collections.emptyList())
                    .build();
        }
        return OpenClientResponse.builder()
                .id(OpenClientIdCodec.toStringValue(dto.getId()))
                .name(dto.getName())
                .status(dto.getStatus() == null ? null : dto.getStatus().value())
                .apiKey(dto.getApiKey())
                .ipWhitelist(dto.getIpWhitelist())
                .expiredAt(dto.getExpiredAt())
                .remarks(dto.getRemarks())
                .permissions(dto.getPermissions() == null ? Collections.emptyList() : dto.getPermissions())
                .build();
    }

    @NonNull
    public static OpenClientSecretResponse toSecretResponse(OpenClientDTO dto) {
        if (dto == null) {
            return OpenClientSecretResponse.builder().build();
        }
        return OpenClientSecretResponse.builder()
                .id(OpenClientIdCodec.toStringValue(dto.getId()))
                .apiKey(dto.getApiKey())
                .apiSecret(dto.getApiSecret())
                .build();
    }
}
