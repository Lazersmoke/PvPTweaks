package com.civclassic.pvptweaks.tweaks;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.civclassic.pvptweaks.PvPTweaks;
import com.civclassic.pvptweaks.Tweak;
import com.civclassic.pvptweaks.util.TickCooldownHandler;

import net.md_5.bungee.api.ChatColor;

public class PearlTweaks extends Tweak {
	
	private TickCooldownHandler<UUID> cds;
	private List<PotionEffect> effectsOnPearl;
	private double percentHealthOnPearl;
	private boolean refundPearl;
	
	private Map<UUID, Long> lastTeleport;

	public PearlTweaks(PvPTweaks plugin, ConfigurationSection config) {
		super(plugin, config);
		lastTeleport = new HashMap<UUID, Long>();
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getEntity() instanceof Player && event.getCause() == DamageCause.FALL) {
			Player player = (Player) event.getEntity();
			if(lastTeleport.containsKey(player.getUniqueId()) && System.currentTimeMillis() - lastTeleport.get(player.getUniqueId()) < 5) {
				event.setCancelled(true);
				double hp = player.getHealth() * percentHealthOnPearl;
				player.damage(hp);
				player.addPotionEffects(effectsOnPearl);
			}
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if(!event.isCancelled() && event.getCause() != TeleportCause.ENDER_PEARL) return;
		lastTeleport.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
	}
	
	@EventHandler
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		if(event.getEntity() instanceof EnderPearl) return;
		if(!(event.getEntity().getShooter() instanceof Player)) return;
		
		Player shooter = (Player)event.getEntity().getShooter();
		if(cds.onCooldown(shooter.getUniqueId())) {
			long cd = cds.getRemainingCooldown(shooter.getUniqueId());
			event.setCancelled(true);
			DecimalFormat df = new DecimalFormat("#.##");
			shooter.sendMessage(ChatColor.RED + "You may pearl again in "
						+ df.format(((double) cd / 20.0)) + " seconds");
			if(refundPearl) {
				shooter.getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
			}
			return;
		}
	}
	
	@Override
	public void loadConfig(ConfigurationSection config) {
		cds = new TickCooldownHandler<UUID>(config.getLong("cooldown"));
		refundPearl = config.getBoolean("refundPearl");
		if(config.contains("effects")) {
			effectsOnPearl = new ArrayList<PotionEffect>();
			for(String key : config.getConfigurationSection("effects").getKeys(false)) {
				ConfigurationSection effect = config.getConfigurationSection("effects." + key);
				PotionEffectType type = PotionEffectType.getByName(key);
				if(type == null) continue;
				int duration = effect.getInt("duration");
				int strength = effect.getInt("strength");
				PotionEffect potion = new PotionEffect(type, duration, strength);
				effectsOnPearl.add(potion);
			}
		}
		percentHealthOnPearl = config.getDouble("percentHealthOnPearl");
	}

}
