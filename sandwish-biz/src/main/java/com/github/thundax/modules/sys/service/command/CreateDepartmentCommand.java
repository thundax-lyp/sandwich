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
public class CreateDepartmentCommand {
    private EntityId id;
    private EntityId parentId;
    private String name;
    private String shortName;
    private int priority;
    private String remarks;
}
