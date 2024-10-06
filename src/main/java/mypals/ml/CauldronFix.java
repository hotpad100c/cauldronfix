package mypals.ml;

import mypals.ml.block.ModBlocks;
import mypals.ml.item.ModItemGroups;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CauldronFix implements ModInitializer {

	public static final String MOD_ID = "cauldronfix";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		ModBlocks.registerModBlocks();
		ModItemGroups.registerModItemGroups();
		LOGGER.info("Hello Fabric world!");
	}
}