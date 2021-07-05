package fr.olympa.bot.discord.support.chat;

import java.util.List;
import java.util.concurrent.TimeUnit;

import fr.olympa.api.common.task.NativeTask;
import fr.olympa.bot.discord.message.JumpURL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SupportChatListener extends ListenerAdapter {

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		Message message = event.getMessage();
		List<User> mentions = message.getMentionedUsers();
		SelfUser me = event.getJDA().getSelfUser();
		if (event.getAuthor().isBot() || !mentions.contains(me))
			return;
		sendMessage(message);
	}

	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		User member = event.getAuthor();
		if (member.isBot())
			return;
		Message message = event.getMessage();
		MessageChannel channel = event.getChannel();
		if (member.getIdLong() != 450125243592343563L && member.getIdLong() != 481941189646483456L)
			sendMessage(message);
		else {
			String msg = message.getContentRaw();
			String[] args = msg.split(" ");
			if (args.length == 0)
				return;
			String id = args[0];
			try {
				if (id.startsWith("@")) {
					User user = event.getJDA().getUserById(id.substring(1));
					String msgFinal = msg.substring(id.length() + 1);
					user.openPrivateChannel().queue(pv -> {
						pv.sendTyping().queue();
						NativeTask.getInstance().runTaskLater(() -> pv.sendMessage(msgFinal).queue(), msgFinal.length() * 100l, TimeUnit.MILLISECONDS);
					});
					channel.sendMessage("Message envoyé à " + user.getAsMention()).queue();

				} else if (id.startsWith("#")) {
					TextChannel txtCh = event.getJDA().getTextChannelById(id.substring(1));
					String msgFinal = msg.substring(id.length() + 1);
					txtCh.sendTyping().queue();
					NativeTask.getInstance().runTaskLater(() -> txtCh.sendMessage(msgFinal).queue(), msgFinal.length() * 100l, TimeUnit.MILLISECONDS);
					channel.sendMessage("Message envoyé dans " + txtCh.getAsMention() + " sur " + txtCh.getGuild().getName()).queue();

				} else if (id.startsWith("!")) {

				}
			} catch (Exception e) {
				channel.sendMessage("Mauvais format " + e.getMessage()).queue();
			}
		}
	}

	public void sendMessage(Message message) {
		User author = message.getJDA().getUserById(450125243592343563L);
		List<Attachment> attachments = message.getAttachments();
		String msg = message.getContentRaw();
		EmbedBuilder eb = new EmbedBuilder();

		User user = message.getAuthor();
		eb.setDescription(user.getAsMention() + "(" + user.getIdLong() + ")");
		eb.addField("Message", msg, false);
		for (Attachment att : attachments)
			eb.addField(att.getFileName(), att.getProxyUrl(), true);
		if (message.isFromGuild()) {
			TextChannel gc = (TextChannel) message.getChannel();
			eb.addField("Dans ", gc.getAsMention() + " " + new JumpURL(message).get(), true);
			if (message.getReferencedMessage() == null)
				eb.setTitle("Mentionner dans le channel " + gc.getName());
			else {
				Message msgReferenced = message.getReferencedMessage();
				eb.setTitle("Répondu à un message du bot dans le channel " + gc.getName());
				eb.addField("Citation de réponse de " + msgReferenced.getAuthor().getAsMention(), msgReferenced.getContentRaw(), false);
			}
		} else
			eb.setTitle("Message privé reçu");
		author.openPrivateChannel().queue(ch -> ch.sendMessageEmbeds(eb.build()).queue());
	}
}
