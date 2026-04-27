package com.github.thundax.modules.sys.service.impl;

import com.github.thundax.common.collect.ListUtils;
import com.github.thundax.common.service.impl.CrudServiceImpl;
import com.github.thundax.modules.assist.service.SignService;
import com.github.thundax.modules.sys.dao.UserDao;
import com.github.thundax.modules.sys.entity.Role;
import com.github.thundax.modules.sys.entity.User;
import com.github.thundax.modules.sys.entity.UserEncrypt;
import com.github.thundax.modules.sys.service.UserEncryptService;
import com.github.thundax.modules.sys.service.UserService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** @author wdit */
@Service
@Transactional(readOnly = true)
public class UserServiceImpl extends CrudServiceImpl<UserDao, User> implements UserService {

    private final SignService signService;
    private final UserEncryptService userEncryptService;

    public UserServiceImpl(
            UserDao dao, SignService signService, UserEncryptService userEncryptService) {
        super(dao);
        this.signService = signService;
        this.userEncryptService = userEncryptService;
    }

    @Override
    public User getByLoginName(String loginName) {
        User user = dao.getByLoginName(loginName);
        if (user != null) {
            userEncryptService.get(user.getId());
        }
        return user;
    }

    @Override
    public User getBySsoLoginName(String ssoLoginName) {
        User user = dao.getBySsoLoginName(ssoLoginName);
        if (user != null) {
            userEncryptService.get(user.getId());
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(User user) {
        if (user.getIsNewRecord()) {
            user.preInsert();
            dao.insert(user);
        } else {
            user.preUpdate();
            dao.update(user);
        }

        dao.deleteUserRole(user);
        if (ListUtils.isNotEmpty(user.getRoleIdList())) {
            dao.insertUserRole(user);
        }
        signService.sign(user.getSignName(), user.getSignId(), user.getSignBody());
        UserEncrypt userEncrypt = new UserEncrypt();
        userEncrypt.setId(user.getId());
        userEncrypt.setCreateUserId(user.getCreateUserId());
        userEncrypt.setUpdateUserId(user.getUpdateUserId());
        userEncrypt.setEmail(user.getEmail());
        userEncrypt.setMobile(user.getMobile());
        userEncrypt.setTel(user.getTel());
        userEncryptService.save(userEncrypt);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(User user) {
        user.preUpdate();
        dao.updateLoginPass(user);
        signService.sign(user.getSignName(), user.getSignId(), user.getSignBody());
        UserEncrypt userEncrypt = new UserEncrypt();
        userEncrypt.setId(user.getId());
        userEncrypt.setCreateUserId(user.getCreateUserId());
        userEncrypt.setUpdateUserId(user.getUpdateUserId());
        userEncrypt.setLoginPass(user.getLoginPass());
        userEncryptService.updateLoginPass(userEncrypt);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLoginInfo(User user) {
        dao.updateLoginInfo(user);
        signService.sign(user.getSignName(), user.getSignId(), user.getSignBody());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateEnableFlag(User user) {
        user.preUpdate();

        int result = dao.updateEnableFlag(user);

        signService.sign(user.getSignName(), user.getSignId(), user.getSignBody());

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateEnableFlag(List<User> list) {
        return batchOperate(list, this::updateEnableFlag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(User user) {
        dao.deleteUserRole(user);

        int result = dao.delete(user);

        signService.deleteSign(user.getSignName(), user.getSignId());

        return result;
    }

    @Override
    public List<Role> findUserRole(User user) {
        return dao.findUserRole(user);
    }
}
