package com.watsonad2000.euexpansion;

public class ClientProxy extends CommonProxy {

    @Override
    public Object getClientGuiElement(int ID, net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, int x, int y, int z) {
        net.minecraft.tileentity.TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof TileEntityTesseract) {
            return new GuiTesseract(player.inventory, (TileEntityTesseract) tileEntity);
        }
        return null;
    }
}
