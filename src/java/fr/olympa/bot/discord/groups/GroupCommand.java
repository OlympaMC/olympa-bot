package fr.olympa.bot.discord.groups;

import java.sql.SQLException;

import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.provider.AccountProvider;
import fr.olympa.api.utils.Utils;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class GroupCommand extends DiscordCommand {

	public GroupCommand() {
		super("groupe", DiscordPermission.HIGH_DEV);
		description = "Donne des infos sur un joueur Olympa.";
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		MessageChannel channel = message.getChannel();

		if (args.length < 1)
			return;

		OlympaPlayer olympaTarget = null;
		try {
			olympaTarget = AccountProvider.getter().getFromDatabase(args[0]);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (olympaTarget == null) {
			channel.sendMessage("Le joueur **" + args[0] + "** est introuvable.");
			return;
		}
		EmbedBuilder em = new EmbedBuilder();
		em.setTitle("Info");
		String uuid = "8667ba71-b85a-4004-af54-457a9734eed7";
		if (olympaTarget.isPremium())
			uuid = olympaTarget.getPremiumUniqueId().toString();
		em.setAuthor(olympaTarget.getName(), null, "https://crafatar.com/avatars/" + uuid);
		em.addField("Groupes", olympaTarget.getGroupsToHumainString(), true);
		em.addField("Première connexion", Utils.timestampToDuration(olympaTarget.getFirstConnection()), true);
		em.addField("Dernière connexion", Utils.timestampToDuration(olympaTarget.getLastConnection()), true);
		channel.sendMessage(em.build()).queue();
	}
}