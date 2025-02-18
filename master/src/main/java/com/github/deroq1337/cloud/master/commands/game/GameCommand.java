package com.github.deroq1337.cloud.master.commands.game;

import com.github.deroq1337.cloud.master.CloudSystemMaster;
import com.github.deroq1337.cloud.master.command.Command;
import com.github.deroq1337.cloud.master.command.CommandInfo;
import com.github.deroq1337.cloud.master.command.CommandMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@Getter
@CommandInfo(
        name = "game",
        description = "Game administration command"
)
public class GameCommand implements Command {

    private final @NotNull CloudSystemMaster master;
    private final @NotNull CommandMap subCommandMap;

    public GameCommand(@NotNull CloudSystemMaster master) {
        this.master = master;
        this.subCommandMap = new CommandMap(Arrays.asList(
                new GameHelpCommand(this),
                new GameCreateCommand(this),
                new GameDeleteCommand(this),
                new GameInfoCommand(this)
        ));
    }

    @Override
    public void execute(@NotNull String[] args) {
        if (args.length == 0) {
            handleSubCommandNotFound();
            return;
        }

        subCommandMap.getCommand(args[0]).ifPresentOrElse(subCommand ->
                        subCommand.execute(buildSubCommandArgs(args)),
                this::handleSubCommandNotFound
        );
    }

    private void handleSubCommandNotFound() {
        master.getConsole().sendMessage("Use 'game help' to view all game commands");
    }

    private @NotNull String[] buildSubCommandArgs(@NotNull String[] args) {
        return Arrays.stream(args)
                .skip(1)
                .toArray(String[]::new);
    }
}
