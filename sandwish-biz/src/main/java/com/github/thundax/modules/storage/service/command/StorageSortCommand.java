package com.github.thundax.modules.storage.service.command;

import com.github.thundax.common.domain.SortDirection;
import com.github.thundax.modules.storage.entity.valueobject.StoredObjectId;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StorageSortCommand {

    private List<StoredObjectId> orderedIds;
    private SortDirection sortDirection;
}
