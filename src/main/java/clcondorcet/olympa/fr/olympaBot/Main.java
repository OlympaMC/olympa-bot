package clcondorcet.olympa.fr.olympaBot;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
	/**
	 * La classe Main est la la class maitresse du bot.
	 * 
	 * Le bot fonctionne avec la librairie JDA qui permet la liaison avec discord.
	 * 
	 * Le bot a été conçu spécialement pour Olympa mais peut être
	 * ajouter par n'importe quel serveur discord sans problème,
	 * si les bonnes permission lui sont donné.
	 * 
	 * @author clcondorcet
	 * 
	 * @version 0.0.2 Master
	 * 
	 */
public class Main {

	
	public static JDA jda = null;
	public static String version = "0.0.2 Master";
	public static boolean canWork = true;
	public static SQL database = null;
	public static String path = "none";
	public static HashMap<String, String> idsChannels = new HashMap<>();
	
	
	/*
	 * 
	 * 
	 */
    public static void main( String[] args ) throws LoginException, InterruptedException {
    	//  Connect to discord
    	JDA jda = new JDABuilder(AccountType.BOT).setToken("NjE0OTM2NjQ5MDk2MjMzMTYw.XXFITw.pKpqS1CnXjBAMembDvW0gSTftyU").buildBlocking();
        
    	// register Events
    	jda.addEventListener(new MessageListener());
        jda.addEventListener(new InvitationListener());
        jda.addEventListener(new GuildListener());
        
        // set the "Joue à  ..."
        jda.getPresence().setGame(Game.playing("play.olympa.fr | !aide"));
        
        Main.jda = jda;
        
        // Get the path of the folder that hold the .jar
        try {
			path = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			path = path.replaceFirst("[^\\/]{3,}$", "");
		} catch (URISyntaxException e) {
			canWork = false;
			System.out.print("[WARN] - Impossible de trouver le chemin du jar, il est donc impossible de créé un fichier de config !\n");
			e.printStackTrace();
			System.out.print("[WARN] - Fin de l'erreur. Le bot est donc inutilisable ! Veuillez le redémarrer, si le problème persiste veuillez consulter son créateur (clcondorcet, discord: clcondorcet#1812) avec l'eureur si dessu.\n");
		}
        if(canWork) {
        	
        	// Get the file for SQL
        	File file = new File(path + "database.db");
        	if(!file.exists()) {
        		try {
					file.createNewFile();
				} catch (IOException e) {
					canWork = false;
					System.out.print("[WARN] - Impossible de créer la base de données.\n");
					e.printStackTrace();
					System.out.print("[WARN] - Fin de l'erreur. Le bot est donc inutilisable ! Veuillez le redémarrer, si le problème persiste veuillez consulter internet ou son créateur (clcondorcet, discord: clcondorcet#1812) avec l'eureur si dessu.\n");
				}
        	}
        	
        	// Stock all invites of all guilds
        	InvitationListener.stockAllGuildsInvites();
        	
        	// Set a thread that reconnect to the database all 5 minutes
        	Runnable databaseRunnable = () -> {
    			boolean task = true;
    			boolean first = true;
    			try {
    				while(task) {
    					// Set the SQL
    					database = new SQL(path + "database.db");
    					try {
							database.open();
						} catch (ClassNotFoundException | SQLException ex) {
							canWork = false;
							System.out.print("[WARN] - Impossible de se connecter à  la base de données.\n");
							ex.printStackTrace();
							System.out.print("[WARN] - Fin de l'erreur. Le bot est donc inutilisable ! Veuillez le redémarrer, si le problème persiste veuillez consulter internet ou son créateur (clcondorcet, discord: clcondorcet#1812) avec l'eureur si dessu.\n");
						}
    					
    					// Set the Tables
    						// connectionsChannels
    					if(!database.isTable("connectionsChannels")) {
    						try {
    							database.set("CREATE TABLE IF NOT EXISTS `connectionsChannels` ( `id` varchar(100) NOT NULL, `channel` varchar(100) NOT NULL);");
							} catch (SQLException e) {
								canWork = false;
								System.out.print("[WARN] - Impossible de créer une table dans la base de donnée.\n");
								e.printStackTrace();
								System.out.print("[WARN] - Fin de l'erreur. Le bot est donc inutilisable ! Veuillez le redémarrer, si le problème persiste veuillez consulter internet ou son créateur (clcondorcet, discord: clcondorcet#1812) avec l'eureur si dessu.\n");
							}
    					}
    						// Users
    					if(!database.isTable("Users")) {
    						try {
    							database.set("CREATE TABLE IF NOT EXISTS `Users` ( `GuildId` varchar(100) NOT NULL, `UserId` varchar(100) NOT NULL, `UserIdWhoInvite` varchar(100), `invites` int(11) NOT NULL, `invitesFakes` int(11) NOT NULL, `invitesLeaves` int(11) NOT NULL, `invitesBonus` int(11) NOT NULL);");
							} catch (SQLException e) {
								canWork = false;
								System.out.print("[WARN] - Impossible de créer une table dans la base de donnée.\n");
								e.printStackTrace();
								System.out.print("[WARN] - Fin de l'erreur. Le bot est donc inutilisable ! Veuillez le redémarrer, si le problème persiste veuillez consulter internet ou son créateur (clcondorcet, discord: clcondorcet#1812) avec l'eureur si dessu.\n");
							}
    					}
    					
    					// Get the stats when the bot start
    					if(first) {
    						first = false;
    						try {
    							// Get the channel where put the connections text for all guilds
								ResultSet result = database.get("SELECT * FROM connectionsChannels;");
								while(result.next()) {
									idsChannels.put(result.getString("id"), result.getString("channel"));
								}
							} catch (SQLException e) {
								canWork = false;
								System.out.print("[WARN] - Impossible de prendre les données dans la table connectionsChannels de la base de données.\n");
								e.printStackTrace();
								System.out.print("[WARN] - Fin de l'erreur. Le bot est donc inutilisable ! Veuillez le redémarrer, si le problème persiste veuillez consulter internet ou son créateur (clcondorcet, discord: clcondorcet#1812) avec l'eureur si dessu.\n");
							}
    						for(Guild guild : jda.getGuilds()) {
    							System.out.print("[INFO] - checkchannel pour le serveur " + guild.getName() + " (" + guild.getId() + ")\n");
    							checkchannel(guild);
    						}
    					}
    					Thread.sleep(1000 * 60 * 5);
    				}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		};
    		Thread databaseThread = new Thread(databaseRunnable);
    		databaseThread.start();
        	System.out.print("[INFO] - Le bot est prêt ! version: " + version + "\n");	
        }
    }
    
    public static void checkchannel(Guild guild) {
    	if(idsChannels.containsKey(guild.getId())) {
    		System.out.print("[INFO] - Le serveur est déjà  enregistrer, récupération du channel ...\n");
    		boolean isChannel = false;
    		for(TextChannel txt : guild.getTextChannels()) {
    			if(txt.getId().equals(idsChannels.get(guild.getId()))) {
    				isChannel = true;
    				break;
    			}
    		}
    		if(!isChannel) {
    			System.out.print("[INFO] - Le channel n'existe plus !\n");
    			idsChannels.put(guild.getId(), guild.getDefaultChannel().getId());
    			try {
    				System.out.print("[INFO] - Essai d'update dans la base de donnée.\n");
					database.set("UPDATE `connectionsChannels` SET channel='" + guild.getDefaultChannel().getId() + "' WHERE id='" + guild.getId() + "';");
					System.out.print("[INFO] - Enregistrement réussi !\n");
    			} catch (SQLException e) {
					System.out.print("[WARN] - Impossible d'enregistrer les nouvelles données dans la base de données de la guilde " + guild.getName() + " (" + guild.getId() + ") avec comme valeur : " + guild.getDefaultChannel().getId() + "\n");
					e.printStackTrace();
					System.out.print("[WARN] - Fin de l'erreur.\n");
				}
    		}
    	}else {
    		System.out.print("[INFO] - Le serveur n'est pas enregistrer.\n");
    		idsChannels.put(guild.getId(), guild.getDefaultChannel().getId());
			try {
				System.out.print("[INFO] - Essai d'enregister dans la base de donnée.\n");
				database.set("INSERT INTO `connectionsChannels` (`id`, `channel`) VALUES ('" + guild.getId() + "','" + guild.getDefaultChannel().getId() + "')");
				System.out.print("[INFO] - Enregistrement réussi !\n");
			} catch (SQLException e) {
				System.out.print("[WARN] - Impossible d'enregistrer les nouvelles données dans la base de données de la guilde " + guild.getName() + " (" + guild.getId() + ") avec comme valeur : " + guild.getDefaultChannel().getId() + "\n");
				e.printStackTrace();
				System.out.print("[WARN] - Fin de l'erreur.\n");
			}
    	}
    }
    
    public TextChannel getConnectionsChannel(Guild guild) {
    	checkchannel(guild);
    	return guild.getTextChannelById(idsChannels.get(guild.getId()));
    }
}
