package fr.olympa.bot.discord.commands;

import java.util.HashMap;
import java.util.Map;

import fr.olympa.bot.discord.api.commands.DiscordCommand;
import fr.olympa.bot.discord.reaction.AwaitReaction;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class ServersCommand extends DiscordCommand {

	public ServersCommand() {
		super("server", "servers");
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		Member member = message.getMember();
		TextChannel channel = message.getTextChannel();

		Map<String, String> map = new HashMap<>();
		map.put("🔄", "refresh");
		channel.sendMessage(RefreshServersMessage.getEmbed()).queue(msg -> AwaitReaction.addReaction(msg, new RefreshServersMessage(map, msg, member.getIdLong())));
	}

}