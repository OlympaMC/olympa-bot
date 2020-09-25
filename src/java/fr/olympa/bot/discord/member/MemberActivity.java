package fr.olympa.bot.discord.member;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import fr.olympa.api.sql.statement.OlympaStatement;
import fr.olympa.api.sql.statement.StatementType;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.entities.User;

public class MemberActivity {

	int id;
	String name;
	ActivityType type;
	String url;
	String emoji;
	Set<Long> usersIds = new HashSet<>();

	public MemberActivity(User user, Activity activity) {
		name = activity.getName();
		type = activity.getType();
		url = activity.getUrl();
		emoji = activity.getEmoji().getName();
		usersIds.add(user.getIdLong());
	}

	public MemberActivity(ResultSet resultSet) throws SQLException {
		id = resultSet.getInt("id");
		name = resultSet.getString("name");
		type = ActivityType.fromKey(resultSet.getInt("type"));
		url = resultSet.getString("url");
		emoji = resultSet.getString("emoji");
		String userIdsString = resultSet.getString("usersIds");
		if (userIdsString != null)
			usersIds = Arrays.stream(userIdsString.split(",")).map(Long::parseLong).collect(Collectors.toSet());
	}

	static String table = "discord.activity";

	private static OlympaStatement selectStatement = new OlympaStatement(StatementType.SELECT, table, "name", null);

	public static MemberActivity getFromDB(Activity activity) throws SQLException {
		return getFromDB(activity.getName(), activity.getType(), activity.getUrl());
	}

	public static MemberActivity getFromDB(String name, ActivityType type, String url) throws SQLException {
		PreparedStatement statement = selectStatement.getStatement();
		MemberActivity activity = null;
		int i = 1;
		statement.setString(i++, name);
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next())
			activity = new MemberActivity(resultSet);
		resultSet.close();
		return activity;
	}

	public void update() throws SQLException {
		if (id == 0)
			insertToDb();
		else
			updateToDb();
	}

	private static OlympaStatement insertSanctionStatement = new OlympaStatement(StatementType.INSERT, table, "name", "type", "url", "emoji", "usersIds");

	private void insertToDb() throws SQLException {
		PreparedStatement statement = insertSanctionStatement.getStatement();
		int i = 1;
		statement.setString(i++, name);
		statement.setInt(i++, type.getKey());
		statement.setString(i++, url);
		statement.setString(i++, emoji);
		if (usersIds != null && !usersIds.isEmpty()) {
			StringJoiner joiner = new StringJoiner(",");
			usersIds.forEach(id -> joiner.add(String.valueOf(id)));
			statement.setString(i++, joiner.toString());
		} else
			statement.setString(i++, null);
		statement.executeUpdate();
		statement.getGeneratedKeys();
		ResultSet resultSet = statement.getGeneratedKeys();
		resultSet.next();
		id = resultSet.getInt("id");
		statement.close();
	}

	private static OlympaStatement updateGuildStatement = new OlympaStatement(StatementType.UPDATE, table, "id", new String[] { "usersIds" });

	private void updateToDb() throws SQLException {
		PreparedStatement statement = updateGuildStatement.getStatement();
		int i = 1;
		if (usersIds != null && !usersIds.isEmpty()) {
			StringJoiner joiner = new StringJoiner(",");
			usersIds.forEach(id -> joiner.add(String.valueOf(id)));
			statement.setString(i++, joiner.toString());
		} else
			statement.setString(i++, null);
		statement.executeUpdate();
		statement.close();
	}

	public void addUser(User user) throws SQLException {
		if (usersIds.add(user.getIdLong()))
			updateToDb();
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public ActivityType getType() {
		return type;
	}

	public String getUrl() {
		return url;
	}

	public String getEmoji() {
		return emoji;
	}

	public Set<Long> getUsersIds() {
		return usersIds;
	}

	public static String getTable() {
		return table;
	}
}
