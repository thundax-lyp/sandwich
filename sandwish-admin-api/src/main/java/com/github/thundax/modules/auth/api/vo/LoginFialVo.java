package com.github.thundax.modules.auth.api.vo;

public class LoginFialVo {
    private String usernme;
    private int count;

    public LoginFialVo() {}

    public LoginFialVo(String usernme, int count) {
        this.usernme = usernme;
        this.count = count;
    }

    public String getUsernme() {
        return usernme;
    }

    public void setUsernme(String usernme) {
        this.usernme = usernme;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
