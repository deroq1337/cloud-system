package com.github.deroq1337.cloud.master.data.commands.game;

import com.github.deroq1337.cloud.master.data.command.Command;
import com.github.deroq1337.cloud.master.data.command.CommandInfo;
import com.github.deroq1337.cloud.master.data.console.Console;
import com.github.deroq1337.cloud.master.data.game.GameManager;
import com.github.deroq1337.cloud.master.data.game.entity.Game;
import com.github.deroq1337.cloud.master.data.game.models.MinecraftVersion;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.Collectors;

@CommandInfo(
        name = "info",
        description = "Displays game information"
)
public class GameInfoCommand implements Command {

    private final @NotNull GameManager gameManager;
    private final @NotNull Console console;
    private final @NotNull Logger log;

    public GameInfoCommand(@NotNull GameCommand gameCommand) {
        this.gameManager = gameCommand.getMaster().getGameManager();
        this.console = gameCommand.getMaster().getConsole();
        this.log = gameCommand.getMaster().getLog();
    }

    @Override
    public void execute(@NotNull String[] args) {
        if (args.length < 1) {
            console.sendMessage("Syntax: 'game info <id>'");
            return;
        }

        String id = args[0];

        Futures.addCallback(gameManager.getGameById(id), new FutureCallback<>() {
            @Override
            public void onSuccess(Optional<Game> optionalGame) {
                optionalGame.ifPresentOrElse(game ->
                        printInfo(game),
                        () -> console.sendMessage(String.format("Game %s was not found", id))
                );
            }

            @Override
            public void onFailure(@NotNull Throwable t) {
                log.error(t);
            }
        }, MoreExecutors.directExecutor());
    }

    private void printInfo(@NotNull Game game) {
        console.sendMessage(String.format(
                """
                        -=-=-=-=-= Information about %s -=-=-=-=-=
                        ID: %s
                        Name: %s
                        Description: %s
                        Version: %s
                        Supported versions: %s
                        -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=""",
                game.getId(),
                game.getId(),
                game.getName(),
                game.getDescription(),
                game.getMinecraftVersion().getVersion(),
                game.getSupportedMinecraftVersions().stream()
                        .map(MinecraftVersion::getVersion)
                        .collect(Collectors.toSet())
        ));
    }
}
