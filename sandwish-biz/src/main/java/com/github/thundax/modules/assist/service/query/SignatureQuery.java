package com.github.thundax.modules.assist.service.query;

import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignatureQuery implements Serializable {
    private String businessType;
    private String businessId;
    private String isVerifySign;
    private List<String> businessIdList;
}
