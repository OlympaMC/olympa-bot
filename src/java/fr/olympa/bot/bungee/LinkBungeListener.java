package fr.olympa.bot.bungee;

import java.sql.SQLException;

import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent.ChangeType;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import fr.olympa.bot.discord.link.LinkHandler;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import fr.olympa.core.bungee.api.customevent.OlympaGroupChangeEvent;
import net.dv8tion.jda.api.entities.Member;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class LinkBungeListener implements Listener {

	@EventHandler
	public void onOlympaGroupChange(OlympaGroupChangeEvent event) {
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		ChangeType state = event.getState();
		switch (state) {
		case ADD:
			break;
		case REMOVE:
			break;
		case SET:
			break;
		}
		DiscordMember discordMember;
		try {
			discordMember = CacheDiscordSQL.getDiscordMemberByOlympaId(olympaPlayer.getId());
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		if (discordMember == null)
			return;
		Member member = GuildHandler.getMember(DiscordGuildType.PUBLIC, discordMember.getDiscordId());
		Member memberStaff = GuildHandler.getMember(DiscordGuildType.STAFF, discordMember.getDiscordId());

		LinkHandler.updateGroups(member, olympaPlayer);
		if (memberStaff != null)
			LinkHandler.updateGroups(memberStaff, olympaPlayer);

	}

	@EventHandler
	public void onPlayerDisconnect(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		LinkHandler.removeWaiting(player);
	}
}
