package com.github.deroq1337.cloud.master.data.commands.game.template;

import com.github.deroq1337.cloud.master.data.command.Command;
import com.github.deroq1337.cloud.master.data.command.CommandInfo;
import com.github.deroq1337.cloud.master.data.console.Console;
import com.github.deroq1337.cloud.master.data.game.GameManager;
import com.github.deroq1337.cloud.master.data.game.entity.Game;
import com.github.deroq1337.cloud.master.data.game.template.GameServerTemplateManager;
import com.github.deroq1337.cloud.master.data.game.template.entity.GameServerTemplate;
import com.github.deroq1337.cloud.master.data.game.template.models.GameServerType;
import com.github.deroq1337.cloud.master.data.utils.AsyncExecutor;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

@CommandInfo(
        name = "create",
        description = "Creates a new game server template"
)
public class GameTemplateCreateCommand implements Command {

    private final @NotNull GameManager gameManager;
    private final @NotNull GameServerTemplateManager serverTemplateManager;
    private final @NotNull Console console;
    private final @NotNull Logger log;

    public GameTemplateCreateCommand(@NotNull GameTemplateCommand gameTemplateCommand) {
        this.gameManager = gameTemplateCommand.getMaster().getGameManager();
        this.serverTemplateManager = gameTemplateCommand.getMaster().getGameManager().getServerTemplateManager();
        this.console = gameTemplateCommand.getMaster().getConsole();
        this.log = gameTemplateCommand.getMaster().getLog();
    }

    @Override
    public void execute(@NotNull String[] args) {
        if (args.length < 7) {
            console.sendMessage("Syntax: 'game template create <id> <gameId> <replicas> <maxPlayers> <ram> <cpu> <type>'");
            return;
        }

        String id = args[0].toUpperCase();
        Futures.addCallback(serverTemplateManager.getTemplateById(id), new FutureCallback<>() {
            @Override
            public void onSuccess(Optional<GameServerTemplate> optionalTemplate) {
                optionalTemplate.ifPresentOrElse(template -> {
                    console.sendMessage(String.format("Template '%s' already exists", id));
                }, () -> {
                    String gameId = args[1];

                    Futures.addCallback(gameManager.getGameById(gameId), new FutureCallback<>() {
                        @Override
                        public void onSuccess(Optional<Game> optionalGame) {
                            optionalGame.ifPresentOrElse(game -> {
                                String typeAsString = args[6].toUpperCase();

                                try {
                                    int replicas = Integer.parseInt(args[2]);
                                    int maxPlayers = Integer.parseInt(args[3]);
                                    int ram = Integer.parseInt(args[4]);
                                    int cpu = Integer.parseInt(args[5]);
                                    GameServerType type = GameServerType.valueOf(typeAsString);

                                    GameServerTemplate template = new GameServerTemplate(id, gameId, replicas, maxPlayers, ram, cpu, type);
                                    Futures.addCallback(serverTemplateManager.createTemplate(template, game.getMinecraftVersion().getVersion()), new FutureCallback<>() {
                                        @Override
                                        public void onSuccess(Boolean success) {
                                            if (!success) {
                                                console.sendMessage("Template was not created. Try again");
                                                return;
                                            }

                                            console.sendMessage(String.format("Template '%s' was created", id));

                                            // just agree to eula because its a test project
                                            Futures.addCallback(gameManager.getFileManager().agreeEULA(gameId, id), new FutureCallback<>() {
                                                @Override
                                                public void onSuccess(Boolean result) {
                                                    if (!result) {
                                                        log.error("Failed to agree to EULA for template '{}'", id);
                                                        return;
                                                    }

                                                    log.info("EULA agreed for template '{}'", id);
                                                }

                                                @Override
                                                public void onFailure(@NotNull Throwable t) {
                                                    log.error("Failed to agree to EULA for template '{}'", id, t);
                                                }
                                            }, MoreExecutors.directExecutor());
                                        }

                                        @Override
                                        public void onFailure(@NotNull Throwable t) {
                                            log.error("Failed to create template '{}'", id, t);
                                        }
                                    }, AsyncExecutor.EXECUTOR_SERVICE);
                                } catch (NumberFormatException e) {
                                    console.sendMessage("You have entered an invalid number. Enter a valid number");
                                } catch (IllegalArgumentException e) {
                                    console.sendMessage(String.format("Unknown Server type: %s. Available types: %s", typeAsString, Arrays.toString(GameServerType.values())));
                                }
                            }, () -> console.sendMessage(String.format("Game '%s' was not found", id)));
                        }

                        @Override
                        public void onFailure(@NotNull Throwable t) {
                            log.error("Error retrieving game by ID '{}'", gameId, t);
                        }
                    }, AsyncExecutor.EXECUTOR_SERVICE);
                });
            }

            @Override
            public void onFailure(@NotNull Throwable t) {
                log.error("Error retrieving template by ID '{}'", id, t);
            }
        }, AsyncExecutor.EXECUTOR_SERVICE);
    }
}