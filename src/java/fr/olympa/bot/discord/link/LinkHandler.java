package fr.olympa.bot.discord.link;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.bot.discord.groups.DiscordGroup;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class LinkHandler {
	
	private static Cache<String, ProxiedPlayer> waiting = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.MINUTES).build();
	
	public static String addWaiting(ProxiedPlayer p) {
		String code = LinkHandler.generateRandomWord(6);
		waiting.put(code, p);
		return code;
	}
	
	private static String generateRandomWord(int wordLength) {
		Random r = new Random();
		StringBuilder sb = new StringBuilder(wordLength);
		for (int i = 0; i < wordLength; i++) {
			char tmp = (char) ('a' + r.nextInt('z' - 'a'));
			sb.append(tmp);
		}
		String code = sb.toString();
		return waiting.asMap().get(code) == null ? code : generateRandomWord(wordLength);
	}
	
	public static String getCode(ProxiedPlayer proxiedPlayer) {
		return waiting.asMap().entrySet().stream().filter(entry -> entry.getValue().getUniqueId().equals(proxiedPlayer.getUniqueId())).map(e -> e.getKey()).findFirst().orElse(null);
	}
	
	public static ProxiedPlayer getPlayer(String code) {
		return waiting.asMap().get(code);
	}
	
	public static void removeWaiting(ProxiedPlayer player) {
		waiting.invalidate(player);
	}
	
	public static void updateGroups(Member member, OlympaPlayer olympaPlayer) {
		Set<OlympaGroup> groups = olympaPlayer.getGroups().keySet();
		Set<Role> roles = DiscordGroup.get(groups).stream().map(g -> g.getRole(member.getGuild())).collect(Collectors.toSet());
		Set<Role> roleToRemoved = new HashSet<>(member.getRoles());
		SetView<Role> communRole = /*roleToRemoved.stream().filter(r1 -> roles.stream().filter(r2 -> r1.getId() == r2.getId()).findFirst().isPresent()).collect(Collectors.toList());*/ Sets.intersection(roles, roleToRemoved);
		roleToRemoved.removeAll(communRole);
		roles.removeAll(communRole);
		
		if (!roles.isEmpty() && !roles.isEmpty())
			member.getGuild().modifyMemberRoles(member, roles, roleToRemoved).queue();
	}
}
