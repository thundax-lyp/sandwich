package com.github.thundax.modules.assist.persistence.dataobject;

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
@TableName("tb_assist_signature")
public class SignatureDO {

    private String businessType;

    private String businessId;
    private String signature;
    private String isVerifySign;



    private Date createDate;
    private Date updateDate;

}
