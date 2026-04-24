package com.github.thundax.modules.sys.entity.base;

import com.github.thundax.common.persistence.AdminDataEntity;
import com.github.thundax.modules.sys.entity.UserEncrypt;

/**
 * @author thundax
 */
public abstract class BaseUserEncrypt extends AdminDataEntity<UserEncrypt> {

    private static final long serialVersionUID = 1L;

    private String loginPass;
    private String email;
    private String mobile;
    private String tel;

    public BaseUserEncrypt() {
        super();
    }

    public BaseUserEncrypt(String id) {
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
