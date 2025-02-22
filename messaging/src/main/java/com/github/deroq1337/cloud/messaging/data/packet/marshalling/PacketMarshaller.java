package com.github.deroq1337.cloud.messaging.data.packet.marshalling;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public interface PacketMarshaller {

    void write(@NotNull ByteBuf byteBuf);

    void read(@NotNull ByteBuf byteBuf);
}
