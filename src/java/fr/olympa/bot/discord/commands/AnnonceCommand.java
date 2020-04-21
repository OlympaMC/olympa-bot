package fr.olympa.bot.discord.commands;

import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class AnnonceCommand extends DiscordCommand {

	public AnnonceCommand() {
		super("annonce", Permission.MESSAGE_MANAGE);
		minArg = 1;
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message) {
		MessageChannel channel = message.getChannel();
		message.delete().queue();

		CharSequence description = String.join(" ", args);
		EmbedBuilder embed = new EmbedBuilder().setDescription(description).setTitle("📢 Annonce");
		embed.setColor(OlympaBots.getInstance().getDiscord().getColor());
		channel.sendMessage("@everyone").queue(msg -> channel.sendMessage(embed.build()).queue(m -> m.addReaction("☑️").queue()));
	}

}
