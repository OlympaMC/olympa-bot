package fr.olympa.bot.discord.spam;

import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordUtils;
import fr.olympa.bot.discord.guild.GuildHandler;
import fr.olympa.bot.discord.guild.OlympaGuild;
import fr.olympa.bot.discord.guild.OlympaGuild.DiscordGuildType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SpamListener extends ListenerAdapter {

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		Message message = event.getMessage();
		Guild guild = message.getGuild();
		Member member = message.getMember();
		OlympaGuild olympaGuild = GuildHandler.getOlympaGuild(guild);
		if (olympaGuild.getType() == DiscordGuildType.STAFF || member == null || member.getUser().isBot() || event.getMessage().getReferencedMessage() != null
				&& message.getMentionedMembers().contains(event.getMessage().getReferencedMessage().getMember()))
			return;
		SpamHandler.removeAllTagMember(member);
		TextChannel channel = message.getTextChannel();
		List<Member> mentionedMembers = message.getMentionedMembers().stream().filter(m -> DiscordUtils.isReal(m.getUser()) && m.getPermissions(channel).contains(Permission.MESSAGE_READ)).distinct()
				.collect(Collectors.toList());
		if (mentionedMembers.isEmpty())
			return;
		List<Member> out = SpamHandler.addData(member, mentionedMembers);
		if (out.isEmpty())
			return;
		EmbedBuilder em = new EmbedBuilder();
		em.setTitle("Tu envoies trop de tag");
		if (out.size() == 1)
			em.setDescription("Attends que " + out.get(0).getAsMention() + " réponde à ta précédente mention avant de le re-mentionner.");
		else
			em.setDescription("Attends que " + out.stream().map(Member::getAsMention).collect(Collectors.joining(", ")) + " répondent à ta précédente mention avant de les re-mentionner.");
		em.setColor(OlympaBots.getInstance().getDiscord().getColor());
		channel.sendMessageEmbeds(em.build()).append(member.getAsMention()).queue();
	}
}
