package fr.olympa.bot.discord.suvey;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import fr.olympa.bot.discord.guild.GuildHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
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
		TextChannel channel = message.getTextChannel();
		Map<String, String> emojis = getEmojis(args);
		channel.sendMessage(getEmbed(emojis)).queue(msg -> {
			SurveyReaction reaction = new SurveyReaction(emojis, msg, GuildHandler.getOlympaGuild(message.getGuild()));
			reaction.addToMessage(msg);
			reaction.saveToDB();
		});
	}

	public static Map<String, String> getEmojis(String[] args) {
		List<String> emojis = Arrays.asList("1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "🔟");
		Map<String, String> data = new HashMap<>();
		for (int i = 0; args.length > i; i++)
			data.put(emojis.get(i), args[i]);
		return data;
	}

	public static MessageEmbed getEmbed(Map<String, String> data) {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Sondage:");
		int i = 1;
		for (Entry<String, String> entry : data.entrySet())
			embedBuilder.addField(String.valueOf(i++), entry.getKey() + " " + entry.getValue(), true);
		embedBuilder.setColor(OlympaBots.getInstance().getDiscord().getColor());
		return embedBuilder.build();
	}

	public static MessageEmbed getEmbed(Message message, Map<String, String> data) {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Sondage:");
		List<MessageReaction> reactions = message.getReactions();
		int i = 1;
		for (Entry<String, String> entry : data.entrySet()) {
			MessageReaction reaction = reactions.stream().filter(r -> r.getReactionEmote().getEmoji().equals(entry.getKey())).findFirst().orElse(null);
			message.retrieveReactionUsers(entry.getKey()).complete().size();
			embedBuilder.addField(String.valueOf(i++), entry.getKey() + " " + reaction.getCount() / (reactions.size() / 100D) + "%" + " " + entry.getValue(), true);
		}
		embedBuilder.setColor(OlympaBots.getInstance().getDiscord().getColor());
		return embedBuilder.build();
	}
}