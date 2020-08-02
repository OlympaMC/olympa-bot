package fr.olympa.bot;

import fr.olympa.api.SwearHandler;
import fr.olympa.api.provider.RedisAccess;
import fr.olympa.bot.bungee.DiscordCommand;
import fr.olympa.bot.bungee.LinkBungeListener;
import fr.olympa.bot.discord.OlympaDiscord;
import fr.olympa.bot.discord.staff.StaffListenerBungee;
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
	private SwearHandler swearHandler;

	public OlympaDiscord getDiscord() {
		return olympaDiscord;
	}

	private String getPrefixConsole() {
		return "&f[&6" + getDescription().getName() + "&f] &e";
	}

	public SwearHandler getSwearHandler() {
		return swearHandler;
	}

	@Override
	public void onDisable() {
		olympaDiscord.disconnect();
		sendMessage("§4" + getDescription().getName() + "§c (" + getDescription().getVersion() + ") est désactivé.");
	}

	@Override
	public void onEnable() {
		instance = this;

		RedisAccess.init("bungeeBot").connect();
		PluginManager pluginManager = getProxy().getPluginManager();
		// swearHandler = new SwearHandler(olympaBungee.getConfig().getStringList("chat.insult"));
		pluginManager.registerListener(this, new LinkBungeListener());
		pluginManager.registerListener(this, new StaffListenerBungee());
		new DiscordCommand(this).register();

		olympaDiscord = new OlympaDiscord();
		olympaDiscord.connect(this);

		//new TwitterAPI(this).connect();
		sendMessage("§2" + getDescription().getName() + "§a (" + getDescription().getVersion() + ") est activé.");
	}

	public void sendMessage(String message) {
		getProxy().getConsole().sendMessage(TextComponent.fromLegacyText(BungeeUtils.color(getPrefixConsole() + message)));
	}
}
