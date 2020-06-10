package fr.olympa.bot.discord.textmessage;

import java.sql.SQLException;

import fr.olympa.bot.discord.api.DiscordMessage;
import fr.olympa.bot.discord.guild.GuildsHandler;
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
		DiscordMessage discordMessage;
		try {
			discordMessage = CacheDiscordSQL.getDiscordMessage(event.getGuild().getIdLong(), event.getChannel().getIdLong(), event.getMessageIdLong());
			if (discordMessage == null)
				return;
			Member member = discordMessage.getAuthor();
			if (member == null || member.getUser().isBot())
				return;
			OlympaGuild olympaGuild = GuildsHandler.getGuild(guild);
			TextChannel channel = discordMessage.getChannel();
			if (olympaGuild.isLogMsg() || olympaGuild.getExcludeChannelsIds().stream().anyMatch(ex -> channel.getIdLong() == ex))
				return;
			discordMessage.setMessageDeleted();
			DiscordSQL.updateMessage(discordMessage);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	
	@Override
	public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		if (member == null || member.getUser().isBot())
			return;
		OlympaGuild olympaGuild = GuildsHandler.getGuild(guild);
		Message message = event.getMessage();
		TextChannel channel = message.getTextChannel();
		if (!olympaGuild.isLogMsg() || olympaGuild.getExcludeChannelsIds().stream().anyMatch(ex -> channel.getIdLong() == ex))
			return;
		DiscordMessage discordMessage;
		try {
			discordMessage = CacheDiscordSQL.getDiscordMessage(message);
			if (discordMessage == null)
				return;
			discordMessage.addEditedMessage(message);
			DiscordSQL.updateMessage(discordMessage);
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
		OlympaGuild olympaGuild = GuildsHandler.getGuild(guild);
		Message message = event.getMessage();
		TextChannel channel = message.getTextChannel();
		if (!olympaGuild.isLogMsg() || olympaGuild.getExcludeChannelsIds().stream().anyMatch(ex -> channel.getIdLong() == ex))
			return;
		try {
			DiscordMessage discordMessage = DiscordSQL.addMessage(new DiscordMessage(message));
			CacheDiscordSQL.cacheMessage.put(member.getIdLong(), discordMessage);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// System.out.println("test1 " + OlympaBungee.getInstance()); null
		//		for (Pattern regex : new SwearHandler(BungeeConfigUtils.getDefaultConfig().getStringList("chat.insult")).getRegexSwear()) {
		//			Matcher matcher = regex.matcher(message.getContentDisplay());
		//			if (matcher.find()) {
		//				String desc = member.getAsMention() + " dans " + channel.getAsMention() + ": **" + matcher.group() + "**.";
		//				EmbedBuilder embed = ObverserEmbed.get("💢 Insulte", null, desc + "\n" + message.getJumpUrl(), member.getUser());
		//				embed.setTimestamp(message.getTimeCreated());
		//				DiscordIds.getChannelInfo().sendMessage(embed.build()).queue();
		//				break;
		//			}
		//		}
	}
}
