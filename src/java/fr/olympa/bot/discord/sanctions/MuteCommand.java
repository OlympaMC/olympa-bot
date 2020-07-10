package fr.olympa.bot.discord.sanctions;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;

import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.NumberEmoji;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.reaction.AwaitReaction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class MuteCommand extends DiscordCommand {

	public MuteCommand() {
		super("mute", DiscordPermission.ASSISTANT);
		minArg = 1;
		usage = "<nom|surnom|tag|id|mention> [temps] <raison>";
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		if (args.length == 0)
			return;

		Member member = message.getMember();
		MessageChannel channel = message.getChannel();
		Guild guild = GuildHandler.getOlympaGuild(DiscordGuildType.PUBLIC).getGuild();
		String name = args[0];
		List<Member> targets = DiscordMember.get(guild, name, message.getMentionedMembers());
		targets = guild.getMembersByEffectiveName(name, true);
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
				} else
					sj.add(t.getAsMention());
				numberEmoji = numberEmoji.getNext();
			}
			em.setDescription(sj.toString());
			channel.sendMessage(em.build()).queue(m -> AwaitReaction.addReaction(m, new MuteChooseCommand(m, data, member.getIdLong())));

			return;
		} else {
			Member target = targets.get(0);
			try {
				SanctionHandler.addSanctionFromMsg(target, message, DiscordSanctionType.MUTE);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
