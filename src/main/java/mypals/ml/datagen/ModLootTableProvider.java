package mypals.ml.datagen;
import mypals.ml.block.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModLootTableProvider extends FabricBlockLootTableProvider {
    public ModLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        addDrop(ModBlocks.CAULDRON_WITH_OBSIDIAN,CAULDRON_DROPS_SILK_TOUCH(Blocks.CAULDRON));
        addDrop(ModBlocks.CAULDRON_WITH_OBSIDIAN,CAULDRON_DROPS_SILK_TOUCH(Blocks.OBSIDIAN));
        //addDrop(ModBlocks.CAULDRON_WITH_OBSIDIAN,oreDrops(ModBlocks.CAULDRON_WITH_OBSIDIAN));


    }
    public LootTable.Builder CAULDRON_DROPS_SILK_TOUCH(Block drop_1_SilkTouch) {
        return this.dropsWithSilkTouch(drop_1_SilkTouch);
    }
    public LootTable.Builder CAULDRON_DROPS_NOT_SILK_TOUCH(Block drop_1_Not_SilkTouch) {
        return this.drops(drop_1_Not_SilkTouch);
    }
}