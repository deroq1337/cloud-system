package com.github.deroq1337.cloud.master.exceptions;

import org.jetbrains.annotations.NotNull;

public class InvalidCommandException extends RuntimeException {

    public InvalidCommandException(@NotNull String message) {
        super(message);
    }
}
