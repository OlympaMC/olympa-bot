package fr.olympa.bot.discord.groups;

import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GroupListener extends ListenerAdapter {
	@Override
	public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = member.getUser();
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
		if (olympaGuild.getType() != DiscordGuildType.PUBLIC || user.isBot() || !DiscordGroup.isStaff(event.getRoles()))
			return;
		GroupHandler.update();
	}

	@Override
	public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = member.getUser();
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
		if (olympaGuild.getType() != DiscordGuildType.PUBLIC || user.isBot() || !DiscordGroup.isStaff(event.getRoles()))
			return;
		GroupHandler.update();
	}

	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		Guild guild = event.getGuild();
		Member member = event.getMember();
		User user = member.getUser();
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
		if (olympaGuild.getType() != DiscordGuildType.PUBLIC || user.isBot() || !DiscordGroup.isStaff(member.getRoles()))
			return;
		GroupHandler.update();
	}
}
