package fr.olympa.bot.discord.sanctions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import fr.olympa.api.utils.Matcher;
import fr.olympa.api.utils.UtilsCore;
import fr.olympa.bot.discord.api.DiscordIds;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.NumberEmoji;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import fr.olympa.bot.discord.api.reaction.AwaitReaction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class MuteCommand extends DiscordCommand {

	public MuteCommand() {
		super("mute", DiscordPermission.ASSISTANT);
		minArg = 1;
		usage = "<nom|surnom|tag|id>";
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message) {
		if (args.length == 0) {
			return;
		}

		Member member = message.getMember();
		MessageChannel channel = message.getChannel();
		Guild guild = DiscordIds.getDefaultGuild();
		List<Member> targets;
		String name = args[0];
		targets = guild.getMembersByEffectiveName(name, true);
		if (targets.isEmpty()) {
			targets = guild.getMembersByName(name, true);
		}
		if (targets.isEmpty() && Matcher.isDisocrdTag(name)) {
			targets = Arrays.asList(guild.getMemberByTag(name));
		}
		if (targets.isEmpty() && Matcher.isInt(name)) {
			targets = Arrays.asList(guild.getMemberById(name));
		}
		if (targets.isEmpty()) {
			targets = UtilsCore.similarWords(name, guild.getMembers().stream().map(Member::getEffectiveName)
					.collect(Collectors.toSet())).stream()
					.map(n -> guild.getMembersByEffectiveName(n, false)).filter(m -> !m.isEmpty()).map(m -> m.get(0))
					.collect(Collectors.toList());
		}
		if (targets.isEmpty()) {
			channel.sendMessage("Aucun joueur trouvé avec `" + name + "`.").queue();
			return;
		} else if (targets.size() > 1) {
			EmbedBuilder em = new EmbedBuilder();
			em.setTitle("Plusieurs membres trouvés:");
			StringJoiner sj = new StringJoiner("\n");
			HashMap<String, String> data = new HashMap<>();
			NumberEmoji numberEmoji = NumberEmoji.ONE;
			for (Member t : targets) {
				if (numberEmoji != null) {
					sj.add(numberEmoji.getEmoji() + " " + t.getAsMention());
					data.put(numberEmoji.getEmoji(), t.getId());
				} else {
					sj.add(t.getAsMention());
				}
				numberEmoji = numberEmoji.getNext();
			}
			em.setDescription(sj.toString());
			channel.sendMessage(em.build()).queue(m -> AwaitReaction.addReaction(m, new MuteChooseCommand(data, member.getIdLong())));

			return;
		} else {
			Member target = targets.get(0);
			SanctionHandler.mute(target, member.getUser(), channel);
		}
	}

}
