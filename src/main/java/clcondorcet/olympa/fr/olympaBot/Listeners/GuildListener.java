package clcondorcet.olympa.fr.olympaBot.Listeners;

import clcondorcet.olympa.fr.olympaBot.Main;
import clcondorcet.olympa.fr.olympaBot.Functions.Invitations;
import clcondorcet.olympa.fr.olympaBot.Functions.WelcomeMessages;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.hooks.EventListener;

/**
 * Listener for the Guild Join and Leave events by JDA.
 * 
 * @author clcondorcet
 * @since 0.0.1
 */
public class GuildListener implements EventListener {
	
	/**
	 * Generic method that is being call by JDA.
	 * 
	 * @param event The event.
	 */
	@Override
    public void onEvent(Event event) {
		if(event instanceof GuildJoinEvent){
			if(!Main.canWork) {
				return;
			}
			GuildJoinEvent e = (GuildJoinEvent) event;
			WelcomeMessages.addNewGuild(e.getGuild());
			Invitations.addGuildInvites(e.getGuild());
		}else if(event instanceof GuildLeaveEvent){
			if(!Main.canWork) {
				return;
			}
			GuildLeaveEvent e = (GuildLeaveEvent) event;
			WelcomeMessages.removeGuild(e.getGuild());
			Invitations.removeGuildInvites(e.getGuild());
		}else if(event instanceof GuildMemberJoinEvent){
			if(!Main.canWork) {
				return;
			}
			GuildMemberJoinEvent e = (GuildMemberJoinEvent) event;
			WelcomeMessages.memberJoinEvent(e);
		}else if(event instanceof GuildMemberLeaveEvent){
			if(!Main.canWork) {
				return;
			}
			GuildMemberLeaveEvent e = (GuildMemberLeaveEvent) event;
			Invitations.memberLeaveEvent(e);
		}
	}
}
