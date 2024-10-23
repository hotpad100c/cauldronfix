package mypals.ml.datagen;

import mypals.ml.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends FabricTagProvider.BlockTagProvider {
    public ModBlockTagsProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(ModBlocks.CAULDRON_WITH_COBBLE_STONE)
                .add(ModBlocks.CAULDRON_WITH_OBSIDIAN)
                .add(ModBlocks.CAULDRON_WITH_STONE)
                .add(ModBlocks.CAULDRON_WITH_HALF_COBBLE_STONE)
                .add(ModBlocks.CAULDRON_WITH_GRAVEL);

        getOrCreateTagBuilder(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(ModBlocks.CAULDRON_WITH_OBSIDIAN);
        getOrCreateTagBuilder(BlockTags.CAULDRONS)
                .add(ModBlocks.MILK_CAULDRON)
                .add(ModBlocks.HONEY_CAULDRON)
                .add(ModBlocks.DRAGONS_BREATH_CAULDRON);
    }
}