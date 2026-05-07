package com.github.thundax.modules.auth.persistence.dataobject;

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
@TableName("member_access_token")
public class MemberAccessTokenDO {
    @TableId(type = IdType.INPUT)
    private Long id;

    private String tokenId;
    private String tokenHash;
    private String sessionId;
    private Long memberId;
    private Date issuedAt;
    private Date expireAt;
    private String status;
    private Date createDate;
    private String createBy;
    private Date updateDate;
    private String updateBy;
}
