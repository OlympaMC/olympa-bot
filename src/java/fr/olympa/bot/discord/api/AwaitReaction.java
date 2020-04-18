package fr.olympa.bot.discord.api;

import java.util.List;
import java.util.function.Consumer;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public class AwaitReaction {

	List<Role> roles;
	List<User> users;

	Message message;
	Consumer<User> consumer;
	Consumer<User> fail;
}
