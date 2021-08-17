package fr.olympa.bot.discord.member;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import fr.olympa.api.common.match.RegexMatcher;
import fr.olympa.api.common.observable.ObservableBoolean;
import fr.olympa.api.common.observable.ObservableDouble;
import fr.olympa.api.common.observable.ObservableLong;
import fr.olympa.api.common.observable.ObservableMap;
import fr.olympa.api.common.sql.SQLColumn;
import fr.olympa.api.common.sql.SQLTable;
import fr.olympa.api.common.task.NativeTask;
import fr.olympa.api.utils.Utils;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class DiscordMember {

	static final SQLColumn<DiscordMember> COLUMN_ID = new SQLColumn<DiscordMember>("id", "INT(10) UNSIGNED NOT NULL AUTO_INCREMENT", Types.INTEGER).setPrimaryKey(DiscordMember::getId);
	static final SQLColumn<DiscordMember> COLUMN_DISCORD_ID = new SQLColumn<DiscordMember>("discord_id", "BIGINT(20) UNSIGNED NOT NULL DEFAULT '0'", Types.BIGINT).setNotDefault();
	static final SQLColumn<DiscordMember> COLUMN_DISCORD_NAME = new SQLColumn<DiscordMember>("discord_name", "VARCHAR(32) NOT NULL COLLATE 'utf8mb4_general_ci'", Types.VARCHAR).setNotDefault();
	static final SQLColumn<DiscordMember> COLUMN_DISCORD_TAG = new SQLColumn<DiscordMember>("discord_tag", "VARCHAR(4) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci'", Types.VARCHAR).setNotDefault();
	static final SQLColumn<DiscordMember> COLUMN_OLYMPA_ID = new SQLColumn<DiscordMember>("olympa_id", "INT(10) UNSIGNED NULL DEFAULT NULL", Types.INTEGER).setUpdatable().allowNull();
	static final SQLColumn<DiscordMember> COLUMN_XP = new SQLColumn<DiscordMember>("xp", "DOUBLE(11,0) NOT NULL DEFAULT '0'", Types.DOUBLE).setUpdatable().allowNull();
	static final SQLColumn<DiscordMember> COLUMN_LAST_SEEN = new SQLColumn<DiscordMember>("last_seen", "TIMESTAMP NULL DEFAULT NULL", Types.TIMESTAMP).setUpdatable().allowNull();
	static final SQLColumn<DiscordMember> COLUMN_JOIN_DATE = new SQLColumn<DiscordMember>("join_date", "DATE NULL DEFAULT NULL", Types.DATE).setUpdatable().allowNull();
	static final SQLColumn<DiscordMember> COLUMN_LEAVE_DATE = new SQLColumn<DiscordMember>("leave_date", "DATE NULL DEFAULT NULL", Types.DATE).setUpdatable().allowNull();
	static final SQLColumn<DiscordMember> COLUMN_OLD_NAMES = new SQLColumn<DiscordMember>("old_names", "TEXT NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci'", Types.VARCHAR).setUpdatable().allowNull();
	static final SQLColumn<DiscordMember> COLUMN_PERMISSIONS = new SQLColumn<DiscordMember>("permissions", "TEXT NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci'", Types.VARCHAR).setUpdatable().allowNull();
	static final SQLColumn<DiscordMember> COLUMN_SETTINGS = new SQLColumn<DiscordMember>("settings", "MEDIUMTEXT NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci'", Types.VARCHAR).setUpdatable().allowNull();

	static final List<SQLColumn<DiscordMember>> COLUMNS = Arrays.asList(COLUMN_ID, COLUMN_DISCORD_ID, COLUMN_DISCORD_NAME, COLUMN_DISCORD_TAG, COLUMN_OLYMPA_ID, COLUMN_XP,
			COLUMN_LAST_SEEN, COLUMN_JOIN_DATE, COLUMN_LEAVE_DATE, COLUMN_LEAVE_DATE, COLUMN_OLD_NAMES, COLUMN_PERMISSIONS, COLUMN_SETTINGS);
	static SQLTable<DiscordMember> table = new SQLTable<>("discord.members", COLUMNS, DiscordMember::createObject);

	long id;
	final long discordId;
	ObservableLong olympaId = new ObservableLong(0);
	String name;
	String tag;
	ObservableDouble xp = new ObservableDouble(0);
	ObservableLong lastSeen = new ObservableLong(0);
	ObservableLong joinTime = new ObservableLong(0);
	ObservableLong leaveTime = new ObservableLong(0);
	ObservableMap<Long, String> oldNames = new ObservableMap<>(new TreeMap<>(Comparator.comparing(Long::longValue).reversed()));
	ObservableMap<DiscordPermission, Long> permissionsOlympaDiscordGuildId = new ObservableMap<>(new HashMap<>());
	ObservableMap<MemberSettings, ObservableBoolean> settings = new ObservableMap<>(new HashMap<>());

	Map<SQLColumn<DiscordMember>, Object> updateQueueSQL = new HashMap<>();

	//	SortedMap<Long, String> oldNames = new TreeMap<>(Comparator.comparing(Long::longValue).reversed());
	//	Map<DiscordPermission, Long> permissionsOlympaDiscordGuildId = new HashMap<>();
	//	Map<MemberSettings,Boolean>settings=new HashMap<>();

	public void addUpdateQueue(SQLColumn<DiscordMember> column, Object newValue) {
		updateQueueSQL.put(column, newValue);
		if (!COLUMN_LAST_SEEN.equals(column))
			addTask();
	}

	public boolean cacheNeedToBeSave() {
		return !updateQueueSQL.isEmpty();
	}

	private void addTask() {
		NativeTask.getInstance().runTaskLater(getName() + "_" + id, () -> saveCacheToDb(), 10, TimeUnit.SECONDS);
	}

	private void removeTask() {
		NativeTask.getInstance().cancelTaskByName(getName() + "_" + id);
	}

	public boolean saveCacheToDb() {
		removeTask();
		if (!updateQueueSQL.isEmpty()) {
			Map<SQLColumn<DiscordMember>, Object> toRemoved = new HashMap<>(updateQueueSQL);
			//				OlympaBungee.getInstance().sendMessage("&7[DEBUG] DiscordMember de %s a été sauvegardé en bdd les modifications %s", getAsTag(),
			//						updateQueueSQL.keySet().stream().map(SQLColumn::getCleanName).collect(Collectors.joining(", ")));
			try {
				table.update(this, updateQueueSQL);
				toRemoved.forEach((key, value) -> updateQueueSQL.remove(key, value));
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	public void obverse() {
		olympaId.observe("datas", () -> addUpdateQueue(COLUMN_OLYMPA_ID, olympaId.get() == 0 ? null : olympaId.get()));
		xp.observe("datas", () -> addUpdateQueue(COLUMN_XP, xp.get()));
		lastSeen.observe("datas", () -> addUpdateQueue(COLUMN_LAST_SEEN, lastSeen.get() == 0 ? null : new Timestamp(lastSeen.get() * 1000L)));
		joinTime.observe("datas", () -> addUpdateQueue(COLUMN_JOIN_DATE, joinTime.get() == 0 ? null : new Date(joinTime.get() * 1000L)));
		leaveTime.observe("datas", () -> addUpdateQueue(COLUMN_LEAVE_DATE, leaveTime.get() == 0 ? null : new Date(leaveTime.get() * 1000L)));
		leaveTime.observe("datas", () -> addUpdateQueue(COLUMN_OLD_NAMES, oldNames.toJson()));
		leaveTime.observe("datas", () -> addUpdateQueue(COLUMN_PERMISSIONS, permissionsOlympaDiscordGuildId.toJson()));
		leaveTime.observe("datas", () -> addUpdateQueue(COLUMN_SETTINGS, settings.toJson()));
		//		olympaId.observe("datas", () -> COLUMN_OLYMPA_ID.updateAsync(this, olympaId.get() == -1 ? null : olympaId.get(), null, null));
		//		xp.observe("datas", () -> COLUMN_XP.updateAsync(this, xp.get(), null, null));
		//		lastSeen.observe("datas", () -> COLUMN_LAST_SEEN.updateAsync(this, lastSeen.get() == -1 ? null : lastSeen.get(), null, null));
		//		joinTime.observe("datas", () -> COLUMN_JOIN_DATE.updateAsync(this, joinTime.get() == -1 ? null : joinTime.get(), null, null));
		//		leaveTime.observe("datas", () -> COLUMN_LEAVE_DATE.updateAsync(this, leaveTime.get() == -1 ? null : leaveTime.get(), null, null));
		//		leaveTime.observe("datas", () -> COLUMN_OLD_NAMES.updateAsync(this, oldNames.toJson(), null, null));
		//		leaveTime.observe("datas", () -> COLUMN_PERMISSIONS.updateAsync(this, permissionsOlympaDiscordGuildId.toJson(), null, null));
		//		leaveTime.observe("datas", () -> COLUMN_SETTINGS.updateAsync(this, settings.toJson(), null, null));
	}

	public long getJoinTime() {
		return joinTime.get();
	}

	public long getLeaveTime() {
		return leaveTime.get();
	}

	public NavigableMap<Long, String> getOldNames() {
		return (NavigableMap<Long, String>) oldNames.getSubMap();
	}

	public static List<Member> get(Guild guild, String name, List<Member> mentionned) {
		if (mentionned != null && !mentionned.isEmpty())
			return mentionned;
		List<Member> targets = guild.getMembersByEffectiveName(name, true);
		if (targets.isEmpty())
			targets = guild.getMembersByName(name, true);
		if (targets.isEmpty() && RegexMatcher.DISCORD_TAG.is(name))
			targets = Arrays.asList(guild.getMemberByTag(name));
		if (targets.isEmpty() && RegexMatcher.INT.is(name))
			targets = Arrays.asList(guild.getMemberById(name));
		//		if (targets.isEmpty())
		//			targets = UtilsCore.similarWords(name, guild.getMembers().stream().map(Member::getEffectiveName)
		//					.collect(Collectors.toSet())).stream()
		//					.map(n -> guild.getMembersByEffectiveName(n, false)).filter(m -> !m.isEmpty()).map(m -> m.get(0))
		//					.toList();
		return targets;
	}

	public static DiscordMember createObject(ResultSet resultSet) throws SQLException {
		return new DiscordMember(resultSet.getLong(COLUMN_ID.getCleanName()),
				resultSet.getLong(COLUMN_DISCORD_ID.getCleanName()),
				resultSet.getLong(COLUMN_OLYMPA_ID.getCleanName()),
				resultSet.getString(COLUMN_DISCORD_NAME.getCleanName()),
				resultSet.getString(COLUMN_DISCORD_TAG.getCleanName()),
				resultSet.getDouble(COLUMN_XP.getCleanName()),
				resultSet.getTimestamp(COLUMN_LAST_SEEN.getCleanName()),
				resultSet.getDate(COLUMN_JOIN_DATE.getCleanName()),
				resultSet.getDate(COLUMN_LEAVE_DATE.getCleanName()),
				resultSet.getString(COLUMN_OLD_NAMES.getCleanName()),
				resultSet.getString(COLUMN_PERMISSIONS.getCleanName()),
				resultSet.getString(COLUMN_SETTINGS.getCleanName()));
	}

	private DiscordMember(long id, long discordId, long olympaId, String name, String tag, double xp, Timestamp lastSeen, Date joinDate, Date leaveDate, String oldNames, String permissions, String settings) {
		this.id = id;
		this.discordId = discordId;
		this.olympaId.set(olympaId);
		this.name = name;
		this.tag = tag;
		this.xp.set(xp);
		if (lastSeen != null)
			this.lastSeen.set(lastSeen.getTime() / 1000L);
		if (joinDate != null)
			joinTime.set(joinDate.getTime() / 1000L);
		if (leaveDate != null)
			leaveTime.set(leaveDate.getTime() / 1000L);
		this.oldNames.putAllFromJson(oldNames);
		permissionsOlympaDiscordGuildId.putAllFromJson(permissions);
		this.settings.putAllFromJson(settings);
		obverse();
	}

	public DiscordMember(Member member) {
		joinTime.set(member.getTimeJoined().toEpochSecond());
		discordId = member.getIdLong();
		updateName(member.getUser());
		obverse();
	}

	public DiscordMember(User user) {
		discordId = user.getIdLong();
		updateName(user);
		obverse();
	}

	private JDA getJDA() {
		return OlympaBots.getInstance().getDiscord().getJda();
	}

	@Nullable
	public User getUser() {
		JDA jda = getJDA();
		if (jda == null)
			return null;
		return jda.getUserById(discordId);
	}

	@Nullable
	public Member getMember(Guild guild) {
		return guild.getMemberById(discordId);
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

	public String getTagName() {
		return name + "#" + tag;
	}

	@Nullable
	public Long getOlympaId() {
		long id = olympaId.get();
		if (id == -1)
			return null;
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void updateName(User user) {
		String newName = user.getName();
		String newTag = user.getDiscriminator();
		if (name == null) {
			name = newName;
			addUpdateQueue(COLUMN_DISCORD_NAME, newName);
		} else if (!newName.equals(name)) {
			oldNames.put(Utils.getCurrentTimeInSeconds(), name);
			Set<Long> nameToRemove = new HashSet<>();
			Iterator<Entry<Long, String>> it = oldNames.entrySet().iterator();
			it.next();
			while (it.hasNext() && oldNames.size() - nameToRemove.size() > 10)
				nameToRemove.add(it.next().getKey());
			oldNames.removeAll(nameToRemove);
			name = newName;
			addUpdateQueue(COLUMN_DISCORD_NAME, newName);
		}
		if (tag == null || !newTag.equals(tag)) {
			tag = newTag;
			addUpdateQueue(COLUMN_DISCORD_TAG, newTag);
		}
	}

	public void setOlympaId(long olympaId) {
		this.olympaId.set(olympaId);
	}

	public String getTag() {
		if (tag == null)
			updateName(getUser());
		return tag;
	}

	public double getXp() {
		return xp.get();
	}

	public Long getLastSeen() {
		long i = lastSeen.get();
		if (i == -1)
			return null;
		return lastSeen.get();
	}

	@Nullable
	public Long getLastSeenTime() {
		return lastSeen.get() == -1 ? null : Utils.getCurrentTimeInSeconds() - lastSeen.get();
	}

	public void updateLastSeen() {
		lastSeen.set(Utils.getCurrentTimeInSeconds());
	}

	public void updateJoinTime(long joinTime) {
		this.joinTime.set(joinTime);
	}

	public void updateLeaveTime(long leaveTime) {
		this.leaveTime.set(leaveTime);
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

	public String getAsMention() {
		return "<@" + discordId + ">";
	}

	public String getAsTag() {
		return name + "#" + tag;
	}

	@NotNull
	public Map<MemberSettings, ObservableBoolean> getSettings() {
		return settings.getSubMap();
	}

	public boolean hasSettingEnable(MemberSettings setting) {
		if (settings.containsKey(setting))
			return settings.get(setting).get();
		else
			return setting.getDefault();
	}

	public boolean toggleSetting(MemberSettings setting) {
		ObservableBoolean state = settings.get(setting);
		if (state == null) {
			state = new ObservableBoolean(!setting.getDefault());
			settings.put(setting, state);
		} else if (state.get() == setting.getDefault())
			state.set(!setting.getDefault());
		else
			settings.remove(setting);
		return !state.get();
	}

}
