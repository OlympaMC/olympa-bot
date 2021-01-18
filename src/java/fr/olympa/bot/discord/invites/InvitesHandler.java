package fr.olympa.bot.discord.invites;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.utils.CacheStats;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.member.DiscordMember;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.User;

public class InvitesHandler {

	public static Cache<String, DiscordInvite> cache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).removalListener(notification -> {
		try {
			((DiscordInvite) notification.getValue()).update();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}).build();

	{
		CacheStats.addCache("DISCORD_INVITES", cache);
	}

	public static Set<DiscordSmallInvite> getAllSmallsInvites(OlympaGuild opGuild) throws SQLException {
		ResultSet result = DiscordInvite.COLUMN_OLYMPA_GUILD_ID.selectBasic(opGuild.getId(), DiscordInvite.COLUMN_USES.getCleanName(), DiscordInvite.COLUMN_CODE.getCleanName());
		Set<DiscordSmallInvite> invites = new HashSet<>();
		while (result.next())
			invites.add(new DiscordSmallInvite(opGuild, result));
		result.close();
		return invites;
	}

	public static void detectNewInvite(OlympaGuild opGuild, Consumer<User> inviter, DiscordMember invited) throws SQLException, IllegalAccessException {
		Collection<DiscordSmallInvite> invites = getAllSmallsInvites(opGuild);
		opGuild.getGuild().retrieveInvites().queue(invs -> {
			List<Invite> discordInvites = invites.stream().map(smi -> invs.stream().filter(iv -> smi.getUses() == iv.getUses() - 1 && smi.getCode().equals(iv.getCode())).findFirst().orElse(null)).filter(di -> di != null)
					.collect(Collectors.toList());
			if (discordInvites.size() != 1) {
				LinkSpigotBungee.Provider.link.sendMessage("&e[DISCORD INVITE] &cImpossible de déterminer comment %s est arrivé là, il y a %d possibilités ...", invited.getName(), discordInvites.size());
				init(opGuild);
				inviter.accept(null);
				return;
			}
			Invite invite = discordInvites.get(0);
			inviter.accept(invite.getInviter());
			DiscordInvite discordInvite = get(invite.getCode());
			discordInvite.addUser(invited);
			try {
				discordInvite.update();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public static void removeLeaverUser(DiscordMember dm, OlympaGuild opGuild) {
		DiscordInvite.getByUser(DiscordInvite.COLUMN_USERS_LEAVER_OLYMPA_DISCORD_ID, dm, opGuild).forEach(di -> {
			di.removeLeaver(dm);
			try {
				di.update();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public static void addUsesLeaver(DiscordMember dm, OlympaGuild opGuild) {
		DiscordInvite.getByUser(DiscordInvite.COLUMN_USERS_OLYMPA_DISCORD_ID, dm, opGuild).forEach(di -> {
			di.removeUser(dm);
			try {
				di.update();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

	public static DiscordInvite get(String code) {
		DiscordInvite discordInvite = cache.getIfPresent(code);
		if (discordInvite == null)
			try {
				discordInvite = DiscordInvite.getByCode(code);
				if (discordInvite != null)
					addInvite(discordInvite);
			} catch (IllegalAccessException | SQLException e) {
				e.printStackTrace();
			}
		return discordInvite;
	}

	public static DiscordInvite getWithoutCaching(String code) {
		DiscordInvite discordInvite = cache.getIfPresent(code);
		if (discordInvite == null)
			try {
				discordInvite = DiscordInvite.getByCode(code);
			} catch (IllegalAccessException | SQLException e) {
				e.printStackTrace();
			}
		return discordInvite;
	}

	public static boolean exist(String code) {
		return get(code) != null;
	}

	public static void addInvite(DiscordInvite discordInvite) {
		cache.put(discordInvite.getCode(), discordInvite);
	}

	public static void init() {
		for (OlympaGuild opGuild : GuildHandler.guilds)
			init(opGuild);
	}

	public static void init(OlympaGuild opGuild) {
		opGuild.getGuild().retrieveInvites().queue(ivs -> {
			try {
				for (Invite iv : ivs) {
					DiscordInvite discordInvite = getWithoutCaching(iv.getCode());
					if (discordInvite == null)
						addInvite(new DiscordInvite(iv).createNew());
					else
						discordInvite.update(iv);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}
}
