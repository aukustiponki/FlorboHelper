package com.jelly.farmhelper.features;

import com.jelly.farmhelper.FarmHelper;
import com.jelly.farmhelper.macros.MacroHandler;
import com.jelly.farmhelper.player.Rotation;
import com.jelly.farmhelper.utils.*;
import com.jelly.farmhelper.world.GameState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.inventory.Slot;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class AutoCookie {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static boolean enabled;
    private static int hoeSlot;
    private static final Clock buyWait = new Clock();
    private static final Rotation rotation = new Rotation();
    private static int cookieSlot;
    public static int failCount;
    private static final Clock cooldown = new Clock();
    private static final Clock timeout = new Clock();

    private static State currentState;
    enum State {
        WALKING,
        BUYING,
        BOUGHT
    }

    private static PickupState pickupState;
    enum PickupState {
        LIFT,
        SWAP,
        DROP
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void enable() {
        LogUtils.sendDebug("[AutoCookie] Going to buy cookie");
        mc.thePlayer.sendChatMessage("/hub");
        hoeSlot = mc.thePlayer.inventory.currentItem;
        currentState = State.WALKING;
        pickupState = PickupState.LIFT;
        timeout.schedule(20000);
        enabled = true;
    }

    public static void disable() {
        LogUtils.sendDebug("[AutoCookie] Finished auto cookie");
        mc.thePlayer.closeScreen();
        mc.thePlayer.inventory.currentItem = hoeSlot;
        KeyBindUtils.stopMovement();
        enabled = false;
        if (cooldown.passed()) {
            cooldown.schedule(1000);
        }
    }

    @SubscribeEvent
    public final void tick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END || mc.thePlayer == null || mc.theWorld == null || FarmHelper.tickCount % 5 != 0)
            return;

        if (!enabled && MacroHandler.isMacroing && MacroHandler.currentMacro.enabled && cooldown.passed() && FarmHelper.config.autoCookie && FarmHelper.gameState.cookie == GameState.EffectState.OFF) {
            LogUtils.sendDebug("[AutoCookie] Detected no cookie buff");
            MacroHandler.disableCurrentMacro();
            enable();
        }
        if (!enabled) return;

        KeyBindUtils.stopMovement();

        if (timeout.passed()) {
            LogUtils.sendDebug("[AutoCookie] Timed out, exiting");
            cooldown.schedule(5000);
            failCount++;
            if (failCount == 3) {
                LogUtils.sendDebug("[AutoCookie] Disabling auto cookie, too many fails");
                FarmHelper.config.autoCookie = false;
            }
            disable();
            return;
        }

        if (LocationUtils.currentIsland != LocationUtils.Island.THE_HUB) {
            LogUtils.sendDebug("[AutoCookie] Waiting for hub");
            return;
        }

        if (rotation.rotating) {
            LogUtils.sendDebug("[AutoCookie] Waiting for rotate");
            return;
        }

        if (!buyWait.passed()) {
            LogUtils.sendDebug("[AutoCookie] Waiting after purchase");
            for (Slot item : PlayerUtils.getInventorySlots()) {
                if (item.getStack().getDisplayName().contains("Booster Cookie") && currentState == State.BUYING) {
                    LogUtils.sendDebug("[AutoCookie] Detected cookie purchase");
                    currentState = State.BOUGHT;
                    pickupState = PickupState.LIFT;
                }
            }
            return;
        }

        switch (currentState) {
            case WALKING:
                // Target: -33, -77, Yaw: 103
                if (mc.currentScreen != null) {
                    LogUtils.sendDebug("[AutoCookie] Closing GUI");
                    mc.thePlayer.closeScreen();
                } else if (distanceToBazaar() < 3) {
                    LogUtils.sendDebug("[AutoCookie] Close to bazaar");
                    currentState = State.BUYING;
                } else if (AngleUtils.smallestAngleDifference(AngleUtils.get360RotationYaw(), 103) > 2) {
                    LogUtils.sendDebug("[AutoCookie] Rotating to bazaar");
                    rotation.easeTo(103, mc.thePlayer.rotationPitch, 1000);
                } else {
                    LogUtils.sendDebug("[AutoCookie] Walking to bazaar");
                    KeyBindUtils.updateKeys(true, false, false, false, false);
                }
                break;
            case BUYING:
                if (mc.currentScreen == null) {
                    LogUtils.sendDebug("[AutoCookie] Attempting to open bazaar");
                    final Entity bazaarNPC = getBazaarNPC();
                    if (bazaarNPC == null) {
                        LogUtils.sendDebug("[AutoCookie] Cannot find bazaar NPC, retrying");
                    } else {
                        LogUtils.sendDebug("[AutoCookie] Sending interact packet");
                        mc.playerController.interactWithEntitySendPacket(mc.thePlayer, bazaarNPC);
                    }
                } else if (PlayerUtils.inventoryNameContains("Bazaar") && PlayerUtils.inventoryNameContains("Oddities")) {
                    LogUtils.sendDebug("[AutoCookie] Found oddities menu, opening cookie menu");
                    PlayerUtils.clickOpenContainerSlot(11);
                } else if (PlayerUtils.inventoryNameContains("Bazaar")) {
                    LogUtils.sendDebug("[AutoCookie] Found bazaar menu, opening oddities");
                    PlayerUtils.clickOpenContainerSlot(36);
                } else if (PlayerUtils.inventoryNameContains("Oddities") && PlayerUtils.inventoryNameContains("Booster Cookie")) {
                    LogUtils.sendDebug("[AutoCookie] Found booster cookie menu, opening instant buy");
                    PlayerUtils.clickOpenContainerSlot(10);
                } else if (PlayerUtils.inventoryNameContains("Oddities")) {
                    LogUtils.sendDebug("[AutoCookie] Found unknown oddity menu, closing");
                    mc.thePlayer.closeScreen();
                } else if (PlayerUtils.inventoryNameContains("Booster Cookie") && PlayerUtils.inventoryNameContains("Instant Buy")) {
                    LogUtils.sendDebug("[AutoCookie] Found instant buy menu, buying");
                    PlayerUtils.clickOpenContainerSlot(10);
                    buyWait.schedule(2000);
                } else if (PlayerUtils.inventoryNameContains("Booster Cookie")) {
                    LogUtils.sendDebug("[AutoCookie] Found unknown booster cookie menu, closing");
                    mc.thePlayer.closeScreen();
                } else {
                    LogUtils.sendDebug("[AutoCookie] Unknown menu " + PlayerUtils.getInventoryName() + ", closing");
                    mc.thePlayer.closeScreen();
                }
                break;

            case BOUGHT:
                if (mc.currentScreen == null) {
                    if (AngleUtils.smallestAngleDifference(AngleUtils.get360RotationYaw(), 270) > 5) {
                        LogUtils.sendDebug("[AutoCookie] Looking away from bazaar");
                        rotation.easeTo(270, mc.thePlayer.rotationPitch, 1000);
                    } else if (PlayerUtils.getItemInHotbar("Booster Cookie") == 6 && pickupState != PickupState.DROP) {
                        LogUtils.sendDebug("[AutoCookie] Detected booster cookie in hotbar");
                        if (mc.thePlayer.inventory.currentItem == 6) {
                            LogUtils.sendDebug("[AutoCookie] Opening consume cookie menu");
                            KeyBindUtils.rightClick();
                        } else {
                            LogUtils.sendDebug("[AutoCookie] Switching to cookie");
                            mc.thePlayer.inventory.currentItem = 6;
                        }
                    } else if (PlayerUtils.getSlotForItem("Booster Cookie") != -1 || pickupState != PickupState.LIFT) {
                        switch (pickupState) {
                            case LIFT:
                                LogUtils.sendDebug("[AutoCookie] Picking up cookie");
                                cookieSlot = PlayerUtils.getSlotForItem("Booster Cookie");
                                PlayerUtils.clickOpenContainerSlot(cookieSlot);
                                pickupState = PickupState.SWAP;
                                break;

                            case SWAP:
                                LogUtils.sendDebug("[AutoCookie] Swapping cookie with slot 7");
                                PlayerUtils.clickOpenContainerSlot(35 + 7);
                                pickupState = PickupState.DROP;
                                break;

                            case DROP:
                                LogUtils.sendDebug("[AutoCookie] Dropping slot 7 to cookie slot");
                                PlayerUtils.clickOpenContainerSlot(cookieSlot);
                                pickupState = PickupState.LIFT;
                                break;
                        }
                    } else {
                        LogUtils.sendDebug("[AutoCookie] No cookie in inventory, quitting");
                        disable();
                    }
                } else if (PlayerUtils.inventoryNameContains("Consume Booster Cookie?")) {
                    LogUtils.sendDebug("[AutoCookie] Detected consume cookie menu, consuming");
                    PlayerUtils.clickOpenContainerSlot(11);
                } else {
                    LogUtils.sendDebug("[AutoCookie] Unknown menu " + PlayerUtils.getInventoryName() + ", closing");
                    mc.thePlayer.closeScreen();
                }
        }
    }

    @SubscribeEvent
    public void onLastRender(RenderWorldLastEvent event) {
        if (rotation.rotating && enabled) {
            rotation.update();
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onMessageReceived(ClientChatReceivedEvent event) {
        if (!enabled) return;
        if (event.type != 0 || event.message == null) return;
        String message = StringUtils.stripControlCodes(event.message.getUnformattedText());
        if (message.contains("Bazaar!") && message.contains("Bought") && message.contains("Booster Cookie") && currentState == State.BUYING) {
            LogUtils.sendDebug("[AutoCookie] Detected cookie purchase");
            currentState = State.BOUGHT;
            pickupState = PickupState.LIFT;
        }
        if (message.contains("Consumed") && message.contains("Booster Cookie") && currentState == State.BOUGHT) {
            LogUtils.sendDebug("[AutoCookie] Detected cookie consume");
            disable();
        }
        if (message.contains("You cannot afford this!") && currentState == State.BUYING) {
            LogUtils.sendDebug("[AutoCookie] Not enough coins for a cookie");
            cooldown.schedule(3000);
            failCount++;
            if (failCount == 3) {
                LogUtils.sendDebug("[AutoCookie] Disabling auto cookie, too many fails");
                FarmHelper.config.autoCookie = false;
            }
            disable();
        }
        if (message.contains("This server is too laggy") && currentState == State.BUYING) {
            LogUtils.sendDebug("[AutoCookie] Server too laggy for bazaar");
            disable();
        }
    }

    @SubscribeEvent
    public void OnKeyPress(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
            if (enabled) {
                LogUtils.sendDebug("[AutoCookie] Interrupting cookie purchase");
                disable();
            }
        }
    }

    private static Entity getBazaarNPC() {
        for (final Entity e : mc.theWorld.loadedEntityList) {
            if (e instanceof EntityArmorStand) {
                final String name = StringUtils.stripControlCodes(e.getDisplayName().getUnformattedText());
                if (name.startsWith("Bazaar")) {
                    return e;
                }
            }
        }
        return null;
    }

    private static float distanceToBazaar() {
        return (float) Math.sqrt(Math.pow(mc.thePlayer.posX - (-33), 2) + Math.pow(mc.thePlayer.posZ - (-77), 2));
    }
}