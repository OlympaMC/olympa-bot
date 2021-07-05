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
	Set<DiscordMember> users, usersReivited, leavers;
	int realUses, totalUses, leaves, allLeaves, reinvited = 0;

	/**
	 * @param guild
	 * @param invites
	 */
	public MemberInvites(OlympaGuild guild, List<DiscordInvite> invites) {
		users = new HashSet<>();
		usersReivited = new HashSet<>();
		leavers = new HashSet<>();
		this.guild = guild;
		this.invites = invites;
		invites.forEach(di -> {
			totalUses += di.getUses();
			realUses += di.getUsesUnique();
			leaves += di.getRealUsesLeaver();
			users.addAll(di.getUsers());
			leavers.addAll(di.getLeaveUsers());
			usersReivited.addAll(di.getPastUsers().stream().filter(dm -> !di.getUsers().contains(dm) && !di.getLeaveUsers().contains(dm)).collect(Collectors.toSet()));
		});
		usersReivited.removeIf(dm -> users.contains(dm) || leavers.contains(dm));
		leavers.removeIf(dm -> users.contains(dm));
		allLeaves = usersReivited.size() + leavers.size();
		reinvited = usersReivited.size();
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
		return usersReivited;
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

	// Not unique, can't check userId for multiple invites
	public int getLeaves() {
		return leaves;
	}

	public int getRealLeaves() {
		return leavers.size();
	}

	public int getAllLeaves() {
		return allLeaves;
	}

	public int getReinvited() {
		return reinvited;
	}

}
