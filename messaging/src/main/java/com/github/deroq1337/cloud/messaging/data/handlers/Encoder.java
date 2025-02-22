package com.github.deroq1337.cloud.messaging.data.handlers;

import com.github.deroq1337.cloud.messaging.data.packet.Packet;
import com.github.deroq1337.cloud.messaging.data.registry.PacketRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class Encoder extends MessageToByteEncoder<Packet> {

    private final @NotNull PacketRegistry packetRegistry;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Packet packet, ByteBuf byteBuf) {
        packetRegistry.getPacketIdByClass(packet.getClass()).ifPresentOrElse(packetId ->{
            byteBuf.writeInt(packetId);
            packet.getMarshaller().write(byteBuf);
        }, () -> {
            throw new NoSuchElementException("Packet of class '" + packet.getClass() + "' was not registered");
        });
    }
}
