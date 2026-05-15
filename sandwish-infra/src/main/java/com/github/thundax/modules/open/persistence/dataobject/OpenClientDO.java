package com.github.thundax.modules.open.persistence.dataobject;

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
@TableName("open_client")
public class OpenClientDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private String name;

    private String status;

    private String ipWhitelist;

    private Date expiredAt;

    private String remarks;
}
