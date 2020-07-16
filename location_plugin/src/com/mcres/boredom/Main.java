package com.mcres.boredom;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends JavaPlugin implements Listener {

    double x, y, z, yaw;
    String item = "", block="", blockState="", worldState="";
    JSONArray location = new JSONArray();
    Player player;
    int numPlayers = 0, sprintFlag = 0;
    String fileName;

    @Override
    public void onEnable() {
        super.onEnable();
        registerEvents();
        getLogger().info("plugin works");
    }


//    public void init() throws IOException {
//        File dir = new File("data/");
//        if (!dir.exists()) {
//            dir.mkdir();
//        }
//        String fileName = "data/" + player.getName() + ".json";
//        FileWriter writer = new FileWriter(fileName, true);
//        JSONObject obj = new JSONObject();
//        JSONArray loc = new JSONArray();
//        obj.put("location", loc);
//        writer.write(obj.toJSONString());
//        writer.close();
//    }

    public void registerEvents() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) throws IOException {
        getLogger().info("Player Joined Update from the Plugin");
        Location spawnLocation = new Location(Bukkit.getWorld("world"), x, y, z);
        event.getPlayer().setBedSpawnLocation(spawnLocation, true);
        player = event.getPlayer();
        numPlayers += 1;
        player = (Player) Bukkit.getOnlinePlayers().toArray()[0];
        init();
        timerData();
    }



    public void init() throws IOException {
        fileName = "data/" + player.getName() + (new Timestamp(System.currentTimeMillis())) + ".json";
        FileWriter writer = new FileWriter(fileName);
        JSONObject ob = new JSONObject();
//        TimeChangeEvent();
        ob.put("events", new JSONArray());
        writer.write(ob.toJSONString());
        writer.close();
    }

    public void TimeChangeEvent() {
        if (day()) {
            worldState = "day";
        } else {
            worldState = "night";
        }
    }

    public void saveData() {
        try {
//            TimeChangeEvent();
            FileReader reader = new FileReader(fileName);
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
            JSONArray loc = (JSONArray) jsonObject.get("events");
            FileWriter writer = new FileWriter(fileName);
            for (Object obj: location) {
                loc.add(obj);
            }
            location.clear();
            jsonObject.put("events", loc);
            writer.write(jsonObject.toJSONString());
            writer.close();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public void timerData() {
        getLogger().info("Saving Data");
        Timer timer = new Timer();
        TimerTask minuteTask = new TimerTask() {
            @Override
            public void run() {
                saveData();
            }
        };
        timer.schedule(minuteTask, 0l, 1000*120);
    }

    public boolean day() {
        Server server = getServer();
        String WorldName = player.getServer().getName();
        long time = server.getWorld(WorldName).getTime();
        return time < 12300 || time > 23850;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        double getX = e.getTo().getBlockX();
        double getY = e.getTo().getBlockY();
        double getZ = e.getTo().getBlockZ();
        double yawL = e.getTo().getYaw();
        double dist = Math.sqrt(Math.pow(x - getX, 2) + Math.pow(y - getY, 2) + Math.pow(z - getZ, 2));
        getLogger().info(String.valueOf(dist));
        if (Math.abs(yawL - yaw) > 45) {
            x = getX;
            y = getY;
            z = getZ;
            yaw = yawL;
            updateObjects("PlayerMoveEvent");
        } else if (dist >= 1) {
            x = getX;
            y = getY;
            z = getZ;
            yaw = yawL;
            updateObjects("PlayerMoveEvent");
        }
    }

    @EventHandler
    public void onPlayerAnimation(final PlayerAnimationEvent event) {
        item = event.getAnimationType().toString();
        updateObjects("PlayerAnimationEvent");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block b = event.getBlock();
        block = b.getBlockData().getAsString();
        blockState = b.getState().toString();
        updateObjects("BlockBreakEvent");
    }

    @EventHandler
    public void checkBlockDamage(final BlockDamageEvent event) {
        blockState = event.getBlock().getState().toString();
        block = event.getBlock().toString();
        updateObjects("BlockDamageEvent");
    }

    public void reinit() {
        item = "";
        block = "";
        blockState = "";
    }

    public void updateObjects(String event) {
        JSONObject coords = new JSONObject();
        JSONArray coordsarr = new JSONArray();
        Timestamp t = new Timestamp(System.currentTimeMillis());
        String time = t.toString();
        coordsarr.add(x);
        coordsarr.add(y);
        coordsarr.add(z);
        coords.put("event", event);
        coords.put("coords", coordsarr);
//        coords.put("time", worldState);
        coords.put("yaw", yaw);
        coords.put("timestamp", time);
        if (event.equals("PlayerAnimationEvent")) {
            coords.put("animation", item);
        }
        if (event.equals("PlayerItemHeldEvent")) {
            coords.put("itemHeld", item);
        }
        if (event.equals("BlockPlaceEvent")) {
            coords.put("block", block);
        }
        if ((event.equals("BlockDamageEvent")) || (event.equals("BlockBreakEvent"))) {
            coords.put("block", block);
            coords.put("blockState", blockState);
        }
        reinit();
        location.add(coords);
    }

    @EventHandler
    public void onMonitorBlockPlace(BlockPlaceEvent event) {
        block = event.getBlock().toString();
        updateObjects("BlockPlaceEvent");
    }

    @EventHandler
    public void onPlayerToggleSprint(final PlayerToggleSprintEvent event) {
        if (event.isSprinting() && sprintFlag == 0) {
            sprintFlag = 1;
            updateObjects("PlayerSprintingEvent");
        }
        if (!event.isSprinting() && sprintFlag == 1) {
            sprintFlag = 0;
            updateObjects("PlayerSlowingEvent");
        }
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        ItemStack stack = event.getPlayer().getItemOnCursor();
        if (stack == null) {
            return;
        }
        item = stack.toString();
        updateObjects("PlayerItemHeldEvent");
    }

    @Override
    public void onDisable() {
        super.onDisable();
        saveData();
    }
}
