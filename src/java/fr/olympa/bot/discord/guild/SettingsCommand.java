package fr.olympa.bot.discord.guild;

import java.sql.SQLException;
import java.util.stream.Collectors;

import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import fr.olympa.bot.discord.sql.DiscordSQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class SettingsCommand extends DiscordCommand {

	public SettingsCommand() {
		super("settings", DiscordPermission.HIGH_DEV);
		description = "Change les paramètres du bot.";
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message) {
		message.delete().queue();
		MessageChannel channel = message.getChannel();
		Guild guild = message.getGuild();
		OlympaGuild olympaGuild = GuildsHandler.getGuild(guild);
		EmbedBuilder embed = new EmbedBuilder().setTitle("🔧 Paramètres");
		embed.setColor(OlympaBots.getInstance().getDiscord().getColor());
		
		if (args.length == 0) {
			embed.setDescription(description);
			embed.addField("Logs msg", olympaGuild.isLogMsg() ? "✅" : "❌", true);
			embed.addField("Logs pièces jointes", olympaGuild.isLogAttachment() ? "✅" : "❌", true);
			embed.addField("Logs entrées", olympaGuild.isLogEntries() ? "✅" : "❌", true);
			embed.addField("Logs rôles", olympaGuild.isLogRoles() ? "✅" : "❌", true);
			embed.addField("Logs pseudo", olympaGuild.isLogUsername() ? "✅" : "❌", true);
			embed.addField("Logs channel vocal", olympaGuild.isLogVoice() ? "✅" : "❌", true);
			String s = "❌";
			if (olympaGuild.getLogChannelId() != 0)
				s = guild.getTextChannelById(olympaGuild.getLogChannelId()).getAsMention();
			embed.addField("Log Channel", s, true);
			s = "❌";
			if (!olympaGuild.getExcludeChannelsIds().isEmpty())
				s = olympaGuild.getExcludeChannelsIds().stream().map(id -> guild.getTextChannelById(id).getAsMention()).collect(Collectors.joining(", "));
			embed.addField("Excludes Log Channel", s, true);
			embed.addField("OlympaGuild Type", olympaGuild.getType().getName(), true);
		} else if (args[0].equalsIgnoreCase("reload"))
			try {
				GuildsHandler.updateGuild(DiscordSQL.selectGuildById(olympaGuild.getId()));
				embed.setDescription("✅ La config du discord `" + guild.getName() + "` a été rechargé.");
			} catch (SQLException e) {
				embed.setDescription("❌ Une erreur SQL est survenu: `" + e.getMessage() + "`.");
				e.printStackTrace();
			}
		channel.sendMessage(embed.build()).queue();
	}

}