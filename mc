#!/bin/bash
# screen -S rush1 -X stuff "say test$(printf '\r')"

# PARAMETRE

# Commandes
START="start"
STOP="stop"
SEE="see"
BACKUP="backup"
RESTART="restart"
SSH="ssh"

# Commande en cas d'erreur ou d'arguments non d?nie
USAGE="Usage: \e[0;36m$(basename "$0") $START|$STOP|$SEE all|<server>\e[0m"

# Format de la date 
DATE="$(date +"%a %d %b %Y %H:%M")"
DATE_DAY="$(date +"%j")"

# Dossier des serveurs (dossier qui contient les dossiers qui contiennent les fichiers du serveur). Il est recommand?e cr?un dossier dans /home tel que /home/<nom de votre network> pour y mettre tous les autres serveurs.
SERVEUR_DIR="/home/serveurs"
# Nom des script qui lance le serveur (qui lance le .jar). Par default start.sh
STARTSH="start.sh"

# Nom de l'utilisateur avec lequel les serveurs se lance. Par default root
USER="minecraft"

# Serveurs:
# ALL="$(ls $SERVEUR_DIR)"
ALL="bungee1 buildeur zta1 auth1"

# Backup mysql
BDD_USER="olympa"
BDD_PASSWORD="UTF7rEREBLgZnWlnrdZtO7"
BDD_HOST="localhost"

BACKUP_DIR="/home/.backup"

# FTP sauvegarde ################################
# IP (chiffr?u dns) du ftp
FTP_HOST=""
# Port du ftp (default 21)
FTP_PORT="21"
# Nom d'utiliser du compte ftp
FTP_USER=""
# Mdp du compte ftp
FTP_PASSWORD=""
# Dossier des backups sur le ftp
FTP_DIR="/serveurs/"
#- - - - - - - - - - - - - - - - - - - - - - - - - - START - - - - - - - - - - - - - - - - - - - - - - - - - -
if [ "$1" = "$START" ]; then
	if [ -n "$2" ]; then
		if [ "$2" = "all" ]; then
			serv="$ALL"
		else
			serv="${*:2}"
		fi
		echo
		for element in $serv
		do
		if [ -d "$SERVEUR_DIR/$element" ];then
			if [ -f "$SERVEUR_DIR/$element/$STARTSH" ];then
				if [ `screen -ls | grep -w $element | wc -l` -eq "0" ]; then
					serv_lance+=" $element"
				else
					serv_ouvert+=" $element"
				fi
			else
				serv_script+=" $element"
			fi
		else
			serv_dossier+=" $element"
		fi
		done
		if [ -n "$serv_dossier" ]; then
			echo -e "\e[31m$serv_dossier\e[0;31m n'existe pas (dossier $SERVEUR_DIR/nom du serveur)\e[0m"
		fi
		if [ -n "$serv_script" ]; then	
			echo -e "\e[35m$serv_script\e[0;35m n'existe pas (fichier $STARTSH)\e[0m"
		fi
		if [ -n "$serv_ouvert" ]; then
			echo -e "\e[36m$serv_ouvert\e[0;36m est déjà ouvert\e[0m"
		fi
		if [ -n "$serv_lance" ]; then
			for element in $serv_lance
			do		
				cd $SERVEUR_DIR/$element
				bash $SERVEUR_DIR/$element/$STARTSH
			done
			echo -e "\e[32m$serv_lance\e[0;32m a démarré\e[0m"
		fi
		echo
	else
		read -p 'Quel(s) serveur(s) ? ' server_select
		$0 $START $server_select
	fi
#- - - - - - - - - - - - - - - - - - - - - - - - - - STOP - - - - - - - - - - - - - - - - - - - - - - - - - -
elif [ "$1" = "$STOP" ]; then
	if [ -n "$2" ]; then
		if [ "$2" = "all" ]; then
			serv="$ALL"
		else
			serv="${*:2}"
		fi
		for element in $serv
		do
			if [ `screen -ls | grep -w $element | wc -l` -eq "1" ]; then
				if [[ $element == bungee* ]]; then
					serv_vafermerb+=" $element"
				else
					serv_vafermer+=" $element"
				fi
			else
				serv_fermer+=" $element"
			fi
		done
	echo
	if [ -n "$serv_vafermerb" ]; then
		for element in $serv_vafermerb
		do
			screen -S $element -X stuff 'end\r'
		done
		echo -e "\e[36m$serv_vafermerb\e[0;36m s'est arrêté (BUNGEE)\e[0m"
		
	fi
	if [ -n "$serv_vafermer" ]; then
		for element in $serv_vafermer
		do		
			screen -S $element -X stuff 'stop\r'
		done
		echo -e "\e[36m$serv_vafermer\e[0;36m s'est arrêté \e[0m"
	fi
	if [ -n "$serv_fermer" ]; then
		echo -e "\e[31m$serv_fermer\e[0;31m n'est pas ouvert\e[0m"
	fi
	
	else
		read -p 'Quel(s) serveur(s) ? ' server_select
		$0 $STOP $server_select
	fi
	echo
#- - - - - - - - - - - - - - - - - - - - - - - - - - - BACKUP - - - - - - - - - - - - - - - - - - - - - - - - - -
elif [ "$1" = "$BACKUP" ]; then
	screen -dmS delLogBackup find "$BACKUP_DIR/" -type f -atime +3 -exec rm {} \;
	echo -e "\e[94mSuppression des backups de + de 3 jours effectué."
	for element in `ls $SERVEUR_DIR`
	do
		screen -dmS delLog$element find "$SERVEUR_DIR/$element/logs" -type f -mtime +3 -exec rm {} \;
		if [ `ls /run/screen/S-$USER | grep -w $element | wc -l` -eq "1" ]; then
			serv_allumer+=" $element"
		fi
	done
	echo -e "\e[94mSuppression des logs des serveurs minecraft de + de 3 jours effectué.\e[0m"
	if [ "$serv_allumer" != "" ]; then
		echo -e "\e[93mLes serveurs$serv_allumer sont encore ouvert, les joueurs ont reçu un message leur annonçant la fermeture dans 15 mins.\e[0m"
		for element in $serv_allumer
		do
			if [[ $element == bungee* ]]; then
				backupb+=" $element"
			else
				backupm+=" $element"
			fi
		done
		for element in $backupm
		do
			screen -S $element -X stuff 'tellraw @a [\"\",{\"text\":\"§4[§6§lINFO§4] \",\"bold\":true,\"text\":\"§c§lRedémarrage des serveurs dans 15 minutes\",\"bold\":true}]\r'
		done
		echo -e "\e[93mFermeture dans 15 mins.\e[0m"
		sleep 300
		
		for element in $backupm
		do
			screen -S $element -X stuff 'tellraw @a [\"\",{\"text\":\"§4[§6§lINFO§4] \",\"bold\":true,\"text\":\"§c§lRedémarrage des serveurs dans 10 minutes\",\"bold\":true}]\r'
		done
		echo -e "\e[93mFermeture dans 10 mins.\e[0m"
		sleep 300
		
		for element in $backupm
		do
			screen -S $element -X stuff 'tellraw @a [\"\",{\"text\":\"§4[§6§lINFO§4] \",\"bold\":true,\"text\":\"§c§lRedémarrage des serveurs dans 5 minutes\",\"bold\":true}]\r'
		done
		echo -e "\e[93mFermeture dans 5 mins.\e[0m"
		sleep 180
		for element in $backupm
		do
			screen -S $element -X stuff 'tellraw @a [\"\",{\"text\":\"§4[§6§lINFO§4] \",\"bold\":true,\"text\":\"§c§lRedémarrage des serveurs dans 2 minutes\",\"bold\":true}]\r'
		done
		echo -e "\e[93mFermeture dans 2 mins.\e[0m"
		sleep 60
		
		for element in $backupm
		do
			screen -S $element -X stuff 'tellraw @a [\"\",{\"text\":\"§4[§6§lINFO§4] \",\"bold\":true,\"text\":\"§c§lRedémarrage des serveurs dans 1 minute\",\"bold\":true}]\r'
		done
		echo -e "\e[93mFermeture dans 1 min.\e[0m"
		sleep 60
		for element in $backupm
		do
			screen -S $element -X stuff 'kickall §6§lRedémarrage des serveurs en cours\r'
		done
		echo -e "\e[93mTous les joueurs ont été kick.\e[0m"
		sleep 2
		mc stop $serv_allumer
		
		serv_allumer2="open"
		i=0
		while [ "$serv_allumer2" != "" ] && [ "$i" -lt "60" ]
		do
			sleep 1
			serv_allumer2=""
			for element in $serv_allumer
			do
				if [ `ls /run/screen/S-$USER | grep -w $element | wc -l` -eq "1" ]; then
					serv_allumer2+=" $element"
				fi
			done
			let "i = i + 1"
			if [ "$serv_allumer2" != "" ]; then
				echo -e "\e[91mLes serveurs\e[36m$serv_allumer2\e[91m ne sont pas encore fermée, attente $i secondes / 60 secondes\e[0m"
			fi
		done
		for element in `ls $SERVEUR_DIR`
		do
			if [ `ls /run/screen/S-$USER | grep -w $element | wc -l` -eq "1" ]; then
				screen -X -S $element kill
				echo -e "\e[91m$element a été fermer de force.\e[0m"
			fi
		done
	fi
	echo -e "\e[32mCréation de la backup des serveurs...\e[0m"
	tar -c $SERVEUR_DIR | pv -s $(du -sb $SERVEUR_DIR | awk '{print $1}') > "$BACKUP_DIR/$DATE_DAY | serveurs.tar"
	echo -e "\e[32mCréation de la backup bdd ...\e[0m"
	
	SIZE_BYTES=$(mysql --skip-column-names -h $BDD_HOST -u $BDD_USER -p$BDD_PASSWORD <<< 'SELECT SUM(data_length) AS "size_bytes" FROM information_schema.TABLES;')
	mysqldump -h $BDD_HOST -u $BDD_USER -p$BDD_PASSWORD -A -x | pv --progress --size $SIZE_BYTES > "$BACKUP_DIR/$DATE_DAY | databases.sql"
	
	echo -e "\e[92mRéouverture de tous les serveurs...\e[0m"
	sleep 5
	mc start all
	echo -e "\e[32mTous est parfait, les serveurs se lancent !\e[0m"
#- - - - - - - - - - - - - - - - - - - - - - - - - - - RESTART - - - - - - - - - - - - - - - - - - - - - - - - - -
elif [ "$1" = "$RESTART" ]; then
	if [ -n "$2" ]; then
		if [ "$2" = "all" ]; then
			serv="$ALL"
		else
			serv="${*:2}"
		fi
		for element in $serv
		do
			if [ `screen -ls | grep -w $element | wc -l` -eq "1" ]; then
				if [[ $element == bungee* ]]; then
					serv_vafermerb+=" $element"
				else
					serv_vafermer+=" $element"
				fi
			else
				serv_fermer+=" $element"
			fi
		done
	echo
	if [ -n "$serv_vafermerb" ]; then
		for element in $serv_vafermerb
		do		
			screen -S $element -X stuff 'end$(printf '\r')'
			mc start $element
		done
		echo -e "\e[36m$serv_vafermerb\e[0;36m s'est redémarrer (BUNGEE)\e[0m"
		
	fi
	if [ -n "$serv_vafermer" ]; then
		for element in $serv_vafermer
		do		
			screen -S $element -X stuff 'restart$(printf '\r')'
		done
		echo -e "\e[36m$serv_vafermer\e[0;36m s'est redémarrer \e[0m"
	fi
	if [ -n "$serv_fermer" ]; then
		echo -e "\e[31m$serv_fermer\e[0;31m n'est pas ouvert\e[0m"
	fi
	
	else
		read -p 'Quel(s) serveur(s) ? ' server_select
		$0 $STOP $server_select
	fi
	echo
#- - - - - - - - - - - - - - - - - - - - - - - - - - - MDP - - - - - - - - - - - - - - - - - - - - - - - - - -
elif [ "$1" = "mdp" ]; then
	length=${2:-50}
	tr -dc A-Za-z0-9_ < /dev/urandom | head -c ${length} | xargs
#- - - - - - - - - - - - - - - - - - - - - - - - - - - INSTALL - - - - - - - - - - - - - - - - - - - - - - - - - -
elif [ "$1" = "install" ]; then
	if [ -n $2 ]; then
		if [ -n $3 ]; then
			mkdir $SERVEUR_DIR/$2
			cp $SERVEUR_DIR/.other/install/$3/* $SERVEUR_DIR/$2/
		fi
	fi
#- - - - - - - - - - - - - - - - - - - - - - - - - - - LINK - - - - - - - - - - - - - - - - - - - - - - - - - -
elif [ "$1" = "link" ]; then

	USAGElink="/mc link <server> <mode>"
	
	if [ -d "/home/serveurs/.other/link/$3" ]; then
		mode="$3"
		echo
		read -p "Etes vous de vouloir link le serveur $2 en mode $mode ? (y/n)" yesorno
		if [ $yesorno == 'y' ]; then
			cp -lr $SERVEUR_DIR/.other/link/$mode/* $SERVEUR_DIR/$2/
			cp -lr $SERVEUR_DIR/.other/link/common/* $SERVEUR_DIR/$2/
			echo "Le serveur a été link"
		else
			echo "Le serveur n'a pas été link"
	
	fi
	else
		echo $USAGElink
	fi

#- - - - - - - - - - - - - - - - - - - - - - - - - - - SEE - - - - - - - - - - - - - - - - - - - - - - - - - -
elif [ "$1" = "$SEE" ]; then
	if [ -n "$2" ]; then
		screen -x '${*:2}'
	fi			
#- - - - - - - - - - - - - - - - - - - - - - - - - - - TS - - - - - - - - - - - - - - - - - - - - - - - - - -
elif [ "$1" = "ts" ]; then
	telnet 172.0.0.1 10011
#- - - - - - - - - - - - - - - - - - - - - - - - - - USAGE - - - - - - - - - - - - - - - - - - - - - - - - - -
else
	echo -e "$USAGE"
fi
