package fr.olympa.bot.discord.reaction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;

import fr.olympa.api.sql.OlympaStatement;
import fr.olympa.api.sql.StatementType;

public class ReactionSQL {
	static String tableReaction = "discord.reactions";

	private static OlympaStatement insertReactionStatement = new OlympaStatement(StatementType.INSERT, tableReaction,
			new String[] { "name", "message_id", "allowed_users_ids", "data", "can_multiple", "remove_when_modclearall", "guild_id" });

	public static void addReaction(ReactionDiscord reaction) throws SQLException, InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException {
		String name = ReactionHandler.toString(reaction);
		if (name == null) {
			System.out.println("Impossible de sauvegarder dans la BDD la réaction " + reaction.getClass().getName() + " : " + "elle n'est pas dans " + ReactionHandler.class.getName() + ".");
			return;
		}
		PreparedStatement statement = insertReactionStatement.getStatement();
		int i = 1;
		statement.setString(i++, ReactionHandler.toString(reaction));
		statement.setLong(i++, reaction.getMessageId());
		if (reaction.getCanReactUserIds() != null && !reaction.getCanReactUserIds().isEmpty())
			statement.setString(i++, new Gson().toJson(reaction.getCanReactUserIds()));
		else
			statement.setString(i++, null);
		statement.setString(i++, new Gson().toJson(reaction.getDatas()));
		statement.setInt(i++, reaction.canMultiple() ? 1 : 0);
		statement.setInt(i++, reaction.isRemoveWhenModClearAll() ? 1 : 0);
		statement.setLong(i++, reaction.getOlympaGuildId());
		statement.executeUpdate();
		statement.close();
	}

	private static OlympaStatement selectReactionStatement = new OlympaStatement(StatementType.SELECT, tableReaction, "message_id", null);

	public static ReactionDiscord selectReaction(long messageId) throws SQLException, InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException {
		PreparedStatement statement = selectReactionStatement.getStatement();
		ReactionDiscord reaction = null;
		int i = 1;
		statement.setLong(i++, messageId);
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next()) {
			reaction = ReactionHandler.getByName(resultSet.getString("name"));
			reaction.createObject(resultSet);
		}
		resultSet.close();
		return reaction;
	}

	private static OlympaStatement selectAllReactionStatement = new OlympaStatement(StatementType.SELECT, tableReaction, (String[]) null, (String[]) null);

	public static Set<ReactionDiscord> selectAllReactions() throws SQLException, InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException {
		PreparedStatement statement = selectAllReactionStatement.getStatement();
		Set<ReactionDiscord> reactions = new HashSet<>();
		ReactionDiscord reaction;
		ResultSet resultSet = statement.executeQuery();
		while (resultSet.next()) {
			reaction = ReactionHandler.getByName(resultSet.getString("name"));
			reaction.createObject(resultSet);
			reactions.add(reaction);
		}
		resultSet.close();
		return reactions;
	}

	private static OlympaStatement removeReactionStatement = new OlympaStatement(StatementType.DELETE, tableReaction, "message_id");

	public static boolean removeReaction(ReactionDiscord reaction) throws SQLException {

		PreparedStatement statement = removeReactionStatement.getStatement();
		boolean reactionIsInDB;
		int i = 1;
		statement.setLong(i++, reaction.getMessageId());
		statement.executeUpdate();
		reactionIsInDB = statement.getGeneratedKeys().first();
		statement.close();
		return reactionIsInDB;
	}
}
