package fr.olympa.bot.discord.commands;

import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import fr.olympa.bot.discord.webhook.WebHookHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class UsurpCommand extends DiscordCommand {

	public UsurpCommand() {
		super("usurper", DiscordPermission.HIGH_STAFF);
		minArg = 3;
		privateChannel = true;
		description = "Usurpe un membre en écrivant un message à sa place.";
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		deleteMessage(message);
		Guild guild = message.getGuild();
		Member targetMember = guild.getMemberById(args[0]);
		TextChannel targetChannel = guild.getTextChannelById(args[1]);
		String targetMessage = buildText(2, args);
		WebHookHandler.send(targetMessage, targetChannel, targetMember);
	}

}