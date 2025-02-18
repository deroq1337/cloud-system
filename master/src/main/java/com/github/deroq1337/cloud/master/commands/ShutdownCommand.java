package com.github.deroq1337.cloud.master.commands;

import com.github.deroq1337.cloud.master.CloudSystemMaster;
import com.github.deroq1337.cloud.master.command.Command;
import com.github.deroq1337.cloud.master.command.CommandInfo;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
@CommandInfo(
        name = "shutdown",
        description = "Shuts down the master"
)
public class ShutdownCommand implements Command {

    private final @NotNull CloudSystemMaster master;

    @Override
    public void execute(@NotNull String[] args) {
        master.shutdown();
    }
}
