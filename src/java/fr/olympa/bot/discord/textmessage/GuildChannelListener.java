package fr.olympa.bot.discord.textmessage;

import java.sql.SQLException;
import java.util.Map.Entry;

import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import fr.olympa.bot.discord.sql.DiscordSQL;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildChannelListener extends ListenerAdapter {

	@Override
	public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
		Guild guild = event.getGuild();
		try {
			Entry<Long, DiscordMessage> entry = CacheDiscordSQL.getDiscordMessage(guild.getIdLong(), event.getChannel().getIdLong(), event.getMessageIdLong());
			if (entry == null)
				return;
			DiscordMessage discordMessage = entry.getValue();
			Member member = discordMessage.getGuild().getMemberById(entry.getKey());
			if (member == null || member.getUser().isBot())
				return;
			discordMessage.setMessageDeleted();
			DiscordSQL.updateMessage(discordMessage);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		if (member == null || member.getUser().isBot())
			return;
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
		Message message = event.getMessage();
		TextChannel channel = message.getTextChannel();
		DiscordMessage discordMessage;
		try {
			Entry<Long, DiscordMessage> entry = CacheDiscordSQL.getDiscordMessage(olympaGuild, message);
			if (entry == null)
				return;
			discordMessage = entry.getValue();
			discordMessage.addEditedMessage(message);
			CacheDiscordSQL.setDiscordMessage(member.getIdLong(), discordMessage);
			if (member.isFake())
				return;
			SwearDiscord.check(member, channel, message, olympaGuild);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		if (member == null || member.getUser().isBot())
			return;
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
		Message message = event.getMessage();
		TextChannel channel = message.getTextChannel();
		try {
			DiscordMessage discordMessage = new DiscordMessage(message);
			DiscordSQL.addMessage(discordMessage);
			long userId = message.getAuthor().getIdLong();
			CacheDiscordSQL.setDiscordMessage(userId, discordMessage);
			if (member.isFake())
				return;
			SwearDiscord.check(member, channel, message, olympaGuild);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
