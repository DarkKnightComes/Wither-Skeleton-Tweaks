package shadows.wstweaks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.monster.WitherSkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = WitherSkeletonTweaks.MODID)
public class WSTEvents {

	@SubscribeEvent
	public static void witherTransform(LivingSpawnEvent.SpecialSpawn event) {
		if (event.getEntity() instanceof SkeletonEntity) {
			Entity entity = event.getEntity();
			World world = entity.world;
			Random rand = world.rand;
			if (!event.getEntity().world.isRemote) {
				double x = entity.posX;
				double y = entity.posY;
				double z = entity.posZ;
				if (world.dimension.getType() == DimensionType.THE_NETHER || (WSTConfig.INSTANCE.allowAllBiomes.get() && !event.getWorld().func_217337_f(new BlockPos(x, y, z)) && rand.nextInt(WSTConfig.INSTANCE.allBiomesChance.get()) == 0)) {
					event.setCanceled(true);
					entity.remove();
					WitherSkeletonEntity k = EntityType.WITHER_SKELETON.create(world);
					k.setLocationAndAngles(x, y, z, 0, 0);
					world.addEntity(k);
					if (WSTConfig.INSTANCE.giveBows.get()) k.setHeldItem(Hand.MAIN_HAND, new ItemStack(Items.BOW));
				}
			}
		}
	}

	@SubscribeEvent
	public static void handleDropsEvent(LivingDropsEvent event) {
		immolate(event);
		addFrags(event);
		delSwords(event);
	}

	public static void immolate(LivingDropsEvent event) {
		if (!event.getEntity().world.isRemote && (event.getSource() == DamageSource.FIREWORKS || hasSword(event.getSource()))) {
			Collection<ItemEntity> drops = event.getDrops();

			if (event.getEntity().getClass() == WitherSkeletonEntity.class) {
				ItemStack stack = new ItemStack(Items.WITHER_SKELETON_SKULL);
				if (!isStackInList(drops, stack)) {
					event.getDrops().add(newEntity(event.getEntity(), stack));
				}
			} else if (event.getEntity() instanceof AbstractSkeletonEntity) {
				ItemEntity toRemove = null;

				for (ItemEntity i : drops)
					if (i.getItem().getItem() == Items.SKELETON_SKULL) toRemove = i;

				if (toRemove != null) drops.remove(toRemove);

				drops.add(newEntity(event.getEntity(), new ItemStack(Items.WITHER_SKELETON_SKULL)));
			}
		}
	}

	private static boolean hasSword(DamageSource source) {
		Entity s = source.getTrueSource();
		if (s instanceof PlayerEntity) return ((PlayerEntity) s).getHeldItemMainhand().getItem() instanceof ItemImmolationBlade;
		else return false;
	}

	public static void addFrags(LivingDropsEvent event) {
		if (WSTConfig.INSTANCE.shardDropChance.get() <= 0) return;
		if (event.getEntity().world.rand.nextInt(WSTConfig.INSTANCE.shardDropChance.get()) == 0) {
			if (!event.getEntity().world.isRemote && event.getEntity().getClass() == WitherSkeletonEntity.class && !(event.getSource() == DamageSource.FIREWORKS)) {
				Collection<ItemEntity> drops = event.getDrops();
				ItemStack stack = new ItemStack(Items.WITHER_SKELETON_SKULL);
				if (!isStackInList(drops, stack)) {
					drops.add(newEntity(event.getEntity(), new ItemStack(WitherSkeletonTweaks.FRAGMENT)));
				}
			}
		}
	}

	public static void delSwords(LivingDropsEvent event) {
		if (WSTConfig.INSTANCE.delSwords.get() && !event.getEntity().world.isRemote && event.getEntity() instanceof AbstractSkeletonEntity) {

			List<ItemEntity> toRemove = new ArrayList<>();
			for (ItemEntity entity : event.getDrops()) {
				ItemStack stack = entity.getItem();
				if (stack.getItem() == Items.STONE_SWORD || stack.getItem() == Items.BOW) toRemove.add(entity);
			}

			for (ItemEntity i : toRemove)
				event.getDrops().remove(i);
		}
	}

	public static ItemEntity newEntity(Entity e, ItemStack stack) {
		return new ItemEntity(e.world, e.posX, e.posY, e.posZ, stack);
	}

	public static boolean isStackInList(Collection<ItemEntity> list, ItemStack stack) {
		for (ItemEntity i : list) {
			ItemStack iStack = i.getItem();
			if (iStack.isItemEqual(stack)) return true;
		}
		return false;
	}

}
