package com.github.deroq1337.cloud.master.data.console;

import com.github.deroq1337.cloud.master.data.CloudSystemMaster;
import org.jetbrains.annotations.NotNull;

public class ConsoleReader extends Thread {

    private static final String THREAD_NAME = "console";

    public ConsoleReader(@NotNull CloudSystemMaster master) {
        super(new ConsoleReaderTask(master), THREAD_NAME);
        setDaemon(false);
    }
}
