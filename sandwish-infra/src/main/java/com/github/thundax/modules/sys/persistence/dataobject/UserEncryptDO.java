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
@TableName("sys_user_encrypt")
public class UserEncryptDO {

    @TableId(value = "id", type = IdType.INPUT)
    private String userId;

    private String loginPass;

    private String email;

    private String mobile;

    private String tel;



    private Date createDate;

    private String createBy;

    private Date updateDate;

    private String updateBy;

}
