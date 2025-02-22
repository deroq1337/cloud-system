package com.github.deroq1337.cloud.messaging.data.registry;

import com.github.deroq1337.cloud.messaging.data.packet.Packet;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultPacketRegistry implements PacketRegistry {

    private static final Map<Integer, Class<? extends Packet>> PACKET_ID_CLASS_MAP = new ConcurrentHashMap<>();
    private static final Map<Class<? extends Packet>, Integer> PACKET_CLASS_ID_MAP = new ConcurrentHashMap<>();

    @Override
    public synchronized boolean registerPacket(@NotNull Class<? extends Packet> packetClass) {
        int id = packetClass.hashCode();
        return PACKET_ID_CLASS_MAP.put(id, packetClass) == null && PACKET_CLASS_ID_MAP.put(packetClass, id) == null;
    }

    @Override
    public synchronized boolean unregisterPacket(@NotNull Class<? extends Packet> packetClass) {
        return Optional.ofNullable(PACKET_CLASS_ID_MAP.get(packetClass)).map(packetId -> {
            return PACKET_ID_CLASS_MAP.remove(packetId) != null && PACKET_CLASS_ID_MAP.remove(packetClass) != null;
        }).orElse(false);
    }

    @Override
    public synchronized boolean unregisterPacket(int packetId) {
        return Optional.ofNullable(PACKET_ID_CLASS_MAP.get(packetId)).map(packetClass -> {
            return PACKET_CLASS_ID_MAP.remove(packetClass) != null && PACKET_ID_CLASS_MAP.remove(packetId) != null;
        }).orElse(false);
    }

    @Override
    public Optional<Class<? extends Packet>> getPacketClassById(int id) {
        return Optional.ofNullable(PACKET_ID_CLASS_MAP.get(id));
    }

    @Override
    public Optional<Integer> getPacketIdByClass(@NotNull Class<? extends Packet> packetClass) {
        return Optional.ofNullable(PACKET_CLASS_ID_MAP.get(packetClass));
    }
}
