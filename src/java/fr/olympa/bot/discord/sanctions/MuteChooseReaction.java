package fr.olympa.bot.discord.sanctions;

import org.apache.commons.collections4.map.LinkedMap;

import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import fr.olympa.bot.discord.reaction.AwaitReaction;
import fr.olympa.bot.discord.reaction.ReactionDiscord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;

public class MuteChooseReaction extends ReactionDiscord {

	public MuteChooseReaction(Message message, LinkedMap<String, String> data, long... canReactUserIds) {
		super(data, message.getIdLong(), GuildHandler.getOlympaGuildByDiscordId(message.getGuild().getIdLong()).getId(), canReactUserIds);
	}

	public MuteChooseReaction() {
		super();
	}

	@Override
	public void onBotStop(long messageId) {
		// TODO remove reactions
	}

	@Override
	public boolean onReactAdd(Message message, MessageChannel messageChannel, User user, MessageReaction messageReaction, String data) {
		Member target = GuildHandler.getOlympaGuild(DiscordGuildType.PUBLIC).getGuild().getMemberById(data);

		EmbedBuilder em = new EmbedBuilder();
		em.setDescription("Tu as choisis " + target.getAsMention());
		messageChannel.sendMessage(em.build()).queue();
		// anctionHandler.mute(target, user, messageChannel);
		messageReaction.clearReactions().queue();
		AwaitReaction.removeReaction(message.getIdLong());
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
	public void onReactRemove(Message message, MessageChannel channel, User user, MessageReaction reaction, String reactionsEmojis) {
		// TODO Auto-generated method stub

	}
}
