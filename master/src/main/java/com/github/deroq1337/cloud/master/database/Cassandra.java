package com.github.deroq1337.cloud.master.database;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.Session;
import com.github.deroq1337.cloud.master.CloudSystemMaster;
import com.github.deroq1337.cloud.master.utils.ConfigLoader;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Getter
@Log4j2
public class Cassandra {

    private final @NotNull CloudSystemMaster master;
    private final @NotNull Cluster cluster;

    private Optional<Session> session = Optional.empty();

    public Cassandra(@NotNull CloudSystemMaster master, @NotNull String configPath) {
        this.master = master;

        CassandraConfig config = ConfigLoader.load(configPath, CassandraConfig.class);
        PoolingOptions poolingOptions = new PoolingOptions()
                .setConnectionsPerHost(HostDistance.LOCAL, config.getPoolingLocal().getCore(), config.getPoolingLocal().getMax())
                .setConnectionsPerHost(HostDistance.REMOTE, config.getPoolingRemote().getCore(), config.getPoolingRemote().getMax());

        this.cluster = Cluster.builder()
                .addContactPoints(config.getContactPoints().toArray(String[]::new))
                .withPort(config.getPort())
                .withCredentials(config.getUsername(), config.getPassword())
                .withPoolingOptions(poolingOptions)
                .build();

        connect();
    }

    public void connect() {
        this.session = Optional.of(cluster.connect());
        master.getLog().info("Cassandra connected successfully. Session established");
    }

    public void disconnect() {
        session.ifPresent(session -> {
            session.close();
            this.session = Optional.empty();
            master.getLog().info("Cassandra session closed");
        });

        cluster.close();
        master.getLog().info("Cassandra cluster closed");
    }

    public @NotNull Session getSession() {
        return session.orElseThrow(() -> new IllegalStateException("No session present because cluster is not connected yet"));
    }
}
