package fr.olympa.bot.discord.member;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import fr.olympa.api.utils.Matcher;
import fr.olympa.api.utils.Utils;
import fr.olympa.api.utils.UtilsCore;
import fr.olympa.bot.OlympaBots;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class DiscordMember {

	long id;
	final long discordId;
	long olympaId;
	String name;
	String tag;
	double xp;
	long lastSeen = -1;
	long joinTime;
	long leaveTime;
	Map<Long, String> oldNames = new HashMap<>();

	public long getJoinTime() {
		return joinTime;
	}

	public long getLeaveTime() {
		return leaveTime;
	}

	public Map<Long, String> getOldNames() {
		return oldNames;
	}

	public static List<Member> get(Guild guild, String name, List<Member> mentionned) {
		if (mentionned != null && !mentionned.isEmpty())
			return mentionned;
		List<Member> targets = guild.getMembersByEffectiveName(name, true);
		if (targets.isEmpty())
			targets = guild.getMembersByName(name, true);
		if (targets.isEmpty() && Matcher.isDiscordTag(name))
			targets = Arrays.asList(guild.getMemberByTag(name));
		if (targets.isEmpty() && Matcher.isInt(name))
			targets = Arrays.asList(guild.getMemberById(name));
		if (targets.isEmpty())
			targets = UtilsCore.similarWords(name, guild.getMembers().stream().map(Member::getEffectiveName)
					.collect(Collectors.toSet())).stream()
					.map(n -> guild.getMembersByEffectiveName(n, false)).filter(m -> !m.isEmpty()).map(m -> m.get(0))
					.collect(Collectors.toList());
		return targets;
	}

	public static DiscordMember createObject(ResultSet resultSet) throws SQLException {
		return new DiscordMember(resultSet.getLong("id"),
				resultSet.getLong("discord_id"),
				resultSet.getLong("olympa_id"),
				resultSet.getString("discord_name"),
				resultSet.getString("discord_tag"),
				resultSet.getDouble("xp"),
				resultSet.getTimestamp("last_seen"),
				resultSet.getDate("join_date"),
				resultSet.getDate("leave_date"),
				resultSet.getString("old_names"));
	}

	public DiscordMember(long id, long discordId, long olympaId, String name, String tag, double xp, Timestamp lastSeen, Date joinDate, Date leaveDate, String oldNames) {
		this.id = id;
		this.discordId = discordId;
		this.olympaId = olympaId;
		this.name = name;
		this.tag = tag;
		this.xp = xp;
		if (lastSeen != null)
			this.lastSeen = lastSeen.getTime() / 1000L;
		if (joinDate != null)
			joinTime = joinDate.getTime() / 1000L;
		if (leaveDate != null)
			leaveTime = leaveDate.getTime() / 1000L;
		if (oldNames != null)
			this.oldNames = new Gson().fromJson(oldNames, new TypeToken<Map<Long, String>>() {
			}.getType());
	}

	public DiscordMember(Member member) {
		discordId = member.getIdLong();
		updateName(member.getUser());
	}

	public DiscordMember(User user) {
		discordId = user.getIdLong();
		updateName(user);
	}

	private JDA getJDA() {
		return OlympaBots.getInstance().getDiscord().getJda();
	}

	public User getUser() {
		return getJDA().getUserById(discordId);
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

	public void updateName(User user) {
		java.util.regex.Matcher matcher = User.USER_TAG.matcher(user.getAsTag());
		if (!matcher.find())
			return;
		String newName = matcher.group(1);
		if (!newName.equals(name)) {
			oldNames.put(Utils.getCurrentTimeInSeconds(), name);
			name = newName;
		}
		tag = matcher.group(2);
	}

	public void setOlympaId(long olympaId) {
		this.olympaId = olympaId;
	}

	public String getTag() {
		return tag;
	}

	public double getXp() {
		return xp;
	}

	public long getLastSeen() {
		return lastSeen;
	}

	public long getLastSeenTime() {
		return lastSeen == -1 ? -1 : Utils.getCurrentTimeInSeconds() - lastSeen;
	}

	public void updateLastSeen() {
		lastSeen = Utils.getCurrentTimeInSeconds();
	}

	public void updateJoinTime(long joinTime) {
		this.joinTime = joinTime;
	}

	public void updateLeaveTime(long leaveTime) {
		this.leaveTime = leaveTime;
	}
}
