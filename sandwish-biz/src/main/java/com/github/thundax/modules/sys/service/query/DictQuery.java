package com.github.thundax.modules.sys.service.query;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DictQuery implements Serializable {
    private String type;
    private String remarks;
    private String label;
}
