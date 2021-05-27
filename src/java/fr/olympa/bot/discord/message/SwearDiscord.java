package fr.olympa.bot.discord.message;

import java.awt.Color;
import java.util.concurrent.TimeUnit;

import fr.olympa.api.SwearHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.webhook.WebHookHandler;
import fr.olympa.core.bungee.OlympaBungee;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class SwearDiscord {
	static SwearHandler swearHandler;

	public static void updatedConfig() {
		swearHandler = new SwearHandler(OlympaBungee.getInstance().getConfig().getStringList("chat.insult"));
	}

	public static void check(Member member, TextChannel channel, Message message, OlympaGuild olympaGuild) {
		if (!olympaGuild.isLogInsult())
			return;
		if (swearHandler == null)
			updatedConfig();
		String messageRaw = swearHandler.testAndReplace(message.getContentRaw(), "**", "**");
		if (messageRaw == null) {
			message.clearReactions("⚠️");
			return;
		}
		message.addReaction("⚠️").queue();
		message.removeReaction("⚠️").queueAfter(1, TimeUnit.SECONDS);
		String desc = member.getAsMention() + " dans " + channel.getAsMention() + ".";
		EmbedBuilder embed = LogsHandler.get("⚠️ Insulte", null, desc + " [jump](" + message.getJumpUrl() + "]", member);

		embed.addField("Message", messageRaw, true);
		embed.setTimestamp(message.getTimeCreated());
		embed.setColor(Color.MAGENTA);
		WebHookHandler.send(embed.build(), olympaGuild.getLogChannel(), member);
	}
}
