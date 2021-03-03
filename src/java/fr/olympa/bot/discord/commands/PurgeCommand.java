package fr.olympa.bot.discord.commands;

import java.sql.SQLException;

import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import fr.olympa.bot.discord.api.reaction.ReactionSQL;
import fr.olympa.bot.discord.message.SQLMessage;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class PurgeCommand extends DiscordCommand {

	public PurgeCommand() {
		super("purge", DiscordPermission.HIGH_DEV);
		description = "Libère la mémoire physique de vieilles ou grosses donnés.";
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		MessageChannel channel = message.getChannel();
		Member member = message.getMember();

		if (args.length == 0)
			try {
				int rows = SQLMessage.purge();
				channel.sendMessage(member.getAsMention() + "➤ " + rows + " données ont été supprimés.").queue();
			} catch (SQLException e) {
				channel.sendMessage(member.getAsMention() + "➤  Une erreur est survenue `" + e.getMessage() + "`").queue();
				e.printStackTrace();
			}
		else if (args[0].equalsIgnoreCase("reaction") || args[0].equalsIgnoreCase("reactions"))
			try {
				int rows = ReactionSQL.purge();
				channel.sendMessage(member.getAsMention() + "➤ " + rows + " données de l'API réaction ont été supprimés.").queue();
			} catch (SQLException e) {
				channel.sendMessage(member.getAsMention() + "➤  Une erreur est survenue `" + e.getMessage() + "`").queue();
				e.printStackTrace();
			}
	}

}
