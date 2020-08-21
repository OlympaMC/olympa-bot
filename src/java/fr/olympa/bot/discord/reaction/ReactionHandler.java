package fr.olympa.bot.discord.reaction;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

import fr.olympa.bot.discord.commands.RefreshServersMessage;
import fr.olympa.bot.discord.sanctions.MuteChooseCommand;
import fr.olympa.bot.discord.sql.DiscordSQL;

public class ReactionHandler {

	public static MuteChooseCommand MUTE_CHOOSE = new MuteChooseCommand();
	public static RefreshServersMessage REFRESH_SERVER = new RefreshServersMessage();

	public static void initReactions() {
		try {
			AwaitReaction.reactions.putAll(DiscordSQL.selectAllReactions().stream().collect(Collectors.toMap(entry -> entry.getMessageId(), entry -> entry)));
		} catch (InstantiationException | IllegalAccessException | NoSuchFieldException | SecurityException | SQLException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public static ReactionDiscord getByName(String name) throws InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Field field = ReactionHandler.class.getField(name);
		if (field != null) {
			Class<?> fieldType = field.getType();
			return (ReactionDiscord) fieldType.newInstance();
		}
		return null;
	}

	public static String toString(ReactionDiscord reaction) throws InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException {
		return Arrays.stream(ReactionHandler.class.getFields()).filter(f -> f.getType().getSimpleName().equals(reaction.getClass().getSimpleName())).map(Field::getName).findFirst().orElse(null);
	}
}
