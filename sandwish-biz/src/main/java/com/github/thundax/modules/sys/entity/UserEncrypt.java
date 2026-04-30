package com.github.thundax.modules.sys.entity;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.id.EntityId;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 用户重要信息加密
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEncrypt implements Auditable {
    private EntityId id;

    private String loginPass;
    private String email;
    private String mobile;
    private String tel;
    private Date createDate;
    private Date updateDate;
    private String createUserId;
    private String updateUserId;

    public static final String BEAN_NAME = "UserEncrypt";
}
