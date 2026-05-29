package com.watsonad2000.euexpansion;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static boolean explodeOnOverheat = true;
    public static double coolantLifeMultiplier = 6.0;
    public static int baseTier = 2;

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        explodeOnOverheat = configuration.getBoolean("explodeOnOverheat", Configuration.CATEGORY_GENERAL, explodeOnOverheat, "Whether the Tesseract explodes when it overheats (hull heat >= 10000).");
        coolantLifeMultiplier = configuration.get(Configuration.CATEGORY_GENERAL, "coolantLifeMultiplier", coolantLifeMultiplier, "Multiplier for the heat capacity / life of one-time use cooling cells.").getDouble();
        baseTier = configuration.getInt("baseTier", Configuration.CATEGORY_GENERAL, baseTier, 1, 5, "The starting tier of the Tesseract with 0 transformer upgrades (1 = LV 32 EU/t, 2 = MV 128 EU/t, 3 = HV 512 EU/t, 4 = EV 2048 EU/t, 5 = IV 8192 EU/t).");

        // Clean up obsolete greeting property from previous versions
        if (configuration.getCategory(Configuration.CATEGORY_GENERAL).containsKey("greeting")) {
            configuration.getCategory(Configuration.CATEGORY_GENERAL).remove("greeting");
        }

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
