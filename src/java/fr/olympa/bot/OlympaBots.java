package fr.olympa.bot;

import java.util.concurrent.TimeUnit;

import fr.olympa.api.redis.RedisAccess;
import fr.olympa.api.redis.RedisChannel;
import fr.olympa.api.utils.ErrorOutputStream;
import fr.olympa.bot.bungee.DiscordCommand;
import fr.olympa.bot.bungee.LinkBungeListener;
import fr.olympa.bot.bungee.SpigotReceiveError;
import fr.olympa.bot.bungee.StaffListenerBungee;
import fr.olympa.bot.bungee.TeamspeakCommand;
import fr.olympa.bot.discord.OlympaDiscord;
import fr.olympa.bot.teamspeak.OlympaTeamspeak;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.api.config.BungeeCustomConfig;
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
		olympaTeamspeak.disconnect();
		olympaDiscord.disconnect();
		sendMessage("§4" + getDescription().getName() + "§c (" + getDescription().getVersion() + ") est désactivé.");
	}

	@Override
	public void onLoad() {
		super.onLoad();
		System.setErr(new ErrorOutputStream(System.err, stackTrace -> bungeeListener.sendError("bungee", stackTrace), run -> getProxy().getScheduler().schedule(this, run, 1, TimeUnit.SECONDS)));
	}

	@Override
	public void onEnable() {
		instance = this;
		defaultConfig = new BungeeCustomConfig(this, "config");
		defaultConfig.loadSafe();
		PluginManager pluginManager = getProxy().getPluginManager();
		// swearHandler = new SwearHandler(olympaBungee.getConfig().getStringList("chat.insult"));
		pluginManager.registerListener(this, new LinkBungeListener());
		pluginManager.registerListener(this, bungeeListener = new StaffListenerBungee());
		new DiscordCommand(this).register();
		new TeamspeakCommand(this).register();

		olympaDiscord = new OlympaDiscord();
		olympaDiscord.connect(this);

		olympaTeamspeak = new OlympaTeamspeak();
		olympaTeamspeak.connect(this);

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
