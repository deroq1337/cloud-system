package com.github.deroq1337.cloud.master.console;

import com.github.deroq1337.cloud.master.CloudSystemMaster;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

@Getter
public class Console {

    private final @NotNull CloudSystemMaster master;
    private final @NotNull Terminal terminal;
    private final @NotNull LineReader lineReader;
    private final @NotNull ConsoleReader consoleReader;

    public Console(@NotNull CloudSystemMaster master) {
        this.master = master;
        this.terminal = createTerminal();
        this.lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();

        this.consoleReader = new ConsoleReader(master, this);
        startReading();
    }

    public void startReading() {
        consoleReader.start();
    }

    public void stopReading() {
        try {
            consoleReader.interrupt();
            terminal.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(@NotNull String message) {
        synchronized (terminal) {
            terminal.writer().println(message);
            terminal.writer().flush();
        }

        master.getLog().debug(message);
    }

    private @NotNull Terminal createTerminal() {
        try {
            return TerminalBuilder.terminal();
        } catch (IOException e) {
            try {
                return TerminalBuilder.builder().build();
            } catch (IOException ex) {
                throw new RuntimeException("Failed to create terminal", ex);
            }
        }
    }
}
