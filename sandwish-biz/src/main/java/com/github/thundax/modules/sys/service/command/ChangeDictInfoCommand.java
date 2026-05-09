package com.github.thundax.modules.sys.service.command;

import com.github.thundax.common.id.EntityId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeDictInfoCommand {
    private EntityId id;
    private String type;
    private String label;
    private String value;
    private Integer priority;
    private String remarks;
}
