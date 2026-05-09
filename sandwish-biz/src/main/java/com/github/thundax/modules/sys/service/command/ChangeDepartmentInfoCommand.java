package com.github.thundax.modules.sys.service.command;

import com.github.thundax.modules.sys.entity.valueobject.DepartmentId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeDepartmentInfoCommand {
    private DepartmentId id;
    private DepartmentId parentId;
    private String name;
    private String shortName;
    private int priority;
    private String remarks;
}
