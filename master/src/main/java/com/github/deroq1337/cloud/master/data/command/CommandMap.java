package com.github.deroq1337.cloud.master.data.command;

import com.github.deroq1337.cloud.master.data.exceptions.InvalidCommandException;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@NoArgsConstructor
public class CommandMap {

    private final @NotNull Map<String, Command> commandMap = new HashMap<>();
    private final @NotNull Map<String, CommandInfo> commandInfoMap = new HashMap<>();

    public CommandMap(@NotNull Collection<Command> commands) {
        commands.forEach(this::register);
    }

    public void register(@NotNull Command command) {
        Class<? extends Command> commandClass = command.getClass();
        CommandInfo commandInfo = Optional.ofNullable(commandClass.getAnnotation(CommandInfo.class))
                .orElseThrow(() -> new InvalidCommandException("Command " + commandClass + " does not have @CommandInfo annotation"));

        commandMap.put(commandInfo.name().toLowerCase(), command);
        commandInfoMap.put(commandInfo.name(), commandInfo);
    }

    public void clear() {
        commandInfoMap.clear();
        commandMap.clear();
    }

    public Optional<Command> getCommand(@NotNull String name) {
        return Optional.ofNullable(commandMap.get(name.toLowerCase()));
    }

    public @NotNull Collection<CommandInfo> getCommandInfos() {
        return commandInfoMap.values();
    }
}
