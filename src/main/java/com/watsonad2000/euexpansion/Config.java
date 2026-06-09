package com.watsonad2000.euexpansion;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static boolean explodeOnOverheat = true;
    public static double coolantLifeMultiplier = 6.0;
    public static int baseTier = 2;

    public static int vacuumHopperBaseRange = 2;
    public static int vacuumHopperRangePerUpgrade = 2;
    public static int vacuumHopperBaseCost = 32;
    public static int vacuumHopperCostPerItem = 100;

    public static float bootsSpeedBoost = 0.33F;
    public static float bootsJumpHeight = 1.75F;

    public static boolean enableDeveloperCapes = true;
    public static String[] capeUsernames = new String[] { "Developer", "watsonad2000" };
    public static String[] capeUUIDs = new String[] { "7af0bae9-3667-47ee-84b9-4ef67d53fbf5" };

    public static boolean enableDirtDepletion = true;
    public static int dirtDepletionCycles = 20;

    public static double thermalGeneratorCoefficient = 0.2D;
    public static double tempLava = 100.0D;
    public static double tempUranium = 140.0D;
    public static double tempSilver = -20.0D;
    public static double tempCopper = -10.0D;
    public static double tempGold = -5.0D;
    public static double tempWater = 20.0D;
    public static double tempIron = 20.0D;
    public static double tempTin = 30.0D;
    public static double tempBronze = 30.0D;
    public static double tempLead = 60.0D;
    public static int lifetimeLava = 36000;
    public static int lifetimeUranium = 288000;
    public static int lifetimeWater = 36000;

    public static Configuration configuration;

    public static void synchronizeConfiguration(File configFile) {
        configuration = new Configuration(configFile);
        sync();
    }

    public static void sync() {
        explodeOnOverheat = configuration.getBoolean("explodeOnOverheat", Configuration.CATEGORY_GENERAL, explodeOnOverheat, "Whether the Tesseract explodes when it overheats (hull heat >= 10000).");
        coolantLifeMultiplier = configuration.get(Configuration.CATEGORY_GENERAL, "coolantLifeMultiplier", coolantLifeMultiplier, "Multiplier for the heat capacity / life of one-time use cooling cells.").getDouble();
        baseTier = configuration.getInt("baseTier", Configuration.CATEGORY_GENERAL, baseTier, 1, 5, "The starting tier of the Tesseract with 0 transformer upgrades (1 = LV 32 EU/t, 2 = MV 128 EU/t, 3 = HV 512 EU/t, 4 = EV 2048 EU/t, 5 = IV 8192 EU/t).");

        vacuumHopperBaseRange = configuration.getInt("vacuumHopperBaseRange", Configuration.CATEGORY_GENERAL, vacuumHopperBaseRange, 1, 64, "The starting range of the Vacuum Hopper with 0 overclocker upgrades.");
        vacuumHopperRangePerUpgrade = configuration.getInt("vacuumHopperRangePerUpgrade", Configuration.CATEGORY_GENERAL, vacuumHopperRangePerUpgrade, 0, 64, "The range increase per installed overclocker upgrade.");
        vacuumHopperBaseCost = configuration.getInt("vacuumHopperBaseCost", Configuration.CATEGORY_GENERAL, vacuumHopperBaseCost, 0, 10000, "Base EU cost to run the Vacuum Hopper per tick.");
        vacuumHopperCostPerItem = configuration.getInt("vacuumHopperCostPerItem", Configuration.CATEGORY_GENERAL, vacuumHopperCostPerItem, 0, 10000, "Additional EU cost per item successfully collected.");

        bootsSpeedBoost = configuration.getFloat("bootsSpeedBoost", Configuration.CATEGORY_GENERAL, bootsSpeedBoost, 0.0F, 5.0F, "Speed boost multiplier (+0.33 = +33% speed) for custom boots.");
        bootsJumpHeight = configuration.getFloat("bootsJumpHeight", Configuration.CATEGORY_GENERAL, bootsJumpHeight, 1.0F, 5.0F, "Jump height in blocks for custom boots.");

        enableDeveloperCapes = configuration.getBoolean("enableDeveloperCapes", Configuration.CATEGORY_GENERAL, enableDeveloperCapes, "Enable developer capes for the creators of the mod.");
        capeUsernames = configuration.getStringList("capeUsernames", Configuration.CATEGORY_GENERAL, capeUsernames, "List of player usernames who will receive the developer cape.");
        capeUUIDs = configuration.getStringList("capeUUIDs", Configuration.CATEGORY_GENERAL, capeUUIDs, "List of player UUIDs who will receive the developer cape.");

        enableDirtDepletion = configuration.getBoolean("enableDirtDepletion", Configuration.CATEGORY_GENERAL, enableDirtDepletion, "Whether dirt/grass/farmland blocks degrade to sand after several unfertilized growth cycles.");
        dirtDepletionCycles = configuration.getInt("dirtDepletionCycles", Configuration.CATEGORY_GENERAL, dirtDepletionCycles, 1, 10000, "The number of unfertilized growth cycles before a dirt block degrades to sand.");

        thermalGeneratorCoefficient = configuration.get(Configuration.CATEGORY_GENERAL, "thermalGeneratorCoefficient", thermalGeneratorCoefficient, "Coefficient for thermal generator power output: Power = coefficient * (T_hot - T_cold).").getDouble();
        tempLava = configuration.get(Configuration.CATEGORY_GENERAL, "tempLava", tempLava, "Temperature of Lava block (heat source).").getDouble();
        tempUranium = configuration.get(Configuration.CATEGORY_GENERAL, "tempUranium", tempUranium, "Temperature of Uranium block (heat source).").getDouble();
        
        tempSilver = configuration.get(Configuration.CATEGORY_GENERAL, "tempSilver", tempSilver, "Temperature of Silver block (cooling source).").getDouble();
        tempCopper = configuration.get(Configuration.CATEGORY_GENERAL, "tempCopper", tempCopper, "Temperature of Copper block (cooling source).").getDouble();
        tempGold = configuration.get(Configuration.CATEGORY_GENERAL, "tempGold", tempGold, "Temperature of Gold block (cooling source).").getDouble();
        tempWater = configuration.get(Configuration.CATEGORY_GENERAL, "tempWater", tempWater, "Temperature of Water block (cooling source).").getDouble();
        tempIron = configuration.get(Configuration.CATEGORY_GENERAL, "tempIron", tempIron, "Temperature of Iron block (cooling source).").getDouble();
        tempTin = configuration.get(Configuration.CATEGORY_GENERAL, "tempTin", tempTin, "Temperature of Tin block (cooling source).").getDouble();
        tempBronze = configuration.get(Configuration.CATEGORY_GENERAL, "tempBronze", tempBronze, "Temperature of Bronze block (cooling source).").getDouble();
        tempLead = configuration.get(Configuration.CATEGORY_GENERAL, "tempLead", tempLead, "Temperature of Lead block (cooling source).").getDouble();

        lifetimeLava = configuration.getInt("lifetimeLava", Configuration.CATEGORY_GENERAL, lifetimeLava, 1, 1000000, "Lifetime of Lava block in ticks (36000 = 30 minutes).");
        lifetimeUranium = configuration.getInt("lifetimeUranium", Configuration.CATEGORY_GENERAL, lifetimeUranium, 1, 10000000, "Lifetime of Uranium block in ticks (288000 = 4 hours).");
        lifetimeWater = configuration.getInt("lifetimeWater", Configuration.CATEGORY_GENERAL, lifetimeWater, 1, 1000000, "Evaporation lifetime of Water block in ticks (36000 = 30 minutes).");

        // Clean up obsolete greeting property from previous versions
        if (configuration.getCategory(Configuration.CATEGORY_GENERAL).containsKey("greeting")) {
            configuration.getCategory(Configuration.CATEGORY_GENERAL).remove("greeting");
        }

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
