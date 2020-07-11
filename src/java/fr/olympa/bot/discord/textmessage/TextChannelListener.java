package fr.olympa.bot.discord.textmessage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.observer.MessageContent;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import fr.olympa.bot.discord.sql.DiscordSQL;
import fr.olympa.bot.discord.webhook.WebHookHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TextChannelListener extends ListenerAdapter {

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = event.getAuthor();
		if (member == null)
			return;
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
		Message message = event.getMessage();
		TextChannel channel = message.getTextChannel();
		try {
			DiscordMessage discordMessage = new DiscordMessage(message);
			DiscordSQL.addMessage(discordMessage);
			CacheDiscordSQL.setDiscordMessage(member.getIdLong(), discordMessage);
			if (user.isBot() || member.isFake())
				return;
			if (olympaGuild.getExcludeChannelsIds().stream().anyMatch(ex -> channel.getIdLong() == ex) || user.isBot())
				return;
			SwearDiscord.check(member, channel, message, olympaGuild);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
		Guild guild = event.getGuild();
		User user = event.getAuthor();
		Member member = event.getMember();
		if (member == null)
			return;
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
		Message message = event.getMessage();
		TextChannel channel = message.getTextChannel();
		DiscordMessage discordMessage = null;
		try {
			Entry<Long, DiscordMessage> entry = CacheDiscordSQL.getDiscordMessage(olympaGuild, message);
			if (entry == null)
				return;
			discordMessage = entry.getValue();
			discordMessage.addEditedMessage(message);
			CacheDiscordSQL.setDiscordMessage(member.getIdLong(), discordMessage);
			if (member.getUser().isBot() || member.isFake())
				return;
			if (olympaGuild.getExcludeChannelsIds().stream().anyMatch(ex -> channel.getIdLong() == ex) || user.isBot())
				return;
			SwearDiscord.check(member, channel, message, olympaGuild);
			if (!olympaGuild.isLogMsg())
				return;
			StringJoiner sj = new StringJoiner(".\n");
			sj.add(member.getAsMention() + " a modifié un message dans " + channel.getAsMention());
			sj.add("S'y rendre: " + message.getJumpUrl());
			SendLogs.sendMessageLog(discordMessage, "✍️ Message modifié", message.getJumpUrl(), sj.toString(), member);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessageBulkDelete(MessageBulkDeleteEvent event) {
		Guild guild = event.getGuild();
		List<String> messageIds = event.getMessageIds();
		TextChannel channel = event.getChannel();
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
		messageIds.forEach(messageId -> {

			try {
				Entry<Long, DiscordMessage> entry = CacheDiscordSQL.getDiscordMessage(guild.getIdLong(), channel.getIdLong(), Integer.parseInt(messageId));
				if (entry == null)
					return;
				DiscordMessage discordMessage = entry.getValue();
				Member member = discordMessage.getGuild().getMemberById(entry.getKey());
				if (member == null)
					return;
				discordMessage.setMessageDeleted();
				CacheDiscordSQL.setDiscordMessage(member.getIdLong(), discordMessage);
				DiscordSQL.updateMessageContent(discordMessage);
				if (member.getUser().isBot() || member.isFake() || !olympaGuild.isLogMsg() || olympaGuild.getExcludeChannelsIds().stream().anyMatch(ex -> channel.getIdLong() == ex))
					return;
				StringJoiner sj = new StringJoiner(".\n");
				sj.add(member.getAsMention() + " a supprimé un message dans " + channel.getAsMention());
				sj.add("S'y rendre: " + discordMessage.getJumpUrl());
				SendLogs.sendMessageLog(discordMessage, "❌ Message supprimé", discordMessage.getJumpUrl(), sj.toString(), member);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
		Guild guild = event.getGuild();
		long messageId = event.getMessageIdLong();
		TextChannel channel = event.getChannel();
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
		try {
			Entry<Long, DiscordMessage> entry = CacheDiscordSQL.getDiscordMessage(guild.getIdLong(), channel.getIdLong(), messageId);
			if (entry == null)
				return;
			DiscordMessage discordMessage = entry.getValue();
			Member member = discordMessage.getGuild().getMemberById(entry.getKey());
			if (member == null)
				return;
			discordMessage.setMessageDeleted();
			CacheDiscordSQL.setDiscordMessage(member.getIdLong(), discordMessage);
			DiscordSQL.updateMessageContent(discordMessage);
			if (member.getUser().isBot() || member.isFake() || !olympaGuild.isLogMsg() || olympaGuild.getExcludeChannelsIds().stream().anyMatch(ex -> channel.getIdLong() == ex))
				return;
			StringJoiner sj = new StringJoiner(".\n");
			sj.add(member.getAsMention() + " a supprimé un message dans " + channel.getAsMention());

			// Check ghost tag
			MessageContent originalContent = discordMessage.getOriginalContent();
			if (originalContent != null && originalContent.getContent() != null) {
				Matcher matcher = Pattern.compile("<@!?(\\d{18,})>").matcher(originalContent.getContent());
				List<Member> mentionneds = new ArrayList<>();
				while (matcher.find()) {
					String userId = matcher.group(1);
					Member mentionned = guild.getMemberById(userId);
					if (mentionned.getPermissions(channel).contains(Permission.MESSAGE_READ))
						mentionneds.add(mentionned);
				}
				if (!mentionneds.isEmpty() && originalContent.getContent().replaceAll("<@!?(\\\\d{18,})>", "").isBlank()) {
					sj.add("😡 Suspicion de ghost tag sur " + mentionneds.stream().map(Member::getAsMention).collect(Collectors.joining(", ")));
					EmbedBuilder embed = new EmbedBuilder();
					embed.setDescription(member.getAsMention() + " Abuses pas des mentions fantômes stp, c'est interdit.");
					embed.setColor(OlympaBots.getInstance().getDiscord().getColor());
					WebHookHandler.send(embed.build(), channel, mentionneds.get(0), t1 -> {
						sj.add("S'y rendre: https://discord.com/channels/" + channel.getGuild().getId() + "/" + channel.getId() + "/" + t1.getId() + ".");
						SendLogs.sendMessageLog(discordMessage, "❌ Message supprimé", discordMessage.getJumpUrl(), sj.toString(), member);
					});
					return;
				}
			}
			sj.add("S'y rendre: " + discordMessage.getJumpUrl());
			SendLogs.sendMessageLog(discordMessage, "❌ Message supprimé", discordMessage.getJumpUrl(), sj.toString(), member);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
