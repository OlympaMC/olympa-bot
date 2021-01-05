package fr.olympa.bot.discord.invites;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import fr.olympa.api.sql.SQLColumn;
import fr.olympa.api.sql.SQLTable;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import net.dv8tion.jda.api.entities.Invite;

public class DiscordInvite extends DiscordSmallInvite {

	static final SQLColumn<DiscordInvite> COLUMN_ID = new SQLColumn<DiscordInvite>("id", "INT(10) UNSIGNED NOT NULL AUTO_INCREMENT", Types.INTEGER).setPrimaryKey(DiscordInvite::getId);
	static final SQLColumn<DiscordInvite> COLUMN_OLYMPA_GUILD_ID = new SQLColumn<DiscordInvite>("olympa_guild_id", "INT(10) UNSIGNED NOT NULL", Types.INTEGER).setNotDefault();
	static final SQLColumn<DiscordInvite> COLUMN_OLYMPA_DISCORD_ID = new SQLColumn<DiscordInvite>("olympa_discord_id", "INT(10) UNSIGNED NOT NULL", Types.INTEGER).setNotDefault();
	static final SQLColumn<DiscordInvite> COLUMN_USES = new SQLColumn<DiscordInvite>("uses", "INT(10) UNSIGNED NULL NOT NULL", Types.INTEGER).setUpdatable().setNotDefault();
	static final SQLColumn<DiscordInvite> COLUMN_USES_LEAVER = new SQLColumn<DiscordInvite>("uses_leaver", "INT(10) UNSIGNED NULL DEFAULT '0'", Types.INTEGER).setUpdatable();
	static final SQLColumn<DiscordInvite> COLUMN_CREATED = new SQLColumn<DiscordInvite>("created", "TIMESTAMP NOT NULL DEFAULT current_timestamp()", Types.TIMESTAMP).setNotDefault();
	static final SQLColumn<DiscordInvite> COLUMN_CODE = new SQLColumn<DiscordInvite>("code", "VARCHAR(7) NOT NULL COLLATE 'utf8mb4_general_ci'", Types.VARCHAR).setNotDefault();
	static final SQLColumn<DiscordInvite> COLUMN_USER_OLYMPA_DISCORD_ID = new SQLColumn<DiscordInvite>("code", "MEDIUMTEXT NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci'", Types.VARCHAR).setUpdatable();
	static final SQLColumn<DiscordInvite> COLUMN_DELETED = new SQLColumn<DiscordInvite>("deleted", "TINYINT(1) UNSIGNED NULL DEFAULT '0'", Types.BOOLEAN).setUpdatable();

	static final List<SQLColumn<DiscordInvite>> COLUMNS = Arrays.asList(COLUMN_ID, COLUMN_OLYMPA_GUILD_ID, COLUMN_OLYMPA_DISCORD_ID, COLUMN_USES, COLUMN_USES_LEAVER, COLUMN_CREATED, COLUMN_CODE, COLUMN_USER_OLYMPA_DISCORD_ID,
			COLUMN_DELETED);
	private static SQLTable<DiscordInvite> inviteTable;

	static {
		try {
			inviteTable = new SQLTable<>("discord.invites", COLUMNS, t -> new DiscordInvite(t)).createOrAlter();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	static DiscordInvite getByCode(String code) throws SQLException, IllegalAccessException {
		List<DiscordInvite> result = COLUMN_CODE.select(code);
		if (!result.isEmpty())
			return result.get(0);
		return null;
	}

	// TODO Finish
	static DiscordInvite getNewInviteJoiner(OlympaGuild opGuild) throws SQLException, IllegalAccessException {
		ResultSet result = COLUMN_OLYMPA_GUILD_ID.selectBasic(opGuild, COLUMN_USES.getCleanName(), COLUMN_CODE.getCleanName());
		Set<DiscordSmallInvite> invites = new HashSet<>();
		while (result.next())
			invites.add(new DiscordSmallInvite(opGuild, result));
		opGuild.getGuild().retrieveInvites().queue(invs -> {
			//			DiscordSmallInvite invite = invs.stream().map(iv -> invites.stream().filter(smi -> smi.getCode().equals(iv.getCode()) && smi.getUses() + 1 == iv.getUses()).findFirst().orElse(null)).findFirst().orElse(null);
			Invite invite = invites.stream().map(smi -> invs.stream().filter(iv -> smi.getCode().equals(iv.getCode()) && smi.getUses() + 1 == iv.getUses()).findFirst().orElse(null)).findFirst().orElse(null);
		});
		return null;
	}

	static List<DiscordInvite> getByOlympaDiscordId(DiscordMember discordMember) throws SQLException, IllegalAccessException {
		return COLUMN_OLYMPA_DISCORD_ID.select(discordMember.getId());
	}

	public DiscordInvite createNew() throws SQLException {
		ResultSet resultSet = inviteTable.insert(
				getGuild().getId(),
				getAuthor().getId(),
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
		inviteTable.updateAsync(this, Map.of(COLUMN_USES, getUses(), COLUMN_USER_OLYMPA_DISCORD_ID, getUsersToDB(), COLUMN_USES_LEAVER, getUsesLeaver(), COLUMN_DELETED, deleted), null, null);
		isUpWithDb = true;
	}

	int id;
	DiscordMember author;
	int usesLeaver = 0;
	long created;
	boolean deleted = false;
	boolean isUpWithDb = false;
	List<DiscordMember> users = new ArrayList<>();

	public DiscordInvite(Invite invite) {
		super(invite);
		try {
			author = CacheDiscordSQL.getDiscordMemberAndCreateIfNotExist(invite.getInviter());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		uses = invite.getUses();
		usesLeaver = 0;
		created = invite.getTimeCreated().toEpochSecond();
		InvitesHandler.addInvite(this);
	}

	private DiscordInvite(ResultSet rs) {
		super(rs);
		try {
			id = rs.getInt(COLUMN_ID.getCleanName());
			author = CacheDiscordSQL.getDiscordMember(rs.getInt(COLUMN_OLYMPA_GUILD_ID.getCleanName()));
			uses = rs.getInt(COLUMN_USES.getCleanName());
			usesLeaver = rs.getInt(COLUMN_USES_LEAVER.getCleanName());
			created = rs.getTimestamp(COLUMN_CREATED.getCleanName()).getTime() / 1000L;
			code = rs.getString(COLUMN_CODE.getCleanName());
			deleted = rs.getBoolean(COLUMN_DELETED.getCleanName());
			isUpWithDb = true;
			InvitesHandler.addInvite(this);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addUser(DiscordMember member) {
		uses++;
		users.add(member);
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

	public DiscordMember getAuthor() {
		return author;
	}

	public int getUsesLeaver() {
		return usesLeaver;
	}

	public long getCreated() {
		return created;
	}

	public List<DiscordMember> getUsers() {
		return users;
	}

	public String getUsersToDB() {
		return users.stream().map(dm -> String.valueOf(dm.getId())).collect(Collectors.joining(";"));
	}

}
