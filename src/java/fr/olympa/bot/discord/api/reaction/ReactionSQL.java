package fr.olympa.bot.discord.api.reaction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;

import fr.olympa.api.common.sql.statement.OlympaStatement;
import fr.olympa.api.common.sql.statement.StatementType;

public class ReactionSQL {
	static String tableReaction = "discord.reactions";

	private static OlympaStatement insertReactionStatement = new OlympaStatement(StatementType.INSERT, tableReaction,
			new String[] { "name", "message_id", "allowed_users_ids", "emojis", "data", "can_multiple", "remove_when_modclearall", "guild_id", "date" });

	public static void addReaction(ReactionDiscord reaction) throws SQLException, InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException {
		String name = ReactionHandler.toString(reaction);
		if (name == null) {
			System.out.println("Impossible de sauvegarder dans la BDD la réaction " + reaction.getClass().getName() + " : " + "elle n'est pas dans " + ReactionHandler.class.getName() + ".");
			return;
		}
		try (PreparedStatement statement = insertReactionStatement.createStatement()) {
			int i = 1;
			statement.setString(i++, ReactionHandler.toString(reaction));
			statement.setLong(i++, reaction.getMessageId());
			if (reaction.getCanReactUserIds() != null && !reaction.getCanReactUserIds().isEmpty())
				statement.setString(i++, new Gson().toJson(reaction.getCanReactUserIds()));
			else
				statement.setString(i++, null);
			statement.setString(i++, new Gson().toJson(reaction.getEmojisData()));
			statement.setString(i++, new Gson().toJson(reaction.getData()));
			statement.setInt(i++, reaction.canMultiple() ? 1 : 0);
			statement.setInt(i++, reaction.isRemoveWhenModClearAll() ? 1 : 0);
			statement.setLong(i++, reaction.getOlympaGuildId());
			statement.setTimestamp(i++, new Timestamp(reaction.getTime() * 1000L));
			insertReactionStatement.executeUpdate(statement);
		}
	}

	private static OlympaStatement updateReactionStatement = new OlympaStatement(StatementType.UPDATE, tableReaction,
			new String[] { "allowed_users_ids", "emojis", "data", "can_multiple", "remove_when_modclearall" }, "message_id");

	public static void updateReaction(ReactionDiscord reaction) throws SQLException, InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException {
		String name = ReactionHandler.toString(reaction);
		if (name == null) {
			System.out.println("Impossible de update dans la BDD la réaction " + reaction.getClass().getName() + " : " + "elle n'est pas dans " + ReactionHandler.class.getName() + ".");
			return;
		}
		try (PreparedStatement statement = updateReactionStatement.createStatement()) {
			int i = 1;
			statement.setLong(i++, reaction.getMessageId());
			if (reaction.getCanReactUserIds() != null && !reaction.getCanReactUserIds().isEmpty())
				statement.setString(i++, new Gson().toJson(reaction.getCanReactUserIds()));
			else
				statement.setString(i++, null);
			statement.setString(i++, new Gson().toJson(reaction.getEmojisData()));
			statement.setString(i++, new Gson().toJson(reaction.getData()));
			statement.setInt(i++, reaction.canMultiple() ? 1 : 0);
			statement.setInt(i++, reaction.isRemoveWhenModClearAll() ? 1 : 0);
			updateReactionStatement.executeUpdate(statement);
		}
	}

	private static OlympaStatement selectReactionStatement = new OlympaStatement(StatementType.SELECT, tableReaction, "message_id", null);

	public static ReactionDiscord selectReaction(long messageId) throws SQLException, InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException {
		try (PreparedStatement statement = selectReactionStatement.createStatement()) {
			ReactionDiscord reaction = null;
			int i = 1;
			statement.setLong(i, messageId);
			ResultSet resultSet = selectReactionStatement.executeQuery(statement);
			if (resultSet.next()) {
				reaction = ReactionHandler.getByName(resultSet.getString("name"));
				reaction.createObject(resultSet);
			}
			resultSet.close();
			return reaction;
		}
	}

	private static OlympaStatement selectAllReactionStatement = new OlympaStatement(StatementType.SELECT, tableReaction, (String[]) null, (String[]) null);

	public static Set<ReactionDiscord> selectAllReactions() throws SQLException, InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Set<ReactionDiscord> reactions = new HashSet<>();
		ReactionDiscord reaction;
		try (PreparedStatement statement = selectAllReactionStatement.createStatement()) {
			ResultSet resultSet = selectAllReactionStatement.executeQuery(statement);
			while (resultSet.next()) {
				reaction = ReactionHandler.getByName(resultSet.getString("name"));
				reaction.createObject(resultSet);
				reactions.add(reaction);
			}
			resultSet.close();
			return reactions;
		}
	}

	private static OlympaStatement removeReactionStatement = new OlympaStatement(StatementType.DELETE, tableReaction, "message_id").returnGeneratedKeys();

	public static boolean removeReaction(ReactionDiscord reaction) throws SQLException {
		try (PreparedStatement statement = removeReactionStatement.createStatement()) {
			boolean reactionIsInDB;
			statement.setLong(1, reaction.getMessageId());
			removeReactionStatement.executeUpdate(statement);
			reactionIsInDB = statement.getGeneratedKeys().first();
			return reactionIsInDB;
		}
	}

	private static OlympaStatement purgeStatement = new OlympaStatement(StatementType.TRUNCATE, tableReaction);

	public static int purge() throws SQLException {
		try (PreparedStatement statement = purgeStatement.createStatement()) {
			return purgeStatement.executeUpdate(statement);
		}
	}
}
