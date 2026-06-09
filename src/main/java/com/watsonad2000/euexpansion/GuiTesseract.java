package com.watsonad2000.euexpansion;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiTesseract extends GuiContainer {

    private static final ResourceLocation texture = new ResourceLocation(EUExpansion.MODID, "textures/gui/tesseract_gui.png");
    private final TileEntityTesseract tileTesseract;

    public GuiTesseract(InventoryPlayer playerInventory, TileEntityTesseract tileTesseract) {
        super(new ContainerTesseract(playerInventory, tileTesseract));
        this.tileTesseract = tileTesseract;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.add(new GuiButton(0, this.guiLeft + 125, this.guiTop + 14, 18, 20, "-"));
        this.buttonList.add(new GuiButton(1, this.guiLeft + 147, this.guiTop + 14, 18, 20, "+"));
        this.buttonList.add(new GuiButton(2, this.guiLeft + 125, this.guiTop + 36, 18, 20, "<<"));
        this.buttonList.add(new GuiButton(3, this.guiLeft + 147, this.guiTop + 36, 18, 20, ">>"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        this.mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, button.id);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // Draw title (Centered perfectly)
        String s = "Tesseract";
        this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 6, 4210752);

        // Draw channel text
        String channelText;
        if (this.tileTesseract.channel == -1) {
            channelText = "CH: PRV";
        } else {
            channelText = "CH: " + Math.min(63, Math.max(0, this.tileTesseract.channel));
        }
        this.fontRendererObj.drawString(channelText, 12, 20, 4210752);

        // Draw energy text
        String energyText = "EU: " + (int) this.tileTesseract.storedEnergy;
        String maxText = "/ " + (int) this.tileTesseract.maxEnergy;

        GL11.glPushMatrix();
        GL11.glScalef(0.75f, 0.75f, 1.0f);
        // Math Correction: Target X=12, Y=36 -> Divided by 0.75 = X=16, Y=48
        this.fontRendererObj.drawString(energyText, 16, 48, 4210752);
        // Math Correction: Target X=12, Y=46 -> Divided by 0.75 = X=16, Y=61
        this.fontRendererObj.drawString(maxText, 16, 61, 4210752);
        GL11.glPopMatrix();

        // Draw efficiency text
        String effText = "Eff: " + (int) Math.round(this.tileTesseract.clientEfficiency * 100) + "%";
        this.fontRendererObj.drawString(effText, 122, 60, 4210752);

        // Draw heat status text (Shifted left from 104 to 86 to give the limit text breathing room)
        String heatText = "Heat: " + (int) this.tileTesseract.clientHullHeat + " / 10000";
        this.fontRendererObj.drawString(heatText, 86, 72, 4210752);

        // Bypassing the calculation with the standard Dropper/Dispenser vertical alignment
        this.fontRendererObj.drawString("Inventory", 8, 74, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(texture);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);

        // Draw custom modern energy bar (horizontal battery style)
        int barX = k + 12;
        int barY = l + 54;
        int barWidth = 40;
        int barHeight = 8;

        // Draw border (dark grey)
        this.drawGradientRect(barX, barY, barX + barWidth, barY + barHeight, 0xFF555555, 0xFF555555);
        // Draw background (very dark grey/black)
        this.drawGradientRect(barX + 1, barY + 1, barX + barWidth - 1, barY + barHeight - 1, 0xFF1E1E1E, 0xFF1E1E1E);

        // Calculate and draw filled portion
        double pct = this.tileTesseract.maxEnergy > 0.0 ? this.tileTesseract.storedEnergy / this.tileTesseract.maxEnergy : 0.0;
        pct = Math.min(1.0, Math.max(0.0, pct));
        int filledWidth = (int) (pct * (barWidth - 2));
        if (filledWidth > 0) {
            // Shaded red-to-dark-red gradient to match IC2 battery colors
            this.drawGradientRect(barX + 1, barY + 1, barX + 1 + filledWidth, barY + barHeight - 1, 0xFFFF3333, 0xFF990000);
        }
    }
}
