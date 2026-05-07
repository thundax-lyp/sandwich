package com.github.thundax.modules.sys.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user_identity")
public class UserIdentityDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long userId;

    private String identityType;

    private String identityValue;

    private String status;
}
