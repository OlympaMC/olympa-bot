package fr.olympa.bot.discord.support;

import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.DiscordUtils;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.managers.ChannelManager;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;

public class SupportCommand extends DiscordCommand {

	public SupportCommand() {
		super("support", DiscordPermission.DEV, "aide", "ticket", "ticket", "tiket", "help");
		description = "Permet de créer un ticket d'aide.";
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		Guild guild = message.getGuild();
		DiscordUtils.deleteTempMessage(message);

		if (args.length == 0)
			return;

		if (args[0].equalsIgnoreCase("del")) {
			Category cat1 = SupportHandler.getCategory(guild);

			for (GuildChannel channel : cat1.getChannels())
				channel.getManager().clearOverridesAdded().queue(v -> channel.delete().queue());
			cat1.delete().queue();
			DiscordUtils.sendTempMessage(message.getChannel(), message.getMember(), "Tous les channels de Support ont été supprimer.");
			return;
		} else if (args[0].equalsIgnoreCase("test")) {

			SupportHandler.createChannel(message.getMember());
			DiscordUtils.sendTempMessage(message.getChannel(), message.getMember(), "Test en cours ...");

		} else if (args[0].equalsIgnoreCase("create"))
			guild.createCategory("🏳️ Support").queue(cat -> {
				Member author = guild.getMemberById(450125243592343563L);
				PermissionOverrideAction permissionAction5 = cat.createPermissionOverride(author);
				permissionAction5.setAllow(Permission.MESSAGE_ADD_REACTION, Permission.VIEW_CHANNEL, Permission.MESSAGE_MENTION_EVERYONE).queue();
				cat.getManager().setPosition(0).queue();

				Role defaultRole = guild.getPublicRole();
				PermissionOverrideAction permissionAction = cat.createPermissionOverride(defaultRole);
				permissionAction.deny(Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_MENTION_EVERYONE, Permission.VIEW_CHANNEL).queue(perm -> {
					for (Member members : guild.getMembers().stream().filter(m -> !m.getUser().isBot()).collect(Collectors.toSet()))
						cat.createTextChannel(members.getEffectiveName()).queue(textChannel -> {
							ChannelManager manager = textChannel.getManager();
							manager.setSlowmode(5).queue();
							manager.setTopic(members.getId() + " Ça c'est notre système de support écrit. Seul toi et le staff à accès aux messages.").queue();
							PermissionOverrideAction permissionAction2 = textChannel.createPermissionOverride(members);
							permissionAction2.setAllow(Permission.MESSAGE_READ).queue();

							List<Role> pententialRole = guild.getRolesByName("🏆 | Administrateur", true);
							if (!pententialRole.isEmpty()) {
								Role admin = pententialRole.get(0);
								PermissionOverrideAction permissionAction3 = textChannel.createPermissionOverride(admin);
								permissionAction3.setAllow(Permission.MESSAGE_READ, Permission.MESSAGE_MENTION_EVERYONE).queue();
							}
							PermissionOverrideAction permissionAction4 = textChannel.createPermissionOverride(author);
							permissionAction4.setAllow(Permission.MESSAGE_READ).queue();
							textChannel.sendMessage("Pour formuler votre demande au support, merci de fournir un maximum de détails.").queue();
						});
				});
			});

	}
}
