package fr.olympa.bot.discord.suvey;

import fr.olympa.bot.discord.reaction.ReactionDiscord;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;

public class SurveyReaction extends ReactionDiscord {

	@Override
	public void onBotStop(long messageId) {

	}

	@Override
	public boolean onReactAdd(Message message, MessageChannel messageChannel, User user, MessageReaction messageReaction, String data) {
		if (!hasData(data))
			return false;
		message.editMessage(SurveyCommand.getEmbed(message, datas)).queue();
		return true;
	}

	@Override
	public void onReactModClearAll(long messageId, MessageChannel messageChannel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onReactModDeleteOne(long messageId, MessageChannel messageChannel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onReactRemove(long messageId, MessageChannel messageChannel, MessageReaction messageReaction, User user) {
		// TODO Auto-generated method stub

	}

}
