package com.github.deroq1337.cloud.master.data.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncExecutor {

    public static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
}
