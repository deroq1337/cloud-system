package com.github.deroq1337.cloud.master.commands;

import com.github.deroq1337.cloud.master.CloudSystemMaster;
import com.github.deroq1337.cloud.master.command.Command;
import com.github.deroq1337.cloud.master.command.CommandInfo;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@CommandInfo(
        name = "help",
        description = "Lists all commands"
)
public class HelpCommand implements Command {

    private final @NotNull CloudSystemMaster master;

    @Override
    public void execute(@NotNull String[] args) {
        String message = master.getCommandMap().getCommandInfos().stream()
                .map(commandInfo -> commandInfo.name() + " - " + commandInfo.description())
                .collect(Collectors.joining("\n"));

        master.getConsole().sendMessage(message);
    }
}
