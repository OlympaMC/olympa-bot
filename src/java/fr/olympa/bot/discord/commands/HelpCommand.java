package fr.olympa.bot.discord.commands;

import java.util.stream.Collectors;

import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class HelpCommand extends DiscordCommand {

	public HelpCommand() {
		super("help");
		description = "Listes des commandes";
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		MessageChannel channel = message.getChannel();
		Member member = message.getMember();
		EmbedBuilder em = new EmbedBuilder();
		em.setTitle("Les commandes:");
		em.setDescription(DiscordCommand.getCommands().entrySet().stream().filter(entry -> DiscordPermission.hasPermission(entry.getValue().getPermission(), member)).map(entry -> {
			String n = entry.getKey();
			DiscordCommand dc = entry.getValue();
			return DiscordCommand.prefix + n + " " + dc.getDescription();
		}).collect(Collectors.joining("\n")));
		channel.sendMessage(em.build()).queue();
	}

}