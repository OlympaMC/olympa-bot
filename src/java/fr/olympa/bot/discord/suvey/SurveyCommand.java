package fr.olympa.bot.discord.suvey;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import fr.olympa.bot.discord.servers.RefreshServersReaction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

public class SurveyCommand extends DiscordCommand {

	public SurveyCommand() {
		super("sondage", DiscordPermission.HIGH_STAFF);
		description = "<question1> [question2] [question...]";
		minArg = 2;
		description = "Propose un sondage.";
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		message.delete().queue();
		Member member = message.getMember();
		TextChannel channel = message.getTextChannel();
		Map<String, String> emojis = getEmojis(args);
		channel.sendMessage(getEmbed(emojis)).queue(msg -> {
			RefreshServersReaction reaction = new RefreshServersReaction(emojis, msg, member.getIdLong());
			reaction.addToMessage(msg);
			reaction.saveToDB();
		});
	}

	public static Map<String, String> getEmojis(String[] args) {
		List<String> emojis = Arrays.asList("1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "🔟");
		Map<String, String> data = new HashMap<>();
		for (String arg : args)
			data.put(emojis.get(0), arg);
		return data;
	}

	public static MessageEmbed getEmbed(Map<String, String> data) {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Sondage:");
		for (Entry<String, String> entry : data.entrySet())
			embedBuilder.addField(entry.getKey(), "**" + entry.getValue() + "**", true);
		embedBuilder.setColor(OlympaBots.getInstance().getDiscord().getColor());
		return embedBuilder.build();
	}
}