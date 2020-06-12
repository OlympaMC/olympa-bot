package fr.olympa.bot.discord.member;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import fr.olympa.api.utils.Utils;
import net.dv8tion.jda.api.entities.User;

public class DiscordMember {
	
	long id;
	final long discordId;
	long olympaId;
	String name;
	double xp;
	long lastSeen;
	
	public static DiscordMember createObject(ResultSet resultSet) throws SQLException {
		return new DiscordMember(resultSet.getLong("id"),
				resultSet.getLong("discord_id"),
				resultSet.getLong("olympa_id"),
				resultSet.getString("discord_name"),
				resultSet.getDouble("xp"),
				resultSet.getTimestamp("last_seen"));
	}
	
	public DiscordMember(long id, long discordId, long olympaId, String name, double xp, Timestamp lastSeen) {
		this.id = id;
		this.discordId = discordId;
		this.olympaId = olympaId;
		this.name = name;
		this.xp = xp;
		if (lastSeen != null)
			this.lastSeen = lastSeen.getTime() / 1000L;
	}
	
	public DiscordMember(User user) {
		discordId = user.getIdLong();
		name = user.getName();
	}

	public long getDiscordId() {
		return discordId;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public long getOlympaId() {
		return olympaId;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOlympaId(long olympaId) {
		this.olympaId = olympaId;
	}
	
	public double getXp() {
		return xp;
	}
	
	public long getLastSeen() {
		return lastSeen;
	}

	public long getLastSeenTime() {
		return lastSeen != 0 ? Utils.getCurrentTimeInSeconds() - lastSeen : 0;
	}
	
	public void updateLastSeen() {
		lastSeen = Utils.getCurrentTimeInSeconds();
	}
}
