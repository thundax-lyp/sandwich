package com.github.thundax.modules.storage.service.command;

import com.github.thundax.modules.storage.entity.valueobject.StoredObjectId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteStorageCommand {
    private StoredObjectId id;
}
