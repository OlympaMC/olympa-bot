package fr.olympa.bot.discord.sanctions;

import java.util.Map;

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

public class MuteChooseCommand extends ReactionDiscord {

	public MuteChooseCommand(Message message, Map<String, String> data, long... canReactUserIds) {
		super(data, message.getIdLong(), GuildHandler.getOlympaGuildByDiscordId(message.getGuild().getIdLong()).getId(), canReactUserIds);
	}

	public MuteChooseCommand() {
		super();
	}

	@Override
	public void onBotStop(long messageId) {
	}

	@Override
	public boolean onReactAdd(long messageId, MessageChannel messageChannel, User user, MessageReaction messageReaction, String data) {
		Member target = GuildHandler.getOlympaGuild(DiscordGuildType.PUBLIC).getGuild().getMemberById(data);

		EmbedBuilder em = new EmbedBuilder();
		em.setDescription("Tu as choisis " + target.getAsMention());
		messageChannel.sendMessage(em.build()).queue();
		// anctionHandler.mute(target, user, messageChannel);
		messageReaction.clearReactions().queue();
		AwaitReaction.removeReaction(messageId);
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
