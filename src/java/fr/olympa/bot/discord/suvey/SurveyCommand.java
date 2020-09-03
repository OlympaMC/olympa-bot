package fr.olympa.bot.discord.suvey;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.map.LinkedMap;

import fr.olympa.api.utils.Utils;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import fr.olympa.bot.discord.guild.GuildHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class SurveyCommand extends DiscordCommand {

	public SurveyCommand() {
		super("sondage", DiscordPermission.HIGH_STAFF);
		description = "\"ta question\" \"réponse1\" \"réponse2...\"";
		minArg = 3;
		description = "Propose un sondage.";
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] baseArgs, Message message, String label) {
		message.delete().queue();
		List<String> args = new ArrayList<>();
		LinkedMap<String, String> reactionEmojis = new LinkedMap<>();
		String allArguments = String.join(" ", baseArgs);
		Pattern p = Pattern.compile("\\\"(.*?)\\\"");
		Matcher m = p.matcher(allArguments);
		while (m.find())
			args.add(m.group(1));
		reactionEmojis.put("question", args.get(0));
		args.remove(0);
		TextChannel channel = message.getTextChannel();

		List<String> emojis = Arrays.asList("1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "🔟");
		for (int i = 0; args.size() > i; i++)
			reactionEmojis.put(emojis.get(i), args.get(i));
		channel.sendMessage(getEmbed(reactionEmojis)).queue(msg -> {
			SurveyReaction reaction = new SurveyReaction(reactionEmojis, msg, GuildHandler.getOlympaGuild(message.getGuild()));
			reaction.addToMessage(msg);
			reaction.saveToDB();
		});
	}

	public static MessageEmbed getEmbed(LinkedMap<String, String> reactionEmojis) {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("📝 Sondage:");
		int i = 0;
		if (reactionEmojis.firstKey().equals("question")) {
			embedBuilder.setDescription(reactionEmojis.getValue(0));
			i = 1;
		}
		while (reactionEmojis.size() > i)
			embedBuilder.addField(reactionEmojis.getValue(i), reactionEmojis.get(i++), false);
		embedBuilder.setColor(OlympaBots.getInstance().getDiscord().getColor());
		return embedBuilder.build();
	}

	public static MessageEmbed getEmbed(Message message, LinkedMap<String, String> reactionEmojis, boolean isUnique) {
		List<MessageReaction> reactions = message.getReactions();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("📝 Sondage:");
		int i = 0;
		if (reactionEmojis.firstKey().equals("question")) {
			embedBuilder.setDescription(reactionEmojis.getValue(0));
			i = 1;
		}
		List<User> users = new ArrayList<>();
		for (MessageReaction r : reactions)
			users.addAll(r.retrieveUsers().complete());
		users.removeIf(user -> user.getIdLong() == message.getJDA().getSelfUser().getIdLong());
		int total = users.size();
		int totalUnique = (int) users.stream().distinct().count();
		embedBuilder.appendDescription("\n\nVotes " + total + "\n" + "Vote unique " + (isUnique ? "✅" : "❌" + "\nNombre de vote unique " + totalUnique));
		while (reactionEmojis.size() > i) {
			String key = reactionEmojis.get(i);
			String value = reactionEmojis.getValue(i);
			MessageReaction reaction = reactions.stream().filter(r -> r.getReactionEmote().getEmoji().equals(key)).findFirst().orElse(null);
			double count = reaction.getCount() - 1;
			String pourcent = new DecimalFormat("0.#").format(count / total * 100D);
			String countRound = new DecimalFormat("0.#").format(count);
			embedBuilder.addField(value, key + " " + pourcent + "% " + (count != 0 ? countRound + " vote" + Utils.withOrWithoutS((int) Math.round(count)) : ""), false);
			i++;
		}
		embedBuilder.setColor(OlympaBots.getInstance().getDiscord().getColor());
		return embedBuilder.build();
	}
}