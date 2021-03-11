package fr.olympa.bot.discord.suvey;

import org.apache.commons.collections4.map.LinkedMap;

import fr.olympa.bot.discord.api.reaction.ReactionDiscord;
import fr.olympa.bot.discord.guild.OlympaGuild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
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
		message.editMessage(SurveyCommand.getEmbed(message, reactionsEmojis, !canMultiple())).queue();
		return true;
	}

	@Override
	public void onReactModClearAll(long messageId, MessageChannel messageChannel) {}

	@Override
	public void onReactModDeleteOne(long messageId, MessageChannel messageChannel) {}

	@Override
	public void onReactRemove(Message message, MessageChannel channel, User user, MessageReaction reaction, String reactionEmoji) {
		message.editMessage(SurveyCommand.getEmbed(message, reactionsEmojis, !canMultiple())).queue();
	}

}
