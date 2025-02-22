package com.github.deroq1337.cloud.master.data;

import com.github.deroq1337.cloud.master.data.command.Command;
import com.github.deroq1337.cloud.master.data.command.CommandMap;
import com.github.deroq1337.cloud.master.data.commands.HelpCommand;
import com.github.deroq1337.cloud.master.data.commands.ShutdownCommand;
import com.github.deroq1337.cloud.master.data.commands.game.GameCommand;
import com.github.deroq1337.cloud.master.data.console.Console;
import com.github.deroq1337.cloud.master.data.utils.AsyncExecutor;
import com.github.deroq1337.cloud.master.data.database.Cassandra;
import com.github.deroq1337.cloud.master.data.game.DefaultGameManager;
import com.github.deroq1337.cloud.master.data.game.GameManager;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Getter
@Log4j2
public class CloudSystemMaster {

    private static final String CASSANDRA_CONFIG_PATH = "configs/cassandra.json";
    private static final String GAMES_DIRECTORY_PATH = "games/";

    private Cassandra cassandra;
    private GameManager gameManager;
    private Console console;
    private CommandMap commandMap;

    public void start() {
        this.cassandra = new Cassandra(this, CASSANDRA_CONFIG_PATH);
        this.gameManager = new DefaultGameManager(this, GAMES_DIRECTORY_PATH);
        this.console = new Console(this);
        this.commandMap = new CommandMap(initCommands());

        log.info("Master started");

        TEST_startServer();
    }

    public void shutdown() {
        log.info("Master shutting down");

        console.stopReading();
        commandMap.clear();
        cassandra.disconnect();
        AsyncExecutor.EXECUTOR_SERVICE.shutdown();

        log.info("Master shutdown completed");
    }

    private void TEST_startServer() {
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://localhost:2375")
                .build();

        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();

        try {
            CreateContainerCmd containerCmd = dockerClient.createContainerCmd("itzg/minecraft-server")
                    .withExposedPorts(ExposedPort.tcp(25565)) // Minecraft-Server-Port
                    .withPortBindings(PortBinding.parse("25565:25565")) // Portbindung
                    .withEnv("EULA=TRUE") // Akzeptiere die EULA
                    .withCmd("start"); // Minecraft-Server starten

            String containerId = containerCmd.exec().getId();

            StartContainerCmd startCmd = dockerClient.startContainerCmd(containerId);
            startCmd.exec();

            System.out.println("Minecraft Server Container läuft mit ID: " + containerId);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                dockerClient.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private @NotNull List<Command> initCommands() {
        return Arrays.asList(
                new HelpCommand(this),
                new ShutdownCommand(this),
                new GameCommand(this)
        );
    }

    public @NotNull Logger getLog() {
        return log;
    }
}
