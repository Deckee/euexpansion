package com.watsonad2000.euexpansion;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class Tesseract extends Block implements ITileEntityProvider {

    @SideOnly(Side.CLIENT)
    private IIcon topIcon;
    @SideOnly(Side.CLIENT)
    private IIcon bottomIcon;
    @SideOnly(Side.CLIENT)
    private IIcon frontIcon;
    @SideOnly(Side.CLIENT)
    private IIcon backIcon;
    @SideOnly(Side.CLIENT)
    private IIcon sideIcon;

    protected Tesseract() {
        super(Material.iron);
        this.setBlockName("tesseract");
        this.setHardness(3.0F);
        this.setCreativeTab(EUExpansion.tabEUExpansion);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityTesseract();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) {
            if (!world.isRemote) {
                float f = world.rand.nextFloat() * 0.8F + 0.1F;
                float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
                float f2 = world.rand.nextFloat() * 0.8F + 0.1F;
                EntityItem entityitem = new EntityItem(world, 
                    (double)((float)x + f), (double)((float)y + f1), (double)((float)z + f2), 
                    new ItemStack(this));
                world.spawnEntityInWorld(entityitem);
                world.setBlockToAir(x, y, z);
            }
            return true;
        }

        if (!world.isRemote) {
            player.openGui(EUExpansion.instance, 0, world, x, y, z);
        }
        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block oldBlock, int oldMeta) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityTesseract) {
            TileEntityTesseract tesseract = (TileEntityTesseract) tile;
            for (int i = 0; i < tesseract.getSizeInventory(); i++) {
                ItemStack stack = tesseract.getStackInSlot(i);
                if (stack != null) {
                    float f = world.rand.nextFloat() * 0.8F + 0.1F;
                    float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
                    float f2 = world.rand.nextFloat() * 0.8F + 0.1F;
                    while (stack.stackSize > 0) {
                        int j = world.rand.nextInt(21) + 10;
                        if (j > stack.stackSize) {
                            j = stack.stackSize;
                        }
                        stack.stackSize -= j;
                        EntityItem entityitem = new EntityItem(world, 
                            (double)((float)x + f), (double)((float)y + f1), (double)((float)z + f2), 
                            new ItemStack(stack.getItem(), j, stack.getItemDamage()));
                        if (stack.hasTagCompound()) {
                            entityitem.getEntityItem().setTagCompound((net.minecraft.nbt.NBTTagCompound)stack.getTagCompound().copy());
                        }
                        float f3 = 0.05F;
                        entityitem.motionX = (double)((float)world.rand.nextGaussian() * f3);
                        entityitem.motionY = (double)((float)world.rand.nextGaussian() * f3 + 0.2F);
                        entityitem.motionZ = (double)((float)world.rand.nextGaussian() * f3);
                        world.spawnEntityInWorld(entityitem);
                    }
                }
            }
            world.func_147453_f(x, y, z, oldBlock);
        }
        super.breakBlock(world, x, y, z, oldBlock, oldMeta);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int l = net.minecraft.util.MathHelper.floor_double((double)(placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        if (l == 0) {
            world.setBlockMetadataWithNotify(x, y, z, 2, 2);
        }
        if (l == 1) {
            world.setBlockMetadataWithNotify(x, y, z, 5, 2);
        }
        if (l == 2) {
            world.setBlockMetadataWithNotify(x, y, z, 3, 2);
        }
        if (l == 3) {
            world.setBlockMetadataWithNotify(x, y, z, 4, 2);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.bottomIcon = reg.registerIcon("euexpansion:tesseract/bottom");
        this.topIcon = reg.registerIcon("euexpansion:tesseract/top");
        this.frontIcon = reg.registerIcon("euexpansion:tesseract/front");
        this.backIcon = reg.registerIcon("euexpansion:tesseract/back");
        this.sideIcon = reg.registerIcon("euexpansion:tesseract/side");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (side == 0) return this.bottomIcon;
        if (side == 1) return this.topIcon;

        // If metadata is 0 (default/uninitialized), assume front is South (3) so the front face renders towards the player in inventory
        int frontSide = (meta >= 2 && meta <= 5) ? meta : 3;
        int backSide = net.minecraftforge.common.util.ForgeDirection.getOrientation(frontSide).getOpposite().ordinal();

        if (side == frontSide) {
            return this.frontIcon;
        } else if (side == backSide) {
            return this.backIcon;
        } else {
            return this.sideIcon;
        }
    }
}
