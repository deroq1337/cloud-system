package com.github.deroq1337.cloud.master.data.game.template;

import com.github.deroq1337.cloud.master.data.game.template.entity.GameServerTemplate;
import com.google.common.util.concurrent.ListenableFuture;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface GameServerTemplateManager {

    @NotNull ListenableFuture<Boolean> createTemplate(@NotNull GameServerTemplate template, @NotNull String minecraftVersion);

    @NotNull ListenableFuture<Boolean> deleteTemplateById(@NotNull String id);

    @NotNull ListenableFuture<Optional<GameServerTemplate>> getTemplateById(@NotNull String id);
}
