package com.advancedposturing.decofirmacraft.network;

import com.advancedposturing.decofirmacraft.common.player.IDFCPlayerInfo;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public enum CycleTrowelModePacket implements CustomPacketPayload
{
    PACKET;

    public static final CustomPacketPayload.Type<CycleTrowelModePacket> TYPE = DFCPackets.type("cycle_trowel_mode");
    public static final StreamCodec<ByteBuf, CycleTrowelModePacket> CODEC = StreamCodec.unit(PACKET);

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    void handle(@Nullable ServerPlayer player)
    {
        if (player != null)
        {
            IDFCPlayerInfo.get(player).cycleTrowelMode();
        }
    }
}
