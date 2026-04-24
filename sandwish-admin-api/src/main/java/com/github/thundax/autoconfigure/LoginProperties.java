package com.github.thundax.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * 登录相关配置
 *
 * @Auther: wdit
 * @Date: 2023/11/18 15:59
 * @Description:
 */
@Configuration
@ConfigurationProperties(prefix = "vltava.login")
public class LoginProperties {
    private boolean enable;
    private int maxFailCount;
    private int expire;
    private int lockTime;

    public boolean getEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getMaxFailCount() {
        return maxFailCount;
    }

    public void setMaxFailCount(int maxFailCount) {
        this.maxFailCount = maxFailCount;
    }

    public int getExpire() {
        return expire;
    }

    public void setExpire(int expire) {
        this.expire = expire;
    }

    public int getLockTime() {
        return lockTime;
    }

    public void setLockTime(int lockTime) {
        this.lockTime = lockTime;
    }

    public String getHours(){
        if(expire % 3600 == 0){
            BigDecimal divide = (new BigDecimal(expire).divide(new BigDecimal(3600),0,BigDecimal.ROUND_HALF_UP));
            return divide.toString();
        }else {
            BigDecimal divide = (new BigDecimal(expire).divide(new BigDecimal(3600),1,BigDecimal.ROUND_HALF_UP));
            return divide.toString();
        }
    }
    public String getLockHours(){
        if(lockTime % 3600 == 0){
            BigDecimal divide = (new BigDecimal(lockTime).divide(new BigDecimal(3600),0,BigDecimal.ROUND_HALF_UP));
            return divide.toString();
        }else {
            BigDecimal divide = (new BigDecimal(lockTime).divide(new BigDecimal(3600),1,BigDecimal.ROUND_HALF_UP));
            return divide.toString();
        }
    }

}
