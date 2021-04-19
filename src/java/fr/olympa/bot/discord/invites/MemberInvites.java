package fr.olympa.bot.discord.invites;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.member.DiscordMember;

public class MemberInvites {

	OlympaGuild guild;
	List<DiscordInvite> invites;
	Set<DiscordMember> users, usersPast, leavers;
	int realUses, totalUses, leaves, realLeaves, reinvited = 0;

	/**
	 * @param guild
	 * @param invites
	 */
	public MemberInvites(OlympaGuild guild, List<DiscordInvite> invites) {
		users = new HashSet<>();
		usersPast = new HashSet<>();
		leavers = new HashSet<>();
		this.guild = guild;
		this.invites = invites;
		invites.forEach(di -> {
			totalUses += di.getUses();
			realUses += di.getRealUse();
			leaves += di.getUsesLeaver();
			users.addAll(di.getUsers());
			leavers.addAll(di.getLeaveUsers());
			usersPast.addAll(di.getPastUsers().stream().filter(dm -> !di.getUsers().contains(dm) && !di.getLeaveUsers().contains(dm)).collect(Collectors.toSet()));
		});
		usersPast.removeIf(dm -> users.contains(dm) || leavers.contains(dm));
		leavers.removeIf(dm -> users.contains(dm));
		realLeaves = usersPast.size() + leavers.size();
		reinvited = usersPast.size();
	}

	public OlympaGuild getGuild() {
		return guild;
	}

	public List<DiscordInvite> getInvites() {
		return invites;
	}

	public Set<DiscordMember> getUsers() {
		return users;
	}

	public Set<DiscordMember> getUsersPast() {
		return usersPast;
	}

	public Set<DiscordMember> getLeavers() {
		return leavers;
	}

	public int getRealUses() {
		return realUses;
	}

	public int getTotalUses() {
		return totalUses;
	}

	public int getLeaves() {
		return leaves;
	}

	public int getRealLeaves() {
		return realLeaves;
	}

	public int getReinvited() {
		return reinvited;
	}

}
