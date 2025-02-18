package com.github.deroq1337.cloud.master;

import com.github.deroq1337.cloud.master.command.Command;
import com.github.deroq1337.cloud.master.command.CommandMap;
import com.github.deroq1337.cloud.master.commands.HelpCommand;
import com.github.deroq1337.cloud.master.commands.ShutdownCommand;
import com.github.deroq1337.cloud.master.commands.game.GameCommand;
import com.github.deroq1337.cloud.master.console.Console;
import com.github.deroq1337.cloud.master.database.AsyncExecutor;
import com.github.deroq1337.cloud.master.database.Cassandra;
import com.github.deroq1337.cloud.master.game.DefaultGameManager;
import com.github.deroq1337.cloud.master.game.GameManager;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@Getter
@Log4j2
public class CloudSystemMaster {

    private static final String CASSANDRA_CONFIG_PATH = "configs/cassandra.json";
    private static final String GAMES_DIRECTORY_PATH = "games/";

    private Cassandra cassandra;
    private CommandMap commandMap;
    private Console console;
    private GameManager gameManager;

    public void start() {
        this.cassandra = new Cassandra(this, CASSANDRA_CONFIG_PATH);
        this.gameManager = new DefaultGameManager(this, GAMES_DIRECTORY_PATH);
        this.commandMap = new CommandMap(initCommands());
        this.console = new Console(this);

        log.info("Master started");
    }

    public void shutdown() {
        log.info("Master shutting down");

        console.stopReading();
        commandMap.clear();
        cassandra.disconnect();
        AsyncExecutor.EXECUTOR_SERVICE.shutdown();

        log.info("Master shutdown completed");
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
