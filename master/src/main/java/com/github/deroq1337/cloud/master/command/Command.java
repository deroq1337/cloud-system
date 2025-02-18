package com.github.deroq1337.cloud.master.command;

import org.jetbrains.annotations.NotNull;

public interface Command {

    void execute(@NotNull String[] args);
}
