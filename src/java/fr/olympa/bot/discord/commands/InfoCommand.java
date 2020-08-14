package fr.olympa.bot.discord.commands;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.olympa.api.utils.Utils;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.OlympaDiscord;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import fr.olympa.bot.discord.groups.DiscordGroup;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;

public class InfoCommand extends DiscordCommand {

	public InfoCommand() {
		super("info", DiscordPermission.ASSISTANT, "credit", "info");
		description = "[ancien|boost|nonsigne|signe|absent|bot|role]";
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		OlympaDiscord discord = OlympaBots.getInstance().getDiscord();
		MessageChannel channel = message.getChannel();
		deleteMessageAfter(message);
		JDA jda = message.getJDA();

		if (args.length == 0) {
			SelfUser user = jda.getSelfUser();
			List<Guild> guilds = jda.getGuilds();
			int usersConnected = 0;
			int usersTotal = 0;
			for (User user2 : jda.getUsers()) {
				if (!user2.isBot()) {
					Guild firstGuild = user2.getMutualGuilds().get(0);
					Member member2 = firstGuild.getMember(user2);
					if (member2.getOnlineStatus() != OnlineStatus.OFFLINE) {
						usersConnected++;
					}
					usersTotal++;
				}
			}
			User author = jda.getUserById(450125243592343563L);
			EmbedBuilder embed = new EmbedBuilder()
					.setTitle("Informations")
					.addField("Nom", user.getName(), true)
					.addField("Prefix", DiscordCommand.prefix, true)
					.addField("Ping", String.valueOf(jda.getGatewayPing()), true)
					.addField("Clients", usersConnected + "/" + usersTotal, true)
					.addField("Serveurs Discord", String.valueOf(guilds.size()), true)
					.addField("Donnés envoyés", String.valueOf(jda.getResponseTotal()), true)
					.addField("Connecté depuis ", Utils.timestampToDuration(OlympaDiscord.lastConnection), true)
					.setFooter("Dev " + author.getName(), author.getAvatarUrl());
			embed.setColor(discord.getColor());
			channel.sendMessage(embed.build()).queue(m -> m.delete().queueAfter(discord.timeToDelete, TimeUnit.SECONDS));
			return;
		}

		switch (Utils.removeAccents(args[0]).toLowerCase()) {
		case "ancien":
		case "vieux":
			List<Member> older = message.getGuild().getMembers().stream().sorted((e1, e2) -> e1.getTimeJoined().compareTo(e2.getTimeJoined())).limit(25).collect(Collectors.toList());
			// TextChannel test =
			// message.getGuild().getTextChannelById(558356359805009931L);
			// test.sendMessage(older.stream().map(Member::getAsMention).collect(Collectors.joining(",
			// "))).queue(msg -> msg.delete().queueAfter(30, TimeUnit.SECONDS));
			EmbedBuilder embed = new EmbedBuilder();
			embed.setTitle("Les 25 membres les plus anciens:");
			embed.setDescription(older.stream().map(m -> m.getUser().getAsMention() + " depuis " + Utils.timestampToDuration(m.getTimeJoined().toEpochSecond())).collect(Collectors.joining("\n")));
			embed.setColor(discord.getColor());
			channel.sendMessage(embed.build()).queue();
			break;
		case "nitro":
		case "boost":
			Stream<Member> boost = message.getGuild().getBoosters().stream().sorted((e1, e2) -> e1.getTimeBoosted().compareTo(e2.getTimeBoosted())).limit(25);
			embed = new EmbedBuilder();
			embed.setTitle("Les Nitros Boost:");
			embed.setDescription(boost.map(m -> m.getAsMention() + " depuis " + Utils.timestampToDuration(m.getTimeBoosted().toEpochSecond())).collect(Collectors.joining("\n")));
			embed.setColor(discord.getColor());
			channel.sendMessage(embed.build()).queue();
			break;
		case "bots":
		case "bot":
			Set<Member> bots = message.getGuild().getMembers().stream().filter(m -> m.getUser().isBot()).collect(Collectors.toSet());
			embed = new EmbedBuilder();
			embed.setTitle("Les Bots: (" + bots.size() + ")");
			embed.setDescription(bots.stream().map(m -> m.getAsMention()).collect(Collectors.joining(", ")));
			embed.setColor(discord.getColor());
			channel.sendMessage(embed.build()).queue();
			break;
		case "nonsigne":
			Guild guild = message.getGuild();
			if (GuildHandler.getOlympaGuild(DiscordGuildType.STAFF).getGuild().getIdLong() != guild.getIdLong()) {
				return;
			}
			Role roleSigned = DiscordGroup.SIGNED.getRole(guild);
			Set<Member> signed = guild.getMembers().stream().filter(m -> m.getRoles().contains(roleSigned) && !m.getUser().isBot()).collect(Collectors.toSet());
			Set<Member> noSigned = guild.getMemberCache().asSet().stream().filter(m -> !signed.contains(m) && DiscordGroup.isStaff(m) && !m.getUser().isBot()).collect(Collectors.toSet());
			int totalSize = noSigned.size() + signed.size();
			embed = new EmbedBuilder();
			embed.setTitle("Ceux qui n'ont pas signé la clause sont : (" + noSigned.size() + "/" + totalSize + ")");
			embed.setDescription(noSigned.stream().map(m -> m.getAsMention()).collect(Collectors.joining(", ")));
			embed.setColor(discord.getColor());
			channel.sendMessage(embed.build()).queue();
			break;
		case "signe":
			guild = message.getGuild();
			if (GuildHandler.getOlympaGuild(DiscordGuildType.STAFF).getGuild().getIdLong() != guild.getIdLong()) {
				return;
			}
			roleSigned = DiscordGroup.SIGNED.getRole(guild);
			signed = guild.getMembers().stream().filter(m -> m.getRoles().contains(roleSigned) && !m.getUser().isBot()).collect(Collectors.toSet());
			noSigned = guild.getMemberCache().asSet().stream().filter(m -> !signed.contains(m) && DiscordGroup.isStaff(m) && !m.getUser().isBot()).collect(Collectors.toSet());
			totalSize = noSigned.size() + signed.size();
			embed = new EmbedBuilder();
			embed.setTitle("Ceux qui ont signé la clause sont : (" + signed.size() + "/" + totalSize + ")");
			embed.setDescription(signed.stream().map(m -> m.getAsMention()).collect(Collectors.joining(", ")));
			embed.setColor(discord.getColor());
			channel.sendMessage(embed.build()).queue();
			break;
		case "joueur":
		case "membre":
			List<Member> members = message.getMentionedMembers();
			if (members.isEmpty()) {
				return;
			}
			Member member = members.get(0);
			User user = member.getUser();
			embed = new EmbedBuilder();
			embed.setTitle("Informations ");
			embed.setDescription(member.getAsMention());
			embed.setImage(user.getAvatarUrl());
			embed.setColor(discord.getColor());
			String t = Utils.timestampToDuration(user.getTimeCreated().toEpochSecond());
			String date = user.getTimeCreated().format(DateTimeFormatter.ISO_LOCAL_DATE.localizedBy(Locale.FRANCE));
			embed.addField("Compte créé", date + " (" + t + ")", true);
			t = Utils.timestampToDuration(member.getTimeJoined().toEpochSecond());
			date = member.getTimeJoined().format(DateTimeFormatter.ISO_LOCAL_DATE.localizedBy(Locale.FRANCE));
			embed.addField("Membre depuis", date + " (" + t + ")", true);
			DiscordMember discordMember;
			try {
				discordMember = CacheDiscordSQL.getDiscordMember(user);
				DecimalFormat df = new DecimalFormat("##.##");
				embed.addField("XP", df.format(discordMember.getXp()), true);
				embed.addField("Compte lié", discordMember.getOlympaId() != 0 ? "✅" : "❌", true);
				OnlineStatus onlineStatus = member.getOnlineStatus();
				if (onlineStatus == OnlineStatus.OFFLINE && discordMember.getLastSeenTime() != 0) {
					embed.addField("Dernière Action", Utils.timestampToDuration(Utils.getCurrentTimeInSeconds() - discordMember.getLastSeenTime()), true);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			channel.sendMessage(embed.build()).queue();
			break;
		case "roles":
		case "role":
			List<Role> roles = message.getMentionedRoles();
			members = message.getGuild().getMembersWithRoles(roles);
			embed = new EmbedBuilder();
			embed.setTitle("Membre avec le role " + roles.stream().map(Role::getName).collect(Collectors.joining(", ")) + ": ");
			embed.setDescription(members.stream().map(m -> m.getAsMention()).collect(Collectors.joining(", ")));
			embed.setColor(discord.getColor());
			channel.sendMessage(embed.build()).queue();
			break;
		case "absent":
			guild = message.getGuild();
			if (GuildHandler.getOlympaGuild(DiscordGuildType.STAFF).getGuild().getIdLong() != guild.getIdLong()) {
				return;
			}
			Role roleAbsent = DiscordGroup.ABSENT.getRole(guild);

			embed = new EmbedBuilder();
			embed.setTitle("Membre avec le role " + roleAbsent.getName() + ": ");
			embed.setDescription(guild.getMembersWithRoles(roleAbsent).stream().map(m -> m.getAsMention()).collect(Collectors.joining(", ")));
			embed.setColor(discord.getColor());
			channel.sendMessage(embed.build()).queue();
			break;
		}
	}

}
