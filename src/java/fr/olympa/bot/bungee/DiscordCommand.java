package fr.olympa.bot.bungee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordIds;
import fr.olympa.bot.discord.groups.DiscordGroup;
import fr.olympa.bot.discord.link.LinkHandler;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;

public class DiscordCommand extends BungeeCommand implements TabExecutor {
	
	public DiscordCommand(Plugin plugin) {
		super(plugin, "discord");
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		if (args.length == 0) {
			TextComponent textComponent = new TextComponent();
			TextComponent textComponent2 = new TextComponent("[");
			textComponent2.setColor(ChatColor.DARK_PURPLE);
			textComponent.addExtra(textComponent2);
			
			textComponent2 = new TextComponent("Discord");
			textComponent2.setColor(ChatColor.LIGHT_PURPLE);
			textComponent.addExtra(textComponent2);
			
			textComponent2 = new TextComponent("] ");
			textComponent2.setColor(ChatColor.DARK_PURPLE);
			textComponent.addExtra(textComponent2);
			
			textComponent2 = new TextComponent("➤ ");
			textComponent2.setColor(ChatColor.DARK_PURPLE);
			textComponent.addExtra(textComponent2);
			
			textComponent2 = new TextComponent("discord.olympa.fr");
			textComponent2.setColor(ChatColor.LIGHT_PURPLE);
			textComponent2.setUnderlined(true);
			textComponent2.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("Clique pour rejoindre le discord").color(ChatColor.GREEN).create()));
			textComponent2.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "discord.olympa.fr"));
			textComponent.addExtra(textComponent2);
			
			textComponent2 = new TextComponent(". Pour relié son compte discord à Minecraft, fait ");
			textComponent2.setColor(ChatColor.DARK_PURPLE);
			textComponent.addExtra(textComponent2);
			
			textComponent2 = new TextComponent("/discord link");
			textComponent2.setColor(ChatColor.LIGHT_PURPLE);
			textComponent.addExtra(textComponent2);
			
			sender.sendMessage(textComponent);
			return;
		}
		if (proxiedPlayer != null) {
			// if (OlympaCorePermissions.DEV.hasPermission(proxiedPlayer.getUniqueId())) {
			
			// }
		}
		
		switch (args[0].toLowerCase()) {
		
		case "link":
			olympaPlayer = AccountProvider.get(proxiedPlayer.getUniqueId());
			if (olympaPlayer == null) {
				sender.sendMessage(Prefix.DEFAULT_BAD + BungeeUtils.color("Impossible d'accéder à tes donnés."));
				return;
			}
			if (olympaPlayer.getDiscordId() != 0)
				sender.sendMessage(Prefix.DEFAULT_BAD + BungeeUtils.color("Tu as déjà un compte Discord relié."));
			//				return;
			
			String code = LinkHandler.getCode(proxiedPlayer);
			if (code == null)
				code = LinkHandler.addWaiting(proxiedPlayer);
			sender.sendMessage(BungeeUtils.color("&5[&dDiscord&5] ➤ &dPour relier ton compte Discord & Olympa, envoie le code &5&l" + code + "&d en privé à &7@&5OlympaBot#5503&d."));
			
			break;
		case "test":
			Guild guild = DiscordIds.getStaffGuild();
			olympaPlayer = AccountProvider.get(proxiedPlayer.getUniqueId());
			String roles = DiscordGroup.get(olympaPlayer.getGroups().keySet()).stream().map(g -> g.getRole(guild).getName()).collect(Collectors.joining("&d,&5 "));
			
			long id = olympaPlayer.getDiscordId();
			Member member = guild.getMemberById(id);
			sender.sendMessage(BungeeUtils.color("&5[&dDiscord&5] ➤ &dRoles de " + proxiedPlayer.getName() + " " + id + ": &5" + roles + "&d."));
			break;
		case "stop":
			if (olympaPlayer != null && !OlympaCorePermissions.DEV.hasPermission(olympaPlayer)) {
				sendDoNotHavePermission();
				return;
			}
			if (OlympaBots.getInstance().getDiscord().getJda() != null) {
				OlympaBots.getInstance().getDiscord().disconnect();
				sender.sendMessage(BungeeUtils.color("&5[&dDiscord&5] ➤ &6Bot éteint."));
			} else
				sender.sendMessage(BungeeUtils.color("&5[&dDiscord&5] ➤ &cBot déjà éteint."));
			
			break;
		case "start":
			if (olympaPlayer != null && !OlympaCorePermissions.DEV.hasPermission(olympaPlayer)) {
				sendDoNotHavePermission();
				return;
			}
			if (OlympaBots.getInstance().getDiscord().getJda() == null) {
				OlympaBots.getInstance().getDiscord().connect(OlympaBots.getInstance());
				sender.sendMessage(BungeeUtils.color("&5[&dDiscord&5] ➤ &aBot allumé."));
			} else
				sender.sendMessage(BungeeUtils.color("&5[&dDiscord&5] ➤ &cBot déjà allumé."));
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
