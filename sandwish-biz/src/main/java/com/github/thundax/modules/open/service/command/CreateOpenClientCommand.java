package com.github.thundax.modules.open.service.command;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOpenClientCommand {
    private String name;
    private String ipWhitelist;
    private Date expiredAt;
    private String remarks;
    private List<String> permissions;
}
