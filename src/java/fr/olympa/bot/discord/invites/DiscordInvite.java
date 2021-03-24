package fr.olympa.bot.discord.invites;

import java.awt.Color;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.match.RegexMatcher;
import fr.olympa.api.sql.SQLColumn;
import fr.olympa.api.sql.SQLNullObject;
import fr.olympa.api.sql.SQLTable;
import fr.olympa.api.sql.statement.OlympaStatement;
import fr.olympa.api.utils.Utils;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

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
	static SQLTable<DiscordInvite> table;

	static {
		try {
			table = new SQLTable<>("discord.invites", COLUMNS, t -> {
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

	public static Set<DiscordSmallInvite> getAllSmalls(OlympaGuild opGuild) throws SQLException {
		Set<DiscordSmallInvite> invites;
		//		if (opGuild.isCacheComplete()) {
		//			invites = InvitesHandler.CACHE.asMap().values().stream().filter(entry -> entry.getDiscordGuild().getId() == opGuild.getId()).collect(Collectors.toSet());
		//			if (!invites.isEmpty())
		//				return invites;
		//			else
		//				opGuild.cacheIncomplete();
		//		}
		ResultSet result = DiscordInvite.COLUMN_OLYMPA_GUILD_ID.selectBasic(opGuild.getId(), DiscordInvite.COLUMN_USES.getCleanName(), DiscordInvite.COLUMN_CODE.getCleanName());
		invites = new HashSet<>();
		while (result.next()) {
			DiscordSmallInvite dsm = new DiscordSmallInvite(opGuild, result);
			//			InvitesHandler.addInvite(dsm);
			invites.add(dsm);
		}
		result.close();
		opGuild.cacheComplete();
		return invites;
	}

	public static List<DiscordInvite> getAll(OlympaGuild opGuild) throws SQLException, IllegalAccessException {
		List<DiscordInvite> list;
		//		if (opGuild.isCacheComplete()) {
		//			list = InvitesHandler.CACHE.asMap().values().stream().filter(entry -> entry.getDiscordGuild().getId() == opGuild.getId()).map(DiscordSmallInvite::expand).collect(Collectors.toList());
		//			if (!list.isEmpty())
		//				return list;
		//			else
		//				opGuild.cacheIncomplete();
		//		}
		list = COLUMN_OLYMPA_GUILD_ID.select(opGuild.getId());
		//		list.forEach(l -> InvitesHandler.addInvite(l));
		opGuild.cacheComplete();
		return list;
	}

	public static Map<Long, Integer> getStats(OlympaGuild opGuild) throws SQLException, IllegalAccessException {
		Map<Long, Integer> stats = new HashMap<>();
		for (DiscordInvite invite : getAll(opGuild)) {
			long user = invite.getAuthorId();
			int uses = invite.getRealUse();
			Integer actualNb = stats.get(user);
			if (uses != 0) {
				if (actualNb != null)
					uses += actualNb;
				stats.put(user, uses);
			}
		}
		return stats.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	public static int getPosOfAuthor(OlympaGuild opGuild, DiscordMember dm) throws SQLException, IllegalAccessException {
		Map<Long, Integer> stats = getStats(opGuild);
		int pos = 1;
		Iterator<Entry<Long, Integer>> it = stats.entrySet().iterator();
		Entry<Long, Integer> entry = null;
		while (it.hasNext() && (entry = it.next()).getKey() != dm.getId())
			pos++;
		if (entry != null && entry.getKey() == dm.getId())
			return pos;
		return -1;
	}

	protected static List<DiscordInvite> getByUser(SQLColumn<DiscordInvite> column, DiscordMember dm, OlympaGuild opGuild) {
		List<DiscordInvite> dis = new ArrayList<>();
		OlympaStatement getUsers = new OlympaStatement("SELECT * FROM " + table.getName() + " WHERE " + column.getName() + " REGEXP ? AND " + COLUMN_OLYMPA_GUILD_ID.getName() + " = ?");
		try (PreparedStatement statement = getUsers.createStatement()) {
			int i = 1;
			statement.setString(i++, String.format("\\b(%d)\\b", dm.getId()));
			statement.setLong(i, opGuild.getId());
			ResultSet resultSet = getUsers.executeQuery(statement);
			while (resultSet.next())
				dis.add(table.initializeFromRow.initialize(resultSet));
			resultSet.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return dis;
	}

	static List<DiscordInvite> getByOlympaDiscordId(DiscordMember discordMember) throws SQLException, IllegalAccessException {
		return COLUMN_OLYMPA_DISCORD_ID.select(discordMember.getId());
	}

	int id;
	DiscordMember author;
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
		isExpand = true;
	}

	private DiscordInvite(ResultSet rs) throws SQLException {
		super(rs);
		id = rs.getInt(COLUMN_ID.getCleanName());
		authorId = rs.getInt(COLUMN_OLYMPA_DISCORD_ID.getCleanName());
		uses = rs.getInt(COLUMN_USES.getCleanName());
		usesUnique = rs.getInt(COLUMN_USES_UNIQUE.getCleanName());
		usesLeaver = rs.getInt(COLUMN_USES_LEAVER.getCleanName());
		created = rs.getTimestamp(COLUMN_CREATED.getCleanName()).getTime() / 1000L;
		code = rs.getString(COLUMN_CODE.getCleanName());
		deleted = rs.getBoolean(COLUMN_DELETED.getCleanName());
		stringToListUsersIds(rs.getString(COLUMN_USERS_OLYMPA_DISCORD_ID.getCleanName()), usersIds);
		stringToListUsersIds(rs.getString(COLUMN_USERS_PAST_OLYMPA_DISCORD_ID.getCleanName()), pastUsersIds);
		stringToListUsersIds(rs.getString(COLUMN_USERS_LEAVER_OLYMPA_DISCORD_ID.getCleanName()), leaveUsersIds);
		isUpWithDb = true;
		isExpand = true;
	}

	public DiscordInvite createNew() throws SQLException {
		ResultSet resultSet = table.insert(
				getDiscordGuild().getId(),
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
		table.updateAsync(this, Map.of(COLUMN_USES, getUses(), COLUMN_USERS_OLYMPA_DISCORD_ID, getUsersToDB(), COLUMN_USERS_PAST_OLYMPA_DISCORD_ID, getPastUsersToDB(), COLUMN_USES_LEAVER, getUsesLeaver(), COLUMN_DELETED, deleted,
				COLUMN_USES_UNIQUE, usesUnique), null, null);
		isUpWithDb = true;
	}

	public void update(Invite invite) throws SQLException {
		//		fixInvite(); // TODO remove this for performance
		if (isDeleted())
			throw new IllegalAccessError("Unable to update deleted DiscordInvite");
		int inviteUses = invite.getUses();
		if (inviteUses != uses) {
			LinkSpigotBungee.Provider.link.sendMessage("&e[DISCORD INVITE] &cL'invitation %s par %s n'était pas à jour dans la bdd ...", invite.getCode(), invite.getInviter().getAsTag());
			isUpWithDb = false;
			uses = invite.getUses();
		}
		update();
	}

	@Override
	public DiscordInvite expand() {
		return this;
	}

	public void removeLeaver(DiscordMember member) {
		removeLeaver(member.getId());
	}

	public void removeLeaver(long memberId) {
		if (leaveUsersIds.contains(memberId)) {
			usesLeaver--;
			usesUnique--;
			leaveUsersIds.remove(memberId);
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

	public void retreiveInvite(Consumer<Invite> succes, Consumer<Throwable> fail) {
		getDiscordGuild().getGuild().retrieveInvites().queue(t -> {
			if (t != null) {
				Invite invite = t.stream().filter(i -> i.getCode().equals(code)).findFirst().orElse(null);
				if (invite != null) {
					succes.accept(invite);
					return;
				}
			}
			fail.accept(new Exception("Unable to get Invite for code " + code + "."));
		});
	}

	public boolean fixInvite() throws SQLException {
		Guild guild = getDiscordGuild().getGuild();
		//		guild = guild.getGuild();
		for (Long userId : usersIds) {
			DiscordMember discordMember = CacheDiscordSQL.getDiscordMemberByDiscordOlympaId(userId);
			if (discordMember == null)
				throw new NullPointerException("Unable to get DiscordMember of olympaDiscordId n°" + userId + ".");
			User user = discordMember.getUser();
			if (user != null && !guild.isMember(user)) {
				removeUser(discordMember);
				discordMember.updateLeaveTime(Utils.getCurrentTimeInSeconds());
				LinkSpigotBungee.Provider.link.sendMessage("&cFix invite sucess -> &4" + code + "&c removeUser");
			}
		}
		for (Long userId : leaveUsersIds) {
			DiscordMember discordMember = CacheDiscordSQL.getDiscordMemberByDiscordOlympaId(userId);
			if (discordMember == null)
				throw new NullPointerException("Unable to get DiscordMember of olympaDiscordId n°" + userId + ".");
			User user = discordMember.getUser();
			if (user != null && guild.isMember(user)) {
				removeLeaver(discordMember);
				LinkSpigotBungee.Provider.link.sendMessage("&cFix invite sucess -> &4" + code + "&c user removeLeaver");
			}
		}
		if (leaveUsersIds.size() != usesLeaver)
			if (leaveUsersIds.size() > usesLeaver) {
				usesLeaver = leaveUsersIds.size();
				LinkSpigotBungee.Provider.link.sendMessage("&cFix invite sucess -> &4" + code + "&c bad usesLeaver, leaveUsersIds.size() > usesLeaver");
			} else {
				int iLeavers = 0;
				for (Long userId : pastUsersIds) {
					DiscordMember discordMember = CacheDiscordSQL.getDiscordMemberByDiscordOlympaId(userId);
					if (discordMember == null)
						throw new NullPointerException("Unable to get DiscordMember of olympaDiscordId n°" + userId + ".");
					User user = discordMember.getUser();
					if (user != null && !guild.isMember(user))
						iLeavers++;
				}
				usesLeaver = iLeavers;
				LinkSpigotBungee.Provider.link.sendMessage("&cFix invite sucess -> &4" + code + "&c bad usesLeaver, leaveUsersIds.size() < usesLeaver");
			}
		if (pastUsersIds.size() != usesUnique) {
			LinkSpigotBungee.Provider.link.sendMessage("&cFix invite sucess -> &4" + code + "&c bad usesUnique (was " + usesUnique + ").");
			if (pastUsersIds.size() < usesUnique) {
				List<Long> userToAdd = usersIds.stream().filter(u -> pastUsersIds.contains(u)).collect(Collectors.toList());
				userToAdd.addAll(leaveUsersIds.stream().filter(u -> pastUsersIds.contains(u)).collect(Collectors.toList()));
				if (!userToAdd.isEmpty()) {
					for (Long users : userToAdd)
						pastUsersIds.add(users);
					isUpWithDb = false;
				}
			}
			usesUnique = pastUsersIds.size();
		}
		return isUpWithDb;
	}

	public DiscordMember getAuthor() throws SQLException {
		if (author == null)
			author = CacheDiscordSQL.getDiscordMemberByDiscordOlympaId(authorId);
		return author;
	}

	public User getAuthorUser() throws SQLException {
		return getAuthor().getUser();
	}

	public void sendNewJoinToAuthor(Member member) {
		try {
			DiscordMember dm = getAuthor();
			User user = dm.getUser();
			EmbedBuilder em = new EmbedBuilder();
			em.setColor(Color.GREEN);
			em.setTitle(member.getEffectiveName() + " est arrivé sur **" + discordGuild.getName() + "** grâce à toi !");
			List<DiscordInvite> invites = InvitesHandler.getByAuthor(discordGuild, dm);
			if (invites.size() > 1) {
				int nbJoueurs = invites.stream().mapToInt(DiscordInvite::getRealUse).sum();
				int nbJoueursLeave = invites.stream().mapToInt(DiscordInvite::getUsesLeaver).sum();
				em.setDescription("Tu as déjà invité `" + nbJoueurs + " joueurs`");
				if (nbJoueursLeave != 0)
					em.appendDescription(" Et malheureusement, " + nbJoueursLeave + " joueurs sont partis...");
				if (nbJoueurs > 3) {
					int i = getPosOfAuthor(getDiscordGuild(), dm);
					if (i != -1)
						em.appendDescription("\nTop " + i + " sur " + discordGuild.getName() + ".");
				}
				List<String> msg = Arrays.asList("Merci à toi !", "Tu es sur la bonne voie !", "Encore encore", "Tu peux mieux faire", "Plus, plus, toujours plus",
						"On aime voir ça !", "T'es le boss !", "Stonks", "Badass quoi");
				em.appendDescription("\n" + msg.get(new Random().nextInt(msg.size())));
			} else
				em.setDescription("\n" + "Plus tu invites de joueurs, plus tu seras récompensé....");
			user.openPrivateChannel().queue(pv -> pv.sendMessage(em.build()).queue());
		} catch (SQLException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public int getUsesLeaver() {
		return usesLeaver;
	}

	public int getRealUsesLeaver() {
		return pastUsersIds.size() - usersIds.size();
	}

	public int getRealUse() {
		return usesUnique - usesLeaver;
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
		return list == null || list.isEmpty() ? new SQLNullObject() : list.stream().map(i -> String.valueOf(i)).collect(Collectors.joining(";"));
	}

	private boolean listIdsContainsUser(List<Long> list, DiscordMember dm) {
		return list == null || list.isEmpty() ? false : list.contains(dm.getId());
	}

	private List<Long> stringToListUsersIds(String s, List<Long> list) {
		if (s != null && !s.isEmpty())
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
