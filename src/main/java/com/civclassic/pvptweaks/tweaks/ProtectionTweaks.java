package com.civclassic.pvptweaks.tweaks;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.civclassic.pvptweaks.PvPTweaks;
import com.civclassic.pvptweaks.Tweak;

public class ProtectionTweaks extends Tweak {

	private int armorMitigation;
	private int protMitigation;
	private double protScale;
	private boolean linearProt;
	
	public ProtectionTweaks(PvPTweaks plugin, ConfigurationSection config) {
		super(plugin, config);
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		double damage = event.getDamage();
		if(damage <= 0.0000001D) return;
		
		DamageCause cause = event.getCause();
		if(!isCommonDamage(cause)) return;
		
		boolean factorProt = cause.equals(DamageCause.ENTITY_ATTACK) || cause.equals(DamageCause.PROJECTILE);
		
		Entity entity = event.getEntity();
		if(!(entity instanceof Player)) return;
		
		Player defender = (Player) entity;
		int defense = getDefense(defender);
		double epf = getAverageEPF(defender);
		double linearepf = getAverageLinearEPF(defender);
		
		double vanillaReduction = defense * 0.04;
		double vanillaProtReduction = 0;
		if(factorProt) {
			vanillaProtReduction = epf * 0.04;
		}
		double vanillaDamageRatio = (1 - vanillaReduction) * (1 - vanillaProtReduction);
		
		double originalDamage = damage / vanillaDamageRatio;
		
		double reduction = defense / (defense + armorMitigation);
		double protReduction = 0;
		if(factorProt) {
			protReduction = linearepf / (linearepf + protMitigation * protScale);
		}
		double damageRatio = (1 - reduction) * (1 - protReduction);
		
		double newDamage = originalDamage * damageRatio;
		
		event.setDamage(newDamage);
	}

	@Override
	public void loadConfig(ConfigurationSection config) {
		armorMitigation = config.getInt("armor_mitigation", 10);
		protMitigation = config.getInt("prot_mitigation", 7);
		protScale = config.getDouble("prot_scale", 0.5);
		linearProt = config.getBoolean("linear_prot", true);
	}

	@Override
	protected String status() {
		StringBuilder status = new StringBuilder();
		status.append("  armor mitigation: ").append(armorMitigation);
		status.append("  prot mitigation: ").append(protMitigation);
		status.append("  prot scale: ").append(protScale);
		status.append("  linear prot: ").append(linearProt);
		return null;
	}
	
	private boolean isCommonDamage(DamageCause cause) {
		return cause.equals(DamageCause.ENTITY_ATTACK) ||
				cause.equals(DamageCause.PROJECTILE) ||
				cause.equals(DamageCause.FIRE) ||
				cause.equals(DamageCause.LAVA) ||
				cause.equals(DamageCause.CONTACT) ||
				cause.equals(DamageCause.ENTITY_EXPLOSION) ||
				cause.equals(DamageCause.LIGHTNING) ||
				cause.equals(DamageCause.BLOCK_EXPLOSION);
	}
	
	private int getDefense(Player player) {
		PlayerInventory inv = player.getInventory();
		ItemStack boots = inv.getBoots();
		ItemStack helm = inv.getHelmet();
		ItemStack chest = inv.getChestplate();
		ItemStack pants = inv.getLeggings();
		int def = 0;
		if(helm != null) {
			switch(helm.getType()) {
			case LEATHER_HELMET: def += 1; break;
			case GOLD_HELMET: 
			case CHAINMAIL_HELMET:
			case IRON_HELMET: def += 2; break;
			case DIAMOND_HELMET: def += 3; break;
			default: break;
			}
		}
		if(boots != null) {
			switch(boots.getType()) {
			case LEATHER_BOOTS:
			case GOLD_BOOTS: 
			case CHAINMAIL_BOOTS: def += 1; break;
			case IRON_BOOTS: def += 2; break;
			case DIAMOND_BOOTS: def += 3; break;
			default: break;
			}
		}
		if(pants != null) {
			switch(pants.getType()) {
			case LEATHER_LEGGINGS: def += 2; break;
			case GOLD_LEGGINGS: def += 3; break;
			case IRON_LEGGINGS: def += 4; break;
			case CHAINMAIL_LEGGINGS: def += 5; break;
			case DIAMOND_LEGGINGS: def += 6; break;
			default: break;
			}
		}
		if(chest != null) {
			switch(chest.getType()) {
			case LEATHER_CHESTPLATE: def += 3; break;
			case GOLD_CHESTPLATE: 
			case IRON_CHESTPLATE: def += 5; break;
			case CHAINMAIL_CHESTPLATE: def += 6; break;
			case DIAMOND_CHESTPLATE: def += 8; break;
			default: break;
			}
		}
		return def;
	}
	
	//EPF is Environmental Protection Factor
	private double getAverageEPF(Player player) {
		PlayerInventory inv = player.getInventory();
		
		int epf = 0;
		for(ItemStack armor : inv.getArmorContents()) {
			if(armor == null){
				continue;
			}
			int level = armor.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
			if(level == 4) level = 5;
			epf += level;
		}
		return epf*0.75;
	}
	
	private double getAverageLinearEPF(Player player) {
		if(!linearProt) return getAverageEPF(player);
		PlayerInventory inv = player.getInventory();
		
		int epf = 0;
		for(ItemStack armor : inv.getArmorContents()) {
			epf += armor.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL) * 1.25;
		}
		return epf*0.75;
	}
}
