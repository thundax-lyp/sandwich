package com.github.thundax.modules.sys.service.command;

import com.github.thundax.modules.sys.entity.valueobject.MenuId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteMenuCommand {
    private MenuId id;
}
