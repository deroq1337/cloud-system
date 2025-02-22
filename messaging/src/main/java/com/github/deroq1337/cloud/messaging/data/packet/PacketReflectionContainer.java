package com.github.deroq1337.cloud.messaging.data.packet;

import com.github.deroq1337.cloud.messaging.data.packet.marshalling.Exclude;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public class PacketReflectionContainer {

    private final @NotNull Packet packet;

    private final @NotNull Map<Class<?>, Method> writeMethods = Arrays.stream(packet.getClass().getDeclaredMethods())
            .filter(method -> method.getParameterCount() == 2 && method.getParameters()[0].getType().equals(ByteBuf.class))
            .collect(Collectors.toMap(method -> method.getParameters()[1].getType(), method -> method));

    private final @NotNull Map<Class<?>, Method> readMethods = Arrays.stream(packet.getClass().getDeclaredMethods())
            .filter(method -> method.getParameterCount() == 1 && method.getParameters()[0].getType().equals(ByteBuf.class))
            .collect(Collectors.toMap(Method::getReturnType, method -> method));

    private final @NotNull Map<Boolean, List<Field>> writeFields = getPacketFields(writeMethods);
    private final @NotNull Map<Boolean, List<Field>> readFields = getPacketFields(readMethods);

    public void invokeWriteMethod(@NotNull Field field, @NotNull ByteBuf byteBuf) {
        try {
            writeMethods.get(field.getType()).invoke(byteBuf, field.get(packet));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setFieldValue(@NotNull Field field, @NotNull ByteBuf byteBuf) {
        try {
            field.setAccessible(true);
            field.set(packet, readMethods.get(field.getType()).invoke(byteBuf));
            field.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public @NotNull Map<Boolean, List<Field>> getPacketFields(@NotNull Map<Class<?>, Method> methods) {
        return Arrays.stream(getClass().getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()) && !field.isAnnotationPresent(Exclude.class))
                .collect(Collectors.partitioningBy(field -> methods.containsKey(field.getType())));
    }
}
