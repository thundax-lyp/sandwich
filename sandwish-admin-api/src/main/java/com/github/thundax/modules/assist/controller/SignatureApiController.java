package com.github.thundax.modules.assist.controller;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.persistence.Signable;
import com.github.thundax.common.vo.PageVo;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.modules.assist.api.SignatureServiceApi;
import com.github.thundax.modules.assist.assembler.SignatureInterfaceAssembler;
import com.github.thundax.modules.assist.entity.Signature;
import com.github.thundax.modules.assist.request.SignatureDeleteRequest;
import com.github.thundax.modules.assist.request.SignaturePageRequest;
import com.github.thundax.modules.assist.request.SignatureVerifyRequest;
import com.github.thundax.modules.assist.response.SignatureResponse;
import com.github.thundax.modules.assist.response.SignatureVerifyResponse;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.assist.service.SignatureService;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.utils.LogServiceHolder;
import com.github.thundax.modules.sys.utils.MenuServiceHolder;
import com.github.thundax.modules.sys.utils.RoleServiceHolder;
import com.github.thundax.modules.sys.utils.UserServiceHolder;
import java.util.List;
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** @author thundax */
@RestController
public class SignatureApiController extends BaseApiController implements SignatureServiceApi {

    private final SignatureService signatureService;
    private final SignService signService;
    private final SignatureInterfaceAssembler signatureInterfaceAssembler;

    @Autowired
    public SignatureApiController(
            SignatureService signatureService,
            SignService signService,
            Validator validator,
            SignatureInterfaceAssembler signatureInterfaceAssembler) {
        super(validator);

        this.signatureService = signatureService;
        this.signService = signService;
        this.signatureInterfaceAssembler = signatureInterfaceAssembler;
    }

    @Override
    @PreAuthorize("@permissionAuthorizationService.isPermitted('assist:signature:view')")
    public PageVo<SignatureResponse> page(@RequestBody SignaturePageRequest request)
            throws ApiException {
        validate(request);

        Signature query = new Signature();
        Signature.Query queryCondition = new Signature.Query();
        queryCondition.setBusinessType(request.getBusinessType());
        query.setQuery(queryCondition);

        return entityPageToVo(
                signatureService.findPage(query, readSignaturePage(request)),
                this::entityToResponse);
    }

    @Override
    @PreAuthorize("@permissionAuthorizationService.isPermitted('assist:signature:view')")
    public SignatureVerifyResponse verify(@RequestBody SignatureVerifyRequest request)
            throws ApiException {
        validate(request);
        Signature bean = signatureService.find(request.getBusinessType(), request.getBusinessId());

        if (bean == null) {
            return signatureInterfaceAssembler.toVerifyResponse(false);
        }

        Signable signable = findSignable(bean);
        if (signable == null) {
            return signatureInterfaceAssembler.toVerifyResponse(false);
        }

        return signatureInterfaceAssembler.toVerifyResponse(
                signService.verifySign(
                        signable.getSignName(), signable.getSignId(), signable.getSignBody()));
    }

    @Override
    @PreAuthorize("@permissionAuthorizationService.isPermitted('assist:signature:edit')")
    public Boolean delete(@RequestBody List<SignatureDeleteRequest> list) throws ApiException {
        List<Signature> beanList =
                validateList(
                        list,
                        vo -> signatureService.find(vo.getBusinessType(), vo.getBusinessId()));

        signatureService.delete(beanList);

        return true;
    }

    private Signable findSignable(Signature bean) {
        switch (bean.getBusinessType()) {
            case Log.BEAN_NAME:
                return LogServiceHolder.get(bean.getBusinessId());
            case User.BEAN_NAME:
                return UserServiceHolder.get(bean.getBusinessId());
            case Menu.BEAN_NAME:
                return MenuServiceHolder.get(bean.getBusinessId());
            case Role.BEAN_NAME:
                return RoleServiceHolder.get(bean.getBusinessId());
            default:
                return null;
        }
    }

    private SignatureResponse entityToResponse(Signature entity) {
        return signatureInterfaceAssembler.toResponse(entity, findSignable(entity));
    }

    private Page<Signature> readSignaturePage(SignaturePageRequest request) {
        Integer pageNo = request.getPageNo();
        Integer pageSize = request.getPageSize();

        if (pageNo == null || pageNo < Page.FIRST_PAGE_INDEX) {
            pageNo = Page.FIRST_PAGE_INDEX;
        }

        if (pageSize == null || pageSize <= 0) {
            pageSize = Page.DEFAULT_PAGE_SIZE;
        }

        Page<Signature> page = new Page<>();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        return page;
    }
}
