package fr.olympa.bot;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.logging.slf4j.Log4jLogger;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.bungee.config.BungeeCustomConfig;
import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.logger.LoggerUtils;
import fr.olympa.api.common.redis.RedisChannel;
import fr.olympa.api.common.task.NativeTask;
import fr.olympa.api.utils.CacheStats;
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
import fr.olympa.core.common.redis.RedisAccess;
import fr.olympa.core.common.utils.ErrorLoggerHandler;
import fr.olympa.core.common.utils.ErrorOutputStream;
import net.dv8tion.jda.internal.JDAImpl;
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

	public void sendBungeeErrorTestStandardOutputError(String stackTrace) {
		OlympaBots.getInstance().sendMessage("[TEST TO BE REMOVED] Standard error output catched.");
	}

	@Override
	public void onLoad() {
		instance = this;
		spigotReceiveError = new SpigotReceiveError();
		//		System.setErr(new PrintStream(new ErrorOutputStream(System.err, spigotReceiveError::sendBungeeError, run -> NativeTask.getInstance().runTaskLater(run, 1, TimeUnit.SECONDS))));
		System.setErr(new PrintStream(new ErrorOutputStream(System.err, this::sendBungeeErrorTestStandardOutputError, run -> NativeTask.getInstance().runTaskLater(run, 1, TimeUnit.SECONDS))));
		for (Logger logger : LoggerUtils.getAllHooks()) {

		}
		LoggerUtils.hook(new ErrorLoggerHandler(spigotReceiveError::sendBungeeError));
		try {
			Field f = Log4jLogger.class.getDeclaredField("logger");
			f.setAccessible(true);
			org.apache.logging.log4j.core.Logger log = (org.apache.logging.log4j.core.Logger) f.get(JDAImpl.LOG);
			log.addAppender(new Log4JErrorAppender(spigotReceiveError::sendBungeeError));
		} catch (Exception ex) {
			sendMessage("§cFailed to implement custom appender in JDA logger (type %s).", JDAImpl.LOG.getClass().getName());
			ex.printStackTrace();
		}
	}

	@Override
	public void onEnable() {
		defaultConfig = new BungeeCustomConfig(this, "config");
		defaultConfig.loadSafe();
		PluginManager pluginManager = getProxy().getPluginManager();
		// swearHandler = new SwearHandler(olympaBungee.getConfig().getStringList("chat.insult"));
		pluginManager.registerListener(this, new LinkBungeeListener());
		pluginManager.registerListener(this, new StaffListenerBungee());
		pluginManager.registerListener(this, new TeamspeakListener());
		new DiscordCommand(this).register();
		new TeamspeakCommand(this).register();

		LinkSpigotBungee.getInstance().launchAsync(() -> {
			olympaDiscord = new OlympaDiscord(this);
			olympaDiscord.connect();

			olympaTeamspeak = new OlympaTeamspeak(this);
			olympaTeamspeak.connect();
			OlympaBungee.getInstance().registerRedisSub(RedisAccess.INSTANCE.connect(), spigotReceiveError, RedisChannel.SPIGOT_RECEIVE_ERROR.name());
		});

		//new TwitterAPI(this).connect();
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

	public String getServerName() {
		return "bungee";
	}
}
