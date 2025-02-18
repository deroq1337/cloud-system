package com.github.deroq1337.cloud.master.data.commands.game.template;

import com.github.deroq1337.cloud.master.data.CloudSystemMaster;
import com.github.deroq1337.cloud.master.data.command.Command;
import com.github.deroq1337.cloud.master.data.command.CommandInfo;
import com.github.deroq1337.cloud.master.data.command.CommandMap;
import com.github.deroq1337.cloud.master.data.commands.game.GameCommand;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@Getter
@CommandInfo(
        name = "template",
        description = "Game template administration command"
)
public class GameTemplateCommand implements Command {

    private final @NotNull CloudSystemMaster master;
    private final @NotNull CommandMap subCommandMap;

    public GameTemplateCommand(@NotNull GameCommand gameCommand) {
        this.master = gameCommand.getMaster();
        this.subCommandMap = new CommandMap(Arrays.asList(
                new GameTemplateHelpCommand(this),
                new GameTemplateCreateCommand(this)
        ));
    }

    @Override
    public void execute(@NotNull String[] args) {
        if (args.length == 0) {
            handleSubCommandNotFound();
            return;
        }

        subCommandMap.getCommand(args[0]).ifPresentOrElse(
                subCommand -> subCommand.execute(buildSubCommandArgs(args)),
                this::handleSubCommandNotFound
        );
    }

    private void handleSubCommandNotFound() {
        master.getConsole().sendMessage("Use 'game template help' to view all game commands");
    }

    private @NotNull String[] buildSubCommandArgs(@NotNull String[] args) {
        return Arrays.stream(args)
                .skip(1)
                .toArray(String[]::new);
    }
}
