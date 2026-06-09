package com.watsonad2000.euexpansion;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;

@Mod(modid = EUExpansion.MODID, version = "1.0.0", name = "EU Expansion", acceptedMinecraftVersions = "[1.7.10]", dependencies = "required-after:IC2;after:Waila", guiFactory = "com.watsonad2000.euexpansion.GuiFactory")
public class EUExpansion {

    public static final String MODID = "euexpansion";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @Mod.Instance(MODID)
    public static EUExpansion instance;

    @SidedProxy(clientSide = "com.watsonad2000.euexpansion.ClientProxy", serverSide = "com.watsonad2000.euexpansion.CommonProxy")
    public static CommonProxy proxy;

    public static final CreativeTabs tabEUExpansion = new CreativeTabs("tabEUExpansion") {
        @Override
        @SideOnly(Side.CLIENT)
        public Item getTabIconItem() {
            return Item.getItemFromBlock(tesseract);
        }
    };

    public static Block tesseract;
    public static Block vacuumHopper;
    public static Block inductionMacerator;
    public static Block combustionGenerator;
    public static Block multiTapTransformer;
    public static Block cropGrower;
    public static Block thermalGenerator;
    public static Item nanoBoots;
    public static Item tesseractKey;
    public static Item electricMultiTool;
    public static final java.util.UUID bootsSpeedModifierUUID = java.util.UUID.fromString("48e8a60e-8c38-4e89-be26-21557bf855b7");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Run before anything else. Read your config, create blocks, items, etc.
        tesseract = new Tesseract();
        GameRegistry.registerBlock(tesseract, "tesseract");
        GameRegistry.registerTileEntity(TileEntityTesseract.class, "TileEntityTesseract");

        vacuumHopper = new VacuumHopper();
        GameRegistry.registerBlock(vacuumHopper, "vacuumHopper");
        GameRegistry.registerTileEntity(TileEntityVacuumHopper.class, "TileEntityVacuumHopper");

        nanoBoots = new CarbonNanoBoots();
        GameRegistry.registerItem(nanoBoots, "carbonNanoBoots");

        tesseractKey = new ItemTesseractKey();
        GameRegistry.registerItem(tesseractKey, "tesseractKey");

        electricMultiTool = new ItemElectricMultiTool();
        GameRegistry.registerItem(electricMultiTool, "electricMultiTool");

        inductionMacerator = new BlockInductionMacerator();
        GameRegistry.registerBlock(inductionMacerator, "inductionMacerator");
        GameRegistry.registerTileEntity(TileEntityInductionMacerator.class, "TileEntityInductionMacerator");

        combustionGenerator = new BlockCombustionGenerator();
        GameRegistry.registerBlock(combustionGenerator, "combustionGenerator");
        GameRegistry.registerTileEntity(TileEntityCombustionGenerator.class, "TileEntityCombustionGenerator");

        multiTapTransformer = new BlockMultiTapTransformer();
        GameRegistry.registerBlock(multiTapTransformer, "multiTapTransformer");
        GameRegistry.registerTileEntity(TileEntityMultiTapTransformer.class, "TileEntityMultiTapTransformer");

        cropGrower = new BlockCropGrower();
        GameRegistry.registerBlock(cropGrower, "cropGrower");
        GameRegistry.registerTileEntity(TileEntityCropGrower.class, "TileEntityCropGrower");

        thermalGenerator = new BlockThermalGenerator();
        GameRegistry.registerBlock(thermalGenerator, "thermalGenerator");
        GameRegistry.registerTileEntity(TileEntityThermalGenerator.class, "TileEntityThermalGenerator");

        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);

        if (proxy != null) proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Do your mod setup. Build data structures. Register recipes.
        net.minecraft.item.ItemStack teleporter = ic2.api.item.IC2Items.getItem("teleporter");
        net.minecraft.item.ItemStack glassFiberCable = ic2.api.item.IC2Items.getItem("glassFiberCableItem");
        net.minecraft.item.ItemStack teslaCoil = ic2.api.item.IC2Items.getItem("teslaCoil");

        if (teleporter != null && glassFiberCable != null && teslaCoil != null) {
            GameRegistry.addShapedRecipe(new net.minecraft.item.ItemStack(tesseract),
                "TTT",
                "CSC",
                "TTT",
                'T', teleporter,
                'C', glassFiberCable,
                'S', teslaCoil
            );
        }

        net.minecraft.item.ItemStack advancedAlloy = ic2.api.item.IC2Items.getItem("advancedAlloy");
        net.minecraft.item.ItemStack electrolyzer = ic2.api.item.IC2Items.getItem("electrolyzer");
        net.minecraft.item.ItemStack hopper = new net.minecraft.item.ItemStack(net.minecraft.init.Blocks.hopper);

        if (advancedAlloy != null && electrolyzer != null && hopper != null) {
            GameRegistry.addShapedRecipe(new net.minecraft.item.ItemStack(vacuumHopper),
                "AAA",
                "EHE",
                "AAA",
                'A', advancedAlloy,
                'E', electrolyzer,
                'H', hopper
            );
        }

        net.minecraft.item.ItemStack carbonPlate = ic2.api.item.IC2Items.getItem("carbonPlate");
        net.minecraft.item.ItemStack ic2NanoBoots = ic2.api.item.IC2Items.getItem("nanoBoots");

        if (carbonPlate != null && ic2NanoBoots != null) {
            ItemStack wildBoots = new ItemStack(ic2NanoBoots.getItem(), 1, net.minecraftforge.oredict.OreDictionary.WILDCARD_VALUE);

            GameRegistry.addShapedRecipe(new ItemStack(nanoBoots),
                "CCC",
                "CNC",
                "CCC",
                'C', carbonPlate,
                'N', wildBoots
            );
        }

        GameRegistry.addShapedRecipe(new ItemStack(tesseractKey),
            " I ",
            " R ",
            " E ",
            'I', new ItemStack(net.minecraft.init.Items.iron_ingot),
            'R', new ItemStack(net.minecraft.init.Items.redstone),
            'E', new ItemStack(net.minecraft.init.Items.ender_pearl)
        );

        net.minecraft.item.ItemStack advancedMachine = ic2.api.item.IC2Items.getItem("advancedMachine");
        net.minecraft.item.ItemStack ic2Macerator = ic2.api.item.IC2Items.getItem("macerator");
        net.minecraft.item.ItemStack copperIngot = ic2.api.item.IC2Items.getItem("copperIngot");

        if (advancedMachine != null && ic2Macerator != null && copperIngot != null) {
            GameRegistry.addShapedRecipe(new ItemStack(inductionMacerator),
                "YYY",
                "MAM",
                "YYY",
                'Y', advancedAlloy,
                'M', ic2Macerator,
                'A', advancedMachine
            );
        }

        net.minecraft.item.ItemStack basicGenerator = ic2.api.item.IC2Items.getItem("generator");
        net.minecraft.item.ItemStack tnt = new ItemStack(net.minecraft.init.Blocks.tnt);
        net.minecraft.item.ItemStack gunpowder = new ItemStack(net.minecraft.init.Items.gunpowder);

        if (basicGenerator != null) {
            GameRegistry.addShapedRecipe(new ItemStack(combustionGenerator),
                "GPG",
                "GTG",
                "GPG",
                'G', gunpowder,
                'T', tnt,
                'P', basicGenerator
            );
        }

        net.minecraft.item.ItemStack hvTransformer = ic2.api.item.IC2Items.getItem("hvTransformer");
        net.minecraft.item.ItemStack mvTransformer = ic2.api.item.IC2Items.getItem("mvTransformer");
        net.minecraft.item.ItemStack goldCable = ic2.api.item.IC2Items.getItem("insulatedGoldCableItem");
        net.minecraft.item.ItemStack copperCable = ic2.api.item.IC2Items.getItem("insulatedCopperCableItem");

        if (hvTransformer != null && mvTransformer != null && goldCable != null && copperCable != null) {
            GameRegistry.addShapedRecipe(new ItemStack(multiTapTransformer),
                " C ",
                "GTM",
                " C ",
                'T', hvTransformer,
                'M', mvTransformer,
                'G', goldCable,
                'C', copperCable
            );
        }

        net.minecraft.item.ItemStack machineCasing = ic2.api.item.IC2Items.getItem("machine");
        net.minecraft.item.ItemStack electronicCircuit = ic2.api.item.IC2Items.getItem("electronicCircuit");
        if (machineCasing != null && electronicCircuit != null && copperCable != null) {
            GameRegistry.addShapedRecipe(new ItemStack(cropGrower),
                "ECE",
                "DMD",
                "WBW",
                'E', electronicCircuit,
                'C', copperCable,
                'D', new ItemStack(net.minecraft.init.Blocks.dirt),
                'M', machineCasing,
                'W', new ItemStack(net.minecraft.init.Items.water_bucket),
                'B', new ItemStack(net.minecraft.init.Items.bucket)
            );
        }

        if (basicGenerator != null && electronicCircuit != null && copperCable != null && copperIngot != null) {
            GameRegistry.addShapedRecipe(new ItemStack(thermalGenerator),
                "CEC",
                "IGI",
                "CEC",
                'C', copperCable,
                'E', electronicCircuit,
                'I', copperIngot,
                'G', basicGenerator
            );
        }

        ItemStack drill = ic2.api.item.IC2Items.getItem("miningDrill");
        ItemStack chainsaw = ic2.api.item.IC2Items.getItem("chainsaw");
        ItemStack hoe = ic2.api.item.IC2Items.getItem("bronzeHoe");
        if (drill != null && chainsaw != null && hoe != null && electronicCircuit != null) {
            GameRegistry.addShapedRecipe(new ItemStack(electricMultiTool),
                " H ",
                "CDC",
                " S ",
                'H', hoe,
                'D', drill,
                'S', chainsaw,
                'C', electronicCircuit
            );
        }

        // Register Waila compatibility callback
        cpw.mods.fml.common.event.FMLInterModComms.sendMessage(
            "Waila", 
            "register", 
            "com.watsonad2000.euexpansion.compat.WailaCompat.register"
        );

        if (proxy != null) proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        // Handle interaction with other mods, complete your setup.
        if (proxy != null) proxy.postInit(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        // Register server commands in this event handler.
        event.registerServerCommand(new CommandTesseractLocator());
        if (proxy != null) proxy.serverStarting(event);
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        TesseractNetwork.clear();
    }

    @SubscribeEvent
    public void onFall(LivingFallEvent event) {
        if (event.entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.entityLiving;
            ItemStack boots = player.getCurrentArmor(0); // 0 = boots
            if (boots != null && boots.getItem() == nanoBoots) {
                double charge = ic2.api.item.ElectricItem.manager.getCharge(boots);
                double cost = event.distance * 1000.0;
                if (cost > 0 && charge > 0) {
                    if (charge >= cost) {
                        ic2.api.item.ElectricItem.manager.discharge(boots, cost, 3, true, false, false);
                        event.setCanceled(true);
                    } else {
                        double absorbed = charge / 1000.0;
                        ic2.api.item.ElectricItem.manager.discharge(boots, charge, 3, true, false, false);
                        event.distance -= (float) absorbed;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerUpdate(LivingUpdateEvent event) {
        if (event.entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.entityLiving;
            ItemStack boots = player.getCurrentArmor(0);
            boolean hasBoots = boots != null && boots.getItem() == nanoBoots && ic2.api.item.ElectricItem.manager.getCharge(boots) >= 100.0;

            // Speed boost using AttributeModifier
            net.minecraft.entity.ai.attributes.IAttributeInstance speedAttr = player.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.movementSpeed);
            if (speedAttr != null) {
                net.minecraft.entity.ai.attributes.AttributeModifier existing = speedAttr.getModifier(bootsSpeedModifierUUID);
                if (hasBoots) {
                    double targetBoost = Config.bootsSpeedBoost;
                    if (existing == null || existing.getAmount() != targetBoost) {
                        if (existing != null) {
                            speedAttr.removeModifier(existing);
                        }
                        speedAttr.applyModifier(new net.minecraft.entity.ai.attributes.AttributeModifier(bootsSpeedModifierUUID, "Carbon Nano Boots Speed Boost", targetBoost, 1));
                    }
                } else {
                    if (existing != null) {
                        speedAttr.removeModifier(existing);
                    }
                }
            }

            // Auto step assist
            if (hasBoots) {
                if (player.stepHeight < 1.0F) {
                    player.stepHeight = 1.0F;
                    player.getEntityData().setBoolean("euexpansion.stepheight", true);
                }
            } else {
                if (player.getEntityData().getBoolean("euexpansion.stepheight")) {
                    player.stepHeight = 0.5F;
                    player.getEntityData().removeTag("euexpansion.stepheight");
                }
            }
        }
    }

    @SubscribeEvent
    public void onJump(LivingJumpEvent event) {
        if (event.entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.entityLiving;
            ItemStack boots = player.getCurrentArmor(0);
            if (boots != null && boots.getItem() == nanoBoots) {
                double charge = ic2.api.item.ElectricItem.manager.getCharge(boots);
                double cost = 500.0;
                if (charge >= cost) {
                    ic2.api.item.ElectricItem.manager.discharge(boots, cost, 3, true, false, false);
                    player.motionY = 0.42D * Math.sqrt(Config.bootsJumpHeight / 1.25D);
                }
            }
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equals(MODID)) {
            Config.sync();
        }
    }
}
