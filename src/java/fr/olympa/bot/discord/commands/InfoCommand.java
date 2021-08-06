package fr.olympa.bot.discord.commands;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.olympa.api.common.player.OlympaPlayer;
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
import fr.olympa.core.common.provider.AccountProvider;
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
		super("info", DiscordPermission.STAFF, "credit");
		description = "Donne diverses informations ";
		usage = "[membre|ancien|boost|nonsigne|signe|absent|bot|role]";
		privateChannel = true;
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		OlympaDiscord discord = OlympaBots.getInstance().getDiscord();
		MessageChannel channel = message.getChannel();

		if (args.length == 0) {
			JDA jda = message.getJDA();
			SelfUser me = jda.getSelfUser();
			List<Guild> guilds = jda.getGuilds();
			int usersConnected = 0;
			int usersTotal = 0;
			for (User user2 : jda.getUsers())
				if (!user2.isBot()) {
					Guild firstGuild = user2.getMutualGuilds().get(0);
					Member member2 = firstGuild.getMember(user2);
					if (member2.getOnlineStatus() != OnlineStatus.OFFLINE)
						usersConnected++;
					usersTotal++;
				}
			EmbedBuilder embed = new EmbedBuilder()
					.setTitle("Informations")
					.addField("Nom", me.getName(), true)
					.addField("Prefix", DiscordCommand.prefix, true)
					.addField("Ping", String.valueOf(jda.getGatewayPing()), true)
					.addField("Clients", usersConnected + "/" + usersTotal, true)
					.addField("Serveurs Discord", String.valueOf(guilds.size()), true)
					.addField("Données envoyées", String.valueOf(jda.getResponseTotal()), true)
					.addField("Connecté depuis ", OlympaDiscord.connectedFrom(), true);
			embed.setColor(discord.getColor());
			channel.sendMessageEmbeds(embed.build()).queue(m -> m.delete().queueAfter(OlympaDiscord.getTimeToDelete(), TimeUnit.SECONDS));
			return;
		}
		User user = message.getAuthor();

		switch (Utils.removeAccents(args[0]).toLowerCase()) {
		case "ancien", "vieux":
			if (!checkPrivateChannel(message, user))
				return;
			List<Member> older = message.getGuild().getMembers().stream().sorted((e1, e2) -> e1.getTimeJoined().compareTo(e2.getTimeJoined())).limit(25).toList();
			EmbedBuilder embed = new EmbedBuilder();
			embed.setTitle("Les 25 membres les plus anciens :");
			embed.setDescription(older.stream().map(m -> "`" + m.getUser().getAsTag() + "` depuis " + Utils.timestampToDuration(m.getTimeJoined().toEpochSecond())).collect(Collectors.joining("\n")));
			embed.setColor(discord.getColor());
			channel.sendMessageEmbeds(embed.build()).queue();
			break;
		case "nitro", "boost":
			if (!checkPrivateChannel(message, user))
				return;
			Stream<Member> boost = message.getGuild().getBoosters().stream().sorted((e1, e2) -> e1.getTimeBoosted().compareTo(e2.getTimeBoosted())).limit(25);
			embed = new EmbedBuilder();
			embed.setTitle("Les Nitros Boost :");
			embed.setDescription(boost.map(m -> m.getAsMention() + " depuis " + Utils.timestampToDuration(m.getTimeBoosted().toEpochSecond())).collect(Collectors.joining("\n")));
			embed.setColor(discord.getColor());
			channel.sendMessageEmbeds(embed.build()).queue();
			break;
		case "bots", "bot":
			if (!checkPrivateChannel(message, user))
				return;
			Set<Member> bots = message.getGuild().getMembers().stream().filter(m -> m.getUser().isBot()).collect(Collectors.toSet());
			embed = new EmbedBuilder();
			embed.setTitle("Les Bots: (" + bots.size() + ")");
			embed.setDescription(bots.stream().map(Member::getAsMention).collect(Collectors.joining(", ")));
			embed.setColor(discord.getColor());
			channel.sendMessageEmbeds(embed.build()).queue();
			break;
		case "nonsigne":
			if (!checkPrivateChannel(message, user))
				return;
			Guild guild = message.getGuild();
			if (GuildHandler.getOlympaGuild(DiscordGuildType.STAFF).getGuild().getIdLong() != guild.getIdLong())
				return;
			Role roleSigned = DiscordGroup.SIGNED.getRole(guild);
			Set<Member> signed = guild.getMembers().stream().filter(m -> m.getRoles().contains(roleSigned) && !m.getUser().isBot()).collect(Collectors.toSet());
			Set<Member> noSigned = guild.getMemberCache().asSet().stream().filter(m -> !signed.contains(m) && DiscordGroup.isStaff(m) && !m.getUser().isBot()).collect(Collectors.toSet());
			int totalSize = noSigned.size() + signed.size();
			embed = new EmbedBuilder();
			embed.setTitle("Ceux qui n'ont pas signé la clause sont : (" + noSigned.size() + "/" + totalSize + ")");
			embed.setDescription(noSigned.stream().map(Member::getAsMention).collect(Collectors.joining(", ")));
			embed.setColor(discord.getColor());
			channel.sendMessageEmbeds(embed.build()).queue();
			break;
		case "signe":
			if (!checkPrivateChannel(message, user))
				return;
			guild = message.getGuild();
			if (GuildHandler.getOlympaGuild(DiscordGuildType.STAFF).getGuild().getIdLong() != guild.getIdLong())
				return;
			roleSigned = DiscordGroup.SIGNED.getRole(guild);
			signed = guild.getMembers().stream().filter(m -> m.getRoles().contains(roleSigned) && !m.getUser().isBot()).collect(Collectors.toSet());
			noSigned = guild.getMemberCache().asSet().stream().filter(m -> !signed.contains(m) && DiscordGroup.isStaff(m) && !m.getUser().isBot()).collect(Collectors.toSet());
			totalSize = noSigned.size() + signed.size();
			embed = new EmbedBuilder();
			embed.setTitle("Ceux qui ont signé la clause sont : (" + signed.size() + "/" + totalSize + ")");
			embed.setDescription(signed.stream().map(Member::getAsMention).collect(Collectors.joining(", ")));
			embed.setColor(discord.getColor());
			channel.sendMessageEmbeds(embed.build()).queue();
			break;
		case "joueur", "membre":
			embed = new EmbedBuilder();
			List<Member> members = message.getMentionedMembers();
			Member memberTarget = null;
			if (!members.isEmpty())
				memberTarget = members.get(0);
			else
				memberTarget = getMember(message.getGuild(), buildText(1, args));
			if (memberTarget == null) {
				embed.setTitle("Erreur");
				embed.setDescription("Membre " + (args.length > 1 ? args[1] : "") + " introuvable.");
				channel.sendMessageEmbeds(embed.build()).queue();
				return;
			}
			User usertarget = memberTarget.getUser();
			embed.setTitle("Informations");
			embed.setDescription(memberTarget.getAsMention());
			embed.setImage(usertarget.getAvatarUrl());
			embed.setColor(discord.getColor());
			String t = Utils.timestampToDuration(usertarget.getTimeCreated().toEpochSecond());
			String date = Utils.timestampToDate(usertarget.getTimeCreated().toEpochSecond());
			embed.addField("Compte créé", date + " (" + t + ")", true);
			t = Utils.timestampToDuration(memberTarget.getTimeJoined().toEpochSecond());
			date = Utils.timestampToDate(memberTarget.getTimeJoined().toEpochSecond());
			embed.addField("Membre depuis", date + " (" + t + ")", true);
			embed.setFooter(usertarget.getAsTag() + "|" + (memberTarget.getNickname() != null ? memberTarget.getNickname() + "|" : "") + usertarget.getIdLong());
			DiscordMember discordMember;
			try {
				discordMember = CacheDiscordSQL.getDiscordMember(usertarget);
				if (discordMember.getOlympaId() != 0) {
					OlympaPlayer olympaTarget = null;
					olympaTarget = AccountProvider.getter().get(discordMember.getOlympaId());
					embed.setThumbnail("https://minotar.net/helm/" + olympaTarget.getName());
					embed.addField("Compte Minecraft :", olympaTarget.getName(), true);
				}
				if (discordMember.getLeaveTime() != 0) {
					t = Utils.timestampToDuration(discordMember.getLeaveTime());
					date = Utils.timestampToDate(discordMember.getLeaveTime());
					embed.addField("Nous a quitté pour la dernière fois le ", date + " (" + t + ")", true);
				}
				if (discordMember.getPermissions() != null && !discordMember.getPermissions().isEmpty())
					embed.addField("Permissions extra " + discordMember.getPermissions().size(), discordMember.getPermissions().keySet().stream().map(dp -> dp.getName()).collect(Collectors.joining(", ")), true);
				if (!discordMember.getOldNames().isEmpty())
					embed.addField("Ancien noms :", discordMember.getOldNames().entrySet().stream().map(entry -> entry.getValue() + " (il y a " + Utils.timestampToDuration(entry.getKey()) + " )").collect(Collectors.joining(", ")), true);
				if (discordMember.getXp() != 0)
					embed.addField("XP", new DecimalFormat("0.#").format(discordMember.getXp()), true);
				if (discordMember.getLastSeenTime() != 0)
					embed.addField("Dernière Action", Utils.timestampToDuration(Utils.getCurrentTimeInSeconds() - discordMember.getLastSeenTime()), true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			channel.sendMessageEmbeds(embed.build()).queue();
			break;
		case "roles", "role":
			if (!checkPrivateChannel(message, user))
				return;
			List<Role> roles = message.getMentionedRoles();
			members = message.getGuild().getMembersWithRoles(roles);
			embed = new EmbedBuilder();
			embed.setTitle("Membre avec le role " + roles.stream().map(Role::getName).collect(Collectors.joining(", ")) + ": ");
			embed.setDescription(members.stream().map(Member::getAsMention).collect(Collectors.joining(", ")));
			embed.setColor(discord.getColor());
			channel.sendMessageEmbeds(embed.build()).queue();
			break;
		case "absent":
			if (!checkPrivateChannel(message, user))
				return;
			guild = message.getGuild();
			if (GuildHandler.getOlympaGuild(DiscordGuildType.STAFF).getGuild().getIdLong() != guild.getIdLong())
				return;
			Role roleAbsent = DiscordGroup.ABSENT.getRole(guild);

			embed = new EmbedBuilder();
			embed.setTitle("Membre avec le role " + roleAbsent.getName() + ": ");
			embed.setDescription(guild.getMembersWithRoles(roleAbsent).stream().map(Member::getAsMention).collect(Collectors.joining(", ")));
			embed.setColor(discord.getColor());
			channel.sendMessageEmbeds(embed.build()).queue();
			break;
		default:
			channel.sendMessage(user.getAsMention() + " > Usage .info " + usage).queue();
			break;
		}
	}

}
