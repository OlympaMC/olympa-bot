package fr.olympa.bot.discord.message;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.DiscordUtils;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.message.file.FileHandler;
import fr.olympa.bot.discord.observer.MessageContent;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import fr.olympa.bot.discord.webhook.WebHookHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.audit.AuditLogOption;
import net.dv8tion.jda.api.audit.TargetType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
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
		File file = new File(OlympaBots.getInstance().getDataFolder(), "discordAttachment");
		if (!file.exists())
			file.mkdirs();
		Map<Attachment, String> map = new HashMap<>();
		if (message.getJDA().getSelfUser().getIdLong() != message.getAuthor().getIdLong())
			message.getAttachments().forEach(att -> {
				//				try {
				//					map.put(att, FileHandler.tryAddFile(att.getProxyUrl(), att.getUrl(), att.getFileName()));
				map.put(att, FileHandler.addFile(att, message));
				//				} catch (Exception e) {
				//					e.printStackTrace();
				//				}
			});
		TextChannel channel = message.getTextChannel();
		try {
			DiscordMessage discordMessage = new DiscordMessage(message, map);
			SQLMessage.addMessage(discordMessage);
			CacheDiscordSQL.setDiscordMessage(member.getIdLong(), discordMessage);
			if (!DiscordUtils.isReal(user) || DiscordPermission.STAFF.hasPermission(member))
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
		File file = new File(OlympaBots.getInstance().getDataFolder(), "discordAttachment");
		if (!file.exists())
			file.mkdirs();
		TextChannel channel = message.getTextChannel();
		DiscordMessage discordMessage = null;
		try {
			Entry<Long, DiscordMessage> entry = CacheDiscordSQL.getDiscordMessage(olympaGuild, message);
			if (entry == null)
				return;
			discordMessage = entry.getValue();
			discordMessage.addEditedMessage(message);
			CacheDiscordSQL.setDiscordMessage(member.getIdLong(), discordMessage);
			SQLMessage.updateMessageContent(discordMessage);
			if (!DiscordUtils.isReal(user))
				return;
			if (olympaGuild.getExcludeChannelsIds().stream().anyMatch(ex -> channel.getIdLong() == ex))
				return;
			SwearDiscord.check(member, channel, message, olympaGuild);
			if (!olympaGuild.isLogMsg() || message.isPinned())
				return;
			String msg = String.format("%s a modifié son message dans %s %s", member.getAsMention(), channel.getAsMention(), discordMessage.getJumpUrl());
			LogsHandler.sendMessage(discordMessage, "✍️ Message modifié", discordMessage.getJumpUrlBrut(), msg, member, null);
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
		TextChannel logChannel = olympaGuild.getLogChannel();

		List<User> userMsg = new ArrayList<>();
		BiConsumer<List<AuditLogEntry>, Throwable> task = (auditLogs, throwable) -> {
			User authorOfAction = null;
			AuditLogEntry auditLog = null;
			if (auditLogs != null)
				auditLog = auditLogs.stream().filter(al -> al.getType().equals(ActionType.MESSAGE_BULK_DELETE) && al.getOption(AuditLogOption.COUNT).equals(String.valueOf(messageIds.size())))
						.limit(1).findFirst().orElse(null);
			else
				logChannel.sendMessage("Une erreur est survenu avec la récupération des logs : `" + throwable.getMessage() + "`").queue();
			if (auditLog != null)
				authorOfAction = auditLog.getUser();
			else
				for (String messageId : messageIds)
					try {
						Entry<Long, DiscordMessage> entry = CacheDiscordSQL.getDiscordMessage(guild.getIdLong(), channel.getIdLong(), Long.parseLong(messageId));
						if (entry == null)
							return;
						DiscordMessage discordMessage = entry.getValue();
						Member member = discordMessage.getGuild().getMemberById(entry.getKey());
						if (member == null)
							return;
						userMsg.add(member.getUser());
						discordMessage.setMessageDeleted();
						CacheDiscordSQL.setDiscordMessage(member.getIdLong(), discordMessage);
						if (!DiscordUtils.isReal(member) || !olympaGuild.isLogMsg() || olympaGuild.getExcludeChannelsIds().stream().anyMatch(ex -> channel.getIdLong() == ex))
							continue;
						SQLMessage.updateMessageContent(discordMessage);
					} catch (SQLException e) {
						e.printStackTrace();
					}
			String description;
			if (authorOfAction == null)
				description = String.format("%d messages de %s ont été supprimés dans %s", messageIds.size(), userMsg.stream()
						.map(user -> DiscordUtils.getMemberMentionNameFull(user)).collect(Collectors.joining(", ")), channel.getAsMention());
			else
				description = String.format("%s a supprimé %d messages de %s dans %s", authorOfAction.getAsMention(), messageIds.size(), userMsg.stream()
						.map(user -> DiscordUtils.getMemberMentionNameFull(user)).collect(Collectors.joining(", ")), channel.getAsMention());
			EmbedBuilder eb = LogsHandler.get("❌ Messages supprimés", null, description, authorOfAction != null ? authorOfAction : null);
			logChannel.sendMessageEmbeds(eb.build());

		};
		guild.retrieveAuditLogs().queue((auditLogs) -> task.accept(auditLogs, null), (throwable) -> task.accept(null, throwable));
		/*messageIds.forEach(messageId -> {
			try {
				Entry<Long, DiscordMessage> entry = CacheDiscordSQL.getDiscordMessage(guild.getIdLong(), channel.getIdLong(), Long.parseLong(messageId));
				if (entry == null)
					return;
				DiscordMessage discordMessage = entry.getValue();
				Member member = discordMessage.getGuild().getMemberById(entry.getKey());
				if (member == null)
					return;
				discordMessage.setMessageDeleted();
				CacheDiscordSQL.setDiscordMessage(member.getIdLong(), discordMessage);
				if (!DiscordUtils.isReal(member) || !olympaGuild.isLogMsg() || olympaGuild.getExcludeChannelsIds().stream().anyMatch(ex -> channel.getIdLong() == ex))
					return;
				String msg = String.format("Un message de %s a été supprimé en clear dans %s %s", member.getAsMention(), channel.getAsMention(), discordMessage.getJumpUrl());
				LogsHandler.sendMessage(discordMessage, "❌ Message supprimé", discordMessage.getJumpUrlBrut(), msg, member);
				SQLMessage.updateMessageContent(discordMessage);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});*/
	}

	// Intresting, to implement
	//	SELECT * FROM messages WHERE
	//	`author_id` = (SELECT id FROM members WHERE `discord_name` = "userName" LIMIT 1) AND
	//	#contents LIKE '%"deleted":true%';
	//	`log_msg_discord_id` IS NOT NULL;

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
			if (!DiscordUtils.isReal(member) || !olympaGuild.isLogMsg() || olympaGuild.getExcludeChannelsIds().stream().anyMatch(ex -> channel.getIdLong() == ex))
				return;

			BiConsumer<List<AuditLogEntry>, Throwable> task = (auditLogs, throwable) -> {
				User authorOfAction = null;
				AuditLogEntry auditLog = null;
				if (auditLogs != null)
					auditLog = auditLogs.stream().filter(al -> al.getType().equals(ActionType.MESSAGE_DELETE) && al.getOption(AuditLogOption.CHANNEL).equals(channel.getId())
							&& al.getTargetType().equals(TargetType.MEMBER) && member.getIdLong() == al.getTargetIdLong())
							.limit(10).findFirst().orElse(null);
				else
					olympaGuild.getLogChannel().sendMessage("Une erreur est survenu avec la récupération des logs : `" + throwable.getMessage() + "`").queue();
				if (auditLog != null)
					authorOfAction = auditLog.getUser();
				else
					authorOfAction = member.getUser();

				StringJoiner sj = new StringJoiner(".\n");
				if (authorOfAction.equals(member.getUser()))
					sj.add(authorOfAction.getAsMention() + " a supprimé son propre message dans " + channel.getAsMention());
				else
					sj.add(authorOfAction.getAsMention() + " a supprimé le message de " + member.getUser().getAsMention() + " dans " + channel.getAsMention());

				MessageContent originalContent = discordMessage.getOriginalContent();
				if (auditLog == null && originalContent != null && originalContent.getContent() != null) {
					Matcher matcher = Pattern.compile("<@!?(\\d{18,})>").matcher(originalContent.getContent());
					List<Member> mentionneds = new ArrayList<>();
					while (matcher.find()) {
						String userId = matcher.group(1);
						Member mentionned = guild.getMemberById(userId);
						if (mentionned.getPermissions(channel).contains(Permission.MESSAGE_READ))
							mentionneds.add(mentionned);
					}
					if (!mentionneds.isEmpty() && originalContent.getContent().replaceAll("<@!?(\\d{18,})>", "").isBlank()) {
						sj.add("😡 Suspicion de ghost tag sur " + mentionneds.stream().map(Member::getAsMention).collect(Collectors.joining(", ")));
						EmbedBuilder embed = new EmbedBuilder();
						embed.setDescription(member.getAsMention() + ", n'abuse pas des mentions fantômes, c'est interdit.");
						embed.setColor(OlympaBots.getInstance().getDiscord().getColor());
						WebhookMessageBuilder messageBuilder = new WebhookMessageBuilder();
						messageBuilder.addEmbeds(WebHookHandler.convertEmbed(embed.build()));
						messageBuilder.append(member.getAsMention());
						WebHookHandler.send(embed.build(), channel, mentionneds.get(0), t1 -> {
							sj.add(discordMessage.getJumpUrl());
							LogsHandler.sendMessage(discordMessage, "❌ Message supprimé", discordMessage.getJumpUrlBrut(), sj.toString(), member, null);
						});
						return;
					}
				}
				sj.add(discordMessage.getJumpUrl());
				LogsHandler.sendMessage(discordMessage, "❌ Message supprimé", discordMessage.getJumpUrlBrut(), sj.toString(), member, auditLog != null ? auditLog.getUser() : null);
				try {
					SQLMessage.updateMessageContent(discordMessage);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			};
			guild.retrieveAuditLogs().queue((auditLogs) -> task.accept(auditLogs, null), (throwable) -> task.accept(null, throwable));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
