package com.github.thundax.modules.assist.service.query;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignatureQuery {
    private String businessType;
    private String businessId;
    private String isVerifySign;
    private List<String> businessIdList;
}
