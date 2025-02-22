package com.github.deroq1337.cloud.messaging.data.handlers;

import com.github.deroq1337.cloud.messaging.data.packet.Packet;
import com.github.deroq1337.cloud.messaging.data.registry.PacketRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class Decoder extends ByteToMessageDecoder {

    private final @NotNull PacketRegistry packetRegistry;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int packetId = byteBuf.readInt();

        packetRegistry.getPacketClassById(packetId).ifPresentOrElse(packetClass -> {
            Packet packet = Packet.instantiate(packetClass);
            packet.getMarshaller().read(byteBuf);

            list.add(packet);
        }, () -> {
            throw new NoSuchElementException("Packet with id '" + packetId + "' was not registered");
        });
    }
}
