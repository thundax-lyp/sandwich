
package com.github.thundax.modules.sys.controller;

import com.github.thundax.common.exception.*;
import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.config.Global;
import com.github.thundax.common.persistence.Page;
import com.github.thundax.common.utils.FileUtils;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.utils.encrypt.Sm2;
import com.github.thundax.common.utils.redis.RedisClient;
import com.github.thundax.common.vo.PageVo;
import com.github.thundax.common.vo.UserVo;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.common.web.RequestUtils;
import com.github.thundax.modules.auth.security.annotation.RequiresPermissions;
import com.github.thundax.modules.auth.service.PasswordService;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.sys.api.UserServiceApi;
import com.github.thundax.modules.sys.api.query.UserQueryParam;
import com.github.thundax.modules.sys.api.vo.OfficeVo;
import com.github.thundax.modules.sys.api.vo.RoleVo;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.OfficeService;
import com.github.thundax.modules.sys.service.RoleService;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.utils.OfficeServiceHolder;
import com.github.thundax.modules.sys.utils.RoleServiceHolder;
import com.github.thundax.modules.utils.AvatarUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.github.thundax.common.Constants.CACHE_PRIVATE_KEY_;

/**
 * @author thundax
 */
@RestController
public class UserApiController extends BaseApiController implements UserServiceApi {

    private static final String AVATAR_URL_FORMAT = "/api/sys/user/avatar?id=%s&token=%s";

    private final UserService userService;
    private final OfficeService officeService;
    private final RoleService roleService;
    private final RedisClient redisClient;
    private final PasswordService passwordService;

    @Autowired
    public UserApiController(UserService userService,
                             OfficeService officeService,
                             RoleService roleService,
                             Validator validator, RedisClient redisClient, PasswordService passwordService) {
        super(validator);

        this.userService = userService;
        this.officeService = officeService;
        this.roleService = roleService;
        this.redisClient = redisClient;
        this.passwordService = passwordService;
    }


    @Override
    @RequiresPermissions("sys:user:view")
    public UserVo get(@RequestBody UserVo vo) throws ApiException {
        User bean = userService.get(vo.getId());
        if (bean == null) {
            throw new NullBeanException(User.BEAN_NAME, vo.getId());
        }
        return entityToVo(bean);
    }


    @Override
    @RequiresPermissions("sys:user:view")
    public List<UserVo> list(@RequestBody UserQueryParam queryParam) throws ApiException {
        validate(queryParam);

        User query = readQuery(queryParam);

        return ListUtils.map(userService.findList(query), this::entityToVo);
    }


    @Override
    @RequiresPermissions("sys:user:view")
    public PageVo<UserVo> page(@RequestBody UserQueryParam queryParam) throws ApiException {
        validate(queryParam);

        User query = readQuery(queryParam);
        Page<User> page = readPage(queryParam);

        return entityPageToVo(userService.findPage(query, page), this::entityToVo);
    }


    @Override
    @RequiresPermissions("sys:user:edit")
    public UserVo add(@RequestBody UserVo vo) throws ApiException {
        // 解密密码（数据需要加密传输）
        String password = Sm2.decrypt(vo.getLoginPass(), redisClient.get(CACHE_PRIVATE_KEY_ + vo.getToken()));
        vo.setLoginPass(password);
        validate(vo);
        validateOffice(vo.getOffice());
        validateRoles(vo.getRoleList());

        if (!check(vo)) {
            throw new InvalidParameterException("loginName");
        }

        if (StringUtils.isBlank(vo.getLoginPass())) {
            throw new InvalidParameterException("password");
        }

        User entity = voToEntity(new User(), vo);
        entity.setLoginPass(passwordService.encrypt(vo.getLoginPass()));

        if (StringUtils.isNotEmpty(entity.getId())) {
            User bean = userService.get(entity.getId());
            if (bean != null) {
                throw new InsertBeanExistException(User.BEAN_NAME, entity.getId());
            }
            entity.setIsNewRecord(true);
        }

        entity.setRegisterDate(new Date());
        entity.setRegisterIp(RequestUtils.getRemoteAddr());

        userService.save(entity);

        return entityToVo(entity);
    }


    @Override
    @RequiresPermissions("sys:user:edit")
    public UserVo update(@RequestBody UserVo vo) throws ApiException {
        // 解密密码（数据需要加密传输）
        if (StringUtils.isNotBlank(vo.getLoginPass())) {
            String password = Sm2.decrypt(vo.getLoginPass(), redisClient.get(CACHE_PRIVATE_KEY_ + vo.getToken()));
            // 先解密，否则密码规则无法校验
            vo.setLoginPass(password);
        }
        validate(vo);
        validateOffice(vo.getOffice());
        validateRoles(vo.getRoleList());

        if (!check(vo)) {
            throw new InvalidParameterException("loginName");
        }

        if(!checkSsoLoginName(vo)){
            throw new InvalidParameterException("ssoLoginName");
        }

        User bean = userService.get(vo.getId());
        if (bean == null) {
            throw new NullBeanException(User.BEAN_NAME, vo.getId());
        }
        // 非超管用户无权限开启/关闭管理员
        if (!currentUser().isSuper() && !(Boolean.TRUE.equals(vo.getAdmin()) ? Global.YES : Global.NO).equals(bean.getAdminFlag())) {
            throw new PermissionDeniedException();
        }
        // 无权限修改超管/等级高于自身的用户信息
        if(!currentUser().isSuper()){
            if (bean.isSuper() || (bean.getRanks() >= currentUser().getRanks())) {
                throw new PermissionDeniedException();
            }
        }


        User entity = voToEntity(bean, vo);

        userService.save(entity);

        if (StringUtils.isNotBlank(vo.getLoginPass())) {
            entity.setLoginPass(passwordService.encrypt(vo.getLoginPass()));
            userService.updatePassword(entity);
        }

        return entityToVo(entity);
    }


    @Override
    @RequiresPermissions("sys:user:edit")
    public Boolean uploadAvatar(@RequestParam(value = "id") String id, MultipartFile avatar) throws ApiException {
        return true;
    }

    @Override
    @RequiresPermissions("sys:user:edit")
    public Boolean deleteAvatar(@RequestBody UserVo user) throws ApiException {
        return true;
    }

    @Override
    @RequiresPermissions("sys:user:view")
    public String avatar(@RequestBody UserVo user) throws ApiException {
        return "";
    }

    @Override
    @RequiresPermissions("sys:user:edit")
    public Boolean updateEnableFlag(@RequestBody List<UserVo> list) throws ApiException {
        User currentUser = currentUser();

        List<User> beanList = validateList(list,
                vo -> userService.get(vo.getId()),
                (bean, vo) -> {
                    if (bean.isSuper() || bean.getRanks() >= currentUser.getRanks()) {
                        throw new PermissionDeniedException();
                    }
                    return true;
                },
                (bean, vo) -> bean.setEnableFlag(Boolean.TRUE.equals(vo.getEnable()) ? Global.ENABLE : Global.DISABLE));

        userService.updateEnableFlag(beanList);

        return true;
    }


    @Override
    @RequiresPermissions("sys:user:edit")
    public Boolean delete(@RequestBody List<UserVo> list) throws ApiException {
        User currentUser = currentUser();

        List<User> beanList = validateList(list,
                vo -> userService.get(vo.getId()),
                (bean, vo) -> {
                    if (bean.isSuper() || bean.getRanks() >= currentUser.getRanks()) {
                        throw new PermissionDeniedException();
                    }
                    return true;
                }, null);

        userService.delete(beanList);

        return true;
    }

    @Override
    @RequiresPermissions("sys:user:view")
    public Boolean check(@RequestBody UserVo user) {
        if (StringUtils.isBlank(user.getLoginName())) {
            return true;
        }
        User bean = userService.getByLoginName(user.getLoginName());
        if (bean == null) {
            return true;
        }

        return StringUtils.equals(bean.getId(), user.getId());
    }

    @Override
    @RequiresPermissions("sys:user:view")
    public Boolean checkSsoLoginName(@RequestBody UserVo user) {
        if (StringUtils.isBlank(user.getSsoLoginName())) {
            return true;
        }
        User bean = userService.getBySsoLoginName(user.getSsoLoginName());
        if (bean == null) {
            return true;
        }

        return StringUtils.equals(bean.getId(), user.getId());
    }

    @Override
    @RequiresPermissions("sys:user:view")
    public List<OfficeVo> officeTree() {
        return ListUtils.map(officeService.findList(new Office()), this::entityToVo);
    }

    @Override
    @RequiresPermissions("sys:user:view")
    public List<RoleVo> roleList() {
        Role query = new Role();
        query.setQueryProp(Role.Query.PROP_ENABLE_FLAG, Global.ENABLE);

        return ListUtils.map(roleService.findList(query), this::entityToVo);
    }

    @Override
    @RequiresPermissions("user")
    public void avatarImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String userId = request.getParameter("id");
        if (StringUtils.isBlank(userId)) {
            response.sendError(HttpStatus.NOT_FOUND.value());
            return;
        }

        File avatarFile = AvatarUtils.getAvatarFile(userId);
        if (!avatarFile.exists()) {
            response.sendError(HttpStatus.NOT_FOUND.value());
            return;
        }

        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);

        IOUtils.write(
                FileUtils.readFileToByteArray(avatarFile),
                response.getOutputStream());
    }

    private User readQuery(UserQueryParam queryParam) throws ApiException {
        User query = new User();

        query.setQueryProp(User.Query.PROP_LOGIN_NAME, queryParam.getLoginName());
        query.setQueryProp(User.Query.PROP_NAME, queryParam.getName());

        if (queryParam.getEnable() != null) {
            query.setQueryProp(User.Query.PROP_ENABLE_FLAG, queryParam.getEnable() ? Global.ENABLE : Global.DISABLE);
        }

        if (StringUtils.isNotBlank(queryParam.getOfficeId())) {
            Office office = OfficeServiceHolder.get(queryParam.getOfficeId());
            if (office == null) {
                throw new NullBeanException(Office.BEAN_NAME, queryParam.getOfficeId());
            }

            query.setQueryProp(User.Query.PROP_OFFICE_TREE_LEFT, office.getLft());
            query.setQueryProp(User.Query.PROP_OFFICE_TREE_RIGHT, office.getRgt());
        }

        query.setQueryProp(User.Query.PROP_ORDER_BY, queryParam.getOrderBy());

        return query;
    }


    private void validateOffice(OfficeVo vo) throws ApiException {
        if (vo == null || StringUtils.isBlank(vo.getId())) {
            throw new InvalidParameterException("office.id");

        } else {
            Office bean = OfficeServiceHolder.get(vo.getId());
            if (bean == null) {
                throw new NullBeanException(Office.BEAN_NAME, vo.getId());
            }
        }
    }


    private void validateRoles(List<RoleVo> voList) throws ApiException {
        if (ListUtils.isEmpty(voList)) {
            return;
        }
        for (RoleVo vo : voList) {
            if (vo == null || StringUtils.isBlank(vo.getId())) {
                throw new InvalidParameterException("roles.id");

            } else {
                Role bean = RoleServiceHolder.get(vo.getId());
                if (bean == null) {
                    throw new NullBeanException(Role.BEAN_NAME, vo.getId());
                }
            }
        }
    }


    @NonNull
    private UserVo entityToVo(User entity) {
        if (entity == null) {
            return new UserVo();
        }

        UserVo vo = baseEntityToVo(new UserVo(), entity);

        vo.setLoginName(entity.getLoginName());
        vo.setRanks(entity.getRanks());

        vo.setName(entity.getName());
        vo.setEmail(entity.getEmail());
        vo.setMobile(entity.getMobile());
        vo.setAvatar(getAvatarUrl(entity.getId(), UserAccessHolder.currentToken()));

        vo.setSuper(entity.isSuper());
        vo.setAdmin(entity.isAdmin());
        vo.setEnable(entity.isEnable());

        vo.setRegisterDate(entity.getRegisterDate());
        vo.setRegisterIp(entity.getRegisterIp());
        vo.setLastLoginDate(entity.getLastLoginDate());
        vo.setLastLoginIp(entity.getLastLoginIp());

        vo.setOffice(entityToVo(entity.getOffice()));
        vo.setRoleList(ListUtils.map(entity.getRoleList(), this::entityToVo));

        return vo;
    }

    @NonNull
    private OfficeVo entityToVo(Office entity) {
        if (entity == null) {
            return new OfficeVo();
        }

        OfficeVo vo = new OfficeVo(entity.getId());
        if (StringUtils.isNotBlank(entity.getParentId())) {
            vo.setParentId(entity.getParentId());
        }
        vo.setName(entity.getName());
        vo.setName(entity.getName());
        vo.setNamePath(entity.getNamePath());

        return vo;
    }

    @NonNull
    private RoleVo entityToVo(Role entity) {
        if (entity == null) {
            return new RoleVo();
        }

        RoleVo vo = new RoleVo(entity.getId());
        vo.setName(entity.getName());

        return vo;
    }

    @NonNull
    private User voToEntity(@NonNull User entity, @NonNull UserVo vo) {
        baseVoToEntity(entity, vo);

        if (vo.getOffice() != null) {
            entity.setOfficeId(vo.getOffice().getId());
        }

        entity.setLoginName(vo.getLoginName());
        entity.setRanks(vo.getRanks());

        entity.setName(vo.getName());
        entity.setEmail(vo.getEmail());
        entity.setMobile(vo.getMobile());

        entity.setAdminFlag(Boolean.TRUE.equals(vo.getAdmin()) ? Global.YES : Global.NO);
        entity.setEnableFlag(Boolean.TRUE.equals(vo.getEnable()) ? Global.ENABLE : Global.DISABLE);

        entity.setRoleIdList(ListUtils.map(vo.getRoleList(), RoleVo::getId));

        return entity;
    }

    public static String getAvatarUrl(String userId, String token) {
        return String.format(AVATAR_URL_FORMAT, userId, token);
    }
}
