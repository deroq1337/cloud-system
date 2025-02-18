package com.github.deroq1337.cloud.master.data.game.entity;

import com.github.deroq1337.cloud.master.data.game.models.MinecraftVersion;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Game {

    private @NotNull String id;
    private @NotNull String name;
    private @Nullable String description;
    private @Nullable String imageUrl;
    private @NotNull MinecraftVersion minecraftVersion;
    private @NotNull Set<MinecraftVersion> supportedMinecraftVersions;
    private long createdAt;
    private long updatedAt;

    public Game(@NotNull String id, @NotNull String name, @NotNull MinecraftVersion minecraftVersion) {
        this.id = id;
        this.name = name;
        this.minecraftVersion = minecraftVersion;
        this.supportedMinecraftVersions = Set.of(minecraftVersion);
        this.createdAt = System.currentTimeMillis();
    }
}
