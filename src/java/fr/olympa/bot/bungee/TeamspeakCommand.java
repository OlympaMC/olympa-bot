package fr.olympa.bot.bungee;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.teamspeak.TeamspeakHandler;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class TeamspeakCommand extends BungeeCommand {

	public TeamspeakCommand(Plugin plugin) {
		super(plugin, "teamspeak", OlympaCorePermissionsBungee.TEAMSPEAK_COMMAND, "ts");
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		ProxiedPlayer player = null;
		if (sender instanceof ProxiedPlayer)
			player = (ProxiedPlayer) sender;

		if (args.length == 0) {
			if (player == null) {
				sendImpossibleWithConsole();
				return;
			}
			sendTeamspeakIp(player);
			return;
		}
		switch (args[0].toLowerCase()) {
		case "link":
			if (player == null) {
				sendImpossibleWithConsole();
				return;
			}
			if (!canUse(player)) {
				sendMessage(Prefix.DEFAULT_BAD, "&cMerci de patienter avant de faire &4/ts link&c.");
				return;
			}
			try {
				TeamspeakHandler.check(player);
			} catch (InterruptedException e) {
				sendError();
				e.printStackTrace();
			}
			break;
		case "stop":
			if (!hasPermission(OlympaCorePermissionsBungee.TEAMSPEAK_COMMAND_MANAGE)) {
				sendDoNotHavePermission();
				return;
			}
			OlympaBots.getInstance().getTeamspeak().disconnect();
			sendMessage(Prefix.DEFAULT_GOOD, "&aTeamspeakBot déconnecté.");
			break;
		case "start":
			if (!hasPermission(OlympaCorePermissionsBungee.TEAMSPEAK_COMMAND_MANAGE)) {
				sendDoNotHavePermission();
				return;
			}
			OlympaBots.getInstance().getTeamspeak().connect();
			sendMessage(Prefix.DEFAULT_GOOD, "&aTeamspeakBot connecté.");
			break;
		}
	}

	private static Map<UUID, Long> cooldown = new HashMap<>();

	public static void addPlayer(ProxiedPlayer player) {
		cooldown.put(player.getUniqueId(), Utils.getCurrentTimeInSeconds() + 60);
	}

	public static boolean canUse(ProxiedPlayer player) {
		if (cooldown.containsKey(player.getUniqueId())) {
			if (cooldown.get(player.getUniqueId()) < Utils.getCurrentTimeInSeconds()) {
				cooldown.remove(player.getUniqueId());
				return true;
			}
			return false;
		}
		return true;
	}

	public static void remove(ProxiedPlayer player) {
		cooldown.remove(player.getUniqueId());
	}

	public static void sendTeamspeakIp(ProxiedPlayer player) {
		TextComponent message = new TextComponent(TextComponent.fromLegacyText(ColorUtils.color(Prefix.DEFAULT_GOOD + "&aTeamspeak : &2")));

		TextComponent ip = new TextComponent(TextComponent.fromLegacyText(ColorUtils.color("&2ts.olympa.fr")));
		ip.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ColorUtils.color("&2Clique pour sur le lien pour te connecter automatique"))));
		ip.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://teamspeak.olympa.fr"));
		message.addExtra(ip);
		player.sendMessage(message);
	}

}
