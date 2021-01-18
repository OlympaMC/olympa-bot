package fr.olympa.bot.discord.invites;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import fr.olympa.api.utils.Utils;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class InviteCommand extends DiscordCommand {

	public InviteCommand() {
		super("invite", DiscordPermission.STAFF);
		description = "Donnes des stats concernant les invitations.";
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		MessageChannel channel = message.getChannel();
		Guild guild = message.getGuild();
		OlympaGuild opGuild = GuildHandler.getOlympaGuild(guild);
		try {
			List<DiscordInvite> invites = DiscordInvite.getAll(opGuild);
			if (args.length == 0) {
				Map<Long, Integer> stats = new HashMap<>();
				EmbedBuilder em = new EmbedBuilder();
				em.setTitle("💌 Invitations");
				for (DiscordInvite invite : invites) {
					long user = invite.getAuthorId();
					int uses = invite.getRealUse();
					Integer actualNb = stats.get(user);
					if (uses != 0) {
						if (actualNb != null)
							uses += actualNb;
						stats.put(user, uses);
					}
				}
				int nb = 1;
				TreeMap<Long, Integer> statsSorted = new TreeMap<>((o1, o2) -> stats.get(o2).compareTo(stats.get(o1)));
				statsSorted.putAll(stats);
				em.setDescription("Il y a " + stats.size() + " joueurs qui ont ramener " + stats.values().stream().mapToInt(Integer::valueOf).sum() + " joueurs.\n");
				for (Entry<Long, Integer> entry : statsSorted.entrySet()) {
					int uses = entry.getValue();
					Member member = guild.getMemberById(entry.getKey());
					String inviterName;
					if (member != null)
						inviterName = member.getAsMention() + "(" + member.getUser().getAsTag() + ")";
					else {
						DiscordMember author = CacheDiscordSQL.getDiscordMember(entry.getKey());
						inviterName = author.getAsMention() + "(" + author.getAsTag() + ")" + " (🚪 " + Utils.tsToShortDur(author.getLeaveTime()) + ")";
					}
					em.appendDescription(nb++ + " | " + uses + " joueurs " + inviterName + ".\n");
					if (em.getDescriptionBuilder().length() > 1800) {
						channel.sendMessage(em.build()).queue();
						em = new EmbedBuilder();
					}
				}
				channel.sendMessage(em.build()).queue();

			} else {
				EmbedBuilder em = new EmbedBuilder();
				Set<Long> invitesPeruser = invites.stream().map(invite -> invite.getAuthorId()).collect(Collectors.toSet());
				em.setTitle("💌 Invitations");
				em.setDescription("Il y a " + invitesPeruser.size() + " invitations par " + invitesPeruser.size() + " membres.\n");
				for (DiscordInvite invite : invites) {
					DiscordMember author;
					author = invite.getAuthor();
					Member member = author.getMember(guild);
					String inviterName;
					if (member != null)
						inviterName = member.getAsMention() + "(" + member.getUser().getAsTag() + ")";
					else
						inviterName = author.getAsMention() + "(" + author.getAsTag() + ")" + " (🚪 " + Utils.tsToShortDur(author.getLeaveTime()) + ")";
					StringBuilder smallSb = new StringBuilder();

					smallSb.append("Utilisé ~~" + invite.getUses() + "~~ " + invite.getRealUse() + " fois *" + invite.getUsesLeaver() + " ont quittés*");
					em.appendDescription(inviterName + ": " + smallSb.toString() + "\n");
					if (em.getDescriptionBuilder().length() > 1900) {
						channel.sendMessage(em.build()).queue(msg -> msg.delete().queueAfter(60, TimeUnit.SECONDS));
						em = new EmbedBuilder();
					}
				}
				channel.sendMessage(em.build()).queue(msg -> msg.delete().queueAfter(60, TimeUnit.SECONDS));
			}
		} catch (SQLException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
