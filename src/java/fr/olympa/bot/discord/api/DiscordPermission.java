package fr.olympa.bot.discord.api;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.groups.DiscordGroup;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public class DiscordPermission {

	//	HIGH_STAFF(DiscordGroup.FONDA, DiscordGroup.ADMIN, DiscordGroup.RESP_TECH, DiscordGroup.MODP, DiscordGroup.RESP_ANIMATION, DiscordGroup.RESP_STAFF, DiscordGroup.RESP_BUILDER),
	//	MODERATOR(DiscordGroup.MOD, DiscordGroup.MODP, DiscordGroup.ADMIN, DiscordGroup.FONDA),
	//	ASSISTANT(DiscordGroup.MOD, DiscordGroup.MODP, DiscordGroup.ADMIN, DiscordGroup.FONDA, DiscordGroup.ASSISTANT),
	//	HIGH_DEV(DiscordGroup.RESP_TECH, DiscordGroup.ADMIN, DiscordGroup.FONDA),
	//	DEV(DiscordGroup.DEV, DiscordGroup.RESP_TECH, DiscordGroup.ADMIN, DiscordGroup.FONDA),
	//	BUILDER(DiscordGroup.RESP_BUILDER, DiscordGroup.BUILDER, DiscordGroup.DEV, DiscordGroup.RESP_TECH, DiscordGroup.ADMIN, DiscordGroup.FONDA),
	//	STAFF(DiscordGroup.MOD, DiscordGroup.MODP, DiscordGroup.ADMIN, DiscordGroup.FONDA, DiscordGroup.ADMIN, DiscordGroup.DEV, DiscordGroup.ASSISTANT, DiscordGroup.BUILDER, DiscordGroup.GRAPHISTE),
	//	;
	public static final DiscordPermission AUTHOR = new DiscordPermission(450_125_243_592_343_563L);
	public static final DiscordPermission ADMIN = new DiscordPermission(DiscordGroup.FONDA, DiscordGroup.ADMIN, DiscordGroup.RESP_TECH);
	public static final DiscordPermission HIGH_DEV = new DiscordPermission(DiscordGroup.getAllUpper(OlympaGroup.RESP_TECH));
	public static final DiscordPermission HIGH_STAFF = new DiscordPermission(DiscordGroup.FONDA, DiscordGroup.ADMIN, DiscordGroup.RESP_TECH, DiscordGroup.MODP, DiscordGroup.RESP_ANIMATION, DiscordGroup.RESP_STAFF,
			DiscordGroup.RESP_BUILDER, DiscordGroup.RESP);
	public static final DiscordPermission MODERATOR = new DiscordPermission(DiscordGroup.getAllUpper(OlympaGroup.MOD));
	public static final DiscordPermission ASSISTANT = new DiscordPermission(DiscordGroup.getAllUpper(OlympaGroup.ASSISTANT));
	public static final DiscordPermission DEV = new DiscordPermission(DiscordGroup.DEV, DiscordGroup.DEVP, DiscordGroup.RESP_TECH, DiscordGroup.ADMIN, DiscordGroup.FONDA, DiscordGroup.RESP_TECH, DiscordGroup.MODP,
			DiscordGroup.RESP_ANIMATION, DiscordGroup.RESP_STAFF,
			DiscordGroup.RESP_BUILDER, DiscordGroup.RESP);
	public static final DiscordPermission BUILDER = new DiscordPermission(DiscordGroup.getAllUpper(OlympaGroup.BUILDER));
	public static final DiscordPermission STAFF = new DiscordPermission(DiscordGroup.getStaffs());

	public static DiscordPermission getByName(String name) {
		return Arrays.stream(DiscordPermission.class.getFields()).map(f -> {
			if (f != null && f.getName().equals(name))
				try {
					return (DiscordPermission) f.get(null);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			return null;
		}).filter(p -> p != null).findFirst().orElse(null);
	}

	public static boolean hasPermission(DiscordPermission permission, Member member) {
		try {
			return permission == null || permission.hasPermission(member) || CacheDiscordSQL.getDiscordMember(member.getUser()).hasPermission(permission, member.getGuild());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	List<DiscordGroup> allow;
	List<Long> allowIds;
	String name;

	private DiscordPermission(DiscordGroup... allow) {
		this.allow = Arrays.asList(allow);
	}

	private DiscordPermission(Long... allowIds) {
		this.allowIds = Arrays.asList(allowIds);
	}

	private DiscordPermission(List<DiscordGroup> allow) {
		this.allow = allow;
	}

	public List<DiscordGroup> getAllow() {
		return allow;
	}

	public String getName() {
		if (name != null)
			return name;
		return name = Arrays.stream(DiscordPermission.class.getFields()).map(f -> {
			if (f != null)
				try {
					DiscordPermission this2 = (DiscordPermission) f.get(null);
					if (equals(this2))
						return f.getName();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			return "unknown";
		}).filter(s -> !s.equals("unknown")).findFirst().orElse("unknown");
	}

	public void fetchAllowIdsUser(Consumer<User> users) {
		if (allowIds == null)
			return;
		JDA jda = OlympaBots.getInstance().getDiscord().getJda();
		for (Long allowId : allowIds)
			jda.retrieveUserById(allowId).queue(u -> users.accept(u));
	}

	public boolean hasPermissionIdUser(User user) {
		return allowIds.contains(user.getIdLong());
	}

	public boolean hasPermission(Member member) {
		return allow.stream().anyMatch(a -> {
			Role role = a.getRole(member.getGuild());
			if (role != null)
				return member.getRoles().contains(role);
			OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(member.getGuild());
			if (olympaGuild != null && olympaGuild.getType() == DiscordGuildType.OTHER)
				return member.hasPermission(Permission.ADMINISTRATOR);
			return false;
		});
	}
}
