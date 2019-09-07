package clcondorcet.olympa.fr.olympaBot;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

import javax.security.auth.login.LoginException;

import clcondorcet.olympa.fr.olympaBot.Functions.Invitations;
import clcondorcet.olympa.fr.olympaBot.Functions.WelcomeMessages;
import clcondorcet.olympa.fr.olympaBot.Listeners.GuildListener;
import clcondorcet.olympa.fr.olympaBot.Listeners.MessageListener;
import clcondorcet.olympa.fr.olympaBot.Utilities.SQL;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;

	/**
	 * La classe Main est la la class maitresse du bot.
	 * 
	 * Le bot fonctionne avec la librairie JDA qui permet la liaison avec discord.
	 * 
	 * Le bot a été conçu spécialement pour Olympa mais peut être
	 * ajouter par n'importe quel serveur discord sans problème,
	 * si les bonnes permissions lui sont donné.
	 * 
	 * ---------------------
	 * 
	 * This class is the Main class of this bot.
	 * 
	 * This bot work with the JDA library that ensure the connection to Discord.
	 * 
	 * This bot is made for Olympa, but it can be add to any
	 * guilds you want as long as you give the good permissions to
	 * the bot.
	 * 
	 * @author clcondorcet
	 * 
	 * @version 0.0.3 Dev
	 * 
	 */
public class Main {

	/**
	 * It's the JDA instance of the bot.
	 */
	public static JDA jda = null;
	
	/**
	 * Version of the code.
	 */
	public static String version = "0.0.3 Dev";
	
	/**
	 * Variable that define if the bot can work proprely or not.
	 */
	public static boolean canWork = true;
	
	/**
	 * The database connection.
	 */
	public static SQL database = null;
	
	/**
	 * The path of the directory where the .jar file is.
	 */
	public static String path = "none";
	
	/**
	 * Main method of the project.
	 * 
	 * Init the bot
	 * 
	 * @param args Main parameters.
	 * @since 0.0.1
	 * @throws LoginException JDA error.
	 * @throws InterruptedException JDA error.
	 */
    public static void main( String[] args ) throws LoginException, InterruptedException {
    	//  Start JDA association with the TOKEN of the bot
    	JDA jda = new JDABuilder(AccountType.BOT).setToken("NjE5MjI0NzAyNTY4NjI4MjQ1.XXFIGg.Sk2dvSiOf2KKzcWV7__1auZFUvE").buildBlocking();
        
    	// Register events
    	jda.addEventListener(new MessageListener());
        jda.addEventListener(new GuildListener());
        
        // Set the playing text.
        jda.getPresence().setGame(Game.playing("play.olympa.fr | !aide"));
        
        // Set the jda instance into a public variable.
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
        	Invitations.stockAllGuildsInvites();
        	
        	// Set a thread that reconnect to the database all 5 minutes
        	Runnable databaseRunnable = () -> {
    			boolean task = true;
    			boolean first = true;
    			try {
    				while(task) {
    					// Set the SQL after close the first one if there is one
    					try{
    						database.closeConnection();
    					}catch(Exception ex){}
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
    						// Recover all the channel data for welcome messages.
    						WelcomeMessages.getChannels();
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
}
