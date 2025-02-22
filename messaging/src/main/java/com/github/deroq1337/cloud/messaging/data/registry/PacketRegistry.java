package com.github.deroq1337.cloud.messaging.data.registry;

import com.github.deroq1337.cloud.messaging.data.packet.Packet;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface PacketRegistry {

    boolean registerPacket(@NotNull Class<? extends Packet> packetClass);

    boolean unregisterPacket(@NotNull Class<? extends Packet> packetClass);

    boolean unregisterPacket(int packetId);

    Optional<Class<? extends Packet>> getPacketClassById(int packetId);

    Optional<Integer> getPacketIdByClass(@NotNull Class<? extends Packet> packetClass);
}
