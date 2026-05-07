package com.github.thundax.modules.member.persistence.dataobject;

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
@TableName(value = "member_member", autoResultMap = true)
public class MemberDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String name;
    private String gender;

    private String status;

    private Integer priority;
    private String remarks;

    private Date createDate;

    private String createBy;

    private Date updateDate;

    private String updateBy;
}
