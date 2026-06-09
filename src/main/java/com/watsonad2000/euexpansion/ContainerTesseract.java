package com.watsonad2000.euexpansion;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ic2.api.item.IElectricItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerTesseract extends Container {

    private final TileEntityTesseract tileTesseract;
    private int lastStoredEnergy = -1;
    private int lastMaxEnergy = -1;
    private int lastChannel = -1;
    private int lastMode = -1;
    private int lastEfficiency = -1;
    private int lastHullHeat = -1;

    private int storedEnergyLower = 0;
    private int storedEnergyUpper = 0;
    private int maxEnergyLower = 0;
    private int maxEnergyUpper = 0;

    public ContainerTesseract(InventoryPlayer playerInventory, TileEntityTesseract tileTesseract) {
        this.tileTesseract = tileTesseract;

        // Add 3 crystal slots (Row 2 of dispenser grid)
        for (int i = 0; i < 3; ++i) {
            this.addSlotToContainer(new Slot(tileTesseract, i, 62 + i * 18, 35) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return stack != null && stack.getItem() instanceof IElectricItem;
                }

                @Override
                public int getSlotStackLimit() {
                    return 1;
                }
            });
        }

        // Add 3 transformer upgrade slots (Row 3 of dispenser grid)
        for (int i = 0; i < 3; ++i) {
            this.addSlotToContainer(new Slot(tileTesseract, 3 + i, 62 + i * 18, 53) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return TileEntityTesseract.isTransformerUpgrade(stack);
                }

                @Override
                public int getSlotStackLimit() {
                    return 1;
                }
            });
        }

        // Add 3 cooling slots (Row 1 of dispenser grid)
        for (int i = 0; i < 3; ++i) {
            this.addSlotToContainer(new Slot(tileTesseract, 6 + i, 62 + i * 18, 17) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return TileEntityTesseract.getActiveCoolingCellCapacity(stack) > 0.0 ||
                           TileEntityTesseract.getVentCoolingValue(stack) > 0.0 ||
                           TileEntityTesseract.isDepletedCoolingCellOrCondensator(stack);
                }

                @Override
                public int getSlotStackLimit() {
                    return 1;
                }
            });
        }

        // Add player inventory slots
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // Add player hotbar slots
        for (int i = 0; i < 9; ++i) {
            this.addSlotToContainer(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tileTesseract.isUseableByPlayer(player);
    }

    @Override
    public boolean enchantItem(EntityPlayer player, int id) {
        if (tileTesseract.getWorldObj().isRemote) return false;

        if (id >= 0 && id <= 3) {
            int oldChannel = tileTesseract.channel;
            int newChannel = oldChannel;

            if (id == 0) {
                newChannel = Math.max(0, oldChannel - 1);
            } else if (id == 1) {
                newChannel = Math.min(63, oldChannel + 1);
            } else if (id == 2) {
                newChannel = Math.max(0, oldChannel - 10);
            } else if (id == 3) {
                newChannel = Math.min(63, oldChannel + 10);
            }
            newChannel = Math.min(63, Math.max(0, newChannel));

            if (newChannel != oldChannel) {
                TesseractNetwork.changeChannel(tileTesseract, tileTesseract.channelStr, String.valueOf(newChannel));
                tileTesseract.channel = newChannel;
                tileTesseract.channelStr = String.valueOf(newChannel);
                tileTesseract.markDirty();
                return true;
            }
        }
        return false;
    }

    @Override
    public void addCraftingToCrafters(ICrafting listener) {
        super.addCraftingToCrafters(listener);
        int stored = (int) TesseractNetwork.getStoredEnergy(tileTesseract.channelStr);
        int max = (int) TesseractNetwork.getMaxEnergy(tileTesseract.channelStr);
        
        listener.sendProgressBarUpdate(this, 0, stored & 0xFFFF);
        listener.sendProgressBarUpdate(this, 1, (stored >> 16) & 0xFFFF);
        listener.sendProgressBarUpdate(this, 2, max & 0xFFFF);
        listener.sendProgressBarUpdate(this, 3, (max >> 16) & 0xFFFF);
        listener.sendProgressBarUpdate(this, 4, tileTesseract.channel);
        listener.sendProgressBarUpdate(this, 5, tileTesseract.mode);
        listener.sendProgressBarUpdate(this, 6, (int) Math.round(tileTesseract.getEfficiency() * 1000));
        listener.sendProgressBarUpdate(this, 7, (int) Math.round(tileTesseract.hullHeat));
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        int stored = (int) TesseractNetwork.getStoredEnergy(tileTesseract.channelStr);
        int max = (int) TesseractNetwork.getMaxEnergy(tileTesseract.channelStr);
        int chan = tileTesseract.channel;
        int mode = tileTesseract.mode;
        int eff = (int) Math.round(tileTesseract.getEfficiency() * 1000);
        int heat = (int) Math.round(tileTesseract.hullHeat);

        for (int i = 0; i < this.crafters.size(); ++i) {
            ICrafting icrafting = (ICrafting) this.crafters.get(i);

            if (this.lastStoredEnergy != stored) {
                icrafting.sendProgressBarUpdate(this, 0, stored & 0xFFFF);
                icrafting.sendProgressBarUpdate(this, 1, (stored >> 16) & 0xFFFF);
            }
            if (this.lastMaxEnergy != max) {
                icrafting.sendProgressBarUpdate(this, 2, max & 0xFFFF);
                icrafting.sendProgressBarUpdate(this, 3, (max >> 16) & 0xFFFF);
            }
            if (this.lastChannel != chan) {
                icrafting.sendProgressBarUpdate(this, 4, chan);
            }
            if (this.lastMode != mode) {
                icrafting.sendProgressBarUpdate(this, 5, mode);
            }
            if (this.lastEfficiency != eff) {
                icrafting.sendProgressBarUpdate(this, 6, eff);
            }
            if (this.lastHullHeat != heat) {
                icrafting.sendProgressBarUpdate(this, 7, heat);
            }
        }

        this.lastStoredEnergy = stored;
        this.lastMaxEnergy = max;
        this.lastChannel = chan;
        this.lastMode = mode;
        this.lastEfficiency = eff;
        this.lastHullHeat = heat;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void updateProgressBar(int id, int data) {
        if (id == 0) {
            storedEnergyLower = data & 0xFFFF;
        } else if (id == 1) {
            storedEnergyUpper = data & 0xFFFF;
        } else if (id == 2) {
            maxEnergyLower = data & 0xFFFF;
        } else if (id == 3) {
            maxEnergyUpper = data & 0xFFFF;
        } else if (id == 4) {
            this.tileTesseract.channel = (data == -1) ? -1 : Math.min(63, Math.max(0, data));
        } else if (id == 5) {
            this.tileTesseract.mode = data;
        } else if (id == 6) {
            this.tileTesseract.clientEfficiency = data / 1000.0;
        } else if (id == 7) {
            this.tileTesseract.clientHullHeat = data;
        }

        this.tileTesseract.storedEnergy = (storedEnergyUpper << 16) | (storedEnergyLower & 0xFFFF);
        this.tileTesseract.maxEnergy = (maxEnergyUpper << 16) | (maxEnergyLower & 0xFFFF);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.inventorySlots.get(slotIndex);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            // From Tesseract inventory (slots 0-8) to player inventory
            if (slotIndex < 9) {
                if (!this.mergeItemStack(itemstack1, 9, 45, true)) {
                    return null;
                }
            }
            // From player inventory to Tesseract slots
            else {
                if (itemstack1.getItem() instanceof IElectricItem) {
                    if (!this.mergeItemStack(itemstack1, 0, 3, false)) {
                        return null;
                    }
                } else if (TileEntityTesseract.isTransformerUpgrade(itemstack1)) {
                    if (!this.mergeItemStack(itemstack1, 3, 6, false)) {
                        return null;
                    }
                } else if (TileEntityTesseract.getActiveCoolingCellCapacity(itemstack1) > 0.0 ||
                           TileEntityTesseract.getVentCoolingValue(itemstack1) > 0.0 ||
                           TileEntityTesseract.isDepletedCoolingCellOrCondensator(itemstack1)) {
                    if (!this.mergeItemStack(itemstack1, 6, 9, false)) {
                        return null;
                    }
                } else {
                    return null;
                }
            }

            if (itemstack1.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.stackSize == itemstack.stackSize) {
                return null;
            }

            slot.onPickupFromSlot(player, itemstack1);
        }

        return itemstack;
    }

    @Override
    protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean flag = false;
        int k = startIndex;

        if (reverseDirection) {
            k = endIndex - 1;
        }

        Slot slot;
        ItemStack itemstack1;

        if (stack.isStackable()) {
            while (stack.stackSize > 0 && (!reverseDirection && k < endIndex || reverseDirection && k >= startIndex)) {
                slot = (Slot) this.inventorySlots.get(k);
                itemstack1 = slot.getStack();

                int maxLimit = Math.min(stack.getMaxStackSize(), slot.getSlotStackLimit());

                if (itemstack1 != null && itemstack1.getItem() == stack.getItem() && 
                    (!stack.getHasSubtypes() || stack.getItemDamage() == itemstack1.getItemDamage()) && 
                    ItemStack.areItemStackTagsEqual(stack, itemstack1)) {
                    
                    int l = itemstack1.stackSize + stack.stackSize;

                    if (l <= maxLimit) {
                        stack.stackSize = 0;
                        itemstack1.stackSize = l;
                        slot.onSlotChanged();
                        flag = true;
                    } else if (itemstack1.stackSize < maxLimit) {
                        stack.stackSize -= maxLimit - itemstack1.stackSize;
                        itemstack1.stackSize = maxLimit;
                        slot.onSlotChanged();
                        flag = true;
                    }
                }

                if (reverseDirection) {
                    --k;
                } else {
                    ++k;
                }
            }
        }

        if (stack.stackSize > 0) {
            if (reverseDirection) {
                k = endIndex - 1;
            } else {
                k = startIndex;
            }

            while (!reverseDirection && k < endIndex || reverseDirection && k >= startIndex) {
                slot = (Slot) this.inventorySlots.get(k);
                itemstack1 = slot.getStack();

                int maxLimit = Math.min(stack.getMaxStackSize(), slot.getSlotStackLimit());

                if (itemstack1 == null && slot.isItemValid(stack)) {
                    if (stack.stackSize <= maxLimit) {
                        slot.putStack(stack.copy());
                        stack.stackSize = 0;
                        slot.onSlotChanged();
                        flag = true;
                        break;
                    } else {
                        ItemStack putStack = stack.copy();
                        putStack.stackSize = maxLimit;
                        slot.putStack(putStack);
                        stack.stackSize -= maxLimit;
                        slot.onSlotChanged();
                        flag = true;
                    }
                }

                if (reverseDirection) {
                    --k;
                } else {
                    ++k;
                }
            }
        }

        return flag;
    }
}
