package com.github.deroq1337.cloud.master.game.template.entity;

import com.github.deroq1337.cloud.master.game.template.models.GameServerCategory;
import lombok.*;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class GameServerTemplate {

    private @NotNull String gameId;
    private @NotNull String templateId;
    private int replicas;
    private int maxPlayers;
    private int ram;
    private int maxRamUsage;
    private @NotNull GameServerCategory category;
}
