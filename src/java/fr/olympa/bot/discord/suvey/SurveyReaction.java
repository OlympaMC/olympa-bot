package fr.olympa.bot.discord.suvey;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.map.LinkedMap;

import fr.olympa.api.utils.Utils;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.reaction.ReactionDiscord;
import fr.olympa.bot.discord.guild.OlympaGuild;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;

public class SurveyReaction extends ReactionDiscord {

	public SurveyReaction(LinkedMap<String, String> map, Message msg, OlympaGuild guild) {
		super(map, msg.getIdLong(), guild.getId());
	}

	public SurveyReaction() {}

	@Override
	public void onBotStop(long messageId) {

	}

	@Override
	public boolean onReactAdd(Message message, MessageChannel messageChannel, User user, MessageReaction messageReaction, String reactionEmoji) {
		message.editMessage(getEmbed(data.get("question").toString(), message, reactionsEmojis, !canMultiple())).queue();
		return true;
	}

	@Override
	public void onReactModClearAll(long messageId, MessageChannel messageChannel) {}

	@Override
	public void onReactModDeleteOne(long messageId, MessageChannel messageChannel) {}

	@Override
	public void onReactRemove(Message message, MessageChannel channel, User user, MessageReaction reaction, String reactionEmoji) {
		message.editMessage(getEmbed(data.get("question").toString(), message, reactionsEmojis, !canMultiple())).queue();
	}

	public static MessageEmbed getEmbed(String question, Message message, LinkedMap<String, String> reactionEmojis, boolean isUnique) {
		List<MessageReaction> reactions = message.getReactions();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("📝 Sondage:");
		int i = 0;
		if (reactionEmojis.firstKey().equals("question")) {
			embedBuilder.setDescription(reactionEmojis.getValue(0));
			i = 1;
		}
		List<User> users = new ArrayList<>();
		for (MessageReaction r : reactions)
			users.addAll(r.retrieveUsers().complete());
		users.removeIf(user -> user.getIdLong() == message.getJDA().getSelfUser().getIdLong());
		int total = users.size();
		int totalUnique = (int) users.stream().distinct().count();
		embedBuilder.appendDescription("\n\nVotes " + total + "\n" + "Vote unique " + (isUnique ? "✅" : "❌" + "\nNombre de vote unique " + totalUnique));
		while (reactionEmojis.size() > i) {
			String key = reactionEmojis.get(i);
			String value = reactionEmojis.getValue(i);
			MessageReaction reaction = reactions.stream().filter(r -> r.getReactionEmote().getEmoji().equals(key)).findFirst().orElse(null);
			double count = reaction.getCount() - 1;
			String pourcent = new DecimalFormat("0.#").format(count / total * 100D);
			String countRound = new DecimalFormat("0.#").format(count);
			embedBuilder.addField(value, key + " " + pourcent + "% " + (count != 0 ? countRound + " vote" + Utils.withOrWithoutS((int) Math.round(count)) : ""), false);
			i++;
		}
		embedBuilder.setColor(OlympaBots.getInstance().getDiscord().getColor());
		return embedBuilder.build();
	}
}
