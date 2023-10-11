package com.jelly.farmhelper.features;

import com.jelly.farmhelper.FarmHelper;
import com.jelly.farmhelper.hud.DebugHUD;
import com.jelly.farmhelper.macros.MacroHandler;
import com.jelly.farmhelper.remote.command.commands.ReconnectCommand;
import com.jelly.farmhelper.utils.Clock;
import com.jelly.farmhelper.utils.LocationUtils;
import com.jelly.farmhelper.utils.LogUtils;
import com.jelly.farmhelper.utils.Utils;
import com.jelly.farmhelper.world.GameState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.concurrent.TimeUnit;

public class Scheduler {

    private static final Minecraft mc = Minecraft.getMinecraft();
    public static Clock farmClock = new Clock();
    public static Clock breakClock = new Clock();
    private static State currentState;

    enum State {
        FARMING,
        BREAK
    }

    public static boolean isFarming() {
        return !FarmHelper.config.enableScheduler || currentState == State.FARMING;
    }
    public static boolean isOnBreak() { return !FarmHelper.config.enableScheduler || currentState == State.BREAK; }
    public static String getStatusString() {
        if (FarmHelper.config.enableScheduler) {
            return (currentState == State.FARMING ? "Farming" : "Break") + " for "
                + Utils.formatTime(currentState == State.FARMING ? Math.max(farmClock.getRemainingTime(), 0) : Math.max(breakClock.getRemainingTime(), 0)) + (farmClock.isPaused() ? " (Paused)" : "");
        } else {
            return "Farming";
        }
    }

    public static void start() {
        currentState = State.FARMING;
        farmClock.schedule(TimeUnit.MINUTES.toMillis(FarmHelper.config.schedulerFarmingTime));
        if (FarmHelper.config.pauseSchedulerDuringJacobsContest && GameState.inJacobContest()) {
            farmClock.pause();
            breakClock.pause();
        }
    }

    public static void pause() {
        LogUtils.sendDebug("[Scheduler] Pausing");
        farmClock.pause();
        breakClock.pause();
    }

    public static void resume() {
        LogUtils.sendDebug("[Scheduler] Resuming");
        farmClock.resume();
        breakClock.resume();
    }

    @SubscribeEvent
    public final void tick(TickEvent.ClientTickEvent event) {

        if (!FarmHelper.config.enableScheduler || event.phase == TickEvent.Phase.END  || FarmHelper.tickCount % 5 != 0 || MacroHandler.currentMacro == null)
            return;

        if (!MacroHandler.randomizing && MacroHandler.isMacroing && MacroHandler.currentMacro.enabled && currentState == State.FARMING && !farmClock.isPaused() && farmClock.passed()) {

            LogUtils.sendDebug("[Scheduler] Farming time has passed, stopping");
            MacroHandler.disableCurrentMacro(true);
            currentState = State.BREAK;
            breakClock.schedule(TimeUnit.MINUTES.toMillis((long)(FarmHelper.config.schedulerBreakTime + (Math.random() * FarmHelper.config.schedulerBreakTimeRandomness))));
            if (FarmHelper.config.disconnectDuringSchedulerBreak)
                disconnectPlayerWithMessage("Disconnected due to scheduler break. Time till reconnect: " + Utils.formatTime(breakClock.getRemainingTime()));

        } else if (!MacroHandler.randomizing && MacroHandler.isMacroing && currentState == State.BREAK && !breakClock.isPaused() && breakClock.passed()) {
            LogUtils.sendDebug("[Scheduler] Break time has passed, starting");
            currentState = State.FARMING;

            if (FarmHelper.config.disconnectDuringSchedulerBreak)
                new Thread(() -> {
                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (LocationUtils.currentIsland == LocationUtils.Island.LOBBY) {
                        mc.thePlayer.sendChatMessage("/skyblock");
                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if(LocationUtils.currentIsland != LocationUtils.Island.GARDEN) {
                        mc.thePlayer.sendChatMessage("/warp garden");
                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }).start();

            farmClock.schedule(TimeUnit.MINUTES.toMillis((long)(FarmHelper.config.schedulerFarmingTime + (Math.random() * FarmHelper.config.schedulerFarmingTimeRandomness))));
        }
        DebugHUD.farmClockremainingTime = farmClock.getRemainingTime();
        DebugHUD.farmClockisPaused = farmClock.isPaused();
        DebugHUD.breakClockremainingTime = breakClock.getRemainingTime();
        DebugHUD.breakClockisPaused = breakClock.isPaused();
        if (currentState != null) {
            LogUtils.sendDebug("[Scheduler] Current state: " + currentState);
        }
    }
    public void disconnectPlayerWithMessage(String message) {
        IChatComponent disconnectReason = new ChatComponentText(message);
        Minecraft.getMinecraft().getNetHandler().getNetworkManager().closeChannel(disconnectReason);
    }
}
