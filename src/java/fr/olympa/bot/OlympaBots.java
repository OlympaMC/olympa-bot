package fr.olympa.bot;

import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import fr.olympa.api.bungee.config.BungeeCustomConfig;
import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.logger.LoggerUtils;
import fr.olympa.api.common.redis.RedisAccess;
import fr.olympa.api.common.redis.RedisChannel;
import fr.olympa.api.common.task.NativeTask;
import fr.olympa.api.utils.CacheStats;
import fr.olympa.api.utils.ErrorLoggerHandler;
import fr.olympa.api.utils.ErrorOutputStream;
import fr.olympa.bot.bungee.DiscordCommand;
import fr.olympa.bot.bungee.LinkBungeeListener;
import fr.olympa.bot.bungee.SpigotReceiveError;
import fr.olympa.bot.bungee.StaffListenerBungee;
import fr.olympa.bot.bungee.TeamspeakCommand;
import fr.olympa.bot.bungee.TeamspeakListener;
import fr.olympa.bot.discord.OlympaDiscord;
import fr.olympa.bot.discord.api.reaction.AwaitReaction;
import fr.olympa.bot.discord.link.LinkHandler;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import fr.olympa.bot.teamspeak.OlympaTeamspeak;
import fr.olympa.core.bungee.OlympaBungee;
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
	public SpigotReceiveError spigotReceiveError;
	private BungeeCustomConfig defaultConfig;

	@Override
	public void onDisable() {
		if (olympaTeamspeak != null)
			olympaTeamspeak.disconnect();
		if (olympaDiscord != null)
			olympaDiscord.disconnect();
		LoggerUtils.unHookAll();
		sendMessage("§4" + getDescription().getName() + "§c (" + getDescription().getVersion() + ") est désactivé.");
	}

	@Override
	public void onLoad() {
		super.onLoad();
		spigotReceiveError = new SpigotReceiveError();
		System.setErr(new PrintStream(new ErrorOutputStream(System.err, spigotReceiveError::sendBungeeError, run -> NativeTask.getInstance().runTaskLater(run, 1, TimeUnit.SECONDS))));
		LoggerUtils.hook(new ErrorLoggerHandler(spigotReceiveError::sendBungeeError));
	}

	@Override
	public void onEnable() {
		instance = this;
		defaultConfig = new BungeeCustomConfig(this, "config");
		defaultConfig.loadSafe();
		PluginManager pluginManager = getProxy().getPluginManager();
		// swearHandler = new SwearHandler(olympaBungee.getConfig().getStringList("chat.insult"));
		pluginManager.registerListener(this, new LinkBungeeListener());
		pluginManager.registerListener(this, new StaffListenerBungee());
		pluginManager.registerListener(this, new TeamspeakListener());
		new DiscordCommand(this).register();
		new TeamspeakCommand(this).register();

		olympaDiscord = new OlympaDiscord(this);
		olympaDiscord.connect();

		olympaTeamspeak = new OlympaTeamspeak(this);
		olympaTeamspeak.connect();

		//new TwitterAPI(this).connect();
		OlympaBungee.getInstance().registerRedisSub(RedisAccess.INSTANCE.connect(), spigotReceiveError, RedisChannel.SPIGOT_RECEIVE_ERROR.name());
		//		CacheDiscordSQL.debug();
		CacheStats.addCache("DISCORD_ERROR_SEND", SpigotReceiveError.cache);
		CacheStats.addCache("DISCORD_AWAIT_REACTIONS", AwaitReaction.reactions);
		CacheStats.addCache("DISCORD_LINK_CODE", LinkHandler.waiting);
		CacheStats.addCache("DISCORD_MEMBERS", CacheDiscordSQL.cacheMembers);
		sendMessage("§2" + getDescription().getName() + "§a (" + getDescription().getVersion() + ") est activé.");
	}

	public void sendMessage(String message, Object... args) {
		getProxy().getConsole().sendMessage(TextComponent.fromLegacyText(ColorUtils.format(getPrefixConsole() + message, args)));
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
