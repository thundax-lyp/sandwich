package com.github.thundax.modules.storage.service.command;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.storage.entity.enums.StoredObjectStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeStorageObjectStatusCommand {
    private EntityId id;
    private StoredObjectStatus objectStatus;
}
