package com.github.thundax.modules.sys.assembler;

import com.github.thundax.modules.sys.controller.request.DictIdRequest;
import com.github.thundax.modules.sys.controller.request.DictPageRequest;
import com.github.thundax.modules.sys.controller.request.DictQueryRequest;
import com.github.thundax.modules.sys.controller.request.DictSaveRequest;
import com.github.thundax.modules.sys.controller.response.DictResponse;
import com.github.thundax.modules.sys.entity.Dict;
import com.github.thundax.modules.sys.entity.valueobject.DictId;
import com.github.thundax.modules.sys.entity.valueobject.DictIdCodec;
import com.github.thundax.modules.sys.service.command.ChangeDictInfoCommand;
import com.github.thundax.modules.sys.service.command.CreateDictCommand;
import com.github.thundax.modules.sys.service.command.DeleteDictCommand;
import com.github.thundax.modules.sys.service.query.DictQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;

public final class DictInterfaceAssembler {
    private DictInterfaceAssembler() {}

    @NonNull
    public static DictResponse toResponse(Dict entity) {
        if (entity == null) {
            return DictResponse.builder().build();
        }
        return DictResponse.builder()
                .id(DictIdCodec.toValue(entity.getId()))
                .remarks(entity.getRemarks())
                .priority(entity.getPriority())
                .label(entity.getLabel())
                .type(entity.getType())
                .value(entity.getValue())
                .build();
    }

    @NonNull
    public static DictId toId(@NonNull DictIdRequest request) {
        return DictIdCodec.toDomain(request.getId());
    }

    @NonNull
    public static DictQuery toQuery(@NonNull DictQueryRequest request) {
        DictQuery query = new DictQuery();
        query.setLabel(emptyToNull(request.getLabel()));
        query.setType(emptyToNull(request.getType()));
        query.setRemarks(emptyToNull(request.getRemarks()));
        return query;
    }

    @NonNull
    public static DictQuery toQuery(@NonNull DictPageRequest request) {
        DictQuery query = new DictQuery();
        query.setLabel(emptyToNull(request.getLabel()));
        query.setType(emptyToNull(request.getType()));
        query.setRemarks(emptyToNull(request.getRemarks()));
        return query;
    }

    @NonNull
    public static CreateDictCommand toCreateCommand(@NonNull DictSaveRequest request) {
        CreateDictCommand command = new CreateDictCommand();
        command.setPriority(request.getPriority());
        command.setRemarks(request.getRemarks());
        command.setLabel(request.getLabel());
        command.setType(request.getType());
        command.setValue(request.getValue());
        return command;
    }

    @NonNull
    public static ChangeDictInfoCommand toChangeInfoCommand(@NonNull DictSaveRequest request) {
        ChangeDictInfoCommand command = new ChangeDictInfoCommand();
        command.setId(DictIdCodec.toDomain(request.getId()));
        command.setPriority(request.getPriority());
        command.setRemarks(request.getRemarks());
        command.setLabel(request.getLabel());
        command.setType(request.getType());
        command.setValue(request.getValue());
        return command;
    }

    @NonNull
    public static DeleteDictCommand toDeleteCommand(@NonNull DictIdRequest request) {
        DeleteDictCommand command = new DeleteDictCommand();
        command.setId(DictIdCodec.toDomain(request.getId()));
        return command;
    }

    private static String emptyToNull(String value) {
        return StringUtils.isEmpty(value) ? null : value;
    }
}
