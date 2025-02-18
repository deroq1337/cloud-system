package com.github.deroq1337.cloud.master.data.game.repository;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.github.deroq1337.cloud.master.data.utils.AsyncExecutor;
import com.github.deroq1337.cloud.master.data.database.Cassandra;
import com.github.deroq1337.cloud.master.data.game.entity.Game;
import com.github.deroq1337.cloud.master.data.game.models.MinecraftVersion;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultGameRepository implements GameRepository {

    private final @NotNull Session session;
    private final @NotNull PreparedStatement persist;
    private final @NotNull PreparedStatement deleteById;
    private final @NotNull PreparedStatement findById;

    public DefaultGameRepository(@NotNull Cassandra cassandra) {
        this.session = cassandra.getSession();
        createTable(session);

        this.persist = session.prepare("INSERT INTO cloud.games(id, name, minecraft_version, supported_minecraft_versions, created_at) " +
                "VALUES(?, ?, ?, ?, ?);");

        this.deleteById = session.prepare("DELETE FROM cloud.games " +
                "WHERE id = ?;");

        this.findById = session.prepare("SELECT * " +
                "FROM cloud.games " +
                "WHERE id = ?;");
    }

    private void createTable(@NotNull Session session) {
        session.execute("CREATE TABLE IF NOT EXISTS cloud.games(" +
                "id VARCHAR," +
                "name VARCHAR," +
                "description TEXT," +
                "image_url TEXT," +
                "minecraft_version VARCHAR," +
                "supported_minecraft_versions SET<VARCHAR>," +
                "created_at BIGINT," +
                "updated_at BIGINT," +
                "PRIMARY KEY(id)" +
                ");");
    }

    @Override
    public @NotNull ListenableFuture<Boolean> create(@NotNull Game game) {
        return Futures.transform(
                session.executeAsync(persist.bind(game.getId(), game.getName(), game.getMinecraftVersion().getVersion(), game.getSupportedMinecraftVersions().stream()
                        .map(MinecraftVersion::getVersion)
                        .collect(Collectors.toSet()), game.getCreatedAt())),
                ResultSet::wasApplied,
                AsyncExecutor.EXECUTOR_SERVICE
        );
    }

    @Override
    public @NotNull ListenableFuture<Boolean> deleteById(@NotNull String id) {
        return Futures.transform(
                session.executeAsync(deleteById.bind(id)),
                ResultSet::wasApplied,
                AsyncExecutor.EXECUTOR_SERVICE
        );
    }

    @Override
    public @NotNull ListenableFuture<Optional<Game>> findById(@NotNull String id) {
        return Futures.transform(session.executeAsync(findById.bind(id)), result -> {
            return Optional.ofNullable(result.one()).map(this::mapGameFromRow);
        }, AsyncExecutor.EXECUTOR_SERVICE);
    }

    private @NotNull Game mapGameFromRow(@NotNull Row row) {
        String minecraftVersionString = row.getString("minecraft_version");
        MinecraftVersion minecraftVersion = MinecraftVersion.fromString(minecraftVersionString)
                .orElseThrow(() -> new IllegalArgumentException("Unknown Minecraft version: " + minecraftVersionString));

        Set<MinecraftVersion> supportedMinecraftVersions = row.getSet("supported_minecraft_versions", String.class).stream()
                .map(version -> MinecraftVersion.fromString(version)
                        .orElseThrow(() -> new IllegalArgumentException("Unknown Minecraft version in supported list: " + version)))
                .collect(Collectors.toSet());

        return new Game(
                row.getString("id"),
                row.getString("name"),
                row.getString("description"),
                row.getString("image_url"),
                minecraftVersion,
                supportedMinecraftVersions,
                row.getLong("created_at"),
                row.getLong("updated_at")
        );
    }
}
