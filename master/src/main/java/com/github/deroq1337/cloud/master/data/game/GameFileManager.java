package com.github.deroq1337.cloud.master.data.game;

import com.github.deroq1337.cloud.master.data.CloudSystemMaster;
import com.github.deroq1337.cloud.master.data.game.entity.Game;
import com.github.deroq1337.cloud.master.data.utils.AsyncExecutor;
import com.github.deroq1337.cloud.master.data.utils.PaperDownloader;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.*;
import java.util.concurrent.TimeUnit;

public class GameFileManager {

    private static final String SERVER_JAR_FILE_NAME = "server.jar";
    private static final long SERVER_JAR_EXECUTION_TIMEOUT = 90 * 1000L;

    private final @NotNull Logger log;
    private final @NotNull Path gamesDirectory;

    public GameFileManager(@NotNull CloudSystemMaster master, @NotNull String gamesDirectoryPath) {
        this.log = master.getLog();
        this.gamesDirectory = Paths.get(gamesDirectoryPath);
    }

    public @NotNull ListenableFuture<Boolean> initGameFiles(@NotNull Game game) {
        String gameId = game.getId();

        return Futures.submitAsync(() -> {
            Path gameDirectory = gamesDirectory.resolve(gameId).resolve("templates");
            Files.createDirectories(gameDirectory);

            return Futures.transformAsync(initServerJar(gameId, game.getMinecraftVersion().getVersion()), success -> {
                if (!success) {
                    log.error("Failed to init server jar for game '{}'", gameId);
                    return Futures.immediateFuture(false);
                }

                log.info("Initialized game files for '{}'", gameId);
                return Futures.immediateFuture(true);
            }, MoreExecutors.directExecutor());
        }, AsyncExecutor.EXECUTOR_SERVICE);
    }

    public @NotNull ListenableFuture<Boolean> initTemplateFiles(@NotNull String gameId, @NotNull String templateId, @NotNull String minecraftVersion) {
        return Futures.submitAsync(() -> {
            try {
                Path templateDirectory = getTemplateDirectory(gameId, templateId);
                Files.createDirectories(templateDirectory);

                Path gameServerJar = gamesDirectory.resolve(gameId).resolve(SERVER_JAR_FILE_NAME);
                if (!Files.exists(gameServerJar)) {
                    log.info("Server jar not found for game '{}'. Initializing server jar", gameId);

                    if (!initServerJar(gameId, minecraftVersion).get()) {
                        return Futures.immediateFailedFuture(new IllegalStateException("Failed to initialize server jar for game '" + gameId + "'"));
                    }
                }

                Path templateServerJar = templateDirectory.resolve(gameServerJar.getFileName().toString());
                Files.copy(gameServerJar, templateServerJar, StandardCopyOption.REPLACE_EXISTING);

                log.info("Running server jar to generate default paper files");

                ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", templateServerJar.getFileName().toString());
                processBuilder.directory(templateDirectory.toFile());
                Process process = processBuilder.start();

                boolean finished = process.waitFor(SERVER_JAR_EXECUTION_TIMEOUT, TimeUnit.MILLISECONDS);
                if (!finished) {
                    process.destroyForcibly();
                    throw new IllegalStateException("Server initialization timed out for template: " + templateId);
                }

                log.info("Initialized template files for '{}'", templateId);
                return Futures.immediateFuture(true);
            } catch (Exception e) {
                log.error("Error initializing files of template '{}'", templateId, e);
                return Futures.immediateFailedFuture(e);
            }
        }, AsyncExecutor.EXECUTOR_SERVICE);
    }
    
    private @NotNull ListenableFuture<Boolean> initServerJar(@NotNull String gameId, @NotNull String minecraftVersion) {
        return Futures.transformAsync(PaperDownloader.downloadServerJar(minecraftVersion), serverJarInput -> {
            if (serverJarInput == null) {
                log.error("Failed to download server jar ({}) for game '{}'", minecraftVersion, gameId);
                return Futures.immediateFuture(false);
            }

            log.info("Server jar ({}) downloaded successfully for game '{}'", minecraftVersion, gameId);
            log.info("Creating server jar in directory for game '{}'", gameId);

            Path serverJarPath = gamesDirectory.resolve(gameId).resolve(SERVER_JAR_FILE_NAME);
            Files.copy(serverJarInput, serverJarPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Created server jar in directory for game '{}'", gameId);
            return Futures.immediateFuture(true);
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

                log.info("Agreed to EULA for template '{}'", templateId);
                return Futures.immediateFuture(true);
            } catch (Exception e) {
                log.error("Error agreeing to EULA for template '{}'", templateId, e);
                return Futures.immediateFailedFuture(e);
            }
        }, AsyncExecutor.EXECUTOR_SERVICE);
    }
}
