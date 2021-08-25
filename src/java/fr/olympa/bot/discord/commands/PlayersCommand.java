package fr.olympa.bot.discord.commands;

import java.awt.Color;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.common.server.ServerInfoAdvancedBungee;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import fr.olympa.core.bungee.OlympaBungee;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * TODO
 * Add global count without vanish players
 */
public class PlayersCommand extends DiscordCommand {

	public PlayersCommand() {
		super("players");
		super.description = "Affiche une liste des joueurs actuellement connectés sur le serveur.";
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		TextChannel channel = message.getTextChannel();
		EmbedBuilder embedBuilder = new EmbedBuilder();
		Collection<ProxiedPlayer> globalPlayers = ProxyServer.getInstance().getPlayers();
		Map<ServerInfoAdvancedBungee, List<ProxiedPlayer>> servers = new HashMap<>();
		globalPlayers.forEach(player -> {
			servers.computeIfAbsent(player.getServer() == null ? null : OlympaBungee.getInstance().getMonitoring().getMonitor(player.getServer().getInfo()), server -> new ArrayList<>()).add(player);
		});
		servers.entrySet().stream().sorted((o1, o2) -> Integer.compare(o2.getValue().size(), o1.getValue().size())).forEach(entry -> {
			@Nullable
			ServerInfoAdvancedBungee server = entry.getKey();
			List<ProxiedPlayer> players = new ArrayList<>();
			players.addAll(entry.getValue());
				players.removeIf(p -> {
					try {
						OlympaPlayer op = new AccountProviderAPI(p.getUniqueId()).get();
						return op != null && op.isVanish();
					} catch (SQLException e) {
						e.printStackTrace();
					}
					return false;
				});
			embedBuilder.addField((server != null ? server.getName() : "En connexion") + " (" + players.size() + ")", players.stream().map(p -> "`" + p.getName() + "`").collect(Collectors.joining(", ")), true);
		});
		embedBuilder.setTitle("Liste des joueurs connectés :");
		embedBuilder.setColor(embedBuilder.getFields().isEmpty() ? Color.RED : Color.GREEN);
		channel.sendMessageEmbeds(embedBuilder.build()).queue();
	}

}