package com.jelly.farmhelper.features;


import com.jelly.farmhelper.FarmHelper;
import com.jelly.farmhelper.config.Config;
import com.jelly.farmhelper.config.structs.Rewarp;
import com.jelly.farmhelper.utils.Clock;
import com.jelly.farmhelper.utils.KeyBindUtils;
import com.jelly.farmhelper.utils.LogUtils;
import com.jelly.farmhelper.utils.PlayerUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class RewarpWhenNotMacroing {
    public boolean isTping = false;
    public BlockPos beforeTeleportationPos = null;
    public static final Minecraft mc = Minecraft.getMinecraft();
    @Getter
    static boolean enabled = false;
    public boolean isRewarpLocationSet() {
        return !Config.rewarpList.isEmpty();
    }

    public final Clock lastTp = new Clock();

    public int layerY = 0;
    private void checkForTeleport() {
        if (beforeTeleportationPos == null) return;
        if (mc.thePlayer.getPosition().distanceSq(beforeTeleportationPos) > 2) {
            LogUtils.sendDebug("Teleported!");


            beforeTeleportationPos = null;
            isTping = false;


        }
    }
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!enabled && FarmHelper.config.rewarpWhenNotMacroing) {
            startMacro();
            return;
        } else if (enabled && !FarmHelper.config.rewarpWhenNotMacroing) {
            stopMacro();
            return;
        }
        if (!FarmHelper.config.rewarpWhenNotMacroing) return;
        if (!enabled) return;
        if (!isRewarpLocationSet()) return;
        checkForTeleport();
        if (isStandingOnRewarpLocation() && !FailsafeNew.emergency) {
            triggerWarpGarden();
            return;
        }

    }

    public boolean isStandingOnRewarpLocation() {
        if (Config.rewarpList.isEmpty()) return false;
        Rewarp closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (Rewarp rewarp : Config.rewarpList) {
            double distance = rewarp.getDistance(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
            if (distance < closestDistance) {
                closest = rewarp;
                closestDistance = distance;
            }
        }
        if (closest == null) return false;
        Vec3 playerPos = mc.thePlayer.getPositionVector();
        Vec3 rewarpPos = new Vec3(closest.getX() + 0.5, closest.getY() + 0.5, closest.getZ() + 0.5);
        return playerPos.distanceTo(rewarpPos) <= FarmHelper.config.rewarpMaxDistance;
    }
    public void triggerWarpGarden() {
        triggerWarpGarden(false);
    }
    public void triggerWarpGarden(boolean force) {
        KeyBindUtils.stopMovement();
        isTping = true;
        if (force || FarmHelper.gameState.canChangeDirection() && beforeTeleportationPos == null) {
            LogUtils.sendDebug("Warping to spawn point");
            mc.thePlayer.sendChatMessage("/warp garden");
            beforeTeleportationPos = mc.thePlayer.getPosition();
        }
    }
    public void startMacro() {
        isTping = false;
        beforeTeleportationPos = null;
        enabled = true;
    }

    public void stopMacro() {
        enabled = false;
        beforeTeleportationPos = null;
        isTping = false;
    }
    public void reset() {

        enabled = false;
        isTping = false;
        beforeTeleportationPos = null;
    }
}
