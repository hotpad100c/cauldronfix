package mypals.ml.block;
import mypals.ml.CauldronFix;
import mypals.ml.block.advancedCauldron.CauldronWithBadOmen;
import mypals.ml.block.advancedCauldron.CauldronWithDragonsBreath;
import mypals.ml.block.advancedCauldron.CauldronWithMilk;
import mypals.ml.block.advancedCauldron.CauldronWithHoney;

import mypals.ml.block.advancedCauldron.coloredCauldrons.ColoredCauldron;
import net.minecraft.block.*;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import static mypals.ml.block.advancedCauldron.BehaciorMaps.*;
import static mypals.ml.block.advancedCauldron.coloredCauldrons.ColoredCauldron.STATE_TO_LUMINANCE;

public class ModBlocks {
    public static final Block CAULDRON_WITH_OBSIDIAN = registerBlocks("cauldron_with_obsidian",
            new CauldronWithObsidian(AbstractBlock.Settings.create().instrument(NoteBlockInstrument.BASEDRUM).strength(
                    50.0F, 6.0F).pistonBehavior(PistonBehavior.BLOCK).nonOpaque().requiresTool().solid().mapColor(MapColor.STONE_GRAY)));
    public static final Block CAULDRON_WITH_STONE = registerBlocks("cauldron_with_stone",
            new CauldronWithStone(AbstractBlock.Settings.create().instrument(NoteBlockInstrument.BASEDRUM).strength(
                    1.5F, 6.0F).nonOpaque().requiresTool().solid().mapColor(MapColor.STONE_GRAY)));
    public static final Block CAULDRON_WITH_COBBLE_STONE = registerBlocks("cauldron_with_cobble_stone",
            new CauldronWithCobblestone(AbstractBlock.Settings.create().instrument(NoteBlockInstrument.BASEDRUM).strength(
                    2.0F, 6.0F).nonOpaque().requiresTool().solid().mapColor(MapColor.STONE_GRAY)));
    public static final Block CAULDRON_WITH_HALF_COBBLE_STONE = registerBlocks("cauldron_with_some_cobble_stones",
            new CauldronWithHalfCobblestone(AbstractBlock.Settings.create().instrument(NoteBlockInstrument.BASEDRUM).strength(
                    2.0F, 6.0F).nonOpaque().requiresTool().mapColor(MapColor.STONE_GRAY)));
    public static final Block CAULDRON_WITH_EMBER = registerBlocks("cauldron_with_ember",
            new CauldronWithEmber(AbstractBlock.Settings.create().instrument(NoteBlockInstrument.BASEDRUM).strength(
                    2.0F, 6.0F).nonOpaque().requiresTool().mapColor(MapColor.STONE_GRAY)));
    public static final Block CAULDRON_WITH_GRAVEL = registerBlocks("cauldron_with_gravel",
            new CauldronWithGravel(AbstractBlock.Settings.create().instrument(NoteBlockInstrument.BASEDRUM).strength(
                    2.0F, 6.0F).nonOpaque().requiresTool().mapColor(MapColor.STONE_GRAY)));
    public static final Block DRAGONS_BREATH_CAULDRON = registerBlocks("cauldron_with_dragons_breath",
            new CauldronWithDragonsBreath(Biome.Precipitation.NONE,new CauldronBehavior.CauldronBehaviorMap("dragon_breath",DRAGON_BREATH_CAULDRON_BEHAVIOR),
                    AbstractBlock.Settings.create().mapColor(MapColor.PURPLE).strength(
                    2.0F, 6.0F).requiresTool(),Biome.Precipitation.NONE));
    public static final Block HONEY_CAULDRON = registerBlocks("cauldron_with_honey",
            new CauldronWithHoney(Biome.Precipitation.NONE,new CauldronBehavior.CauldronBehaviorMap("honey",HONEY_CAULDRON_BEHAVIOR),
                    AbstractBlock.Settings.create().mapColor(MapColor.YELLOW).strength(
                            2.0F, 6.0F).requiresTool(),Biome.Precipitation.NONE));
    public static final Block MILK_CAULDRON = registerBlocks("cauldron_with_milk",
            new CauldronWithMilk(Biome.Precipitation.NONE,new CauldronBehavior.CauldronBehaviorMap("milk",MILK_CAULDRON_BEHAVIOR),
                    AbstractBlock.Settings.create().mapColor(MapColor.WHITE).strength(
                            2.0F, 6.0F).requiresTool(),Biome.Precipitation.NONE));
    public static final Block COLORED_CAULDRON = registerBlocks("colored_cauldron",
            new ColoredCauldron(Biome.Precipitation.NONE, new CauldronBehavior.CauldronBehaviorMap("colored",COLORED_CAULDRON_BEHAVIOR),
                AbstractBlock.Settings.create().strength(
                        2.0F, 6.0F).requiresTool().luminance(STATE_TO_LUMINANCE).
                    nonOpaque(),Biome.Precipitation.NONE));
    public static final Block BAD_OMEN_CAULDRON = registerBlocks("cauldron_with_bad_omen",
            new CauldronWithBadOmen(Biome.Precipitation.NONE, new CauldronBehavior.CauldronBehaviorMap("bad_omen",BAD_OMEN_CAULDRON_BEHAVIOR),
                    AbstractBlock.Settings.create().strength(
                            -1F, 9999.0F).requiresTool(),Biome.Precipitation.NONE));




    public static void registerBlockItems(String name, Block block) {
        Item.Settings settings = new Item.Settings();

        if (block == ModBlocks.CAULDRON_WITH_GRAVEL) {
            settings.recipeRemainder(Blocks.CAULDRON.asItem());
        }

        Item item = Registry.register(Registries.ITEM, Identifier.of(CauldronFix.MOD_ID, name), new BlockItem(block, settings));

        if (item instanceof BlockItem) {
            ((BlockItem) item).appendBlocks(Item.BLOCK_ITEMS, item);
        }
    }

    public static Block registerBlocks(String name, Block block) {
        registerBlockItems(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(CauldronFix.MOD_ID, name), block);
    }
    public static void registerModBlocks(){
        CauldronFix.LOGGER.info("Registering Blocks");
    }
}
