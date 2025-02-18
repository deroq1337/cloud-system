package com.github.deroq1337.cloud.master.data.console;

import com.github.deroq1337.cloud.master.data.CloudSystemMaster;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jline.reader.LineReader;

import java.util.Arrays;

@RequiredArgsConstructor
public class ConsoleReaderTask implements Runnable {

    private static final String LINE_PREFIX = "> ";

    private final @NotNull CloudSystemMaster master;

    @Override
    public void run() {
        LineReader lineReader = master.getConsole().getLineReader();

        while (true) {
            String line = lineReader.readLine(LINE_PREFIX);
            if (line == null || line.isEmpty()) {
                continue;
            }

            master.getLog().debug(LINE_PREFIX + line);

            String[] commandTokens = line.split(" ");
            master.getCommandMap().getCommand(commandTokens[0]).ifPresentOrElse(
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
        master.getConsole().sendMessage("Command not found. Use 'help' to view all commands");
    }
}
