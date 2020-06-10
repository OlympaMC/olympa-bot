package fr.olympa.bot.discord.guild;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import fr.olympa.bot.OlympaBots;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

public class OlympaGuild {

	public enum DiscordGuildType {
		OTHER("Normal"),
		STAFF("Staff"),
		PUBLIC("Public");

		String name;

		private DiscordGuildType(String name) {
			this.name = name;
		}

		public static DiscordGuildType get(int id) {
			return DiscordGuildType.values()[id];
		}

		public String getName() {
			return name;
		}
	}

	private String name;
	private final long id;
	private long guildId, logChannelId;
	private boolean logVoice, logMsg, logUsername, logAttachment, logRoles, logEntries;
	private DiscordGuildType type;
	private List<Long> excludeChannelsIds;
	
	public static OlympaGuild createObject(ResultSet resultSet) throws JsonSyntaxException, SQLException {
		String excludeChIds = resultSet.getString("exclude_channels_ids");
		List<Long> listExcludeChIds = new ArrayList<>();
		if (excludeChIds != null && !excludeChIds.isBlank())
			listExcludeChIds = new Gson().fromJson(excludeChIds, new TypeToken<List<Long>>() {
			}.getType());
		return new OlympaGuild(
				resultSet.getLong("id"),
				resultSet.getLong("guild_id"),
				resultSet.getString("guild_name"),
				resultSet.getInt("log_voice"),
				resultSet.getInt("log_msg"),
				resultSet.getInt("log_username"),
				resultSet.getInt("log_attachment"),
				resultSet.getInt("log_roles"),
				resultSet.getInt("log_entries"),
				resultSet.getLong("log_channel_id"),
				listExcludeChIds,
				resultSet.getInt("guild_type"));
	}

	public OlympaGuild(long id, long guildId, String guildName, int logVoice, int logMsg, int logUsername, int logAttachment, int logRoles, int logEntries, long logChannelId, List<Long> excludeChannelsIds, int type) {
		this.id = id;
		this.guildId = guildId;
		name = guildName;
		this.logVoice = logVoice == 1;
		this.logMsg = logMsg == 1;
		this.logUsername = logUsername == 1;
		this.logAttachment = logAttachment == 1;
		this.logRoles = logRoles == 1;
		this.logEntries = logEntries == 1;
		this.logChannelId = logChannelId;
		this.excludeChannelsIds = excludeChannelsIds;
		this.type = DiscordGuildType.get(type);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public long getGuildId() {
		return guildId;
	}
	
	public void setGuildId(long guildId) {
		this.guildId = guildId;
	}
	
	public long getLogChannelId() {
		return logChannelId;
	}

	private JDA getJda() {
		return OlympaBots.getInstance().getDiscord().getJda();
	}
	
	public Guild getGuild() {
		return getJda().getGuildById(guildId);
	}

	public void setLogChannelId(long logChannelId) {
		this.logChannelId = logChannelId;
	}
	
	public boolean isLogVoice() {
		return logVoice;
	}
	
	public void setLogVoice(boolean logVoice) {
		this.logVoice = logVoice;
	}
	
	public boolean isLogMsg() {
		return logMsg;
	}
	
	public void setLogMsg(boolean logMsg) {
		this.logMsg = logMsg;
	}
	
	public boolean isLogUsername() {
		return logUsername;
	}
	
	public void setLogUsername(boolean logUsername) {
		this.logUsername = logUsername;
	}
	
	public boolean isLogAttachment() {
		return logAttachment;
	}
	
	public void setLogAttachment(boolean logAttachment) {
		this.logAttachment = logAttachment;
	}
	
	public boolean isLogRoles() {
		return logRoles;
	}
	
	public void setLogRoles(boolean logRoles) {
		this.logRoles = logRoles;
	}
	
	public boolean isLogEntries() {
		return logEntries;
	}
	
	public void setLogEntries(boolean logEntries) {
		this.logEntries = logEntries;
	}
	
	public DiscordGuildType getType() {
		return type;
	}
	
	public void setType(DiscordGuildType type) {
		this.type = type;
	}
	
	public long getId() {
		return id;
	}
	
	public List<Long> getExcludeChannelsIds() {
		return excludeChannelsIds;
	}

}
