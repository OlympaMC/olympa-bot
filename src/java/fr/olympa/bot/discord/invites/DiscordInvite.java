package fr.olympa.bot.discord.invites;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.api.sql.MySQL;
import fr.olympa.api.sql.SQLColumn;
import fr.olympa.api.sql.SQLTable;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import net.dv8tion.jda.api.entities.Invite;

public class DiscordInvite {

	private static final SQLColumn<DiscordInvite> COLUMN_ID = new SQLColumn<>("id", "INT(10) UNSIGNED NOT NULL AUTO_INCREMENT", Types.INTEGER);
	private static final SQLColumn<DiscordInvite> COLUMN_OLYMPA_GUILD_ID = new SQLColumn<DiscordInvite>("olympa_guild_id", "INT(10) UNSIGNED NULL DEFAULT NULL", Types.INTEGER).setNotDefault().setUpdatable();
	private static final SQLColumn<DiscordInvite> COLUMN_OLYMPA_DISCORD_ID = new SQLColumn<DiscordInvite>("olympa_discord_id", "INT(10) UNSIGNED NULL DEFAULT NULL", Types.INTEGER).setNotDefault().setUpdatable();
	private static final SQLColumn<DiscordInvite> COLUMN_USES = new SQLColumn<DiscordInvite>("uses", "INT(10) UNSIGNED NULL DEFAULT NULL", Types.INTEGER).setNotDefault().setUpdatable();
	private static final SQLColumn<DiscordInvite> COLUMN_USES_LEAVER = new SQLColumn<DiscordInvite>("uses_leaver", "INT(10) UNSIGNED NULL DEFAULT NULL", Types.INTEGER).setUpdatable();
	private static final SQLColumn<DiscordInvite> COLUMN_CREATED = new SQLColumn<DiscordInvite>("uses_leaver", "TIMESTAMP NULL DEFAULT NULL", Types.TIMESTAMP).setNotDefault();
	private static final SQLColumn<DiscordInvite> COLUMN_CODE = new SQLColumn<DiscordInvite>("code", "VARCHAR(10) NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci'", Types.VARCHAR).setNotDefault();
	private static final SQLColumn<DiscordInvite> COLUMN_USER_OLYMPA_DISCORD_ID = new SQLColumn<DiscordInvite>("code", "MEDIUMTEXT NULL DEFAULT NULL COLLATE 'utf8mb4_general_ci'", Types.VARCHAR).setUpdatable();

	static final List<SQLColumn<DiscordInvite>> COLUMNS = Arrays.asList(COLUMN_ID, COLUMN_OLYMPA_GUILD_ID, COLUMN_OLYMPA_DISCORD_ID, COLUMN_USES, COLUMN_USES_LEAVER, COLUMN_CREATED, COLUMN_CODE, COLUMN_USER_OLYMPA_DISCORD_ID);
	private static SQLTable<DiscordInvite> inviteTable;

	public static void init(MySQL mysql) throws SQLException {
		inviteTable = new SQLTable<>("discord.invites", COLUMNS).createOrAlter();
	}

	//	private static OlympaStatement insert = new OlympaStatement(StatementType.INSERT, "discord.invites", "olympa_guild_id", "olympa_discord_id", "created", "code").returnGeneratedKeys();
	//
	//	public void create() throws SQLException {
	//		PreparedStatement statement = insert.getStatement();
	//		int i = 1;
	//		statement.setLong(i++, getGuild().getId());
	//		statement.setLong(i++, getAuthor().getId());
	//		statement.setTimestamp(i++, new Timestamp(getCreated() * 1000L));
	//		statement.setString(i, getCode());
	//		statement.executeUpdate();
	//		statement.close();
	//	}

	public void createNew() throws SQLException {
		ResultSet resultSet = inviteTable.insert(
				getGuild().getId(),
				getAuthor().getId(),
				getUses(),
				new Timestamp(getCreated() * 1000L),
				getCode());
		resultSet.next();
		id = resultSet.getInt("id");
		resultSet.close();
	}

	public void update() throws SQLException {
		COLUMN_USES.updateAsync(this, getUses(), null, null);
		COLUMN_USER_OLYMPA_DISCORD_ID.updateAsync(this, getUsersToDB(), null, null);
		COLUMN_USES_LEAVER.updateAsync(this, getUsesLeaver(), null, null);

	}

	int id;
	OlympaGuild guild;
	DiscordMember author;
	int uses;
	int usesLeaver = 0;
	long created;
	String code;
	List<DiscordMember> users = new ArrayList<>();

	public DiscordInvite(Invite invite) {
		guild = GuildHandler.getOlympaGuildByDiscordId(invite.getGuild().getIdLong());
		try {
			author = CacheDiscordSQL.getDiscordMember(invite.getInviter());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		uses = invite.getUses();
		usesLeaver = 0;
		created = invite.getTimeCreated().toEpochSecond();
		code = invite.getCode();
	}

	public void addUser(DiscordMember member) {
		uses++;
		users.add(member);
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

	public int getUses() {
		return uses;
	}

	public int getUsesLeaver() {
		return usesLeaver;
	}

	public long getCreated() {
		return created;
	}

	public String getCode() {
		return code;
	}

	public String getUrl() {
		return "https://discord.gg/" + code;
	}

	public List<DiscordMember> getUsers() {
		return users;
	}

	public String getUsersToDB() {
		return users.stream().map(dm -> String.valueOf(dm.getId())).collect(Collectors.joining(";"));
	}

}
