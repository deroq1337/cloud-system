package com.github.deroq1337.cloud.master.data.database;

import com.google.common.util.concurrent.ListenableFuture;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface Repository<E, ID> {

    @NotNull ListenableFuture<Boolean> create(@NotNull E game);

    @NotNull ListenableFuture<Boolean> deleteById(@NotNull ID id);

    @NotNull ListenableFuture<Optional<E>> findById(@NotNull ID id);
}
