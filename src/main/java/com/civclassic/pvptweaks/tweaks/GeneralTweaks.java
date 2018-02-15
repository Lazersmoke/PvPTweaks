package com.civclassic.pvptweaks.tweaks;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.Bukkit;

import com.civclassic.pvptweaks.PvPTweaks;
import com.civclassic.pvptweaks.Tweak;

public class GeneralTweaks extends Tweak {

	private double healthRegen;
	private float exhaustionCost;
	private long regenFrequency;
	private Integer bukkitTaskId;
	
	public GeneralTweaks(PvPTweaks plugin, ConfigurationSection config) {
		super(plugin, config);
	}

	@Override
	public void loadConfig(ConfigurationSection config) {
		healthRegen = config.getDouble("healthRegen");
		exhaustionCost = (float) config.getDouble("exhaustionCost");
		regenFrequency = config.getLong("regenDelay");
		if(bukkitTaskId != null){
			// Cancel old task
			Bukkit.getScheduler().cancelTask(bukkitTaskId);
		}
		// Disable natural regen completely
		Bukkit.getServer().getWorlds().forEach(w -> w.setGameRuleValue("naturalRegeneration","false"));
		bukkitTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin(), () -> {
			Bukkit.getOnlinePlayers().forEach(p -> {
				// If they are healed with health <= 0, they turn sideways and red
				// and can still walk around.
				if(p.getFoodLevel() >= 18 && p.getHealth() < p.getMaxHealth() && !(p.getHealth() <= 0)){
					p.setHealth(Math.min(p.getMaxHealth(),p.getHealth() + healthRegen));
					p.setExhaustion(p.getExhaustion() + exhaustionCost);
				}
			});
		}, 1L, regenFrequency);
	}

	@Override
	protected String status() {
		StringBuilder status = new StringBuilder();
		status.append("  healthRegen: ").append(healthRegen);
		status.append("  regenDelay: ").append(regenFrequency);
		status.append("  exhaustionCost: ").append(exhaustionCost);
		return status.toString();
	}
}
