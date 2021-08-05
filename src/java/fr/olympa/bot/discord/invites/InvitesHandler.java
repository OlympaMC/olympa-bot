package fr.olympa.bot.discord.invites;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.sql.CacheDiscordSQL;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.User;

public class InvitesHandler {

	//	protected static final Cache<String, DiscordSmallInvite> CACHE = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).removalListener(notification -> {
	//		DiscordInvite value = (DiscordInvite) notification.getValue();
	//		if (!notification.getCause().equals(RemovalCause.REPLACED))
	//			value.getDiscordGuild().cacheIncomplete();
	//		try {
	//			value.update();
	//		} catch (SQLException e) {
	//			e.printStackTrace();
	//		}
	//	}).build();

	static {
		//		CacheStats.addCache("DISCORD_INVITES", CACHE);
	}

	public static Comparator<DiscordInvite> getComparator() {
		return (o1, o2) -> {
			int i = o2.getUsesUnique() - o1.getUsesUnique();
			if (i == 0)
				i = o2.getUses() - o1.getUses();
			if (i == 0)
				i = o1.getUsesLeaver() - o2.getUsesLeaver();
			return i;
		};
	}

	public static void detectNewInvite(OlympaGuild opGuild, Consumer<List<User>> inviter, DiscordMember memberInvited, BiConsumer<MemberInvites, DiscordMember> memberWhoInviteScore) throws SQLException {
		Collection<DiscordSmallInvite> invites = DiscordInvite.getAllSmalls(opGuild);
		opGuild.getGuild().retrieveInvites().queue(invs -> {
			//			Map<Invite, DiscordInvite> discordInvitesMap = new HashMap<>();
			//			for (DiscordSmallInvite dsi : invites) {
			//				for (Invite iv : invs) {
			//					if (dsi.getUses() == iv.getUses() - 1 && dsi.getCode().equals(iv.getCode())) {
			//						discordInvitesMap.put(iv, dsi);
			//					}
			//				}
			//			}
			List<Invite> discordInvites = invites.stream()
					.map(dsi -> invs.stream().filter(iv -> dsi.getUses() == iv.getUses() - 1 && dsi.getCode().equals(iv.getCode())).findFirst().orElse(null))
					.filter(di -> di != null).collect(Collectors.toList());
			inviter.accept(discordInvites.stream().map(Invite::getInviter).collect(Collectors.toList()));
			if (discordInvites.size() != 1) {
				if (!discordInvites.isEmpty()) { // != discord.gg/olympa
					OlympaBots.getInstance().sendMessage("&e[DISCORD INVITE] &cImpossible de déterminer comment %s est arrivé là, il y a %d possibilités ...", memberInvited.getName(), discordInvites.size());
					init(opGuild);
				}
				if (memberWhoInviteScore != null)
					memberWhoInviteScore.accept(null, null);
				return;
			}
			Invite invite = discordInvites.get(0);
			DiscordInvite discordInvite = get(invite.getCode()).expand();
			discordInvite.addUser(memberInvited);
			discordInvite.update(() -> {
				try {
					if (memberWhoInviteScore != null) {
						DiscordMember authorMember = CacheDiscordSQL.getDiscordMember(invite.getInviter());
						memberWhoInviteScore.accept(new MemberInvites(opGuild, InvitesHandler.getByAuthor(opGuild, authorMember)), authorMember);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			});
			addInvite(discordInvite);
		});
	}

	public static void removeLeaverUser(DiscordMember dm, OlympaGuild opGuild) {
		DiscordInvite.getByUser(DiscordInvite.COLUMN_USERS_LEAVER_OLYMPA_DISCORD_ID, dm, opGuild).forEach(di -> {
			di.removeLeaver(dm);
			di.update();
		});
	}

	public static void addUsesLeaver(DiscordMember dm, OlympaGuild opGuild, Consumer<List<DiscordInvite>> invite) {
		List<DiscordInvite> invites = DiscordInvite.getByUser(DiscordInvite.COLUMN_USERS_OLYMPA_DISCORD_ID, dm, opGuild);
		invites.forEach(di -> {
			di.removeUser(dm);
			di.update();
		});
		if (invite != null)
			invite.accept(invites);
	}

	public static List<DiscordInvite> getByAuthor(OlympaGuild opGuild, DiscordMember discordMember) {
		List<DiscordInvite> list;
		//		if (opGuild.isCacheComplete()) {
		//			list = CACHE.asMap().values().stream().filter(iv -> iv.getDiscordGuild().getId() == opGuild.getId()).map(DiscordSmallInvite::expand).filter(iv -> iv.getAuthorId() == discordMember.getId())
		//					.collect(Collectors.toList());
		//			if (!list.isEmpty())
		//				return list;
		//			else
		//				opGuild.cacheIncomplete();
		//		}
		try {
			list = DiscordInvite.table.select(Map.of(DiscordInvite.COLUMN_OLYMPA_GUILD_ID, opGuild.getId(), DiscordInvite.COLUMN_OLYMPA_DISCORD_ID, discordMember.getId()));
			//			list.forEach(l -> addInvite(l));
			return list;
		} catch (IllegalAccessException | SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static DiscordInvite get(String code) {
		//		DiscordSmallInvite discordInvite = CACHE.getIfPresent(code);
		DiscordInvite discordInvite = null;
		if (discordInvite == null)
			try {
				discordInvite = DiscordInvite.getByCode(code);
				//				if (discordInvite != null)
				//					addInvite(discordInvite);
			} catch (IllegalAccessException | SQLException e) {
				e.printStackTrace();
			}
		return discordInvite;
	}

	public static DiscordSmallInvite getWithoutCaching(String code) {
		//		DiscordSmallInvite discordInvite = CACHE.getIfPresent(code);
		DiscordSmallInvite discordInvite = null;
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

	public static void addInvite(DiscordSmallInvite discordInvite) {
		//		CACHE.put(discordInvite.getCode(), discordInvite);
	}

	//	public static void init() {
	//		for (OlympaGuild opGuild : GuildHandler.guilds)
	//			init(opGuild);
	//	}

	public static void init(OlympaGuild opGuild) {
		opGuild.getGuild().retrieveInvites().queue(ivs -> {
			try {
				for (Invite iv : ivs) {
					DiscordInvite discordInvite = get(iv.getCode());
					if (discordInvite == null)
						discordInvite = new DiscordInvite(iv).createNew();
					//						addInvite(discordInvite);
					else
						discordInvite.update(iv);
				}
				opGuild.cacheComplete();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}

}
