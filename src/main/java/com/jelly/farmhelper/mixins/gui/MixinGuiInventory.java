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
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiInventory.class)
public abstract class MixinGuiInventory extends MixinInventoryEffectRenderer {

    /**
     * @author ignoreCompilationWarning
     * @reason ignoreCompilationWarning
     */
    @Final
    @Overwrite
    public void initGui() {
        LogUtils.sendShenanigans("WOW HE OBENING DA INVENDORY");

    }


}
