package com.github.thundax.modules.sys.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_role")
public class RoleDO {


    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String name;

    private String adminFlag;

    private String enableFlag;

    private Integer priority;

    private String remarks;

    private Date createDate;

    private String createBy;

    private Date updateDate;

    private String updateBy;

}
