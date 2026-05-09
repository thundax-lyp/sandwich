package com.github.thundax.modules.sys.entity;

import com.github.thundax.common.domain.Sortable;
import com.github.thundax.modules.sys.entity.valueobject.DictId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Dict implements Sortable {
    private DictId id;
    private String type;
    private String label;
    private String value;
    private int priority;
    private String remarks;
}
