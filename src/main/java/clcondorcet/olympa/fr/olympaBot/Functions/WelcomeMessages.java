package clcondorcet.olympa.fr.olympaBot.Functions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import clcondorcet.olympa.fr.olympaBot.Main;
import clcondorcet.olympa.fr.olympaBot.Listeners.GuildListener;
import clcondorcet.olympa.fr.olympaBot.Utilities.Messages;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Invite;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;

/**
 * Welcome messages manager
 * 
 * Events: 
 *  - GuildJoinEvent
 *  - GuildLeaveEvent
 *  - GuildMemberJoinEvent
 * Commands:
 *  - setChannel
 *  
 * @author clcondorcet
 * @since 0.0.3
 */
public class WelcomeMessages {
	
	/**
	 * Id of the channel where welcomes messages will be send.
	 */
	public static HashMap<String, String> idsChannels = new HashMap<>();
	
	/**
	 * Recover channels data for every guilds from the database.
	 */
	public static void getChannels(){
		try {
			// Get the channel where put the connections text for all guilds
			ResultSet result = Main.database.get("SELECT * FROM connectionsChannels;");
			while(result.next()) {
				idsChannels.put(result.getString("id"), result.getString("channel"));
			}
		} catch (SQLException e) {
			Main.canWork = false;
			System.out.print("[WARN] - Impossible de prendre les données dans la table connectionsChannels de la base de données.\n");
			e.printStackTrace();
			System.out.print("[WARN] - Fin de l'erreur. Le bot est donc inutilisable ! Veuillez le redémarrer, si le problème persiste veuillez consulter internet ou son créateur (clcondorcet, discord: clcondorcet#1812) avec l'eureur si dessu.\n");
		}
		for(Guild guild : Main.jda.getGuilds()) {
			System.out.print("[INFO] - checkchannel pour le serveur " + guild.getName() + " (" + guild.getId() + ")\n");
			checkchannel(guild);
		}

	}
	
	/**
     * Check if channels where messages will be send exist in the database and are always here.
     * 
     * @param guild The guild where the check is going to occure.
     * @since 0.0.1
     */
    public static void checkchannel(Guild guild) {
    	// If the channel is in the database, it will be also in the "idsChannels" HashMap
    	if(idsChannels.containsKey(guild.getId())) {
    		// This channel already exist in the database.
    		
    		System.out.print("[INFO] - Le serveur est déjà  enregistrer, récupération du channel ...\n");
    		boolean isChannel = false;
    		// Check if the channel exist in the guild.
    		for(TextChannel txt : guild.getTextChannels()) {
    			if(txt.getId().equals(idsChannels.get(guild.getId()))) {
    				isChannel = true;
    				break;
    			}
    		}
    		if(!isChannel) {
    			// The channel does'nt exist anymore. So the channel is replaced by the default channel of the guild.
    			System.out.print("[INFO] - Le channel n'existe plus !\n");
    			idsChannels.put(guild.getId(), guild.getDefaultChannel().getId());
    			try {
    				System.out.print("[INFO] - Essai d'update dans la base de donnée.\n");
					Main.database.set("UPDATE `connectionsChannels` SET channel='" + guild.getDefaultChannel().getId() + "' WHERE id='" + guild.getId() + "';");
					System.out.print("[INFO] - Enregistrement réussi !\n");
    			} catch (SQLException e) {
					System.out.print("[WARN] - Impossible d'enregistrer les nouvelles données dans la base de données de la guilde " + guild.getName() + " (" + guild.getId() + ") avec comme valeur : " + guild.getDefaultChannel().getId() + "\n");
					e.printStackTrace();
					System.out.print("[WARN] - Fin de l'erreur.\n");
				}
    		}
    	}else {
    		// The guild is not in the database. So the default channel of the guild is use and the guild is register in the database.
    		System.out.print("[INFO] - Le serveur n'est pas enregistrer.\n");
    		idsChannels.put(guild.getId(), guild.getDefaultChannel().getId());
			try {
				System.out.print("[INFO] - Essai d'enregister dans la base de donnée.\n");
				Main.database.set("INSERT INTO `connectionsChannels` (`id`, `channel`) VALUES ('" + guild.getId() + "','" + guild.getDefaultChannel().getId() + "')");
				System.out.print("[INFO] - Enregistrement réussi !\n");
			} catch (SQLException e) {
				System.out.print("[WARN] - Impossible d'enregistrer les nouvelles données dans la base de données de la guilde " + guild.getName() + " (" + guild.getId() + ") avec comme valeur : " + guild.getDefaultChannel().getId() + "\n");
				e.printStackTrace();
				System.out.print("[WARN] - Fin de l'erreur.\n");
			}
    	}
    }
    
    /**
     * get the channel of this guild where the bot is going to send welcome messages.
     * 
     * @param guild The guild where to get the channel to.
     * @return the textChannel
     */
    public TextChannel getConnectionsChannel(Guild guild) {
    	checkchannel(guild);
    	return guild.getTextChannelById(idsChannels.get(guild.getId()));
    }
    
    /**
     * When the bot join a new guild, listener call it.
     * So a the default channel is register in the database and in the HashMap
     * 
     * @param guild The joined guild
     */
    public static void addNewGuild(Guild guild){
    	idsChannels.put(guild.getId(), guild.getDefaultChannel().getId());
		try {
			Main.database.set("INSERT INTO `connectionsChannels` (`id`, `channel`) VALUES ('" + guild.getId() + "','" + guild.getDefaultChannel().getId() + "')");
		} catch (SQLException ex) {
			System.out.print("[WARN] - Impossible d'enregistrer les nouvelles données dans la base de données de la guilde " + guild.getName() + " (" + guild.getId() + ") avec comme valeur : " + guild.getDefaultChannel().getId() + "\n");
			ex.printStackTrace();
			System.out.print("[WARN] - Fin de l'erreur.\n");
		}
    }
    
    /**
     * When the bot leave a guild, listener call it.
     * So this guild is remove in the database and in the HashMap
     * 
     * @param guild The joined guild
     */
    public static void removeGuild(Guild guild){
    	if(idsChannels.containsKey(guild.getId())) {
			idsChannels.remove(guild.getId());
			try {
				Main.database.set("DELETE FROM `connectionsChannels` WHERE id='" + guild.getId() + "';");
			} catch (SQLException ex) {
				System.out.print("[WARN] - Impossible d'enregistrer les nouvelles données dans la base de données de la guilde " + guild.getName() + " (" + guild.getId() + ") avec comme valeur : " + guild.getDefaultChannel().getId() + "\n");
				ex.printStackTrace();
				System.out.print("[WARN] - Fin de l'erreur.\n");
			}
		}
    }
    
    
    /**
     * Send the welcome message in the correct TextChannel.
     * Also get and set the invite of a Member.
     * @see Invitations
     * 
     * @param e get the event {@link GuildMemberJoinEvent} from {@link GuildListener}.
     */
    public static void memberJoinEvent(GuildMemberJoinEvent e){
    	// Take invites from the guild to compare with past data
    	Invite inv = Invitations.getInviteThatChange(e.getGuild());
    	// Update the data for next event
    	Invitations.invites.get(e.getGuild().getId()).put(inv.getCode(), inv.getUses());
    	// Get the type of account
    	if(inv.getInviter().isFake() || inv.getInviter().isBot()){
    		// Get the number of people invited by this user
    		int i = Invitations.getNumberOfInvitesFakes(e.getGuild(), inv.getInviter().getId()) + 1;
    		// Update the number
    		Invitations.setNumberOfInvitesFakes(e.getGuild(), inv.getInviter().getId(), i);
    		Invitations.addMemberToDb(e.getGuild(), e.getUser().getId(), inv.getInviter().getId());
    	}else{
    		// Get the number of people invited by this user
    		int i = Invitations.getNumberOfInvites(e.getGuild(), inv.getInviter().getId()) + 1;
    		// Update the number
    		Invitations.setNumberOfInvites(e.getGuild(), inv.getInviter().getId(), i);
    		Invitations.addMemberToDb(e.getGuild(), e.getUser().getId(), inv.getInviter().getId());
    	}
    	int i = Invitations.getNumberOfInvites(e.getGuild(), inv.getInviter().getId()) + Invitations.getNumberOfInvitesBonus(e.getGuild(), inv.getInviter().getId()) - Invitations.getNumberOfInvitesLeaves(e.getGuild(), inv.getInviter().getId());
    	
    	// Message
    	Messages.sendWelcomeMessage(Main.jda.getTextChannelById(e.getGuild().getId()), e.getMember().getAsMention(), inv.getInviter().getAsMention(), i, e.getGuild().getMembers().size());
    }
    
    /**
     * Set the channel where welcome messages is going to be send.
     * Send an error message an log the error if there is an SQL error.
     * 
     * @param guild The guild where the command is send.
     * @param channel The channel where the command is send.
     * @param author The author of the command.
     */
    public static void setChannelCommand(Guild guild, TextChannel channel, User author){
		try {
			Main.database.set("UPDATE `connectionsChannels` SET channel='" + channel.getId() + "' WHERE id='" + guild.getId() + "';");
			idsChannels.put(guild.getId(), channel.getId());
			Messages.sendDoneMessage(channel, "Le channel ou les messages seront émis a bien été mis à jour ! ;)", 10);
		} catch (SQLException e1) {
			e1.printStackTrace();
			Messages.sendErrorMessage(channel, "Erreur ! Veuillez ressayer plus tard.", 10);
		}
    }
    
}
