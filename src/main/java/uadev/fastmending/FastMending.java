package uadev.fastmending;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.view.AnvilView;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class FastMending extends JavaPlugin implements Listener {

    private FileConfiguration config;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, Command command, @NonNull String label, String @NonNull [] args) {
        if (command.getName().equalsIgnoreCase("fmreload")) {
            if (!sender.hasPermission("fastmending.reload")) {
                sender.sendMessage(Objects.requireNonNull(config.getString("messages.no-permission")).replace("&", "§"));
                return true;
            }
            reloadConfig();
            config = getConfig();
            sender.sendMessage(Objects.requireNonNull(config.getString("messages.reload-success")).replace("&", "§"));
            return true;
        }
        return false;
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (!config.getBoolean("enabled")) return;

        AnvilInventory inv = event.getInventory();
        ItemStack left = inv.getItem(0);
        ItemStack right = inv.getItem(1);

        if (left == null || right == null) return;
        if (right.getType() != Material.ENCHANTED_BOOK) return;

        if (!(right.getItemMeta() instanceof EnchantmentStorageMeta bookMeta)) return;
        if (!bookMeta.hasStoredEnchant(Enchantment.MENDING)) return;

        AnvilView anvilView = event.getView();

        if (!(left.getItemMeta() instanceof Damageable dmg) || !dmg.hasDamage()) {
            event.setResult(null);
            return;
        }

        ItemStack repaired = left.clone();
        if (repaired.getItemMeta() instanceof Damageable damageMeta) {
            damageMeta.setDamage(0);
            repaired.setItemMeta(damageMeta);
        }

        event.setResult(repaired);
        anvilView.setRepairCost(config.getInt("repair-cost", 1));
    }
}