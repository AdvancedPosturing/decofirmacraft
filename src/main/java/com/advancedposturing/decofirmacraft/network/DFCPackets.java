package com.advancedposturing.decofirmacraft.network;

import com.advancedposturing.decofirmacraft.DecoFirmaCraft;
import com.advancedposturing.decofirmacraft.util.DFCHelpers;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class DFCPackets
{
    public static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> type(String id)
    {
        return new CustomPacketPayload.Type<T>(DFCHelpers.identifier(id));
    }

    public static void setup(RegisterPayloadHandlersEvent event)
    {
        final PayloadRegistrar register = event.registrar(ModList.get().getModFileById(DecoFirmaCraft.MOD_ID).versionString());

        // Server -> Client
        register.playToClient(PlayerInfoPacket.TYPE, PlayerInfoPacket.CODEC, onClient(PlayerInfoPacket::handle));

        // Client -> Server
        register.playToServer(StartContinuousUsePacket.TYPE, StartContinuousUsePacket.CODEC, onServer(StartContinuousUsePacket::handle));
        register.playToServer(StopContinuousUsePacket.TYPE, StopContinuousUsePacket.CODEC, onServer(StopContinuousUsePacket::handle));
        register.playToServer(CycleTrowelModePacket.TYPE, CycleTrowelModePacket.CODEC, onServer(CycleTrowelModePacket::handle));
    }

    private static <T extends CustomPacketPayload> IPayloadHandler<T> onClient(Consumer<T> handler)
    {
        return (payload, context) -> context.enqueueWork(() -> handler.accept(payload));
    }

    private static <T extends CustomPacketPayload> IPayloadHandler<T> onServer(BiConsumer<T, ServerPlayer> handler)
    {
        return (payload, context) -> context.enqueueWork(() -> handler.accept(payload, (ServerPlayer) context.player()));
    }
}
