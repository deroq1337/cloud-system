package com.github.deroq1337.cloud.master.commands.game;

import com.github.deroq1337.cloud.master.command.Command;
import com.github.deroq1337.cloud.master.command.CommandInfo;
import com.github.deroq1337.cloud.master.console.Console;
import com.github.deroq1337.cloud.master.database.AsyncExecutor;
import com.github.deroq1337.cloud.master.game.GameManager;
import com.github.deroq1337.cloud.master.game.entity.Game;
import com.github.deroq1337.cloud.master.game.models.MinecraftVersion;
import com.github.deroq1337.cloud.master.utils.PaperDownloader;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
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
    private final @NotNull Logger logger;

    public GameCreateCommand(@NotNull GameCommand gameCommand) {
        this.gameManager = gameCommand.getMaster().getGameManager();
        this.console = gameCommand.getMaster().getConsole();
        this.logger = gameCommand.getMaster().getLog();
    }

    @Override
    public void execute(@NotNull String[] args) {
        if (args.length < 3) {
            console.sendMessage("Syntax: 'game create <id> <name> <version>'");
            return;
        }

        String id = args[0];
        Futures.addCallback(gameManager.getGameById(id), new FutureCallback<>() {
            @Override
            public void onSuccess(Optional<Game> optionalGame) {
                if (optionalGame.isPresent()) {
                    console.sendMessage(String.format("Game '%s' already exists", id));
                    return;
                }

                String version = args[2];

                MinecraftVersion.fromString(version).ifPresentOrElse(minecraftVersion -> {
                    Game game = new Game(id, args[1], minecraftVersion);

                    Futures.addCallback(createGame(game), new FutureCallback<>() {
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
                            logger.error("Failed to create game '{}'", id, t);
                        }
                    }, AsyncExecutor.EXECUTOR_SERVICE);
                }, () -> console.sendMessage(String.format("Unknown Minecraft version: %s. Available versions: %s", version, MinecraftVersion.getVersions())));
            }

            @Override
            public void onFailure(@NotNull Throwable t) {
                logger.error("Error retrieving game by ID '{}'", id, t);
            }
        }, AsyncExecutor.EXECUTOR_SERVICE);
    }

    private @NotNull ListenableFuture<Boolean> createGame(@NotNull Game game) {
        String gameId = game.getId();
        String minecraftVersion = game.getMinecraftVersion().getVersion();
        logger.info("Downloading server jar ({}) for game '{}'", minecraftVersion, gameId);

        return Futures.transformAsync(PaperDownloader.downloadServerJar(minecraftVersion), inputStream -> {
            if (inputStream == null) {
                logger.error("Failed to download server jar ({}) for game '{}'", minecraftVersion, gameId);
                return Futures.immediateFuture(false);
            }

            logger.info("Server jar ({}) downloaded successfully for game '{}'", minecraftVersion, gameId);
            logger.info("Creating directory for game '{}'", gameId);

            return Futures.transformAsync(gameManager.getGameFileManager().initGameDirectory(game.getId(), inputStream), directoryCreated -> {
                if (!directoryCreated) {
                    logger.error("Failed to create directory for game '{}'", gameId);
                    return Futures.immediateFuture(false);
                }

                logger.info("Directory created for '{}'", gameId);

                return Futures.transformAsync(gameManager.createGame(game), gameCreated -> {
                    if (!gameCreated) {
                        logger.error("Failed to create game '{}'", gameId);
                        return Futures.immediateFuture(false);
                    }

                    logger.info("Game '{}' created successfully", gameId);
                    return Futures.immediateFuture(true);
                }, AsyncExecutor.EXECUTOR_SERVICE);
            }, AsyncExecutor.EXECUTOR_SERVICE);
        }, AsyncExecutor.EXECUTOR_SERVICE);
    }
}
