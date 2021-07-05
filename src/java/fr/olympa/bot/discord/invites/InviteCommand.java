package fr.olympa.bot.discord.invites;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import fr.olympa.api.utils.Utils;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.DiscordUtils;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public class InviteCommand extends DiscordCommand {

	public InviteCommand() {
		super("invite", "invitetop", "inviteall", "inviteparrain", "invitefix");
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
				List<Member> members = message.getMentionedMembers();
				Member memberTarget = null;
				if (args.length != 0) {
					if (!members.isEmpty())
						memberTarget = members.get(0);
					else {
						memberTarget = getMember(message.getGuild(), args[0]);
						if (memberTarget == null) {
							channel.sendMessage(String.format("%s, %s est inconnu.", member.getAsMention(), args[0])).queue();
							return;
						}
					}
				} else
					memberTarget = member;
				DiscordMember dmTarget = CacheDiscordSQL.getDiscordMember(memberTarget.getUser());
				MemberInvites mInv = new MemberInvites(opGuild, InvitesHandler.getByAuthor(opGuild, dmTarget));
				em.setTitle("💌 Invitations de " + memberTarget.getEffectiveName());
				em.addField("Utilisations Uniques", String.valueOf(mInv.getRealUses()), true);
				em.addField("Nombre de quittés", String.valueOf(mInv.getRealLeaves()), true);
				// em.addField("Dont réinvité", String.valueOf(mInv.getReinvited()), true);
				int nb = DiscordInvite.getPosOfAuthor(opGuild, dmTarget);
				if (nb > 0)
					em.addField("Classement du serveur", "n°" + nb, true);
				em.addField("Utilisations Totales", String.valueOf(mInv.getTotalUses()), true);
				em.addField("Membres parrainés", mInv.getUsers().stream().map(DiscordMember::getAsMention).collect(Collectors.joining(", ")), false);
				em.addField("Liens", mInv.getInvites().stream().map(di -> di.getUrl()).collect(Collectors.joining(", ")), false);
				em.setFooter(DiscordCommand.prefix + "invitetop pour voir le classement");
				channel.sendMessageEmbeds(em.build()).queue();
			} else if (label.equalsIgnoreCase("inviteparrain")) {
				List<Member> members = message.getMentionedMembers();
				Member memberTarget = null;
				if (args.length != 0) {
					if (!members.isEmpty())
						memberTarget = members.get(0);
					else {
						memberTarget = getMember(message.getGuild(), args[0]);
						if (memberTarget == null) {
							channel.sendMessage(String.format("%s, %s est inconnu.", member.getAsMention(), args[0])).queue();
							return;
						}
					}
				} else
					memberTarget = member;
				DiscordMember dmTarget = CacheDiscordSQL.getDiscordMember(memberTarget.getUser());
				MemberInvites mInv = new MemberInvites(opGuild, InvitesHandler.getByAuthor(opGuild, dmTarget));
				StringJoiner sj = new StringJoiner("\n");
				Set<DiscordMember> users = mInv.getUsers();
				sj.add(String.format("**Membre%s parrainé%s** (%d) %s", Utils.withOrWithoutS(users.size()), Utils.withOrWithoutS(users.size()), users.size(), users.stream().map(DiscordMember::getAsTag)
						.collect(Collectors.joining(", "))));
				users = mInv.getLeavers();
				sj.add(String.format("**Membre%s %s quitté%s** (%d) %s", Utils.withOrWithoutS(users.size()), users.size() > 1 ? "qui ont" : "qui a", Utils.withOrWithoutS(users.size()), users.size(),
						users.stream().map(DiscordMember::getAsTag).collect(Collectors.joining(", "))));
				users = mInv.getUsersPast();
				sj.add(String.format("**Membre%s qui %s revenu avec une autre invitation** (%d) %s", Utils.withOrWithoutS(users.size()), users.size() > 1 ? "sont" : "est",
						users.size(), users.stream().map(DiscordMember::getAsTag).collect(Collectors.joining(", "))));
				channel.sendMessage(sj.toString()).allowedMentions(Arrays.asList(MentionType.EMOTE)).queue();
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
					if (user == null || !guild.isMember(user))
						inviterName += " (🚪 " + Utils.tsToShortDur(author.getLeaveTime()) + ")";
					String out = nb++ + " | `" + uses + " membre" + Utils.withOrWithoutS(uses) + "` " + inviterName + ".\n";
					if (em.getDescriptionBuilder().length() + out.length() >= MessageEmbed.TEXT_MAX_LENGTH)
						break;
					//						channel.sendMessage(em.build()).queue();
					//em = new EmbedBuilder();
					//					em.setColor(OlympaBots.getInstance().getDiscord().getColor());
					em.appendDescription(out);
				}
				em.setFooter(DiscordCommand.prefix + "invite [nom|mention] pour voir les stats d'un membre");
				channel.sendMessageEmbeds(em.build()).queue();
			} else if (label.equalsIgnoreCase("inviteall")) {
				List<MessageEmbed> embeds = new ArrayList<>();
				if (!DiscordPermission.STAFF.hasPermission(member)) {
					MessageAction out = channel.sendMessage(user.getAsMention() + " ➤ Tu n'a pas la permission :open_mouth:.");
					if (!message.isFromGuild())
						out.queue();
					else {
						DiscordUtils.deleteTempMessage(message);
						DiscordUtils.sendTempMessage(out);
					}
				}
				List<DiscordInvite> invites = DiscordInvite.getAll(opGuild);
				em.setTitle("💌 Invitations");
				if (invites.isEmpty()) {
					em.setDescription("Il n'y a aucune données concernant les invitations.");
					channel.sendMessageEmbeds(em.build()).queue(msg -> msg.delete().queueAfter(1, TimeUnit.MINUTES));
				} else {
					long invitesPerUser = invites.stream().map(invite -> invite.getAuthorId()).distinct().count();
					em.setDescription("Il y a " + invites.size() + " invitations par " + invitesPerUser + " membres.\n");
					for (DiscordInvite invite : invites.stream().sorted(InvitesHandler.getComparator()).collect(Collectors.toList())) {
						DiscordMember author;
						author = invite.getAuthor();
						User user = author.getUser();
						String inviterName = author.getAsMention() + "(`" + author.getAsTag() + "`)";
						if (user != null && !guild.isMember(user) && author.getLeaveTime() != 0)
							inviterName += " (🚪 " + Utils.tsToShortDur(author.getLeaveTime()) + ")";
						StringBuilder smallSb = new StringBuilder();
						smallSb.append("Utilisé ~~" + invite.getUses() + "~~ `" + invite.getUsesUnique() + " fois`");
						if (invite.getUsesLeaver() != 0)
							smallSb.append(" *" + invite.getRealUsesLeaver() + " ont quitté" + Utils.withOrWithoutS(invite.getUsesLeaver()) + "*");
						String out = inviterName + ": " + smallSb.toString() + "\n";
						if (em.getDescriptionBuilder().length() + out.length() >= MessageEmbed.TEXT_MAX_LENGTH) {
							embeds.add(em.build());
							if (embeds.size() == 5) {
								channel.sendMessageEmbeds(embeds).queue(msg -> msg.delete().queueAfter(1, TimeUnit.MINUTES));
								embeds.clear();
							}
							em = new EmbedBuilder();
							em.setColor(OlympaBots.getInstance().getDiscord().getColor());
						}
						em.appendDescription(out);
					}
					if (!embeds.isEmpty())
						channel.sendMessageEmbeds(embeds).queue(msg -> msg.delete().queueAfter(1, TimeUnit.MINUTES));
				}
			} else if (label.equalsIgnoreCase("invitefix")) {
				List<DiscordInvite> targetInvites = null;

				if (!DiscordPermission.STAFF.hasPermission(member)) {
					MessageAction out = channel.sendMessage(user.getAsMention() + " ➤ Tu n'a pas la permission :open_mouth:.");
					if (!message.isFromGuild())
						out.queue();
					else {
						DiscordUtils.deleteTempMessage(message);
						DiscordUtils.sendTempMessage(out);
					}
				}
				List<Member> members = message.getMentionedMembers();
				Member memberTarget = null;
				if (args.length != 0) {
					if (!members.isEmpty())
						memberTarget = members.get(0);
					else if (args[0].equals("ALL"))
						targetInvites = DiscordInvite.getAll(opGuild);
					else {
						memberTarget = getMember(message.getGuild(), args[0]);
						if (memberTarget == null) {
							channel.sendMessage(String.format("%s, %s est inconnu.", member.getAsMention(), args[0])).queue();
							return;
						}
					}
				} else
					memberTarget = member;
				if (memberTarget != null) {
					DiscordMember dmTarget = CacheDiscordSQL.getDiscordMember(memberTarget.getUser());
					targetInvites = InvitesHandler.getByAuthor(opGuild, dmTarget);
				}
				em.setDescription("Les invitation qui ont été fixés (" + targetInvites.size() + (memberTarget != null ? " " + member.getAsMention() : "") + ") :");
				for (DiscordInvite di : targetInvites)
					try {
						boolean b = di.fixInvite();
						em.addField(di.getCode(), b ? "✅" : "❌", true);
						if (b)
							di.update();
					} catch (Exception e) {
						e.printStackTrace();
						em.addField(di.getCode(), "Erreur > `" + e.getMessage() + "`", true);
					}
				channel.sendMessageEmbeds(em.build()).queue();
			}
		} catch (SQLException | IllegalAccessException e) {
			e.printStackTrace();
			channel.sendMessage("Erreur > `" + e.getMessage() + "`").queue();
		}
	}
}
