package com.mcres.boredom;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        super.onEnable();
        getLogger().info("plugin works");
        saveLoc();
    }

    public void saveLoc() {
        Timer timer = new Timer();
        TimerTask minuteTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    FileWriter writer = new FileWriter("location.txt", true);
                    for (Player player: Bukkit.getOnlinePlayers()) {
                        writer.write("Player: " + player.getName() + "; Location: " + player.getLocation().toString());
                        getLogger().info("Player: " + player.getName() + "; Location: " + player.getLocation().toString());
                    }
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        // schedule the task to run starting now and then every hour...
        timer.schedule(minuteTask, 0l, 1000*5);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
