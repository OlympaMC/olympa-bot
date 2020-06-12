package fr.olympa.bot.discord.webhook;

import java.awt.Color;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Consumer;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbed.EmbedAuthor;
import club.minnced.discord.webhook.send.WebhookEmbed.EmbedField;
import club.minnced.discord.webhook.send.WebhookEmbed.EmbedFooter;
import club.minnced.discord.webhook.send.WebhookEmbed.EmbedTitle;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.AuthorInfo;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.MessageEmbed.Footer;
import net.dv8tion.jda.api.entities.MessageEmbed.ImageInfo;
import net.dv8tion.jda.api.entities.MessageEmbed.Thumbnail;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;

public class WebHookHandler {
	
	public static String defaultName = "OlympaBot";
	
	private static WebhookClient sendWebhook(TextChannel channel, Consumer<? super Webhook> success) {
		channel.retrieveWebhooks().queue(wbs -> {
			Webhook webhook = wbs.stream().filter(wb -> wb.getName().equals(defaultName)).findFirst().orElse(null);
			if (webhook == null)
				channel.createWebhook(defaultName).queue(wb -> success.accept(wb));
			else
				success.accept(webhook);
		});
		return null;
	}
	
	private static WebhookClient getClient(Webhook webhook) {
		WebhookClientBuilder clientBuilder = new WebhookClientBuilder(webhook.getUrl());
		clientBuilder.setThreadFactory((job) -> {
			Thread thread = new Thread(job);
			thread.setName("Discord WebHook Client: Channel '" + webhook.getChannel().getName() + "'");
			thread.setDaemon(true);
			return thread;
		});
		clientBuilder.setWait(true);
		return clientBuilder.build();
	}

	public static WebhookEmbed convertEmbed(MessageEmbed embed) {
		WebhookEmbedBuilder webhookEmbed = new WebhookEmbedBuilder();
		AuthorInfo oldAuthor = embed.getAuthor();
		if (oldAuthor != null)
			webhookEmbed.setAuthor(new EmbedAuthor(oldAuthor.getName(), oldAuthor.getIconUrl(), oldAuthor.getUrl()));
		Color oldColor = embed.getColor();
		if (oldColor != null)
			webhookEmbed.setColor(oldColor.getRGB());
		String oldDesc = embed.getDescription();
		if (oldDesc != null)
			webhookEmbed.setDescription(oldDesc);
		Footer oldFooter = embed.getFooter();
		if (oldFooter != null)
			webhookEmbed.setFooter(new EmbedFooter(oldFooter.getText(), oldFooter.getIconUrl()));
		ImageInfo oldImage = embed.getImage();
		if (oldImage != null)
			webhookEmbed.setImageUrl(oldImage.getUrl());
		Thumbnail oldThumbnail = embed.getThumbnail();
		if (oldThumbnail != null)
			webhookEmbed.setThumbnailUrl(oldThumbnail.getUrl());
		OffsetDateTime oldTimestamp = embed.getTimestamp();
		if (oldTimestamp != null)
			webhookEmbed.setTimestamp(oldTimestamp.toInstant());
		String oldTitle = embed.getTitle();
		String oldTitleUrl = embed.getUrl();
		if (oldTitle != null)
			webhookEmbed.setTitle(new EmbedTitle(oldTitle, oldTitleUrl));
		List<Field> oldFields = embed.getFields();
		if (oldFields != null && !oldFields.isEmpty())
			for (Field field : oldFields)
				webhookEmbed.addField(new EmbedField(field.isInline(), field.getName(), field.getValue()));
		return webhookEmbed.build();
		
	}
	
	public static void send(MessageEmbed messageEmbed, TextChannel channel, Member member) {
		Consumer<? super Webhook> success = webhook -> {
			WebhookClient client = getClient(webhook);
			WebhookMessageBuilder messageBuilder = new WebhookMessageBuilder();
			messageBuilder.setUsername(member.getEffectiveName());
			messageBuilder.setAvatarUrl(member.getUser().getAvatarUrl());
			messageBuilder.addEmbeds(convertEmbed(messageEmbed));
			client.send(messageBuilder.build());
		};
		sendWebhook(channel, success);
	}
	
	public static void send(String content, TextChannel channel, Member member) {
		Consumer<? super Webhook> success = webhook -> {
			WebhookClient client = getClient(webhook);
			WebhookMessageBuilder messageBuilder = new WebhookMessageBuilder();
			messageBuilder.setUsername(member.getEffectiveName());
			messageBuilder.setAvatarUrl(member.getUser().getAvatarUrl());
			messageBuilder.append(content);
			client.send(messageBuilder.build());
		};
		sendWebhook(channel, success);
	}
}
