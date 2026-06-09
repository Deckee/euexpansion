package com.watsonad2000.euexpansion;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(cpw.mods.fml.common.event.FMLInitializationEvent event) {
        super.init(event);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);

        if (cpw.mods.fml.common.Loader.isModLoaded("NotEnoughItems")) {
            NeiIntegration.register();
        }
    }

    @Override
    public Object getClientGuiElement(int ID, net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, int x, int y, int z) {
        net.minecraft.tileentity.TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityTesseract) {
            return new GuiTesseract(player.inventory, (TileEntityTesseract) tileEntity);
        }
        if (tileEntity instanceof TileEntityVacuumHopper) {
            return new GuiVacuumHopper(player.inventory, (TileEntityVacuumHopper) tileEntity);
        }
        if (tileEntity instanceof TileEntityInductionMacerator) {
            return new GuiInductionMacerator(new ContainerInductionMacerator(player, (TileEntityInductionMacerator) tileEntity));
        }
        if (tileEntity instanceof TileEntityCombustionGenerator) {
            return new GuiCombustionGenerator(new ContainerCombustionGenerator(player, (TileEntityCombustionGenerator) tileEntity));
        }
        if (tileEntity instanceof TileEntityCropGrower) {
            return new GuiCropGrower(new ContainerCropGrower(player, (TileEntityCropGrower) tileEntity));
        }
        return null;
    }

    @cpw.mods.fml.common.eventhandler.SubscribeEvent
    public void onFOVUpdate(net.minecraftforge.client.event.FOVUpdateEvent event) {
        if (event.entity != null) {
            net.minecraft.item.ItemStack boots = event.entity.getCurrentArmor(0); // 0 = boots
            if (boots != null && boots.getItem() == EUExpansion.nanoBoots) {
                double charge = ic2.api.item.ElectricItem.manager.getCharge(boots);
                if (charge >= 100.0) {
                    net.minecraft.entity.ai.attributes.IAttributeInstance speedAttr = event.entity.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.movementSpeed);
                    if (speedAttr != null) {
                        double attributeValue = speedAttr.getAttributeValue();
                        double walkSpeed = event.entity.capabilities.getWalkSpeed();
                        double attributeValueWithoutBoots = attributeValue - (Config.bootsSpeedBoost * speedAttr.getBaseValue());
                        
                        double f_with = (attributeValue / walkSpeed + 1.0D) / 2.0D;
                        double f_without = (attributeValueWithoutBoots / walkSpeed + 1.0D) / 2.0D;
                        if (f_without > 0.0D && f_with > 0.0D) {
                            event.newfov = (float) (event.newfov * (f_without / f_with));
                        }
                    }
                }
            }
        }
    }

    @cpw.mods.fml.common.eventhandler.SubscribeEvent
    public void onRenderPlayer(net.minecraftforge.client.event.RenderPlayerEvent.Pre event) {
        if (Config.enableDeveloperCapes && event.entityPlayer instanceof net.minecraft.client.entity.AbstractClientPlayer) {
            net.minecraft.client.entity.AbstractClientPlayer player = (net.minecraft.client.entity.AbstractClientPlayer) event.entityPlayer;
            boolean isDev = false;

            // Check UUID
            if (player.getUniqueID() != null) {
                String playerUuid = player.getUniqueID().toString();
                for (String uuid : Config.capeUUIDs) {
                    if (playerUuid.equalsIgnoreCase(uuid.trim())) {
                        isDev = true;
                        break;
                    }
                }
            }

            // Check username if not matched by UUID
            if (!isDev && player.getCommandSenderName() != null) {
                String name = player.getCommandSenderName();
                for (String username : Config.capeUsernames) {
                    if (name.equalsIgnoreCase(username.trim())) {
                        isDev = true;
                        break;
                    }
                }
            }

            if (isDev) {
                net.minecraft.util.ResourceLocation customCape = new net.minecraft.util.ResourceLocation("euexpansion:textures/capes/developer.png");
                if (player.getLocationCape() == null || !player.getLocationCape().equals(customCape)) {
                    player.func_152121_a(com.mojang.authlib.minecraft.MinecraftProfileTexture.Type.CAPE, customCape);
                }
            }
        }
    }
}
