package fr.olympa.bot.discord.support;

import java.util.Map.Entry;

import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordUtils;
import fr.olympa.bot.discord.groups.DiscordGroup;
import fr.olympa.bot.discord.message.DiscordURL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SupportListener extends ListenerAdapter {

	@Override
	public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
		Member member = event.getMember();
		SupportHandler.updateChannel(member);
	}

	@Override
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
		Member member = event.getMember();
		TextChannel channel = event.getChannel();
		if (DiscordUtils.isMe(member))
			return;
		Entry<Member, StatusSupportChannel> entry = SupportHandler.getMemberStatusByChannel(channel);
		if (entry == null)
			return;
		StatusSupportChannel status = entry.getValue();
		Member supporter = entry.getKey();
		if (supporter.getIdLong() == member.getIdLong()) {

		}
		switch (status) {
		case WAITING:
			if (SupportHandler.panelStaff(event.getReactionEmote(), channel, event.getMessageIdLong(), member))
				SupportHandler.setChannelStatus(channel, StatusSupportChannel.PROGRESS);
			break;
		// default:
		// channel.retrieveMessageById(event.getMessageIdLong()).queue(msg ->
		// msg.removeReaction(event.getReactionEmote().getEmoji(), member.getUser()));
		// break;
		// }
		// } else {
		// switch (status) {
		case PROGRESS:
			String emoji = event.getReactionEmote().getEmoji();
			DiscordGroup group = DiscordGroup.get(channel.getGuild(), emoji);
			if (group == null) {
				AutoResponse resp = AutoResponse.get(emoji);
				if (resp == null) {
					channel.retrieveMessageById(event.getMessageIdLong()).queue(msg -> msg.removeReaction(event.getReactionEmote().getEmoji(), member.getUser()));
					return;
				}
				EmbedBuilder embed = new EmbedBuilder().setTitle("Support Olympa");
				embed.setDescription(resp.getMsg());
				embed.setColor(OlympaBots.getInstance().getDiscord().getColor());
				channel.sendMessageEmbeds(embed.build()).append(supporter.getAsMention()).queue();
				if (resp == AutoResponse.CLOSE) {
					SupportHandler.setChannelStatus(channel, StatusSupportChannel.CLOSE);
					DiscordUtils.deny(channel, supporter, Permission.MESSAGE_WRITE);
				}
			} else
				DiscordUtils.allow(channel, group.getRole(channel.getGuild()), Permission.MESSAGE_READ, Permission.MESSAGE_MENTION_EVERYONE, Permission.MESSAGE_ADD_REACTION);
			// Alert group
			break;
		case CLOSE:
			DiscordUtils.sendTempMessage(channel, member, "Le ticket est fermé. Ouvre-le sur le panel staff du ticket : " + new DiscordURL(channel, event.getMessageId()).get());
			break;
		default:
			channel.retrieveMessageById(event.getMessageIdLong()).queue(msg -> msg.removeReaction(event.getReactionEmote().getEmoji(), member.getUser()));
			break;
		}

	}

	@Override
	public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {
		Member member = event.getMember();
		TextChannel channel = event.getChannel();
		if (member == null || DiscordUtils.isMe(member))
			return;
		Entry<Member, StatusSupportChannel> entry = SupportHandler.getMemberStatusByChannel(channel);
		if (entry == null)
			return;
		StatusSupportChannel status = entry.getValue();
		Member supporter = entry.getKey();
		String emoji = event.getReactionEmote().getEmoji();
		System.out.println("emoji " + emoji);
		DiscordGroup group = DiscordGroup.get(channel.getGuild(), emoji);
		AutoResponse resp = null;
		if (group == null)
			resp = AutoResponse.get(emoji);
		if (supporter.getIdLong() != member.getIdLong())
			switch (status) {
			default:
				if (group != null)
					channel.getPermissionOverride(group.getRole(channel.getGuild())).delete().queue();
				else if (resp != null)
					DiscordUtils.allow(channel, supporter, Permission.MESSAGE_WRITE);
				break;
			}
		channel.retrieveMessageById(event.getMessageIdLong()).queue(message -> {
			message.retrieveReactionUsers(emoji).queue(users -> {
				users.stream().filter(user -> !user.isBot()).forEach(user -> message.removeReaction(emoji).queue());
			});
		});
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		Member member = event.getMember();
		TextChannel channel = event.getChannel();
		if (event.isWebhookMessage() || !SupportHandler.isSupportChannel(channel, member))
			return;
		TextChannel channelSupport = SupportHandler.getChannel(member);
		if (channelSupport.getIdLong() != channel.getIdLong())
			return;
		StatusSupportChannel status = SupportHandler.getChannelStatus(channelSupport);
		switch (status) {
		case CLOSE:
			break;
		case OPEN:
			SupportHandler.askWhoStaff(channelSupport, member);
			SupportHandler.setChannelStatus(channelSupport, StatusSupportChannel.WAITING);
			break;
		case PROGRESS:
			break;
		case WAITING:
			break;
		default:
			break;
		}
	}

	@Override
	public void onUserUpdateName(UserUpdateNameEvent event) {
		User user = event.getEntity();
		user.getMutualGuilds().stream().forEach(guild -> SupportHandler.updateChannel(guild.getMember(user)));
	}
}
