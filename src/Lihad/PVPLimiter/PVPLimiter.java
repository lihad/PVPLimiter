package Lihad.PVPLimiter;

import java.util.logging.Logger;
import java.util.Date;

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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class PVPLimiter extends JavaPlugin implements Listener {
    protected static String PLUGIN_NAME = "BeyondPVPLimiter";
    protected static String header = "[" + PLUGIN_NAME + "] ";
    public static PermissionHandler handler;
    private static Logger log = Logger.getLogger("Minecraft");

    static final long pvpToggleCooldown = 48 * 3600 * 1000; // 48 hours
    
    java.util.Map<String, Long> pvpDisabledPlayers = new java.util.HashMap<String, Long>();
    java.util.Map<String, Long> pvpEnabledPlayers = new java.util.HashMap<String, Long>();

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        getCommand("pvpenable").setExecutor(this);
        getCommand("pvpdisable").setExecutor(this);
        load();

        getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
            public void run() {
                save(); // every 5 mins
            }
        },1200L, 6000L);

    }
    
    @Override
    public void onDisable() {
        save();
    }
    
    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event){

        if(event.getEntity().getWorld().getName().equals("tekkit")) {
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

                    if (!pvpEnabledPlayers.containsKey(hurt.getName()) && !pvpEnabledPlayers.containsKey(attacker.getName())) {
                        event.setCancelled(true);
                    }

                    if (handler == null) { setupPermissions(); }
                    if (handler.has(hurt, "beyondpvp.disabledefend") || handler.has(attacker, "beyondpvp.disableattack")) {
                        event.setCancelled(true);
                    }
                    
                    if (handler.has(hurt, "beyondpvp.thorns")) {
                        attacker.damage(event.getDamage() * 2);
                    }
                }
            }
        }
    }

    void load() {
        java.io.File configFile = new java.io.File(getDataFolder(), "players.yml");
        YamlConfiguration config = loadInfoFile(configFile);

        pvpDisabledPlayers.clear();
        ConfigurationSection section = config.getConfigurationSection("pvpdisable");
        if (config != null && section != null) {
            java.util.Set<String> keys = section.getKeys(false);
            for (String key : keys) {
                long timestamp = section.getLong(key);
                pvpDisabledPlayers.put(key, timestamp);
            }
        }

        pvpEnabledPlayers.clear();
        section = config.getConfigurationSection("pvpenable");
        if (config != null && section != null) {
            java.util.Set<String> keys = section.getKeys(false);
            for (String key : keys) {
                long timestamp = section.getLong(key);
                pvpEnabledPlayers.put(key, timestamp);
            }
        }
    }

    void save() {
        YamlConfiguration config = new YamlConfiguration();
        ConfigurationSection section = config.createSection("pvpdisable");
        for (java.util.Map.Entry<String, Long> entry : pvpDisabledPlayers.entrySet()) {
            section.set(entry.getKey(), entry.getValue());
        }        
        section = config.createSection("pvpenable");
        for (java.util.Map.Entry<String, Long> entry : pvpEnabledPlayers.entrySet()) {
            section.set(entry.getKey(), entry.getValue());
        }        
        java.io.File configFile = new java.io.File(getDataFolder(), "players.yml");
        try {
            config.save(configFile);
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
        } catch (java.io.IOException e) {
            e.printStackTrace();
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

    public static YamlConfiguration loadInfoFile(java.io.File file) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (java.io.FileNotFoundException e) {
            // Blank config
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } catch (org.bukkit.configuration.InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return config;
    }

    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command cmd, String string, String[] arg) {
    
        if(cmd.getName().equalsIgnoreCase("pvpdisable")) {

            if (arg.length == 0) {
                sender.sendMessage("Disable PVP has a 48 hour cooldown.");
                sender.sendMessage("Type /pvpdisable Confirm to confirm");
                return true;
            }
            if (arg[0].equalsIgnoreCase("confirm") && (sender instanceof Player)) {
                String playerName = ((Player)sender).getName();
                if (pvpEnabledPlayers.containsKey(playerName)) {
                     Date cooldownTime = new Date((long)pvpEnabledPlayers.get(playerName) + pvpToggleCooldown);
                     if ((new Date()).before(cooldownTime)) {
                        sender.sendMessage("You can't disable PvP right now.");
                        return true;
                    }
                }
                sender.sendMessage("PvP is now disabled for you in survival world.");
                pvpDisabledPlayers.put(playerName, (new Date()).getTime());
                pvpEnabledPlayers.remove(playerName);
                return true;
            }
            
            else if (sender.isOp() && arg[0].equalsIgnoreCase("reload")) {
                load();
                sender.sendMessage("players.yml reloaded");
                return true;
            }
        }
        else if(cmd.getName().equalsIgnoreCase("pvpenable")) {

            if (arg.length == 0) {
                sender.sendMessage("Enable PVP has a 48 hour cooldown.");
                sender.sendMessage("Type /pvpenable Confirm to confirm");
                return true;
            }
            if (arg[0].equalsIgnoreCase("confirm") && (sender instanceof Player)) {
                String playerName = ((Player)sender).getName();
                if (pvpDisabledPlayers.containsKey(playerName)) {
                     Date cooldownTime = new Date((long)pvpDisabledPlayers.get(playerName) + pvpToggleCooldown);
                     if ((new Date()).before(cooldownTime)) {
                        sender.sendMessage("You can't enable PvP right now.");
                        return true;
                    }
                }
                sender.sendMessage("PvP is now enabled for you in survival world.");
                pvpEnabledPlayers.put(playerName, (new Date()).getTime());
                pvpDisabledPlayers.remove(playerName);
                return true;
            }
            else if (sender.isOp() && arg[0].equalsIgnoreCase("reload")) {
                load();
                sender.sendMessage("players.yml reloaded");
                return true;
            }
        }
        
        return false;
    }
}
