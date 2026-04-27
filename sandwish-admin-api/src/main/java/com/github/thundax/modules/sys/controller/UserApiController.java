
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
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.common.web.RequestUtils;
import com.github.thundax.modules.auth.security.annotation.RequiresPermissions;
import com.github.thundax.modules.auth.service.PasswordService;
import com.github.thundax.modules.sys.assembler.UserInterfaceAssembler;
import com.github.thundax.modules.sys.api.UserServiceApi;
import com.github.thundax.modules.sys.entity.Office;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.request.UserAvatarRequest;
import com.github.thundax.modules.sys.request.UserCheckRequest;
import com.github.thundax.modules.sys.request.UserIdRequest;
import com.github.thundax.modules.sys.request.UserOfficeRequest;
import com.github.thundax.modules.sys.request.UserQueryRequest;
import com.github.thundax.modules.sys.request.UserRoleRequest;
import com.github.thundax.modules.sys.request.UserSaveRequest;
import com.github.thundax.modules.sys.request.UserStatusRequest;
import com.github.thundax.modules.sys.response.UserOfficeResponse;
import com.github.thundax.modules.sys.response.UserResponse;
import com.github.thundax.modules.sys.response.UserRoleResponse;
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
    private final UserInterfaceAssembler userInterfaceAssembler;

    @Autowired
    public UserApiController(UserService userService,
                             OfficeService officeService,
                             RoleService roleService,
                             Validator validator,
                             RedisClient redisClient,
                             PasswordService passwordService,
                             UserInterfaceAssembler userInterfaceAssembler) {
        super(validator);

        this.userService = userService;
        this.officeService = officeService;
        this.roleService = roleService;
        this.redisClient = redisClient;
        this.passwordService = passwordService;
        this.userInterfaceAssembler = userInterfaceAssembler;
    }


    @Override
    @RequiresPermissions("sys:user:view")
    public UserResponse get(@RequestBody UserIdRequest request) throws ApiException {
        User bean = userService.get(request.getId());
        if (bean == null) {
            throw new NullBeanException(User.BEAN_NAME, request.getId());
        }
        return userInterfaceAssembler.toResponse(bean);
    }


    @Override
    @RequiresPermissions("sys:user:view")
    public List<UserResponse> list(@RequestBody UserQueryRequest request) throws ApiException {
        validate(request);

        User query = readQuery(request);

        return ListUtils.map(userService.findList(query), userInterfaceAssembler::toResponse);
    }


    @Override
    @RequiresPermissions("sys:user:view")
    public PageVo<UserResponse> page(@RequestBody UserQueryRequest request) throws ApiException {
        validate(request);

        User query = readQuery(request);
        Page<User> page = readUserPage(request);

        return entityPageToVo(userService.findPage(query, page), userInterfaceAssembler::toResponse);
    }


    @Override
    @RequiresPermissions("sys:user:edit")
    public UserResponse add(@RequestBody UserSaveRequest request) throws ApiException {
        // 解密密码（数据需要加密传输）
        String password = Sm2.decrypt(request.getLoginPass(), redisClient.get(CACHE_PRIVATE_KEY_ + request.getToken()));
        request.setLoginPass(password);
        validate(request);
        validateOffice(request.getOffice());
        validateRoles(request.getRoleList());

        if (!isLoginNameAvailable(request.getLoginName(), request.getId())) {
            throw new InvalidParameterException("loginName");
        }

        if (StringUtils.isBlank(request.getLoginPass())) {
            throw new InvalidParameterException("password");
        }

        User entity = userInterfaceAssembler.toEntity(new User(), request);
        entity.setLoginPass(passwordService.encrypt(request.getLoginPass()));

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

        return userInterfaceAssembler.toResponse(entity);
    }


    @Override
    @RequiresPermissions("sys:user:edit")
    public UserResponse update(@RequestBody UserSaveRequest request) throws ApiException {
        // 解密密码（数据需要加密传输）
        if (StringUtils.isNotBlank(request.getLoginPass())) {
            String password = Sm2.decrypt(request.getLoginPass(), redisClient.get(CACHE_PRIVATE_KEY_ + request.getToken()));
            // 先解密，否则密码规则无法校验
            request.setLoginPass(password);
        }
        validate(request);
        validateOffice(request.getOffice());
        validateRoles(request.getRoleList());

        if (!isLoginNameAvailable(request.getLoginName(), request.getId())) {
            throw new InvalidParameterException("loginName");
        }

        if(!isSsoLoginNameAvailable(request.getSsoLoginName(), request.getId())){
            throw new InvalidParameterException("ssoLoginName");
        }

        User bean = userService.get(request.getId());
        if (bean == null) {
            throw new NullBeanException(User.BEAN_NAME, request.getId());
        }
        // 非超管用户无权限开启/关闭管理员
        if (!currentUser().isSuper() && !(Boolean.TRUE.equals(request.getAdmin()) ? Global.YES : Global.NO).equals(bean.getAdminFlag())) {
            throw new PermissionDeniedException();
        }
        // 无权限修改超管/等级高于自身的用户信息
        if(!currentUser().isSuper()){
            if (bean.isSuper() || (bean.getRanks() >= currentUser().getRanks())) {
                throw new PermissionDeniedException();
            }
        }


        User entity = userInterfaceAssembler.toEntity(bean, request);

        userService.save(entity);

        if (StringUtils.isNotBlank(request.getLoginPass())) {
            entity.setLoginPass(passwordService.encrypt(request.getLoginPass()));
            userService.updatePassword(entity);
        }

        return userInterfaceAssembler.toResponse(entity);
    }


    @Override
    @RequiresPermissions("sys:user:edit")
    public Boolean uploadAvatar(@RequestParam(value = "id") String id, MultipartFile avatar) throws ApiException {
        return true;
    }

    @Override
    @RequiresPermissions("sys:user:edit")
    public Boolean deleteAvatar(@RequestBody UserAvatarRequest request) throws ApiException {
        return true;
    }

    @Override
    @RequiresPermissions("sys:user:view")
    public String avatar(@RequestBody UserAvatarRequest request) throws ApiException {
        return "";
    }

    @Override
    @RequiresPermissions("sys:user:edit")
    public Boolean updateEnableFlag(@RequestBody List<UserStatusRequest> list) throws ApiException {
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
    public Boolean delete(@RequestBody List<UserIdRequest> list) throws ApiException {
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
    public Boolean check(@RequestBody UserCheckRequest request) {
        return isLoginNameAvailable(request.getLoginName(), request.getId());
    }

    @Override
    @RequiresPermissions("sys:user:view")
    public Boolean checkSsoLoginName(@RequestBody UserCheckRequest request) {
        return isSsoLoginNameAvailable(request.getSsoLoginName(), request.getId());
    }

    @Override
    @RequiresPermissions("sys:user:view")
    public List<UserOfficeResponse> officeTree() {
        return ListUtils.map(officeService.findList(new Office()), userInterfaceAssembler::toOfficeResponse);
    }

    @Override
    @RequiresPermissions("sys:user:view")
    public List<UserRoleResponse> roleList() {
        Role query = new Role();
        Role.Query queryCondition = new Role.Query();
        queryCondition.setEnableFlag(Global.ENABLE);
        query.setQuery(queryCondition);

        return ListUtils.map(roleService.findList(query), userInterfaceAssembler::toRoleResponse);
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

    private User readQuery(UserQueryRequest request) throws ApiException {
        User query = new User();
        User.Query queryCondition = new User.Query();

        queryCondition.setLoginName(request.getLoginName());
        queryCondition.setName(request.getName());

        if (request.getEnable() != null) {
            queryCondition.setEnableFlag(request.getEnable() ? Global.ENABLE : Global.DISABLE);
        }

        if (StringUtils.isNotBlank(request.getOfficeId())) {
            Office office = OfficeServiceHolder.get(request.getOfficeId());
            if (office == null) {
                throw new NullBeanException(Office.BEAN_NAME, request.getOfficeId());
            }

            queryCondition.setOfficeId(office.getId());
        }

        queryCondition.setOrderBy(request.getOrderBy());
        query.setQuery(queryCondition);

        return query;
    }


    private void validateOffice(UserOfficeRequest request) throws ApiException {
        if (request == null || StringUtils.isBlank(request.getId())) {
            throw new InvalidParameterException("office.id");

        } else {
            Office bean = OfficeServiceHolder.get(request.getId());
            if (bean == null) {
                throw new NullBeanException(Office.BEAN_NAME, request.getId());
            }
        }
    }


    private void validateRoles(List<UserRoleRequest> requestList) throws ApiException {
        if (ListUtils.isEmpty(requestList)) {
            return;
        }
        for (UserRoleRequest request : requestList) {
            if (request == null || StringUtils.isBlank(request.getId())) {
                throw new InvalidParameterException("roles.id");

            } else {
                Role bean = RoleServiceHolder.get(request.getId());
                if (bean == null) {
                    throw new NullBeanException(Role.BEAN_NAME, request.getId());
                }
            }
        }
    }

    private Page<User> readUserPage(UserQueryRequest request) {
        Integer pageNo = request.getPageNo();
        Integer pageSize = request.getPageSize();

        if (pageNo == null || pageNo < Page.FIRST_PAGE_INDEX) {
            pageNo = Page.FIRST_PAGE_INDEX;
        }

        if (pageSize == null || pageSize <= 0) {
            pageSize = Page.DEFAULT_PAGE_SIZE;
        }

        Page<User> page = new Page<>();
        page.setPageNo(pageNo);
        page.setPageSize(pageSize);
        return page;
    }


    private boolean isLoginNameAvailable(String loginName, String id) {
        if (StringUtils.isBlank(loginName)) {
            return true;
        }
        User bean = userService.getByLoginName(loginName);
        if (bean == null) {
            return true;
        }

        return StringUtils.equals(bean.getId(), id);
    }

    private boolean isSsoLoginNameAvailable(String ssoLoginName, String id) {
        if (StringUtils.isBlank(ssoLoginName)) {
            return true;
        }
        User bean = userService.getBySsoLoginName(ssoLoginName);
        if (bean == null) {
            return true;
        }

        return StringUtils.equals(bean.getId(), id);
    }

    public static String getAvatarUrl(String userId, String token) {
        return String.format(AVATAR_URL_FORMAT, userId, token);
    }
}
