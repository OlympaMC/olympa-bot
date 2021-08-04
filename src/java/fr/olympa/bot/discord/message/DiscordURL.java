package fr.olympa.bot.discord.message;

import fr.olympa.bot.discord.guild.OlympaGuild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class DiscordURL {

	String guildId;
	String channelId;
	String messageId;

	String finalUrl;

	public DiscordURL(String url) {
		finalUrl = url;
	}

	public DiscordURL(Message message) {
		this(message.isFromGuild() ? message.getGuild().getId() : "@me", message.getChannel().getId(), message.getId());
	}

	public DiscordURL(TextChannel channel, String messageId) {
		this(channel.getGuild().getId(), channel.getId(), messageId);
	}

	public DiscordURL(OlympaGuild olympaGuild, TextChannel channel, String messageId) {
		this(olympaGuild.getGuild().getId(), channel.getId(), messageId);
	}

	public DiscordURL(long guildId, long channelId, long messageId) {
		this(String.valueOf(guildId), String.valueOf(channelId), String.valueOf(messageId));
	}

	public DiscordURL(String guildId, String channelId, String messageId) {
		this.guildId = guildId;
		this.channelId = channelId;
		this.messageId = messageId;
		finalUrl = String.format("https://discordapp.com/channels/%s/%s/%s", guildId, channelId, messageId);
	}

	public String get() {
		return finalUrl;
	}

	public String withLabel(String label) {
		return String.format("[%s](%s)", label, finalUrl);
	}

	public String getJumpLabel() {
		return withLabel("Jump");
	}
}
