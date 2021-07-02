package fr.olympa.bot.discord.commands;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.OlympaDiscord;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import fr.olympa.bot.discord.sql.DiscordSQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class PermissionCommand extends DiscordCommand {

	public PermissionCommand() {
		super("permission", DiscordPermission.ADMIN, "removepermission", "addpermission", "p", "padd", "premove");
		description = "Donne des permissions pour les commandes du bots.";
		usage = "<pseudo|idDiscord|tag> [permission]";
		minArg = 1;
		privateChannel = false;
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		OlympaDiscord discord = OlympaBots.getInstance().getDiscord();
		MessageChannel channel = message.getChannel();
		DiscordMember discordMember = null;
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(discord.getColor());
		List<Member> members = message.getMentionedMembers();
		Member memberTarget = null;
		if (!members.isEmpty())
			memberTarget = members.get(0);
		else
			memberTarget = getMember(message.getGuild(), args[0]);
		if (memberTarget == null) {
			embed.setTitle("Erreur");
			embed.setDescription("Membre " + String.join(" ", args) + " introuvable.");
			channel.sendMessageEmbeds(embed.build()).queue();
			return;
		}
		discordMember = command.getDm();
		if (discordMember == null) {
			embed.setTitle("Erreur");
			embed.setDescription("Données de " + memberTarget.getAsMention() + " introuvables.");
			channel.sendMessageEmbeds(embed.build()).queue();
			return;
		}
		if (args.length == 1) {
			embed.setTitle("Permission de " + memberTarget.getEffectiveName());
			embed.setDescription(discordMember.getPermissionsWithOlympaGuild().entrySet().stream().map(entry -> entry.getKey().getName()).collect(Collectors.joining("\n")));
			channel.sendMessageEmbeds(embed.build()).queue(m -> m.delete().queueAfter(OlympaDiscord.getTimeToDelete(), TimeUnit.SECONDS));
			return;
		}
		DiscordPermission newPermission = DiscordPermission.getByName(args[1]);
		if (newPermission == null) {
			embed.setTitle("Erreur");
			embed.setDescription("La permission discord " + args[1] + " n'existe pas.");
			channel.sendMessageEmbeds(embed.build()).queue();
			return;
		}
		if (label.equalsIgnoreCase("removepermission") || label.equalsIgnoreCase("premove")) {
			discordMember.removePermission(newPermission);
			embed.setTitle("Permission retirée à " + memberTarget.getEffectiveName() + ".");
		} else if (label.equalsIgnoreCase("addpermission") || label.equalsIgnoreCase("padd")) {
			discordMember.addPermission(newPermission, (OlympaGuild) null);
			embed.setTitle("Permission ajouté à " + memberTarget.getEffectiveName() + ".");
		}
		CacheDiscordSQL.update(discordMember);
		try {
			DiscordSQL.updateMember(discordMember);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		embed.setDescription(newPermission.getName() + " (" + newPermission.getAllow().stream().map(dg -> dg.getOlympaGroup().getName()).collect(Collectors.joining(", ")) + ")");
		channel.sendMessageEmbeds(embed.build()).queue(m -> m.delete().queueAfter(OlympaDiscord.getTimeToDelete(), TimeUnit.SECONDS));
	}

}
