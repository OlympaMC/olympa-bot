package fr.olympa.bot.discord.invites;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.member.DiscordMember;

public class DiscordInvitesFullStats {

	OlympaGuild discordGuild;
	DiscordMember originalAuthor;
	List<Long> authorsOlympaDiscordId = new ArrayList<>();
	int invited;

	public void addInvited(ResultSet rs) {
		try {
			discordGuild = GuildHandler.getOlympaGuildById(rs.getLong(DiscordInvite.COLUMN_OLYMPA_GUILD_ID.getCleanName()));
			int invited = rs.getInt(DiscordInvite.COLUMN_USES_UNIQUE.getCleanName());
			if (invited == 0) {
				rs.close();
				return;
			}
			this.invited += invited;
			long invitedId = rs.getLong(DiscordInvite.COLUMN_ID.getCleanName());
			authorsOlympaDiscordId.add(invitedId);
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public DiscordInvitesFullStats(OlympaGuild discordGuild, DiscordMember originalAuthor) {
		this.discordGuild = discordGuild;
		this.originalAuthor = originalAuthor;
		invited = 0;
	}


}
