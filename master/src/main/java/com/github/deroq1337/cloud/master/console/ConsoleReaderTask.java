package com.github.deroq1337.cloud.master.console;

import com.github.deroq1337.cloud.master.CloudSystemMaster;
import com.github.deroq1337.cloud.master.command.CommandMap;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jline.reader.LineReader;

import java.util.Arrays;

public class ConsoleReaderTask implements Runnable {

    private static final String LINE_PREFIX = "> ";

    private final @NotNull Logger logger;
    private final @NotNull Console console;
    private final @NotNull CommandMap commandMap;

    public ConsoleReaderTask(@NotNull CloudSystemMaster master, @NotNull Console console) {
        this.logger = master.getLog();
        this.console = console;
        this.commandMap = master.getCommandMap();
    }

    @Override
    public void run() {
        LineReader lineReader = console.getLineReader();

        while (true) {
            String line = lineReader.readLine(LINE_PREFIX);
            if (line == null || line.isEmpty()) {
                continue;
            }

            logger.debug(LINE_PREFIX + line);

            String[] commandTokens = line.split(" ");
            commandMap.getCommand(commandTokens[0]).ifPresentOrElse(
                    command -> command.execute(buildArgs(commandTokens)),
                    this::handleCommandNotFound
            );
        }
    }

    private @NotNull String[] buildArgs(@NotNull String[] commandTokens) {
        return Arrays.stream(commandTokens)
                .skip(1)
                .toArray(String[]::new);
    }

    private void handleCommandNotFound() {
        console.sendMessage("Command not found. Use 'help' to view all commands");
    }
}
