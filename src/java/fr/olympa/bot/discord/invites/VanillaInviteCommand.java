package fr.olympa.bot.discord.invites;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public class VanillaInviteCommand extends DiscordCommand {

	public VanillaInviteCommand() {
		super("vanillainvite", DiscordPermission.STAFF, "oldinvite");
		description = "Donnes des stats concernant les invitations.";
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		MessageChannel channel = message.getChannel();
		Guild guild = message.getGuild();

		if (args.length == 0) {
			Map<User, Integer> stats = new HashMap<>();
			guild.retrieveInvites().queue(invites -> {
				EmbedBuilder em = new EmbedBuilder();
				em.setTitle("💌 Invitations");
				for (Invite invite : invites) {
					User user = invite.getInviter();
					int uses = invite.getUses();
					Integer actualNb = stats.get(user);
					if (uses != 0) {
						if (actualNb != null)
							uses += actualNb;
						stats.put(user, uses);
					}
				}
				int nb = 1;
				TreeMap<User, Integer> statsSorted = new TreeMap<>((o1, o2) -> stats.get(o2).compareTo(stats.get(o1)));
				statsSorted.putAll(stats);
				em.setDescription("Il y a " + statsSorted.size() + " joueurs qui ont ramener " + stats.values().stream().mapToInt(Integer::valueOf).sum() + " joueurs.\n");
				for (Entry<User, Integer> entry : statsSorted.entrySet()) {
					User user = entry.getKey();
					Integer uses = entry.getValue();
					Member member = guild.getMember(user);
					String inviterName;
					if (member != null)
						inviterName = member.getAsMention() + "(" + member.getUser().getAsTag() + ")";
					else
						inviterName = user.getAsMention() + "(" + user.getAsTag() + ") 🚪 ";
					em.appendDescription(nb++ + " | " + uses + " joueurs " + inviterName + ".\n");
					if (em.getDescriptionBuilder().length() > 1800) {
						channel.sendMessage(em.build()).queue();
						em = new EmbedBuilder();
					}
				}
				channel.sendMessage(em.build()).queue();
			});
		} else {
			EmbedBuilder em = new EmbedBuilder();
			List<Invite> invites = guild.retrieveInvites().complete();
			Set<Long> invitesPeruser = invites.stream().map(invite -> invite.getInviter().getIdLong()).collect(Collectors.toSet());

			em.setTitle("💌 Invitations");
			em.setDescription("Il y a " + invites.size() + " invations par " + invitesPeruser.size() + " membres.\n");
			for (Invite invite : invites) {
				User user = invite.getInviter();
				Member member = guild.getMember(user);
				String inviterName;
				if (member != null)
					inviterName = member.getAsMention() + "(" + member.getEffectiveName() + ")";
				else
					inviterName = user.getAsMention() + "(" + user.getAsTag() + ") 🚪 ";
				StringBuilder smallSb = new StringBuilder();
				int maxAge = 0;
				int maxInvite = 0;
				int uses = 0;
				if (invite.isExpanded()) {
					maxAge = invite.getMaxAge();
					maxInvite = invite.getMaxUses();
					uses = invite.getUses();
				}
				String timeCreated = invite.getTimeCreated().format(DateTimeFormatter.ISO_LOCAL_DATE);

				smallSb.append("Utilisé " + uses + " fois ");
				if (maxInvite != 0 || maxAge != 0) {
					smallSb.append("Valable ");
					if (maxInvite != 0)
						smallSb.append(maxInvite + " fois ");
					if (maxAge != 0) {
						if (maxInvite != 0)
							smallSb.append("et ");
						smallSb.append("pendant " + maxAge + " secondes ");
					}
				}
				smallSb.append("Crée le " + timeCreated + " ");
				em.appendDescription(inviterName + ": " + smallSb.toString() + "\n");
				if (em.getDescriptionBuilder().length() > 1900) {
					channel.sendMessage(em.build()).queue(msg -> msg.delete().queueAfter(60, TimeUnit.SECONDS));
					em = new EmbedBuilder();
				}
			}
			channel.sendMessage(em.build()).queue(msg -> msg.delete().queueAfter(60, TimeUnit.SECONDS));
		}
	}
}
