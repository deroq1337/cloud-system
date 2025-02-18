package com.github.deroq1337.cloud.master.data.game;

import com.github.deroq1337.cloud.master.data.game.entity.Game;
import com.github.deroq1337.cloud.master.data.game.template.GameServerTemplateManager;
import com.google.common.util.concurrent.ListenableFuture;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface GameManager {

    @NotNull ListenableFuture<Boolean> createGame(@NotNull Game game);

    @NotNull ListenableFuture<Boolean> deleteGameById(@NotNull String id);

    @NotNull ListenableFuture<Optional<Game>> getGameById(@NotNull String id);

    @NotNull GameServerTemplateManager getServerTemplateManager();

    @NotNull GameFileManager getFileManager();
}
