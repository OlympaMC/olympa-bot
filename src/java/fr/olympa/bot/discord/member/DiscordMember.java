package fr.olympa.bot.discord.member;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import fr.olympa.api.utils.Matcher;
import fr.olympa.api.utils.Utils;
import fr.olympa.api.utils.UtilsCore;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
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
	TreeMap<Long, String> oldNames = new TreeMap<>(Comparator.comparing(Long::longValue).reversed());
	Map<DiscordPermission, Long> permissionsOlympaDiscordGuildId = new HashMap<>();

	public long getJoinTime() {
		return joinTime;
	}

	public long getLeaveTime() {
		return leaveTime;
	}

	public TreeMap<Long, String> getOldNames() {
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
				resultSet.getString("old_names"),
				resultSet.getString("permissions"));
	}

	public DiscordMember(long id, long discordId, long olympaId, String name, String tag, double xp, Timestamp lastSeen, Date joinDate, Date leaveDate, String oldNames, String permissions) {
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
			this.oldNames.putAll(new Gson().fromJson(oldNames, new TypeToken<Map<Long, String>>() {
			}.getType()));
		if (permissions != null)
			permissionsOlympaDiscordGuildId.putAll(new Gson().fromJson(oldNames, new TypeToken<Map<DiscordPermission, Long>>() {
			}.getType()));
	}

	public DiscordMember(Member member) {
		discordId = member.getIdLong();
		joinTime = member.getTimeJoined().toEpochSecond();
		updateName(member.getUser());
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
		if (name != null && !newName.equals(name)) {
			oldNames.put(Utils.getCurrentTimeInSeconds(), name);
			Iterator<Entry<Long, String>> it = oldNames.entrySet().iterator();
			it.next();
			while (it.hasNext() && oldNames.size() > 10)
				oldNames.remove(it.next().getKey(), it.next().getValue());
			name = newName;
		} else
			name = newName;
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

	public Map<DiscordPermission, Long> getPermissions() {
		return permissionsOlympaDiscordGuildId;
	}

	public Map<DiscordPermission, OlympaGuild> getPermissionsWithOlympaGuild() {
		Map<DiscordPermission, OlympaGuild> permissions = new HashMap<>();
		permissionsOlympaDiscordGuildId.entrySet().forEach(action -> {
			if (action.getValue() != null)
				permissions.put(action.getKey(), GuildHandler.getOlympaGuildById(action.getValue()));
			else
				permissions.put(action.getKey(), null);
		});
		return permissions;
	}

	public boolean addPermission(DiscordPermission permission, Guild guild) {
		if (guild == null) {
			addPermission(permission, (OlympaGuild) null);
			return true;
		}
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
		if (olympaGuild != null) {
			addPermission(permission, olympaGuild);
			return true;
		}
		return false;
	}

	public void removePermission(DiscordPermission permission) {
		permissionsOlympaDiscordGuildId.remove(permission);
	}

	public void addPermission(DiscordPermission permission, OlympaGuild olympaGuild) {
		Long guildId;
		if (olympaGuild != null)
			guildId = olympaGuild.getId();
		else
			guildId = null;
		permissionsOlympaDiscordGuildId.put(permission, guildId);
	}

	public boolean hasPermission(DiscordPermission permission, Guild guild) {
		if (permissionsOlympaDiscordGuildId.containsKey(permission)) {
			OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
			if (olympaGuild != null) {
				Long guildId = permissionsOlympaDiscordGuildId.get(permission);
				return guildId == null ? true : olympaGuild.getId() == permissionsOlympaDiscordGuildId.get(permission);
			}
		}
		return false;
	}
}
