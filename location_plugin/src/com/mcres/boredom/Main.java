package com.mcres.boredom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
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

    double x, y, z;
    JSONArray location = new JSONArray();
    Player player;
    int numPlayers = 0;
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
        ob.put("location", new JSONArray());
        writer.write(ob.toJSONString());
        writer.close();
    }

    public void saveData() {
        try {
            FileReader reader = new FileReader(fileName);
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);
            JSONArray loc = (JSONArray) jsonObject.get("location");
            FileWriter writer = new FileWriter(fileName);
            for (Object obj: location) {
                loc.add(obj);
            }
            location.clear();
            jsonObject.put("location", loc);
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



    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        double getX = e.getTo().getBlockX();
        double getY = e.getTo().getBlockY();
        double getZ = e.getTo().getBlockZ();
        double dist = Math.sqrt(Math.pow(x - getX, 2) + Math.pow(y - getY, 2) + Math.pow(z - getZ, 2));
        getLogger().info(String.valueOf(dist));
        if (dist >= 1) {
            x = getX;
            y = getY;
            z = getZ;
            updateObjects(0);
        }
    }

    public void updateObjects(int block) {
        JSONObject coords = new JSONObject();
        JSONArray coordsarr = new JSONArray();
        coordsarr.add(x);
        coordsarr.add(y);
        coordsarr.add(z);
        coords.put("coords", coordsarr);
        coords.put("timestamp", new Timestamp(System.currentTimeMillis()));
//        coords.put("view_angle", viewVector);
//        if (block == 0) {
//            coords.put("block", null);
//        } else {
//            coords.put("block", blockActivity);
//        }
        location.add(coords);
    }


    @Override
    public void onDisable() {
        super.onDisable();
        saveData();
    }
}
