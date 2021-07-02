package fr.olympa.bot.discord.commands;

import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class AnnonceCommand extends DiscordCommand {

	public AnnonceCommand() {
		super("annonce", DiscordPermission.HIGH_STAFF);
		minArg = 1;
		description = "Écrit une annonce par le bot, et tag everyone.";
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		MessageChannel channel = message.getChannel();
		deleteMessage(message);

		CharSequence description = String.join(" ", args);
		EmbedBuilder embed = new EmbedBuilder().setDescription(description).setTitle("📢 Annonce");
		embed.setColor(OlympaBots.getInstance().getDiscord().getColor());
		channel.sendMessage("@everyone").queue(msg -> channel.sendMessageEmbeds(embed.build()).queue(m -> m.addReaction("☑️").queue()));
	}
}
