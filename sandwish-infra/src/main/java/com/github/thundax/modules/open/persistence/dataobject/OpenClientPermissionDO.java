package com.github.thundax.modules.open.persistence.dataobject;

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
@TableName("open_client_permission")
public class OpenClientPermissionDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long clientId;

    private String permission;
}
