package com.github.thundax.modules.sys.entity;

import com.github.thundax.common.domain.Auditable;
import com.github.thundax.common.id.EntityId;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 用户重要信息加密。
 *
 * <p>密码字段仅作为旧用户加密表兼容镜像，认证凭据以 auth 模型 UserCredential 为准。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEncrypt implements Auditable {
    private EntityId id;

    /**
     * 迁移兼容字段，目标密码凭据固定由 UserCredential.credentialValue 承载。
     */
    @Deprecated
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
