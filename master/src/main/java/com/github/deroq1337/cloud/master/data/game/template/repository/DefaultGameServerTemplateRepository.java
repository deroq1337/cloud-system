package com.github.deroq1337.cloud.master.data.game.template.repository;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.github.deroq1337.cloud.master.data.database.Cassandra;
import com.github.deroq1337.cloud.master.data.game.template.entity.GameServerTemplate;
import com.github.deroq1337.cloud.master.data.game.template.models.GameServerType;
import com.github.deroq1337.cloud.master.data.utils.AsyncExecutor;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class DefaultGameServerTemplateRepository implements GameServerTemplateRepository {

    private final @NotNull Session session;
    private final @NotNull PreparedStatement persist;
    private final @NotNull PreparedStatement deleteById;
    private final @NotNull PreparedStatement findById;

    public DefaultGameServerTemplateRepository(@NotNull Cassandra cassandra) {
        this.session = cassandra.getSession();
        createTable(session);

        this.persist = session.prepare("INSERT INTO cloud.game_server_templates(id, game_id, replicas, max_players, ram, max_ram_usage, cpu, max_cpu_usage, type, created_at) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");

        this.deleteById = session.prepare("DELETE FROM cloud.game_server_templates " +
                "WHERE id = ?;");

        this.findById = session.prepare("SELECT * " +
                "FROM cloud.game_server_templates " +
                "WHERE id = ?;");
    }

    private void createTable(@NotNull Session session) {
        session.execute("CREATE TABLE IF NOT EXISTS cloud.game_server_templates(" +
                "id VARCHAR," +
                "game_id VARCHAR," +
                "replicas INT," +
                "max_players INT," +
                "ram INT," +
                "max_ram_usage FLOAT," +
                "cpu INT," +
                "max_cpu_usage FLOAT," +
                "type VARCHAR," +
                "created_at BIGINT," +
                "updated_at BIGINT," +
                "PRIMARY KEY(id)" +
                ");");
    }

    @Override
    public @NotNull ListenableFuture<Boolean> create(@NotNull GameServerTemplate template) {
        return Futures.transform(
                session.executeAsync(persist.bind(template.getId(), template.getGameId(), template.getReplicas(), template.getMaxPlayers(), template.getRam(), template.getMaxRamUsage(),
                        template.getCpu(), template.getMaxCpuUsage(), template.getType().toString(), template.getCreatedAt())),
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
    public @NotNull ListenableFuture<Optional<GameServerTemplate>> findById(@NotNull String id) {
        return Futures.transform(session.executeAsync(findById.bind(id)), result -> {
            return Optional.ofNullable(result.one()).map(this::mapTemplateFromRow);
        }, AsyncExecutor.EXECUTOR_SERVICE);
    }

    private @NotNull GameServerTemplate mapTemplateFromRow(@NotNull Row row) {
        return new GameServerTemplate(
                row.getString("id"),
                row.getString("game_id"),
                row.getInt("replicas"),
                row.getInt("max_players"),
                row.getInt("ram"),
                row.getFloat("max_ram_usage"),
                row.getInt("cpu"),
                row.getFloat("max_cpu_usage"),
                GameServerType.valueOf(row.getString("type")),
                row.getLong("created_at"),
                row.getLong("updated_at")
        );
    }
}
