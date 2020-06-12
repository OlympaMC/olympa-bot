package fr.olympa.bot.discord.groups;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

public class GroupHandler {

	public static void update() {
		Guild guild = GuildHandler.getOlympaGuild(DiscordGuildType.PUBLIC).getGuild();
		TextChannel channel = guild.getTextChannelById(558148740628611092L);
		channel.retrieveMessageById(697756335235792907L).queue(msg -> {
			EmbedBuilder mb = new EmbedBuilder().setTitle("Membres du Staff");
			Set<Member> staff = new HashSet<>();
			mb.setColor(OlympaBots.getInstance().getDiscord().getColor());
			mb.setTimestamp(OffsetDateTime.now());
			for (DiscordGroup discordGroup : DiscordGroup.values()) {
				if (!discordGroup.isStaff())
					continue;
				Role role = discordGroup.getRole(guild);
				if (role != null) {
					Set<Member> membersRole = guild.getMembersWithRoles(role).stream().filter(m -> !m.getUser().isBot()).collect(Collectors.toSet());
					String membersRoleS = membersRole.stream().map(Member::getAsMention).collect(Collectors.joining(", "));
					if (!membersRoleS.isEmpty()) {
						staff.addAll(membersRole);
						mb.addField(role.getName() + " (" + membersRole.size() + ")", membersRoleS, true);
					}
				}
			}

			mb.setDescription("Le staff est composé de " + staff.size() + " membres.");
			msg.editMessage(mb.build()).queue();
		});

		return;
	}

}
