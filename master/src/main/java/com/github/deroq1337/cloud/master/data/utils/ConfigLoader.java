package com.github.deroq1337.cloud.master.data.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.InputStream;

public class ConfigLoader {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static <C> @NotNull C load(@NotNull String path, @NotNull Class<C> configClass) {
        try (InputStream inputStream = new FileInputStream(path)) {
            return OBJECT_MAPPER.readValue(inputStream, configClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
