package com.github.thundax.modules.assist.service.command;

import com.github.thundax.common.id.EntityId;
import com.github.thundax.modules.assist.entity.AsyncTask;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AsyncTaskCommand {
    private EntityId id;
    private AsyncTask asyncTask;
}
