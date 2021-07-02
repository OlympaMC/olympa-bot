package fr.olympa.bot.discord.message;

import fr.olympa.bot.discord.guild.OlympaGuild;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class JumpURL {

	String guildId;
	String channelId;
	String messageId;

	public JumpURL(Message message) {
		guildId = message.getGuild().getId();
		channelId = message.getChannel().getId();
		messageId = message.getId();
	}

	public JumpURL(TextChannel channel, String messageId) {
		guildId = channel.getGuild().getId();
		channelId = channel.getId();
		this.messageId = messageId;
	}

	public JumpURL(OlympaGuild olympaGuild, TextChannel channel, String messageId) {
		Guild guild = olympaGuild.getGuild();
		guildId = guild.getId();
		channelId = channel.getId();
		this.messageId = messageId;
	}

	public JumpURL(long guildId, long channelId, long messageId) {
		this.guildId = String.valueOf(guildId);
		this.channelId = String.valueOf(channelId);
		this.messageId = String.valueOf(messageId);
	}

	public JumpURL(String guildId, String channelId, String messageId) {
		this.guildId = guildId;
		this.channelId = channelId;
		this.messageId = messageId;
	}

	public String get() {
		return String.format("https://discordapp.com/channels/%s/%s/%s", guildId, channelId, messageId);
	}

	public String get(String label) {

		return String.format("[%s](https://discordapp.com/channels/%s/%s/%s)", label, guildId, channelId, messageId);
	}

	public String getJumpLabel() {
		return get("Jump");
	}
}
