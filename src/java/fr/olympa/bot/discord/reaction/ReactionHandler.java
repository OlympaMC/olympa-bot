package fr.olympa.bot.discord.reaction;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

import fr.olympa.bot.discord.sanctions.MuteChooseReaction;
import fr.olympa.bot.discord.servers.RefreshServersReaction;
import fr.olympa.bot.discord.sql.DiscordSQL;

public class ReactionHandler {

	public static MuteChooseReaction MUTE_CHOOSE = new MuteChooseReaction();
	public static RefreshServersReaction REFRESH_SERVER = new RefreshServersReaction();

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
