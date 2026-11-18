package com.advancedposturing.decofirmacraft.network;

import com.advancedposturing.decofirmacraft.common.block.IContinuouslyInteractable;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public record StartContinuousUsePacket(BlockPos pos) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<StartContinuousUsePacket> TYPE = DFCPackets.type("start_continuous_use");
    public static final StreamCodec<ByteBuf, StartContinuousUsePacket> CODEC = BlockPos.STREAM_CODEC.map(StartContinuousUsePacket::new, c -> c.pos);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    void handle(@Nullable ServerPlayer player)
    {
        if (player != null)
        {
            final Level level = player.level();
            if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof IContinuouslyInteractable continuouslyInteractable)
            {
                continuouslyInteractable.startInteracting(player);
            }
        }
    }
}
