package com.github.thundax.modules.assist.controller;

import com.github.thundax.common.Constants;
import com.github.thundax.common.domain.Signable;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.id.EntityIdCodec;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.vo.PageVo;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.modules.assist.assembler.SignatureInterfaceAssembler;
import com.github.thundax.modules.assist.entity.Signature;
import com.github.thundax.modules.assist.request.SignatureDeleteRequest;
import com.github.thundax.modules.assist.request.SignaturePageRequest;
import com.github.thundax.modules.assist.request.SignatureVerifyRequest;
import com.github.thundax.modules.assist.response.SignatureResponse;
import com.github.thundax.modules.assist.response.SignatureVerifyResponse;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.assist.service.SignatureService;
import com.github.thundax.modules.sys.aop.annotation.SysLogger;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.LogService;
import com.github.thundax.modules.sys.service.MenuService;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "08-04.辅助-签名与验签")
@SysLogger(module = {"辅助", "签名"})
@RequestMapping(value = "/api/assist/signature")
@RestController
public class SignatureApiController extends BaseApiController {

    private final SignatureService signatureService;
    private final SignService signService;
    private final LogService logService;
    private final UserService userService;
    private final MenuService menuService;
    private final RoleService roleService;

    @Autowired
    public SignatureApiController(
            SignatureService signatureService,
            SignService signService,
            LogService logService,
            UserService userService,
            MenuService menuService,
            RoleService roleService) {

        this.signatureService = signatureService;
        this.signService = signService;
        this.logService = logService;
        this.userService = userService;
        this.menuService = menuService;
        this.roleService = roleService;
    }

    @ApiOperation(value = "获取列表", notes = "assist:signature:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("列表")
    @RequestMapping(value = "page", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('assist:signature:view')")
    public PageVo<SignatureResponse> page(@Valid @RequestBody SignaturePageRequest request) throws ApiException {
        return entityPageToVo(
                signatureService.page(request.getBusinessType(), readSignaturePage(request)), this::entityToResponse);
    }

    @ApiOperation(value = "校验", notes = "assist:signature:view")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("校验")
    @RequestMapping(value = "verify", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('assist:signature:view')")
    public SignatureVerifyResponse verify(@Valid @RequestBody SignatureVerifyRequest request) throws ApiException {
        Signature bean = signatureService.getByBusiness(request.getBusinessType(), request.getBusinessId());

        if (bean == null) {
            return SignatureInterfaceAssembler.toVerifyResponse(false);
        }

        Signable signable = findSignable(bean);
        if (signable == null) {
            return SignatureInterfaceAssembler.toVerifyResponse(false);
        }

        return SignatureInterfaceAssembler.toVerifyResponse(
                signService.verifySign(signable.getSignName(), signable.getSignId(), signable.getSignBody()));
    }

    @ApiOperation(value = "删除", notes = "assist:signature:edit")
    @ApiImplicitParams({
        @ApiImplicitParam(
                name = Constants.HEADER_TOKEN,
                value = "令牌",
                paramType = "header",
                dataTypeClass = String.class),
    })
    @SysLogger("删除")
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    @PreAuthorize("@permissionAuthorizationService.isPermitted('assist:signature:edit')")
    public Boolean delete(@RequestBody List<SignatureDeleteRequest> list) throws ApiException {
        List<Signature> beanList =
                validateList(list, vo -> signatureService.getByBusiness(vo.getBusinessType(), vo.getBusinessId()));

        signatureService.batchDeleteByBusiness(beanList);

        return true;
    }

    private Signable findSignable(Signature bean) {
        switch (bean.getBusinessType()) {
            case Log.BEAN_NAME:
                return logService.getById(EntityIdCodec.toDomain(bean.getBusinessId()));
            case User.BEAN_NAME:
                return userService.getById(EntityIdCodec.toDomain(bean.getBusinessId()));
            case Menu.BEAN_NAME:
                return menuService.getById(EntityIdCodec.toDomain(bean.getBusinessId()));
            case Role.BEAN_NAME:
                return roleService.getById(EntityIdCodec.toDomain(bean.getBusinessId()));
            default:
                return null;
        }
    }

    private SignatureResponse entityToResponse(Signature entity) {
        return SignatureInterfaceAssembler.toResponse(entity, findSignable(entity));
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
