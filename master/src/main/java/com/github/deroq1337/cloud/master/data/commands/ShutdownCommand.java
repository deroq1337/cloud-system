package com.github.deroq1337.cloud.master.data.commands;

import com.github.deroq1337.cloud.master.data.CloudSystemMaster;
import com.github.deroq1337.cloud.master.data.command.Command;
import com.github.deroq1337.cloud.master.data.command.CommandInfo;
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
