package fr.olympa.bot.teamspeak;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.theholywaffle.teamspeak3.api.wrapper.ChannelInfo;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerGroup;

import fr.olympa.bot.OlympaBots;

public class TeamspeakUtils {

	static ServerGroup defaultServerGroup = null;
	static Map<Integer, ServerGroup> minecraftGroup = new HashMap<>();
	static List<Integer> minecraftStaffGroup = new ArrayList<>();

	public static String getChannelURI(ChannelInfo channel) {
		return "[URL=channelid://" + channel.getId() + "]" + channel.getName() + "[/URL]";
	}

	public static String getClientURI(Client client) {
		return "[URL=" + client.getClientURI() + "]" + client.getNickname() + "[/URL]";
	}

	public static boolean isInGroup(Client client, int groupID) {
		return Arrays.stream(client.getServerGroups()).filter(s -> groupID == s).findFirst().isPresent();
	}

	public static boolean isStaff(Client client) {
		return Arrays.stream(client.getServerGroups()).filter(s -> minecraftStaffGroup.contains(s)).findFirst().isPresent();
	}

	public static void removeAllServerGroup(Client client) {
		for (int clientservergroupid : client.getServerGroups())
			OlympaBots.getInstance().getTeamspeak().getQuery().removeClientFromServerGroup(clientservergroupid, client.getDatabaseId());
	}

}
