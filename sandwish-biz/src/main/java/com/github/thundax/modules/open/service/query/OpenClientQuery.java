package com.github.thundax.modules.open.service.query;

import com.github.thundax.modules.open.entity.enums.OpenClientStatus;
import com.github.thundax.modules.open.entity.valueobject.OpenClientId;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenClientQuery {
    private List<OpenClientId> ids;
    private String name;
    private OpenClientStatus status;
}
