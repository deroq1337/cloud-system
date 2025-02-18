package com.github.deroq1337.cloud.master.game;

import com.github.deroq1337.cloud.master.CloudSystemMaster;
import com.github.deroq1337.cloud.master.game.entity.Game;
import com.github.deroq1337.cloud.master.game.repository.DefaultGameRepository;
import com.github.deroq1337.cloud.master.game.repository.GameRepository;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Getter
public class DefaultGameManager implements GameManager {

    private final @NotNull GameRepository repository;
    private final @NotNull GameFileManager gameFileManager;

    public DefaultGameManager(@NotNull CloudSystemMaster master, @NotNull String gamesDirectory) {
        this.repository = new DefaultGameRepository(master.getCassandra());
        this.gameFileManager = new GameFileManager(gamesDirectory);
    }

    @Override
    public @NotNull ListenableFuture<Boolean> createGame(@NotNull Game game) {
        return repository.persist(game);
    }

    @Override
    public @NotNull ListenableFuture<Boolean> deleteGameById(@NotNull String id) {
        return repository.deleteById(id);
    }

    @Override
    public @NotNull ListenableFuture<Optional<Game>> getGameById(@NotNull String id) {
        return repository.findById(id.toUpperCase());
    }
}
