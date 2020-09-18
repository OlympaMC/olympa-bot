package fr.olympa.bot.teamspeak;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode;
import com.github.theholywaffle.teamspeak3.api.event.ClientMovedEvent;
import com.github.theholywaffle.teamspeak3.api.event.TS3EventAdapter;
import com.github.theholywaffle.teamspeak3.api.event.TextMessageEvent;
import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import com.github.theholywaffle.teamspeak3.api.wrapper.ChannelInfo;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.ClientInfo;

import fr.olympa.bot.OlympaBots;

public class TsListener extends TS3EventAdapter {

	@Override
	public void onTextMessage(TextMessageEvent event) {
		TS3Api query = OlympaBots.getInstance().getTeamspeak().getQuery();
		int clientID = event.getInvokerId();
		ClientInfo client = query.getClientInfo(clientID);
		if (event.getTargetMode() == TextMessageTargetMode.CLIENT && !client.isServerQueryClient()) {
			String message = event.getMessage().toLowerCase();

			if (message.startsWith("salut"))
				query.sendPrivateMessage(clientID, "Bonjour " + event.getInvokerName() + "!");
		}
	}

	//	@Override
	//	public void onClientJoin(ClientJoinEvent event) {
	//		TS3Api query = TeamspeakBot.query;
	//		ClientInfo client = query.getClientInfo(event.getClientId());
	//
	//		if (!client.isServerQueryClient()) {
	//			int[] clientgroupsid = client.getServerGroups();
	//
	//			List<Integer> ClientMinecraftGroup = Arrays.stream(clientgroupsid).filter(clientgroupid -> TeamspeakUtils.minecraftGroup.containsKey(clientgroupid)).boxed().collect(
	//					Collectors.toList());
	//
	//			if (ClientMinecraftGroup.size() > 1) {
	//				TeamspeakUtils.removeAllServerGroup(client);
	//				query.sendPrivateMessage(event.getClientId(), "Il est impossible d'avoir plusieurs grades sur le teamspeak.");
	//			}
	//
	//				OlympaPlayer Olympaplayer = MySQL.getPlayerByTS3ID(client.getDatabaseId());
	//			if (Olympaplayer == null) {
	//				query.editClient(client.getId(), Collections.singletonMap(ClientProperty.CLIENT_DESCRIPTION, ""));
	//				query.pokeClient(event.getClientId(), "Pour avoir accès à toutes les fonctionnalités, faites '/ts link' sur le serveur Minecraft.");
	//				return;
	//			}
	//
	//			if (!client.getNickname().equals(Olympaplayer.getName()))
	//				query.kickClientFromServer("Il est interdit de changer votre pseudo. Merci de garder '" + Olympaplayer.getName() + "'.", client);
	//
	//			TeamspeakUtils.updateRank(Olympaplayer, client);
	//		}
	//	}

	@Override
	public void onClientMoved(ClientMovedEvent event) {
		OlympaTeamspeak ts = OlympaBots.getInstance().getTeamspeak();
		TS3Api query = OlympaBots.getInstance().getTeamspeak().getQuery();
		int channelID = event.getTargetChannelId();

		ChannelInfo channelInfo = query.getChannelInfo(channelID);
		Channel channel = query.getChannelByNameExact(channelInfo.getName(), false);
		if (channel.getTotalClients() != 1)
			return;

		//		if (ts.helpchannelsId.contains(channelID)) {
		//			int clientID = event.getClientId();
		//			ClientInfo clientInfo = query.getClientInfo(clientID);
		//			int i = 0;
		//			for (Client staff : query.getClients())
		//				if (TeamspeakUtils.isInGroup(staff, OlympaGroup.MODERATEUR, OlympaGroup.Assistant, OlympaGroup.RESPMODO) && !TeamspeakUtils.isInGroup(staff, 40)) {
		//					query.sendPrivateMessage(staff.getId(),
		//							TeamspeakUtils.getClientURI(clientInfo) + "[color=green] a besoin de l'aide d'un Modérateur/Assistant dans le channel [/color]" + TeamspeakUtils
		//									.getChannelURI(channelInfo));
		//					i++;
		//				}
		//			if (i == 0) {
		//				query.kickClientFromChannel("[color=green]Aucun Assistant ou Modérateur n'est actuellement disponible, merci de réésayer plus tard.[/color]", clientID);
		//				return;
		//			}
		//
		//			query.pokeClient(clientID, "[color=green]Merci de patienter l'arrivée d'un Guide ou Modérateur.[/color]");
		//		}
		if (ts.helpChannelsAdmin.getId() == channelID) {
			int clientID = event.getClientId();
			ClientInfo clientInfo = query.getClientInfo(clientID);
			int i = 0;
			for (Client staff : query.getClients())
				if (TeamspeakGroups.ADMIN.hasPermission(staff)) {
					query.sendPrivateMessage(staff.getDatabaseId(), TeamspeakUtils.getClientURI(clientInfo) + "[color=red] a besoin de l'aide d'un Admin dans le channel [/color]" + TeamspeakUtils.getChannelURI(channelInfo));
					i++;
				}
			if (i == 0)
				query.pokeClient(clientID, "[color=red]Aucun Administrateur n'est actuellement disponible, merci de patienter.[/color]");
			else
				query.pokeClient(clientID, "[color=red]Tu es en attente d'un administrateur. Merci de patienter ...[/color]");
		}
	}

}
