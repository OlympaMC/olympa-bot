package fr.olympa.bot.discord.invites;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import fr.olympa.api.utils.Utils;
import fr.olympa.bot.OlympaBots;
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
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

public class InviteCommand extends DiscordCommand {

	public InviteCommand() {
		super("invite", "invitetop", "inviteall");
		description = "Donnes des stats concernant les invitations.";
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		MessageChannel channel = message.getChannel();
		Member member = this.member;
		Guild guild = message.getGuild();
		OlympaGuild opGuild = GuildHandler.getOlympaGuild(guild);
		EmbedBuilder em = new EmbedBuilder();
		em.setColor(OlympaBots.getInstance().getDiscord().getColor());
		try {
			if (label.equalsIgnoreCase("invite")) {
				DiscordMember discordMember = CacheDiscordSQL.getDiscordMember(message.getAuthor());
				MemberInvites mInv = new MemberInvites(opGuild, InvitesHandler.getByAuthor(opGuild, discordMember));
				em.setTitle("💌 Invitations de " + member.getEffectiveName());
				em.addField("Utilisation", String.valueOf(mInv.getRealUses()), true);
				em.addField("Nombre de leave", String.valueOf(mInv.getRealLeaves()), true);
				em.addField("Dont réinvité", String.valueOf(mInv.getReinvited()), true);
				em.addField("Utilisations Totales", String.valueOf(mInv.getTotalUses()), true);
				em.addField("Joueurs parrainés", mInv.getUsers().stream().map(DiscordMember::getAsMention).collect(Collectors.joining(", ")), true);
				em.addField("Classement du serveur", "n°" + DiscordInvite.getPosOfAuthor(opGuild, discordMember), true);
				channel.sendMessage(em.build()).queue();
			} else if (label.equalsIgnoreCase("invitetop")) {
				Map<Long, Integer> stats = DiscordInvite.getStats(opGuild);
				em.setTitle("💌 Invitations");
				int nb = 1;
				em.setDescription("Il y a " + stats.size() + " membres qui ont ramené " + stats.values().stream().mapToInt(Integer::valueOf).sum() + " joueurs.\n");
				for (Entry<Long, Integer> entry : stats.entrySet()) {
					int uses = entry.getValue();
					long userId = entry.getKey();
					DiscordMember author = CacheDiscordSQL.getDiscordMemberByDiscordOlympaId(userId);
					User user = author.getUser();
					String inviterName = author.getAsMention() + "(`" + author.getAsTag() + "`)";
					if (user != null && !guild.isMember(user) && author.getLeaveTime() != 0)
						inviterName += " (🚪 " + Utils.tsToShortDur(author.getLeaveTime()) + ")";
					String out = nb++ + " | `" + uses + " joueur" + Utils.withOrWithoutS(uses) + "` " + inviterName + ".\n";
					if (em.getDescriptionBuilder().length() + out.length() >= MessageEmbed.TEXT_MAX_LENGTH) {
						channel.sendMessage(em.build()).queue();
						em = new EmbedBuilder();
						em.setColor(OlympaBots.getInstance().getDiscord().getColor());
					}
					em.appendDescription(out);
				}
				channel.sendMessage(em.build()).queue();
			} else if (label.equalsIgnoreCase("inviteall") && DiscordPermission.STAFF.hasPermission(member)) {
				List<DiscordInvite> invites = DiscordInvite.getAll(opGuild);
				long invitesPerUser = invites.stream().map(invite -> invite.getAuthorId()).distinct().count();
				em.setTitle("💌 Invitations");
				em.setDescription("Il y a " + invites.size() + " invitations par " + invitesPerUser + " membres.\n");
				for (DiscordInvite invite : invites.stream().sorted(InvitesHandler.getComparator()).collect(Collectors.toList())) {
					DiscordMember author;
					author = invite.getAuthor();
					User user = author.getUser();
					String inviterName = author.getAsMention() + "(`" + author.getAsTag() + "`)";
					if (user != null && !guild.isMember(user) && author.getLeaveTime() != 0)
						inviterName += " (🚪 " + Utils.tsToShortDur(author.getLeaveTime()) + ")";
					StringBuilder smallSb = new StringBuilder();
					smallSb.append("Utilisé ~~" + invite.getUses() + "~~ `" + invite.getRealUse() + " fois`");
					if (invite.getUsesLeaver() != 0)
						smallSb.append(" *" + invite.getRealUsesLeaver() + " ont quitté" + Utils.withOrWithoutS(invite.getUsesLeaver()) + "*");
					String out = inviterName + ": " + smallSb.toString() + "\n";
					if (em.getDescriptionBuilder().length() + out.length() >= MessageEmbed.TEXT_MAX_LENGTH) {
						channel.sendMessage(em.build()).queue(msg -> msg.delete().queueAfter(1, TimeUnit.HOURS));
						em = new EmbedBuilder();
						em.setColor(OlympaBots.getInstance().getDiscord().getColor());
					}
					em.appendDescription(out);
				}
				channel.sendMessage(em.build()).queue(msg -> msg.delete().queueAfter(1, TimeUnit.HOURS));
			}
		} catch (SQLException | IllegalAccessException e) {
			e.printStackTrace();
			channel.sendMessage("Error > " + e.getMessage()).queue();
		}
	}
}
