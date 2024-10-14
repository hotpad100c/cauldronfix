package mypals.ml.DamageTypes;
import mypals.ml.CauldronFix;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import static mypals.ml.CauldronFix.MOD_ID;
public class ModDamageTypes{
    public static final RegistryKey<DamageType> FALLING_OBSIDIAN_CAULDRON_DAMAGE = registerDamageTypes("falling_obsidian_cauldron");
    public static RegistryKey<DamageType> registerDamageTypes(String name) {
        return RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(CauldronFix.MOD_ID, name));
    }
}
