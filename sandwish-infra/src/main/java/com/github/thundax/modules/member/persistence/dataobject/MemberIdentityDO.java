package com.github.thundax.modules.member.persistence.dataobject;

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
@TableName("member_identity")
public class MemberIdentityDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long memberId;

    private String identityType;

    private String identityValue;

    private String status;
}
