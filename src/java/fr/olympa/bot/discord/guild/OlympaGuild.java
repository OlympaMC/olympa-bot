package fr.olympa.bot.discord.guild;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import fr.olympa.bot.OlympaBots;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class OlympaGuild {

	public enum DiscordGuildType {
		OTHER("Normal"),
		STAFF("Staff"),
		PUBLIC("Public");

		String name;

		DiscordGuildType(String name) {
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
	private final long id, discordId;
	private long logChannelId, staffChannelId, bugsChannelId, minecraftChannelId;
	private boolean logVoice, logMsg, logUsername, logAttachment, logRoles, logEntries, logInsult, statusMessageEnabled;
	private DiscordGuildType type;
	private List<Long> excludeChannelsIds;

	private boolean isCacheComplete;

	public static OlympaGuild createObject(ResultSet resultSet) throws JsonSyntaxException, SQLException {
		String excludeChIds = resultSet.getString("exclude_channels_ids");
		List<Long> listExcludeChIds = new ArrayList<>();
		if (excludeChIds != null && !excludeChIds.isBlank())
			listExcludeChIds = new Gson().fromJson(excludeChIds, new TypeToken<List<Long>>() {}.getType());
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
				resultSet.getInt("log_insult"),
				resultSet.getInt("status_message_enabled"),
				resultSet.getLong("log_channel_id"),
				resultSet.getLong("staff_channel_id"),
				resultSet.getLong("bugs_channel_id"),
				resultSet.getLong("minecraft_channel_id"),
				listExcludeChIds,
				resultSet.getInt("guild_type"));
	}

	public long getStaffChannelId() {
		return staffChannelId;
	}

	public long getBugsChannelId() {
		return bugsChannelId;
	}

	public OlympaGuild(long id, long discordId, String guildName, int logVoice, int logMsg, int logUsername, int logAttachment, int logRoles, int logEntries, int logInsult, int statusMessageEnabled,
			long logChannelId, long staffChannelId, long bugsChannelId, long minecraftChannelId,
			List<Long> excludeChannelsIds, int type) {
		this.id = id;
		this.discordId = discordId;
		name = guildName;
		this.logVoice = logVoice == 1;
		this.logMsg = logMsg == 1;
		this.logUsername = logUsername == 1;
		this.logAttachment = logAttachment == 1;
		this.logRoles = logRoles == 1;
		this.logEntries = logEntries == 1;
		this.logInsult = logInsult == 1;
		this.statusMessageEnabled = statusMessageEnabled == 1;
		this.logChannelId = logChannelId;
		this.staffChannelId = staffChannelId;
		this.bugsChannelId = bugsChannelId;
		this.minecraftChannelId = minecraftChannelId;
		this.excludeChannelsIds = excludeChannelsIds;
		this.type = DiscordGuildType.get(type);
		isCacheComplete = false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getDiscordId() {
		return discordId;
	}

	public long getLogChannelId() {
		return logChannelId;
	}

	@Nullable
	private JDA getJda() {
		return OlympaBots.getInstance().getDiscord().getJda();
	}

	@Nullable
	public Guild getGuild() {
		JDA jda = getJda();
		if (jda == null)
			return null;
		return jda.getGuildById(discordId);
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

	public boolean isOlympaDiscord() {
		return type == DiscordGuildType.PUBLIC || type == DiscordGuildType.STAFF;
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

	@Nullable
	public TextChannel getLogChannel() {
		JDA jda = getJda();
		if (jda == null)
			return null;
		return jda.getTextChannelById(logChannelId);
	}

	@Nullable
	public TextChannel getStaffChannel() {
		JDA jda = getJda();
		if (jda == null)
			return null;
		return jda.getTextChannelById(staffChannelId);
	}

	@Nullable
	public TextChannel getBugsChannel() {
		JDA jda = getJda();
		if (jda == null)
			return null;
		return jda.getTextChannelById(bugsChannelId);
	}

	@Nullable
	public TextChannel getMinecraftChannel() {
		JDA jda = getJda();
		if (jda == null)
			return null;
		return jda.getTextChannelById(minecraftChannelId);
	}

	public boolean isLogInsult() {
		return logInsult;
	}

	public boolean isStatusMessageEnabled() {
		return statusMessageEnabled;
	}

	public long getMinecraftChannelId() {
		return minecraftChannelId;
	}

	//	public boolean isCacheComplete() {
	//		return isCacheComplete;
	//	}

	public void cacheComplete() {
		isCacheComplete = true;
	}

	public void cacheIncomplete() {
		isCacheComplete = false;
	}
}
