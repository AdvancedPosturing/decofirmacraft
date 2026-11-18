package com.advancedposturing.decofirmacraft.common.item;

import com.advancedposturing.decofirmacraft.DecoFirmaCraft;
import com.advancedposturing.decofirmacraft.common.block.metal.DFCExtendedMetal;
import com.advancedposturing.decofirmacraft.common.item.metal.TrowelItem;
import net.dries007.tfc.common.items.ToolItem;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.Metal;
import net.dries007.tfc.util.registry.IdHolder;
import net.dries007.tfc.util.registry.RegistryHolder;
import net.dries007.tfc.util.registry.RegistryMetal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.*;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public final class DFCItems {

	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DecoFirmaCraft.MOD_ID);

//	public static final ItemId WOODEN_MINECART = register("wooden_minecart", () -> new MinecartItem(AbstractMinecart.Type.CHEST, (new Item.Properties()).stacksTo(1)));
//	public static final ItemId LABEL = register("label", () -> new LabelItem())
	public static final ItemId CONCRETE_LAYER = register("concrete_layer", () -> new ConcreteLayerItem(new Properties()));

	public static final Map<DFCExtendedMetal, Map<DFCExtendedMetal.DFCMetalItemType, ItemId>> DFC_METAL_ITEMS = Helpers.mapOf(DFCExtendedMetal.class, metal ->
			Helpers.mapOf(DFCExtendedMetal.DFCMetalItemType.class, type -> type.has(metal), type ->
					register("metal/" + type.name() + "/" + metal.name(), () ->  type.create(metal))
			)
	);

	public static final Map<Metal, ItemId> TROWEL_HEADS = Helpers.mapOf(Metal.class, Metal::allParts, metal -> register("metal/trowel_head/" + metal.name()));
	public static final Map<Metal, ItemId> TROWELS = Helpers.mapOf(Metal.class, Metal::allParts, metal -> register("metal/trowel/" + metal.name(), () -> new TrowelItem(metal.toolTier(), tool(metal, 0.5f, -3.0f))));

	private static <T extends Mob> ItemId registerSpawnEgg(IdHolder<EntityType<T>> entity)
	{
		return register("spawn_egg/" + entity.getId().getPath(), () -> new DeferredSpawnEggItem(entity.holder(), 0xffffff, 0xffffff, new Properties()));
	}

	private static ItemId register(String name)
	{
		return register(name, () -> new Item(new Properties()));
	}

	private static ItemId register(String name, Properties properties)
	{
		return new ItemId(ITEMS.register(name.toLowerCase(Locale.ROOT), () -> new Item(properties)));
	}

	private static ItemId register(String name, Supplier<Item> item)
	{
		return new ItemId(ITEMS.register(name.toLowerCase(Locale.ROOT), item));
	}

	public record ItemId(DeferredHolder<Item, Item> holder) implements RegistryHolder<Item, Item>, ItemLike
	{
		@Override
		public Item asItem()
		{
			return get();
		}
	}

	private static Item.Properties tool(RegistryMetal metal, float attackDamageFactor, float attackSpeed)
	{
		return base(metal).attributes(ToolItem.productAttributes(metal.toolTier(), attackDamageFactor, attackSpeed));
	}

	private static Item.Properties base(RegistryMetal metal)
	{
		return new Item.Properties().rarity(metal.rarity());
	}
}