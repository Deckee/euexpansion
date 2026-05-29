package com.watsonad2000.euexpansion;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.item.ItemStack;

public class TesseractNetwork {

    private static final Map<Integer, List<TileEntityTesseract>> channels = new HashMap<>();

    public static synchronized void register(TileEntityTesseract tile) {
        int channel = tile.channel;
        List<TileEntityTesseract> list = channels.computeIfAbsent(channel, k -> new ArrayList<>());
        if (!list.contains(tile)) {
            list.add(tile);
        }
        redistributeEnergy(channel);
    }

    public static synchronized void unregister(TileEntityTesseract tile) {
        int channel = tile.channel;
        List<TileEntityTesseract> list = channels.get(channel);
        if (list != null) {
            list.remove(tile);
            if (list.isEmpty()) {
                channels.remove(channel);
            }
        }
        redistributeEnergy(channel);
    }

    public static synchronized void changeChannel(TileEntityTesseract tile, int oldChannel, int newChannel) {
        List<TileEntityTesseract> oldList = channels.get(oldChannel);
        if (oldList != null) {
            oldList.remove(tile);
            if (oldList.isEmpty()) {
                channels.remove(oldChannel);
            }
        }
        List<TileEntityTesseract> newList = channels.computeIfAbsent(newChannel, k -> new ArrayList<>());
        if (!newList.contains(tile)) {
            newList.add(tile);
        }
        redistributeEnergy(oldChannel);
        redistributeEnergy(newChannel);
    }

    public static synchronized double getStoredEnergy(int channel) {
        List<TileEntityTesseract> list = channels.get(channel);
        if (list == null) return 0.0;
        double total = 0.0;
        for (TileEntityTesseract tile : list) {
            for (int i = 0; i < 3; i++) {
                ItemStack stack = tile.inventory[i];
                if (stack != null && stack.getItem() instanceof IElectricItem) {
                    total += ElectricItem.manager.getCharge(stack);
                }
            }
        }
        return total;
    }

    public static synchronized double getMaxEnergy(int channel) {
        List<TileEntityTesseract> list = channels.get(channel);
        if (list == null) return 0.0;
        double total = 0.0;
        for (TileEntityTesseract tile : list) {
            for (int i = 0; i < 3; i++) {
                ItemStack stack = tile.inventory[i];
                if (stack != null && stack.getItem() instanceof IElectricItem) {
                    total += ((IElectricItem) stack.getItem()).getMaxCharge(stack);
                }
            }
        }
        return total;
    }

    public static synchronized double injectEnergy(int channel, double amount, double voltage) {
        List<TileEntityTesseract> list = channels.get(channel);
        if (list == null || amount <= 0) return amount;

        double remaining = amount;
        for (TileEntityTesseract tile : list) {
            for (int i = 0; i < 3; i++) {
                ItemStack stack = tile.inventory[i];
                if (stack != null && stack.getItem() instanceof IElectricItem) {
                    int tier = ((IElectricItem) stack.getItem()).getTier(stack);
                    double maxCharge = ((IElectricItem) stack.getItem()).getMaxCharge(stack);
                    double currentCharge = ElectricItem.manager.getCharge(stack);
                    double needed = maxCharge - currentCharge;
                    if (needed > 0) {
                        double toCharge = Math.min(remaining, needed);
                        double charged = ElectricItem.manager.charge(stack, toCharge, tier, true, false);
                        remaining -= charged;
                        if (remaining <= 0) {
                            tile.markDirty();
                            return 0.0;
                        }
                    }
                }
            }
            tile.markDirty();
        }
        return remaining;
    }

    public static synchronized double drawEnergy(int channel, double amount) {
        List<TileEntityTesseract> list = channels.get(channel);
        if (list == null || amount <= 0) return 0.0;

        double remaining = amount;
        for (TileEntityTesseract tile : list) {
            for (int i = 0; i < 3; i++) {
                ItemStack stack = tile.inventory[i];
                if (stack != null && stack.getItem() instanceof IElectricItem) {
                    int tier = ((IElectricItem) stack.getItem()).getTier(stack);
                    double currentCharge = ElectricItem.manager.getCharge(stack);
                    double toDraw = Math.min(remaining, currentCharge);
                    double discharged = ElectricItem.manager.discharge(stack, toDraw, tier, true, false, false);
                    remaining -= discharged;
                    if (remaining <= 0) {
                        tile.markDirty();
                        return amount;
                    }
                }
            }
            tile.markDirty();
        }
        return amount - remaining;
    }

    public static synchronized void redistributeEnergy(int channel) {
        List<TileEntityTesseract> list = channels.get(channel);
        if (list == null) return;

        double totalStored = 0.0;
        double totalMax = 0.0;

        for (TileEntityTesseract tile : list) {
            for (int i = 0; i < 3; i++) {
                ItemStack stack = tile.inventory[i];
                if (stack != null && stack.getItem() instanceof IElectricItem) {
                    totalStored += ElectricItem.manager.getCharge(stack);
                    totalMax += ((IElectricItem) stack.getItem()).getMaxCharge(stack);
                }
            }
        }

        if (totalMax <= 0) {
            return;
        }

        double fillRatio = totalStored / totalMax;

        for (TileEntityTesseract tile : list) {
            boolean dirty = false;
            for (int i = 0; i < 3; i++) {
                ItemStack stack = tile.inventory[i];
                if (stack != null && stack.getItem() instanceof IElectricItem) {
                    int tier = ((IElectricItem) stack.getItem()).getTier(stack);
                    double maxCharge = ((IElectricItem) stack.getItem()).getMaxCharge(stack);
                    double targetCharge = fillRatio * maxCharge;
                    
                    double current = ElectricItem.manager.getCharge(stack);
                    double diff = targetCharge - current;
                    if (diff > 0) {
                        ElectricItem.manager.charge(stack, diff, tier, true, false);
                        dirty = true;
                    } else if (diff < 0) {
                        ElectricItem.manager.discharge(stack, -diff, tier, true, true, false);
                        dirty = true;
                    }
                }
            }
            if (dirty) {
                tile.markDirty();
            }
        }
    }

    public static synchronized void clear() {
        channels.clear();
    }

    public static synchronized double getNetworkEfficiency(int channel) {
        List<TileEntityTesseract> list = channels.get(channel);
        if (list == null || list.isEmpty()) return 0.30;
        
        double totalIO = 0.0;
        double weightedEfficiencySum = 0.0;
        
        for (TileEntityTesseract tile : list) {
            double io = tile.lastTickIO;
            double eff = tile.getEfficiency();
            weightedEfficiencySum += eff * io;
            totalIO += io;
        }
        
        if (totalIO > 0.0) {
            return weightedEfficiencySum / totalIO;
        } else {
            double sum = 0.0;
            for (TileEntityTesseract tile : list) {
                sum += tile.getEfficiency();
            }
            return sum / list.size();
        }
    }
}
