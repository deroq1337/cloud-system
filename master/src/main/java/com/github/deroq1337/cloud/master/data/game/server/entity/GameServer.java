package com.github.deroq1337.cloud.master.data.game.server.entity;

import com.github.deroq1337.cloud.master.data.game.server.models.GameServerState;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class GameServer {

    private @NotNull UUID id;
    private @NotNull String containerId;
    private @NotNull String gameId;
    private @NotNull String templateId;
    private @NotNull String ip;
    private @NotNull GameServerState state;
    private int playerCount;
    private long startedAt;
    private long stoppedAt;
}
