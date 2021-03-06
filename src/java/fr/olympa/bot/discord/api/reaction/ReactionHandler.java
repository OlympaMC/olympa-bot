package fr.olympa.bot.discord.api.reaction;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

import fr.olympa.bot.discord.ErrorReaction;
import fr.olympa.bot.discord.sanctions.MuteChooseReaction;
import fr.olympa.bot.discord.servers.RefreshServersReaction;
import fr.olympa.bot.discord.suvey.SurveyReaction;

public class ReactionHandler {

	public static MuteChooseReaction MUTE_CHOOSE = new MuteChooseReaction();
	public static RefreshServersReaction REFRESH_SERVER = new RefreshServersReaction();
	public static SurveyReaction SURVEY = new SurveyReaction();
	public static ErrorReaction ERROR = new ErrorReaction();

	public static void initReactions() {
		try {
			AwaitReaction.reactions.putAll(ReactionSQL.selectAllReactions().stream().collect(Collectors.toMap(entry -> entry.getMessageId(), entry -> entry)));
		} catch (InstantiationException | IllegalAccessException | NoSuchFieldException | SecurityException | SQLException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public static ReactionDiscord getByName(String name) throws NoSuchFieldException, SecurityException, InstantiationException, IllegalAccessException {
		Field field = ReactionHandler.class.getField(name);
		if (field != null) {
			Class<?> fieldType = field.getType();
			//			new CreateInstance<ReactionDiscord>().of(fieldType);
			return (ReactionDiscord) fieldType.newInstance();
		}
		return null;
	}

	public static String toString(ReactionDiscord reaction) throws InstantiationException, IllegalAccessException, NoSuchFieldException, SecurityException {
		//		return Arrays.stream(ReactionHandler.class.getFields()).filter(f -> f.getType().getSimpleName().equals(reaction.getClass().getSimpleName())).map(Field::getName).findFirst().orElse(null);
		return Arrays.stream(ReactionHandler.class.getFields()).filter(f -> f.getType().isAssignableFrom(reaction.getClass())).map(Field::getName).findFirst().orElse(null);
	}
}
