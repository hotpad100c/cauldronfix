package mypals.ml.block;
import mypals.ml.CauldronFix;
import mypals.ml.block.advancedCauldron.CauldronWithBadOmen;
import mypals.ml.block.advancedCauldron.CauldronWithDragonsBreath;
import mypals.ml.block.advancedCauldron.CauldronWithMilk;
import mypals.ml.block.advancedCauldron.CauldronWithHoney;

import mypals.ml.block.advancedCauldron.coloredCauldrons.ColoredCauldron;
import mypals.ml.block.advancedCauldron.potionCauldrons.PotionCauldron;
import net.minecraft.block.*;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import java.util.function.Function;

import static mypals.ml.block.advancedCauldron.BehaciorMaps.*;
import static mypals.ml.block.advancedCauldron.coloredCauldrons.ColoredCauldron.STATE_TO_LUMINANCE_1;
import static mypals.ml.block.advancedCauldron.potionCauldrons.PotionCauldron.STATE_TO_LUMINANCE_2;

public class ModBlocks {
    public static final Block CAULDRON_WITH_OBSIDIAN = register("cauldron_with_obsidian",CauldronWithObsidian::new,
            AbstractBlock.Settings.create()
                    .instrument(NoteBlockInstrument.BASEDRUM).strength(50.0F, 6.0F)
                    .pistonBehavior(PistonBehavior.BLOCK).nonOpaque().requiresTool().solid().mapColor(MapColor.STONE_GRAY));

    public static final Block CAULDRON_WITH_STONE = register("cauldron_with_stone",CauldronWithStone::new,
            AbstractBlock.Settings.create()
                    .instrument(NoteBlockInstrument.BASEDRUM).strength(1.5F, 6.0F)
                    .nonOpaque().requiresTool().solid().mapColor(MapColor.STONE_GRAY));

    public static final Block CAULDRON_WITH_COBBLE_STONE = register("cauldron_with_cobble_stone",CauldronWithCobblestone::new,
            AbstractBlock.Settings.create()
                    .instrument(NoteBlockInstrument.BASEDRUM).strength(2.0F, 6.0F)
                    .nonOpaque().requiresTool().solid().mapColor(MapColor.STONE_GRAY));

    public static final Block CAULDRON_WITH_HALF_COBBLE_STONE = register("cauldron_with_some_cobble_stones",CauldronWithHalfCobblestone::new,
            AbstractBlock.Settings.create()
                    .instrument(NoteBlockInstrument.BASEDRUM).strength(2.0F, 6.0F)
                    .nonOpaque().requiresTool().mapColor(MapColor.STONE_GRAY));

    public static final Block CAULDRON_WITH_EMBER = register("cauldron_with_ember",CauldronWithEmber::new,
            AbstractBlock.Settings.create()
                    .instrument(NoteBlockInstrument.BASEDRUM).strength(2.0F, 6.0F)
                    .nonOpaque().requiresTool().mapColor(MapColor.STONE_GRAY));

    public static final Block CAULDRON_WITH_GRAVEL = register("cauldron_with_gravel",CauldronWithGravel::new,
            AbstractBlock.Settings.create()
                    .instrument(NoteBlockInstrument.BASEDRUM).strength(2.0F, 6.0F)
                    .nonOpaque().requiresTool().mapColor(MapColor.STONE_GRAY));

    public static final Block DRAGONS_BREATH_CAULDRON = register(
            "cauldron_with_dragons_breath",
            settings -> new CauldronWithDragonsBreath(Biome.Precipitation.NONE,
                    new CauldronBehavior.CauldronBehaviorMap("dragon_breath", DRAGON_BREATH_CAULDRON_BEHAVIOR),
                    settings,
                    Biome.Precipitation.NONE),

            AbstractBlock.Settings.create().mapColor(MapColor.PURPLE).strength(2.0F, 6.0F)
                    .requiresTool().luminance(state -> 15)
            );

    public static final Block HONEY_CAULDRON = register(
            "cauldron_with_honey",
            settings -> new CauldronWithHoney(Biome.Precipitation.NONE,
                    new CauldronBehavior.CauldronBehaviorMap("honey", HONEY_CAULDRON_BEHAVIOR),settings,
                    Biome.Precipitation.NONE),
            AbstractBlock.Settings.create().mapColor(MapColor.YELLOW).strength(2.0F, 6.0F)
                    .requiresTool());

    public static final Block MILK_CAULDRON = register(
            "cauldron_with_milk",
            settings -> new CauldronWithMilk(Biome.Precipitation.NONE,
                    new CauldronBehavior.CauldronBehaviorMap("milk", MILK_CAULDRON_BEHAVIOR),
                    settings,
                    Biome.Precipitation.NONE),
            AbstractBlock.Settings.create().mapColor(MapColor.WHITE).strength(2.0F, 6.0F)
                    .requiresTool());

    public static final Block COLORED_CAULDRON = register(
            "colored_cauldron",
            settings -> new ColoredCauldron(Biome.Precipitation.NONE,
                    new CauldronBehavior.CauldronBehaviorMap("colored", COLORED_CAULDRON_BEHAVIOR),
                    settings,
                    Biome.Precipitation.NONE),
            AbstractBlock.Settings.create().strength(2.0F, 6.0F)
                    .requiresTool().luminance(STATE_TO_LUMINANCE_1).nonOpaque());

    public static final Block POTION_CAULDRON = register(
            "cauldron_with_potions",
            settings -> new PotionCauldron(Biome.Precipitation.NONE,
                    new CauldronBehavior.CauldronBehaviorMap("potion", POTION_CAULDRON_BEHAVIOR),
                    settings,
                    Biome.Precipitation.NONE),
            AbstractBlock.Settings.create().strength(2.0F, 6.0F)
                    .requiresTool().luminance(STATE_TO_LUMINANCE_2).nonOpaque());

    public static final Block BAD_OMEN_CAULDRON = register("cauldron_with_bad_omen",
            settings -> new CauldronWithBadOmen(Biome.Precipitation.NONE,
                    new CauldronBehavior.CauldronBehaviorMap("bad_omen", BAD_OMEN_CAULDRON_BEHAVIOR),
                    settings,
                    Biome.Precipitation.NONE),
            AbstractBlock.Settings.create().strength(114514F, 9999.0F).requiresTool());





    public static void registerBlockItems(String name, Block block) {
        Items.register(block);
    }

    public static Block register(String name, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings) {
        final Identifier identifier = Identifier.of(CauldronFix.MOD_ID, name);
        final RegistryKey<Block> registryKey = RegistryKey.of(RegistryKeys.BLOCK, identifier);
        final Block b = Blocks.register(registryKey, factory, settings);
        registerBlockItems(name, b);
        return b;
    }
    public static void registerModBlocks(){
        CauldronFix.LOGGER.info("Registering Blocks");
    }
}
