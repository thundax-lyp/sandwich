package com.github.thundax.modules.assist.controller;

import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.persistence.Signable;
import com.github.thundax.common.vo.PageVo;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.modules.assist.api.SignatureServiceApi;
import com.github.thundax.modules.assist.api.query.SignatureQueryParam;
import com.github.thundax.modules.assist.api.vo.SignatureVo;
import com.github.thundax.modules.assist.entity.Signature;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.assist.service.SignatureService;
import com.github.thundax.modules.auth.security.annotation.RequiresPermissions;
import com.github.thundax.modules.sys.entity.Log;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.utils.LogServiceHolder;
import com.github.thundax.modules.sys.utils.MenuServiceHolder;
import com.github.thundax.modules.sys.utils.RoleServiceHolder;
import com.github.thundax.modules.sys.utils.UserServiceHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Validator;
import java.util.List;

/**
 * @author thundax
 */
@RestController
public class SignatureApiController extends BaseApiController implements SignatureServiceApi {

    private final SignatureService signatureService;
    private final SignService signService;

    @Autowired
    public SignatureApiController(SignatureService signatureService,
                                  SignService signService,
                                  Validator validator) {
        super(validator);

        this.signatureService = signatureService;
        this.signService = signService;
    }


    @Override
    @RequiresPermissions("assist:signature:view")
    public PageVo<SignatureVo> page(@RequestBody SignatureQueryParam queryParam) throws ApiException {
        validate(queryParam);

        Signature query = new Signature();
        query.setQueryProp(Signature.Query.PROP_BUSINESS_TYPE, queryParam.getBusinessType());

        return entityPageToVo(signatureService.findPage(query, readPage(queryParam)), this::entityToVo);
    }


    @Override
    @RequiresPermissions("assist:signature:view")
    public Boolean verify(@RequestBody SignatureVo vo) throws ApiException {
        Signature bean = signatureService.find(vo.getBusinessType(), vo.getBusinessId());

        if (bean == null) {
            return false;
        }

        Signable signable = findSignable(bean);
        if (signable == null) {
            return false;
        }

        return signService.verifySign(signable.getSignName(), signable.getSignId(), signable.getSignBody());
    }


    @Override
    @RequiresPermissions("assist:signature:edit")
    public Boolean delete(@RequestBody List<SignatureVo> list) throws ApiException {
        List<Signature> beanList = validateList(list,
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


    @NonNull
    private SignatureVo entityToVo(Signature entity) {
        if (entity == null) {
            return new SignatureVo();
        }

        SignatureVo vo = baseEntityToVo(new SignatureVo(), entity);

        vo.setBusinessType(entity.getBusinessType());
        vo.setBusinessId(entity.getBusinessId());
        vo.setSignature(entity.getSignature());

        Signable signable = findSignable(entity);
        if (signable != null) {
            vo.setBodyParams(signable.getSignBody());
        }

        return vo;
    }

}
