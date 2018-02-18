package com.civclassic.pvptweaks.tweaks;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import com.civclassic.pvptweaks.PvPTweaks;
import com.civclassic.pvptweaks.Tweak;

public class ProjectileTweaks extends Tweak {
	private boolean tweakPearls;
	private boolean tweakPots;
	private boolean tweakAll;

	public ProjectileTweaks(PvPTweaks plugin, ConfigurationSection config) {
		super(plugin, config);
		tweakPearls = false;
		tweakPots = false;
		tweakAll = false;
	}

	@EventHandler(ignoreCancelled=true)
	public void onThrowProjectile(ProjectileLaunchEvent e) {
		if(!(e.getEntity().getShooter() instanceof Player) || (!tweakAll
		  && ((!tweakPearls && e.getEntity() instanceof EnderPearl)
		  || (!tweakPots && e.getEntity() instanceof ThrownPotion)))){
			return;
		}
		Player p = (Player) e.getEntity().getShooter();
		e.getEntity().setVelocity(e.getEntity().getVelocity().subtract(p.getVelocity()));
	}

	@Override
	public void loadConfig(ConfigurationSection config) {
		tweakPearls = config.getBoolean("tweakPearls");
		tweakPots = config.getBoolean("tweakPots");
		tweakAll = config.getBoolean("tweakAll");
	}

	@Override
	public String status() {
		StringBuilder status = new StringBuilder();
		status.append("  Enabled projectiles: \n");
		if(tweakPearls){
			status.append("    ").append("Pearls").append("\n");
		}
		if(tweakPots){
			status.append("    ").append("Potions").append("\n");
		}
		if(tweakAll){
			status.append("    ").append("All").append("\n");
		}
		return status.toString();
	}
}
