package com.watsonad2000.euexpansion;

import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityTesseract) {
            return new ContainerTesseract(player.inventory, (TileEntityTesseract) tileEntity);
        }
        if (tileEntity instanceof TileEntityVacuumHopper) {
            return new ContainerVacuumHopper(player.inventory, (TileEntityVacuumHopper) tileEntity);
        }
        if (tileEntity instanceof TileEntityInductionMacerator) {
            return new ContainerInductionMacerator(player, (TileEntityInductionMacerator) tileEntity);
        }
        if (tileEntity instanceof TileEntityCombustionGenerator) {
            return new ContainerCombustionGenerator(player, (TileEntityCombustionGenerator) tileEntity);
        }
        if (tileEntity instanceof TileEntityCropGrower) {
            return new ContainerCropGrower(player, (TileEntityCropGrower) tileEntity);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return EUExpansion.proxy.getClientGuiElement(ID, player, world, x, y, z);
    }
}
