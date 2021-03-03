package fr.olympa.bot.discord.invites;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.member.DiscordMember;

public class MemberInvites {

	OlympaGuild guild;
	List<DiscordInvite> invites;
	List<DiscordMember> users, usersPast, leavers;
	int realUses, totalUses, leaves, realLeaves, reinvited = 0;

	/**
	 * @param guild
	 * @param invites
	 */
	public MemberInvites(OlympaGuild guild, List<DiscordInvite> invites) {
		users = new ArrayList<>();
		usersPast = new ArrayList<>();
		leavers = new ArrayList<>();
		this.guild = guild;
		this.invites = invites;
		invites.forEach(di -> {
			totalUses += di.getUses();
			realUses += di.getRealUse();
			leaves += di.getUsesLeaver();
			users.addAll(di.getUsers());
			leavers.addAll(di.getLeaveUsers());
			usersPast.addAll(di.getPastUsers().stream().filter(dm -> !usersPast.contains(dm) && !di.getUsers().contains(dm) && !di.getLeaveUsers().contains(dm)).collect(Collectors.toList()));
		});
		realLeaves = usersPast.size() + leavers.size();
		reinvited = usersPast.size();
	}

	public OlympaGuild getGuild() {
		return guild;
	}

	public List<DiscordInvite> getInvites() {
		return invites;
	}

	public List<DiscordMember> getUsers() {
		return users;
	}

	public List<DiscordMember> getUsersPast() {
		return usersPast;
	}

	public List<DiscordMember> getLeavers() {
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