package com.github.deroq1337.cloud.messaging.data.packet;

import com.github.deroq1337.cloud.messaging.data.packet.marshalling.Exclude;
import com.github.deroq1337.cloud.messaging.data.packet.marshalling.DefaultPacketMarshaller;
import com.github.deroq1337.cloud.messaging.data.packet.marshalling.PacketMarshaller;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public abstract class Packet {

    private static final Map<Class<? extends Packet>, PacketMarshaller> PACKET_MARSHALLER_MAP = new ConcurrentHashMap<>();

    @Exclude
    private final @NotNull PacketMarshaller marshaller;

    private final @NotNull UUID id = UUID.randomUUID();

    public Packet() {
        this.marshaller = PACKET_MARSHALLER_MAP.computeIfAbsent(getClass(), cls -> new DefaultPacketMarshaller(this));
    }

    public static @NotNull Packet instantiate(@NotNull Class<? extends Packet> packetClass) {
        try {
            return packetClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void writeString(@NotNull ByteBuf byteBuf, @NotNull String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);
    }

    private @NotNull String readString(@NotNull ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readInt()];
        byteBuf.readBytes(bytes);

        return new String(bytes, StandardCharsets.UTF_8);
    }

    private void writeUuid(@NotNull ByteBuf byteBuf, @NotNull UUID uuid) {
        byteBuf.writeLong(uuid.getMostSignificantBits());
        byteBuf.writeLong(uuid.getLeastSignificantBits());
    }

    private @NotNull UUID readUuid(@NotNull ByteBuf byteBuf) {
        return new UUID(byteBuf.readLong(), byteBuf.readLong());
    }

    private <T extends Enum<T>> void writeEnum(@NotNull ByteBuf byteBuf, @NotNull T enumValue) {
        byteBuf.writeInt(enumValue.ordinal());
    }

    private @NotNull <T extends Enum<T>> T readEnum(@NotNull ByteBuf byteBuf, @NotNull Class<T> enumClass) {
        int ordinal = byteBuf.readInt();

        T[] enumConstants = enumClass.getEnumConstants();
        if (ordinal < 0 || ordinal >= enumConstants.length) {
            throw new IllegalArgumentException("Invalid ordinal for enum " + enumClass.getName() + ": " + ordinal);
        }

        return enumConstants[ordinal];
    }

    private void writeStringList(@NotNull ByteBuf byteBuf, @NotNull List<String> list) {
        byteBuf.writeInt(list.size());
        for (String s : list) {
            writeString(byteBuf, s);
        }
    }

    private @NotNull List<String> readStringList(@NotNull ByteBuf byteBuf) {
        int size = byteBuf.readInt();

        List<String> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(readString(byteBuf));
        }

        return list;
    }
}
