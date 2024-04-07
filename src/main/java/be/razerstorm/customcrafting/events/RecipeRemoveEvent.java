package be.razerstorm.customcrafting.events;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Recipe;

public class RecipeRemoveEvent extends Event {
    public static HandlerList handlerList = new HandlerList();
    public final Recipe recipe;
    public final NamespacedKey namespacedKey;

    public RecipeRemoveEvent(NamespacedKey namespacedKey, Recipe recipe) {
        this.namespacedKey = namespacedKey;
        this.recipe = recipe;
    }



    public static HandlerList getHandlerList() {
        return handlerList;
    }


    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
