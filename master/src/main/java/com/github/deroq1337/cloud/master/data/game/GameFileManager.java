package com.github.deroq1337.cloud.master.data.game;

import com.github.deroq1337.cloud.master.data.CloudSystemMaster;
import com.github.deroq1337.cloud.master.data.utils.AsyncExecutor;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

public class GameFileManager {

    private static final String SERVER_JAR_FILE_NAME = "server.jar";
    private static final long SERVER_JAR_EXECUTION_TIMEOUT = 90 * 1000L;

    private final @NotNull CloudSystemMaster master;
    private final @NotNull Path gamesDirectory;

    public GameFileManager(@NotNull CloudSystemMaster master, @NotNull String gamesDirectoryPath) {
        this.master = master;
        this.gamesDirectory = Paths.get(gamesDirectoryPath);
    }

    public @NotNull ListenableFuture<Boolean> initGameFiles(@NotNull String gameId, @NotNull InputStream serverJarInput) {
        return Futures.submitAsync(() -> {
            try (serverJarInput) {
                Path gameDirectory = gamesDirectory.resolve(gameId).resolve("templates");
                Files.createDirectories(gameDirectory);

                Path serverJarPath = gameDirectory.resolve(SERVER_JAR_FILE_NAME);
                Files.copy(serverJarInput, serverJarPath, StandardCopyOption.REPLACE_EXISTING);

                master.getLog().info("Initialized game files for '{}'", gameId);
                return Futures.immediateFuture(true);
            } catch (IOException e) {
                master.getLog().error("Error initializing files of game '{}'", gameId, e);
                return Futures.immediateFailedFuture(e);
            }
        }, AsyncExecutor.EXECUTOR_SERVICE);
    }

    public @NotNull ListenableFuture<Boolean> initTemplateFiles(@NotNull String gameId, @NotNull String templateId) {
        return Futures.submitAsync(() -> {
            try {
                Path templateDirectory = getTemplateDirectory(gameId, templateId);
                Files.createDirectories(templateDirectory);

                Path gameServerJar = gamesDirectory.resolve(gameId).resolve(SERVER_JAR_FILE_NAME);
                Path templateServerJar = templateDirectory.resolve(gameServerJar.getFileName().toString());
                Files.copy(gameServerJar, templateServerJar, StandardCopyOption.REPLACE_EXISTING);

                ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", templateServerJar.getFileName().toString());
                processBuilder.directory(templateDirectory.toFile());
                Process process = processBuilder.start();

                boolean finished = process.waitFor(SERVER_JAR_EXECUTION_TIMEOUT, TimeUnit.MILLISECONDS);
                if (!finished) {
                    process.destroyForcibly();
                    throw new IllegalStateException("Server initialization timed out for template: " + templateId);
                }

                master.getLog().info("Initialized template files for '{}'", templateId);
                return Futures.immediateFuture(true);
            } catch (Exception e) {
                master.getLog().error("Error initializing files of template '{}'", templateId, e);
                return Futures.immediateFailedFuture(e);
            }
        }, AsyncExecutor.EXECUTOR_SERVICE);
    }

    private @NotNull Path getTemplateDirectory(@NotNull String gameId, @NotNull String templateId) {
        return gamesDirectory.resolve(gameId)
                .resolve("templates")
                .resolve(templateId);
    }

    public @NotNull ListenableFuture<Boolean> agreeEULA(@NotNull String gameId, @NotNull String templateId) {
        return Futures.submitAsync(() -> {
            try {
                Path eulaFile = getTemplateDirectory(gameId, templateId).resolve("eula.txt");
                Files.writeString(eulaFile, "eula=true", StandardOpenOption.TRUNCATE_EXISTING);

                master.getLog().info("Agreed to EULA for template '{}'", templateId);
                return Futures.immediateFuture(true);
            } catch (Exception e) {
                master.getLog().error("Error agreeing to EULA for template '{}'", templateId, e);
                return Futures.immediateFailedFuture(e);
            }
        }, AsyncExecutor.EXECUTOR_SERVICE);
    }
}
