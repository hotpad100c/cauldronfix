package mypals.ml.block;
import mypals.ml.CauldronFix;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static final Block CAULDRON_WITH_OBSIDIAN = registerBlocks("cauldron_with_obsidian",
            new CAULDRON_WITH_OBSIDIAN(AbstractBlock.Settings.create().instrument(NoteBlockInstrument.BASEDRUM).strength(
                    50.0F, 1200.0F).pistonBehavior(PistonBehavior.BLOCK).nonOpaque().requiresTool().solid().mapColor(MapColor.STONE_GRAY)));
    public static final Block CAULDRON_WITH_STONE = registerBlocks("cauldron_with_stone",
            new CAULDRON_WITH_STONE(AbstractBlock.Settings.create().instrument(NoteBlockInstrument.BASEDRUM).strength(
                    1.5F, 6.0F).nonOpaque().requiresTool().solid().mapColor(MapColor.STONE_GRAY)));
    public static final Block CAULDRON_WITH_COBBLE_STONE = registerBlocks("cauldron_with_cobble_stone",
            new CAULDRON_WITH_COBBLE_STONE(AbstractBlock.Settings.create().instrument(NoteBlockInstrument.BASEDRUM).strength(
                    2.0F, 6.0F).nonOpaque().requiresTool().solid().mapColor(MapColor.STONE_GRAY)));
    public static final Block CAULDRON_WITH_HALF_COBBLE_STONE = registerBlocks("cauldron_with_some_cobble_stones",
            new CAULDRON_WITH_HALF_COBBLE_STONE(AbstractBlock.Settings.create().instrument(NoteBlockInstrument.BASEDRUM).strength(
                    2.0F, 6.0F).nonOpaque().requiresTool().mapColor(MapColor.STONE_GRAY)));

    public static void registerBlockItems(String name, Block block) {
        Item item = Registry.register(Registries.ITEM, Identifier.of(CauldronFix.MOD_ID, name), new BlockItem(block, new Item.Settings()));
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
