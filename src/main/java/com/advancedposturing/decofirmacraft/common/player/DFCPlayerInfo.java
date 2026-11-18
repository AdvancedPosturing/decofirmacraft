package com.advancedposturing.decofirmacraft.common.player;

import com.advancedposturing.decofirmacraft.network.PlayerInfoPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public class DFCPlayerInfo implements IDFCPlayerInfo
{
    private final Player player; // The player associated with this object
    private TrowelMode trowelMode = TrowelMode.SMOOTH.value();

    private boolean modified = true;

    //add some sort of heavy metal poisoning and asbestos poisoning at some point, like it just builds up in the player
    //also maybe a unlockable recipe system?

    public DFCPlayerInfo(Player player)
    {
        this.player = player;
    }

    //runs on the event bus, im not sure if making it use the interface tick() would break anything i presume not
    @Override
    public void tick(Player player)
    {
        if (modified && player instanceof ServerPlayer serverPlayer)
        {
            PacketDistributor.sendToPlayer(serverPlayer, new PlayerInfoPacket(
                    trowelMode
            ));
            modified = false;
        }
    }

    @Override
    public TrowelMode trowelMode()
    {
        return trowelMode;
    }

    @Override
    public void cycleTrowelMode()
    {
        trowelMode = trowelMode.next();
        modified = true;
    }

    /**
     * Sets data from a packet, received on client side. Does not contain the full data only the important information
     */
    @Override
    public void onClientUpdate(PlayerInfoPacket packet)
    {
        this.trowelMode = packet.trowelMode();
    }
}
