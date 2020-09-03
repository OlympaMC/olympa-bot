package fr.olympa.bot.bungee;

import java.awt.Color;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import fr.olympa.bot.discord.webhook.WebHookHandler;
import fr.olympa.core.bungee.staffchat.StaffChatEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class StaffListenerBungee implements Listener {

	@EventHandler
	public void onPlayerDisconnect(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		OlympaPlayer olympaPlayer = null;
		try {
			olympaPlayer = new AccountProvider(player.getUniqueId()).get();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		DiscordMember discordMember = null;
		String playerName = player.getName();
		try {
			discordMember = CacheDiscordSQL.getDiscordMemberByOlympaId(olympaPlayer.getId());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (discordMember != null) {
			User user = discordMember.getUser();
			playerName = user.getAsMention();
		}
		TextChannel channelStaffDiscord = GuildHandler.getMinecraftChannel();
		EmbedBuilder eb = new EmbedBuilder();
		eb.setAuthor(player.getName(), null, "https://minotar.net/helm/" + player.getName());
		eb.setDescription(playerName + " s'est déconnecté du serveur.");
		eb.setColor(Color.RED);
		channelStaffDiscord.sendMessage(eb.build()).queue();
	}

	@EventHandler
	public void onPostLogin(ServerSwitchEvent event) {
		if (event.getFrom() != null)
			return;
		ProxiedPlayer player = event.getPlayer();
		OlympaPlayer olympaPlayer = null;
		try {
			olympaPlayer = new AccountProvider(player.getUniqueId()).get();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		DiscordMember discordMember = null;
		String playerName = player.getName();
		try {
			discordMember = CacheDiscordSQL.getDiscordMemberByOlympaId(olympaPlayer.getId());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (discordMember != null) {
			User user = discordMember.getUser();
			playerName = user.getAsMention();
		}
		TextChannel channelStaffDiscord = GuildHandler.getMinecraftChannel();
		EmbedBuilder eb = new EmbedBuilder();
		eb.setAuthor(player.getName(), null, "https://minotar.net/helm/" + player.getName());
		eb.setDescription(playerName + " s'est connecté au serveur " + player.getServer().getInfo().getName() + ".");
		eb.setColor(Color.GREEN);
		channelStaffDiscord.sendMessage(eb.build()).queue();
	}

	@EventHandler
	public void onStaffChat(StaffChatEvent event) {
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		String message = event.getMessage();

		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(DiscordGuildType.STAFF);
		Guild guild = olympaGuild.getGuild();
		TextChannel channelStaffDiscord = olympaGuild.getStaffChannel();
		if (olympaPlayer != null) {
			Member member = null;
			try {
				DiscordMember dm = CacheDiscordSQL.getDiscordMemberByOlympaId(olympaPlayer.getId());
				if (dm != null)
					member = guild.getMemberById(dm.getDiscordId());
			} catch (SQLException e) {
				e.printStackTrace();
				return;
			}
			if (member == null)
				member = guild.getMembersByEffectiveName(olympaPlayer.getName(), true).get(0);
			if (member != null)
				WebHookHandler.send(message, channelStaffDiscord, member);
		} else
			WebHookHandler.send(message, channelStaffDiscord, event.getSender().getName(), "https://c7.uihere.com/files/250/925/132/computer-terminal-linux-console-computer-icons-command-line-interface-linux.jpg");
	}

	public void sendError(String serverName, String stackTrace) {
		TextChannel channelStaffDiscord = GuildHandler.getBugsChannel();
		List<String> strings = new ArrayList<>(2);
		if (stackTrace.length() < 2048)
			strings.add(stackTrace);
		else {
			StringTokenizer tok = new StringTokenizer(stackTrace, "\n");
			StringBuilder output = new StringBuilder(stackTrace.length());
			int lineLen = 0;
			while (tok.hasMoreTokens()) {
				String word = tok.nextToken() + "\n";

				if (lineLen + word.length() > 2048) {
					strings.add(output.toString());
					output = new StringBuilder(stackTrace.length());
					lineLen = 0;
				}
				output.append(word);
				lineLen += word.length();
			}
			strings.add(output.toString());
		}
		//		for (int i = 0; i < strings.size(); i++)
		// channelStaffDiscord.sendMessage(new EmbedBuilder().setTitle("Erreur sur " + serverName + " (" + (i + 1) + "/" + strings.size() + ")").setDescription("```" + strings.get(i) + "```").setColor(Color.RED).build()).queue();
		for (int i = 0; i < strings.size(); i++)
			channelStaffDiscord.sendMessage((i == 0 ? "**Erreur sur " + serverName + "**\n" : "") + "```Java\n" + strings.get(i) + "```").queue();
	}
}
