package fr.olympa.bot.discord.spam;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.entities.Member;

public class SpamHandler {
	
	public static List<SpamDiscord> data = new ArrayList<>();
	
	public static List<Member> addData(Member member, List<Member> mentionedMembers) {
		List<Member> out = new ArrayList<>();
		SpamDiscord data = get(member.getIdLong());
		if (data == null)
			data = new SpamDiscord(member.getIdLong());
		for (Member mem : mentionedMembers)
			if (data.addMention(mem))
				out.add(mem);
		addData(data);
		return out;
	}
	
	public static void addData(SpamDiscord data) {
		SpamDiscord oldData = get(data.getUserId());
		if (oldData != null)
			SpamHandler.data.remove(oldData);
		SpamHandler.data.add(data);
	}
	
	public static SpamDiscord get(long id) {
		return data.stream().filter(sd -> sd.getUserId() == id).findFirst().orElse(null);
	}
	
	public static SpamDiscord get(Member member) {
		return get(member.getIdLong());
	}
	
	public static List<SpamDiscord> getData() {
		return data;
	}
	
	public static void remove(long id) {
		SpamDiscord oldData = get(id);
		if (oldData != null)
			data.remove(oldData);
	}
	
	public static void remove(Member member) {
		remove(member.getIdLong());
	}
	
	public static void removeAllTagMember(Member member) {
		for (SpamDiscord sd : data)
			sd.remove(member);
	}
}
