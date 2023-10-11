package com.jelly.farmhelper.remote.command.discordCommands;

import com.google.gson.JsonObject;
import com.jelly.farmhelper.remote.WebsocketHandler;
import com.jelly.farmhelper.remote.discordStruct.DiscordCommand;
import com.jelly.farmhelper.remote.discordStruct.Option;
import com.jelly.farmhelper.remote.waiter.Waiter;
import com.jelly.farmhelper.remote.waiter.WaiterHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.utils.FileUpload;

import java.util.Base64;
import java.util.Objects;

public class Connect extends DiscordCommand {
    public static final String name = "connect";
    public static final String description = "Connect to hypixel";

    public Connect() {
        super(Connect.name, Connect.description);
        Option ign = new Option(OptionType.STRING, "ign", "The IGN of the player you want to get information about", false, true);
        addOptions(ign);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event){
        event.deferReply().queue();
        int timeout = 5000;
        WaiterHandler.register(new Waiter(
                timeout,
                name,
                action -> {
                    String username = action.args.get("username").getAsString();
                    String image = action.args.get("image").getAsString();
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.addField("Username", username, true);
                    embedBuilder.setImage("attachment://image.png");
                    int random = (int) (Math.random() * 0xFFFFFF);
                    embedBuilder.setColor(random);
                    embedBuilder.setFooter("-> FarmHelper Remote Control", "https://cdn.discordapp.com/attachments/861700235890130986/1144673641951395982/icon.png");
                    String avatar = "https://crafatar.com/avatars/" + action.args.get("uuid").getAsString();
                    embedBuilder.setAuthor("Instance name -> " + username, avatar, avatar);

                    MessageEmbed em = embedBuilder.build();
                    try {
                        event.getHook().sendMessageEmbeds(em).addFiles(FileUpload.fromData(Base64.getDecoder().decode(image), "image.png")).queue();
                    } catch (Exception e) {
                        event.getChannel().sendMessageEmbeds(em).addFiles(FileUpload.fromData(Base64.getDecoder().decode(image), "image.png")).queue();
                    }
                },
                timeoutAction -> {
                    try {
                        event.getHook().sendMessage("Can't invoke the connect").queue();
                    } catch (Exception e) {
                        event.getChannel().sendMessage("Can't invoke the connect").queue();
                    }
                },
                event
        ));

        JsonObject args = new JsonObject();

        if (event.getOption("ign") != null) {
            String ign = Objects.requireNonNull(event.getOption("ign")).getAsString();
            if (WebsocketHandler.websocketServer.minecraftInstances.containsValue(ign)) {
                WebsocketHandler.websocketServer.minecraftInstances.forEach((webSocket, s) -> {
                    if (s.equals(ign)) {
                        sendMessage(webSocket, args);
                    }
                });
            } else {
                event.getHook().sendMessage("There isn't any instances connected with that IGN").queue();
            }
        } else {
            WebsocketHandler.websocketServer.minecraftInstances.forEach((webSocket, s) -> {
                sendMessage(webSocket, args);
            });
        }
    }
}
