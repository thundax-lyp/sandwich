package com.github.thundax.modules.sys.controller;

import com.google.common.collect.Lists;
import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.collect.SetUtils;
import com.github.thundax.common.exception.ApiException;
import com.github.thundax.common.exception.InvalidParameterException;
import com.github.thundax.common.exception.InvalidTokenException;
import com.github.thundax.common.utils.StringUtils;
import com.github.thundax.common.utils.encrypt.Sm2;
import com.github.thundax.common.utils.redis.RedisClient;
import com.github.thundax.common.vo.UserVo;
import com.github.thundax.common.web.BaseApiController;
import com.github.thundax.modules.auth.exception.InvalidPasswordException;
import com.github.thundax.modules.auth.security.SecurityUtils;
import com.github.thundax.modules.auth.security.subject.Subject;
import com.github.thundax.modules.auth.service.PasswordService;
import com.github.thundax.modules.auth.utils.UserAccessHolder;
import com.github.thundax.modules.sys.api.PersonalServiceApi;
import com.github.thundax.modules.sys.api.query.UpdatePasswordQueryParam;
import com.github.thundax.modules.sys.api.vo.MenuVo;
import com.github.thundax.modules.sys.entity.Menu;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.service.UserService;
import com.github.thundax.modules.sys.utils.SysApiUtils;
import com.github.thundax.modules.sys.utils.UserServiceHolder;
import com.github.thundax.modules.utils.AvatarUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Validator;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.github.thundax.common.Constants.CACHE_PRIVATE_KEY_;

/**
 * @author thundax
 */
@RestController
public class PersonalApiController extends BaseApiController implements PersonalServiceApi {

    private final UserService userService;
    private final PasswordService passwordService;
    private final RedisClient redisClient;

    public PersonalApiController(Validator validator,
                                 UserService userService,
                                 PasswordService passwordService, RedisClient redisClient) {
        super(validator);

        this.userService = userService;
        this.passwordService = passwordService;
        this.redisClient = redisClient;
    }


    @Override
    public UserVo info() throws ApiException {
        User currentUser = UserAccessHolder.currentUser();
        if (currentUser.getId() == null || !currentUser.isEnable()) {
            throw new InvalidTokenException();
        }

        return entityToVo(currentUser);
    }


    @Override
    public UserVo updateInfo(@RequestBody UserVo vo) throws ApiException {
        validate(vo, "name", "email", "mobile");

        User currentUser = UserAccessHolder.currentUser();

        currentUser.setName(vo.getName());
        currentUser.setEmail(vo.getEmail());
        currentUser.setMobile(vo.getMobile());
        userService.save(currentUser);

        return entityToVo(currentUser);
    }


    @Override
    public Boolean updatePassword(@RequestBody UpdatePasswordQueryParam queryParam) throws ApiException {

        // 解密密码（数据需要加密传输）
        String password = Sm2.decrypt(queryParam.getPassword(), redisClient.get(CACHE_PRIVATE_KEY_ + queryParam.getToken()));
        String oldPassword = Sm2.decrypt(queryParam.getOldPassword(), redisClient.get(CACHE_PRIVATE_KEY_ + queryParam.getToken()));
        queryParam.setPassword(password);
        queryParam.setOldPassword(oldPassword);
        validate(queryParam);

        if (StringUtils.isBlank(password)) {
            throw new InvalidParameterException("password");
        } else if (!password.matches(SysApiUtils.PASSWORD_VALIDATE_PATTERN)) {
            throw new ApiException(SysApiUtils.PASSWORD_VALIDATE_MESSAGE);
        }

        User currentUser = UserAccessHolder.currentUser();

        if (!passwordService.validate(oldPassword, currentUser.getLoginPass())) {
            throw new InvalidPasswordException();
        }

        currentUser.setLoginPass(passwordService.encrypt(password));
        userService.updatePassword(currentUser);

        return true;
    }


    @Override
    public UserVo uploadAvatar(MultipartFile file) throws ApiException {
        User currentUser = UserAccessHolder.currentUser();

        try {
            AvatarUtils.saveAvatar(currentUser.getId(), file.getInputStream());
        } catch (IOException e) {
            throw new ApiException(e.getMessage());
        }

        return entityToVo(currentUser);
    }


    @Override
    public UserVo deleteAvatar() {
        User currentUser = UserAccessHolder.currentUser();

        AvatarUtils.deleteAvatar(currentUser.getId());

        return entityToVo(currentUser);
    }


    @Override
    public List<MenuVo> menus() {
        // 获取可见菜单
        List<Menu> allMenuList = UserServiceHolder.findMenuList(currentUser());

        Menu rootMenu = new Menu(null);
        List<Menu> menuList = Lists.newArrayList(rootMenu);
        // 构建parent-child关系，并递归下去
        for (int idx = 0; idx < menuList.size(); idx++) {
            Menu parent = menuList.get(idx);

            List<Menu> childList = ListUtils.filter(allMenuList, item ->
                    item.isDisplay() && Objects.equals(item.getParentId(), parent.getId()));

            menuList.addAll(idx + 1, childList);
        }
        // 移除根节点
        menuList.remove(0);

        return ListUtils.map(menuList, entity -> {
            MenuVo vo = new MenuVo();
            vo.setId(entity.getId());
            vo.setParentId(entity.getParentId());
            vo.setName(entity.getName());
            vo.setPriority(entity.getPriority());
            vo.setUrl(entity.getUrl());
            vo.setDisplayParams(entity.getDisplayParams());
            return vo;
        });
    }


    @Override
    public Set<String> perms() {
        Subject subject = SecurityUtils.getSubject();

        if (subject == null) {
            return SetUtils.newHashSet();
        }

        return subject.getPermissions();
    }

    private UserVo entityToVo(User entity) {
        if (entity == null) {
            return new UserVo();
        }

        UserVo vo = new UserVo();
        vo.setId(entity.getId());
        vo.setLoginName(entity.getLoginName());
        vo.setRanks(entity.getRanks());

        vo.setName(entity.getName());
        vo.setMobile(entity.getMobile());
        vo.setEmail(entity.getEmail());

        if (StringUtils.isNotBlank(entity.getId()) && AvatarUtils.existAvatar(entity.getId())) {
            vo.setAvatar(UserApiController.getAvatarUrl(entity.getId(), UserAccessHolder.currentToken()));
        }

        vo.setLastLoginDate(entity.getLastLoginDate());
        vo.setLastLoginIp(entity.getLastLoginIp());
        vo.setRegisterDate(entity.getRegisterDate());
        vo.setRegisterIp(entity.getRegisterIp());

        vo.setAdmin(entity.isAdmin());
        vo.setSuper(entity.isSuper());

        return vo;
    }

}
