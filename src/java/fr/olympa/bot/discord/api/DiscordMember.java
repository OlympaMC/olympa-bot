package fr.olympa.bot.discord.api;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.dv8tion.jda.api.entities.User;

public class DiscordMember {

	long id;
	final long discordId;
	long olympaId;
	String name;

	public static DiscordMember createObject(ResultSet resultSet) throws SQLException {
		return new DiscordMember(resultSet.getLong("id"),
				resultSet.getLong("discord_id"),
				resultSet.getLong("olympa_id"),
				resultSet.getString("discord_name"));
	}

	public DiscordMember(long id, long discordId, long olympaId, String name) {
		this.id = id;
		this.discordId = discordId;
		this.olympaId = olympaId;
		this.name = name;
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
}
