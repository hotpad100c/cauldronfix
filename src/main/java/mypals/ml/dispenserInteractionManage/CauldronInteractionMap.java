package mypals.ml.dispenserInteractionManage;

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
    }};

    public static final Map<Block, Item> CAULDRON_TO_BUCKET_MAP = new HashMap<>() {{
        put(Blocks.LAVA_CAULDRON, Items.LAVA_BUCKET);
        put(Blocks.POWDER_SNOW_CAULDRON,Items.POWDER_SNOW_BUCKET);
        put(Blocks.WATER_CAULDRON,Items.WATER_BUCKET);
    }};
    public static final Map<Item, Block> POTION_TO_CAULDRON_MAP = new HashMap<>() {{
        put(PotionContentsComponent.createStack(Items.POTION, Potions.WATER).getItem(),Blocks.WATER_CAULDRON);
    }};
    public static final Map<Block, Item> CAULDRON_TO_POTION_MAP = new HashMap<>() {{
        put(Blocks.WATER_CAULDRON,PotionContentsComponent.createStack(Items.POTION, Potions.WATER).getItem());
    }};
}
