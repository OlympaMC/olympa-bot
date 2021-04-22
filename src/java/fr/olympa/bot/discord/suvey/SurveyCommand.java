package fr.olympa.bot.discord.suvey;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.map.LinkedMap;

import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;

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
		TextChannel channel = message.getTextChannel();
		if (baseArgs.length == 0 || baseArgs[0].equalsIgnoreCase("help") || baseArgs.length < 3) {
			EmbedBuilder embed = new EmbedBuilder();
			embed.setTitle("Aide ❓ Sondage");
			embed.setDescription("Voici les exemples de l'utilisation du `." + command.getName() + "`. Vous pouvez terminer un sondage avec un clique droit sur le message puis `Supprimer toutes les réactions`.");
			embed.addField("Sondage Oui ou Non", "`." + command.getName() + "\"Votre question ici\" \"Oui\" \"Non\"`", false);
			embed.addField("Sondage plusieures réponses", "`." + command.getName() + "\"Qui est le plus fort en pvp ?\" \"Bullobily\" \"SkyAsult\" \"Gareth\"` \"Tristiisch\"`", false);
			embed.addField("Sondage plusieures réponses avec Emoji", "`." + command.getName() + "\"Votre humour actuellement ?\" \"🤣 Heureux\" \"🥰 Amoureux\" \"😵Stressé\"` \"😤Impatient\"`", false);
			channel.sendMessage(embed.build()).mention(message.getAuthor()).queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
			return;
		}

		List<String> args = new ArrayList<>();
		LinkedMap<String, String> reactionEmojis = new LinkedMap<>();
		String allArguments = String.join(" ", baseArgs);
		Pattern p = Pattern.compile("\\\"(.*?)\\\"");
		Matcher m = p.matcher(allArguments);
		while (m.find())
			args.add(m.group(1));

		List<String> defaultEmojis = Arrays.asList("1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "🔟");
		for (int i = 1; args.size() >= i; i++) {
			String emoji = defaultEmojis.get(i);
			String awnser = args.get(i);
			List<String> emojis = EmojiParser.extractEmojis(awnser);
			if (!emojis.isEmpty()) {
				emoji = emojis.get(0);
				EmojiParser.removeEmojis(awnser, Arrays.asList(EmojiManager.getByUnicode(emojis.get(0))));
			}
			reactionEmojis.put(emoji, awnser);
		}
		channel.sendMessage(getEmbed(args.get(0), reactionEmojis)).queue(msg -> {
			SurveyReaction reaction = new SurveyReaction(reactionEmojis, msg, GuildHandler.getOlympaGuild(message.getGuild()));
			reaction.getData().put("question", args.get(0));
			reaction.addToMessage(msg);
			reaction.saveToDB();
		});
	}

	public static MessageEmbed getEmbed(String question, LinkedMap<String, String> reactionEmojis) {
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

	public static MessageEmbed getEmbed(String question, Message message, LinkedMap<String, String> reactionEmojis, boolean isUnique) {
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