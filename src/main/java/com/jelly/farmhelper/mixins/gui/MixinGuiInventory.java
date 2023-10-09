package com.jelly.farmhelper.mixins.gui;

import com.jelly.farmhelper.mixins.render.MixinInventoryEffectRenderer;
import com.jelly.farmhelper.utils.LogUtils;
import jline.internal.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GuiInventory.class)
public abstract class MixinGuiInventory extends MixinInventoryEffectRenderer {
    private final Minecraft mc = Minecraft.getMinecraft();

    /**
     * @author ignoreCompilationWarning
     * @reason ignoreCompilationWarning
     */
    @Final
    @Overwrite
    public void initGui() {
        LogUtils.sendShenanigans("WOW HE OBENING DA INVENDORY");
    }
    /**
     * @author ignoreCompilationWarning
     * @reason ignoreCompilationWarning
     */
    @Final
    @Overwrite
    public void updateScreen() {
        try {
            if (this.mc.theWorld == null || this.mc.thePlayer == null || this.mc.playerController == null) return;
            if (this.mc.playerController.isInCreativeMode()) {
                this.mc.displayGuiScreen(new GuiContainerCreative(this.mc.thePlayer));
            }
            this.updateActivePotionEffects();
        } catch (Exception e) {
            e.printStackTrace();
            updateScreen();
        }

    }

    /*
    @Final
    @Overwrite
    public static void drawEntityOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, EntityLivingBase ent) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, posY, 50.0f);
        GlStateManager.scale(-scale, scale, scale);
        GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
        float f = ent.renderYawOffset;
        float g = ent.rotationYaw;
        float h = ent.rotationPitch;
        float i = ent.prevRotationYawHead;
        float j = ent.rotationYawHead;
        GlStateManager.rotate(135.0f, 0.0f, 1.0f, 0.0f);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(-((float)Math.atan(mouseY / 40.0f)) * 20.0f, 1.0f, 0.0f, 0.0f);
        ent.renderYawOffset = (float)Math.atan(mouseX / 40.0f) * 20.0f;
        ent.rotationYaw = (float)Math.atan(mouseX / 40.0f) * 40.0f;
        ent.rotationPitch = -((float)Math.atan(mouseY / 40.0f)) * 20.0f;
        ent.rotationYawHead = ent.rotationYaw;
        ent.prevRotationYawHead = ent.rotationYaw;
        GlStateManager.translate(0.0f, 0.0f, 0.0f);
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        renderManager.setPlayerViewY(180.0f);
        renderManager.setRenderShadow(false);
        renderManager.renderEntityWithPosYaw(ent, 0.0, 0.0, 0.0, 0.0f, 1.0f);
        renderManager.setRenderShadow(true);
        ent.renderYawOffset = f;
        ent.rotationYaw = g;
        ent.rotationPitch = h;
        ent.prevRotationYawHead = i;
        ent.rotationYawHead = j;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    } */
}
