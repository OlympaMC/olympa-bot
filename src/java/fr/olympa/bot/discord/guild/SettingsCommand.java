package fr.olympa.bot.discord.guild;

import java.sql.SQLException;
import java.util.stream.Collectors;

import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordPermission;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
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
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		MessageChannel channel = message.getChannel();
		Guild guild = message.getGuild();
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
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
			embed.addField("Logs insultes", olympaGuild.isLogInsult() ? "✅" : "❌", true);
			embed.addField("Message de status du bot", olympaGuild.isStatusMessageEnabled() ? "✅" : "❌", true);
			String s = "❌";
			if (olympaGuild.getLogChannelId() != 0)
				s = olympaGuild.getLogChannel().getAsMention();
			embed.addField("Log Channel", s, true);
			if (olympaGuild.getStaffChannelId() != 0)
				embed.addField("Staff Channel", olympaGuild.getStaffChannel().getAsMention(), true);
			if (olympaGuild.getBugsChannelId() != 0)
				embed.addField("Bugs Channel", olympaGuild.getBugsChannel().getAsMention(), true);
			if (olympaGuild.getMinecraftChannelId() != 0)
				embed.addField("Minecraft Channel", olympaGuild.getMinecraftChannel().getAsMention(), true);
			if (!olympaGuild.getExcludeChannelsIds().isEmpty())
				s = olympaGuild.getExcludeChannelsIds().stream().map(id -> guild.getTextChannelById(id).getAsMention()).collect(Collectors.joining(", "));
			embed.addField("Excludes Log Channel", s, true);
			embed.addField("OlympaGuild Type", olympaGuild.getType().getName(), true);
		} else if (args[0].equalsIgnoreCase("reload"))
			try {
				GuildHandler.updateGuild(GuildSQL.selectGuildById(olympaGuild.getId()));
				embed.setDescription("✅ La config du discord `" + guild.getName() + "` a été rechargé.");
			} catch (SQLException e) {
				embed.setDescription("❌ Une erreur SQL est survenu: `" + e.getMessage() + "`.");
				e.printStackTrace();
			}
		channel.sendMessageEmbeds(embed.build()).queue(msg -> {
			for (String unicode : new String[] { "0️⃣", "1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣" })
				msg.addReaction(unicode).queue();

		});
	}

}