package mypals.ml.item;

import mypals.ml.block.ModBlocks;
import mypals.ml.CauldronFix;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    public static final ItemGroup CAULDRON_FIX_GROUP = Registry.register(Registries.ITEM_GROUP, Identifier.of(CauldronFix.MOD_ID, "retutorial_group"),
            ItemGroup.create(null, -1).displayName(Text.translatable("itemGroup.cauldron_fix"))
                    .icon(() -> new ItemStack(Blocks.CAULDRON)).entries((displayContext, entries) -> {
                        entries.add(ModBlocks.CAULDRON_WITH_OBSIDIAN);
                        entries.add(ModBlocks.CAULDRON_WITH_STONE);
                        entries.add(ModBlocks.CAULDRON_WITH_HALF_COBBLE_STONE);
                        entries.add(ModBlocks.CAULDRON_WITH_EMBER);
                        entries.add(ModBlocks.CAULDRON_WITH_COBBLE_STONE);
                        entries.add(ModBlocks.CAULDRON_WITH_GRAVEL);
                        entries.add(ModBlocks.DRAGONS_BREATH_CAULDRON);
                        entries.add(ModBlocks.HONEY_CAULDRON);
                        entries.add(ModBlocks.MILK_CAULDRON);
                        entries.add(ModBlocks.BAD_OMEN_CAULDRON);
                        entries.add(Blocks.CAULDRON);
                    }).build());
    public static void registerModItemGroups() {
        CauldronFix.LOGGER.info("Registering Item Groups");
    }
}
