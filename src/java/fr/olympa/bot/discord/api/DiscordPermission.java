package fr.olympa.bot.discord.api;

import java.util.Arrays;
import java.util.List;

import fr.olympa.bot.discord.groups.DiscordGroup;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public enum DiscordPermission {

	HIGH_STAFF(DiscordGroup.RESP_TECH, DiscordGroup.RESP_ANIMATION, DiscordGroup.RESP_STAFF, DiscordGroup.RESP_BUILDER, DiscordGroup.MODP, DiscordGroup.ADMIN, DiscordGroup.FONDA),
	MODERATOR(DiscordGroup.MOD, DiscordGroup.MODP, DiscordGroup.ADMIN, DiscordGroup.FONDA),
	ASSISTANT(DiscordGroup.MOD, DiscordGroup.MODP, DiscordGroup.ADMIN, DiscordGroup.FONDA, DiscordGroup.ASSISTANT),
	HIGH_DEV(DiscordGroup.RESP_TECH, DiscordGroup.ADMIN, DiscordGroup.FONDA),
	DEV(DiscordGroup.DEV, DiscordGroup.RESP_TECH, DiscordGroup.ADMIN, DiscordGroup.FONDA),
	;

	public static boolean hasPermission(DiscordPermission permission, Member member) {
		return permission == null || permission.hasPermission(member);
	}

	List<DiscordGroup> allow;

	private DiscordPermission(DiscordGroup... allow) {
		this.allow = Arrays.asList(allow);
	}

	public List<DiscordGroup> getAllow() {
		return allow;
	}

	public boolean hasPermission(Member member) {
		return allow.stream().anyMatch(a -> {
			Role role = a.getRole(member.getGuild());
			return role != null ? member.getRoles().contains(role) : false;
		});

	}
}
