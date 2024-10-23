package mypals.ml.block;

import mypals.ml.CauldronFix;
import mypals.ml.block.advancedCauldron.coloredCauldrons.ColoredCauldronBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static mypals.ml.CauldronFix.MOD_ID;

public class ModBlockEntityTypes {
    public static <T extends BlockEntityType<?>> T register(String path, T blockEntityType) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(MOD_ID, path), blockEntityType);
    }

    public static final BlockEntityType<ColoredCauldronBlockEntity> COLORED_CAULDRON_BLOCK_ENTITY = register(
            "colored_cauldron",
            BlockEntityType.Builder.create(ColoredCauldronBlockEntity::new, ModBlocks.COLORED_CAULDRON).build()
    );

    public static void registerBlockEntities() {
        CauldronFix.LOGGER.info("Registering BlockEntities");
    }
}
