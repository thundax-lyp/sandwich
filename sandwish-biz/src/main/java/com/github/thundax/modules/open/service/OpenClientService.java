package com.github.thundax.modules.open.service;

import com.github.thundax.common.arch.LayerPublicApi;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.modules.open.entity.valueobject.OpenClientId;
import com.github.thundax.modules.open.service.command.ChangeOpenClientStatusCommand;
import com.github.thundax.modules.open.service.command.CreateOpenClientCommand;
import com.github.thundax.modules.open.service.command.ResetOpenClientSecretCommand;
import com.github.thundax.modules.open.service.command.UpdateOpenClientCommand;
import com.github.thundax.modules.open.service.dto.OpenClientDTO;
import com.github.thundax.modules.open.service.query.OpenClientQuery;

public interface OpenClientService {

    OpenClientDTO get(OpenClientId id);

    PageResult<OpenClientDTO> page(OpenClientQuery query, PageQuery page);

    OpenClientDTO create(CreateOpenClientCommand command);

    OpenClientDTO change(UpdateOpenClientCommand command);

    void changeStatus(ChangeOpenClientStatusCommand command);

    @LayerPublicApi(reason = "开放客户端管理台重置 API SECRET 时返回一次性明文的业务入口")
    OpenClientDTO resetSecret(ResetOpenClientSecretCommand command);
}
