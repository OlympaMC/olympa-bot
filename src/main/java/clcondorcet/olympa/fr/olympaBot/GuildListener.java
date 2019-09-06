package clcondorcet.olympa.fr.olympaBot;

import java.sql.SQLException;

import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.hooks.EventListener;

/**
 * @author clcondorcet
 *
 * 
 */
public class GuildListener implements EventListener {
	
	@Override
    public void onEvent(Event event) {
		if(event instanceof GuildJoinEvent){
			if(!Main.canWork) {
				return;
			}
			GuildJoinEvent e = (GuildJoinEvent) event;
			Main.idsChannels.put(e.getGuild().getId(), e.getGuild().getDefaultChannel().getId());
			try {
				Main.database.set("INSERT INTO `connectionsChannels` (`id`, `channel`) VALUES ('" + e.getGuild().getId() + "','" + e.getGuild().getDefaultChannel().getId() + "')");
			} catch (SQLException ex) {
				System.out.print("[WARN] - Impossible d'enregistrer les nouvelles données dans la base de données de la guilde " + e.getGuild().getName() + " (" + e.getGuild().getId() + ") avec comme valeur : " + e.getGuild().getDefaultChannel().getId() + "\n");
				ex.printStackTrace();
				System.out.print("[WARN] - Fin de l'erreur.\n");
			}
			InvitationListener.addGuildInvites(e.getGuild());
		}else if(event instanceof GuildLeaveEvent){
			if(!Main.canWork) {
				return;
			}
			GuildLeaveEvent e = (GuildLeaveEvent) event;
			if(Main.idsChannels.containsKey(e.getGuild().getId())) {
				Main.idsChannels.remove(e.getGuild().getId());
				try {
					Main.database.set("DELETE FROM `connectionsChannels` WHERE id='" + e.getGuild().getId() + "';");
				} catch (SQLException ex) {
					System.out.print("[WARN] - Impossible d'enregistrer les nouvelles données dans la base de données de la guilde " + e.getGuild().getName() + " (" + e.getGuild().getId() + ") avec comme valeur : " + e.getGuild().getDefaultChannel().getId() + "\n");
					ex.printStackTrace();
					System.out.print("[WARN] - Fin de l'erreur.\n");
				}
			}
		}
	}
}
