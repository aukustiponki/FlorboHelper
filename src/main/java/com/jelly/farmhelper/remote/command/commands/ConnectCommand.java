package com.jelly.farmhelper.remote.command.commands;

import com.google.gson.JsonObject;
import com.jelly.farmhelper.remote.struct.ClientCommand;
import com.jelly.farmhelper.remote.struct.Command;
import com.jelly.farmhelper.remote.struct.RemoteMessage;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.fml.client.FMLClientHandler;

@Command(label = "connect")
public class ConnectCommand extends ClientCommand {
    public static boolean isEnabled = false;

    @Override
    public void execute(RemoteMessage event) {
        JsonObject args = event.args;


        FMLClientHandler.instance().connectToServer(mc.currentScreen, new ServerData("Hypixel", "mc.hypixel.net", false));
        isEnabled = true;
        JsonObject data = new JsonObject();
        data.addProperty("username", mc.getSession().getUsername());
        data.addProperty("image", getScreenshot());
        data.addProperty("uuid", mc.getSession().getPlayerID());
        RemoteMessage response = new RemoteMessage(label, data);
        send(response);
    }
}
