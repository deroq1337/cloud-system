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
    private GameManager gameManager;
    private Console console;
    private CommandMap commandMap;

    public void start() {
        this.cassandra = new Cassandra(this, CASSANDRA_CONFIG_PATH);
        this.gameManager = new DefaultGameManager(this, GAMES_DIRECTORY_PATH);
        this.console = new Console(this);
        this.commandMap = new CommandMap(initCommands());
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
