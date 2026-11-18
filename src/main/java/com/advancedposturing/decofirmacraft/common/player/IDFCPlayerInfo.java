package com.advancedposturing.decofirmacraft.common.player;

import com.advancedposturing.decofirmacraft.network.PlayerInfoPacket;
import net.dries007.tfc.common.player.PlayerInfo;
import net.minecraft.world.entity.player.Player;

public interface IDFCPlayerInfo
{
    /**
     * @return The player info for a given player.
     * @see PlayerInfo
     */
    static IDFCPlayerInfo get(Player player)
    {
        return ((DFCPlayerBridge) player).dfc$getPlayerInfo();
    }

    void tick(Player player);

    TrowelMode trowelMode();

    void cycleTrowelMode();

    /**
     * Updates the client-side food data when received on client
     * @param packet The packed with updated data
     */
    void onClientUpdate(PlayerInfoPacket packet);
}
