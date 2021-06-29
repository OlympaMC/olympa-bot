package fr.olympa.bot.bungee;

import java.awt.Color;
import java.sql.SQLException;

import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import fr.olympa.bot.discord.webhook.WebHookHandler;
import fr.olympa.core.bungee.staffchat.StaffChatEvent;
import fr.olympa.core.common.provider.AccountProvider;
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
		if (olympaPlayer != null)
			try {
				discordMember = CacheDiscordSQL.getDiscordMemberByOlympaId(olympaPlayer.getId());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		if (discordMember != null) {
			User user = discordMember.getUser();
			if (user != null)
				playerName = user.getAsMention();
		}
		TextChannel channelStaffDiscord = GuildHandler.getMinecraftChannel();
		if (channelStaffDiscord == null)
			return;
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
		if (olympaPlayer != null)
			try {
				discordMember = CacheDiscordSQL.getDiscordMemberByOlympaId(olympaPlayer.getId());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		if (discordMember != null) {
			User user = discordMember.getUser();
			if (user != null)
				playerName = user.getAsMention();
		}
		TextChannel channelStaffDiscord = GuildHandler.getMinecraftChannel();
		if (channelStaffDiscord == null)
			return;
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

}
