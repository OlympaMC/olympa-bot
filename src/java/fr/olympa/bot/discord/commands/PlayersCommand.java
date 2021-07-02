package fr.olympa.bot.discord.commands;

import java.awt.Color;
import java.util.Collection;
import java.util.stream.Collectors;

import fr.olympa.bot.discord.api.commands.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PlayersCommand extends DiscordCommand {

	public PlayersCommand() {
		super("players");
		super.description = "Affiche une liste des joueurs actuellement connectés sur le serveur.";
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		TextChannel channel = message.getTextChannel();

		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.setTitle("Liste des joueurs connectés :");
		for (ServerInfo server : ProxyServer.getInstance().getServersCopy().values()) {
			Collection<ProxiedPlayer> players = server.getPlayers();
			if (!players.isEmpty())
				embedBuilder.addField(server.getName() + " (" + players.size() + ")", players.stream().map(ProxiedPlayer::getName).collect(Collectors.joining(", ")), true);
		}

		embedBuilder.setColor(embedBuilder.getFields().isEmpty() ? Color.RED : Color.GREEN);
		channel.sendMessageEmbeds(embedBuilder.build()).queue();
	}

}