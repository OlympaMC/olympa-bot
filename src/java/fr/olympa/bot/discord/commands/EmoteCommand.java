package fr.olympa.bot.discord.commands;

import java.awt.Color;
import java.util.concurrent.TimeUnit;

import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.DiscordUtils;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class EmoteCommand extends DiscordCommand {

	public EmoteCommand() {
		super("emote", DiscordPermission.DEV);
		minArg = 1;
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message) {
		MessageChannel channel = message.getChannel();
		message.delete().queue();

		String test = args[0];
		Emote emote = message.getJDA().getEmotesByName(test, true).get(0);
		if (emote == null) {
			DiscordUtils.sendTempMessage(channel, "Cet emote n'existe pas");
			return;
		}
		EmbedBuilder embed = new EmbedBuilder().setDescription("Id: " + emote.getId() + " Name: " + emote.getName()).setTitle("Emote");
		embed.setColor(Color.YELLOW);
		channel.sendMessage(embed.build()).queue(m -> m.addReaction(emote).queue(m2 -> m.delete().queueAfter(OlympaBots.getInstance().getDiscord().timeToDelete, TimeUnit.SECONDS)));
	}

}
