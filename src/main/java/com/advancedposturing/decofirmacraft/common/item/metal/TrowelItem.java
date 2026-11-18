package com.advancedposturing.decofirmacraft.common.item.metal;

import com.advancedposturing.decofirmacraft.common.player.IDFCPlayerInfo;
import com.advancedposturing.decofirmacraft.common.player.TrowelMode;
import com.advancedposturing.decofirmacraft.util.DFCTags;
import net.dries007.tfc.common.items.ToolItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;

public class TrowelItem extends ToolItem
{
    public TrowelItem(Tier tier, Properties properties)
    {
        super(tier, DFCTags.Blocks.MINEABLE_WITH_TROWEL, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Player player = context.getPlayer();

        if (player != null)
        {
            TrowelMode mode = IDFCPlayerInfo.get(player).trowelMode();

            if (mode != null)
            {
                if (mode.modifyState(context))
                {
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.PASS;
    }
}
