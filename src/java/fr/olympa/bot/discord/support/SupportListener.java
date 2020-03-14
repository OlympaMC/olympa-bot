package fr.olympa.bot.discord.support;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SupportListener extends ListenerAdapter {
	
	@Override
	public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
		Member member = event.getMember();
		SupportHandler.updateChannel(member);
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		Member member = event.getMember();
		MessageChannel channel = event.getChannel();
		if (!event.isFromGuild() || !SupportHandler.isSupportChannel(channel, member)) {
			return;
		}
		TextChannel channelSupport = SupportHandler.getChannel(member);
		if (channelSupport.getIdLong() != channel.getIdLong()) {
			return;
		}
		StatusSupportChannel status = SupportHandler.getChannelStatus(channelSupport);
		switch (status) {
		case CLOSE:
			break;
		case OPEN:
			SupportHandler.setChannelStatus(channelSupport, StatusSupportChannel.WAITING);
			SupportHandler.askWhoStaff(channelSupport, member);
			break;
		case PROGRESS:
			SupportHandler.setChannelStatus(channelSupport, StatusSupportChannel.WAITING);
			break;
		case WAITING:
			break;
		default:
			break;
		}
	}

}
