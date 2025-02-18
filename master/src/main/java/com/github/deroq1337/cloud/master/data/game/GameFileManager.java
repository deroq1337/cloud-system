package com.github.deroq1337.cloud.master.data.game;

import com.github.deroq1337.cloud.master.data.CloudSystemMaster;
import com.github.deroq1337.cloud.master.data.utils.AsyncExecutor;
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

    private final @NotNull CloudSystemMaster master;
    private final @NotNull Path gamesDirectory;

    public GameFileManager(@NotNull CloudSystemMaster master, @NotNull String gamesDirectoryPath) {
        this.master = master;
        this.gamesDirectory = Paths.get(gamesDirectoryPath);
    }

    public @NotNull ListenableFuture<Boolean> initGameFiles(@NotNull String gameId, @NotNull InputStream serverJarInput) {
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
                master.getLog().error("Error initializing files of Game '{}'", gameId, e);
                return Futures.immediateFailedFuture(e);
            }
        }, AsyncExecutor.EXECUTOR_SERVICE);
    }

    public @NotNull ListenableFuture<Boolean> initTemplateFiles(@NotNull String gameId, @NotNull String templateId) {
        return Futures.submitAsync(() -> {
            try {
                Path gameDirectory = gamesDirectory.resolve(gameId + "/");
                Path templatesDirectory = gameDirectory.resolve("templates/");
                Files.createDirectories(templatesDirectory);

                Path templateDirectory = templatesDirectory.resolve(templateId + "/");
                Files.createDirectories(templateDirectory);

                Path gameServerJar = gameDirectory.resolve("server.jar");
                Path templateServerJar = templateDirectory.resolve(gameServerJar.getFileName().toString());
                Files.copy(gameServerJar, templateServerJar, StandardCopyOption.REPLACE_EXISTING);

                ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", templateServerJar.getFileName().toString());
                processBuilder.directory(templateDirectory.toFile());
                Process process = processBuilder.start();

                process.waitFor();
                process.destroy();

                return Futures.immediateFuture(true);
            } catch (Exception e) {
                master.getLog().error("Error initializing files of Template '{}'", templateId, e);
                return Futures.immediateFailedFuture(e);
            }
        }, AsyncExecutor.EXECUTOR_SERVICE);
    }
}
