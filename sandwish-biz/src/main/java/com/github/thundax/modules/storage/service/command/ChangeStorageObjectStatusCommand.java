package com.github.thundax.modules.storage.service.command;

import com.github.thundax.modules.storage.entity.enums.StoredObjectStatus;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeStorageObjectStatusCommand {
    private StoredObjectId id;
    private StoredObjectStatus objectStatus;
}
