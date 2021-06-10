package fr.olympa.bot.discord.suvey;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import org.apache.commons.collections4.map.LinkedMap;

import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;

import fr.olympa.api.common.match.MatcherPattern;
import fr.olympa.api.common.match.RegexMatcher;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

public class SurveyCommand extends DiscordCommand {

	public SurveyCommand() {
		super("sondage", DiscordPermission.HIGH_STAFF);
		usage = "\"ta question\" \"réponse1\" \"réponse2...\" ";
		description = "Propose un sondage.";
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] baseArgs, Message message, String label) {
		TextChannel channel = message.getTextChannel();
		if (baseArgs.length == 0 || baseArgs[0].equalsIgnoreCase("help") || baseArgs.length < 3) {
			EmbedBuilder embed = new EmbedBuilder();
			embed.setTitle("Aide ❓ Sondage");
			embed.setDescription("Voici les exemples de l'utilisation du `." + command.getName() + "`. Vous pouvez terminer un sondage avec un clique droit sur le message puis `Supprimer toutes les réactions`.");
			embed.addField("Sondage Oui ou Non", "```." + command.getName() + " \"Votre question ici\" \"Oui\" \"Non\" -unique```", false);
			embed.addField("Sondage plusieures réponses", "```." + command.getName() + " \"Qui est le plus fort en pvp ?\" \"Bullobily\" \"SkyAsult\" \"Gareth\"` \"Tristiisch\" -unique```", false);
			embed.addField("Sondage plusieures réponses avec Emoji", "```." + command.getName() + " \"Votre humour actuellement ?\" \"🤣Heureux\" \"🥰 Amoureux\" \"😵Stressé\" \"😤Impatient\" -unique```", false);
			embed.addField("Sondage multi réponses avec Emoji", "```." + command.getName() + " \"Quel est le serveur le plus prometteur ?\" \"🥵Olympa\" \"😈Olympa\" \"😇Olympa\" \"🥳Olympa\" -multi```", false);
			embed.addField("Sondage avec fin dans 7jours (en secondes)", "```." + command.getName() + " \"Votre mode de jeux préférer ?\" \"BedWars/Rush\" \"SkyBlock\" \"Practice\" \"Semi-RP\" -t604800```", false);
			embed.setColor(Color.CYAN);
			channel.sendMessage(embed.build()).mention(message.getAuthor()).queue(msg -> msg.delete().queueAfter(10, TimeUnit.MINUTES));
			return;
		}

		List<String> args = new ArrayList<>();
		LinkedMap<String, String> reactionEmojis = new LinkedMap<>();
		SurveyReaction reaction = new SurveyReaction(reactionEmojis, false);
		StringJoiner sj = new StringJoiner(" ");
		for (String s : baseArgs)
			if (s.equalsIgnoreCase("-m") || s.equalsIgnoreCase("-multi"))
				reaction.setMutiple(true);
			else if (s.equalsIgnoreCase("-u") || s.equalsIgnoreCase("-unique"))
				reaction.setMutiple(false);
			else if (s.startsWith("-time") || s.startsWith("-t"))
				reaction.setTime(RegexMatcher.INT.extractAndParse(s));
			else
				sj.add(s);

		MatcherPattern<?> p = MatcherPattern.of("(\\\"|')(.*?)(\\\"|')");
		Matcher m = p.getPattern().matcher(sj.toString());
		while (m.find()) {
			String s = m.group(2);
			args.add(s);
		}
		String question = args.get(0);
		reaction.putData("question", question);

		message.getEmotes().forEach(e -> e.isAnimated());
		List<String> defaultEmojis = Arrays.asList("1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "🔟");
		for (int i = 1; args.size() > i; i++) {
			String emoji = defaultEmojis.get(i - 1);
			String awnser = args.get(i);
			List<String> emojis = EmojiParser.extractEmojis(awnser);
			if (!emojis.isEmpty()) {
				emoji = emojis.get(0);
				awnser = EmojiParser.removeEmojis(awnser, Arrays.asList(EmojiManager.getByUnicode(emoji)));
			}
			reactionEmojis.put(emoji, awnser);
		}
		channel.sendMessage(getEmbed(question, reactionEmojis)).queue(msg -> {
			reaction.addToMessage(msg);
			reaction.saveToDB();
		});
	}

	public static MessageEmbed getEmbed(String question, LinkedMap<String, String> reactionEmojis) {
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("📝 Sondage:");
		embedBuilder.setDescription(question);
		for (Entry<String, String> entry : reactionEmojis.entrySet())
			embedBuilder.addField(entry.getValue(), entry.getKey(), false);
		embedBuilder.setColor(Color.GREEN);
		return embedBuilder.build();
	}
}