package fr.olympa.bot.discord.reaction;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import fr.olympa.bot.discord.commands.RefreshServersMessage;
import fr.olympa.bot.discord.sanctions.MuteChooseCommand;
import fr.olympa.bot.discord.sql.DiscordSQL;

// TODO à tester & corriger ;)
public class ReactionHandler {

	static Set<ReactionDiscord> activeReactions = new HashSet<>();

	static ReactionDiscord MUTE_CHOOSE = new MuteChooseCommand();
	static ReactionDiscord REFRESH_SERVER = new RefreshServersMessage();

	public static void initReactions() {
		try {
			activeReactions = DiscordSQL.selectAllReactions();
		} catch (InstantiationException | IllegalAccessException | NoSuchFieldException | SecurityException | SQLException e) {
			System.out.println("DEBUG pour fix les réactions selectAllDB: ");
			e.printStackTrace();
		}
	}

	public static ReactionDiscord getByName(String name) throws InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Field field = ReactionHandler.class.getField(name);
		Class<?> fieldType = field.getType();
		if (fieldType == ReactionDiscord.class)
			return (ReactionDiscord) fieldType.newInstance();
		return null;
	}

	public static String toString(ReactionDiscord reaction) throws InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Field field = Arrays.stream(ReactionHandler.class.getFields()).filter(f -> f.getName().equals(reaction.getClass().getName())).findFirst().orElse(null);
		if (field == null)
			return null;
		return field.getName();
	}
}
