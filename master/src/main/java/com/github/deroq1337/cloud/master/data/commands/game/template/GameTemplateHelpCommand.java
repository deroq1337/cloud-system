package com.github.deroq1337.cloud.master.data.commands.game.template;

import com.github.deroq1337.cloud.master.data.command.Command;
import com.github.deroq1337.cloud.master.data.command.CommandInfo;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

@CommandInfo(
        name = "help",
        description = "Lists all game template commands"
)
public class GameTemplateHelpCommand implements Command {

    private final @NotNull GameTemplateCommand templateCommand;

    public GameTemplateHelpCommand(@NotNull GameTemplateCommand templateCommand) {
        this.templateCommand = templateCommand;
    }

    @Override
    public void execute(@NotNull String[] args) {
        String message = templateCommand.getSubCommandMap().getCommandInfos().stream()
                .map(subCommandInfo -> subCommandInfo.name() + " - " + subCommandInfo.description())
                .collect(Collectors.joining("\n"));

        templateCommand.getMaster().getConsole().sendMessage(message);
    }
}