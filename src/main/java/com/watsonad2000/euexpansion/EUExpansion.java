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

import cpw.mods.fml.common.event.FMLServerStoppingEvent;

@Mod(modid = EUExpansion.MODID, version = "1.0.0", name = "EU Expansion", acceptedMinecraftVersions = "[1.7.10]")
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

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Run before anything else. Read your config, create blocks, items, etc.
        tesseract = new Tesseract();
        GameRegistry.registerBlock(tesseract, "tesseract");
        GameRegistry.registerTileEntity(TileEntityTesseract.class, "TileEntityTesseract");

        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());

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
        if (proxy != null) proxy.serverStarting(event);
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        TesseractNetwork.clear();
    }
}
