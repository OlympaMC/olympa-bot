package fr.olympa.bot.teamspeak;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.api.ClientProperty;
import com.github.theholywaffle.teamspeak3.api.wrapper.ChannelInfo;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerGroup;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.bot.OlympaBots;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class TeamspeakUtils {

	static ServerGroup defaultServerGroup = null;
	static Map<Integer, ServerGroup> minecraftGroup = new HashMap<>();
	static List<Integer> minecraftStaffGroup = new ArrayList<>();

	@SuppressWarnings("deprecation")
	public static void check(ProxiedPlayer player) {
		TS3Api query = OlympaBots.getInstance().getTeamspeak().getQuery();

		if (query == null) {
			player.sendMessage(ColorUtils.color("&2Olympa &7» &cLa laison teamspeak/serveur s'est cassé."));
			return;
		}
		List<Client> sameip = query.getClients().stream().filter(client -> client.getIp().equals(player.getAddress().getAddress().getHostAddress())).collect(Collectors.toList());
		if (sameip.isEmpty()) {
			player.sendMessage(ColorUtils.color("&2Olympa &7» &cVous devez être connecté sur le Teamspeak."));
			return;
		}

		Client cl = sameip.stream().filter(client -> client.getNickname().equalsIgnoreCase(player.getName())).findFirst().orElse(null);

		if (cl == null) {
			player.sendMessage(ColorUtils.color("&2Olympa &7» &cVous devez utiliser le même pseudo sur Minecraft & sur Teamspeak !"));
			return;
		}
		setSynchronized(player, cl);
	}

	public static String getChannelURI(ChannelInfo channel) {
		return "[URL=channelid://" + channel.getId() + "]" + channel.getName() + "[/URL]";
	}

	public static String getClientURI(Client client) {
		return "[URL=" + client.getClientURI() + "]" + client.getNickname() + "[/URL]";
	}
	//
	//	public static boolean isInGroup(Client client, OlympaGroup... groups) {
	//		return Arrays.stream(client.getServerGroups()).filter(s -> Arrays.stream(groups).map(OlympaGroup::getTS3ID).filter(gId -> gId == s).findFirst().isPresent()).findFirst().isPresent();
	//	}

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

	@SuppressWarnings("deprecation")
	public static void setSynchronized(ProxiedPlayer player, Client client) {
		AccountProvider accountProvider = new AccountProvider(player.getUniqueId());
		OlympaPlayer olympaPlayer;
		try {
			olympaPlayer = accountProvider.get();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		TS3Api query = OlympaBots.getInstance().getTeamspeak().getQuery();
		query.editClient(client.getId(), Collections.singletonMap(ClientProperty.CLIENT_DESCRIPTION, "Pseudo Minecraft: " + player.getName()));
		removeAllServerGroup(client);
		olympaPlayer.setTeamspeakId(client.getDatabaseId());
		//			TeamspeakBot.query.addClientToServerGroup(OlympaPlayer.getGroup().getTS3ID(), client.getDatabaseId());
		//
		//			OlympaPlayer.setTS3ID(client.getDatabaseId());
		//					accountProvider.sendAccountToRedis(OlympaPlayer);
		query.sendPrivateMessage(client.getId(), "Ton indentité Teamspeak est désormais liée au compte Minecraft '" + player.getName() + "'.");
		player.sendMessage(ColorUtils.color("&2Olympa &7» &aTon compte Minecraft est désormais liée à ton identité Teamspeak '" + client.getNickname() + "'."));
	}

	//	public static void updateRank(OlympaPlayer OlympaPlayer, ClientInfo client) {
	//
	//		TeamspeakBot.query.editClient(client.getId(), Collections.singletonMap(ClientProperty.CLIENT_DESCRIPTION, "Pseudo Minecraft: " + OlympaPlayer.getName()));
	//		if (!Ints.asList(client.getServerGroups()).contains(OlympaPlayer.getGroup().getTS3ID()))
	//			removeAllServerGroup(client);
	//
	//		TeamspeakBot.query.addClientToServerGroup(OlympaPlayer.getGroup().getTS3ID(), client.getDatabaseId());
	//
	//		OlympaPlayer.setTS3ID(client.getDatabaseId());
	//	}
}
