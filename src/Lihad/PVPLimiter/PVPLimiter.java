package Lihad.PVPLimiter;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class PVPLimiter extends JavaPlugin implements Listener {
	protected static String PLUGIN_NAME = "BeyondTag";
	protected static String header = "[" + PLUGIN_NAME + "] ";
	public static PermissionHandler handler;
	private static Logger log = Logger.getLogger("Minecraft");
	
	@Override
	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this, this);
	}
	@EventHandler
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event){
		if(event.getDamager() instanceof Player && event.getEntity() instanceof Player 
				&& handler.inGroup(((Player)event.getEntity()).getName(), "Moderator")){
			event.setCancelled(true);
		}
	}
	@EventHandler
	public void onPluginEnable(PluginEnableEvent event){
		if((event.getPlugin().getDescription().getName().equals("Permissions"))) setupPermissions();
	}
	public static void setupPermissions() {
		Plugin permissionsPlugin = Bukkit.getServer().getPluginManager().getPlugin("Permissions");
		if (permissionsPlugin != null) {
			info("Succesfully connected to Permissions!");
			handler = ((Permissions) permissionsPlugin).getHandler();
		} else {
			handler = null;
			warning("Disconnected from Permissions...what could possibly go wrong?");
		}
	}
	public static void info(String message){ 
		log.info(header + ChatColor.WHITE + message);
	}
	public static void severe(String message){
		log.severe(header + ChatColor.RED + message);
	}
	public static void warning(String message){
		log.warning(header + ChatColor.YELLOW + message);
	}
	public static void log(java.util.logging.Level level, String message){
		log.log(level, header + message);
	}
}
