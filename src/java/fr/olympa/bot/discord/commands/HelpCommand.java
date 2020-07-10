package fr.olympa.bot.discord.commands;

import java.util.ArrayList;
import java.util.List;

import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class HelpCommand extends DiscordCommand {

	public HelpCommand() {
		super("help");
		description = "Affiche la liste des commandes.";
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		MessageChannel channel = message.getChannel();
		Member member = message.getMember();
		EmbedBuilder em = new EmbedBuilder();
		em.setTitle("Les commandes:");
		DiscordCommand.getCommands().values().stream().filter(x -> DiscordPermission.hasPermission(x.getPermission(), member)).forEach(x -> {
			List<String> names = new ArrayList<>(3);
			names.add(x.getName());
			if (x.getAliases() != null) names.addAll(x.getAliases());
			em.addField(DiscordCommand.prefix + String.join(", ", names), x.getDescription() == null ? "Pas de description." : x.getDescription(), false);
		});
		channel.sendMessage(em.build()).queue();
	}

}