package be.razerstorm.customcrafting.managers;

import be.razerstorm.customcrafting.CustomCrafting;
import be.razerstorm.customcrafting.enums.RecipeType;
import be.razerstorm.customcrafting.events.PushRecipeToServerEvent;
import be.razerstorm.customcrafting.events.RecipeRemoveEvent;
import be.razerstorm.customcrafting.objects.RecipeInfo;
import be.razerstorm.customcrafting.utils.BukkitEventCaller;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class RecipeManager {

    private static RecipeManager instance;

    public void loadRecipes() {
        CustomCrafting customCrafting = CustomCrafting.getInstance();
        FileConfiguration config = customCrafting.getConfig();

        long initializeTime = System.currentTimeMillis();
        AtomicInteger recipesLoaded = new AtomicInteger();

        Objects.requireNonNull(config.getConfigurationSection("recipes")).getKeys(false).forEach(recipeName -> {
            customCrafting.getLogger().info("Loading recipe " + recipeName);
            if (config.get("recipes." + recipeName) == null ||
                    config.get("recipes." + recipeName + ".result") == null ||
                    config.get("recipes." + recipeName + ".type") == null) {
                customCrafting.getLogger().warning("Recipe " + recipeName + " is invalid!");
                return;
            }

            RecipeType type = RecipeType.valueOf(config.getString("recipes." + recipeName + ".type"));
            ItemStack output = (ItemStack) config.get("recipes." + recipeName + ".result");

            switch (type) {
                case CRAFTING: {
                    if (config.get("recipes." + recipeName + ".ingredients") == null ||
                            config.get("recipes." + recipeName + ".shape") == null) {
                        customCrafting.getLogger().warning("Recipe " + recipeName + " is invalid!");
                        return;
                    }

                    String[] shape = config.getStringList("recipes." + recipeName + ".shape").toArray(new String[0]);
                    HashMap<Character, ItemStack> ingredients = new HashMap<>();

                    config.getConfigurationSection("recipes." + recipeName + ".ingredients").getKeys(false).forEach(ingredientKey -> {
                        ingredients.put(ingredientKey.charAt(0), (ItemStack) config.get("recipes." + recipeName + ".ingredients." + ingredientKey));
                    });

                    pushToServerRecipes(output, ingredients, new NamespacedKey(customCrafting, recipeName), shape);
                    break;
                }
                case FURNACE: {
                    if (config.get("recipes." + recipeName + ".ingredient") == null ||
                            config.get("recipes." + recipeName + ".experience") == null ||
                            config.get("recipes." + recipeName + ".cookingTime") == null) {
                        customCrafting.getLogger().warning("Recipe " + recipeName + " is invalid!");
                        return;
                    }

                    ItemStack ingredient = (ItemStack) config.get("recipes." + recipeName + ".ingredient");
                    int experience = config.getInt("recipes." + recipeName + ".experience");
                    int cookingTime = config.getInt("recipes." + recipeName + ".cookingTime");

                    pushToServerRecipes(output, ingredient, new NamespacedKey(customCrafting, recipeName), experience, cookingTime);
                    break;
                }
            }
            recipesLoaded.getAndIncrement();
        });

        customCrafting.getLogger().info("Loaded " + recipesLoaded.get() + " recipes in " + (System.currentTimeMillis() - initializeTime) + "ms!");
    }

    public void addRecipe(String recipeName, ItemStack output, HashMap<Character, ItemStack> ingredients, String... shape) {
        CustomCrafting customCrafting = CustomCrafting.getInstance();
        FileConfiguration config = customCrafting.getConfig();

        config.set("recipes." + recipeName + ".type", RecipeType.CRAFTING.name());
        config.set("recipes." + recipeName + ".result", output);
        config.set("recipes." + recipeName + ".shape", shape);

        ingredients.forEach((identifier, ingredient) -> {
            config.set("recipes." + recipeName + ".ingredients." + identifier, ingredient);
        });

        customCrafting.saveConfig();
        CustomCrafting.getInstance().reloadConfig();

        pushToServerRecipes(output, ingredients, new NamespacedKey(customCrafting, recipeName), shape);
    }

    public void addRecipe(String recipeName, ItemStack output, ItemStack ingredient, int experience, int cookingTime) {
        CustomCrafting customCrafting = CustomCrafting.getInstance();
        FileConfiguration config = customCrafting.getConfig();

        config.set("recipes." + recipeName + ".type", RecipeType.FURNACE.name());
        config.set("recipes." + recipeName + ".result", output);
        config.set("recipes." + recipeName + ".ingredient", ingredient);
        config.set("recipes." + recipeName + ".experience", experience);
        config.set("recipes." + recipeName + ".cookingTime", cookingTime);


        customCrafting.saveConfig();
        CustomCrafting.getInstance().reloadConfig();

        pushToServerRecipes(output, ingredient, new NamespacedKey(customCrafting, recipeName), experience, cookingTime);
    }

    public void deleteRecipe(String recipeName) {
        CustomCrafting customCrafting = CustomCrafting.getInstance();
        FileConfiguration config = customCrafting.getConfig();

        config.set("recipes." + recipeName, null);
        customCrafting.saveConfig();
        CustomCrafting.getInstance().reloadConfig();

        NamespacedKey namespacedKey = new NamespacedKey(customCrafting, recipeName);
        Server server = CustomCrafting.getInstance().getServer();
        Recipe recipe = server.getRecipe(namespacedKey);

        if (recipe != null) BukkitEventCaller.callEvent(new RecipeRemoveEvent(namespacedKey,
                recipe));

        server.removeRecipe(namespacedKey);
    }

    public void editRecipe(String recipeName, ItemStack output, HashMap<Character, ItemStack> ingredients, String... shape) {
        CustomCrafting customCrafting = CustomCrafting.getInstance();
        FileConfiguration config = customCrafting.getConfig();

        config.set("recipes." + recipeName, null);

        config.set("recipes." + recipeName + ".type", RecipeType.CRAFTING.name());
        config.set("recipes." + recipeName + ".result", output);
        config.set("recipes." + recipeName + ".shape", shape);

        ingredients.forEach((identifier, ingredient) -> {
            config.set("recipes." + recipeName + ".ingredients." + identifier, ingredient);
        });

        customCrafting.saveConfig();
        CustomCrafting.getInstance().reloadConfig();

        NamespacedKey recipeKey = new NamespacedKey(customCrafting, recipeName);

        customCrafting.getServer().removeRecipe(recipeKey);
        pushToServerRecipes(output, ingredients, recipeKey, shape);
    }

    public void editRecipe(String recipeName, ItemStack output, ItemStack ingredient, int experience, int cookingTime) {
        CustomCrafting customCrafting = CustomCrafting.getInstance();
        FileConfiguration config = customCrafting.getConfig();

        config.set("recipes." + recipeName, null);

        config.set("recipes." + recipeName + ".type", RecipeType.FURNACE.name());
        config.set("recipes." + recipeName + ".result", output);
        config.set("recipes." + recipeName + ".ingredient", ingredient);
        config.set("recipes." + recipeName + ".experience", experience);
        config.set("recipes." + recipeName + ".cookingTime", cookingTime);

        customCrafting.saveConfig();
        CustomCrafting.getInstance().reloadConfig();

        NamespacedKey recipeKey = new NamespacedKey(customCrafting, recipeName);

        customCrafting.getServer().removeRecipe(recipeKey);
        pushToServerRecipes(output, ingredient, new NamespacedKey(customCrafting, recipeName), experience, cookingTime);
    }

    public int getExperience(String recipeName) {
        CustomCrafting customCrafting = CustomCrafting.getInstance();
        FileConfiguration config = customCrafting.getConfig();

        return config.getInt("recipes." + recipeName + ".experience");
    }

    public int getCookingTime(String recipeName) {
        CustomCrafting customCrafting = CustomCrafting.getInstance();
        FileConfiguration config = customCrafting.getConfig();

        return config.getInt("recipes." + recipeName + ".cookingTime");
    }

    public ItemStack getOutput(String recipeName) {
        CustomCrafting customCrafting = CustomCrafting.getInstance();
        FileConfiguration config = customCrafting.getConfig();

        return (ItemStack) config.get("recipes." + recipeName + ".result");
    }

    public ItemStack getIngredient(String recipeName) {
        CustomCrafting customCrafting = CustomCrafting.getInstance();
        FileConfiguration config = customCrafting.getConfig();

        return (ItemStack) config.get("recipes." + recipeName + ".ingredient");
    }

    public void pushToServerRecipes(ItemStack output, HashMap<Character, ItemStack> ingredients, NamespacedKey recipeKey, String... shape) {
        CustomCrafting customCrafting = CustomCrafting.getInstance();
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, output);
        recipe.shape(shape);

        ingredients.forEach((identifier, ingredient) -> {
            recipe.setIngredient(identifier, new RecipeChoice.ExactChoice(ingredient));
        });

        customCrafting.getServer().addRecipe(recipe);

        BukkitEventCaller.callEvent(new PushRecipeToServerEvent(recipeKey,recipe));

    }

    public void pushToServerRecipes(ItemStack output, ItemStack ingredient, NamespacedKey recipeKey, int experience, int cookingTime) {
        CustomCrafting customCrafting = CustomCrafting.getInstance();

        FurnaceRecipe furnaceRecipe = new FurnaceRecipe(recipeKey,
                output,
                new RecipeChoice.ExactChoice(ingredient),
                experience, cookingTime
        );

        BukkitEventCaller.callEvent(new PushRecipeToServerEvent(recipeKey,furnaceRecipe));

        customCrafting.getServer().addRecipe(furnaceRecipe);
    }

    public ArrayList<String> getRecipes() {
        CustomCrafting customCrafting = CustomCrafting.getInstance();
        FileConfiguration config = customCrafting.getConfig();

        return new ArrayList<>(config.getConfigurationSection("recipes").getKeys(false));
    }

    public RecipeInfo getRecipeInfo(String recipeName) {
        CustomCrafting customCrafting = CustomCrafting.getInstance();
        FileConfiguration config = customCrafting.getConfig();

        ItemStack output = (ItemStack) config.get("recipes." + recipeName + ".result");
        String[] shape = config.getStringList("recipes." + recipeName + ".shape").toArray(new String[0]);
        HashMap<Character, ItemStack> ingredients = new HashMap<>();

        config.getConfigurationSection("recipes." + recipeName + ".ingredients").getKeys(false).forEach(ingredientKey -> {
            ingredients.put(ingredientKey.charAt(0), (ItemStack) config.get("recipes." + recipeName + ".ingredients." + ingredientKey));
        });

        return new RecipeInfo(recipeName, output, ingredients, shape);
    }

    public boolean recipeExists(String recipe) {
        CustomCrafting customCrafting = CustomCrafting.getInstance();
        FileConfiguration config = customCrafting.getConfig();

        return config.get("recipes." + recipe) != null;
    }

    public RecipeType getType(String recipe) {
        CustomCrafting customCrafting = CustomCrafting.getInstance();
        FileConfiguration config = customCrafting.getConfig();

        return RecipeType.valueOf(config.getString("recipes." + recipe + ".type"));
    }

    public static RecipeManager getInstance() {
        if (instance == null) {
            instance = new RecipeManager();
        }
        return instance;
    }
}
