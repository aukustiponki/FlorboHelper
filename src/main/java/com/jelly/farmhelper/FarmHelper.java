package com.jelly.farmhelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jelly.farmhelper.commands.FarmHelperCommand;
import com.jelly.farmhelper.commands.RewarpCommand;
import com.jelly.farmhelper.config.Config;
import com.jelly.farmhelper.features.*;
import com.jelly.farmhelper.macros.MacroHandler;
import com.jelly.farmhelper.remote.DiscordBotHandler;
import com.jelly.farmhelper.remote.WebsocketHandler;
import com.jelly.farmhelper.remote.command.commands.SetSpeedCommand;
import com.jelly.farmhelper.utils.LocationUtils;
import com.jelly.farmhelper.utils.TickTask;
import com.jelly.farmhelper.utils.Utils;
import com.jelly.farmhelper.world.GameState;
import com.jelly.farmhelper.world.JacobsContestHandler;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.Display;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.Manifest;


@Mod(modid = FarmHelper.MODID, name = FarmHelper.NAME, version = FarmHelper.VERSION)
public class FarmHelper {
    public static final String MODID = "farmhelper";
    public static final String NAME = "Florbo Helper";
    // Version gets automatically set. If you wish to change it, change it in the build.gradle.kts file
    public static final String VERSION = "%%VERSION%%";

    // the actual mod version from gradle properties, should match with VERSION
    public static String MODVERSION = VERSION;
    public static String BOTVERSION = "-1";
    public static int tickCount = 0;
    public static TickTask ticktask;
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static GameState gameState;
    public static PetSwapper petSwapper = new PetSwapper();

    public static Config config;
    public static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        setVersions();
        config = new Config();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new MacroHandler());
        MinecraftForge.EVENT_BUS.register(new FailsafeNew());
        MinecraftForge.EVENT_BUS.register(new Antistuck());
        MinecraftForge.EVENT_BUS.register(new AutoSellNew());
        MinecraftForge.EVENT_BUS.register(new Scheduler());
        MinecraftForge.EVENT_BUS.register(new AutoReconnect());
        MinecraftForge.EVENT_BUS.register(new AutoCookie());
        MinecraftForge.EVENT_BUS.register(new AutoPot());
        MinecraftForge.EVENT_BUS.register(new BanwaveChecker());
        MinecraftForge.EVENT_BUS.register(new RewarpWhenNotMacroing());
        if (Loader.isModLoaded("farmhelperjdadependency")) {
            MinecraftForge.EVENT_BUS.register(new DiscordBotHandler());
        }
        MinecraftForge.EVENT_BUS.register(new ProfitCalculator());
        MinecraftForge.EVENT_BUS.register(new Utils());
        MinecraftForge.EVENT_BUS.register(new VisitorsMacro());
        MinecraftForge.EVENT_BUS.register(new Pinger());
        MinecraftForge.EVENT_BUS.register(new JacobsContestHandler());
        MinecraftForge.EVENT_BUS.register(new LocationUtils());
        MinecraftForge.EVENT_BUS.register(new LagDetection());
        MinecraftForge.EVENT_BUS.register(new SetSpeedCommand());
        MinecraftForge.EVENT_BUS.register(petSwapper);
        MinecraftForge.EVENT_BUS.register(new WebsocketHandler());
        MinecraftForge.EVENT_BUS.register(AntiAfk.getInstance());
        ClientCommandHandler.instance.registerCommand(new RewarpCommand());
        ClientCommandHandler.instance.registerCommand(new FarmHelperCommand());

        gameState = new GameState();

        if (FarmHelper.config.SShapeMacroType > 6) // fix for old config
            FarmHelper.config.SShapeMacroType = 6;

        mc.gameSettings.pauseOnLostFocus = false;
        mc.gameSettings.gammaSetting = 1000;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public final void tick(TickEvent.ClientTickEvent event) throws IOException {
        if (ticktask != null) {
            ticktask.onTick();
        }
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer != null && mc.theWorld != null) {
            gameState.update();
        }
        tickCount += 1;
        tickCount %= 20;
    }

    @SneakyThrows
    public static void setVersions() {
        Class<FarmHelper> clazz = FarmHelper.class;
        String className = clazz.getSimpleName() + ".class";
        String classPath = Objects.requireNonNull(clazz.getResource(className)).toString();
        if (!classPath.startsWith("jar")) return;

        String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) +
                "/META-INF/MANIFEST.MF";
        Manifest manifest = new Manifest(new URL(manifestPath).openStream());
        Attributes attr = manifest.getMainAttributes();
        BOTVERSION = attr.getValue("botversion");
        Display.setTitle(FarmHelper.NAME + " " + MODVERSION + " | Bing Chilling");
    }
}
