package com.github.deroq1337.cloud.master.commands.game;

import com.github.deroq1337.cloud.master.command.Command;
import com.github.deroq1337.cloud.master.command.CommandInfo;
import com.github.deroq1337.cloud.master.console.Console;
import com.github.deroq1337.cloud.master.game.GameManager;
import com.github.deroq1337.cloud.master.game.entity.Game;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@CommandInfo(
        name = "delete",
        description = "Deletes a game"
)
public class GameDeleteCommand implements Command {

    private final @NotNull GameManager gameManager;
    private final @NotNull Console console;
    private final @NotNull Logger logger;

    public GameDeleteCommand(@NotNull GameCommand gameCommand) {
        this.gameManager = gameCommand.getMaster().getGameManager();
        this.console = gameCommand.getMaster().getConsole();
        this.logger = gameCommand.getMaster().getLog();
    }

    @Override
    public void execute(@NotNull String[] args) {
        if (args.length < 1) {
            console.sendMessage("Syntax: 'game delete <id>'");
            return;
        }

        String id = args[0];

        Futures.addCallback(gameManager.getGameById(id), new FutureCallback<>() {
            @Override
            public void onSuccess(Optional<Game> optionalGame) {
                optionalGame.ifPresentOrElse(game -> {
                    Futures.addCallback(gameManager.deleteGameById(id), new FutureCallback<>() {
                        @Override
                        public void onSuccess(Boolean success) {
                            if (!success) {
                                console.sendMessage("Game was not deleted. Try again");
                                return;
                            }

                            // TODO: delete files
                            console.sendMessage(String.format("Game '%s' was deleted", id));
                        }

                        @Override
                        public void onFailure(@NotNull Throwable t) {
                            logger.error(t);
                        }
                    }, MoreExecutors.directExecutor());
                }, () -> console.sendMessage(String.format("Game '%s' was not found", id)));
            }

            @Override
            public void onFailure(@NotNull Throwable t) {
                logger.error(t);
            }
        }, MoreExecutors.directExecutor());
    }
}
