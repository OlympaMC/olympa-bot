package fr.olympa.bot;

import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import fr.olympa.api.bungee.config.BungeeCustomConfig;
import fr.olympa.api.redis.RedisAccess;
import fr.olympa.api.redis.RedisChannel;
import fr.olympa.api.utils.ErrorLoggerHandler;
import fr.olympa.api.utils.ErrorOutputStream;
import fr.olympa.bot.bungee.DiscordCommand;
import fr.olympa.bot.bungee.LinkBungeeListener;
import fr.olympa.bot.bungee.SpigotReceiveError;
import fr.olympa.bot.bungee.StaffListenerBungee;
import fr.olympa.bot.bungee.TeamspeakCommand;
import fr.olympa.bot.bungee.TeamspeakListener;
import fr.olympa.bot.discord.OlympaDiscord;
import fr.olympa.bot.teamspeak.OlympaTeamspeak;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

public class OlympaBots extends Plugin {

	private static OlympaBots instance;

	public static OlympaBots getInstance() {
		return instance;
	}

	private OlympaDiscord olympaDiscord;
	private OlympaTeamspeak olympaTeamspeak;
	public StaffListenerBungee bungeeListener;
	private BungeeCustomConfig defaultConfig;

	@Override
	public void onDisable() {
		if (olympaTeamspeak != null)
			olympaTeamspeak.disconnect();
		if (olympaDiscord != null)
			olympaDiscord.disconnect();
		sendMessage("§4" + getDescription().getName() + "§c (" + getDescription().getVersion() + ") est désactivé.");
	}

	@Override
	public void onLoad() {
		super.onLoad();
		bungeeListener = new StaffListenerBungee();
		System.setErr(new PrintStream(new ErrorOutputStream(System.err, bungeeListener::sendBungeeError, run -> getProxy().getScheduler().schedule(this, run, 1, TimeUnit.SECONDS))));
		ErrorLoggerHandler errorHandler = new ErrorLoggerHandler(bungeeListener::sendBungeeError);
		for (Plugin plugin : getProxy().getPluginManager().getPlugins()) {
			plugin.getLogger().addHandler(errorHandler);
			sendMessage("Hooked dans le logger de §6" + plugin.getDescription().getName());
		}
	}

	@Override
	public void onEnable() {
		instance = this;
		defaultConfig = new BungeeCustomConfig(this, "config");
		defaultConfig.loadSafe();
		PluginManager pluginManager = getProxy().getPluginManager();
		// swearHandler = new SwearHandler(olympaBungee.getConfig().getStringList("chat.insult"));
		pluginManager.registerListener(this, new LinkBungeeListener());
		pluginManager.registerListener(this, bungeeListener);
		pluginManager.registerListener(this, new TeamspeakListener());
		new DiscordCommand(this).register();
		new TeamspeakCommand(this).register();

		olympaDiscord = new OlympaDiscord(this);
		olympaDiscord.connect();

		olympaTeamspeak = new OlympaTeamspeak(this);
		olympaTeamspeak.connect();

		//new TwitterAPI(this).connect();
		OlympaBungee.getInstance().registerRedisSub(RedisAccess.INSTANCE.connect(), new SpigotReceiveError(), RedisChannel.SPIGOT_RECEIVE_ERROR.name());
		sendMessage("§2" + getDescription().getName() + "§a (" + getDescription().getVersion() + ") est activé.");
	}

	public void sendMessage(String message) {
		getProxy().getConsole().sendMessage(TextComponent.fromLegacyText(BungeeUtils.color(getPrefixConsole() + message)));
	}

	public OlympaTeamspeak getTeamspeak() {
		return olympaTeamspeak;
	}

	public BungeeCustomConfig getConfig() {
		return defaultConfig;
	}

	public OlympaDiscord getDiscord() {
		return olympaDiscord;
	}

	private String getPrefixConsole() {
		return "&f[&6" + getDescription().getName() + "&f] &e";
	}
}
