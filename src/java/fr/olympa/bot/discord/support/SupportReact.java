package fr.olympa.bot.discord.support;

import java.util.Map.Entry;

import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordUtils;
import fr.olympa.bot.discord.api.reaction.ReactionDiscord;
import fr.olympa.bot.discord.groups.DiscordGroup;
import fr.olympa.bot.discord.message.JumpURL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class SupportReact extends ReactionDiscord {

	@Override
	public void onBotStop(long messageId) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onReactAdd(Message message, MessageChannel messageChannel, User user, MessageReaction messageReaction, String data) {
		if (!message.isFromGuild())
			return true;
		Member member = message.getMember();
		TextChannel channel = (TextChannel) messageChannel;
		ReactionEmote reactionEmote = messageReaction.getReactionEmote();
		if (DiscordUtils.isMe(member))
			return true;
		Entry<Member, StatusSupportChannel> entry = SupportHandler.getMemberStatusByChannel(channel);
		if (entry == null)
			return true;
		StatusSupportChannel status = entry.getValue();
		Member supporter = entry.getKey();
		if (supporter.getIdLong() == member.getIdLong()) {

		}
		switch (status) {
		case WAITING:
			if (SupportHandler.panelStaff(reactionEmote, channel, message.getIdLong(), member))
				SupportHandler.setChannelStatus(channel, StatusSupportChannel.PROGRESS);
			return true;
		//			break;
		// default:
		// channel.retrieveMessageById(event.getMessageIdLong()).queue(msg ->
		// msg.removeReaction(event.getReactionEmote().getEmoji(), member.getUser()));
		// break;
		// }
		// } else {
		// switch (status) {
		case PROGRESS:
			String emoji = reactionEmote.getEmoji();
			DiscordGroup group = DiscordGroup.get(channel.getGuild(), emoji);
			if (group == null) {
				AutoResponse resp = AutoResponse.get(emoji);
				if (resp == null)
					//					channel.retrieveMessageById(event.getMessageIdLong()).queue(msg -> msg.removeReaction(event.getReactionEmote().getEmoji(), member.getUser()));
					return false;
				EmbedBuilder embed = new EmbedBuilder().setTitle("Support Olympa");
				embed.setDescription(resp.getMsg());
				embed.setColor(OlympaBots.getInstance().getDiscord().getColor());
				channel.sendMessage(embed.build()).append(supporter.getAsMention()).queue();
				if (resp == AutoResponse.CLOSE) {
					SupportHandler.setChannelStatus(channel, StatusSupportChannel.CLOSE);
					DiscordUtils.deny(channel, supporter, Permission.MESSAGE_WRITE);
				}
			} else
				DiscordUtils.allow(channel, group.getRole(channel.getGuild()), Permission.MESSAGE_READ, Permission.MESSAGE_MENTION_EVERYONE, Permission.MESSAGE_ADD_REACTION);
			// Alert group
			return true;
		case CLOSE:
			DiscordUtils.sendTempMessage(channel, member, "Le ticket est fermé. Ouvre-le sur le panel staff du ticket : " + new JumpURL(message).get());
			break;
		//		default:
		//			message.removeReaction(reactionEmote.getEmoji(), member.getUser());
		//			break;
		case OPEN:
			break;
		default:
			break;
		}
		return false;
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
