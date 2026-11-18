package com.advancedposturing.decofirmacraft.network;

import com.advancedposturing.decofirmacraft.common.player.IDFCPlayerInfo;
import com.advancedposturing.decofirmacraft.common.player.TrowelMode;
import net.dries007.tfc.client.ClientHelpers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record PlayerInfoPacket(
TrowelMode trowelMode
) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<PlayerInfoPacket> TYPE = DFCPackets.type("player_info");
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerInfoPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(TrowelMode.KEY), c -> c.trowelMode,
            PlayerInfoPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    void handle()
    {
        final Player player = ClientHelpers.getPlayer();
        if (player != null)
        {
            IDFCPlayerInfo.get(player).onClientUpdate(this);
        }
    }
}
