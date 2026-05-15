package com.github.thundax.modules.open.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.thundax.common.arch.OneLineMethodAllowed;
import com.github.thundax.common.exception.BizException;
import com.github.thundax.common.exception.BizExceptionBoundary;
import com.github.thundax.common.id.UuidHelper;
import com.github.thundax.common.jasypt.JasyptStringEncryptor;
import com.github.thundax.common.page.PageQuery;
import com.github.thundax.common.page.PageResult;
import com.github.thundax.modules.auth.dao.PrincipalCredentialDao;
import com.github.thundax.modules.auth.dao.PrincipalIdentityDao;
import com.github.thundax.modules.auth.entity.PrincipalCredential;
import com.github.thundax.modules.auth.entity.PrincipalIdentity;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalCredentialType;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityStatus;
import com.github.thundax.modules.auth.entity.enums.PrincipalIdentityType;
import com.github.thundax.modules.auth.entity.enums.PrincipalType;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalIdentityId;
import com.github.thundax.modules.auth.entity.valueobject.PrincipalKey;
import com.github.thundax.modules.open.dao.OpenClientDao;
import com.github.thundax.modules.open.entity.OpenClient;
import com.github.thundax.modules.open.entity.OpenClientPermission;
import com.github.thundax.modules.open.entity.enums.OpenClientStatus;
import com.github.thundax.modules.open.entity.valueobject.OpenClientId;
import com.github.thundax.modules.open.service.OpenClientService;
import com.github.thundax.modules.open.service.command.ChangeOpenClientStatusCommand;
import com.github.thundax.modules.open.service.command.CreateOpenClientCommand;
import com.github.thundax.modules.open.service.command.ResetOpenClientSecretCommand;
import com.github.thundax.modules.open.service.command.UpdateOpenClientCommand;
import com.github.thundax.modules.open.service.dto.OpenClientDTO;
import com.github.thundax.modules.open.service.query.OpenClientQuery;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@BizExceptionBoundary
public class OpenClientServiceImpl implements OpenClientService {

    private static final String API_KEY_PREFIX = "swak_";
    private static final String API_SECRET_PREFIX = "swas_";

    private final OpenClientDao openClientDao;
    private final PrincipalIdentityDao principalIdentityDao;
    private final PrincipalCredentialDao principalCredentialDao;
    private final JasyptStringEncryptor secretEncryptor = new JasyptStringEncryptor();

    public OpenClientServiceImpl(
            OpenClientDao openClientDao,
            PrincipalIdentityDao principalIdentityDao,
            PrincipalCredentialDao principalCredentialDao) {
        this.openClientDao = openClientDao;
        this.principalIdentityDao = principalIdentityDao;
        this.principalCredentialDao = principalCredentialDao;
    }

    @Override
    public OpenClientDTO get(OpenClientId id) {
        if (id == null) {
            return null;
        }
        return toDTO(openClientDao.getById(id), null, null);
    }

    @Override
    public PageResult<OpenClientDTO> page(OpenClientQuery query, PageQuery page) {
        PageQuery effectivePage = page == null ? new PageQuery() : page;
        effectivePage.normalize();
        String status = query == null || query.getStatus() == null
                ? null
                : query.getStatus().value();
        IPage<OpenClient> dataPage = openClientDao.page(
                query == null ? null : query.getName(), status, effectivePage.getPageNo(), effectivePage.getPageSize());
        List<OpenClientDTO> records = dataPage.getRecords().stream()
                .map(item -> toDTO(item, null, null))
                .collect(Collectors.toList());
        return PageResult.of((int) dataPage.getCurrent(), (int) dataPage.getSize(), dataPage.getTotal(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OpenClientDTO create(CreateOpenClientCommand command) {
        validateCreate(command);

        OpenClient client = new OpenClient();
        client.setName(command.getName());
        client.setStatus(OpenClientStatus.ENABLED);
        client.setIpWhitelist(command.getIpWhitelist());
        client.setExpiredAt(command.getExpiredAt());
        client.setRemarks(command.getRemarks());
        client.setId(openClientDao.insert(client));

        String apiKey = generateApiKey();
        String apiSecret = generateApiSecret();
        PrincipalIdentity identity = createIdentity(client.getId(), apiKey);
        createCredential(client.getId(), identity.getId(), apiSecret);
        replacePermissions(client.getId(), command.getPermissions());
        return toDTO(client, apiKey, apiSecret);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OpenClientDTO change(UpdateOpenClientCommand command) {
        validateUpdate(command);

        OpenClient client = requireClient(command.getId());
        client.setName(command.getName());
        client.setStatus(command.getStatus() == null ? client.getStatus() : command.getStatus());
        client.setIpWhitelist(command.getIpWhitelist());
        client.setExpiredAt(command.getExpiredAt());
        client.setRemarks(command.getRemarks());
        openClientDao.update(client);
        replacePermissions(client.getId(), command.getPermissions());
        return toDTO(client, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(ChangeOpenClientStatusCommand command) {
        if (command == null || command.getId() == null || command.getStatus() == null) {
            throw invalidParameter("OpenClient status parameter is invalid");
        }
        OpenClient client = requireClient(command.getId());
        client.setStatus(command.getStatus());
        openClientDao.updateStatus(client);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OpenClientDTO resetSecret(ResetOpenClientSecretCommand command) {
        if (command == null || command.getId() == null) {
            throw invalidParameter("OpenClient secret reset parameter is invalid");
        }

        OpenClient client = requireClient(command.getId());
        PrincipalKey principalKey = principalKey(client.getId());
        PrincipalIdentity identity =
                principalIdentityDao.getByPrincipalKeyAndType(principalKey, PrincipalIdentityType.API_KEY);
        if (identity == null || identity.getId() == null) {
            throw invalidParameter("OpenClient API key does not exist");
        }

        String apiSecret = generateApiSecret();
        PrincipalCredential credential =
                principalCredentialDao.getByIdentityIdAndType(identity.getId(), PrincipalCredentialType.API_SECRET);
        if (credential == null) {
            createCredential(client.getId(), identity.getId(), apiSecret);
        } else {
            credential.setCredentialValue(secretEncryptor.encrypt(apiSecret));
            credential.setStatus(PrincipalCredentialStatus.ACTIVE);
            principalCredentialDao.update(credential);
        }
        return toDTO(client, identity.getIdentityValue(), apiSecret);
    }

    private OpenClient requireClient(OpenClientId id) {
        if (id == null) {
            throw invalidParameter("OpenClient id is required");
        }
        OpenClient client = openClientDao.getById(id);
        if (client == null) {
            throw invalidParameter("OpenClient does not exist");
        }
        return client;
    }

    private PrincipalIdentity createIdentity(OpenClientId clientId, String apiKey) {
        PrincipalIdentity identity = new PrincipalIdentity();
        identity.setPrincipalKey(principalKey(clientId));
        identity.setType(PrincipalIdentityType.API_KEY);
        identity.setIdentityValue(apiKey);
        identity.setStatus(PrincipalIdentityStatus.ENABLED);
        PrincipalIdentityId identityId = principalIdentityDao.insert(identity);
        identity.setId(identityId);
        return identity;
    }

    private void createCredential(OpenClientId clientId, PrincipalIdentityId identityId, String apiSecret) {
        PrincipalCredential credential = new PrincipalCredential();
        credential.setPrincipalKey(principalKey(clientId));
        credential.setIdentityId(identityId);
        credential.setCredentialType(PrincipalCredentialType.API_SECRET);
        credential.setCredentialValue(secretEncryptor.encrypt(apiSecret));
        credential.setStatus(PrincipalCredentialStatus.ACTIVE);
        principalCredentialDao.insert(credential);
    }

    private void replacePermissions(OpenClientId clientId, List<String> permissions) {
        openClientDao.deleteByClientId(clientId);
        List<OpenClientPermission> normalizedPermissions = toPermissions(clientId, permissions);
        if (!normalizedPermissions.isEmpty()) {
            openClientDao.batchInsertPermissions(normalizedPermissions);
        }
    }

    private OpenClientDTO toDTO(OpenClient client, String apiKey, String apiSecret) {
        if (client == null) {
            return null;
        }
        return new OpenClientDTO(
                client.getId(),
                client.getName(),
                client.getStatus(),
                apiKey,
                apiSecret,
                client.getIpWhitelist(),
                client.getExpiredAt(),
                client.getRemarks(),
                listPermissionValues(client.getId()));
    }

    private List<String> listPermissionValues(OpenClientId clientId) {
        List<OpenClientPermission> permissions = openClientDao.listPermissionsByClientId(clientId);
        if (permissions == null) {
            return Collections.emptyList();
        }
        return permissions.stream()
                .map(OpenClientPermission::getPermission)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    private static List<OpenClientPermission> toPermissions(OpenClientId clientId, List<String> permissions) {
        Set<String> normalizedValues = new LinkedHashSet<>();
        if (permissions != null) {
            for (String permission : permissions) {
                if (StringUtils.isNotBlank(permission)) {
                    normalizedValues.add(permission.trim());
                }
            }
        }

        List<OpenClientPermission> result = new ArrayList<>();
        for (String permission : normalizedValues) {
            OpenClientPermission clientPermission = new OpenClientPermission();
            clientPermission.setClientId(clientId);
            clientPermission.setPermission(permission);
            result.add(clientPermission);
        }
        return result;
    }

    private static PrincipalKey principalKey(OpenClientId clientId) {
        return PrincipalKey.of(PrincipalType.OPEN_CLIENT, clientId.value());
    }

    private static void validateCreate(CreateOpenClientCommand command) {
        if (command == null || StringUtils.isBlank(command.getName())) {
            throw invalidParameter("OpenClient create parameter is invalid");
        }
    }

    private static void validateUpdate(UpdateOpenClientCommand command) {
        if (command == null || command.getId() == null || StringUtils.isBlank(command.getName())) {
            throw invalidParameter("OpenClient update parameter is invalid");
        }
    }

    @OneLineMethodAllowed(reason = "表达 API KEY 生成前缀和随机值组合")
    private static String generateApiKey() {
        return API_KEY_PREFIX + UuidHelper.compact();
    }

    private static String generateApiSecret() {
        return API_SECRET_PREFIX + UuidHelper.compact() + UuidHelper.compact();
    }

    private static BizException invalidParameter(String message) {
        return new BizException("OPEN-00001", "open.exception.invalid-parameter", message);
    }
}
