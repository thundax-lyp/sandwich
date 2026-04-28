package com.github.thundax.modules.auth.persistence.dataobject;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessTokenDO implements Serializable {

    private String token;
    private String userId;
    private String checkCode;
}
