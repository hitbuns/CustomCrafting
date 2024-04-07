package be.razerstorm.customcrafting.utils;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class BukkitEventCaller {

    public static boolean callEvent(Event event) {
        Bukkit.getPluginManager().callEvent(event);
        return event instanceof Cancellable && ((Cancellable) event).isCancelled();
    }

}