package com.github.deroq1337.cloud.master.console;

import com.github.deroq1337.cloud.master.CloudSystemMaster;
import org.jetbrains.annotations.NotNull;

public class ConsoleReader extends Thread {

    private static final String THREAD_NAME = "console";

    public ConsoleReader(@NotNull CloudSystemMaster master, @NotNull Console console) {
        super(new ConsoleReaderTask(master, console), THREAD_NAME);
        setDaemon(false);
    }
}
