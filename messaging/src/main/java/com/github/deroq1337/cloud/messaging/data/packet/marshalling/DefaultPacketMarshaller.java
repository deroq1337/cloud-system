package com.github.deroq1337.cloud.messaging.data.packet.marshalling;

import com.github.deroq1337.cloud.messaging.data.packet.Packet;
import com.github.deroq1337.cloud.messaging.data.packet.PacketReflectionContainer;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class DefaultPacketMarshaller implements PacketMarshaller {

    private final @NotNull PacketReflectionContainer packetReflectionContainer;

    public DefaultPacketMarshaller(@NotNull Packet packet) {
        this.packetReflectionContainer = new PacketReflectionContainer(packet);
    }

    @Override
    public void write(@NotNull ByteBuf byteBuf) {
        Map<Boolean, List<Field>> fields = packetReflectionContainer.getWriteFields();
        if (fields.containsKey(false)) {
            throw new IllegalStateException("Some fields do not have corresponding write methods.");
        }

        fields.get(true).forEach(field -> packetReflectionContainer.invokeWriteMethod(field, byteBuf));
    }

    @Override
    public void read(@NotNull ByteBuf byteBuf) {
        Map<Boolean, List<Field>> fields = packetReflectionContainer.getReadFields();
        if (fields.containsKey(false)) {
            throw new IllegalStateException("Some fields do not have corresponding read methods.");
        }

        fields.get(true).forEach(field -> packetReflectionContainer.setFieldValue(field, byteBuf));
    }
}
