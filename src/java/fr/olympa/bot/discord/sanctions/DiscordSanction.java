package fr.olympa.bot.discord.sanctions;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DiscordSanction {

	int id;
	long targetOlympaDiscordId, authorOlympaDiscordId;
	DiscordSanctionType type;
	String reason;
	long created, expire;

	public static DiscordSanction createObject(ResultSet resultSet) throws SQLException {
		return new DiscordSanction(
				resultSet.getInt("id"),
				resultSet.getLong("target_id"),
				resultSet.getLong("author_id"),
				DiscordSanctionType.get(resultSet.getInt("type")),
				resultSet.getString("reason"),
				resultSet.getTimestamp("created").getTime() / 1000L,
				resultSet.getTimestamp("expire").getTime() / 1000L);
	}

	public DiscordSanction(int id, long targetOlympaDiscordId, long authorOlympaDiscordId, DiscordSanctionType type, String reason, long created, long expire) {
		this.id = id;
		this.targetOlympaDiscordId = targetOlympaDiscordId;
		this.authorOlympaDiscordId = authorOlympaDiscordId;
		this.type = type;
		this.reason = reason;
		this.created = created;
		this.expire = expire;
	}

	public DiscordSanction(long targetOlympaDiscordId, long authorOlympaDiscordId, DiscordSanctionType type, String reason, Long expire) {
		this.targetOlympaDiscordId = targetOlympaDiscordId;
		this.authorOlympaDiscordId = authorOlympaDiscordId;
		this.type = type;
		this.reason = reason;
		this.expire = expire;
	}

	public int getId() {
		return id;
	}

	public long getTargetOlympaDiscordId() {
		return targetOlympaDiscordId;
	}

	public long getAuthorOlympaDiscordId() {
		return authorOlympaDiscordId;
	}

	public DiscordSanctionType getType() {
		return type;
	}

	public String getReason() {
		return reason;
	}

	public long getCreated() {
		return created;
	}

	public Long getExpire() {
		return expire;
	}

}
