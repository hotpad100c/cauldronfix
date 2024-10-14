package mypals.ml.dispenserInteractionManage;

import mypals.ml.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;

import java.util.HashMap;
import java.util.Map;

public class CauldronInteractionMap {
    public static final Map<Item, Block> BUCKET_TO_CAULDRON_MAP = new HashMap<>() {{
        put(Items.LAVA_BUCKET,Blocks.LAVA_CAULDRON);
        put(Items.POWDER_SNOW_BUCKET,Blocks.POWDER_SNOW_CAULDRON);
        put(Items.WATER_BUCKET,Blocks.WATER_CAULDRON);
        put(Items.MILK_BUCKET, ModBlocks.CAULDRON_WITH_MILK);
    }};

    public static final Map<Block, Item> CAULDRON_TO_BUCKET_MAP = new HashMap<>() {{
        put(Blocks.LAVA_CAULDRON, Items.LAVA_BUCKET);
        put(Blocks.POWDER_SNOW_CAULDRON,Items.POWDER_SNOW_BUCKET);
        put(Blocks.WATER_CAULDRON,Items.WATER_BUCKET);
        put(ModBlocks.CAULDRON_WITH_MILK,Items.MILK_BUCKET);
    }};
    public static final Map<Item, Block> POTION_TO_CAULDRON_MAP = new HashMap<>() {{
        put(PotionContentsComponent.createStack(Items.POTION, Potions.WATER).getItem(),Blocks.WATER_CAULDRON);
        put(Items.DRAGON_BREATH, ModBlocks.CAULDRON_WITH_DRAGONS_BREATH);
        put(Items.HONEY_BOTTLE, ModBlocks.CAULDRON_WITH_HONEY);

    }};
    public static final Map<Block, Item> CAULDRON_TO_POTION_MAP = new HashMap<>() {{
        put(Blocks.WATER_CAULDRON,PotionContentsComponent.createStack(Items.POTION, Potions.WATER).getItem());
        put(ModBlocks.CAULDRON_WITH_DRAGONS_BREATH, Items.DRAGON_BREATH);
        put(ModBlocks.CAULDRON_WITH_HONEY, Items.HONEY_BOTTLE);
    }};
}
