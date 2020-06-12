package fr.olympa.bot.discord.textmessage;

import java.awt.Color;

import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.webhook.WebHookHandler;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.spigot.chat.SwearHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class SwearDiscord {
	static SwearHandler swearHandler = new SwearHandler(OlympaBungee.getInstance().getConfig().getStringList("chat.insult"));

	public static void check(Member member, TextChannel channel, Message message, OlympaGuild olympaGuild) {
		String messageRaw = swearHandler.testAndReplace(message.getContentRaw(), "**", "**");
		if (messageRaw == null)
			return;
		String desc = member.getAsMention() + " dans " + channel.getAsMention() + ".";
		EmbedBuilder embed = SendLogs.get("💢 Insulte", null, desc + "\n" + message.getJumpUrl(), member);
		embed.addField("Message", messageRaw, true);
		embed.setTimestamp(message.getTimeCreated());
		embed.setColor(Color.MAGENTA);
		WebHookHandler.send(embed.build(), olympaGuild.getLogChannel(), member);
		//olympaGuild.getLogChannel().sendMessage(embed.build()).queue();
	}
}
