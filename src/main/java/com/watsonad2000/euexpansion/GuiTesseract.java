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

    private static final ResourceLocation texture = new ResourceLocation("textures/gui/container/dispenser.png");
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
        this.buttonList.add(new GuiButton(0, this.guiLeft + 122, this.guiTop + 14, 20, 20, "-"));
        this.buttonList.add(new GuiButton(1, this.guiLeft + 147, this.guiTop + 14, 20, 20, "+"));
        this.buttonList.add(new GuiButton(2, this.guiLeft + 122, this.guiTop + 36, 20, 20, "<<"));
        this.buttonList.add(new GuiButton(3, this.guiLeft + 147, this.guiTop + 36, 20, 20, ">>"));
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
        String channelText = "CH: " + Math.min(63, Math.max(0, this.tileTesseract.channel));
        this.fontRendererObj.drawString(channelText, 8, 20, 4210752);

        // Draw energy text
        String energyText = "EU: " + (int) this.tileTesseract.storedEnergy;
        String maxText = "/ " + (int) this.tileTesseract.maxEnergy;

        GL11.glPushMatrix();
        GL11.glScalef(0.75f, 0.75f, 1.0f);
        // Math Correction: Target X=8, Y=36 -> Divided by 0.75 = X=11, Y=48
        this.fontRendererObj.drawString(energyText, 11, 48, 4210752);
        // Math Correction: Target X=8, Y=46 -> Divided by 0.75 = X=11, Y=61
        this.fontRendererObj.drawString(maxText, 11, 61, 4210752);
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
    }
}
