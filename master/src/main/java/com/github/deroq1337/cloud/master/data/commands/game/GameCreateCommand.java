package com.github.deroq1337.cloud.master.data.commands.game;

import com.github.deroq1337.cloud.master.data.command.Command;
import com.github.deroq1337.cloud.master.data.command.CommandInfo;
import com.github.deroq1337.cloud.master.data.console.Console;
import com.github.deroq1337.cloud.master.data.utils.AsyncExecutor;
import com.github.deroq1337.cloud.master.data.game.GameManager;
import com.github.deroq1337.cloud.master.data.game.entity.Game;
import com.github.deroq1337.cloud.master.data.game.models.MinecraftVersion;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@CommandInfo(
        name = "create",
        description = "Creates a new game"
)
public class GameCreateCommand implements Command {

    private final @NotNull GameManager gameManager;
    private final @NotNull Console console;
    private final @NotNull Logger log;

    public GameCreateCommand(@NotNull GameCommand gameCommand) {
        this.gameManager = gameCommand.getMaster().getGameManager();
        this.console = gameCommand.getMaster().getConsole();
        this.log = gameCommand.getMaster().getLog();
    }

    @Override
    public void execute(@NotNull String[] args) {
        if (args.length < 3) {
            console.sendMessage("Syntax: 'game create <id> <name> <version>'");
            return;
        }

        String id = args[0].toUpperCase();
        Futures.addCallback(gameManager.getGameById(id), new FutureCallback<>() {
            @Override
            public void onSuccess(Optional<Game> optionalGame) {
                optionalGame.ifPresentOrElse(unused -> {
                    console.sendMessage(String.format("Game '%s' already exists", id));
                }, () -> {
                    String version = args[2];

                    MinecraftVersion.fromString(version).ifPresentOrElse(minecraftVersion -> {
                        Game game = new Game(id, args[1], minecraftVersion);

                        Futures.addCallback(gameManager.createGame(game), new FutureCallback<>() {
                            @Override
                            public void onSuccess(Boolean success) {
                                if (!success) {
                                    console.sendMessage("Game was not created. Try again");
                                    return;
                                }

                                console.sendMessage(String.format("Game '%s' was created", id));
                            }

                            @Override
                            public void onFailure(@NotNull Throwable t) {
                                log.error("Failed to create game '{}'", id, t);
                            }
                        }, AsyncExecutor.EXECUTOR_SERVICE);
                    }, () -> console.sendMessage(String.format("Unknown Minecraft version: %s. Available versions: %s", version, MinecraftVersion.getVersions())));
                });
            }

            @Override
            public void onFailure(@NotNull Throwable t) {
                log.error("Error retrieving game by ID '{}'", id, t);
            }
        }, AsyncExecutor.EXECUTOR_SERVICE);
    }
}
