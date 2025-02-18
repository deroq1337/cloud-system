package com.github.deroq1337.cloud.master.data.game.template;

import com.github.deroq1337.cloud.master.data.CloudSystemMaster;
import com.github.deroq1337.cloud.master.data.game.GameFileManager;
import com.github.deroq1337.cloud.master.data.game.GameManager;
import com.github.deroq1337.cloud.master.data.game.template.entity.GameServerTemplate;
import com.github.deroq1337.cloud.master.data.game.template.repository.DefaultGameServerTemplateRepository;
import com.github.deroq1337.cloud.master.data.game.template.repository.GameServerTemplateRepository;
import com.github.deroq1337.cloud.master.data.utils.AsyncExecutor;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class DefaultGameServerTemplateManager implements GameServerTemplateManager {

    private final @NotNull GameServerTemplateRepository repository;
    private final @NotNull GameFileManager gameFileManager;
    private final @NotNull Logger log;

    public DefaultGameServerTemplateManager(@NotNull CloudSystemMaster master, @NotNull GameManager gameManager) {
        this.repository = new DefaultGameServerTemplateRepository(master.getCassandra());
        this.gameFileManager = gameManager.getFileManager();
        this.log = master.getLog();
    }

    @Override
    public @NotNull ListenableFuture<Boolean> createTemplate(@NotNull GameServerTemplate template) {
        String templateId = template.getId();
        log.info("Creating files for template '{}'", templateId);

        return Futures.transformAsync(gameFileManager.initTemplateFiles(template.getGameId(), templateId), success -> {
            if (!success) {
                log.warn("Files for template '{}' were not created", templateId);
                return Futures.immediateFuture(false);
            }

            log.info("Created files for template '{}'", template);

            return Futures.transformAsync(repository.create(template), gameCreated -> {
                if (!gameCreated) {
                    log.warn("Failed to create template '{}'", templateId);
                    return Futures.immediateFuture(false);
                }

                log.info("Template '{}' created successfully", templateId);
                return Futures.immediateFuture(true);
            }, AsyncExecutor.EXECUTOR_SERVICE);
        }, AsyncExecutor.EXECUTOR_SERVICE);
    }

    @Override
    public @NotNull ListenableFuture<Boolean> deleteTemplateById(@NotNull String id) {
        return repository.deleteById(id.toUpperCase());
    }

    @Override
    public @NotNull ListenableFuture<Optional<GameServerTemplate>> getTemplateById(@NotNull String id) {
        return repository.findById(id.toUpperCase());
    }
}
