package com.github.thundax.configure;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "sandwish.login")
public class LoginProperties {
    private boolean enable;
    private int maxFailCount;
    private int expire;
    private int lockTime;

    public boolean getEnable() {
        return enable;
    }

    public String getHours() {
        if (expire % 3600 == 0) {
            BigDecimal divide = (new BigDecimal(expire).divide(new BigDecimal(3600), 0, BigDecimal.ROUND_HALF_UP));
            return divide.toString();
        } else {
            BigDecimal divide = (new BigDecimal(expire).divide(new BigDecimal(3600), 1, BigDecimal.ROUND_HALF_UP));
            return divide.toString();
        }
    }

    public String getLockHours() {
        if (lockTime % 3600 == 0) {
            BigDecimal divide = (new BigDecimal(lockTime).divide(new BigDecimal(3600), 0, BigDecimal.ROUND_HALF_UP));
            return divide.toString();
        } else {
            BigDecimal divide = (new BigDecimal(lockTime).divide(new BigDecimal(3600), 1, BigDecimal.ROUND_HALF_UP));
            return divide.toString();
        }
    }
}
