package fr.olympa.bot.discord.invites;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.match.RegexMatcher;
import fr.olympa.api.sql.SQLColumn;
import fr.olympa.api.sql.SQLNullObject;
import fr.olympa.api.sql.SQLTable;
import fr.olympa.api.sql.statement.OlympaStatement;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import net.dv8tion.jda.api.entities.Invite;

public class DiscordInvite extends DiscordSmallInvite {

	static final SQLColumn<DiscordInvite> COLUMN_ID = new SQLColumn<DiscordInvite>("id", "INT(10) UNSIGNED NOT NULL AUTO_INCREMENT", Types.INTEGER).setPrimaryKey(DiscordInvite::getId);
	static final SQLColumn<DiscordInvite> COLUMN_OLYMPA_GUILD_ID = new SQLColumn<DiscordInvite>("olympa_guild_id", "INT(10) UNSIGNED NOT NULL", Types.INTEGER).setNotDefault();
	static final SQLColumn<DiscordInvite> COLUMN_OLYMPA_DISCORD_ID = new SQLColumn<DiscordInvite>("olympa_discord_id", "INT(10) UNSIGNED NOT NULL", Types.INTEGER).setNotDefault();
	static final SQLColumn<DiscordInvite> COLUMN_USES = new SQLColumn<DiscordInvite>("uses", "INT(10) UNSIGNED NULL NOT NULL", Types.INTEGER).setUpdatable().setNotDefault();
	static final SQLColumn<DiscordInvite> COLUMN_USES_UNIQUE = new SQLColumn<DiscordInvite>("uses_unique", "INT(10) UNSIGNED NULL DEFAULT '0'", Types.INTEGER).setUpdatable();
	static final SQLColumn<DiscordInvite> COLUMN_USES_LEAVER = new SQLColumn<DiscordInvite>("uses_leaver", "INT(10) UNSIGNED NULL DEFAULT '0'", Types.INTEGER).setUpdatable();
	static final SQLColumn<DiscordInvite> COLUMN_CREATED = new SQLColumn<DiscordInvite>("created", "TIMESTAMP NOT NULL DEFAULT current_timestamp()", Types.TIMESTAMP).setNotDefault();
	static final SQLColumn<DiscordInvite> COLUMN_CODE = new SQLColumn<DiscordInvite>("code", "VARCHAR(7) NOT NULL COLLATE 'utf8mb4_general_ci'", Types.VARCHAR).setNotDefault();
	static final SQLColumn<DiscordInvite> COLUMN_USERS_OLYMPA_DISCORD_ID = new SQLColumn<DiscordInvite>("users_olympa_discord_id", "MEDIUMTEXT NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci'", Types.VARCHAR).setUpdatable();
	static final SQLColumn<DiscordInvite> COLUMN_USERS_PAST_OLYMPA_DISCORD_ID = new SQLColumn<DiscordInvite>("users_olympa_past_discord_id", "MEDIUMTEXT NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci'", Types.VARCHAR).setUpdatable();
	static final SQLColumn<DiscordInvite> COLUMN_USERS_LEAVER_OLYMPA_DISCORD_ID = new SQLColumn<DiscordInvite>("users_olympa_leaver_discord_id", "MEDIUMTEXT NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci'", Types.VARCHAR).setUpdatable();
	static final SQLColumn<DiscordInvite> COLUMN_DELETED = new SQLColumn<DiscordInvite>("deleted", "TINYINT(1) UNSIGNED NULL DEFAULT '0'", Types.BOOLEAN).setUpdatable();

	static final List<SQLColumn<DiscordInvite>> COLUMNS = Arrays.asList(COLUMN_ID, COLUMN_OLYMPA_GUILD_ID, COLUMN_OLYMPA_DISCORD_ID, COLUMN_USES, COLUMN_USES_UNIQUE, COLUMN_USES_LEAVER, COLUMN_CREATED, COLUMN_CODE,
			COLUMN_USERS_OLYMPA_DISCORD_ID, COLUMN_USERS_PAST_OLYMPA_DISCORD_ID, COLUMN_USERS_LEAVER_OLYMPA_DISCORD_ID, COLUMN_DELETED);
	private static SQLTable<DiscordInvite> inviteTable;

	static {
		try {
			inviteTable = new SQLTable<>("discord.invites", COLUMNS, t -> {
				try {
					return new DiscordInvite(t);
				} catch (IllegalArgumentException | SQLException e) {
					e.printStackTrace();
				}
				return null;
			}).createOrAlter();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected static DiscordInvite getByCode(String code) throws SQLException, IllegalAccessException {
		List<DiscordInvite> result = COLUMN_CODE.select(code);
		if (!result.isEmpty())
			return result.get(0);
		return null;
	}

	public static List<DiscordInvite> getAll(OlympaGuild opGuild) throws SQLException, IllegalAccessException {
		return COLUMN_OLYMPA_DISCORD_ID.select(opGuild.getId());
	}

	protected static List<DiscordInvite> getByUser(SQLColumn<DiscordInvite> column, DiscordMember dm, OlympaGuild opGuild) {
		List<DiscordInvite> dis = new ArrayList<>();
		OlympaStatement getUsers = new OlympaStatement("SELECT * FROM " + inviteTable.getName() + " WHERE " + column.getName() + " REGEXP ? AND " + COLUMN_OLYMPA_GUILD_ID.getName() + " = ?");
		try {
			PreparedStatement statement = getUsers.getStatement();
			int i = 1;
			statement.setString(i++, String.format("\\b(%d)\\b", dm.getId()));
			statement.setLong(i, opGuild.getId());
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next())
				dis.add(inviteTable.initializeFromRow.apply(resultSet));
			resultSet.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return dis;
	}

	static List<DiscordInvite> getByOlympaDiscordId(DiscordMember discordMember) throws SQLException, IllegalAccessException {
		return COLUMN_OLYMPA_DISCORD_ID.select(discordMember.getId());
	}

	public DiscordInvite createNew() throws SQLException {
		ResultSet resultSet = inviteTable.insert(
				getGuild().getId(),
				getAuthorId(),
				getUses(),
				new Timestamp(getCreated() * 1000L),
				getCode());
		resultSet.next();
		id = resultSet.getInt("id");
		resultSet.close();
		isUpWithDb = true;
		return this;
	}

	public void update() throws SQLException {
		if (isUpWithDb)
			return;
		inviteTable.updateAsync(this, Map.of(COLUMN_USES, getUses(), COLUMN_USERS_OLYMPA_DISCORD_ID, getUsersToDB(), COLUMN_USERS_PAST_OLYMPA_DISCORD_ID, getPastUsersToDB(), COLUMN_USES_LEAVER, getUsesLeaver(), COLUMN_DELETED, deleted,
				COLUMN_USES_UNIQUE, usesUnique), null, null);
		isUpWithDb = true;
	}

	public void update(Invite invite) throws SQLException {
		int inviteUses = invite.getUses();
		if (inviteUses != uses) {
			LinkSpigotBungee.Provider.link.sendMessage("&e[DISCORD INVITE] &cL'invitation %s par %s n'était pas à jour dans la bdd ...", invite.getCode(), invite.getInviter().getAsTag());
			isUpWithDb = false;
			uses = invite.getUses();
			update();
		}
	}

	int id;
	long authorId;
	int usesLeaver;
	int usesUnique;
	long created;
	boolean deleted = false;
	boolean isUpWithDb = false;
	List<Long> usersIds = new ArrayList<>();
	List<Long> pastUsersIds = new ArrayList<>();
	List<Long> leaveUsersIds = new ArrayList<>();

	public DiscordInvite(Invite invite) {
		super(invite);
		try {
			authorId = CacheDiscordSQL.getDiscordMemberAndCreateIfNotExist(invite.getInviter()).getId();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		uses = invite.getUses();
		usesLeaver = 0;
		usesUnique = 0;
		created = invite.getTimeCreated().toEpochSecond();
		InvitesHandler.addInvite(this);
	}

	private DiscordInvite(ResultSet rs) throws SQLException {
		super(rs);
		id = rs.getInt(COLUMN_ID.getCleanName());
		authorId = rs.getInt(COLUMN_OLYMPA_GUILD_ID.getCleanName());
		uses = rs.getInt(COLUMN_USES.getCleanName());
		usesUnique = rs.getInt(COLUMN_USES_UNIQUE.getCleanName());
		usesLeaver = rs.getInt(COLUMN_USES_LEAVER.getCleanName());
		created = rs.getTimestamp(COLUMN_CREATED.getCleanName()).getTime() / 1000L;
		code = rs.getString(COLUMN_CODE.getCleanName());
		deleted = rs.getBoolean(COLUMN_DELETED.getCleanName());
		usersIds = stringToListUsersIds(rs.getString(COLUMN_USERS_OLYMPA_DISCORD_ID.getCleanName()), usersIds);
		pastUsersIds = stringToListUsersIds(rs.getString(COLUMN_USERS_PAST_OLYMPA_DISCORD_ID.getCleanName()), pastUsersIds);
		leaveUsersIds = stringToListUsersIds(rs.getString(COLUMN_USERS_LEAVER_OLYMPA_DISCORD_ID.getCleanName()), leaveUsersIds);
		isUpWithDb = true;
		InvitesHandler.addInvite(this);
	}

	public void removeLeaver(DiscordMember member) {
		if (listIdsContainsUser(leaveUsersIds, member)) {
			usesLeaver--;
			leaveUsersIds.remove(member.getId());
			isUpWithDb = false;
		}
	}

	public void addUser(DiscordMember member) {
		uses++;
		if (!usersIds.contains(member.getId()))
			usersIds.add(member.getId());
		removeLeaver(member);
		if (!listIdsContainsUser(pastUsersIds, member)) {
			usesUnique++;
			pastUsersIds.add(member.getId());
		}
		isUpWithDb = false;
	}

	public void removeUser(DiscordMember member) {
		usesLeaver++;
		usersIds.remove(member.getId());
		if (!listIdsContainsUser(leaveUsersIds, member))
			leaveUsersIds.add(member.getId());
		isUpWithDb = false;
	}

	public void delete() {
		deleted = true;
		isUpWithDb = false;
	}

	public int getId() {
		return id;
	}

	public OlympaGuild getGuild() {
		return guild;
	}

	public DiscordMember getAuthor() throws SQLException {
		return CacheDiscordSQL.getDiscordMember(authorId);
	}

	public int getUsesLeaver() {
		return usesLeaver;
	}

	public int getRealUse() {
		return usesLeaver - usesUnique;
	}

	public long getCreated() {
		return created;
	}

	public List<DiscordMember> getUsers() {
		return listUsersIdsToListUsers(usersIds);
	}

	public List<DiscordMember> getPastUsers() {
		return listUsersIdsToListUsers(pastUsersIds);
	}

	public List<DiscordMember> getLeaveUsers() {
		return listUsersIdsToListUsers(leaveUsersIds);
	}

	public Object getUsersToDB() {
		return listUsersToString(usersIds);
	}

	public Object getPastUsersToDB() {
		return listUsersToString(pastUsersIds);
	}

	public Object getLeaveUsersToDB() {
		return listUsersToString(leaveUsersIds);
	}

	public long getAuthorId() {
		return authorId;
	}

	public int getUsesUnique() {
		return usesUnique;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public boolean isUpWithDb() {
		return isUpWithDb;
	}

	public List<Long> getUsersIds() {
		return usersIds;
	}

	public List<Long> getPastUsersIds() {
		return pastUsersIds;
	}

	public List<Long> getLeaveUsersIds() {
		return leaveUsersIds;
	}

	private Object listUsersToString(List<Long> list) {
		return list == null || list.isEmpty() ? new SQLNullObject() : list.stream().map(id -> String.valueOf(id)).collect(Collectors.joining(";"));
	}

	private boolean listIdsContainsUser(List<Long> list, DiscordMember dm) {
		return list == null || list.isEmpty() ? false : list.contains(dm.getId());
	}

	private List<Long> stringToListUsersIds(String s, List<Long> list) throws SQLException {
		if (s != null)
			for (String idOlympaDiscord : s.split(";"))
				list.add((long) RegexMatcher.LONG.parse(idOlympaDiscord));
		return list;
	}

	private List<DiscordMember> listUsersIdsToListUsers(List<Long> from) {
		return from.stream().map(f -> {
			try {
				return CacheDiscordSQL.getDiscordMemberByDiscordOlympaId(f);
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		}).filter(dm -> dm != null).collect(Collectors.toList());
	}
}
