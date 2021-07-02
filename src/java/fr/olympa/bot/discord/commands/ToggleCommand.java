package fr.olympa.bot.discord.commands;

import fr.olympa.api.common.match.RegexMatcher;
import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.commands.DiscordCommand;
import fr.olympa.bot.discord.member.DiscordMember;
import fr.olympa.bot.discord.member.MemberSettings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

public class ToggleCommand extends DiscordCommand {

	public ToggleCommand() {
		super("toggle", "parametre", "togglesettings");
		description = "Change tes paramètres.";
		privateChannel = true;
	}

	@Override
	public void onCommandSend(DiscordCommand command, String[] args, Message message, String label) {
		DiscordMember discordMember = getDm();
		EmbedBuilder embed = new EmbedBuilder().setTitle("🔧 Préférences");
		embed.setColor(OlympaBots.getInstance().getDiscord().getColor());

		if (args.length == 0) {
			embed.setDescription(description);
			int i = 1;
			for (MemberSettings set : MemberSettings.values())
				embed.addField(set.getName() + " (n°" + i++ + ")", discordMember.hasSetting(set) ? "✅" : "❌", true);
			embed.setFooter(DiscordCommand.prefix + label + " <numéro> pour changer un paramètre");
			message.getChannel().sendMessageEmbeds(embed.build()).queue();
		} else if (args.length > 0 && RegexMatcher.INT.is(args[0])) {
			MemberSettings[] values = MemberSettings.values();
			int i = RegexMatcher.INT.parse(args[0]) - 1;
			if (i >= 0 && i < values.length) {
				boolean newValue = discordMember.toggleSetting(values[i]);
				message.getChannel().sendMessage("Le paramètre `" + values[i].getName() + "` est désormais " + (newValue ? "✅" : "❌") + ".").queue();
			}
		} else
			message.getChannel().sendMessage("Le n° de paramètre `" + args[0] + "` est inconnu.").queue();
	}
}