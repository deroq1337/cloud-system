package com.github.deroq1337.cloud.master.data.commands.game;

import com.github.deroq1337.cloud.master.data.command.Command;
import com.github.deroq1337.cloud.master.data.command.CommandInfo;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

@CommandInfo(
        name = "help",
        description = "Lists all game commands"
)
public class GameHelpCommand implements Command {

    private final @NotNull GameCommand gameCommand;

    public GameHelpCommand(@NotNull GameCommand gameCommand) {
        this.gameCommand = gameCommand;
    }

    @Override
    public void execute(@NotNull String[] args) {
        String message = gameCommand.getSubCommandMap().getCommandInfos().stream()
                .map(subCommandInfo -> subCommandInfo.name() + " - " + subCommandInfo.description())
                .collect(Collectors.joining("\n"));

        gameCommand.getMaster().getConsole().sendMessage(message);
    }
}
