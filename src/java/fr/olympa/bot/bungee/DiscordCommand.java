package fr.olympa.bot.bungee;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Utils;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import fr.olympa.bot.discord.link.LinkHandler;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;

public class DiscordCommand extends BungeeCommand implements TabExecutor {

	private TextComponent message;

	public DiscordCommand(Plugin plugin) {
		super(plugin, "discord");
		message = new TextComponent();
		TextComponent textComponent = new TextComponent("[");
		textComponent.setColor(ChatColor.DARK_PURPLE);
		message.addExtra(textComponent);

		textComponent = new TextComponent("Discord");
		textComponent.setColor(ChatColor.LIGHT_PURPLE);
		message.addExtra(textComponent);

		textComponent = new TextComponent("] ");
		textComponent.setColor(ChatColor.DARK_PURPLE);
		message.addExtra(textComponent);

		textComponent = new TextComponent("➤ ");
		textComponent.setColor(ChatColor.DARK_PURPLE);
		message.addExtra(textComponent);

		textComponent = new TextComponent("discord.olympa.fr");
		textComponent.setColor(ChatColor.LIGHT_PURPLE);
		textComponent.setUnderlined(true);
		textComponent.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("Clique pour rejoindre le discord").color(ChatColor.GREEN).create()));
		textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "discord.olympa.fr"));
		message.addExtra(textComponent);

		textComponent = new TextComponent(". Pour relier ton compte discord à Minecraft, fais ");
		textComponent.setColor(ChatColor.DARK_PURPLE);
		message.addExtra(textComponent);

		textComponent = new TextComponent("/discord link");
		textComponent.setColor(ChatColor.LIGHT_PURPLE);
		message.addExtra(textComponent);
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(message);
			return;
		}

		getOlympaPlayer();
		switch (args[0].toLowerCase()) {

		case "link":
			DiscordMember discordMember;
			try {
				discordMember = CacheDiscordSQL.getDiscordMemberByOlympaId(olympaPlayer.getId());
			} catch (SQLException e) {
				e.printStackTrace();
				sendError();
				return;
			}
			if (discordMember != null) {
				sendError("Tu as déjà un compte Discord relié.");
				return;
			}

			String code = LinkHandler.getCode(proxiedPlayer);
			if (code == null)
				code = LinkHandler.addWaiting(proxiedPlayer);
			//sendMessage("&5[&dDiscord&5] ➤ &dPour relier ton compte Discord & Olympa, envoie le code &5&l" + code + "&d en privé à &7@&5OlympaBot#5503&d.");

			TextComponent message = new TextComponent(TextComponent.fromLegacyText("§5[§dDiscord§5] ➤ §dPour relier ton compte Discord & Olympa, envoie le code "));
			TextComponent codeComponent = new TextComponent(code);
			codeComponent.setColor(ChatColor.DARK_PURPLE);
			codeComponent.setBold(true);
			codeComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§eClique ici pour copier le code")));
			codeComponent.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, code));
			message.addExtra(codeComponent);
			for (BaseComponent baseComponent : TextComponent.fromLegacyText("§d en privé à §7@§5OlympaBot#5503§d, trouve le dans #informations sur Discord."))
				message.addExtra(baseComponent);
			proxiedPlayer.sendMessage(message);

			break;
		case "info":
		case "roles":
			OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(DiscordGuildType.PUBLIC);
			Guild guild = olympaGuild.getGuild();
			if (args.length > 2) {
				proxiedPlayer = ProxyServer.getInstance().getPlayer(args[1]);
				if (proxiedPlayer == null) {
					sendUnknownPlayer(args[1]);
					return;
				}
			}
			olympaPlayer = AccountProvider.get(proxiedPlayer.getUniqueId());
			try {
				discordMember = CacheDiscordSQL.getDiscordMemberByOlympaId(olympaPlayer.getId());
				if (discordMember == null) {
					if (args.length > 2)
						sendError("&4" + proxiedPlayer.getDisplayName() + "&c n'as pas lié son compte Discord et Minecraft.");
					else
						sendError("Tu n'as pas lié ton compte Discord et Minecraft.");
					return;
				}
				User user = discordMember.getUser();
				Member member = guild.getMember(user);
				String roles = member.getRoles().stream().map(g -> g.getName()).collect(Collectors.joining("&d,&5 "));
				StringJoiner sj = new StringJoiner("\n");
				sj.add("&5[&dDiscord&5] Compte Discord de " + proxiedPlayer.getDisplayName());
				if (!member.getEffectiveName().equals(user.getName()))
					sj.add("&7➤ &dNom: &5" + member.getEffectiveName() + "&d.");
				sj.add("&7➤ &dTag: &5" + user.getAsTag() + "&d.");
				sj.add("&7➤ &dRoles: &5" + roles + "&d.");
				String t = Utils.timestampToDuration(user.getTimeCreated().toEpochSecond());
				String date = user.getTimeCreated().format(DateTimeFormatter.ISO_LOCAL_DATE);
				sj.add("&7➤ &dCompte discord crée: &5" + date + "&d (" + t + ").");
				t = Utils.timestampToDuration(member.getTimeJoined().toEpochSecond());
				date = member.getTimeJoined().format(DateTimeFormatter.ISO_LOCAL_DATE);
				sj.add("&7➤ &dRejoint le discord le &5" + date + "&d (" + t + ").");
				sj.add("&7➤ &dXP: &5" + String.valueOf(discordMember.getXp()) + "&d.");
				OnlineStatus onlineStatus = member.getOnlineStatus();
				if (onlineStatus == OnlineStatus.OFFLINE && discordMember.getLastSeenTime() != 0)
					sj.add("&7➤ &dDernière Action: &5il y a " + Utils.timestampToDuration(Utils.getCurrentTimeInSeconds() - discordMember.getLastSeenTime()) + "&d.");
				else
					sj.add("&7➤ &dStatut: &5" + Utils.capitalize(onlineStatus.name().replaceAll("_", " ")) + "&d.");
				sendMessage(sj.toString());
			} catch (SQLException e) {
				e.printStackTrace();
			}
			break;
		case "stop":
			if (olympaPlayer != null && !OlympaCorePermissions.DISCORD_BOT.hasPermission(olympaPlayer)) {
				sendDoNotHavePermission();
				return;
			}
			if (OlympaBots.getInstance().getDiscord().getJda() != null) {
				OlympaBots.getInstance().getDiscord().disconnect();
				sendMessage("&5[&dDiscord&5] ➤ &6Bot éteint.");
			} else
				sendMessage("&5[&dDiscord&5] ➤ &cBot déjà éteint.");

			break;
		case "start":
			if (olympaPlayer != null && !OlympaCorePermissions.DISCORD_BOT.hasPermission(olympaPlayer)) {
				sendDoNotHavePermission();
				return;
			}
			if (OlympaBots.getInstance().getDiscord().getJda() == null) {
				OlympaBots.getInstance().getDiscord().connect(OlympaBots.getInstance());
				sendMessage("&5[&dDiscord&5] ➤ &aBot allumé.");
			} else
				sendMessage("&5[&dDiscord&5] ➤ &cBot déjà allumé.");
			break;
		default:
			sendUsage();
			break;
		}
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		if (args.length == 1) {
			List<String> reasons = Arrays.asList("link", "info");
			return Utils.startWords(args[0], reasons);
		}
		return new ArrayList<>();
	}

}
