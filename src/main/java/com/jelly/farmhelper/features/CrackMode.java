package com.jelly.farmhelper.features;

import com.jelly.farmhelper.FarmHelper;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;


public class CrackMode {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static boolean enabled;
    private static boolean isOn = true;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (FarmHelper.config.crackMode) {
            if (!isOn) return;
            mc.gameSettings.fovSetting = 240f;
            isOn = false;
        } else if (isOn && !FarmHelper.config.crackMode) {
            mc.gameSettings.fovSetting = FarmHelper.getOldFov();
        }

    }

}
