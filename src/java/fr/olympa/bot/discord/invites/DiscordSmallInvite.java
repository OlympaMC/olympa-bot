package fr.olympa.bot.discord.invites;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import net.dv8tion.jda.api.entities.Invite;

public class DiscordSmallInvite {

	OlympaGuild guild;
	int uses;
	String code;

	public DiscordSmallInvite(Invite invite) {
		guild = GuildHandler.getOlympaGuildByDiscordId(invite.getGuild().getIdLong());
		uses = invite.getUses();
		code = invite.getCode();
	}

	DiscordSmallInvite(ResultSet rs) {
		try {
			guild = GuildHandler.getOlympaGuildById(rs.getInt(DiscordInvite.COLUMN_OLYMPA_GUILD_ID.getCleanName()));
			uses = rs.getInt(DiscordInvite.COLUMN_USES.getCleanName());
			code = rs.getString(DiscordInvite.COLUMN_CODE.getCleanName());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	DiscordSmallInvite(OlympaGuild guild, ResultSet rs) {
		this.guild = guild;
		try {
			uses = rs.getInt(DiscordInvite.COLUMN_USES.getCleanName());
			code = rs.getString(DiscordInvite.COLUMN_CODE.getCleanName());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public int getUses() {
		return uses;
	}

	public String getCode() {
		return code;
	}

	public String getUrl() {
		return "https://discord.gg/" + code;
	}

	public void isTheNewOne() {
		getInvite(x -> x.getUses());
	}

	public void getInvite(Consumer<Invite> successCallback) {
		guild.getGuild().retrieveInvites().queue(invs -> {
			Invite invite = invs.stream().filter(iv -> iv.getCode().equals(code)).findFirst().orElse(null);
			if (invite != null)
				successCallback.accept(invite);
		});
	}

}
