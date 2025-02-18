package com.github.deroq1337.cloud.master.game.repository;

import com.github.deroq1337.cloud.master.game.entity.Game;
import com.google.common.util.concurrent.ListenableFuture;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface GameRepository {

    @NotNull ListenableFuture<Boolean> persist(@NotNull Game game);

    @NotNull ListenableFuture<Boolean> deleteById(@NotNull String id);

    @NotNull ListenableFuture<Optional<Game>> findById(@NotNull String id);
}
