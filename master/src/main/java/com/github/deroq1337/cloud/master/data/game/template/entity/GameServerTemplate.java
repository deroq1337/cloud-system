package com.github.deroq1337.cloud.master.data.game.template.entity;

import com.github.deroq1337.cloud.master.data.game.template.models.GameServerType;
import lombok.*;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class GameServerTemplate {

    private @NotNull String id;
    private @NotNull String gameId;
    private int replicas;
    private int maxPlayers;
    private int ram;
    private float maxRamUsage;
    private int cpu;
    private float maxCpuUsage;
    private @NotNull GameServerType type;
    private long createdAt;
    private long updatedAt;

    public GameServerTemplate(@NotNull String id, @NotNull String gameId, int replicas, int maxPlayers, int ram, int cpu, GameServerType type) {
        this.id = id;
        this.gameId = gameId;
        this.replicas = replicas;
        this.maxPlayers = maxPlayers;
        this.ram = ram;
        this.maxRamUsage = 0.8f;
        this.cpu = cpu;
        this.maxCpuUsage = 0.8f;
        this.type = type;
        this.createdAt = System.currentTimeMillis();
    }
}
