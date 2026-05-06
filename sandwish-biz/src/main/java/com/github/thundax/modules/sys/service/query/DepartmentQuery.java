package com.github.thundax.modules.sys.service.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentQuery {
    private String parentId;
    private String name;
    private String remarks;
}
