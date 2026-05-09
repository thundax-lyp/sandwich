package com.github.thundax.modules.sys.service.command;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.common.tree.TreeNodeMoveType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MoveMenuCommand {
    private EntityId fromId;
    private EntityId toId;
    private TreeNodeMoveType moveType;
}
