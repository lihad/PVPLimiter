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
import org.bukkit.entity.Projectile;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class PVPLimiter extends JavaPlugin implements Listener {
	protected static String PLUGIN_NAME = "BeyondPVPLimiter";
	protected static String header = "[" + PLUGIN_NAME + "] ";
	public static PermissionHandler handler;
	private static Logger log = Logger.getLogger("Minecraft");
	
	@Override
	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this, this);
	}
	@EventHandler
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event){

        if(event.getEntity().getWorld().getName().equals("world")) {
            if (event.getEntity() instanceof Player) {
                Player hurt = (Player)event.getEntity();

                if(event.getDamager() instanceof Player || (event.getDamager() instanceof Projectile && ((Projectile)event.getDamager()).getShooter() instanceof Player)) {

                    Player attacker;
                    if(event.getDamager() instanceof Player) attacker = (Player)event.getDamager();
                    else attacker = (Player)((Projectile)event.getDamager()).getShooter();

                    if(attacker.isOp()){
                        // ops can always hurt you.
                        return;
                    }
                    if (handler == null) { setupPermissions(); }
                    if (handler.has(hurt, "beyondpvp.disabledefend") || handler.has(attacker, "beyondpvp.disableattack")) {

                        event.setCancelled(true);
                    }
                }
            }
		}
	}
	// @EventHandler
	// public void onPluginEnable(PluginEnableEvent event){
		// if((event.getPlugin().getDescription().getName().equals("Permissions"))) setupPermissions();
	// }
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
