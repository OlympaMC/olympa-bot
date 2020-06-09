package fr.olympa.bot.discord.spam;

import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.bot.OlympaBots;
import fr.olympa.bot.discord.api.DiscordIds;
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
		if (DiscordIds.getDefaultGuild().getIdLong() != guild.getIdLong() || member.getUser().isBot())
			return;
		SpamHandler.removeAllTagMember(member);
		TextChannel channel = message.getTextChannel();
		List<Member> mentionedMembers = message.getMentionedMembers().stream().filter(m -> !m.getUser().isBot() && !m.getUser().isFake() && m.getPermissions(channel).contains(Permission.MESSAGE_READ)).collect(Collectors.toList());
		if (mentionedMembers.isEmpty())
			return;
		List<Member> out = SpamHandler.addData(member, mentionedMembers);
		if (out.isEmpty())
			return;
		EmbedBuilder em = new EmbedBuilder();
		em.setTitle("Mais pas si vite !");
		if (out.size() == 1)
			em.setDescription("Attends que " + out.get(0).getAsMention() + " réponde à ta précédente mention avant de le re-mentionner.");
		else
			em.setDescription("Attends que " + out.stream().map(Member::getAsMention).collect(Collectors.joining(", ")) + " répondent à ta précédente mention avant de les re-mentionner.");
		em.setColor(OlympaBots.getInstance().getDiscord().getColor());
		channel.sendMessage(member.getAsMention()).queue(m -> channel.sendMessage(em.build()).queue());
	}
}
