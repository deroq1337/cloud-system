package com.github.deroq1337.cloud.master.data.game.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public enum MinecraftVersion {

    VERSION_1_21("1.21"),
    VERSION_1_20("1.20"),
    VERSION_1_19("1.19"),
    VERSION_1_18("1.18"),
    VERSION_1_17("1.17"),
    VERSION_1_16("1.16"),
    VERSION_1_15("1.15"),
    VERSION_1_14("1.14"),
    VERSION_1_13("1.13"),
    VERSION_1_12("1.12"),
    VERSION_1_11("1.11"),
    VERSION_1_10("1.10"),
    VERSION_1_9("1.9"),
    VERSION_1_8("1.8");

    private final @NotNull String version;

    public static Optional<MinecraftVersion> fromString(@NotNull String version) {
        return Arrays.stream(values())
                .filter(minecraftVersion -> minecraftVersion.getVersion().equals(version))
                .findFirst();
    }

    public static @NotNull Set<String> getVersions() {
        return Arrays.stream(values())
                .map(MinecraftVersion::getVersion)
                .collect(Collectors.toSet());
    }
}
