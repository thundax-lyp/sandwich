package com.github.thundax.modules.assist.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 签名持久化对象。 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_assist_signature")
public class SignatureDO {

    @TableField(exist = false)
    private String id;

    @TableField(exist = false)
    private boolean isNewRecord;

    private String businessType;
    private String businessId;
    private String signature;
    private String isVerifySign;

    @TableField(exist = false)
    private Integer priority;

    @TableField(exist = false)
    private String remarks;

    private Date createDate;
    private Date updateDate;

    @TableField(exist = false)
    private String delFlag;
}
