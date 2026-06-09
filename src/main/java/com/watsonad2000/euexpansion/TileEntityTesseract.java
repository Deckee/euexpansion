package com.watsonad2000.euexpansion;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.item.IElectricItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityTesseract extends TileEntity implements IEnergySink, IEnergySource, IInventory, ic2.api.tile.IWrenchable {

    public ItemStack[] inventory = new ItemStack[9]; // 0,1,2 = Crystals; 3,4,5 = Upgrades; 6,7,8 = Cooling
    public int channel = 0;
    public String channelStr = "0";
    public int mode = 2; // Forced to 2 (BOTH) to follow IC2 standards
    public double storedEnergy = 0.0;
    public double maxEnergy = 0.0;
    private boolean addedToEnergyNet = false;

    // Per-tick limits tracking
    public double energyInjectedThisTick = 0.0;
    public double energyDrawnThisTick = 0.0;

    // Cooling accumulation
    public double[] coolantDamageAccumulator = new double[3];
    public double clientEfficiency = 0.30;
    public double lastTickIO = 0.0;
    public double hullHeat = 0.0;
    public double clientHullHeat = 0.0;

    public static boolean isTransformerUpgrade(ItemStack stack) {
        if (stack == null) return false;
        ItemStack upgrade = ic2.api.item.IC2Items.getItem("transformerUpgrade");
        return upgrade != null && stack.getItem() == upgrade.getItem();
    }

    public static boolean isOverclockedVent(ItemStack stack) {
        if (stack == null) return false;
        ItemStack vent = ic2.api.item.IC2Items.getItem("reactorVentGold");
        return vent != null && stack.getItem() == vent.getItem();
    }

    public static boolean isAdvancedVent(ItemStack stack) {
        if (stack == null) return false;
        ItemStack vent = ic2.api.item.IC2Items.getItem("reactorVentDiamond");
        return vent != null && stack.getItem() == vent.getItem();
    }

    public static boolean isStandardVent(ItemStack stack) {
        if (stack == null) return false;
        ItemStack standard = ic2.api.item.IC2Items.getItem("reactorVent");
        ItemStack core = ic2.api.item.IC2Items.getItem("reactorVentCore");
        ItemStack spread = ic2.api.item.IC2Items.getItem("reactorVentSpread");
        
        return (standard != null && stack.getItem() == standard.getItem()) ||
               (core != null && stack.getItem() == core.getItem()) ||
               (spread != null && stack.getItem() == spread.getItem());
    }

    public static double getVentCoolingValue(ItemStack stack) {
        if (stack == null) return 0.0;
        
        ItemStack standard = ic2.api.item.IC2Items.getItem("reactorVent");
        ItemStack core = ic2.api.item.IC2Items.getItem("reactorVentCore");
        ItemStack spread = ic2.api.item.IC2Items.getItem("reactorVentSpread");
        ItemStack diamond = ic2.api.item.IC2Items.getItem("reactorVentDiamond");
        ItemStack gold = ic2.api.item.IC2Items.getItem("reactorVentGold");

        if (standard != null && stack.getItem() == standard.getItem()) return 6.0;
        if (core != null && stack.getItem() == core.getItem()) return 5.0;
        if (spread != null && stack.getItem() == spread.getItem()) return 4.0;
        if (diamond != null && stack.getItem() == diamond.getItem()) return 12.0;
        if (gold != null && stack.getItem() == gold.getItem()) return 20.0;
        
        return 0.0;
    }

    public static double getCellHeat(ItemStack stack) {
        if (stack == null) return 0.0;
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("tesseractHeat")) {
            return stack.getTagCompound().getDouble("tesseractHeat");
        }
        return stack.getItemDamage() & 0xFFFF;
    }

    public static void setCellHeat(ItemStack stack, double heat, double baseCap) {
        if (stack == null) return;
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setDouble("tesseractHeat", heat);
        
        if (baseCap > 0.0) {
            double maxMeta = Math.min(baseCap, 30000.0);
            int metaDamage = (int) Math.round((heat / baseCap) * maxMeta);
            // Ensure we never reach maxMeta (0 durability), leaving at least 1 durability remaining
            int limit = (int) maxMeta - 1;
            if (limit < 0) limit = 0;
            metaDamage = Math.max(0, Math.min(limit, metaDamage));
            stack.setItemDamage(metaDamage);
        } else {
            stack.setItemDamage(0);
        }
    }

    public static double getActiveCoolingCellCapacity(ItemStack stack) {
        if (stack == null) return 0.0;
        
        ItemStack simple = ic2.api.item.IC2Items.getItem("reactorCoolantSimple");
        ItemStack triple = ic2.api.item.IC2Items.getItem("reactorCoolantTriple");
        ItemStack six = ic2.api.item.IC2Items.getItem("reactorCoolantSix");
        ItemStack rsh = ic2.api.item.IC2Items.getItem("reactorCondensator");
        ItemStack lzh = ic2.api.item.IC2Items.getItem("reactorCondensatorLap");

        double cap = 0.0;
        if (simple != null && stack.getItem() == simple.getItem()) cap = 10000.0;
        else if (rsh != null && stack.getItem() == rsh.getItem()) cap = 20000.0;
        else if (triple != null && stack.getItem() == triple.getItem()) cap = 30000.0;
        else if (six != null && stack.getItem() == six.getItem()) cap = 60000.0;
        else if (lzh != null && stack.getItem() == lzh.getItem()) cap = 100000.0;

        if (cap > 0.0) {
            double limit = cap - 1.0;
            if (getCellHeat(stack) < limit) {
                return limit * Config.coolantLifeMultiplier;
            }
        }
        return 0.0;
    }

    public static boolean isDepletedCoolingCellOrCondensator(ItemStack stack) {
        if (stack == null) return false;
        ItemStack simple = ic2.api.item.IC2Items.getItem("reactorCoolantSimple");
        ItemStack triple = ic2.api.item.IC2Items.getItem("reactorCoolantTriple");
        ItemStack six = ic2.api.item.IC2Items.getItem("reactorCoolantSix");
        ItemStack rsh = ic2.api.item.IC2Items.getItem("reactorCondensator");
        ItemStack lzh = ic2.api.item.IC2Items.getItem("reactorCondensatorLap");
        
        return (simple != null && stack.getItem() == simple.getItem()) ||
               (rsh != null && stack.getItem() == rsh.getItem()) ||
               (triple != null && stack.getItem() == triple.getItem()) ||
               (six != null && stack.getItem() == six.getItem()) ||
               (lzh != null && stack.getItem() == lzh.getItem());
    }

    public double getEfficiency() {
        int totalCooling = 0;
        for (int i = 0; i < 9; i++) {
            totalCooling += (int) getVentCoolingValue(inventory[i]);
        }

        int activeCoolantCells = 0;
        for (int i = 6; i < 9; i++) {
            if (getActiveCoolingCellCapacity(inventory[i]) > 0.0) {
                activeCoolantCells++;
            }
        }

        if (activeCoolantCells == 3) {
            return 1.0;
        }

        double eff = 0.30 + (totalCooling / 100.0);
        return Math.min(1.0, Math.max(0.0, eff));
    }

    public double getEffectiveEfficiency() {
        if (worldObj != null && worldObj.isRemote) {
            return clientEfficiency;
        }
        return TesseractNetwork.getNetworkEfficiency(channelStr);
    }

    public int getTier() {
        int count = 0;
        for (int i = 3; i < 6; i++) {
            if (inventory[i] != null && isTransformerUpgrade(inventory[i])) {
                count += inventory[i].stackSize;
            }
        }
        return Math.min(5, Config.baseTier + count);
    }

    public double getMaxVoltage() {
        int tier = getTier();
        if (tier == 1) return 32.0;
        if (tier == 2) return 128.0;
        if (tier == 3) return 512.0;
        if (tier == 4) return 2048.0;
        return 8192.0;
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        
        if (!worldObj.isRemote) {
            this.lastTickIO = this.energyInjectedThisTick + this.energyDrawnThisTick;
            
            double transferred = Math.max(energyInjectedThisTick, energyDrawnThisTick);
            
            // 1. Calculate totalCooling by looping through all 9 slots of the 3x3 inventory
            int totalCooling = 0;
            for (int i = 0; i < 9; i++) {
                totalCooling += (int) getVentCoolingValue(inventory[i]);
            }
            
            // 2. Count active consumable cooling cells in designated slots (6, 7, 8)
            int activeCoolantCells = 0;
            for (int i = 6; i < 9; i++) {
                if (getActiveCoolingCellCapacity(inventory[i]) > 0.0) {
                    activeCoolantCells++;
                }
            }
            
            float efficiency;
            if (activeCoolantCells == 3) {
                efficiency = 1.0f;
            } else {
                efficiency = 0.30f + (totalCooling / 100.0f);
            }
            
            // 3. Calculate heatGenerated
            float heatGenerated = (transferred > 0.0) ? (efficiency * 66.67f) : 0.0f;
            
            // 4. Calculate unventedHeat
            float unventedHeat = heatGenerated - totalCooling;
            
            // 5. If unventedHeat <= 0, smoothly subtract from hull pool by cooling surplus
            if (unventedHeat <= 0.0f) {
                float coolingSurplus = -unventedHeat;
                hullHeat = Math.max(0.0f, (float) hullHeat - coolingSurplus);
                unventedHeat = 0.0f;
                markDirty();
            }
            
            // 6. Only if unventedHeat > 0 or (hullHeat > 0 and coolant cells are present), apply damage to cells or add to hull pool
            float heatToAbsorb = unventedHeat;
            float hullHeatToCool = 0.0f;
            
            boolean hasActiveCoolant = false;
            for (int i = 6; i < 9; i++) {
                if (getActiveCoolingCellCapacity(inventory[i]) > 0.0) {
                    hasActiveCoolant = true;
                    break;
                }
            }

            if (hullHeat > 0.0 && hasActiveCoolant) {
                hullHeatToCool = (float) Math.min(hullHeat, 600.0);
                heatToAbsorb += hullHeatToCool;
            }

            if (heatToAbsorb > 0.0f) {
                float initialHeatToAbsorb = heatToAbsorb;
                // Up to 3 passes to handle cells breaking and redistributing leftover heat
                for (int pass = 0; pass < 3; pass++) {
                    if (heatToAbsorb <= 0.0f) break;
                    
                    double totalMaxCap = 0.0;
                    int activeCount = 0;
                    for (int i = 6; i < 9; i++) {
                        ItemStack stack = inventory[i];
                        double cap = getActiveCoolingCellCapacity(stack);
                        if (cap > 0.0) {
                            totalMaxCap += cap;
                            activeCount++;
                        }
                    }
                    if (activeCount == 0) break;
                    
                    double absorbedThisPass = 0.0;
                    for (int i = 6; i < 9; i++) {
                        ItemStack stack = inventory[i];
                        double cap = getActiveCoolingCellCapacity(stack);
                        if (cap > 0.0) {
                            // Proportional share based on maximum capacity
                            double share = heatToAbsorb * (cap / totalMaxCap);
                            
                            double baseCap = cap / Config.coolantLifeMultiplier;
                            double currentDamage = getCellHeat(stack) + coolantDamageAccumulator[i - 6];
                            double remainingDamageCapacity = baseCap - currentDamage;
                            double remainingHeatCapacity = remainingDamageCapacity * Config.coolantLifeMultiplier;
                            
                            if (share <= remainingHeatCapacity) {
                                // Apply all of the share to the item's damage
                                double damageToAdd = share / Config.coolantLifeMultiplier;
                                coolantDamageAccumulator[i - 6] += damageToAdd;
                                int dmg = (int) coolantDamageAccumulator[i - 6];
                                if (dmg > 0) {
                                    coolantDamageAccumulator[i - 6] -= dmg;
                                    double newDamage = getCellHeat(stack) + dmg;
                                    if (newDamage >= baseCap) {
                                        setCellHeat(stack, baseCap, baseCap + 1.0);
                                        coolantDamageAccumulator[i - 6] = 0.0;
                                    } else {
                                        newDamage = Math.max(0.0, newDamage);
                                        setCellHeat(stack, newDamage, baseCap + 1.0);
                                    }
                                }
                                absorbedThisPass += share;
                            } else {
                                // Share is greater than remaining capacity: only apply enough damage to hit baseCap and leave it in slot
                                setCellHeat(stack, baseCap, baseCap + 1.0);
                                coolantDamageAccumulator[i - 6] = 0.0;
                                absorbedThisPass += remainingHeatCapacity;
                            }
                        }
                    }
                    
                    heatToAbsorb -= absorbedThisPass;
                    if (absorbedThisPass == 0.0) break;
                }
                
                // Calculate how much was actually absorbed
                float totalAbsorbed = initialHeatToAbsorb - heatToAbsorb;
                
                // First, satisfy the unventedHeat
                float absorbedForUnvented = Math.min(unventedHeat, totalAbsorbed);
                float absorbedForHull = totalAbsorbed - absorbedForUnvented;
                
                // Subtract from hullHeat
                if (absorbedForHull > 0.0f) {
                    hullHeat = Math.max(0.0, hullHeat - absorbedForHull);
                }
                
                // Remaining unabsorbed unvented heat goes straight to the hull only if no cooling vents are installed
                float unabsorbedUnvented = unventedHeat - absorbedForUnvented;
                if (unabsorbedUnvented > 0.0f && totalCooling == 0) {
                    hullHeat += unabsorbedUnvented;
                }
                markDirty();
            }
            
            // 7. Zap nearby players if efficiency is 40% or less and transferring energy
            if (transferred > 0.0 && efficiency <= 0.40f) {
                if (worldObj.rand.nextInt(20) == 0) { // Once per second on average
                    double range = 4.0D;
                    net.minecraft.util.AxisAlignedBB aabb = net.minecraft.util.AxisAlignedBB.getBoundingBox(
                        xCoord - range, yCoord - range, zCoord - range,
                        xCoord + range + 1, yCoord + range + 1, zCoord + range + 1
                    );
                    @SuppressWarnings("unchecked")
                    java.util.List<net.minecraft.entity.player.EntityPlayer> players = 
                        worldObj.getEntitiesWithinAABB(net.minecraft.entity.player.EntityPlayer.class, aabb);
                    
                    for (net.minecraft.entity.player.EntityPlayer player : players) {
                        double distSq = player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D);
                        if (distSq <= range * range) {
                            player.attackEntityFrom(net.minecraft.util.DamageSource.magic, 2.0F);
                            worldObj.playSoundEffect(player.posX, player.posY, player.posZ, "random.fizz", 1.0F, 0.6F + worldObj.rand.nextFloat() * 0.3F);
                            // Spawn electric spark particles (potion effect ID 8198: swiftness/light blue)
                            worldObj.playAuxSFX(2002, (int)Math.round(player.posX), (int)Math.round(player.posY), (int)Math.round(player.posZ), 8198);
                        }
                    }
                }
            }

            // Check if hull has overheated and explodes
            if (hullHeat >= 10000.0) {
                if (Config.explodeOnOverheat) {
                    worldObj.createExplosion(null, xCoord, yCoord, zCoord, 2.0F, true);
                }
                worldObj.setBlockToAir(xCoord, yCoord, zCoord);
                return; // Machine is destroyed, stop processing!
            }
        }

        // Reset limits once per tick
        this.energyInjectedThisTick = 0.0;
        this.energyDrawnThisTick = 0.0;

        if (!worldObj.isRemote && !addedToEnergyNet) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
            addedToEnergyNet = true;
            TesseractNetwork.register(this);
        }
    }

    @Override
    public void invalidate() {
        if (!worldObj.isRemote) {
            if (addedToEnergyNet) {
                MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
                addedToEnergyNet = false;
            }
            TesseractNetwork.unregister(this);
        }
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        if (!worldObj.isRemote) {
            if (addedToEnergyNet) {
                MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
                addedToEnergyNet = false;
            }
            TesseractNetwork.unregister(this);
        }
        super.onChunkUnload();
    }

    public ForgeDirection getFacingDirection() {
        if (worldObj == null) return ForgeDirection.NORTH;
        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        if (meta >= 2 && meta <= 5) {
            return ForgeDirection.getOrientation(meta);
        }
        return ForgeDirection.NORTH;
    }

    // IEnergySink
    @Override
    public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction) {
        if (mode == 1) return false;
        if (mode == 2) {
            return direction != getFacingDirection().getOpposite();
        }
        return true;
    }

    @Override
    public double getDemandedEnergy() {
        if (mode == 1) return 0.0;
        
        double maxVoltage = getMaxVoltage();
        double remainingLimit = maxVoltage - energyInjectedThisTick;
        if (remainingLimit <= 0) return 0.0;

        double max = TesseractNetwork.getMaxEnergy(channelStr);
        double stored = TesseractNetwork.getStoredEnergy(channelStr);
        double needed = max - stored;
        if (needed <= 0) return 0.0;

        double efficiency = getEfficiency();
        double demanded = needed / efficiency;

        // Prevent energy waste: only demand energy if the network can accept at least a full packet
        if (demanded < maxVoltage) {
            return 0.0;
        }

        return Math.min(remainingLimit, demanded);
    }

    @Override
    public double injectEnergy(ForgeDirection directionFrom, double amount, double voltage) {
        if (mode == 1) return amount;

        double maxVoltage = getMaxVoltage();
        if (voltage > maxVoltage) {
            // Explode on overvoltage!
            if (!worldObj.isRemote) {
                worldObj.createExplosion(null, xCoord, yCoord, zCoord, 2.0F, true);
                worldObj.setBlockToAir(xCoord, yCoord, zCoord);
            }
            return amount;
        }

        double remainingLimit = maxVoltage - energyInjectedThisTick;
        if (remainingLimit <= 0) return amount;

        double toInject = Math.min(amount, remainingLimit);
        double efficiency = getEfficiency();
        double actualToInject = toInject * efficiency;

        double rejected = TesseractNetwork.injectEnergy(channelStr, actualToInject, voltage);
        double actualInjected = actualToInject - rejected;

        double injectedRaw = actualInjected / efficiency;
        energyInjectedThisTick += injectedRaw;

        return amount - injectedRaw;
    }

    @Override
    public int getSinkTier() {
        return getTier();
    }

    // IEnergySource
    @Override
    public double getOfferedEnergy() {
        if (mode == 0) return 0.0;
        
        double maxVoltage = getMaxVoltage();
        double remainingLimit = maxVoltage - energyDrawnThisTick;
        if (remainingLimit <= 0) return 0.0;

        double networkStored = TesseractNetwork.getStoredEnergy(channelStr);
        double efficiency = getEfficiency();
        double offeredFromNetwork = networkStored * efficiency;
        
        return Math.min(remainingLimit, offeredFromNetwork);
    }

    @Override
    public void drawEnergy(double amount) {
        if (mode == 0) return;
        
        double maxVoltage = getMaxVoltage();
        double remainingLimit = maxVoltage - energyDrawnThisTick;
        double toDraw = Math.min(amount, remainingLimit);
        
        double efficiency = getEfficiency();
        double networkDraw = toDraw / efficiency;
        
        TesseractNetwork.drawEnergy(channelStr, networkDraw);
        energyDrawnThisTick += toDraw;
    }

    @Override
    public int getSourceTier() {
        return getTier();
    }

    @Override
    public boolean emitsEnergyTo(TileEntity receiver, ForgeDirection direction) {
        if (mode == 0) return false;
        if (mode == 2) {
            return direction == getFacingDirection().getOpposite();
        }
        return true;
    }

    // IInventory implementation
    @Override
    public int getSizeInventory() {
        return 9;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventory[index];
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (inventory[index] != null) {
            ItemStack stack;
            if (inventory[index].stackSize <= count) {
                stack = inventory[index];
                inventory[index] = null;
                markDirty();
                TesseractNetwork.redistributeEnergy(channelStr);
                return stack;
            } else {
                stack = inventory[index].splitStack(count);
                if (inventory[index].stackSize == 0) {
                    inventory[index] = null;
                }
                markDirty();
                TesseractNetwork.redistributeEnergy(channelStr);
                return stack;
            }
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        if (inventory[index] != null) {
            ItemStack stack = inventory[index];
            inventory[index] = null;
            TesseractNetwork.redistributeEnergy(channelStr);
            return stack;
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventory[index] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
        if (index >= 6 && index < 9) {
            coolantDamageAccumulator[index - 6] = 0.0;
        }
        markDirty();
        TesseractNetwork.redistributeEnergy(channelStr);
    }

    @Override
    public String getInventoryName() {
        return "container.tesseract";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this && player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (stack == null) return false;
        if (index < 3) {
            return stack.getItem() instanceof IElectricItem;
        } else if (index < 6) {
            return isTransformerUpgrade(stack);
        } else {
            return getActiveCoolingCellCapacity(stack) > 0.0 ||
                   getVentCoolingValue(stack) > 0.0 ||
                   isDepletedCoolingCellOrCondensator(stack);
        }
    }

    // NBT saving/loading
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("channelStr")) {
            this.channelStr = tag.getString("channelStr");
            if (this.channelStr.contains("#")) {
                this.channel = -1;
            } else {
                try {
                    this.channel = Integer.parseInt(this.channelStr);
                } catch (NumberFormatException e) {
                    this.channel = 0;
                }
            }
        } else {
            this.channel = Math.min(63, Math.max(0, tag.getInteger("channel")));
            this.channelStr = String.valueOf(this.channel);
        }
        this.mode = 2; // Forced to 2 (BOTH) to follow IC2 standards
        this.hullHeat = tag.getDouble("hullHeat");
        
        NBTTagList list = tag.getTagList("Items", 10);
        this.inventory = new ItemStack[9];
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound compound = list.getCompoundTagAt(i);
            int slot = compound.getByte("Slot") & 255;
            if (slot >= 0 && slot < this.inventory.length) {
                this.inventory[slot] = ItemStack.loadItemStackFromNBT(compound);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("channel", this.channel);
        tag.setString("channelStr", this.channelStr);
        tag.setInteger("mode", this.mode);
        tag.setDouble("hullHeat", this.hullHeat);

        NBTTagList list = new NBTTagList();
        for (int i = 0; i < this.inventory.length; i++) {
            if (this.inventory[i] != null) {
                NBTTagCompound compound = new NBTTagCompound();
                compound.setByte("Slot", (byte) i);
                this.inventory[i].writeToNBT(compound);
                list.appendTag(compound);
            }
        }
        tag.setTag("Items", list);
    }

    // IWrenchable implementation
    @Override
    public boolean wrenchCanSetFacing(EntityPlayer player, int side) {
        return side >= 2 && side <= 5;
    }

    @Override
    public short getFacing() {
        if (worldObj == null) return 2; // Default NORTH
        return (short) worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
    }

    @Override
    public void setFacing(short facing) {
        if (worldObj != null && facing >= 2 && facing <= 5) {
            worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, facing, 3);
            
            // Re-register with IC2 EnergyNet to refresh grid connections
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
        }
    }

    @Override
    public boolean wrenchCanRemove(EntityPlayer player) {
        return true;
    }

    @Override
    public float getWrenchDropRate() {
        return 1.0F;
    }

    @Override
    public ItemStack getWrenchDrop(EntityPlayer player) {
        return new ItemStack(EUExpansion.tesseract);
    }
}
