package fr.olympa.bot.bungee;

import java.sql.SQLException;

import fr.olympa.api.bungee.customevent.BungeeOlympaGroupChangeEvent;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import fr.olympa.bot.discord.link.LinkHandler;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import fr.olympa.bot.teamspeak.TeamspeakHandler;
import net.dv8tion.jda.api.entities.Member;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class LinkBungeeListener implements Listener {

	@EventHandler
	public void onOlympaGroupChange(BungeeOlympaGroupChangeEvent event) {
		System.out.println("update ok");
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		//		ChangeType state = event.getState();
		//		switch (state) {
		//		case ADD:
		//			break;
		//		case REMOVE:
		//			break;
		//		case SET:
		//			break;
		//		}
		DiscordMember discordMember;
		try {
			discordMember = CacheDiscordSQL.getDiscordMemberByOlympaId(olympaPlayer.getId());
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		try {
			if (discordMember != null) {
				Member member = GuildHandler.getMember(DiscordGuildType.PUBLIC, discordMember.getDiscordId());
				Member memberStaff = GuildHandler.getMember(DiscordGuildType.STAFF, discordMember.getDiscordId());
				if (member != null)
					LinkHandler.updateGroups(member, olympaPlayer);
				if (memberStaff != null)
					LinkHandler.updateGroups(memberStaff, olympaPlayer);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			TeamspeakHandler.updateRank(olympaPlayer);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@EventHandler
	public void onPlayerDisconnect(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		LinkHandler.removeWaiting(player);
	}
}
