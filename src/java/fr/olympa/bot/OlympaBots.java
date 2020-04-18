package fr.olympa.bot;

import java.sql.Connection;
import java.sql.SQLException;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.provider.RedisAccess;
import fr.olympa.bot.bungee.DiscordCommand;
import fr.olympa.bot.discord.OlympaDiscord;
import fr.olympa.bot.discord.link.LinkBungeListener;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

public class OlympaBots extends Plugin implements LinkSpigotBungee {

	private static OlympaBots instance;

	public static OlympaBots getInstance() {
		return instance;
	}

	private OlympaDiscord olympaDiscord;

	@Override
	public Connection getDatabase() throws SQLException {
		return OlympaBungee.getInstance().getDatabase();
	}

	public OlympaDiscord getDiscord() {
		return olympaDiscord;
	}

	private String getPrefixConsole() {
		return "&f[&6" + getDescription().getName() + "&f] &e";
	}

	@Override
	public void launchAsync(Runnable run) {
		OlympaBungee.getInstance().launchAsync(run);
	}

	@Override
	public void onDisable() {
		olympaDiscord.disconnect();
		sendMessage("§4" + getDescription().getName() + "§c (" + getDescription().getVersion() + ") est désactivé.");
	}

	@Override
	public void onEnable() {
		instance = this;

		LinkSpigotBungee.Provider.link = OlympaBungee.getInstance();
		RedisAccess.init("bungeeBot").connect();
		PluginManager pluginManager = getProxy().getPluginManager();
		// new
		// SwearHandler(BungeeConfigUtils.getDefaultConfig().getStringList("chat.insult"));
		pluginManager.registerListener(this, new LinkBungeListener());
		new DiscordCommand(this).register();
		olympaDiscord = new OlympaDiscord();
		olympaDiscord.connect();
		sendMessage("§2" + getDescription().getName() + "§a (" + getDescription().getVersion() + ") est activé.");
	}

	@SuppressWarnings("deprecation")
	public void sendMessage(String message) {
		getProxy().getConsole().sendMessage(BungeeUtils.color(getPrefixConsole() + message));
	}
}
