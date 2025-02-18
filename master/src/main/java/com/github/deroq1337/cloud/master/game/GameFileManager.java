package com.github.deroq1337.cloud.master.game;

import com.github.deroq1337.cloud.master.database.AsyncExecutor;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class GameFileManager {

    private final @NotNull Path gamesDirectory;

    public GameFileManager(@NotNull String gamesDirectory) {
        this.gamesDirectory = Paths.get(gamesDirectory);
    }

    public @NotNull ListenableFuture<Boolean> initGameDirectory(@NotNull String gameId, @NotNull InputStream serverJarInput) {
        return Futures.submitAsync(() -> {
            try (serverJarInput) {
                Path gameDirectory = gamesDirectory.resolve(gameId + "/");
                Files.createDirectories(gameDirectory);

                Path serverJarPath = Paths.get(gameDirectory.toString(), "server.jar");
                Files.copy(serverJarInput, serverJarPath, StandardCopyOption.REPLACE_EXISTING);

                Path gameServerTemplatesDirectory = gameDirectory.resolve("templates/");
                Files.createDirectories(gameServerTemplatesDirectory);

                return Futures.immediateFuture(true);
            } catch (IOException e) {
                return Futures.immediateFailedFuture(e);
            }
        }, AsyncExecutor.EXECUTOR_SERVICE);
    }
}
