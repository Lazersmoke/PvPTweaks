package com.civclassic.pvptweaks.tweaks;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.Bukkit;

import com.civclassic.pvptweaks.PvPTweaks;
import com.civclassic.pvptweaks.Tweak;

public class GeneralTweaks extends Tweak {

	private double healthRegen;
	private float exhaustionCost;
	private Integer bukkitTaskId;
	
	public GeneralTweaks(PvPTweaks plugin, ConfigurationSection config) {
		super(plugin, config);
	}

	@Override
	public void loadConfig(ConfigurationSection config) {
		healthRegen = config.getDouble("healthRegen");
		exhaustionCost = (float) config.getDouble("exhaustionCost");
		long regenFrequency = config.getLong("regenDelay");
		if(bukkitTaskId != null){
			// Cancel old task
			Bukkit.getScheduler().cancelTask(bukkitTaskId);
		}
		bukkitTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin(), new Runnable() {
			@Override
			public void run() {
				Bukkit.getOnlinePlayers().forEach(p -> {
					p.getWorld().setGameRuleValue("naturalRegeneration","false");
					if(p.getFoodLevel() >= 18 && p.getHealth() < p.getMaxHealth()){
						p.setHealth(Math.min(p.getMaxHealth(),p.getHealth() + healthRegen));
						p.setExhaustion(p.getExhaustion() + exhaustionCost);
					}
				});
			}
}, 1L, regenFrequency);
	}

	@Override
	protected String status() {
		StringBuilder status = new StringBuilder();
		status.append("  healthRegen: ").append(healthRegen);
		return status.toString();
	}
}
