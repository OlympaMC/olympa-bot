package fr.olympa.bot.discord.support;

import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.bot.discord.api.DiscordUtils;
import fr.olympa.bot.discord.commands.api.DiscordCommand;
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
		super("support", "aide", "ticket", "ticket", "tiket", "help");
		this.permission = Permission.MESSAGE_MANAGE;
	}
	
	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message) {
		Guild guild = message.getGuild();
		DiscordUtils.deleteTempMessage(message);
		
		if (args.length == 0) {
			DiscordUtils.sendTempMessage(message.getChannel(), "");
		}

		if (args[0].equalsIgnoreCase("del")) {
			List<Category> cats = guild.getCategoriesByName("🏳 Support", true);
			if (cats.isEmpty()) {
				return;
			}
			Category cat1 = cats.get(0);
			for (GuildChannel channel : cat1.getChannels()) {
				channel.delete().queue();
			}
			cat1.delete().queue();
			DiscordUtils.sendTempMessage(message.getChannel(), message.getMember(), "Tous les channels de Support ont été supprimer.");
			return;
		} else if (args[0].equalsIgnoreCase("test")) {

			SupportHandler.createChannel(message.getMember());

		} else if (args[0].equalsIgnoreCase("create")) {
			guild.createCategory("🏳️ Support").queue(cat -> {
				cat.getManager().setPosition(0).queue();

				Role defaultRole = guild.getPublicRole();
				PermissionOverrideAction permissionAction = cat.createPermissionOverride(defaultRole);
				permissionAction.deny(Permission.VIEW_CHANNEL).queue(perm -> {
					for (Member members : guild.getMembers().stream().filter(m -> !m.getUser().isBot()).collect(Collectors.toSet())) {
						cat.createTextChannel(members.getEffectiveName()).queue(textChannel -> {
							ChannelManager manager = textChannel.getManager();
							manager.setSlowmode(5).queue();
							manager.setTopic(members.getId() + " Ça c'est notre système de support écrit. Seul toi et le staff à accès aux messages.").queue();
							PermissionOverrideAction permissionAction2 = textChannel.createPermissionOverride(members);
							permissionAction2.setAllow(Permission.MESSAGE_READ, Permission.VIEW_CHANNEL).queue();

							Member author = guild.getMemberById(450125243592343563L);
							List<Role> pententialRole = guild.getRolesByName("🏆 | Administrateur", true);
							if (!pententialRole.isEmpty()) {
								Role admin = pententialRole.get(0);
								PermissionOverrideAction permissionAction3 = textChannel.createPermissionOverride(admin);
								PermissionOverrideAction permissionAction4 = textChannel.createPermissionOverride(author);
								permissionAction4.setAllow(Permission.MESSAGE_READ, Permission.VIEW_CHANNEL).queue();
								permissionAction3.setAllow(Permission.MESSAGE_READ, Permission.VIEW_CHANNEL).queue();
							}
							textChannel.sendMessage("Pour formuler votre demande au support, merci de fournir un maximum de détails.").queue();
						});
					}
				});
			});
		}

	}
}
