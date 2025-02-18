package com.github.deroq1337.cloud.master.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.deroq1337.cloud.master.database.AsyncExecutor;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletionException;

public class PaperDownloader {

    private static final String PAPER_MC_BUILDS_URL = "https://api.papermc.io/v2/projects/paper/versions/%s/builds";
    private static final String PAPER_MC_DOWNLOAD_URL = "https://api.papermc.io/v2/projects/paper/versions/%s/builds/%s/downloads/%s";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static @NotNull ListenableFuture<InputStream> downloadServerJar(@NotNull String version) {
        return Futures.submitAsync(() -> {
            try {
                String latestBuild = getLatestBuild(version);
                String jarName = String.format("paper-%s-%s.jar", version, latestBuild);
                String downloadUrl = String.format(PAPER_MC_DOWNLOAD_URL, version, latestBuild, jarName);

                HttpURLConnection downloadConnection = createHttpConnection(URI.create(downloadUrl).toURL());
                return Futures.immediateFuture(downloadConnection.getInputStream());
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, AsyncExecutor.EXECUTOR_SERVICE);
    }

    private static @NotNull String getLatestBuild(@NotNull String version) throws Exception {
        String buildsUrl = String.format(PAPER_MC_BUILDS_URL, version);
        HttpURLConnection connection = createHttpConnection(URI.create(buildsUrl).toURL());

        try (InputStream inputStream = connection.getInputStream()) {
            JsonNode jsonResponse = OBJECT_MAPPER.readTree(inputStream);
            JsonNode builds = jsonResponse.get("builds");

            return Optional.ofNullable(builds).flatMap(buildArray -> {
                for (JsonNode build : buildArray) {
                    if (build.get("channel").asText().equals("default")) {
                        return Optional.ofNullable(build.get("build").asText());
                    }
                }

                return Optional.empty();
            }).orElseThrow(() -> new CompletionException(new NoSuchElementException("No stable build found for version " + version)));
        } finally {
            connection.disconnect();
        }
    }

    private static @NotNull HttpURLConnection createHttpConnection(@NotNull URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.connect();

        return connection;
    }
}
