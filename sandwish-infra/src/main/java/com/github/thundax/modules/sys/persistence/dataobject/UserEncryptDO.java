package com.github.thundax.modules.sys.persistence.dataobject;

import com.github.thundax.common.persistence.AdminDataEntity;
import lombok.NoArgsConstructor;

/**
 * 用户加密信息持久化对象。
 */
@NoArgsConstructor
public class UserEncryptDO extends AdminDataEntity<UserEncryptDO> {

    private String loginPass;
    private String email;
    private String mobile;
    private String tel;

    public UserEncryptDO(String id) {
        super(id);
    }

    public String getLoginPass() {
        return loginPass;
    }

    public void setLoginPass(String loginPass) {
        this.loginPass = loginPass;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }
}
