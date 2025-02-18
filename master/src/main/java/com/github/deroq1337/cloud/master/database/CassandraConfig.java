package com.github.deroq1337.cloud.master.database;

import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class CassandraConfig {

    private @NotNull List<String> contactPoints;
    private int port;
    private @NotNull String username;
    private @NotNull String password;
    private @NotNull String namespace;
    private @NotNull PoolingConfig poolingLocal;
    private @NotNull PoolingConfig poolingRemote;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    public static class PoolingConfig {
        private int core;
        private int max;
    }
}
