package com.github.deroq1337.cloud.master.data.game;

import com.github.deroq1337.cloud.master.data.CloudSystemMaster;
import com.github.deroq1337.cloud.master.data.game.entity.Game;
import com.github.deroq1337.cloud.master.data.game.repository.DefaultGameRepository;
import com.github.deroq1337.cloud.master.data.game.repository.GameRepository;
import com.github.deroq1337.cloud.master.data.game.template.DefaultGameServerTemplateManager;
import com.github.deroq1337.cloud.master.data.game.template.GameServerTemplateManager;
import com.github.deroq1337.cloud.master.data.utils.AsyncExecutor;
import com.github.deroq1337.cloud.master.data.utils.PaperDownloader;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.Getter;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Getter
public class DefaultGameManager implements GameManager {

    private final @NotNull GameRepository repository;
    private final @NotNull GameFileManager fileManager;
    private final @NotNull GameServerTemplateManager serverTemplateManager;
    private final @NotNull Logger log;

    public DefaultGameManager(@NotNull CloudSystemMaster master, @NotNull String gamesDirectory) {
        this.repository = new DefaultGameRepository(master.getCassandra());
        this.fileManager = new GameFileManager(master, gamesDirectory);
        this.serverTemplateManager = new DefaultGameServerTemplateManager(master, this);
        this.log = master.getLog();
    }

    @Override
    public @NotNull ListenableFuture<Boolean> createGame(@NotNull Game game) {
        String gameId = game.getId();
        String minecraftVersion = game.getMinecraftVersion().getVersion();
        log.info("Downloading server jar ({}) for game '{}'", minecraftVersion, gameId);

        return Futures.transformAsync(PaperDownloader.downloadServerJar(minecraftVersion), inputStream -> {
            if (inputStream == null) {
                log.error("Failed to download server jar ({}) for game '{}'", minecraftVersion, gameId);
                return Futures.immediateFuture(false);
            }

            log.info("Server jar ({}) downloaded successfully for game '{}'", minecraftVersion, gameId);
            log.info("Creating directory for game '{}'", gameId);

            return Futures.transformAsync(fileManager.initGameFiles(game.getId(), inputStream), filesCreated -> {
                if (!filesCreated) {
                    log.warn("Failed to create files for game '{}'", gameId);
                    return Futures.immediateFuture(false);
                }

                log.info("Files created for '{}'", gameId);

                return Futures.transformAsync(repository.create(game), gameCreated -> {
                    if (!gameCreated) {
                        log.warn("Failed to create game '{}'", gameId);
                        return Futures.immediateFuture(false);
                    }

                    log.info("Game '{}' created successfully", gameId);
                    return Futures.immediateFuture(true);
                }, AsyncExecutor.EXECUTOR_SERVICE);
            }, AsyncExecutor.EXECUTOR_SERVICE);
        }, AsyncExecutor.EXECUTOR_SERVICE);
    }

    @Override
    public @NotNull ListenableFuture<Boolean> deleteGameById(@NotNull String id) {
        return repository.deleteById(id.toUpperCase());
    }

    @Override
    public @NotNull ListenableFuture<Optional<Game>> getGameById(@NotNull String id) {
        return repository.findById(id.toUpperCase());
    }
}
