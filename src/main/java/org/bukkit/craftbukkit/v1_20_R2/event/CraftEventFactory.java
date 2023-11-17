package org.bukkit.craftbukkit.v1_20_R2.event;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.SpellcasterIllager;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Statistic;
import org.bukkit.Statistic.Type;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.craftbukkit.v1_20_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_20_R2.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_20_R2.CraftLootTable;
import org.bukkit.craftbukkit.v1_20_R2.CraftRaid;
import org.bukkit.craftbukkit.v1_20_R2.CraftServer;
import org.bukkit.craftbukkit.v1_20_R2.CraftStatistic;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R2.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R2.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_20_R2.block.CraftBlockStates;
import org.bukkit.craftbukkit.v1_20_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftRaider;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftSpellcaster;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftInventoryCrafting;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftMetaBook;
import org.bukkit.craftbukkit.v1_20_R2.potion.CraftPotionUtil;
import org.bukkit.craftbukkit.v1_20_R2.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_20_R2.util.CraftNamespacedKey;
import org.bukkit.craftbukkit.v1_20_R2.util.CraftVector;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Spellcaster;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BellResonateEvent;
import org.bukkit.event.block.BellRingEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockShearEntityEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.FluidLevelChangeEvent;
import org.bukkit.event.block.MoistureChangeEvent;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.event.block.TNTPrimeEvent.PrimeCause;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.ArrowBodyCountChangeEvent;
import org.bukkit.event.entity.BatToggleSleepEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.CreeperPowerEvent;
import org.bukkit.event.entity.CreeperPowerEvent.PowerCause;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEnterLoveModeEvent;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.event.entity.EntityExhaustionEvent.ExhaustionReason;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntitySpellCastEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.EntityTransformEvent.TransformReason;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.HorseJumpEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.entity.StriderTemperatureChangeEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent.ChangeReason;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEntityEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerBucketFishEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerExpCooldownChangeEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerRecipeBookClickEvent;
import org.bukkit.event.player.PlayerRecipeDiscoverEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerSignOpenEvent;
import org.bukkit.event.player.PlayerSignOpenEvent.Cause;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.event.raid.RaidSpawnWaveEvent;
import org.bukkit.event.raid.RaidStopEvent;
import org.bukkit.event.raid.RaidStopEvent.Reason;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

public class CraftEventFactory {

    public static Block blockDamage;
    public static Entity entityDamage;
    public static BlockPos sourceBlockOverride = null;
    private static final Function ZERO = Functions.constant(-0.0D);
    private static volatile int[] $SWITCH_TABLE$org$bukkit$event$block$Action;
    private static volatile int[] $SWITCH_TABLE$org$bukkit$event$entity$CreatureSpawnEvent$SpawnReason;
    private static volatile int[] $SWITCH_TABLE$org$bukkit$Material;
    private static volatile int[] $SWITCH_TABLE$org$bukkit$entity$EntityType;
    private static volatile int[] $SWITCH_TABLE$org$bukkit$Statistic;

    private static boolean canBuild(ServerLevel world, Player player, int x, int z) {
        int spawnSize = Bukkit.getServer().getSpawnRadius();

        if (world.dimension() != Level.OVERWORLD) {
            return true;
        } else if (spawnSize <= 0) {
            return true;
        } else if (((CraftServer) Bukkit.getServer()).getHandle().getOps().isEmpty()) {
            return true;
        } else if (player.isOp()) {
            return true;
        } else {
            BlockPos chunkcoordinates = world.getSharedSpawnPos();
            int distanceFromSpawn = Math.max(Math.abs(x - chunkcoordinates.getX()), Math.abs(z - chunkcoordinates.getZ()));

            return distanceFromSpawn > spawnSize;
        }
    }

    public static Event callEvent(Event event) {
        Bukkit.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static boolean callPlayerSignOpenEvent(net.minecraft.world.entity.player.Player player, SignBlockEntity tileEntitySign, boolean front, Cause cause) {
        CraftBlock block = CraftBlock.at(tileEntitySign.getLevel(), tileEntitySign.getBlockPos());
        Sign sign = (Sign) CraftBlockStates.getBlockState(block);
        Side side = front ? Side.FRONT : Side.BACK;

        return callPlayerSignOpenEvent((Player) player.getBukkitEntity(), sign, side, cause);
    }

    public static boolean callPlayerSignOpenEvent(Player player, Sign sign, Side side, Cause cause) {
        PlayerSignOpenEvent event = new PlayerSignOpenEvent(player, sign, side, cause);

        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static Either callPlayerBedEnterEvent(net.minecraft.world.entity.player.Player player, BlockPos bed, Either nmsBedResult) {
        BedEnterResult bedEnterResult = (BedEnterResult) nmsBedResult.mapBoth(new Function() {
            private static volatile int[] $SWITCH_TABLE$net$minecraft$world$entity$player$EntityHuman$EnumBedResult;

            public BedEnterResult apply(net.minecraft.world.entity.player.Player.BedSleepingProblem t) {
                switch ($SWITCH_TABLE$net$minecraft$world$entity$player$EntityHuman$EnumBedResult()[t.ordinal()]) {
                    case 1:
                        return BedEnterResult.NOT_POSSIBLE_HERE;
                    case 2:
                        return BedEnterResult.NOT_POSSIBLE_NOW;
                    case 3:
                        return BedEnterResult.TOO_FAR_AWAY;
                    case 4:
                    case 5:
                    default:
                        return BedEnterResult.OTHER_PROBLEM;
                    case 6:
                        return BedEnterResult.NOT_SAFE;
                }
            }

            static int[] $SWITCH_TABLE$net$minecraft$world$entity$player$EntityHuman$EnumBedResult() {
                int[] aint = null.$SWITCH_TABLE$net$minecraft$world$entity$player$EntityHuman$EnumBedResult;

                if (aint != null) {
                    return aint;
                } else {
                    int[] aint1 = new int[net.minecraft.world.entity.player.Player.BedSleepingProblem.values().length];

                    try {
                        aint1[net.minecraft.world.entity.player.Player.BedSleepingProblem.NOT_POSSIBLE_HERE.ordinal()] = 1;
                    } catch (NoSuchFieldError nosuchfielderror) {
                        ;
                    }

                    try {
                        aint1[net.minecraft.world.entity.player.Player.BedSleepingProblem.NOT_POSSIBLE_NOW.ordinal()] = 2;
                    } catch (NoSuchFieldError nosuchfielderror1) {
                        ;
                    }

                    try {
                        aint1[net.minecraft.world.entity.player.Player.BedSleepingProblem.NOT_SAFE.ordinal()] = 6;
                    } catch (NoSuchFieldError nosuchfielderror2) {
                        ;
                    }

                    try {
                        aint1[net.minecraft.world.entity.player.Player.BedSleepingProblem.OBSTRUCTED.ordinal()] = 4;
                    } catch (NoSuchFieldError nosuchfielderror3) {
                        ;
                    }

                    try {
                        aint1[net.minecraft.world.entity.player.Player.BedSleepingProblem.OTHER_PROBLEM.ordinal()] = 5;
                    } catch (NoSuchFieldError nosuchfielderror4) {
                        ;
                    }

                    try {
                        aint1[net.minecraft.world.entity.player.Player.BedSleepingProblem.TOO_FAR_AWAY.ordinal()] = 3;
                    } catch (NoSuchFieldError nosuchfielderror5) {
                        ;
                    }

                    null.$SWITCH_TABLE$net$minecraft$world$entity$player$EntityHuman$EnumBedResult = aint1;
                    return aint1;
                }
            }
        }, (tx) -> {
            return BedEnterResult.OK;
        }).map(java.util.function.Function.identity(), java.util.function.Function.identity());
        PlayerBedEnterEvent event = new PlayerBedEnterEvent((Player) player.getBukkitEntity(), CraftBlock.at(player.level(), bed), bedEnterResult);

        Bukkit.getServer().getPluginManager().callEvent(event);
        Result result = event.useBed();

        return result == Result.ALLOW ? Either.right(Unit.INSTANCE) : (result == Result.DENY ? Either.left(net.minecraft.world.entity.player.Player.BedSleepingProblem.OTHER_PROBLEM) : nmsBedResult);
    }

    public static EntityEnterLoveModeEvent callEntityEnterLoveModeEvent(net.minecraft.world.entity.player.Player entityHuman, Animal entityAnimal, int loveTicks) {
        EntityEnterLoveModeEvent entityEnterLoveModeEvent = new EntityEnterLoveModeEvent((Animals) entityAnimal.getBukkitEntity(), entityHuman != null ? entityHuman.getBukkitEntity() : null, loveTicks);

        Bukkit.getPluginManager().callEvent(entityEnterLoveModeEvent);
        return entityEnterLoveModeEvent;
    }

    public static PlayerHarvestBlockEvent callPlayerHarvestBlockEvent(Level world, BlockPos blockposition, net.minecraft.world.entity.player.Player who, InteractionHand enumhand, List itemsToHarvest) {
        ArrayList bukkitItemsToHarvest = new ArrayList((Collection) itemsToHarvest.stream().map(CraftItemStack::asBukkitCopy).collect(Collectors.toList()));
        Player player = (Player) who.getBukkitEntity();
        PlayerHarvestBlockEvent playerHarvestBlockEvent = new PlayerHarvestBlockEvent(player, CraftBlock.at(world, blockposition), CraftEquipmentSlot.getHand(enumhand), bukkitItemsToHarvest);

        Bukkit.getPluginManager().callEvent(playerHarvestBlockEvent);
        return playerHarvestBlockEvent;
    }

    public static PlayerBucketEntityEvent callPlayerFishBucketEvent(LivingEntity fish, net.minecraft.world.entity.player.Player entityHuman, ItemStack originalBucket, ItemStack entityBucket, InteractionHand enumhand) {
        Player player = (Player) entityHuman.getBukkitEntity();
        EquipmentSlot hand = CraftEquipmentSlot.getHand(enumhand);
        Object event;

        if (fish instanceof AbstractFish) {
            event = new PlayerBucketFishEvent(player, (Fish) fish.getBukkitEntity(), CraftItemStack.asBukkitCopy(originalBucket), CraftItemStack.asBukkitCopy(entityBucket), hand);
        } else {
            event = new PlayerBucketEntityEvent(player, fish.getBukkitEntity(), CraftItemStack.asBukkitCopy(originalBucket), CraftItemStack.asBukkitCopy(entityBucket), hand);
        }

        Bukkit.getPluginManager().callEvent((Event) event);
        return (PlayerBucketEntityEvent) event;
    }

    public static TradeSelectEvent callTradeSelectEvent(ServerPlayer player, int newIndex, MerchantMenu merchant) {
        TradeSelectEvent tradeSelectEvent = new TradeSelectEvent(merchant.getBukkitView(), newIndex);

        Bukkit.getPluginManager().callEvent(tradeSelectEvent);
        return tradeSelectEvent;
    }

    public static boolean handleBellRingEvent(Level world, BlockPos position, Direction direction, Entity entity) {
        CraftBlock block = CraftBlock.at(world, position);
        BlockFace bukkitDirection = CraftBlock.notchToBlockFace(direction);
        BellRingEvent event = new BellRingEvent(block, bukkitDirection, entity != null ? entity.getBukkitEntity() : null);

        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static Stream handleBellResonateEvent(Level world, BlockPos position, List bukkitEntities) {
        CraftBlock block = CraftBlock.at(world, position);
        BellResonateEvent event = new BellResonateEvent(block, bukkitEntities);

        Bukkit.getPluginManager().callEvent(event);
        return event.getResonatedEntities().stream().map((bukkitEntityx) -> {
            return ((CraftLivingEntity) bukkitEntityx).getHandle();
        });
    }

    public static BlockMultiPlaceEvent callBlockMultiPlaceEvent(ServerLevel world, net.minecraft.world.entity.player.Player who, InteractionHand hand, List blockStates, int clickedX, int clickedY, int clickedZ) {
        CraftWorld craftWorld = world.getWorld();
        CraftServer craftServer = world.getCraftServer();
        Player player = (Player) who.getBukkitEntity();
        Block blockClicked = craftWorld.getBlockAt(clickedX, clickedY, clickedZ);
        boolean canBuild = true;

        for (int i = 0; i < blockStates.size(); ++i) {
            if (!canBuild(world, player, ((BlockState) blockStates.get(i)).getX(), ((BlockState) blockStates.get(i)).getZ())) {
                canBuild = false;
                break;
            }
        }

        org.bukkit.inventory.ItemStack item;

        if (hand == InteractionHand.MAIN_HAND) {
            item = player.getInventory().getItemInMainHand();
        } else {
            item = player.getInventory().getItemInOffHand();
        }

        BlockMultiPlaceEvent event = new BlockMultiPlaceEvent(blockStates, blockClicked, item, player, canBuild);

        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    public static BlockPlaceEvent callBlockPlaceEvent(ServerLevel world, net.minecraft.world.entity.player.Player who, InteractionHand hand, BlockState replacedBlockState, int clickedX, int clickedY, int clickedZ) {
        CraftWorld craftWorld = world.getWorld();
        CraftServer craftServer = world.getCraftServer();
        Player player = (Player) who.getBukkitEntity();
        Block blockClicked = craftWorld.getBlockAt(clickedX, clickedY, clickedZ);
        Block placedBlock = replacedBlockState.getBlock();
        boolean canBuild = canBuild(world, player, placedBlock.getX(), placedBlock.getZ());
        org.bukkit.inventory.ItemStack item;
        EquipmentSlot equipmentSlot;

        if (hand == InteractionHand.MAIN_HAND) {
            item = player.getInventory().getItemInMainHand();
            equipmentSlot = EquipmentSlot.HAND;
        } else {
            item = player.getInventory().getItemInOffHand();
            equipmentSlot = EquipmentSlot.OFF_HAND;
        }

        BlockPlaceEvent event = new BlockPlaceEvent(placedBlock, replacedBlockState, blockClicked, item, player, canBuild, equipmentSlot);

        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    public static void handleBlockDropItemEvent(Block block, BlockState state, ServerPlayer player, List items) {
        BlockDropItemEvent event = new BlockDropItemEvent(block, state, player.getBukkitEntity(), Lists.transform(items, (itemx) -> {
            return (Item) itemx.getBukkitEntity();
        }));

        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            Iterator iterator = items.iterator();

            while (iterator.hasNext()) {
                ItemEntity item = (ItemEntity) iterator.next();

                item.level().addFreshEntity(item);
            }
        }

    }

    public static EntityPlaceEvent callEntityPlaceEvent(UseOnContext itemactioncontext, Entity entity) {
        return callEntityPlaceEvent(itemactioncontext.getLevel(), itemactioncontext.getClickedPos(), itemactioncontext.getClickedFace(), itemactioncontext.getPlayer(), entity, itemactioncontext.getHand());
    }

    public static EntityPlaceEvent callEntityPlaceEvent(Level world, BlockPos clickPosition, Direction clickedFace, net.minecraft.world.entity.player.Player human, Entity entity, InteractionHand enumhand) {
        Player who = human == null ? null : (Player) human.getBukkitEntity();
        CraftBlock blockClicked = CraftBlock.at(world, clickPosition);
        BlockFace blockFace = CraftBlock.notchToBlockFace(clickedFace);
        EntityPlaceEvent event = new EntityPlaceEvent(entity.getBukkitEntity(), who, blockClicked, blockFace, CraftEquipmentSlot.getHand(enumhand));

        entity.level().getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerBucketEmptyEvent callPlayerBucketEmptyEvent(ServerLevel world, net.minecraft.world.entity.player.Player who, BlockPos changed, BlockPos clicked, Direction clickedFace, ItemStack itemInHand, InteractionHand enumhand) {
        return (PlayerBucketEmptyEvent) getPlayerBucketEvent(false, world, who, changed, clicked, clickedFace, itemInHand, Items.BUCKET, enumhand);
    }

    public static PlayerBucketFillEvent callPlayerBucketFillEvent(ServerLevel world, net.minecraft.world.entity.player.Player who, BlockPos changed, BlockPos clicked, Direction clickedFace, ItemStack itemInHand, net.minecraft.world.item.Item bucket, InteractionHand enumhand) {
        return (PlayerBucketFillEvent) getPlayerBucketEvent(true, world, who, clicked, changed, clickedFace, itemInHand, bucket, enumhand);
    }

    private static PlayerEvent getPlayerBucketEvent(boolean isFilling, ServerLevel world, net.minecraft.world.entity.player.Player who, BlockPos changed, BlockPos clicked, Direction clickedFace, ItemStack itemstack, net.minecraft.world.item.Item item, InteractionHand enumhand) {
        Player player = (Player) who.getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asNewCraftStack(item);
        Material bucket = CraftMagicNumbers.getMaterial(itemstack.getItem());
        CraftServer craftServer = (CraftServer) player.getServer();
        CraftBlock block = CraftBlock.at(world, changed);
        CraftBlock blockClicked = CraftBlock.at(world, clicked);
        BlockFace blockFace = CraftBlock.notchToBlockFace(clickedFace);
        EquipmentSlot hand = CraftEquipmentSlot.getHand(enumhand);
        Object event;

        if (isFilling) {
            event = new PlayerBucketFillEvent(player, block, blockClicked, blockFace, bucket, itemInHand, hand);
            ((PlayerBucketFillEvent) event).setCancelled(!canBuild(world, player, changed.getX(), changed.getZ()));
        } else {
            event = new PlayerBucketEmptyEvent(player, block, blockClicked, blockFace, bucket, itemInHand, hand);
            ((PlayerBucketEmptyEvent) event).setCancelled(!canBuild(world, player, changed.getX(), changed.getZ()));
        }

        craftServer.getPluginManager().callEvent((Event) event);
        return (PlayerEvent) event;
    }

    public static PlayerInteractEvent callPlayerInteractEvent(net.minecraft.world.entity.player.Player who, Action action, ItemStack itemstack, InteractionHand hand) {
        if (action != Action.LEFT_CLICK_AIR && action != Action.RIGHT_CLICK_AIR) {
            throw new AssertionError(String.format("%s performing %s with %s", who, action, itemstack));
        } else {
            return callPlayerInteractEvent(who, action, (BlockPos) null, Direction.SOUTH, itemstack, hand);
        }
    }

    public static PlayerInteractEvent callPlayerInteractEvent(net.minecraft.world.entity.player.Player who, Action action, BlockPos position, Direction direction, ItemStack itemstack, InteractionHand hand) {
        return callPlayerInteractEvent(who, action, position, direction, itemstack, false, hand, (Vec3) null);
    }

    public static PlayerInteractEvent callPlayerInteractEvent(net.minecraft.world.entity.player.Player who, Action action, BlockPos position, Direction direction, ItemStack itemstack, boolean cancelledBlock, InteractionHand hand, Vec3 targetPos) {
        Player player = who == null ? null : (Player) who.getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asCraftMirror(itemstack);
        Vector clickedPos = null;

        if (position != null && targetPos != null) {
            clickedPos = CraftVector.toBukkit(targetPos.subtract(Vec3.atLowerCornerOf(position)));
        }

        CraftWorld craftWorld = (CraftWorld) player.getWorld();
        CraftServer craftServer = (CraftServer) player.getServer();
        Block blockClicked = null;

        if (position != null) {
            blockClicked = craftWorld.getBlockAt(position.getX(), position.getY(), position.getZ());
        } else {
            switch ($SWITCH_TABLE$org$bukkit$event$block$Action()[action.ordinal()]) {
                case 1:
                    action = Action.LEFT_CLICK_AIR;
                    break;
                case 2:
                    action = Action.RIGHT_CLICK_AIR;
            }
        }

        BlockFace blockFace = CraftBlock.notchToBlockFace(direction);

        if (itemInHand.getType() == Material.AIR || itemInHand.getAmount() == 0) {
            itemInHand = null;
        }

        PlayerInteractEvent event = new PlayerInteractEvent(player, action, itemInHand, blockClicked, blockFace, hand == null ? null : (hand == InteractionHand.OFF_HAND ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND), clickedPos);

        if (cancelledBlock) {
            event.setUseInteractedBlock(Result.DENY);
        }

        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    public static EntityTransformEvent callEntityTransformEvent(LivingEntity original, LivingEntity coverted, TransformReason transformReason) {
        return callEntityTransformEvent(original, Collections.singletonList(coverted), transformReason);
    }

    public static EntityTransformEvent callEntityTransformEvent(LivingEntity original, List convertedList, TransformReason convertType) {
        ArrayList list = new ArrayList();
        Iterator iterator = convertedList.iterator();

        while (iterator.hasNext()) {
            LivingEntity entityLiving = (LivingEntity) iterator.next();

            list.add(entityLiving.getBukkitEntity());
        }

        EntityTransformEvent event = new EntityTransformEvent(original.getBukkitEntity(), list, convertType);

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static EntityShootBowEvent callEntityShootBowEvent(LivingEntity who, ItemStack bow, ItemStack consumableItem, Entity entityArrow, InteractionHand hand, float force, boolean consumeItem) {
        org.bukkit.entity.LivingEntity shooter = (org.bukkit.entity.LivingEntity) who.getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asCraftMirror(bow);
        CraftItemStack itemConsumable = CraftItemStack.asCraftMirror(consumableItem);
        CraftEntity arrow = entityArrow.getBukkitEntity();
        EquipmentSlot handSlot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND;

        if (itemInHand != null && (itemInHand.getType() == Material.AIR || itemInHand.getAmount() == 0)) {
            itemInHand = null;
        }

        EntityShootBowEvent event = new EntityShootBowEvent(shooter, itemInHand, itemConsumable, arrow, handSlot, force, consumeItem);

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static VillagerCareerChangeEvent callVillagerCareerChangeEvent(Villager vilager, Profession future, ChangeReason reason) {
        VillagerCareerChangeEvent event = new VillagerCareerChangeEvent((org.bukkit.entity.Villager) vilager.getBukkitEntity(), future, reason);

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static BlockDamageEvent callBlockDamageEvent(ServerPlayer who, BlockPos pos, ItemStack itemstack, boolean instaBreak) {
        CraftPlayer player = who.getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asCraftMirror(itemstack);
        CraftBlock blockClicked = CraftBlock.at(who.level(), pos);
        BlockDamageEvent event = new BlockDamageEvent(player, blockClicked, itemInHand, instaBreak);

        player.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockDamageAbortEvent callBlockDamageAbortEvent(ServerPlayer who, BlockPos pos, ItemStack itemstack) {
        CraftPlayer player = who.getBukkitEntity();
        CraftItemStack itemInHand = CraftItemStack.asCraftMirror(itemstack);
        CraftBlock blockClicked = CraftBlock.at(who.level(), pos);
        BlockDamageAbortEvent event = new BlockDamageAbortEvent(player, blockClicked, itemInHand);

        player.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static boolean doEntityAddEventCalling(Level world, Entity entity, SpawnReason spawnReason) {
        if (entity == null) {
            return false;
        } else {
            Object event = null;

            if (entity instanceof LivingEntity && !(entity instanceof ServerPlayer)) {
                boolean isAnimal = entity instanceof Animal || entity instanceof WaterAnimal || entity instanceof AbstractGolem;
                boolean isMonster = entity instanceof Monster || entity instanceof Ghast || entity instanceof Slime;
                boolean isNpc = entity instanceof Npc;

                if (spawnReason != SpawnReason.CUSTOM && (isAnimal && !world.getWorld().getAllowAnimals() || isMonster && !world.getWorld().getAllowMonsters() || isNpc && !world.getCraftServer().getServer().areNpcsEnabled())) {
                    entity.discard();
                    return false;
                }

                event = callCreatureSpawnEvent((LivingEntity) entity, spawnReason);
            } else if (entity instanceof ItemEntity) {
                event = callItemSpawnEvent((ItemEntity) entity);
            } else if (entity.getBukkitEntity() instanceof Projectile) {
                event = callProjectileLaunchEvent(entity);
            } else if (entity.getBukkitEntity() instanceof Vehicle) {
                event = callVehicleCreateEvent(entity);
            } else if (entity.getBukkitEntity() instanceof LightningStrike) {
                org.bukkit.event.weather.LightningStrikeEvent.Cause cause = org.bukkit.event.weather.LightningStrikeEvent.Cause.UNKNOWN;

                switch ($SWITCH_TABLE$org$bukkit$event$entity$CreatureSpawnEvent$SpawnReason()[spawnReason.ordinal()]) {
                    case 4:
                        cause = org.bukkit.event.weather.LightningStrikeEvent.Cause.SPAWNER;
                        break;
                    case 37:
                        cause = org.bukkit.event.weather.LightningStrikeEvent.Cause.COMMAND;
                        break;
                    case 38:
                        cause = org.bukkit.event.weather.LightningStrikeEvent.Cause.CUSTOM;
                }

                if (cause == org.bukkit.event.weather.LightningStrikeEvent.Cause.UNKNOWN && spawnReason == SpawnReason.DEFAULT) {
                    return true;
                }

                event = callLightningStrikeEvent((LightningStrike) entity.getBukkitEntity(), cause);
            } else if (entity instanceof ExperienceOrb) {
                ExperienceOrb xp = (ExperienceOrb) entity;
                double radius = world.spigotConfig.expMerge;

                if (radius > 0.0D) {
                    List entities = world.getEntities(entity, entity.getBoundingBox().inflate(radius, radius, radius));
                    Iterator iterator = entities.iterator();

                    while (iterator.hasNext()) {
                        Entity e = (Entity) iterator.next();

                        if (e instanceof ExperienceOrb) {
                            ExperienceOrb loopItem = (ExperienceOrb) e;

                            if (!loopItem.isRemoved()) {
                                xp.value += loopItem.value;
                                loopItem.discard();
                            }
                        }
                    }
                }
            } else if (!(entity instanceof ServerPlayer)) {
                event = callEntitySpawnEvent(entity);
            }

            if (event != null && (((Cancellable) event).isCancelled() || entity.isRemoved())) {
                Entity vehicle = entity.getVehicle();

                if (vehicle != null) {
                    vehicle.discard();
                }

                Iterator iterator1 = entity.getIndirectPassengers().iterator();

                while (iterator1.hasNext()) {
                    Entity passenger = (Entity) iterator1.next();

                    passenger.discard();
                }

                entity.discard();
                return false;
            } else {
                return true;
            }
        }
    }

    public static EntitySpawnEvent callEntitySpawnEvent(Entity entity) {
        CraftEntity bukkitEntity = entity.getBukkitEntity();
        EntitySpawnEvent event = new EntitySpawnEvent(bukkitEntity);

        bukkitEntity.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static CreatureSpawnEvent callCreatureSpawnEvent(LivingEntity entityliving, SpawnReason spawnReason) {
        org.bukkit.entity.LivingEntity entity = (org.bukkit.entity.LivingEntity) entityliving.getBukkitEntity();
        CraftServer craftServer = (CraftServer) entity.getServer();
        CreatureSpawnEvent event = new CreatureSpawnEvent(entity, spawnReason);

        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    public static EntityTameEvent callEntityTameEvent(Mob entity, net.minecraft.world.entity.player.Player tamer) {
        CraftEntity bukkitEntity = entity.getBukkitEntity();
        CraftHumanEntity bukkitTamer = tamer != null ? tamer.getBukkitEntity() : null;
        CraftServer craftServer = (CraftServer) bukkitEntity.getServer();
        EntityTameEvent event = new EntityTameEvent((org.bukkit.entity.LivingEntity) bukkitEntity, bukkitTamer);

        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    public static ItemSpawnEvent callItemSpawnEvent(ItemEntity entityitem) {
        Item entity = (Item) entityitem.getBukkitEntity();
        CraftServer craftServer = (CraftServer) entity.getServer();
        ItemSpawnEvent event = new ItemSpawnEvent(entity);

        craftServer.getPluginManager().callEvent(event);
        return event;
    }

    public static ItemDespawnEvent callItemDespawnEvent(ItemEntity entityitem) {
        Item entity = (Item) entityitem.getBukkitEntity();
        ItemDespawnEvent event = new ItemDespawnEvent(entity, entity.getLocation());

        entity.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static boolean callItemMergeEvent(ItemEntity merging, ItemEntity mergingWith) {
        Item entityMerging = (Item) merging.getBukkitEntity();
        Item entityMergingWith = (Item) mergingWith.getBukkitEntity();
        ItemMergeEvent event = new ItemMergeEvent(entityMerging, entityMergingWith);

        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static PotionSplashEvent callPotionSplashEvent(ThrownPotion potion, HitResult position, Map affectedEntities) {
        org.bukkit.entity.ThrownPotion thrownPotion = (org.bukkit.entity.ThrownPotion) potion.getBukkitEntity();
        CraftBlock hitBlock = null;
        BlockFace hitFace = null;

        if (position.getType() == HitResult.Type.BLOCK) {
            BlockHitResult positionBlock = (BlockHitResult) position;

            hitBlock = CraftBlock.at(potion.level(), positionBlock.getBlockPos());
            hitFace = CraftBlock.notchToBlockFace(positionBlock.getDirection());
        }

        CraftEntity hitEntity = null;

        if (position.getType() == HitResult.Type.ENTITY) {
            hitEntity = ((EntityHitResult) position).getEntity().getBukkitEntity();
        }

        PotionSplashEvent event = new PotionSplashEvent(thrownPotion, hitEntity, hitBlock, hitFace, affectedEntities);

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static LingeringPotionSplashEvent callLingeringPotionSplashEvent(ThrownPotion potion, HitResult position, AreaEffectCloud cloud) {
        org.bukkit.entity.ThrownPotion thrownPotion = (org.bukkit.entity.ThrownPotion) potion.getBukkitEntity();
        org.bukkit.entity.AreaEffectCloud effectCloud = (org.bukkit.entity.AreaEffectCloud) cloud.getBukkitEntity();
        CraftBlock hitBlock = null;
        BlockFace hitFace = null;

        if (position.getType() == HitResult.Type.BLOCK) {
            BlockHitResult positionBlock = (BlockHitResult) position;

            hitBlock = CraftBlock.at(potion.level(), positionBlock.getBlockPos());
            hitFace = CraftBlock.notchToBlockFace(positionBlock.getDirection());
        }

        CraftEntity hitEntity = null;

        if (position.getType() == HitResult.Type.ENTITY) {
            hitEntity = ((EntityHitResult) position).getEntity().getBukkitEntity();
        }

        LingeringPotionSplashEvent event = new LingeringPotionSplashEvent(thrownPotion, hitEntity, hitBlock, hitFace, effectCloud);

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static BlockFadeEvent callBlockFadeEvent(LevelAccessor world, BlockPos pos, net.minecraft.world.level.block.state.BlockState newBlock) {
        CraftBlockState state = CraftBlockStates.getBlockState(world, pos);

        state.setData(newBlock);
        BlockFadeEvent event = new BlockFadeEvent(state.getBlock(), state);

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static boolean handleMoistureChangeEvent(Level world, BlockPos pos, net.minecraft.world.level.block.state.BlockState newBlock, int flag) {
        CraftBlockState state = CraftBlockStates.getBlockState(world, pos, flag);

        state.setData(newBlock);
        MoistureChangeEvent event = new MoistureChangeEvent(state.getBlock(), state);

        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            state.update(true);
        }

        return !event.isCancelled();
    }

    public static boolean handleBlockSpreadEvent(Level world, BlockPos source, BlockPos target, net.minecraft.world.level.block.state.BlockState block) {
        return handleBlockSpreadEvent(world, source, target, block, 2);
    }

    public static boolean handleBlockSpreadEvent(LevelAccessor world, BlockPos source, BlockPos target, net.minecraft.world.level.block.state.BlockState block, int flag) {
        if (!(world instanceof Level)) {
            world.setBlock(target, block, flag);
            return true;
        } else {
            CraftBlockState state = CraftBlockStates.getBlockState(world, target, flag);

            state.setData(block);
            BlockSpreadEvent event = new BlockSpreadEvent(state.getBlock(), CraftBlock.at(world, CraftEventFactory.sourceBlockOverride != null ? CraftEventFactory.sourceBlockOverride : source), state);

            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                state.update(true);
            }

            return !event.isCancelled();
        }
    }

    public static EntityDeathEvent callEntityDeathEvent(LivingEntity victim) {
        return callEntityDeathEvent(victim, new ArrayList(0));
    }

    public static EntityDeathEvent callEntityDeathEvent(LivingEntity victim, List drops) {
        CraftLivingEntity entity = (CraftLivingEntity) victim.getBukkitEntity();
        EntityDeathEvent event = new EntityDeathEvent(entity, drops, victim.getExpReward());
        CraftWorld world = (CraftWorld) entity.getWorld();

        Bukkit.getServer().getPluginManager().callEvent(event);
        victim.expToDrop = event.getDroppedExp();
        Iterator iterator = event.getDrops().iterator();

        if (victim instanceof ServerPlayer) //Ketting - player death drops are handled in callPlayerDeathEvent
            return event;

        while (iterator.hasNext()) {
            org.bukkit.inventory.ItemStack stack = (org.bukkit.inventory.ItemStack) iterator.next();

            if (stack != null && stack.getType() != Material.AIR && stack.getAmount() != 0) {
                world.dropItem(entity.getLocation(), stack);
            }
        }

        return event;
    }

    public static PlayerDeathEvent callPlayerDeathEvent(ServerPlayer victim, List drops, String deathMessage, boolean keepInventory) {
        CraftPlayer entity = victim.getBukkitEntity();
        PlayerDeathEvent event = new PlayerDeathEvent(entity, drops, victim.getExpReward(), 0, deathMessage);

        event.setKeepInventory(keepInventory);
        event.setKeepLevel(victim.keepLevel);
        World world = entity.getWorld();

        Bukkit.getServer().getPluginManager().callEvent(event);
        victim.keepLevel = event.getKeepLevel();
        victim.newLevel = event.getNewLevel();
        victim.newTotalExp = event.getNewTotalExp();
        victim.expToDrop = event.getDroppedExp();
        victim.newExp = event.getNewExp();
        Iterator iterator = event.getDrops().iterator();

        while (iterator.hasNext()) {
            org.bukkit.inventory.ItemStack stack = (org.bukkit.inventory.ItemStack) iterator.next();

            if (stack != null && stack.getType() != Material.AIR) {
                world.dropItem(entity.getLocation(), stack);
            }
        }

        return event;
    }

    public static ServerListPingEvent callServerListPingEvent(SocketAddress address, String motd, int numPlayers, int maxPlayers) {
        ServerListPingEvent event = new ServerListPingEvent("", ((InetSocketAddress) address).getAddress(), motd, numPlayers, maxPlayers);

        Bukkit.getServer().getPluginManager().callEvent(event);
        return event;
    }

    private static EntityDamageEvent handleEntityDamageEvent(Entity entity, DamageSource source, Map modifiers, Map modifierFunctions) {
        return handleEntityDamageEvent(entity, source, modifiers, modifierFunctions, false);
    }

    private static EntityDamageEvent handleEntityDamageEvent(Entity entity, DamageSource source, Map modifiers, Map modifierFunctions, boolean cancelled) {
        DamageCause cause;

        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            Entity damager = CraftEventFactory.entityDamage;

            CraftEventFactory.entityDamage = null;
            Object event;

            if (damager == null) {
                event = new EntityDamageByBlockEvent((Block) null, entity.getBukkitEntity(), DamageCause.BLOCK_EXPLOSION, modifiers, modifierFunctions);
            } else {
                boolean flag = entity instanceof EnderDragon;

                if (damager instanceof TNTPrimed) {
                    cause = DamageCause.BLOCK_EXPLOSION;
                } else {
                    cause = DamageCause.ENTITY_EXPLOSION;
                }

                event = new EntityDamageByEntityEvent(damager.getBukkitEntity(), entity.getBukkitEntity(), cause, modifiers, modifierFunctions);
            }

            ((EntityDamageEvent) event).setCancelled(cancelled);
            callEvent((Event) event);
            if (!((EntityDamageEvent) event).isCancelled()) {
                ((EntityDamageEvent) event).getEntity().setLastDamageCause((EntityDamageEvent) event);
            } else {
                entity.lastDamageCancelled = true;
            }

            return (EntityDamageEvent) event;
        } else {
            Entity cause;

            if (source.getEntity() == null && source.getDirectEntity() == null) {
                EntityDamageByBlockEvent event;

                if (source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
                    event = new EntityDamageByBlockEvent((Block) null, entity.getBukkitEntity(), DamageCause.VOID, modifiers, modifierFunctions);
                    event.setCancelled(cancelled);
                    callEvent(event);
                    if (!event.isCancelled()) {
                        event.getEntity().setLastDamageCause(event);
                    } else {
                        entity.lastDamageCancelled = true;
                    }

                    return event;
                } else {
                    Block damager;

                    if (source.is(DamageTypes.LAVA)) {
                        event = new EntityDamageByBlockEvent(CraftEventFactory.blockDamage, entity.getBukkitEntity(), DamageCause.LAVA, modifiers, modifierFunctions);
                        event.setCancelled(cancelled);
                        damager = CraftEventFactory.blockDamage;
                        CraftEventFactory.blockDamage = null;
                        callEvent(event);
                        CraftEventFactory.blockDamage = damager;
                        if (!event.isCancelled()) {
                            event.getEntity().setLastDamageCause(event);
                        } else {
                            entity.lastDamageCancelled = true;
                        }

                        return event;
                    } else if (CraftEventFactory.blockDamage != null) {
                        cause = null;
                        damager = CraftEventFactory.blockDamage;
                        if (!source.is(DamageTypes.CACTUS) && !source.is(DamageTypes.SWEET_BERRY_BUSH) && !source.is(DamageTypes.STALAGMITE) && !source.is(DamageTypes.FALLING_STALACTITE) && !source.is(DamageTypes.FALLING_ANVIL)) {
                            if (source.is(DamageTypes.HOT_FLOOR)) {
                                cause = DamageCause.HOT_FLOOR;
                            } else if (source.is(DamageTypes.MAGIC)) {
                                cause = DamageCause.MAGIC;
                            } else {
                                if (!source.is(DamageTypes.IN_FIRE)) {
                                    throw new IllegalStateException(String.format("Unhandled damage of %s by %s from %s", entity, damager, source.getMsgId()));
                                }

                                cause = DamageCause.FIRE;
                            }
                        } else {
                            cause = DamageCause.CONTACT;
                        }

                        EntityDamageByBlockEvent event = new EntityDamageByBlockEvent(damager, entity.getBukkitEntity(), cause, modifiers, modifierFunctions);

                        event.setCancelled(cancelled);
                        CraftEventFactory.blockDamage = null;
                        callEvent(event);
                        CraftEventFactory.blockDamage = damager;
                        if (!event.isCancelled()) {
                            event.getEntity().setLastDamageCause(event);
                        } else {
                            entity.lastDamageCancelled = true;
                        }

                        return event;
                    } else if (CraftEventFactory.entityDamage == null) {
                        cause = null;
                        if (source.is(DamageTypes.IN_FIRE)) {
                            cause = DamageCause.FIRE;
                        } else if (source.is(DamageTypes.STARVE)) {
                            cause = DamageCause.STARVATION;
                        } else if (source.is(DamageTypes.WITHER)) {
                            cause = DamageCause.WITHER;
                        } else if (source.is(DamageTypes.IN_WALL)) {
                            cause = DamageCause.SUFFOCATION;
                        } else if (source.is(DamageTypes.DROWN)) {
                            cause = DamageCause.DROWNING;
                        } else if (source.is(DamageTypes.ON_FIRE)) {
                            cause = DamageCause.FIRE_TICK;
                        } else if (source.isMelting()) {
                            cause = DamageCause.MELTING;
                        } else if (source.isPoison()) {
                            cause = DamageCause.POISON;
                        } else if (source.is(DamageTypes.MAGIC)) {
                            cause = DamageCause.MAGIC;
                        } else if (source.is(DamageTypes.FALL)) {
                            cause = DamageCause.FALL;
                        } else if (source.is(DamageTypes.FLY_INTO_WALL)) {
                            cause = DamageCause.FLY_INTO_WALL;
                        } else if (source.is(DamageTypes.CRAMMING)) {
                            cause = DamageCause.CRAMMING;
                        } else if (source.is(DamageTypes.DRY_OUT)) {
                            cause = DamageCause.DRYOUT;
                        } else if (source.is(DamageTypes.FREEZE)) {
                            cause = DamageCause.FREEZE;
                        } else if (source.is(DamageTypes.GENERIC_KILL)) {
                            cause = DamageCause.KILL;
                        } else if (source.is(DamageTypes.OUTSIDE_BORDER)) {
                            cause = DamageCause.WORLD_BORDER;
                        } else {
                            cause = DamageCause.CUSTOM;
                        }

                        if (cause != null) {
                            return callEntityDamageEvent((Entity) null, entity, cause, modifiers, modifierFunctions, cancelled);
                        } else {
                            throw new IllegalStateException(String.format("Unhandled damage of %s from %s", entity, source.getMsgId()));
                        }
                    } else {
                        cause = null;
                        CraftEntity damager = CraftEventFactory.entityDamage.getBukkitEntity();

                        CraftEventFactory.entityDamage = null;
                        if (!source.is(DamageTypes.FALLING_STALACTITE) && !source.is(DamageTypes.FALLING_BLOCK) && !source.is(DamageTypes.FALLING_ANVIL)) {
                            if (damager instanceof LightningStrike) {
                                cause = DamageCause.LIGHTNING;
                            } else if (source.is(DamageTypes.FALL)) {
                                cause = DamageCause.FALL;
                            } else if (source.is(DamageTypes.DRAGON_BREATH)) {
                                cause = DamageCause.DRAGON_BREATH;
                            } else {
                                if (!source.is(DamageTypes.MAGIC)) {
                                    throw new IllegalStateException(String.format("Unhandled damage of %s by %s from %s", entity, damager.getHandle(), source.getMsgId()));
                                }

                                cause = DamageCause.MAGIC;
                            }
                        } else {
                            cause = DamageCause.FALLING_BLOCK;
                        }

                        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(damager, entity.getBukkitEntity(), cause, modifiers, modifierFunctions);

                        event.setCancelled(cancelled);
                        callEvent(event);
                        if (!event.isCancelled()) {
                            event.getEntity().setLastDamageCause(event);
                        } else {
                            entity.lastDamageCancelled = true;
                        }

                        return event;
                    }
                }
            } else {
                cause = source.getEntity();
                DamageCause cause = source.isSweep() ? DamageCause.ENTITY_SWEEP_ATTACK : DamageCause.ENTITY_ATTACK;

                if (source.isIndirect() && source.getDirectEntity() != null) {
                    cause = source.getDirectEntity();
                }

                if (cause instanceof net.minecraft.world.entity.projectile.Projectile) {
                    if (cause.getBukkitEntity() instanceof org.bukkit.entity.ThrownPotion) {
                        cause = DamageCause.MAGIC;
                    } else if (cause.getBukkitEntity() instanceof Projectile) {
                        cause = DamageCause.PROJECTILE;
                    }
                } else if (source.is(DamageTypes.THORNS)) {
                    cause = DamageCause.THORNS;
                } else if (source.is(DamageTypes.SONIC_BOOM)) {
                    cause = DamageCause.SONIC_BOOM;
                }

                return callEntityDamageEvent(cause, entity, cause, modifiers, modifierFunctions, cancelled);
            }
        }
    }

    private static EntityDamageEvent callEntityDamageEvent(Entity damager, Entity damagee, DamageCause cause, Map modifiers, Map modifierFunctions) {
        return callEntityDamageEvent(damager, damagee, cause, modifiers, modifierFunctions, false);
    }

    private static EntityDamageEvent callEntityDamageEvent(Entity damager, Entity damagee, DamageCause cause, Map modifiers, Map modifierFunctions, boolean cancelled) {
        Object event;

        if (damager != null) {
            event = new EntityDamageByEntityEvent(damager.getBukkitEntity(), damagee.getBukkitEntity(), cause, modifiers, modifierFunctions);
        } else {
            event = new EntityDamageEvent(damagee.getBukkitEntity(), cause, modifiers, modifierFunctions);
        }

        ((EntityDamageEvent) event).setCancelled(cancelled);
        callEvent((Event) event);
        if (!((EntityDamageEvent) event).isCancelled()) {
            ((EntityDamageEvent) event).getEntity().setLastDamageCause((EntityDamageEvent) event);
        } else {
            damagee.lastDamageCancelled = true;
        }

        return (EntityDamageEvent) event;
    }

    public static EntityDamageEvent handleLivingEntityDamageEvent(Entity damagee, DamageSource source, double rawDamage, double hardHatModifier, double blockingModifier, double armorModifier, double resistanceModifier, double magicModifier, double absorptionModifier, Function hardHat, Function blocking, Function armor, Function resistance, Function magic, Function absorption) {
        EnumMap modifiers = new EnumMap(DamageModifier.class);
        EnumMap modifierFunctions = new EnumMap(DamageModifier.class);

        modifiers.put(DamageModifier.BASE, rawDamage);
        modifierFunctions.put(DamageModifier.BASE, CraftEventFactory.ZERO);
        if (source.is(DamageTypes.FALLING_BLOCK) || source.is(DamageTypes.FALLING_ANVIL)) {
            modifiers.put(DamageModifier.HARD_HAT, hardHatModifier);
            modifierFunctions.put(DamageModifier.HARD_HAT, hardHat);
        }

        if (damagee instanceof net.minecraft.world.entity.player.Player) {
            modifiers.put(DamageModifier.BLOCKING, blockingModifier);
            modifierFunctions.put(DamageModifier.BLOCKING, blocking);
        }

        modifiers.put(DamageModifier.ARMOR, armorModifier);
        modifierFunctions.put(DamageModifier.ARMOR, armor);
        modifiers.put(DamageModifier.RESISTANCE, resistanceModifier);
        modifierFunctions.put(DamageModifier.RESISTANCE, resistance);
        modifiers.put(DamageModifier.MAGIC, magicModifier);
        modifierFunctions.put(DamageModifier.MAGIC, magic);
        modifiers.put(DamageModifier.ABSORPTION, absorptionModifier);
        modifierFunctions.put(DamageModifier.ABSORPTION, absorption);
        return handleEntityDamageEvent(damagee, source, modifiers, modifierFunctions);
    }

    public static boolean handleNonLivingEntityDamageEvent(Entity entity, DamageSource source, double damage) {
        return handleNonLivingEntityDamageEvent(entity, source, damage, true);
    }

    public static boolean handleNonLivingEntityDamageEvent(Entity entity, DamageSource source, double damage, boolean cancelOnZeroDamage) {
        return handleNonLivingEntityDamageEvent(entity, source, damage, cancelOnZeroDamage, false);
    }

    public static EntityDamageEvent callNonLivingEntityDamageEvent(Entity entity, DamageSource source, double damage, boolean cancelled) {
        EnumMap modifiers = new EnumMap(DamageModifier.class);
        EnumMap functions = new EnumMap(DamageModifier.class);

        modifiers.put(DamageModifier.BASE, damage);
        functions.put(DamageModifier.BASE, CraftEventFactory.ZERO);
        return handleEntityDamageEvent(entity, source, modifiers, functions, cancelled);
    }

    public static boolean handleNonLivingEntityDamageEvent(Entity entity, DamageSource source, double damage, boolean cancelOnZeroDamage, boolean cancelled) {
        EntityDamageEvent event = callNonLivingEntityDamageEvent(entity, source, damage, cancelled);

        return event == null ? false : event.isCancelled() || cancelOnZeroDamage && event.getDamage() == 0.0D;
    }

    public static PlayerLevelChangeEvent callPlayerLevelChangeEvent(Player player, int oldLevel, int newLevel) {
        PlayerLevelChangeEvent event = new PlayerLevelChangeEvent(player, oldLevel, newLevel);

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerExpChangeEvent callPlayerExpChangeEvent(net.minecraft.world.entity.player.Player entity, int expAmount) {
        Player player = (Player) entity.getBukkitEntity();
        PlayerExpChangeEvent event = new PlayerExpChangeEvent(player, expAmount);

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerExpCooldownChangeEvent callPlayerXpCooldownEvent(net.minecraft.world.entity.player.Player entity, int newCooldown, org.bukkit.event.player.PlayerExpCooldownChangeEvent.ChangeReason changeReason) {
        Player player = (Player) entity.getBukkitEntity();
        PlayerExpCooldownChangeEvent event = new PlayerExpCooldownChangeEvent(player, newCooldown, changeReason);

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerItemMendEvent callPlayerItemMendEvent(net.minecraft.world.entity.player.Player entity, ExperienceOrb orb, ItemStack nmsMendedItem, net.minecraft.world.entity.EquipmentSlot slot, int repairAmount) {
        Player player = (Player) entity.getBukkitEntity();
        CraftItemStack bukkitStack = CraftItemStack.asCraftMirror(nmsMendedItem);
        PlayerItemMendEvent event = new PlayerItemMendEvent(player, bukkitStack, CraftEquipmentSlot.getSlot(slot), (org.bukkit.entity.ExperienceOrb) orb.getBukkitEntity(), repairAmount);

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static boolean handleBlockGrowEvent(Level world, BlockPos pos, net.minecraft.world.level.block.state.BlockState block) {
        return handleBlockGrowEvent(world, pos, block, 3);
    }

    public static boolean handleBlockGrowEvent(Level world, BlockPos pos, net.minecraft.world.level.block.state.BlockState newData, int flag) {
        Block block = world.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        CraftBlockState state = (CraftBlockState) block.getState();

        state.setData(newData);
        BlockGrowEvent event = new BlockGrowEvent(block, state);

        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            state.update(true);
        }

        return !event.isCancelled();
    }

    public static FluidLevelChangeEvent callFluidLevelChangeEvent(Level world, BlockPos block, net.minecraft.world.level.block.state.BlockState newData) {
        FluidLevelChangeEvent event = new FluidLevelChangeEvent(CraftBlock.at(world, block), CraftBlockData.fromData(newData));

        world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static FoodLevelChangeEvent callFoodLevelChangeEvent(net.minecraft.world.entity.player.Player entity, int level) {
        return callFoodLevelChangeEvent(entity, level, (ItemStack) null);
    }

    public static FoodLevelChangeEvent callFoodLevelChangeEvent(net.minecraft.world.entity.player.Player entity, int level, ItemStack item) {
        FoodLevelChangeEvent event = new FoodLevelChangeEvent(entity.getBukkitEntity(), level, item == null ? null : CraftItemStack.asBukkitCopy(item));

        entity.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static PigZapEvent callPigZapEvent(Entity pig, Entity lightning, Entity pigzombie) {
        PigZapEvent event = new PigZapEvent((Pig) pig.getBukkitEntity(), (LightningStrike) lightning.getBukkitEntity(), (PigZombie) pigzombie.getBukkitEntity());

        pig.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static boolean callHorseJumpEvent(Entity horse, float power) {
        HorseJumpEvent event = new HorseJumpEvent((AbstractHorse) horse.getBukkitEntity(), power);

        horse.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static boolean callEntityChangeBlockEvent(Entity entity, BlockPos position, net.minecraft.world.level.block.state.BlockState newBlock) {
        return callEntityChangeBlockEvent(entity, position, newBlock, false);
    }

    public static boolean callEntityChangeBlockEvent(Entity entity, BlockPos position, net.minecraft.world.level.block.state.BlockState newBlock, boolean cancelled) {
        Block block = entity.level().getWorld().getBlockAt(position.getX(), position.getY(), position.getZ());
        EntityChangeBlockEvent event = new EntityChangeBlockEvent(entity.getBukkitEntity(), block, CraftBlockData.fromData(newBlock));

        event.setCancelled(cancelled);
        event.getEntity().getServer().getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static CreeperPowerEvent callCreeperPowerEvent(Entity creeper, Entity lightning, PowerCause cause) {
        CreeperPowerEvent event = new CreeperPowerEvent((Creeper) creeper.getBukkitEntity(), (LightningStrike) lightning.getBukkitEntity(), cause);

        creeper.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static EntityTargetEvent callEntityTargetEvent(Entity entity, Entity target, TargetReason reason) {
        EntityTargetEvent event = new EntityTargetEvent(entity.getBukkitEntity(), target == null ? null : target.getBukkitEntity(), reason);

        entity.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static EntityTargetLivingEntityEvent callEntityTargetLivingEvent(Entity entity, LivingEntity target, TargetReason reason) {
        EntityTargetLivingEntityEvent event = new EntityTargetLivingEntityEvent(entity.getBukkitEntity(), target == null ? null : (org.bukkit.entity.LivingEntity) target.getBukkitEntity(), reason);

        entity.getBukkitEntity().getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static EntityBreakDoorEvent callEntityBreakDoorEvent(Entity entity, BlockPos pos) {
        CraftEntity entity1 = entity.getBukkitEntity();
        CraftBlock block = CraftBlock.at(entity.level(), pos);
        EntityBreakDoorEvent event = new EntityBreakDoorEvent((org.bukkit.entity.LivingEntity) entity1, block);

        entity1.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static AbstractContainerMenu callInventoryOpenEvent(ServerPlayer player, AbstractContainerMenu container) {
        return callInventoryOpenEvent(player, container, false);
    }

    public static AbstractContainerMenu callInventoryOpenEvent(ServerPlayer player, AbstractContainerMenu container, boolean cancelled) {
        if (player.containerMenu != player.inventoryMenu) {
            player.connection.handleContainerClose(new ServerboundContainerClosePacket(player.containerMenu.containerId));
        }

        CraftServer server = player.level().getCraftServer();
        CraftPlayer craftPlayer = player.getBukkitEntity();

        player.containerMenu.transferTo(container, craftPlayer);
        InventoryOpenEvent event = new InventoryOpenEvent(container.getBukkitView());

        event.setCancelled(cancelled);
        server.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            container.transferTo(player.containerMenu, craftPlayer);
            return null;
        } else {
            return container;
        }
    }

    public static ItemStack callPreCraftEvent(Container matrix, Container resultInventory, ItemStack result, InventoryView lastCraftView, boolean isRepair) {
        CraftInventoryCrafting inventory = new CraftInventoryCrafting(matrix, resultInventory);

        inventory.setResult(CraftItemStack.asCraftMirror(result));
        PrepareItemCraftEvent event = new PrepareItemCraftEvent(inventory, lastCraftView, isRepair);

        Bukkit.getPluginManager().callEvent(event);
        org.bukkit.inventory.ItemStack bitem = event.getInventory().getResult();

        return CraftItemStack.asNMSCopy(bitem);
    }

    public static ProjectileLaunchEvent callProjectileLaunchEvent(Entity entity) {
        Projectile bukkitEntity = (Projectile) entity.getBukkitEntity();
        ProjectileLaunchEvent event = new ProjectileLaunchEvent(bukkitEntity);

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static ProjectileHitEvent callProjectileHitEvent(Entity entity, HitResult position) {
        if (position.getType() == HitResult.Type.MISS) {
            return null;
        } else {
            CraftBlock hitBlock = null;
            BlockFace hitFace = null;

            if (position.getType() == HitResult.Type.BLOCK) {
                BlockHitResult positionBlock = (BlockHitResult) position;

                hitBlock = CraftBlock.at(entity.level(), positionBlock.getBlockPos());
                hitFace = CraftBlock.notchToBlockFace(positionBlock.getDirection());
            }

            CraftEntity hitEntity = null;

            if (position.getType() == HitResult.Type.ENTITY) {
                hitEntity = ((EntityHitResult) position).getEntity().getBukkitEntity();
            }

            ProjectileHitEvent event = new ProjectileHitEvent((Projectile) entity.getBukkitEntity(), hitEntity, hitBlock, hitFace);

            entity.level().getCraftServer().getPluginManager().callEvent(event);
            return event;
        }
    }

    public static ExpBottleEvent callExpBottleEvent(Entity entity, HitResult position, int exp) {
        ThrownExpBottle bottle = (ThrownExpBottle) entity.getBukkitEntity();
        CraftBlock hitBlock = null;
        BlockFace hitFace = null;

        if (position.getType() == HitResult.Type.BLOCK) {
            BlockHitResult positionBlock = (BlockHitResult) position;

            hitBlock = CraftBlock.at(entity.level(), positionBlock.getBlockPos());
            hitFace = CraftBlock.notchToBlockFace(positionBlock.getDirection());
        }

        CraftEntity hitEntity = null;

        if (position.getType() == HitResult.Type.ENTITY) {
            hitEntity = ((EntityHitResult) position).getEntity().getBukkitEntity();
        }

        ExpBottleEvent event = new ExpBottleEvent(bottle, hitEntity, hitBlock, hitFace, exp);

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static BlockRedstoneEvent callRedstoneChange(Level world, BlockPos pos, int oldCurrent, int newCurrent) {
        BlockRedstoneEvent event = new BlockRedstoneEvent(world.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()), oldCurrent, newCurrent);

        world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static NotePlayEvent callNotePlayEvent(Level world, BlockPos pos, NoteBlockInstrument instrument, int note) {
        NotePlayEvent event = new NotePlayEvent(world.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()), Instrument.getByType((byte) instrument.ordinal()), new Note(note));

        world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static void callPlayerItemBreakEvent(net.minecraft.world.entity.player.Player human, ItemStack brokenItem) {
        CraftItemStack item = CraftItemStack.asCraftMirror(brokenItem);
        PlayerItemBreakEvent event = new PlayerItemBreakEvent((Player) human.getBukkitEntity(), item);

        Bukkit.getPluginManager().callEvent(event);
    }

    public static BlockIgniteEvent callBlockIgniteEvent(Level world, BlockPos block, BlockPos source) {
        CraftWorld bukkitWorld = world.getWorld();
        Block igniter = bukkitWorld.getBlockAt(source.getX(), source.getY(), source.getZ());
        IgniteCause cause;

        switch ($SWITCH_TABLE$org$bukkit$Material()[igniter.getType().ordinal()]) {
            case 647:
                cause = IgniteCause.FLINT_AND_STEEL;
                break;
            case 1257:
                cause = IgniteCause.LAVA;
                break;
            case 1262:
            default:
                cause = IgniteCause.SPREAD;
        }

        BlockIgniteEvent event = new BlockIgniteEvent(bukkitWorld.getBlockAt(block.getX(), block.getY(), block.getZ()), cause, igniter);

        world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockIgniteEvent callBlockIgniteEvent(Level world, BlockPos pos, Entity igniter) {
        CraftWorld bukkitWorld = world.getWorld();
        CraftEntity bukkitIgniter = igniter.getBukkitEntity();
        IgniteCause cause;

        switch ($SWITCH_TABLE$org$bukkit$entity$EntityType()[bukkitIgniter.getType().ordinal()]) {
            case 10:
                cause = IgniteCause.ARROW;
                break;
            case 12:
            case 13:
                cause = IgniteCause.FIREBALL;
                break;
            case 83:
                cause = IgniteCause.ENDER_CRYSTAL;
                break;
            case 123:
                cause = IgniteCause.LIGHTNING;
                break;
            default:
                cause = IgniteCause.FLINT_AND_STEEL;
        }

        if (igniter instanceof net.minecraft.world.entity.projectile.Projectile) {
            Entity shooter = ((net.minecraft.world.entity.projectile.Projectile) igniter).getOwner();

            if (shooter != null) {
                bukkitIgniter = shooter.getBukkitEntity();
            }
        }

        BlockIgniteEvent event = new BlockIgniteEvent(bukkitWorld.getBlockAt(pos.getX(), pos.getY(), pos.getZ()), cause, bukkitIgniter);

        world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockIgniteEvent callBlockIgniteEvent(Level world, int x, int y, int z, Explosion explosion) {
        CraftWorld bukkitWorld = world.getWorld();
        CraftEntity igniter = explosion.source == null ? null : explosion.source.getBukkitEntity();
        BlockIgniteEvent event = new BlockIgniteEvent(bukkitWorld.getBlockAt(x, y, z), IgniteCause.EXPLOSION, igniter);

        world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockIgniteEvent callBlockIgniteEvent(Level world, BlockPos pos, IgniteCause cause, Entity igniter) {
        BlockIgniteEvent event = new BlockIgniteEvent(world.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ()), cause, igniter.getBukkitEntity());

        world.getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static void handleInventoryCloseEvent(net.minecraft.world.entity.player.Player human) {
        InventoryCloseEvent event = new InventoryCloseEvent(human.containerMenu.getBukkitView());

        human.level().getCraftServer().getPluginManager().callEvent(event);
        human.containerMenu.transferTo(human.inventoryMenu, human.getBukkitEntity());
    }

    public static ItemStack handleEditBookEvent(ServerPlayer player, int itemInHandIndex, ItemStack itemInHand, ItemStack newBookItem) {
        PlayerEditBookEvent editBookEvent = new PlayerEditBookEvent(player.getBukkitEntity(), itemInHandIndex >= 0 && itemInHandIndex <= 8 ? itemInHandIndex : -1, (BookMeta) CraftItemStack.getItemMeta(itemInHand), (BookMeta) CraftItemStack.getItemMeta(newBookItem), newBookItem.getItem() == Items.WRITTEN_BOOK);

        player.level().getCraftServer().getPluginManager().callEvent(editBookEvent);
        if (itemInHand != null && itemInHand.getItem() == Items.WRITABLE_BOOK) {
            if (!editBookEvent.isCancelled()) {
                if (editBookEvent.isSigning()) {
                    itemInHand.setItem(Items.WRITTEN_BOOK);
                }

                CraftMetaBook meta = (CraftMetaBook) editBookEvent.getNewBookMeta();

                CraftItemStack.setItemMeta(itemInHand, meta);
            } else {
                player.getBukkitEntity().updateInventory();
            }
        }

        return itemInHand;
    }

    public static PlayerUnleashEntityEvent callPlayerUnleashEntityEvent(Mob entity, net.minecraft.world.entity.player.Player player, InteractionHand enumhand) {
        PlayerUnleashEntityEvent event = new PlayerUnleashEntityEvent(entity.getBukkitEntity(), (Player) player.getBukkitEntity(), CraftEquipmentSlot.getHand(enumhand));

        entity.level().getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static PlayerLeashEntityEvent callPlayerLeashEntityEvent(Mob entity, Entity leashHolder, net.minecraft.world.entity.player.Player player, InteractionHand enumhand) {
        PlayerLeashEntityEvent event = new PlayerLeashEntityEvent(entity.getBukkitEntity(), leashHolder.getBukkitEntity(), (Player) player.getBukkitEntity(), CraftEquipmentSlot.getHand(enumhand));

        entity.level().getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockShearEntityEvent callBlockShearEntityEvent(Entity animal, Block dispenser, CraftItemStack is) {
        BlockShearEntityEvent bse = new BlockShearEntityEvent(dispenser, animal.getBukkitEntity(), is);

        Bukkit.getPluginManager().callEvent(bse);
        return bse;
    }

    public static boolean handlePlayerShearEntityEvent(net.minecraft.world.entity.player.Player player, Entity sheared, ItemStack shears, InteractionHand hand) {
        if (!(player instanceof ServerPlayer)) {
            return true;
        } else {
            PlayerShearEntityEvent event = new PlayerShearEntityEvent((Player) player.getBukkitEntity(), sheared.getBukkitEntity(), CraftItemStack.asCraftMirror(shears), hand == InteractionHand.OFF_HAND ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND);

            Bukkit.getPluginManager().callEvent(event);
            return !event.isCancelled();
        }
    }

    public static Cancellable handleStatisticsIncrease(net.minecraft.world.entity.player.Player entityHuman, Stat statistic, int current, int newValue) {
        CraftPlayer player = ((ServerPlayer) entityHuman).getBukkitEntity();
        Statistic stat = CraftStatistic.getBukkitStatistic(statistic);

        if (stat == null) {
            System.err.println("Unhandled statistic: " + statistic);
            return null;
        } else {
            switch ($SWITCH_TABLE$org$bukkit$Statistic()[stat.ordinal()]) {
                case 13:
                case 14:
                case 15:
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 35:
                case 60:
                case 61:
                case 83:
                    return null;
                default:
                    PlayerStatisticIncrementEvent event;

                    if (stat.getType() == Type.UNTYPED) {
                        event = new PlayerStatisticIncrementEvent(player, stat, current, newValue);
                    } else if (stat.getType() == Type.ENTITY) {
                        EntityType entityType = CraftStatistic.getEntityTypeFromStatistic(statistic);

                        event = new PlayerStatisticIncrementEvent(player, stat, current, newValue, entityType);
                    } else {
                        Material material = CraftStatistic.getMaterialFromStatistic(statistic);

                        event = new PlayerStatisticIncrementEvent(player, stat, current, newValue, material);
                    }

                    entityHuman.level().getCraftServer().getPluginManager().callEvent(event);
                    return (Cancellable) event;
            }
        }
    }

    public static FireworkExplodeEvent callFireworkExplodeEvent(FireworkRocketEntity firework) {
        FireworkExplodeEvent event = new FireworkExplodeEvent((Firework) firework.getBukkitEntity());

        firework.level().getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static PrepareAnvilEvent callPrepareAnvilEvent(InventoryView view, ItemStack item) {
        PrepareAnvilEvent event = new PrepareAnvilEvent(view, CraftItemStack.asCraftMirror(item).clone());

        event.getView().getPlayer().getServer().getPluginManager().callEvent(event);
        event.getInventory().setItem(2, event.getResult());
        return event;
    }

    public static PrepareGrindstoneEvent callPrepareGrindstoneEvent(InventoryView view, ItemStack item) {
        PrepareGrindstoneEvent event = new PrepareGrindstoneEvent(view, CraftItemStack.asCraftMirror(item).clone());

        event.getView().getPlayer().getServer().getPluginManager().callEvent(event);
        event.getInventory().setItem(2, event.getResult());
        return event;
    }

    public static PrepareSmithingEvent callPrepareSmithingEvent(InventoryView view, ItemStack item) {
        PrepareSmithingEvent event = new PrepareSmithingEvent(view, CraftItemStack.asCraftMirror(item).clone());

        event.getView().getPlayer().getServer().getPluginManager().callEvent(event);
        event.getInventory().setResult(event.getResult());
        return event;
    }

    public static SpawnerSpawnEvent callSpawnerSpawnEvent(Entity spawnee, BlockPos pos) {
        CraftEntity entity = spawnee.getBukkitEntity();
        BlockState state = CraftBlock.at(spawnee.level(), pos).getState();

        if (!(state instanceof CreatureSpawner)) {
            state = null;
        }

        SpawnerSpawnEvent event = new SpawnerSpawnEvent(entity, (CreatureSpawner) state);

        entity.getServer().getPluginManager().callEvent(event);
        return event;
    }

    public static EntityToggleGlideEvent callToggleGlideEvent(LivingEntity entity, boolean gliding) {
        EntityToggleGlideEvent event = new EntityToggleGlideEvent((org.bukkit.entity.LivingEntity) entity.getBukkitEntity(), gliding);

        entity.level().getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static EntityToggleSwimEvent callToggleSwimEvent(LivingEntity entity, boolean swimming) {
        EntityToggleSwimEvent event = new EntityToggleSwimEvent((org.bukkit.entity.LivingEntity) entity.getBukkitEntity(), swimming);

        entity.level().getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static AreaEffectCloudApplyEvent callAreaEffectCloudApplyEvent(AreaEffectCloud cloud, List entities) {
        AreaEffectCloudApplyEvent event = new AreaEffectCloudApplyEvent((org.bukkit.entity.AreaEffectCloud) cloud.getBukkitEntity(), entities);

        cloud.level().getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static VehicleCreateEvent callVehicleCreateEvent(Entity entity) {
        Vehicle bukkitEntity = (Vehicle) entity.getBukkitEntity();
        VehicleCreateEvent event = new VehicleCreateEvent(bukkitEntity);

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static EntityBreedEvent callEntityBreedEvent(LivingEntity child, LivingEntity mother, LivingEntity father, LivingEntity breeder, ItemStack bredWith, int experience) {
        org.bukkit.entity.LivingEntity breederEntity = (org.bukkit.entity.LivingEntity) (breeder == null ? null : breeder.getBukkitEntity());
        CraftItemStack bredWithStack = bredWith == null ? null : CraftItemStack.asCraftMirror(bredWith).clone();
        EntityBreedEvent event = new EntityBreedEvent((org.bukkit.entity.LivingEntity) child.getBukkitEntity(), (org.bukkit.entity.LivingEntity) mother.getBukkitEntity(), (org.bukkit.entity.LivingEntity) father.getBukkitEntity(), breederEntity, bredWithStack, experience);

        child.level().getCraftServer().getPluginManager().callEvent(event);
        return event;
    }

    public static BlockPhysicsEvent callBlockPhysicsEvent(LevelAccessor world, BlockPos blockposition) {
        CraftBlock block = CraftBlock.at(world, blockposition);
        BlockPhysicsEvent event = new BlockPhysicsEvent(block, block.getBlockData());

        if (world instanceof Level) {
            ((Level) world).getServer().server.getPluginManager().callEvent(event);
        }

        return event;
    }

    public static boolean handleBlockFormEvent(Level world, BlockPos pos, net.minecraft.world.level.block.state.BlockState block) {
        return handleBlockFormEvent(world, pos, block, 3);
    }

    public static EntityPotionEffectEvent callEntityPotionEffectChangeEvent(LivingEntity entity, @Nullable MobEffectInstance oldEffect, @Nullable MobEffectInstance newEffect, org.bukkit.event.entity.EntityPotionEffectEvent.Cause cause) {
        return callEntityPotionEffectChangeEvent(entity, oldEffect, newEffect, cause, true);
    }

    public static EntityPotionEffectEvent callEntityPotionEffectChangeEvent(LivingEntity entity, @Nullable MobEffectInstance oldEffect, @Nullable MobEffectInstance newEffect, org.bukkit.event.entity.EntityPotionEffectEvent.Cause cause, org.bukkit.event.entity.EntityPotionEffectEvent.Action action) {
        return callEntityPotionEffectChangeEvent(entity, oldEffect, newEffect, cause, action, true);
    }

    public static EntityPotionEffectEvent callEntityPotionEffectChangeEvent(LivingEntity entity, @Nullable MobEffectInstance oldEffect, @Nullable MobEffectInstance newEffect, org.bukkit.event.entity.EntityPotionEffectEvent.Cause cause, boolean willOverride) {
        org.bukkit.event.entity.EntityPotionEffectEvent.Action action = org.bukkit.event.entity.EntityPotionEffectEvent.Action.CHANGED;

        if (oldEffect == null) {
            action = org.bukkit.event.entity.EntityPotionEffectEvent.Action.ADDED;
        } else if (newEffect == null) {
            action = org.bukkit.event.entity.EntityPotionEffectEvent.Action.REMOVED;
        }

        return callEntityPotionEffectChangeEvent(entity, oldEffect, newEffect, cause, action, willOverride);
    }

    public static EntityPotionEffectEvent callEntityPotionEffectChangeEvent(LivingEntity entity, @Nullable MobEffectInstance oldEffect, @Nullable MobEffectInstance newEffect, org.bukkit.event.entity.EntityPotionEffectEvent.Cause cause, org.bukkit.event.entity.EntityPotionEffectEvent.Action action, boolean willOverride) {
        PotionEffect bukkitOldEffect = oldEffect == null ? null : CraftPotionUtil.toBukkit(oldEffect);
        PotionEffect bukkitNewEffect = newEffect == null ? null : CraftPotionUtil.toBukkit(newEffect);

        Preconditions.checkState(bukkitOldEffect != null || bukkitNewEffect != null, "Old and new potion effect are both null");
        EntityPotionEffectEvent event = new EntityPotionEffectEvent((org.bukkit.entity.LivingEntity) entity.getBukkitEntity(), bukkitOldEffect, bukkitNewEffect, cause, action, willOverride);

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static boolean handleBlockFormEvent(Level world, BlockPos pos, net.minecraft.world.level.block.state.BlockState block, @Nullable Entity entity) {
        return handleBlockFormEvent(world, pos, block, 3, entity);
    }

    public static boolean handleBlockFormEvent(Level world, BlockPos pos, net.minecraft.world.level.block.state.BlockState block, int flag) {
        return handleBlockFormEvent(world, pos, block, flag, (Entity) null);
    }

    public static boolean handleBlockFormEvent(Level world, BlockPos pos, net.minecraft.world.level.block.state.BlockState block, int flag, @Nullable Entity entity) {
        CraftBlockState blockState = CraftBlockStates.getBlockState(world, pos, flag);

        blockState.setData(block);
        Object event = entity == null ? new BlockFormEvent(blockState.getBlock(), blockState) : new EntityBlockFormEvent(entity.getBukkitEntity(), blockState.getBlock(), blockState);

        world.getCraftServer().getPluginManager().callEvent((Event) event);
        if (!((BlockFormEvent) event).isCancelled()) {
            blockState.update(true);
        }

        return !((BlockFormEvent) event).isCancelled();
    }

    public static boolean handleBatToggleSleepEvent(Entity bat, boolean awake) {
        BatToggleSleepEvent event = new BatToggleSleepEvent((Bat) bat.getBukkitEntity(), awake);

        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static boolean handlePlayerRecipeListUpdateEvent(net.minecraft.world.entity.player.Player who, ResourceLocation recipe) {
        PlayerRecipeDiscoverEvent event = new PlayerRecipeDiscoverEvent((Player) who.getBukkitEntity(), CraftNamespacedKey.fromMinecraft(recipe));

        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static EntityPickupItemEvent callEntityPickupItemEvent(Entity who, ItemEntity item, int remaining, boolean cancelled) {
        EntityPickupItemEvent event = new EntityPickupItemEvent((org.bukkit.entity.LivingEntity) who.getBukkitEntity(), (Item) item.getBukkitEntity(), remaining);

        event.setCancelled(cancelled);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static LightningStrikeEvent callLightningStrikeEvent(LightningStrike entity, org.bukkit.event.weather.LightningStrikeEvent.Cause cause) {
        LightningStrikeEvent event = new LightningStrikeEvent(entity.getWorld(), entity, cause);

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static boolean callRaidTriggerEvent(Raid raid, ServerPlayer player) {
        RaidTriggerEvent event = new RaidTriggerEvent(new CraftRaid(raid), raid.getLevel().getWorld(), player.getBukkitEntity());

        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static void callRaidFinishEvent(Raid raid, List players) {
        RaidFinishEvent event = new RaidFinishEvent(new CraftRaid(raid), raid.getLevel().getWorld(), players);

        Bukkit.getPluginManager().callEvent(event);
    }

    public static void callRaidStopEvent(Raid raid, Reason reason) {
        RaidStopEvent event = new RaidStopEvent(new CraftRaid(raid), raid.getLevel().getWorld(), reason);

        Bukkit.getPluginManager().callEvent(event);
    }

    public static void callRaidSpawnWaveEvent(Raid raid, Raider leader, List raiders) {
        CraftRaider craftLeader = (CraftRaider) leader.getBukkitEntity();
        ArrayList craftRaiders = new ArrayList();
        Iterator iterator = raiders.iterator();

        while (iterator.hasNext()) {
            Raider entityRaider = (Raider) iterator.next();

            craftRaiders.add((org.bukkit.entity.Raider) entityRaider.getBukkitEntity());
        }

        RaidSpawnWaveEvent event = new RaidSpawnWaveEvent(new CraftRaid(raid), raid.getLevel().getWorld(), craftLeader, craftRaiders);

        Bukkit.getPluginManager().callEvent(event);
    }

    public static LootGenerateEvent callLootGenerateEvent(Container inventory, LootTable lootTable, LootContext lootInfo, List loot, boolean plugin) {
        CraftWorld world = lootInfo.getLevel().getWorld();
        Entity entity = (Entity) lootInfo.getParamOrNull(LootContextParams.THIS_ENTITY);
        List bukkitLoot = (List) loot.stream().map(CraftItemStack::asCraftMirror).collect(Collectors.toCollection(ArrayList::new));
        LootGenerateEvent event = new LootGenerateEvent(world, entity != null ? entity.getBukkitEntity() : null, inventory.getOwner(), lootTable.craftLootTable, CraftLootTable.convertContext(lootInfo), bukkitLoot, plugin);

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static boolean callStriderTemperatureChangeEvent(Strider strider, boolean shivering) {
        StriderTemperatureChangeEvent event = new StriderTemperatureChangeEvent((org.bukkit.entity.Strider) strider.getBukkitEntity(), shivering);

        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static boolean handleEntitySpellCastEvent(SpellcasterIllager caster, SpellcasterIllager.IllagerSpell spell) {
        EntitySpellCastEvent event = new EntitySpellCastEvent((Spellcaster) caster.getBukkitEntity(), CraftSpellcaster.toBukkitSpell(spell));

        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static ArrowBodyCountChangeEvent callArrowBodyCountChangeEvent(LivingEntity entity, int oldAmount, int newAmount, boolean isReset) {
        org.bukkit.entity.LivingEntity bukkitEntity = (org.bukkit.entity.LivingEntity) entity.getBukkitEntity();
        ArrowBodyCountChangeEvent event = new ArrowBodyCountChangeEvent(bukkitEntity, oldAmount, newAmount, isReset);

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static EntityExhaustionEvent callPlayerExhaustionEvent(net.minecraft.world.entity.player.Player humanEntity, ExhaustionReason exhaustionReason, float exhaustion) {
        EntityExhaustionEvent event = new EntityExhaustionEvent(humanEntity.getBukkitEntity(), exhaustionReason, exhaustion);

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static PiglinBarterEvent callPiglinBarterEvent(Piglin piglin, List outcome, ItemStack input) {
        PiglinBarterEvent event = new PiglinBarterEvent((org.bukkit.entity.Piglin) piglin.getBukkitEntity(), CraftItemStack.asBukkitCopy(input), (List) outcome.stream().map(CraftItemStack::asBukkitCopy).collect(Collectors.toList()));

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static void callEntitiesLoadEvent(Level world, ChunkPos coords, List entities) {
        List bukkitEntities = Collections.unmodifiableList((List) entities.stream().map(Entity::getBukkitEntity).collect(Collectors.toList()));
        EntitiesLoadEvent event = new EntitiesLoadEvent(new CraftChunk((ServerLevel) world, coords.x, coords.z), bukkitEntities);

        Bukkit.getPluginManager().callEvent(event);
    }

    public static void callEntitiesUnloadEvent(Level world, ChunkPos coords, List entities) {
        List bukkitEntities = Collections.unmodifiableList((List) entities.stream().map(Entity::getBukkitEntity).collect(Collectors.toList()));
        EntitiesUnloadEvent event = new EntitiesUnloadEvent(new CraftChunk((ServerLevel) world, coords.x, coords.z), bukkitEntities);

        Bukkit.getPluginManager().callEvent(event);
    }

    public static boolean callTNTPrimeEvent(Level world, BlockPos pos, PrimeCause cause, Entity causingEntity, BlockPos causePosition) {
        CraftEntity bukkitEntity = causingEntity == null ? null : causingEntity.getBukkitEntity();
        CraftBlock bukkitBlock = causePosition == null ? null : CraftBlock.at(world, causePosition);
        TNTPrimeEvent event = new TNTPrimeEvent(CraftBlock.at(world, pos), cause, bukkitEntity, bukkitBlock);

        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static PlayerRecipeBookClickEvent callRecipeBookClickEvent(ServerPlayer player, Recipe recipe, boolean shiftClick) {
        PlayerRecipeBookClickEvent event = new PlayerRecipeBookClickEvent(player.getBukkitEntity(), recipe, shiftClick);

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static EntityTeleportEvent callEntityTeleportEvent(Entity nmsEntity, double x, double y, double z) {
        CraftEntity entity = nmsEntity.getBukkitEntity();
        Location to = new Location(entity.getWorld(), x, y, z, nmsEntity.getYRot(), nmsEntity.getXRot());
        EntityTeleportEvent event = new EntityTeleportEvent(entity, entity.getLocation(), to);

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static boolean callEntityInteractEvent(Entity nmsEntity, Block block) {
        EntityInteractEvent event = new EntityInteractEvent(nmsEntity.getBukkitEntity(), block);

        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static ExplosionPrimeEvent callExplosionPrimeEvent(Explosive explosive) {
        ExplosionPrimeEvent event = new ExplosionPrimeEvent(explosive);

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    public static ExplosionPrimeEvent callExplosionPrimeEvent(Entity nmsEntity, float size, boolean fire) {
        ExplosionPrimeEvent event = new ExplosionPrimeEvent(nmsEntity.getBukkitEntity(), size, fire);

        Bukkit.getPluginManager().callEvent(event);
        return event;
    }

    static int[] $SWITCH_TABLE$org$bukkit$event$block$Action() {
        int[] aint = CraftEventFactory.$SWITCH_TABLE$org$bukkit$event$block$Action;

        if (aint != null) {
            return aint;
        } else {
            int[] aint1 = new int[Action.values().length];

            try {
                aint1[Action.LEFT_CLICK_AIR.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                aint1[Action.LEFT_CLICK_BLOCK.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            try {
                aint1[Action.PHYSICAL.ordinal()] = 5;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            try {
                aint1[Action.RIGHT_CLICK_AIR.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror3) {
                ;
            }

            try {
                aint1[Action.RIGHT_CLICK_BLOCK.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror4) {
                ;
            }

            CraftEventFactory.$SWITCH_TABLE$org$bukkit$event$block$Action = aint1;
            return aint1;
        }
    }

    static int[] $SWITCH_TABLE$org$bukkit$event$entity$CreatureSpawnEvent$SpawnReason() {
        int[] aint = CraftEventFactory.$SWITCH_TABLE$org$bukkit$event$entity$CreatureSpawnEvent$SpawnReason;

        if (aint != null) {
            return aint;
        } else {
            int[] aint1 = new int[SpawnReason.values().length];

            try {
                aint1[SpawnReason.BEEHIVE.ordinal()] = 31;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                aint1[SpawnReason.BREEDING.ordinal()] = 13;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            try {
                aint1[SpawnReason.BUILD_IRONGOLEM.ordinal()] = 9;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            try {
                aint1[SpawnReason.BUILD_SNOWMAN.ordinal()] = 8;
            } catch (NoSuchFieldError nosuchfielderror3) {
                ;
            }

            try {
                aint1[SpawnReason.BUILD_WITHER.ordinal()] = 10;
            } catch (NoSuchFieldError nosuchfielderror4) {
                ;
            }

            try {
                aint1[SpawnReason.CHUNK_GEN.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror5) {
                ;
            }

            try {
                aint1[SpawnReason.COMMAND.ordinal()] = 37;
            } catch (NoSuchFieldError nosuchfielderror6) {
                ;
            }

            try {
                aint1[SpawnReason.CURED.ordinal()] = 19;
            } catch (NoSuchFieldError nosuchfielderror7) {
                ;
            }

            try {
                aint1[SpawnReason.CUSTOM.ordinal()] = 38;
            } catch (NoSuchFieldError nosuchfielderror8) {
                ;
            }

            try {
                aint1[SpawnReason.DEFAULT.ordinal()] = 39;
            } catch (NoSuchFieldError nosuchfielderror9) {
                ;
            }

            try {
                aint1[SpawnReason.DISPENSE_EGG.ordinal()] = 17;
            } catch (NoSuchFieldError nosuchfielderror10) {
                ;
            }

            try {
                aint1[SpawnReason.DROWNED.ordinal()] = 26;
            } catch (NoSuchFieldError nosuchfielderror11) {
                ;
            }

            try {
                aint1[SpawnReason.DUPLICATION.ordinal()] = 36;
            } catch (NoSuchFieldError nosuchfielderror12) {
                ;
            }

            try {
                aint1[SpawnReason.EGG.ordinal()] = 5;
            } catch (NoSuchFieldError nosuchfielderror13) {
                ;
            }

            try {
                aint1[SpawnReason.ENDER_PEARL.ordinal()] = 24;
            } catch (NoSuchFieldError nosuchfielderror14) {
                ;
            }

            try {
                aint1[SpawnReason.EXPLOSION.ordinal()] = 28;
            } catch (NoSuchFieldError nosuchfielderror15) {
                ;
            }

            try {
                aint1[SpawnReason.FROZEN.ordinal()] = 34;
            } catch (NoSuchFieldError nosuchfielderror16) {
                ;
            }

            try {
                aint1[SpawnReason.INFECTION.ordinal()] = 18;
            } catch (NoSuchFieldError nosuchfielderror17) {
                ;
            }

            try {
                aint1[SpawnReason.JOCKEY.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror18) {
                ;
            }

            try {
                aint1[SpawnReason.LIGHTNING.ordinal()] = 7;
            } catch (NoSuchFieldError nosuchfielderror19) {
                ;
            }

            try {
                aint1[SpawnReason.METAMORPHOSIS.ordinal()] = 35;
            } catch (NoSuchFieldError nosuchfielderror20) {
                ;
            }

            try {
                aint1[SpawnReason.MOUNT.ordinal()] = 22;
            } catch (NoSuchFieldError nosuchfielderror21) {
                ;
            }

            try {
                aint1[SpawnReason.NATURAL.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror22) {
                ;
            }

            try {
                aint1[SpawnReason.NETHER_PORTAL.ordinal()] = 16;
            } catch (NoSuchFieldError nosuchfielderror23) {
                ;
            }

            try {
                aint1[SpawnReason.OCELOT_BABY.ordinal()] = 20;
            } catch (NoSuchFieldError nosuchfielderror24) {
                ;
            }

            try {
                aint1[SpawnReason.PATROL.ordinal()] = 30;
            } catch (NoSuchFieldError nosuchfielderror25) {
                ;
            }

            try {
                aint1[SpawnReason.PIGLIN_ZOMBIFIED.ordinal()] = 32;
            } catch (NoSuchFieldError nosuchfielderror26) {
                ;
            }

            try {
                aint1[SpawnReason.RAID.ordinal()] = 29;
            } catch (NoSuchFieldError nosuchfielderror27) {
                ;
            }

            try {
                aint1[SpawnReason.REINFORCEMENTS.ordinal()] = 15;
            } catch (NoSuchFieldError nosuchfielderror28) {
                ;
            }

            try {
                aint1[SpawnReason.SHEARED.ordinal()] = 27;
            } catch (NoSuchFieldError nosuchfielderror29) {
                ;
            }

            try {
                aint1[SpawnReason.SHOULDER_ENTITY.ordinal()] = 25;
            } catch (NoSuchFieldError nosuchfielderror30) {
                ;
            }

            try {
                aint1[SpawnReason.SILVERFISH_BLOCK.ordinal()] = 21;
            } catch (NoSuchFieldError nosuchfielderror31) {
                ;
            }

            try {
                aint1[SpawnReason.SLIME_SPLIT.ordinal()] = 14;
            } catch (NoSuchFieldError nosuchfielderror32) {
                ;
            }

            try {
                aint1[SpawnReason.SPAWNER.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror33) {
                ;
            }

            try {
                aint1[SpawnReason.SPAWNER_EGG.ordinal()] = 6;
            } catch (NoSuchFieldError nosuchfielderror34) {
                ;
            }

            try {
                aint1[SpawnReason.SPELL.ordinal()] = 33;
            } catch (NoSuchFieldError nosuchfielderror35) {
                ;
            }

            try {
                aint1[SpawnReason.TRAP.ordinal()] = 23;
            } catch (NoSuchFieldError nosuchfielderror36) {
                ;
            }

            try {
                aint1[SpawnReason.VILLAGE_DEFENSE.ordinal()] = 11;
            } catch (NoSuchFieldError nosuchfielderror37) {
                ;
            }

            try {
                aint1[SpawnReason.VILLAGE_INVASION.ordinal()] = 12;
            } catch (NoSuchFieldError nosuchfielderror38) {
                ;
            }

            CraftEventFactory.$SWITCH_TABLE$org$bukkit$event$entity$CreatureSpawnEvent$SpawnReason = aint1;
            return aint1;
        }
    }

    static int[] $SWITCH_TABLE$org$bukkit$Material() {
        int[] aint = CraftEventFactory.$SWITCH_TABLE$org$bukkit$Material;

        if (aint != null) {
            return aint;
        } else {
            int[] aint1 = new int[Material.values().length];

            try {
                aint1[Material.ACACIA_BOAT.ordinal()] = 745;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                aint1[Material.ACACIA_BUTTON.ordinal()] = 667;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            try {
                aint1[Material.ACACIA_CHEST_BOAT.ordinal()] = 746;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            try {
                aint1[Material.ACACIA_DOOR.ordinal()] = 694;
            } catch (NoSuchFieldError nosuchfielderror3) {
                ;
            }

            try {
                aint1[Material.ACACIA_FENCE.ordinal()] = 294;
            } catch (NoSuchFieldError nosuchfielderror4) {
                ;
            }

            try {
                aint1[Material.ACACIA_FENCE_GATE.ordinal()] = 717;
            } catch (NoSuchFieldError nosuchfielderror5) {
                ;
            }

            try {
                aint1[Material.ACACIA_HANGING_SIGN.ordinal()] = 862;
            } catch (NoSuchFieldError nosuchfielderror6) {
                ;
            }

            try {
                aint1[Material.ACACIA_LEAVES.ordinal()] = 159;
            } catch (NoSuchFieldError nosuchfielderror7) {
                ;
            }

            try {
                aint1[Material.ACACIA_LOG.ordinal()] = 115;
            } catch (NoSuchFieldError nosuchfielderror8) {
                ;
            }

            try {
                aint1[Material.ACACIA_PLANKS.ordinal()] = 28;
            } catch (NoSuchFieldError nosuchfielderror9) {
                ;
            }

            try {
                aint1[Material.ACACIA_PRESSURE_PLATE.ordinal()] = 682;
            } catch (NoSuchFieldError nosuchfielderror10) {
                ;
            }

            try {
                aint1[Material.ACACIA_SAPLING.ordinal()] = 40;
            } catch (NoSuchFieldError nosuchfielderror11) {
                ;
            }

            try {
                aint1[Material.ACACIA_SIGN.ordinal()] = 851;
            } catch (NoSuchFieldError nosuchfielderror12) {
                ;
            }

            try {
                aint1[Material.ACACIA_SLAB.ordinal()] = 235;
            } catch (NoSuchFieldError nosuchfielderror13) {
                ;
            }

            try {
                aint1[Material.ACACIA_STAIRS.ordinal()] = 366;
            } catch (NoSuchFieldError nosuchfielderror14) {
                ;
            }

            try {
                aint1[Material.ACACIA_TRAPDOOR.ordinal()] = 706;
            } catch (NoSuchFieldError nosuchfielderror15) {
                ;
            }

            try {
                aint1[Material.ACACIA_WALL_HANGING_SIGN.ordinal()] = 1277;
            } catch (NoSuchFieldError nosuchfielderror16) {
                ;
            }

            try {
                aint1[Material.ACACIA_WALL_SIGN.ordinal()] = 1268;
            } catch (NoSuchFieldError nosuchfielderror17) {
                ;
            }

            try {
                aint1[Material.ACACIA_WOOD.ordinal()] = 149;
            } catch (NoSuchFieldError nosuchfielderror18) {
                ;
            }

            try {
                aint1[Material.ACTIVATOR_RAIL.ordinal()] = 727;
            } catch (NoSuchFieldError nosuchfielderror19) {
                ;
            }

            try {
                aint1[Material.AIR.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror20) {
                ;
            }

            try {
                aint1[Material.ALLAY_SPAWN_EGG.ordinal()] = 968;
            } catch (NoSuchFieldError nosuchfielderror21) {
                ;
            }

            try {
                aint1[Material.ALLIUM.ordinal()] = 200;
            } catch (NoSuchFieldError nosuchfielderror22) {
                ;
            }

            try {
                aint1[Material.AMETHYST_BLOCK.ordinal()] = 73;
            } catch (NoSuchFieldError nosuchfielderror23) {
                ;
            }

            try {
                aint1[Material.AMETHYST_CLUSTER.ordinal()] = 1211;
            } catch (NoSuchFieldError nosuchfielderror24) {
                ;
            }

            try {
                aint1[Material.AMETHYST_SHARD.ordinal()] = 769;
            } catch (NoSuchFieldError nosuchfielderror25) {
                ;
            }

            try {
                aint1[Material.ANCIENT_DEBRIS.ordinal()] = 68;
            } catch (NoSuchFieldError nosuchfielderror26) {
                ;
            }

            try {
                aint1[Material.ANDESITE.ordinal()] = 7;
            } catch (NoSuchFieldError nosuchfielderror27) {
                ;
            }

            try {
                aint1[Material.ANDESITE_SLAB.ordinal()] = 627;
            } catch (NoSuchFieldError nosuchfielderror28) {
                ;
            }

            try {
                aint1[Material.ANDESITE_STAIRS.ordinal()] = 610;
            } catch (NoSuchFieldError nosuchfielderror29) {
                ;
            }

            try {
                aint1[Material.ANDESITE_WALL.ordinal()] = 386;
            } catch (NoSuchFieldError nosuchfielderror30) {
                ;
            }

            try {
                aint1[Material.ANGLER_POTTERY_SHERD.ordinal()] = 1236;
            } catch (NoSuchFieldError nosuchfielderror31) {
                ;
            }

            try {
                aint1[Material.ANVIL.ordinal()] = 398;
            } catch (NoSuchFieldError nosuchfielderror32) {
                ;
            }

            try {
                aint1[Material.APPLE.ordinal()] = 760;
            } catch (NoSuchFieldError nosuchfielderror33) {
                ;
            }

            try {
                aint1[Material.ARCHER_POTTERY_SHERD.ordinal()] = 1237;
            } catch (NoSuchFieldError nosuchfielderror34) {
                ;
            }

            try {
                aint1[Material.ARMOR_STAND.ordinal()] = 1078;
            } catch (NoSuchFieldError nosuchfielderror35) {
                ;
            }

            try {
                aint1[Material.ARMS_UP_POTTERY_SHERD.ordinal()] = 1238;
            } catch (NoSuchFieldError nosuchfielderror36) {
                ;
            }

            try {
                aint1[Material.ARROW.ordinal()] = 762;
            } catch (NoSuchFieldError nosuchfielderror37) {
                ;
            }

            try {
                aint1[Material.ATTACHED_MELON_STEM.ordinal()] = 1289;
            } catch (NoSuchFieldError nosuchfielderror38) {
                ;
            }

            try {
                aint1[Material.ATTACHED_PUMPKIN_STEM.ordinal()] = 1288;
            } catch (NoSuchFieldError nosuchfielderror39) {
                ;
            }

            try {
                aint1[Material.AXOLOTL_BUCKET.ordinal()] = 880;
            } catch (NoSuchFieldError nosuchfielderror40) {
                ;
            }

            try {
                aint1[Material.AXOLOTL_SPAWN_EGG.ordinal()] = 969;
            } catch (NoSuchFieldError nosuchfielderror41) {
                ;
            }

            try {
                aint1[Material.AZALEA.ordinal()] = 176;
            } catch (NoSuchFieldError nosuchfielderror42) {
                ;
            }

            try {
                aint1[Material.AZALEA_LEAVES.ordinal()] = 163;
            } catch (NoSuchFieldError nosuchfielderror43) {
                ;
            }

            try {
                aint1[Material.AZURE_BLUET.ordinal()] = 201;
            } catch (NoSuchFieldError nosuchfielderror44) {
                ;
            }

            try {
                aint1[Material.BAKED_POTATO.ordinal()] = 1054;
            } catch (NoSuchFieldError nosuchfielderror45) {
                ;
            }

            try {
                aint1[Material.BAMBOO.ordinal()] = 230;
            } catch (NoSuchFieldError nosuchfielderror46) {
                ;
            }

            try {
                aint1[Material.BAMBOO_BLOCK.ordinal()] = 123;
            } catch (NoSuchFieldError nosuchfielderror47) {
                ;
            }

            try {
                aint1[Material.BAMBOO_BUTTON.ordinal()] = 671;
            } catch (NoSuchFieldError nosuchfielderror48) {
                ;
            }

            try {
                aint1[Material.BAMBOO_CHEST_RAFT.ordinal()] = 754;
            } catch (NoSuchFieldError nosuchfielderror49) {
                ;
            }

            try {
                aint1[Material.BAMBOO_DOOR.ordinal()] = 698;
            } catch (NoSuchFieldError nosuchfielderror50) {
                ;
            }

            try {
                aint1[Material.BAMBOO_FENCE.ordinal()] = 298;
            } catch (NoSuchFieldError nosuchfielderror51) {
                ;
            }

            try {
                aint1[Material.BAMBOO_FENCE_GATE.ordinal()] = 721;
            } catch (NoSuchFieldError nosuchfielderror52) {
                ;
            }

            try {
                aint1[Material.BAMBOO_HANGING_SIGN.ordinal()] = 866;
            } catch (NoSuchFieldError nosuchfielderror53) {
                ;
            }

            try {
                aint1[Material.BAMBOO_MOSAIC.ordinal()] = 35;
            } catch (NoSuchFieldError nosuchfielderror54) {
                ;
            }

            try {
                aint1[Material.BAMBOO_MOSAIC_SLAB.ordinal()] = 240;
            } catch (NoSuchFieldError nosuchfielderror55) {
                ;
            }

            try {
                aint1[Material.BAMBOO_MOSAIC_STAIRS.ordinal()] = 371;
            } catch (NoSuchFieldError nosuchfielderror56) {
                ;
            }

            try {
                aint1[Material.BAMBOO_PLANKS.ordinal()] = 32;
            } catch (NoSuchFieldError nosuchfielderror57) {
                ;
            }

            try {
                aint1[Material.BAMBOO_PRESSURE_PLATE.ordinal()] = 686;
            } catch (NoSuchFieldError nosuchfielderror58) {
                ;
            }

            try {
                aint1[Material.BAMBOO_RAFT.ordinal()] = 753;
            } catch (NoSuchFieldError nosuchfielderror59) {
                ;
            }

            try {
                aint1[Material.BAMBOO_SAPLING.ordinal()] = 1366;
            } catch (NoSuchFieldError nosuchfielderror60) {
                ;
            }

            try {
                aint1[Material.BAMBOO_SIGN.ordinal()] = 855;
            } catch (NoSuchFieldError nosuchfielderror61) {
                ;
            }

            try {
                aint1[Material.BAMBOO_SLAB.ordinal()] = 239;
            } catch (NoSuchFieldError nosuchfielderror62) {
                ;
            }

            try {
                aint1[Material.BAMBOO_STAIRS.ordinal()] = 370;
            } catch (NoSuchFieldError nosuchfielderror63) {
                ;
            }

            try {
                aint1[Material.BAMBOO_TRAPDOOR.ordinal()] = 710;
            } catch (NoSuchFieldError nosuchfielderror64) {
                ;
            }

            try {
                aint1[Material.BAMBOO_WALL_HANGING_SIGN.ordinal()] = 1284;
            } catch (NoSuchFieldError nosuchfielderror65) {
                ;
            }

            try {
                aint1[Material.BAMBOO_WALL_SIGN.ordinal()] = 1273;
            } catch (NoSuchFieldError nosuchfielderror66) {
                ;
            }

            try {
                aint1[Material.BARREL.ordinal()] = 1155;
            } catch (NoSuchFieldError nosuchfielderror67) {
                ;
            }

            try {
                aint1[Material.BARRIER.ordinal()] = 422;
            } catch (NoSuchFieldError nosuchfielderror68) {
                ;
            }

            try {
                aint1[Material.BASALT.ordinal()] = 307;
            } catch (NoSuchFieldError nosuchfielderror69) {
                ;
            }

            try {
                aint1[Material.BAT_SPAWN_EGG.ordinal()] = 970;
            } catch (NoSuchFieldError nosuchfielderror70) {
                ;
            }

            try {
                aint1[Material.BEACON.ordinal()] = 375;
            } catch (NoSuchFieldError nosuchfielderror71) {
                ;
            }

            try {
                aint1[Material.BEDROCK.ordinal()] = 44;
            } catch (NoSuchFieldError nosuchfielderror72) {
                ;
            }

            try {
                aint1[Material.BEEF.ordinal()] = 948;
            } catch (NoSuchFieldError nosuchfielderror73) {
                ;
            }

            try {
                aint1[Material.BEEHIVE.ordinal()] = 1173;
            } catch (NoSuchFieldError nosuchfielderror74) {
                ;
            }

            try {
                aint1[Material.BEETROOT.ordinal()] = 1109;
            } catch (NoSuchFieldError nosuchfielderror75) {
                ;
            }

            try {
                aint1[Material.BEETROOTS.ordinal()] = 1352;
            } catch (NoSuchFieldError nosuchfielderror76) {
                ;
            }

            try {
                aint1[Material.BEETROOT_SEEDS.ordinal()] = 1110;
            } catch (NoSuchFieldError nosuchfielderror77) {
                ;
            }

            try {
                aint1[Material.BEETROOT_SOUP.ordinal()] = 1111;
            } catch (NoSuchFieldError nosuchfielderror78) {
                ;
            }

            try {
                aint1[Material.BEE_NEST.ordinal()] = 1172;
            } catch (NoSuchFieldError nosuchfielderror79) {
                ;
            }

            try {
                aint1[Material.BEE_SPAWN_EGG.ordinal()] = 971;
            } catch (NoSuchFieldError nosuchfielderror80) {
                ;
            }

            try {
                aint1[Material.BELL.ordinal()] = 1163;
            } catch (NoSuchFieldError nosuchfielderror81) {
                ;
            }

            try {
                aint1[Material.BIG_DRIPLEAF.ordinal()] = 228;
            } catch (NoSuchFieldError nosuchfielderror82) {
                ;
            }

            try {
                aint1[Material.BIG_DRIPLEAF_STEM.ordinal()] = 1400;
            } catch (NoSuchFieldError nosuchfielderror83) {
                ;
            }

            try {
                aint1[Material.BIRCH_BOAT.ordinal()] = 741;
            } catch (NoSuchFieldError nosuchfielderror84) {
                ;
            }

            try {
                aint1[Material.BIRCH_BUTTON.ordinal()] = 665;
            } catch (NoSuchFieldError nosuchfielderror85) {
                ;
            }

            try {
                aint1[Material.BIRCH_CHEST_BOAT.ordinal()] = 742;
            } catch (NoSuchFieldError nosuchfielderror86) {
                ;
            }

            try {
                aint1[Material.BIRCH_DOOR.ordinal()] = 692;
            } catch (NoSuchFieldError nosuchfielderror87) {
                ;
            }

            try {
                aint1[Material.BIRCH_FENCE.ordinal()] = 292;
            } catch (NoSuchFieldError nosuchfielderror88) {
                ;
            }

            try {
                aint1[Material.BIRCH_FENCE_GATE.ordinal()] = 715;
            } catch (NoSuchFieldError nosuchfielderror89) {
                ;
            }

            try {
                aint1[Material.BIRCH_HANGING_SIGN.ordinal()] = 860;
            } catch (NoSuchFieldError nosuchfielderror90) {
                ;
            }

            try {
                aint1[Material.BIRCH_LEAVES.ordinal()] = 157;
            } catch (NoSuchFieldError nosuchfielderror91) {
                ;
            }

            try {
                aint1[Material.BIRCH_LOG.ordinal()] = 113;
            } catch (NoSuchFieldError nosuchfielderror92) {
                ;
            }

            try {
                aint1[Material.BIRCH_PLANKS.ordinal()] = 26;
            } catch (NoSuchFieldError nosuchfielderror93) {
                ;
            }

            try {
                aint1[Material.BIRCH_PRESSURE_PLATE.ordinal()] = 680;
            } catch (NoSuchFieldError nosuchfielderror94) {
                ;
            }

            try {
                aint1[Material.BIRCH_SAPLING.ordinal()] = 38;
            } catch (NoSuchFieldError nosuchfielderror95) {
                ;
            }

            try {
                aint1[Material.BIRCH_SIGN.ordinal()] = 849;
            } catch (NoSuchFieldError nosuchfielderror96) {
                ;
            }

            try {
                aint1[Material.BIRCH_SLAB.ordinal()] = 233;
            } catch (NoSuchFieldError nosuchfielderror97) {
                ;
            }

            try {
                aint1[Material.BIRCH_STAIRS.ordinal()] = 364;
            } catch (NoSuchFieldError nosuchfielderror98) {
                ;
            }

            try {
                aint1[Material.BIRCH_TRAPDOOR.ordinal()] = 704;
            } catch (NoSuchFieldError nosuchfielderror99) {
                ;
            }

            try {
                aint1[Material.BIRCH_WALL_HANGING_SIGN.ordinal()] = 1276;
            } catch (NoSuchFieldError nosuchfielderror100) {
                ;
            }

            try {
                aint1[Material.BIRCH_WALL_SIGN.ordinal()] = 1267;
            } catch (NoSuchFieldError nosuchfielderror101) {
                ;
            }

            try {
                aint1[Material.BIRCH_WOOD.ordinal()] = 147;
            } catch (NoSuchFieldError nosuchfielderror102) {
                ;
            }

            try {
                aint1[Material.BLACKSTONE.ordinal()] = 1178;
            } catch (NoSuchFieldError nosuchfielderror103) {
                ;
            }

            try {
                aint1[Material.BLACKSTONE_SLAB.ordinal()] = 1179;
            } catch (NoSuchFieldError nosuchfielderror104) {
                ;
            }

            try {
                aint1[Material.BLACKSTONE_STAIRS.ordinal()] = 1180;
            } catch (NoSuchFieldError nosuchfielderror105) {
                ;
            }

            try {
                aint1[Material.BLACKSTONE_WALL.ordinal()] = 391;
            } catch (NoSuchFieldError nosuchfielderror106) {
                ;
            }

            try {
                aint1[Material.BLACK_BANNER.ordinal()] = 1103;
            } catch (NoSuchFieldError nosuchfielderror107) {
                ;
            }

            try {
                aint1[Material.BLACK_BED.ordinal()] = 940;
            } catch (NoSuchFieldError nosuchfielderror108) {
                ;
            }

            try {
                aint1[Material.BLACK_CANDLE.ordinal()] = 1207;
            } catch (NoSuchFieldError nosuchfielderror109) {
                ;
            }

            try {
                aint1[Material.BLACK_CANDLE_CAKE.ordinal()] = 1396;
            } catch (NoSuchFieldError nosuchfielderror110) {
                ;
            }

            try {
                aint1[Material.BLACK_CARPET.ordinal()] = 440;
            } catch (NoSuchFieldError nosuchfielderror111) {
                ;
            }

            try {
                aint1[Material.BLACK_CONCRETE.ordinal()] = 549;
            } catch (NoSuchFieldError nosuchfielderror112) {
                ;
            }

            try {
                aint1[Material.BLACK_CONCRETE_POWDER.ordinal()] = 565;
            } catch (NoSuchFieldError nosuchfielderror113) {
                ;
            }

            try {
                aint1[Material.BLACK_DYE.ordinal()] = 920;
            } catch (NoSuchFieldError nosuchfielderror114) {
                ;
            }

            try {
                aint1[Material.BLACK_GLAZED_TERRACOTTA.ordinal()] = 533;
            } catch (NoSuchFieldError nosuchfielderror115) {
                ;
            }

            try {
                aint1[Material.BLACK_SHULKER_BOX.ordinal()] = 517;
            } catch (NoSuchFieldError nosuchfielderror116) {
                ;
            }

            try {
                aint1[Material.BLACK_STAINED_GLASS.ordinal()] = 465;
            } catch (NoSuchFieldError nosuchfielderror117) {
                ;
            }

            try {
                aint1[Material.BLACK_STAINED_GLASS_PANE.ordinal()] = 481;
            } catch (NoSuchFieldError nosuchfielderror118) {
                ;
            }

            try {
                aint1[Material.BLACK_TERRACOTTA.ordinal()] = 421;
            } catch (NoSuchFieldError nosuchfielderror119) {
                ;
            }

            try {
                aint1[Material.BLACK_WALL_BANNER.ordinal()] = 1349;
            } catch (NoSuchFieldError nosuchfielderror120) {
                ;
            }

            try {
                aint1[Material.BLACK_WOOL.ordinal()] = 196;
            } catch (NoSuchFieldError nosuchfielderror121) {
                ;
            }

            try {
                aint1[Material.BLADE_POTTERY_SHERD.ordinal()] = 1239;
            } catch (NoSuchFieldError nosuchfielderror122) {
                ;
            }

            try {
                aint1[Material.BLAST_FURNACE.ordinal()] = 1157;
            } catch (NoSuchFieldError nosuchfielderror123) {
                ;
            }

            try {
                aint1[Material.BLAZE_POWDER.ordinal()] = 962;
            } catch (NoSuchFieldError nosuchfielderror124) {
                ;
            }

            try {
                aint1[Material.BLAZE_ROD.ordinal()] = 954;
            } catch (NoSuchFieldError nosuchfielderror125) {
                ;
            }

            try {
                aint1[Material.BLAZE_SPAWN_EGG.ordinal()] = 972;
            } catch (NoSuchFieldError nosuchfielderror126) {
                ;
            }

            try {
                aint1[Material.BLUE_BANNER.ordinal()] = 1099;
            } catch (NoSuchFieldError nosuchfielderror127) {
                ;
            }

            try {
                aint1[Material.BLUE_BED.ordinal()] = 936;
            } catch (NoSuchFieldError nosuchfielderror128) {
                ;
            }

            try {
                aint1[Material.BLUE_CANDLE.ordinal()] = 1203;
            } catch (NoSuchFieldError nosuchfielderror129) {
                ;
            }

            try {
                aint1[Material.BLUE_CANDLE_CAKE.ordinal()] = 1392;
            } catch (NoSuchFieldError nosuchfielderror130) {
                ;
            }

            try {
                aint1[Material.BLUE_CARPET.ordinal()] = 436;
            } catch (NoSuchFieldError nosuchfielderror131) {
                ;
            }

            try {
                aint1[Material.BLUE_CONCRETE.ordinal()] = 545;
            } catch (NoSuchFieldError nosuchfielderror132) {
                ;
            }

            try {
                aint1[Material.BLUE_CONCRETE_POWDER.ordinal()] = 561;
            } catch (NoSuchFieldError nosuchfielderror133) {
                ;
            }

            try {
                aint1[Material.BLUE_DYE.ordinal()] = 916;
            } catch (NoSuchFieldError nosuchfielderror134) {
                ;
            }

            try {
                aint1[Material.BLUE_GLAZED_TERRACOTTA.ordinal()] = 529;
            } catch (NoSuchFieldError nosuchfielderror135) {
                ;
            }

            try {
                aint1[Material.BLUE_ICE.ordinal()] = 598;
            } catch (NoSuchFieldError nosuchfielderror136) {
                ;
            }

            try {
                aint1[Material.BLUE_ORCHID.ordinal()] = 199;
            } catch (NoSuchFieldError nosuchfielderror137) {
                ;
            }

            try {
                aint1[Material.BLUE_SHULKER_BOX.ordinal()] = 513;
            } catch (NoSuchFieldError nosuchfielderror138) {
                ;
            }

            try {
                aint1[Material.BLUE_STAINED_GLASS.ordinal()] = 461;
            } catch (NoSuchFieldError nosuchfielderror139) {
                ;
            }

            try {
                aint1[Material.BLUE_STAINED_GLASS_PANE.ordinal()] = 477;
            } catch (NoSuchFieldError nosuchfielderror140) {
                ;
            }

            try {
                aint1[Material.BLUE_TERRACOTTA.ordinal()] = 417;
            } catch (NoSuchFieldError nosuchfielderror141) {
                ;
            }

            try {
                aint1[Material.BLUE_WALL_BANNER.ordinal()] = 1345;
            } catch (NoSuchFieldError nosuchfielderror142) {
                ;
            }

            try {
                aint1[Material.BLUE_WOOL.ordinal()] = 192;
            } catch (NoSuchFieldError nosuchfielderror143) {
                ;
            }

            try {
                aint1[Material.BONE.ordinal()] = 922;
            } catch (NoSuchFieldError nosuchfielderror144) {
                ;
            }

            try {
                aint1[Material.BONE_BLOCK.ordinal()] = 499;
            } catch (NoSuchFieldError nosuchfielderror145) {
                ;
            }

            try {
                aint1[Material.BONE_MEAL.ordinal()] = 921;
            } catch (NoSuchFieldError nosuchfielderror146) {
                ;
            }

            try {
                aint1[Material.BOOK.ordinal()] = 886;
            } catch (NoSuchFieldError nosuchfielderror147) {
                ;
            }

            try {
                aint1[Material.BOOKSHELF.ordinal()] = 265;
            } catch (NoSuchFieldError nosuchfielderror148) {
                ;
            }

            try {
                aint1[Material.BOW.ordinal()] = 761;
            } catch (NoSuchFieldError nosuchfielderror149) {
                ;
            }

            try {
                aint1[Material.BOWL.ordinal()] = 809;
            } catch (NoSuchFieldError nosuchfielderror150) {
                ;
            }

            try {
                aint1[Material.BRAIN_CORAL.ordinal()] = 579;
            } catch (NoSuchFieldError nosuchfielderror151) {
                ;
            }

            try {
                aint1[Material.BRAIN_CORAL_BLOCK.ordinal()] = 574;
            } catch (NoSuchFieldError nosuchfielderror152) {
                ;
            }

            try {
                aint1[Material.BRAIN_CORAL_FAN.ordinal()] = 589;
            } catch (NoSuchFieldError nosuchfielderror153) {
                ;
            }

            try {
                aint1[Material.BRAIN_CORAL_WALL_FAN.ordinal()] = 1362;
            } catch (NoSuchFieldError nosuchfielderror154) {
                ;
            }

            try {
                aint1[Material.BREAD.ordinal()] = 816;
            } catch (NoSuchFieldError nosuchfielderror155) {
                ;
            }

            try {
                aint1[Material.BREWER_POTTERY_SHERD.ordinal()] = 1240;
            } catch (NoSuchFieldError nosuchfielderror156) {
                ;
            }

            try {
                aint1[Material.BREWING_STAND.ordinal()] = 964;
            } catch (NoSuchFieldError nosuchfielderror157) {
                ;
            }

            try {
                aint1[Material.BRICK.ordinal()] = 882;
            } catch (NoSuchFieldError nosuchfielderror158) {
                ;
            }

            try {
                aint1[Material.BRICKS.ordinal()] = 264;
            } catch (NoSuchFieldError nosuchfielderror159) {
                ;
            }

            try {
                aint1[Material.BRICK_SLAB.ordinal()] = 249;
            } catch (NoSuchFieldError nosuchfielderror160) {
                ;
            }

            try {
                aint1[Material.BRICK_STAIRS.ordinal()] = 340;
            } catch (NoSuchFieldError nosuchfielderror161) {
                ;
            }

            try {
                aint1[Material.BRICK_WALL.ordinal()] = 378;
            } catch (NoSuchFieldError nosuchfielderror162) {
                ;
            }

            try {
                aint1[Material.BROWN_BANNER.ordinal()] = 1100;
            } catch (NoSuchFieldError nosuchfielderror163) {
                ;
            }

            try {
                aint1[Material.BROWN_BED.ordinal()] = 937;
            } catch (NoSuchFieldError nosuchfielderror164) {
                ;
            }

            try {
                aint1[Material.BROWN_CANDLE.ordinal()] = 1204;
            } catch (NoSuchFieldError nosuchfielderror165) {
                ;
            }

            try {
                aint1[Material.BROWN_CANDLE_CAKE.ordinal()] = 1393;
            } catch (NoSuchFieldError nosuchfielderror166) {
                ;
            }

            try {
                aint1[Material.BROWN_CARPET.ordinal()] = 437;
            } catch (NoSuchFieldError nosuchfielderror167) {
                ;
            }

            try {
                aint1[Material.BROWN_CONCRETE.ordinal()] = 546;
            } catch (NoSuchFieldError nosuchfielderror168) {
                ;
            }

            try {
                aint1[Material.BROWN_CONCRETE_POWDER.ordinal()] = 562;
            } catch (NoSuchFieldError nosuchfielderror169) {
                ;
            }

            try {
                aint1[Material.BROWN_DYE.ordinal()] = 917;
            } catch (NoSuchFieldError nosuchfielderror170) {
                ;
            }

            try {
                aint1[Material.BROWN_GLAZED_TERRACOTTA.ordinal()] = 530;
            } catch (NoSuchFieldError nosuchfielderror171) {
                ;
            }

            try {
                aint1[Material.BROWN_MUSHROOM.ordinal()] = 213;
            } catch (NoSuchFieldError nosuchfielderror172) {
                ;
            }

            try {
                aint1[Material.BROWN_MUSHROOM_BLOCK.ordinal()] = 331;
            } catch (NoSuchFieldError nosuchfielderror173) {
                ;
            }

            try {
                aint1[Material.BROWN_SHULKER_BOX.ordinal()] = 514;
            } catch (NoSuchFieldError nosuchfielderror174) {
                ;
            }

            try {
                aint1[Material.BROWN_STAINED_GLASS.ordinal()] = 462;
            } catch (NoSuchFieldError nosuchfielderror175) {
                ;
            }

            try {
                aint1[Material.BROWN_STAINED_GLASS_PANE.ordinal()] = 478;
            } catch (NoSuchFieldError nosuchfielderror176) {
                ;
            }

            try {
                aint1[Material.BROWN_TERRACOTTA.ordinal()] = 418;
            } catch (NoSuchFieldError nosuchfielderror177) {
                ;
            }

            try {
                aint1[Material.BROWN_WALL_BANNER.ordinal()] = 1346;
            } catch (NoSuchFieldError nosuchfielderror178) {
                ;
            }

            try {
                aint1[Material.BROWN_WOOL.ordinal()] = 193;
            } catch (NoSuchFieldError nosuchfielderror179) {
                ;
            }

            try {
                aint1[Material.BRUSH.ordinal()] = 1218;
            } catch (NoSuchFieldError nosuchfielderror180) {
                ;
            }

            try {
                aint1[Material.BUBBLE_COLUMN.ordinal()] = 1370;
            } catch (NoSuchFieldError nosuchfielderror181) {
                ;
            }

            try {
                aint1[Material.BUBBLE_CORAL.ordinal()] = 580;
            } catch (NoSuchFieldError nosuchfielderror182) {
                ;
            }

            try {
                aint1[Material.BUBBLE_CORAL_BLOCK.ordinal()] = 575;
            } catch (NoSuchFieldError nosuchfielderror183) {
                ;
            }

            try {
                aint1[Material.BUBBLE_CORAL_FAN.ordinal()] = 590;
            } catch (NoSuchFieldError nosuchfielderror184) {
                ;
            }

            try {
                aint1[Material.BUBBLE_CORAL_WALL_FAN.ordinal()] = 1363;
            } catch (NoSuchFieldError nosuchfielderror185) {
                ;
            }

            try {
                aint1[Material.BUCKET.ordinal()] = 869;
            } catch (NoSuchFieldError nosuchfielderror186) {
                ;
            }

            try {
                aint1[Material.BUDDING_AMETHYST.ordinal()] = 74;
            } catch (NoSuchFieldError nosuchfielderror187) {
                ;
            }

            try {
                aint1[Material.BUNDLE.ordinal()] = 891;
            } catch (NoSuchFieldError nosuchfielderror188) {
                ;
            }

            try {
                aint1[Material.BURN_POTTERY_SHERD.ordinal()] = 1241;
            } catch (NoSuchFieldError nosuchfielderror189) {
                ;
            }

            try {
                aint1[Material.CACTUS.ordinal()] = 287;
            } catch (NoSuchFieldError nosuchfielderror190) {
                ;
            }

            try {
                aint1[Material.CAKE.ordinal()] = 924;
            } catch (NoSuchFieldError nosuchfielderror191) {
                ;
            }

            try {
                aint1[Material.CALCITE.ordinal()] = 12;
            } catch (NoSuchFieldError nosuchfielderror192) {
                ;
            }

            try {
                aint1[Material.CALIBRATED_SCULK_SENSOR.ordinal()] = 655;
            } catch (NoSuchFieldError nosuchfielderror193) {
                ;
            }

            try {
                aint1[Material.CAMEL_SPAWN_EGG.ordinal()] = 974;
            } catch (NoSuchFieldError nosuchfielderror194) {
                ;
            }

            try {
                aint1[Material.CAMPFIRE.ordinal()] = 1168;
            } catch (NoSuchFieldError nosuchfielderror195) {
                ;
            }

            try {
                aint1[Material.CANDLE.ordinal()] = 1191;
            } catch (NoSuchFieldError nosuchfielderror196) {
                ;
            }

            try {
                aint1[Material.CANDLE_CAKE.ordinal()] = 1380;
            } catch (NoSuchFieldError nosuchfielderror197) {
                ;
            }

            try {
                aint1[Material.CARROT.ordinal()] = 1052;
            } catch (NoSuchFieldError nosuchfielderror198) {
                ;
            }

            try {
                aint1[Material.CARROTS.ordinal()] = 1325;
            } catch (NoSuchFieldError nosuchfielderror199) {
                ;
            }

            try {
                aint1[Material.CARROT_ON_A_STICK.ordinal()] = 734;
            } catch (NoSuchFieldError nosuchfielderror200) {
                ;
            }

            try {
                aint1[Material.CARTOGRAPHY_TABLE.ordinal()] = 1158;
            } catch (NoSuchFieldError nosuchfielderror201) {
                ;
            }

            try {
                aint1[Material.CARVED_PUMPKIN.ordinal()] = 302;
            } catch (NoSuchFieldError nosuchfielderror202) {
                ;
            }

            try {
                aint1[Material.CAT_SPAWN_EGG.ordinal()] = 973;
            } catch (NoSuchFieldError nosuchfielderror203) {
                ;
            }

            try {
                aint1[Material.CAULDRON.ordinal()] = 965;
            } catch (NoSuchFieldError nosuchfielderror204) {
                ;
            }

            try {
                aint1[Material.CAVE_AIR.ordinal()] = 1369;
            } catch (NoSuchFieldError nosuchfielderror205) {
                ;
            }

            try {
                aint1[Material.CAVE_SPIDER_SPAWN_EGG.ordinal()] = 975;
            } catch (NoSuchFieldError nosuchfielderror206) {
                ;
            }

            try {
                aint1[Material.CAVE_VINES.ordinal()] = 1398;
            } catch (NoSuchFieldError nosuchfielderror207) {
                ;
            }

            try {
                aint1[Material.CAVE_VINES_PLANT.ordinal()] = 1399;
            } catch (NoSuchFieldError nosuchfielderror208) {
                ;
            }

            try {
                aint1[Material.CHAIN.ordinal()] = 335;
            } catch (NoSuchFieldError nosuchfielderror209) {
                ;
            }

            try {
                aint1[Material.CHAINMAIL_BOOTS.ordinal()] = 824;
            } catch (NoSuchFieldError nosuchfielderror210) {
                ;
            }

            try {
                aint1[Material.CHAINMAIL_CHESTPLATE.ordinal()] = 822;
            } catch (NoSuchFieldError nosuchfielderror211) {
                ;
            }

            try {
                aint1[Material.CHAINMAIL_HELMET.ordinal()] = 821;
            } catch (NoSuchFieldError nosuchfielderror212) {
                ;
            }

            try {
                aint1[Material.CHAINMAIL_LEGGINGS.ordinal()] = 823;
            } catch (NoSuchFieldError nosuchfielderror213) {
                ;
            }

            try {
                aint1[Material.CHAIN_COMMAND_BLOCK.ordinal()] = 494;
            } catch (NoSuchFieldError nosuchfielderror214) {
                ;
            }

            try {
                aint1[Material.CHARCOAL.ordinal()] = 764;
            } catch (NoSuchFieldError nosuchfielderror215) {
                ;
            }

            try {
                aint1[Material.CHERRY_BOAT.ordinal()] = 747;
            } catch (NoSuchFieldError nosuchfielderror216) {
                ;
            }

            try {
                aint1[Material.CHERRY_BUTTON.ordinal()] = 668;
            } catch (NoSuchFieldError nosuchfielderror217) {
                ;
            }

            try {
                aint1[Material.CHERRY_CHEST_BOAT.ordinal()] = 748;
            } catch (NoSuchFieldError nosuchfielderror218) {
                ;
            }

            try {
                aint1[Material.CHERRY_DOOR.ordinal()] = 695;
            } catch (NoSuchFieldError nosuchfielderror219) {
                ;
            }

            try {
                aint1[Material.CHERRY_FENCE.ordinal()] = 295;
            } catch (NoSuchFieldError nosuchfielderror220) {
                ;
            }

            try {
                aint1[Material.CHERRY_FENCE_GATE.ordinal()] = 718;
            } catch (NoSuchFieldError nosuchfielderror221) {
                ;
            }

            try {
                aint1[Material.CHERRY_HANGING_SIGN.ordinal()] = 863;
            } catch (NoSuchFieldError nosuchfielderror222) {
                ;
            }

            try {
                aint1[Material.CHERRY_LEAVES.ordinal()] = 160;
            } catch (NoSuchFieldError nosuchfielderror223) {
                ;
            }

            try {
                aint1[Material.CHERRY_LOG.ordinal()] = 116;
            } catch (NoSuchFieldError nosuchfielderror224) {
                ;
            }

            try {
                aint1[Material.CHERRY_PLANKS.ordinal()] = 29;
            } catch (NoSuchFieldError nosuchfielderror225) {
                ;
            }

            try {
                aint1[Material.CHERRY_PRESSURE_PLATE.ordinal()] = 683;
            } catch (NoSuchFieldError nosuchfielderror226) {
                ;
            }

            try {
                aint1[Material.CHERRY_SAPLING.ordinal()] = 41;
            } catch (NoSuchFieldError nosuchfielderror227) {
                ;
            }

            try {
                aint1[Material.CHERRY_SIGN.ordinal()] = 852;
            } catch (NoSuchFieldError nosuchfielderror228) {
                ;
            }

            try {
                aint1[Material.CHERRY_SLAB.ordinal()] = 236;
            } catch (NoSuchFieldError nosuchfielderror229) {
                ;
            }

            try {
                aint1[Material.CHERRY_STAIRS.ordinal()] = 367;
            } catch (NoSuchFieldError nosuchfielderror230) {
                ;
            }

            try {
                aint1[Material.CHERRY_TRAPDOOR.ordinal()] = 707;
            } catch (NoSuchFieldError nosuchfielderror231) {
                ;
            }

            try {
                aint1[Material.CHERRY_WALL_HANGING_SIGN.ordinal()] = 1278;
            } catch (NoSuchFieldError nosuchfielderror232) {
                ;
            }

            try {
                aint1[Material.CHERRY_WALL_SIGN.ordinal()] = 1269;
            } catch (NoSuchFieldError nosuchfielderror233) {
                ;
            }

            try {
                aint1[Material.CHERRY_WOOD.ordinal()] = 150;
            } catch (NoSuchFieldError nosuchfielderror234) {
                ;
            }

            try {
                aint1[Material.CHEST.ordinal()] = 278;
            } catch (NoSuchFieldError nosuchfielderror235) {
                ;
            }

            try {
                aint1[Material.CHEST_MINECART.ordinal()] = 730;
            } catch (NoSuchFieldError nosuchfielderror236) {
                ;
            }

            try {
                aint1[Material.CHICKEN.ordinal()] = 950;
            } catch (NoSuchFieldError nosuchfielderror237) {
                ;
            }

            try {
                aint1[Material.CHICKEN_SPAWN_EGG.ordinal()] = 976;
            } catch (NoSuchFieldError nosuchfielderror238) {
                ;
            }

            try {
                aint1[Material.CHIPPED_ANVIL.ordinal()] = 399;
            } catch (NoSuchFieldError nosuchfielderror239) {
                ;
            }

            try {
                aint1[Material.CHISELED_BOOKSHELF.ordinal()] = 266;
            } catch (NoSuchFieldError nosuchfielderror240) {
                ;
            }

            try {
                aint1[Material.CHISELED_DEEPSLATE.ordinal()] = 329;
            } catch (NoSuchFieldError nosuchfielderror241) {
                ;
            }

            try {
                aint1[Material.CHISELED_NETHER_BRICKS.ordinal()] = 347;
            } catch (NoSuchFieldError nosuchfielderror242) {
                ;
            }

            try {
                aint1[Material.CHISELED_POLISHED_BLACKSTONE.ordinal()] = 1185;
            } catch (NoSuchFieldError nosuchfielderror243) {
                ;
            }

            try {
                aint1[Material.CHISELED_QUARTZ_BLOCK.ordinal()] = 401;
            } catch (NoSuchFieldError nosuchfielderror244) {
                ;
            }

            try {
                aint1[Material.CHISELED_RED_SANDSTONE.ordinal()] = 490;
            } catch (NoSuchFieldError nosuchfielderror245) {
                ;
            }

            try {
                aint1[Material.CHISELED_SANDSTONE.ordinal()] = 171;
            } catch (NoSuchFieldError nosuchfielderror246) {
                ;
            }

            try {
                aint1[Material.CHISELED_STONE_BRICKS.ordinal()] = 322;
            } catch (NoSuchFieldError nosuchfielderror247) {
                ;
            }

            try {
                aint1[Material.CHORUS_FLOWER.ordinal()] = 273;
            } catch (NoSuchFieldError nosuchfielderror248) {
                ;
            }

            try {
                aint1[Material.CHORUS_FRUIT.ordinal()] = 1105;
            } catch (NoSuchFieldError nosuchfielderror249) {
                ;
            }

            try {
                aint1[Material.CHORUS_PLANT.ordinal()] = 272;
            } catch (NoSuchFieldError nosuchfielderror250) {
                ;
            }

            try {
                aint1[Material.CLAY.ordinal()] = 288;
            } catch (NoSuchFieldError nosuchfielderror251) {
                ;
            }

            try {
                aint1[Material.CLAY_BALL.ordinal()] = 883;
            } catch (NoSuchFieldError nosuchfielderror252) {
                ;
            }

            try {
                aint1[Material.CLOCK.ordinal()] = 893;
            } catch (NoSuchFieldError nosuchfielderror253) {
                ;
            }

            try {
                aint1[Material.COAL.ordinal()] = 763;
            } catch (NoSuchFieldError nosuchfielderror254) {
                ;
            }

            try {
                aint1[Material.COAL_BLOCK.ordinal()] = 69;
            } catch (NoSuchFieldError nosuchfielderror255) {
                ;
            }

            try {
                aint1[Material.COAL_ORE.ordinal()] = 50;
            } catch (NoSuchFieldError nosuchfielderror256) {
                ;
            }

            try {
                aint1[Material.COARSE_DIRT.ordinal()] = 17;
            } catch (NoSuchFieldError nosuchfielderror257) {
                ;
            }

            try {
                aint1[Material.COAST_ARMOR_TRIM_SMITHING_TEMPLATE.ordinal()] = 1222;
            } catch (NoSuchFieldError nosuchfielderror258) {
                ;
            }

            try {
                aint1[Material.COBBLED_DEEPSLATE.ordinal()] = 10;
            } catch (NoSuchFieldError nosuchfielderror259) {
                ;
            }

            try {
                aint1[Material.COBBLED_DEEPSLATE_SLAB.ordinal()] = 631;
            } catch (NoSuchFieldError nosuchfielderror260) {
                ;
            }

            try {
                aint1[Material.COBBLED_DEEPSLATE_STAIRS.ordinal()] = 614;
            } catch (NoSuchFieldError nosuchfielderror261) {
                ;
            }

            try {
                aint1[Material.COBBLED_DEEPSLATE_WALL.ordinal()] = 394;
            } catch (NoSuchFieldError nosuchfielderror262) {
                ;
            }

            try {
                aint1[Material.COBBLESTONE.ordinal()] = 23;
            } catch (NoSuchFieldError nosuchfielderror263) {
                ;
            }

            try {
                aint1[Material.COBBLESTONE_SLAB.ordinal()] = 248;
            } catch (NoSuchFieldError nosuchfielderror264) {
                ;
            }

            try {
                aint1[Material.COBBLESTONE_STAIRS.ordinal()] = 283;
            } catch (NoSuchFieldError nosuchfielderror265) {
                ;
            }

            try {
                aint1[Material.COBBLESTONE_WALL.ordinal()] = 376;
            } catch (NoSuchFieldError nosuchfielderror266) {
                ;
            }

            try {
                aint1[Material.COBWEB.ordinal()] = 173;
            } catch (NoSuchFieldError nosuchfielderror267) {
                ;
            }

            try {
                aint1[Material.COCOA.ordinal()] = 1296;
            } catch (NoSuchFieldError nosuchfielderror268) {
                ;
            }

            try {
                aint1[Material.COCOA_BEANS.ordinal()] = 904;
            } catch (NoSuchFieldError nosuchfielderror269) {
                ;
            }

            try {
                aint1[Material.COD.ordinal()] = 896;
            } catch (NoSuchFieldError nosuchfielderror270) {
                ;
            }

            try {
                aint1[Material.COD_BUCKET.ordinal()] = 878;
            } catch (NoSuchFieldError nosuchfielderror271) {
                ;
            }

            try {
                aint1[Material.COD_SPAWN_EGG.ordinal()] = 977;
            } catch (NoSuchFieldError nosuchfielderror272) {
                ;
            }

            try {
                aint1[Material.COMMAND_BLOCK.ordinal()] = 374;
            } catch (NoSuchFieldError nosuchfielderror273) {
                ;
            }

            try {
                aint1[Material.COMMAND_BLOCK_MINECART.ordinal()] = 1085;
            } catch (NoSuchFieldError nosuchfielderror274) {
                ;
            }

            try {
                aint1[Material.COMPARATOR.ordinal()] = 640;
            } catch (NoSuchFieldError nosuchfielderror275) {
                ;
            }

            try {
                aint1[Material.COMPASS.ordinal()] = 889;
            } catch (NoSuchFieldError nosuchfielderror276) {
                ;
            }

            try {
                aint1[Material.COMPOSTER.ordinal()] = 1154;
            } catch (NoSuchFieldError nosuchfielderror277) {
                ;
            }

            try {
                aint1[Material.CONDUIT.ordinal()] = 599;
            } catch (NoSuchFieldError nosuchfielderror278) {
                ;
            }

            try {
                aint1[Material.COOKED_BEEF.ordinal()] = 949;
            } catch (NoSuchFieldError nosuchfielderror279) {
                ;
            }

            try {
                aint1[Material.COOKED_CHICKEN.ordinal()] = 951;
            } catch (NoSuchFieldError nosuchfielderror280) {
                ;
            }

            try {
                aint1[Material.COOKED_COD.ordinal()] = 900;
            } catch (NoSuchFieldError nosuchfielderror281) {
                ;
            }

            try {
                aint1[Material.COOKED_MUTTON.ordinal()] = 1087;
            } catch (NoSuchFieldError nosuchfielderror282) {
                ;
            }

            try {
                aint1[Material.COOKED_PORKCHOP.ordinal()] = 843;
            } catch (NoSuchFieldError nosuchfielderror283) {
                ;
            }

            try {
                aint1[Material.COOKED_RABBIT.ordinal()] = 1074;
            } catch (NoSuchFieldError nosuchfielderror284) {
                ;
            }

            try {
                aint1[Material.COOKED_SALMON.ordinal()] = 901;
            } catch (NoSuchFieldError nosuchfielderror285) {
                ;
            }

            try {
                aint1[Material.COOKIE.ordinal()] = 941;
            } catch (NoSuchFieldError nosuchfielderror286) {
                ;
            }

            try {
                aint1[Material.COPPER_BLOCK.ordinal()] = 76;
            } catch (NoSuchFieldError nosuchfielderror287) {
                ;
            }

            try {
                aint1[Material.COPPER_INGOT.ordinal()] = 773;
            } catch (NoSuchFieldError nosuchfielderror288) {
                ;
            }

            try {
                aint1[Material.COPPER_ORE.ordinal()] = 54;
            } catch (NoSuchFieldError nosuchfielderror289) {
                ;
            }

            try {
                aint1[Material.CORNFLOWER.ordinal()] = 207;
            } catch (NoSuchFieldError nosuchfielderror290) {
                ;
            }

            try {
                aint1[Material.COW_SPAWN_EGG.ordinal()] = 978;
            } catch (NoSuchFieldError nosuchfielderror291) {
                ;
            }

            try {
                aint1[Material.CRACKED_DEEPSLATE_BRICKS.ordinal()] = 326;
            } catch (NoSuchFieldError nosuchfielderror292) {
                ;
            }

            try {
                aint1[Material.CRACKED_DEEPSLATE_TILES.ordinal()] = 328;
            } catch (NoSuchFieldError nosuchfielderror293) {
                ;
            }

            try {
                aint1[Material.CRACKED_NETHER_BRICKS.ordinal()] = 346;
            } catch (NoSuchFieldError nosuchfielderror294) {
                ;
            }

            try {
                aint1[Material.CRACKED_POLISHED_BLACKSTONE_BRICKS.ordinal()] = 1189;
            } catch (NoSuchFieldError nosuchfielderror295) {
                ;
            }

            try {
                aint1[Material.CRACKED_STONE_BRICKS.ordinal()] = 321;
            } catch (NoSuchFieldError nosuchfielderror296) {
                ;
            }

            try {
                aint1[Material.CRAFTING_TABLE.ordinal()] = 279;
            } catch (NoSuchFieldError nosuchfielderror297) {
                ;
            }

            try {
                aint1[Material.CREEPER_BANNER_PATTERN.ordinal()] = 1148;
            } catch (NoSuchFieldError nosuchfielderror298) {
                ;
            }

            try {
                aint1[Material.CREEPER_HEAD.ordinal()] = 1062;
            } catch (NoSuchFieldError nosuchfielderror299) {
                ;
            }

            try {
                aint1[Material.CREEPER_SPAWN_EGG.ordinal()] = 979;
            } catch (NoSuchFieldError nosuchfielderror300) {
                ;
            }

            try {
                aint1[Material.CREEPER_WALL_HEAD.ordinal()] = 1331;
            } catch (NoSuchFieldError nosuchfielderror301) {
                ;
            }

            try {
                aint1[Material.CRIMSON_BUTTON.ordinal()] = 672;
            } catch (NoSuchFieldError nosuchfielderror302) {
                ;
            }

            try {
                aint1[Material.CRIMSON_DOOR.ordinal()] = 699;
            } catch (NoSuchFieldError nosuchfielderror303) {
                ;
            }

            try {
                aint1[Material.CRIMSON_FENCE.ordinal()] = 299;
            } catch (NoSuchFieldError nosuchfielderror304) {
                ;
            }

            try {
                aint1[Material.CRIMSON_FENCE_GATE.ordinal()] = 722;
            } catch (NoSuchFieldError nosuchfielderror305) {
                ;
            }

            try {
                aint1[Material.CRIMSON_FUNGUS.ordinal()] = 215;
            } catch (NoSuchFieldError nosuchfielderror306) {
                ;
            }

            try {
                aint1[Material.CRIMSON_HANGING_SIGN.ordinal()] = 867;
            } catch (NoSuchFieldError nosuchfielderror307) {
                ;
            }

            try {
                aint1[Material.CRIMSON_HYPHAE.ordinal()] = 153;
            } catch (NoSuchFieldError nosuchfielderror308) {
                ;
            }

            try {
                aint1[Material.CRIMSON_NYLIUM.ordinal()] = 21;
            } catch (NoSuchFieldError nosuchfielderror309) {
                ;
            }

            try {
                aint1[Material.CRIMSON_PLANKS.ordinal()] = 33;
            } catch (NoSuchFieldError nosuchfielderror310) {
                ;
            }

            try {
                aint1[Material.CRIMSON_PRESSURE_PLATE.ordinal()] = 687;
            } catch (NoSuchFieldError nosuchfielderror311) {
                ;
            }

            try {
                aint1[Material.CRIMSON_ROOTS.ordinal()] = 217;
            } catch (NoSuchFieldError nosuchfielderror312) {
                ;
            }

            try {
                aint1[Material.CRIMSON_SIGN.ordinal()] = 856;
            } catch (NoSuchFieldError nosuchfielderror313) {
                ;
            }

            try {
                aint1[Material.CRIMSON_SLAB.ordinal()] = 241;
            } catch (NoSuchFieldError nosuchfielderror314) {
                ;
            }

            try {
                aint1[Material.CRIMSON_STAIRS.ordinal()] = 372;
            } catch (NoSuchFieldError nosuchfielderror315) {
                ;
            }

            try {
                aint1[Material.CRIMSON_STEM.ordinal()] = 121;
            } catch (NoSuchFieldError nosuchfielderror316) {
                ;
            }

            try {
                aint1[Material.CRIMSON_TRAPDOOR.ordinal()] = 711;
            } catch (NoSuchFieldError nosuchfielderror317) {
                ;
            }

            try {
                aint1[Material.CRIMSON_WALL_HANGING_SIGN.ordinal()] = 1282;
            } catch (NoSuchFieldError nosuchfielderror318) {
                ;
            }

            try {
                aint1[Material.CRIMSON_WALL_SIGN.ordinal()] = 1374;
            } catch (NoSuchFieldError nosuchfielderror319) {
                ;
            }

            try {
                aint1[Material.CROSSBOW.ordinal()] = 1144;
            } catch (NoSuchFieldError nosuchfielderror320) {
                ;
            }

            try {
                aint1[Material.CRYING_OBSIDIAN.ordinal()] = 1177;
            } catch (NoSuchFieldError nosuchfielderror321) {
                ;
            }

            try {
                aint1[Material.CUT_COPPER.ordinal()] = 83;
            } catch (NoSuchFieldError nosuchfielderror322) {
                ;
            }

            try {
                aint1[Material.CUT_COPPER_SLAB.ordinal()] = 91;
            } catch (NoSuchFieldError nosuchfielderror323) {
                ;
            }

            try {
                aint1[Material.CUT_COPPER_STAIRS.ordinal()] = 87;
            } catch (NoSuchFieldError nosuchfielderror324) {
                ;
            }

            try {
                aint1[Material.CUT_RED_SANDSTONE.ordinal()] = 491;
            } catch (NoSuchFieldError nosuchfielderror325) {
                ;
            }

            try {
                aint1[Material.CUT_RED_SANDSTONE_SLAB.ordinal()] = 255;
            } catch (NoSuchFieldError nosuchfielderror326) {
                ;
            }

            try {
                aint1[Material.CUT_SANDSTONE.ordinal()] = 172;
            } catch (NoSuchFieldError nosuchfielderror327) {
                ;
            }

            try {
                aint1[Material.CUT_SANDSTONE_SLAB.ordinal()] = 246;
            } catch (NoSuchFieldError nosuchfielderror328) {
                ;
            }

            try {
                aint1[Material.CYAN_BANNER.ordinal()] = 1097;
            } catch (NoSuchFieldError nosuchfielderror329) {
                ;
            }

            try {
                aint1[Material.CYAN_BED.ordinal()] = 934;
            } catch (NoSuchFieldError nosuchfielderror330) {
                ;
            }

            try {
                aint1[Material.CYAN_CANDLE.ordinal()] = 1201;
            } catch (NoSuchFieldError nosuchfielderror331) {
                ;
            }

            try {
                aint1[Material.CYAN_CANDLE_CAKE.ordinal()] = 1390;
            } catch (NoSuchFieldError nosuchfielderror332) {
                ;
            }

            try {
                aint1[Material.CYAN_CARPET.ordinal()] = 434;
            } catch (NoSuchFieldError nosuchfielderror333) {
                ;
            }

            try {
                aint1[Material.CYAN_CONCRETE.ordinal()] = 543;
            } catch (NoSuchFieldError nosuchfielderror334) {
                ;
            }

            try {
                aint1[Material.CYAN_CONCRETE_POWDER.ordinal()] = 559;
            } catch (NoSuchFieldError nosuchfielderror335) {
                ;
            }

            try {
                aint1[Material.CYAN_DYE.ordinal()] = 914;
            } catch (NoSuchFieldError nosuchfielderror336) {
                ;
            }

            try {
                aint1[Material.CYAN_GLAZED_TERRACOTTA.ordinal()] = 527;
            } catch (NoSuchFieldError nosuchfielderror337) {
                ;
            }

            try {
                aint1[Material.CYAN_SHULKER_BOX.ordinal()] = 511;
            } catch (NoSuchFieldError nosuchfielderror338) {
                ;
            }

            try {
                aint1[Material.CYAN_STAINED_GLASS.ordinal()] = 459;
            } catch (NoSuchFieldError nosuchfielderror339) {
                ;
            }

            try {
                aint1[Material.CYAN_STAINED_GLASS_PANE.ordinal()] = 475;
            } catch (NoSuchFieldError nosuchfielderror340) {
                ;
            }

            try {
                aint1[Material.CYAN_TERRACOTTA.ordinal()] = 415;
            } catch (NoSuchFieldError nosuchfielderror341) {
                ;
            }

            try {
                aint1[Material.CYAN_WALL_BANNER.ordinal()] = 1343;
            } catch (NoSuchFieldError nosuchfielderror342) {
                ;
            }

            try {
                aint1[Material.CYAN_WOOL.ordinal()] = 190;
            } catch (NoSuchFieldError nosuchfielderror343) {
                ;
            }

            try {
                aint1[Material.DAMAGED_ANVIL.ordinal()] = 400;
            } catch (NoSuchFieldError nosuchfielderror344) {
                ;
            }

            try {
                aint1[Material.DANDELION.ordinal()] = 197;
            } catch (NoSuchFieldError nosuchfielderror345) {
                ;
            }

            try {
                aint1[Material.DANGER_POTTERY_SHERD.ordinal()] = 1242;
            } catch (NoSuchFieldError nosuchfielderror346) {
                ;
            }

            try {
                aint1[Material.DARK_OAK_BOAT.ordinal()] = 749;
            } catch (NoSuchFieldError nosuchfielderror347) {
                ;
            }

            try {
                aint1[Material.DARK_OAK_BUTTON.ordinal()] = 669;
            } catch (NoSuchFieldError nosuchfielderror348) {
                ;
            }

            try {
                aint1[Material.DARK_OAK_CHEST_BOAT.ordinal()] = 750;
            } catch (NoSuchFieldError nosuchfielderror349) {
                ;
            }

            try {
                aint1[Material.DARK_OAK_DOOR.ordinal()] = 696;
            } catch (NoSuchFieldError nosuchfielderror350) {
                ;
            }

            try {
                aint1[Material.DARK_OAK_FENCE.ordinal()] = 296;
            } catch (NoSuchFieldError nosuchfielderror351) {
                ;
            }

            try {
                aint1[Material.DARK_OAK_FENCE_GATE.ordinal()] = 719;
            } catch (NoSuchFieldError nosuchfielderror352) {
                ;
            }

            try {
                aint1[Material.DARK_OAK_HANGING_SIGN.ordinal()] = 864;
            } catch (NoSuchFieldError nosuchfielderror353) {
                ;
            }

            try {
                aint1[Material.DARK_OAK_LEAVES.ordinal()] = 161;
            } catch (NoSuchFieldError nosuchfielderror354) {
                ;
            }

            try {
                aint1[Material.DARK_OAK_LOG.ordinal()] = 117;
            } catch (NoSuchFieldError nosuchfielderror355) {
                ;
            }

            try {
                aint1[Material.DARK_OAK_PLANKS.ordinal()] = 30;
            } catch (NoSuchFieldError nosuchfielderror356) {
                ;
            }

            try {
                aint1[Material.DARK_OAK_PRESSURE_PLATE.ordinal()] = 684;
            } catch (NoSuchFieldError nosuchfielderror357) {
                ;
            }

            try {
                aint1[Material.DARK_OAK_SAPLING.ordinal()] = 42;
            } catch (NoSuchFieldError nosuchfielderror358) {
                ;
            }

            try {
                aint1[Material.DARK_OAK_SIGN.ordinal()] = 853;
            } catch (NoSuchFieldError nosuchfielderror359) {
                ;
            }

            try {
                aint1[Material.DARK_OAK_SLAB.ordinal()] = 237;
            } catch (NoSuchFieldError nosuchfielderror360) {
                ;
            }

            try {
                aint1[Material.DARK_OAK_STAIRS.ordinal()] = 368;
            } catch (NoSuchFieldError nosuchfielderror361) {
                ;
            }

            try {
                aint1[Material.DARK_OAK_TRAPDOOR.ordinal()] = 708;
            } catch (NoSuchFieldError nosuchfielderror362) {
                ;
            }

            try {
                aint1[Material.DARK_OAK_WALL_HANGING_SIGN.ordinal()] = 1280;
            } catch (NoSuchFieldError nosuchfielderror363) {
                ;
            }

            try {
                aint1[Material.DARK_OAK_WALL_SIGN.ordinal()] = 1271;
            } catch (NoSuchFieldError nosuchfielderror364) {
                ;
            }

            try {
                aint1[Material.DARK_OAK_WOOD.ordinal()] = 151;
            } catch (NoSuchFieldError nosuchfielderror365) {
                ;
            }

            try {
                aint1[Material.DARK_PRISMARINE.ordinal()] = 484;
            } catch (NoSuchFieldError nosuchfielderror366) {
                ;
            }

            try {
                aint1[Material.DARK_PRISMARINE_SLAB.ordinal()] = 259;
            } catch (NoSuchFieldError nosuchfielderror367) {
                ;
            }

            try {
                aint1[Material.DARK_PRISMARINE_STAIRS.ordinal()] = 487;
            } catch (NoSuchFieldError nosuchfielderror368) {
                ;
            }

            try {
                aint1[Material.DAYLIGHT_DETECTOR.ordinal()] = 653;
            } catch (NoSuchFieldError nosuchfielderror369) {
                ;
            }

            try {
                aint1[Material.DEAD_BRAIN_CORAL.ordinal()] = 583;
            } catch (NoSuchFieldError nosuchfielderror370) {
                ;
            }

            try {
                aint1[Material.DEAD_BRAIN_CORAL_BLOCK.ordinal()] = 569;
            } catch (NoSuchFieldError nosuchfielderror371) {
                ;
            }

            try {
                aint1[Material.DEAD_BRAIN_CORAL_FAN.ordinal()] = 594;
            } catch (NoSuchFieldError nosuchfielderror372) {
                ;
            }

            try {
                aint1[Material.DEAD_BRAIN_CORAL_WALL_FAN.ordinal()] = 1357;
            } catch (NoSuchFieldError nosuchfielderror373) {
                ;
            }

            try {
                aint1[Material.DEAD_BUBBLE_CORAL.ordinal()] = 584;
            } catch (NoSuchFieldError nosuchfielderror374) {
                ;
            }

            try {
                aint1[Material.DEAD_BUBBLE_CORAL_BLOCK.ordinal()] = 570;
            } catch (NoSuchFieldError nosuchfielderror375) {
                ;
            }

            try {
                aint1[Material.DEAD_BUBBLE_CORAL_FAN.ordinal()] = 595;
            } catch (NoSuchFieldError nosuchfielderror376) {
                ;
            }

            try {
                aint1[Material.DEAD_BUBBLE_CORAL_WALL_FAN.ordinal()] = 1358;
            } catch (NoSuchFieldError nosuchfielderror377) {
                ;
            }

            try {
                aint1[Material.DEAD_BUSH.ordinal()] = 178;
            } catch (NoSuchFieldError nosuchfielderror378) {
                ;
            }

            try {
                aint1[Material.DEAD_FIRE_CORAL.ordinal()] = 585;
            } catch (NoSuchFieldError nosuchfielderror379) {
                ;
            }

            try {
                aint1[Material.DEAD_FIRE_CORAL_BLOCK.ordinal()] = 571;
            } catch (NoSuchFieldError nosuchfielderror380) {
                ;
            }

            try {
                aint1[Material.DEAD_FIRE_CORAL_FAN.ordinal()] = 596;
            } catch (NoSuchFieldError nosuchfielderror381) {
                ;
            }

            try {
                aint1[Material.DEAD_FIRE_CORAL_WALL_FAN.ordinal()] = 1359;
            } catch (NoSuchFieldError nosuchfielderror382) {
                ;
            }

            try {
                aint1[Material.DEAD_HORN_CORAL.ordinal()] = 586;
            } catch (NoSuchFieldError nosuchfielderror383) {
                ;
            }

            try {
                aint1[Material.DEAD_HORN_CORAL_BLOCK.ordinal()] = 572;
            } catch (NoSuchFieldError nosuchfielderror384) {
                ;
            }

            try {
                aint1[Material.DEAD_HORN_CORAL_FAN.ordinal()] = 597;
            } catch (NoSuchFieldError nosuchfielderror385) {
                ;
            }

            try {
                aint1[Material.DEAD_HORN_CORAL_WALL_FAN.ordinal()] = 1360;
            } catch (NoSuchFieldError nosuchfielderror386) {
                ;
            }

            try {
                aint1[Material.DEAD_TUBE_CORAL.ordinal()] = 587;
            } catch (NoSuchFieldError nosuchfielderror387) {
                ;
            }

            try {
                aint1[Material.DEAD_TUBE_CORAL_BLOCK.ordinal()] = 568;
            } catch (NoSuchFieldError nosuchfielderror388) {
                ;
            }

            try {
                aint1[Material.DEAD_TUBE_CORAL_FAN.ordinal()] = 593;
            } catch (NoSuchFieldError nosuchfielderror389) {
                ;
            }

            try {
                aint1[Material.DEAD_TUBE_CORAL_WALL_FAN.ordinal()] = 1356;
            } catch (NoSuchFieldError nosuchfielderror390) {
                ;
            }

            try {
                aint1[Material.DEBUG_STICK.ordinal()] = 1122;
            } catch (NoSuchFieldError nosuchfielderror391) {
                ;
            }

            try {
                aint1[Material.DECORATED_POT.ordinal()] = 267;
            } catch (NoSuchFieldError nosuchfielderror392) {
                ;
            }

            try {
                aint1[Material.DEEPSLATE.ordinal()] = 9;
            } catch (NoSuchFieldError nosuchfielderror393) {
                ;
            }

            try {
                aint1[Material.DEEPSLATE_BRICKS.ordinal()] = 325;
            } catch (NoSuchFieldError nosuchfielderror394) {
                ;
            }

            try {
                aint1[Material.DEEPSLATE_BRICK_SLAB.ordinal()] = 633;
            } catch (NoSuchFieldError nosuchfielderror395) {
                ;
            }

            try {
                aint1[Material.DEEPSLATE_BRICK_STAIRS.ordinal()] = 616;
            } catch (NoSuchFieldError nosuchfielderror396) {
                ;
            }

            try {
                aint1[Material.DEEPSLATE_BRICK_WALL.ordinal()] = 396;
            } catch (NoSuchFieldError nosuchfielderror397) {
                ;
            }

            try {
                aint1[Material.DEEPSLATE_COAL_ORE.ordinal()] = 51;
            } catch (NoSuchFieldError nosuchfielderror398) {
                ;
            }

            try {
                aint1[Material.DEEPSLATE_COPPER_ORE.ordinal()] = 55;
            } catch (NoSuchFieldError nosuchfielderror399) {
                ;
            }

            try {
                aint1[Material.DEEPSLATE_DIAMOND_ORE.ordinal()] = 65;
            } catch (NoSuchFieldError nosuchfielderror400) {
                ;
            }

            try {
                aint1[Material.DEEPSLATE_EMERALD_ORE.ordinal()] = 61;
            } catch (NoSuchFieldError nosuchfielderror401) {
                ;
            }

            try {
                aint1[Material.DEEPSLATE_GOLD_ORE.ordinal()] = 57;
            } catch (NoSuchFieldError nosuchfielderror402) {
                ;
            }

            try {
                aint1[Material.DEEPSLATE_IRON_ORE.ordinal()] = 53;
            } catch (NoSuchFieldError nosuchfielderror403) {
                ;
            }

            try {
                aint1[Material.DEEPSLATE_LAPIS_ORE.ordinal()] = 63;
            } catch (NoSuchFieldError nosuchfielderror404) {
                ;
            }

            try {
                aint1[Material.DEEPSLATE_REDSTONE_ORE.ordinal()] = 59;
            } catch (NoSuchFieldError nosuchfielderror405) {
                ;
            }

            try {
                aint1[Material.DEEPSLATE_TILES.ordinal()] = 327;
            } catch (NoSuchFieldError nosuchfielderror406) {
                ;
            }

            try {
                aint1[Material.DEEPSLATE_TILE_SLAB.ordinal()] = 634;
            } catch (NoSuchFieldError nosuchfielderror407) {
                ;
            }

            try {
                aint1[Material.DEEPSLATE_TILE_STAIRS.ordinal()] = 617;
            } catch (NoSuchFieldError nosuchfielderror408) {
                ;
            }

            try {
                aint1[Material.DEEPSLATE_TILE_WALL.ordinal()] = 397;
            } catch (NoSuchFieldError nosuchfielderror409) {
                ;
            }

            try {
                aint1[Material.DETECTOR_RAIL.ordinal()] = 725;
            } catch (NoSuchFieldError nosuchfielderror410) {
                ;
            }

            try {
                aint1[Material.DIAMOND.ordinal()] = 765;
            } catch (NoSuchFieldError nosuchfielderror411) {
                ;
            }

            try {
                aint1[Material.DIAMOND_AXE.ordinal()] = 801;
            } catch (NoSuchFieldError nosuchfielderror412) {
                ;
            }

            try {
                aint1[Material.DIAMOND_BLOCK.ordinal()] = 78;
            } catch (NoSuchFieldError nosuchfielderror413) {
                ;
            }

            try {
                aint1[Material.DIAMOND_BOOTS.ordinal()] = 832;
            } catch (NoSuchFieldError nosuchfielderror414) {
                ;
            }

            try {
                aint1[Material.DIAMOND_CHESTPLATE.ordinal()] = 830;
            } catch (NoSuchFieldError nosuchfielderror415) {
                ;
            }

            try {
                aint1[Material.DIAMOND_HELMET.ordinal()] = 829;
            } catch (NoSuchFieldError nosuchfielderror416) {
                ;
            }

            try {
                aint1[Material.DIAMOND_HOE.ordinal()] = 802;
            } catch (NoSuchFieldError nosuchfielderror417) {
                ;
            }

            try {
                aint1[Material.DIAMOND_HORSE_ARMOR.ordinal()] = 1081;
            } catch (NoSuchFieldError nosuchfielderror418) {
                ;
            }

            try {
                aint1[Material.DIAMOND_LEGGINGS.ordinal()] = 831;
            } catch (NoSuchFieldError nosuchfielderror419) {
                ;
            }

            try {
                aint1[Material.DIAMOND_ORE.ordinal()] = 64;
            } catch (NoSuchFieldError nosuchfielderror420) {
                ;
            }

            try {
                aint1[Material.DIAMOND_PICKAXE.ordinal()] = 800;
            } catch (NoSuchFieldError nosuchfielderror421) {
                ;
            }

            try {
                aint1[Material.DIAMOND_SHOVEL.ordinal()] = 799;
            } catch (NoSuchFieldError nosuchfielderror422) {
                ;
            }

            try {
                aint1[Material.DIAMOND_SWORD.ordinal()] = 798;
            } catch (NoSuchFieldError nosuchfielderror423) {
                ;
            }

            try {
                aint1[Material.DIORITE.ordinal()] = 5;
            } catch (NoSuchFieldError nosuchfielderror424) {
                ;
            }

            try {
                aint1[Material.DIORITE_SLAB.ordinal()] = 630;
            } catch (NoSuchFieldError nosuchfielderror425) {
                ;
            }

            try {
                aint1[Material.DIORITE_STAIRS.ordinal()] = 613;
            } catch (NoSuchFieldError nosuchfielderror426) {
                ;
            }

            try {
                aint1[Material.DIORITE_WALL.ordinal()] = 390;
            } catch (NoSuchFieldError nosuchfielderror427) {
                ;
            }

            try {
                aint1[Material.DIRT.ordinal()] = 16;
            } catch (NoSuchFieldError nosuchfielderror428) {
                ;
            }

            try {
                aint1[Material.DIRT_PATH.ordinal()] = 443;
            } catch (NoSuchFieldError nosuchfielderror429) {
                ;
            }

            try {
                aint1[Material.DISC_FRAGMENT_5.ordinal()] = 1139;
            } catch (NoSuchFieldError nosuchfielderror430) {
                ;
            }

            try {
                aint1[Material.DISPENSER.ordinal()] = 647;
            } catch (NoSuchFieldError nosuchfielderror431) {
                ;
            }

            try {
                aint1[Material.DOLPHIN_SPAWN_EGG.ordinal()] = 980;
            } catch (NoSuchFieldError nosuchfielderror432) {
                ;
            }

            try {
                aint1[Material.DONKEY_SPAWN_EGG.ordinal()] = 981;
            } catch (NoSuchFieldError nosuchfielderror433) {
                ;
            }

            try {
                aint1[Material.DRAGON_BREATH.ordinal()] = 1112;
            } catch (NoSuchFieldError nosuchfielderror434) {
                ;
            }

            try {
                aint1[Material.DRAGON_EGG.ordinal()] = 358;
            } catch (NoSuchFieldError nosuchfielderror435) {
                ;
            }

            try {
                aint1[Material.DRAGON_HEAD.ordinal()] = 1063;
            } catch (NoSuchFieldError nosuchfielderror436) {
                ;
            }

            try {
                aint1[Material.DRAGON_WALL_HEAD.ordinal()] = 1332;
            } catch (NoSuchFieldError nosuchfielderror437) {
                ;
            }

            try {
                aint1[Material.DRIED_KELP.ordinal()] = 945;
            } catch (NoSuchFieldError nosuchfielderror438) {
                ;
            }

            try {
                aint1[Material.DRIED_KELP_BLOCK.ordinal()] = 884;
            } catch (NoSuchFieldError nosuchfielderror439) {
                ;
            }

            try {
                aint1[Material.DRIPSTONE_BLOCK.ordinal()] = 14;
            } catch (NoSuchFieldError nosuchfielderror440) {
                ;
            }

            try {
                aint1[Material.DROPPER.ordinal()] = 648;
            } catch (NoSuchFieldError nosuchfielderror441) {
                ;
            }

            try {
                aint1[Material.DROWNED_SPAWN_EGG.ordinal()] = 982;
            } catch (NoSuchFieldError nosuchfielderror442) {
                ;
            }

            try {
                aint1[Material.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE.ordinal()] = 1221;
            } catch (NoSuchFieldError nosuchfielderror443) {
                ;
            }

            try {
                aint1[Material.ECHO_SHARD.ordinal()] = 1217;
            } catch (NoSuchFieldError nosuchfielderror444) {
                ;
            }

            try {
                aint1[Material.EGG.ordinal()] = 888;
            } catch (NoSuchFieldError nosuchfielderror445) {
                ;
            }

            try {
                aint1[Material.ELDER_GUARDIAN_SPAWN_EGG.ordinal()] = 983;
            } catch (NoSuchFieldError nosuchfielderror446) {
                ;
            }

            try {
                aint1[Material.ELYTRA.ordinal()] = 736;
            } catch (NoSuchFieldError nosuchfielderror447) {
                ;
            }

            try {
                aint1[Material.EMERALD.ordinal()] = 766;
            } catch (NoSuchFieldError nosuchfielderror448) {
                ;
            }

            try {
                aint1[Material.EMERALD_BLOCK.ordinal()] = 361;
            } catch (NoSuchFieldError nosuchfielderror449) {
                ;
            }

            try {
                aint1[Material.EMERALD_ORE.ordinal()] = 60;
            } catch (NoSuchFieldError nosuchfielderror450) {
                ;
            }

            try {
                aint1[Material.ENCHANTED_BOOK.ordinal()] = 1069;
            } catch (NoSuchFieldError nosuchfielderror451) {
                ;
            }

            try {
                aint1[Material.ENCHANTED_GOLDEN_APPLE.ordinal()] = 846;
            } catch (NoSuchFieldError nosuchfielderror452) {
                ;
            }

            try {
                aint1[Material.ENCHANTING_TABLE.ordinal()] = 354;
            } catch (NoSuchFieldError nosuchfielderror453) {
                ;
            }

            try {
                aint1[Material.ENDERMAN_SPAWN_EGG.ordinal()] = 985;
            } catch (NoSuchFieldError nosuchfielderror454) {
                ;
            }

            try {
                aint1[Material.ENDERMITE_SPAWN_EGG.ordinal()] = 986;
            } catch (NoSuchFieldError nosuchfielderror455) {
                ;
            }

            try {
                aint1[Material.ENDER_CHEST.ordinal()] = 360;
            } catch (NoSuchFieldError nosuchfielderror456) {
                ;
            }

            try {
                aint1[Material.ENDER_DRAGON_SPAWN_EGG.ordinal()] = 984;
            } catch (NoSuchFieldError nosuchfielderror457) {
                ;
            }

            try {
                aint1[Material.ENDER_EYE.ordinal()] = 966;
            } catch (NoSuchFieldError nosuchfielderror458) {
                ;
            }

            try {
                aint1[Material.ENDER_PEARL.ordinal()] = 953;
            } catch (NoSuchFieldError nosuchfielderror459) {
                ;
            }

            try {
                aint1[Material.END_CRYSTAL.ordinal()] = 1104;
            } catch (NoSuchFieldError nosuchfielderror460) {
                ;
            }

            try {
                aint1[Material.END_GATEWAY.ordinal()] = 1353;
            } catch (NoSuchFieldError nosuchfielderror461) {
                ;
            }

            try {
                aint1[Material.END_PORTAL.ordinal()] = 1295;
            } catch (NoSuchFieldError nosuchfielderror462) {
                ;
            }

            try {
                aint1[Material.END_PORTAL_FRAME.ordinal()] = 355;
            } catch (NoSuchFieldError nosuchfielderror463) {
                ;
            }

            try {
                aint1[Material.END_ROD.ordinal()] = 271;
            } catch (NoSuchFieldError nosuchfielderror464) {
                ;
            }

            try {
                aint1[Material.END_STONE.ordinal()] = 356;
            } catch (NoSuchFieldError nosuchfielderror465) {
                ;
            }

            try {
                aint1[Material.END_STONE_BRICKS.ordinal()] = 357;
            } catch (NoSuchFieldError nosuchfielderror466) {
                ;
            }

            try {
                aint1[Material.END_STONE_BRICK_SLAB.ordinal()] = 623;
            } catch (NoSuchFieldError nosuchfielderror467) {
                ;
            }

            try {
                aint1[Material.END_STONE_BRICK_STAIRS.ordinal()] = 605;
            } catch (NoSuchFieldError nosuchfielderror468) {
                ;
            }

            try {
                aint1[Material.END_STONE_BRICK_WALL.ordinal()] = 389;
            } catch (NoSuchFieldError nosuchfielderror469) {
                ;
            }

            try {
                aint1[Material.EVOKER_SPAWN_EGG.ordinal()] = 987;
            } catch (NoSuchFieldError nosuchfielderror470) {
                ;
            }

            try {
                aint1[Material.EXPERIENCE_BOTTLE.ordinal()] = 1045;
            } catch (NoSuchFieldError nosuchfielderror471) {
                ;
            }

            try {
                aint1[Material.EXPLORER_POTTERY_SHERD.ordinal()] = 1243;
            } catch (NoSuchFieldError nosuchfielderror472) {
                ;
            }

            try {
                aint1[Material.EXPOSED_COPPER.ordinal()] = 80;
            } catch (NoSuchFieldError nosuchfielderror473) {
                ;
            }

            try {
                aint1[Material.EXPOSED_CUT_COPPER.ordinal()] = 84;
            } catch (NoSuchFieldError nosuchfielderror474) {
                ;
            }

            try {
                aint1[Material.EXPOSED_CUT_COPPER_SLAB.ordinal()] = 92;
            } catch (NoSuchFieldError nosuchfielderror475) {
                ;
            }

            try {
                aint1[Material.EXPOSED_CUT_COPPER_STAIRS.ordinal()] = 88;
            } catch (NoSuchFieldError nosuchfielderror476) {
                ;
            }

            try {
                aint1[Material.EYE_ARMOR_TRIM_SMITHING_TEMPLATE.ordinal()] = 1225;
            } catch (NoSuchFieldError nosuchfielderror477) {
                ;
            }

            try {
                aint1[Material.FARMLAND.ordinal()] = 280;
            } catch (NoSuchFieldError nosuchfielderror478) {
                ;
            }

            try {
                aint1[Material.FEATHER.ordinal()] = 812;
            } catch (NoSuchFieldError nosuchfielderror479) {
                ;
            }

            try {
                aint1[Material.FERMENTED_SPIDER_EYE.ordinal()] = 961;
            } catch (NoSuchFieldError nosuchfielderror480) {
                ;
            }

            try {
                aint1[Material.FERN.ordinal()] = 175;
            } catch (NoSuchFieldError nosuchfielderror481) {
                ;
            }

            try {
                aint1[Material.FILLED_MAP.ordinal()] = 942;
            } catch (NoSuchFieldError nosuchfielderror482) {
                ;
            }

            try {
                aint1[Material.FIRE.ordinal()] = 1262;
            } catch (NoSuchFieldError nosuchfielderror483) {
                ;
            }

            try {
                aint1[Material.FIREWORK_ROCKET.ordinal()] = 1067;
            } catch (NoSuchFieldError nosuchfielderror484) {
                ;
            }

            try {
                aint1[Material.FIREWORK_STAR.ordinal()] = 1068;
            } catch (NoSuchFieldError nosuchfielderror485) {
                ;
            }

            try {
                aint1[Material.FIRE_CHARGE.ordinal()] = 1046;
            } catch (NoSuchFieldError nosuchfielderror486) {
                ;
            }

            try {
                aint1[Material.FIRE_CORAL.ordinal()] = 581;
            } catch (NoSuchFieldError nosuchfielderror487) {
                ;
            }

            try {
                aint1[Material.FIRE_CORAL_BLOCK.ordinal()] = 576;
            } catch (NoSuchFieldError nosuchfielderror488) {
                ;
            }

            try {
                aint1[Material.FIRE_CORAL_FAN.ordinal()] = 591;
            } catch (NoSuchFieldError nosuchfielderror489) {
                ;
            }

            try {
                aint1[Material.FIRE_CORAL_WALL_FAN.ordinal()] = 1364;
            } catch (NoSuchFieldError nosuchfielderror490) {
                ;
            }

            try {
                aint1[Material.FISHING_ROD.ordinal()] = 892;
            } catch (NoSuchFieldError nosuchfielderror491) {
                ;
            }

            try {
                aint1[Material.FLETCHING_TABLE.ordinal()] = 1159;
            } catch (NoSuchFieldError nosuchfielderror492) {
                ;
            }

            try {
                aint1[Material.FLINT.ordinal()] = 841;
            } catch (NoSuchFieldError nosuchfielderror493) {
                ;
            }

            try {
                aint1[Material.FLINT_AND_STEEL.ordinal()] = 759;
            } catch (NoSuchFieldError nosuchfielderror494) {
                ;
            }

            try {
                aint1[Material.FLOWERING_AZALEA.ordinal()] = 177;
            } catch (NoSuchFieldError nosuchfielderror495) {
                ;
            }

            try {
                aint1[Material.FLOWERING_AZALEA_LEAVES.ordinal()] = 164;
            } catch (NoSuchFieldError nosuchfielderror496) {
                ;
            }

            try {
                aint1[Material.FLOWER_BANNER_PATTERN.ordinal()] = 1147;
            } catch (NoSuchFieldError nosuchfielderror497) {
                ;
            }

            try {
                aint1[Material.FLOWER_POT.ordinal()] = 1051;
            } catch (NoSuchFieldError nosuchfielderror498) {
                ;
            }

            try {
                aint1[Material.FOX_SPAWN_EGG.ordinal()] = 988;
            } catch (NoSuchFieldError nosuchfielderror499) {
                ;
            }

            try {
                aint1[Material.FRIEND_POTTERY_SHERD.ordinal()] = 1244;
            } catch (NoSuchFieldError nosuchfielderror500) {
                ;
            }

            try {
                aint1[Material.FROGSPAWN.ordinal()] = 1216;
            } catch (NoSuchFieldError nosuchfielderror501) {
                ;
            }

            try {
                aint1[Material.FROG_SPAWN_EGG.ordinal()] = 989;
            } catch (NoSuchFieldError nosuchfielderror502) {
                ;
            }

            try {
                aint1[Material.FROSTED_ICE.ordinal()] = 1354;
            } catch (NoSuchFieldError nosuchfielderror503) {
                ;
            }

            try {
                aint1[Material.FURNACE.ordinal()] = 281;
            } catch (NoSuchFieldError nosuchfielderror504) {
                ;
            }

            try {
                aint1[Material.FURNACE_MINECART.ordinal()] = 731;
            } catch (NoSuchFieldError nosuchfielderror505) {
                ;
            }

            try {
                aint1[Material.GHAST_SPAWN_EGG.ordinal()] = 990;
            } catch (NoSuchFieldError nosuchfielderror506) {
                ;
            }

            try {
                aint1[Material.GHAST_TEAR.ordinal()] = 955;
            } catch (NoSuchFieldError nosuchfielderror507) {
                ;
            }

            try {
                aint1[Material.GILDED_BLACKSTONE.ordinal()] = 1181;
            } catch (NoSuchFieldError nosuchfielderror508) {
                ;
            }

            try {
                aint1[Material.GLASS.ordinal()] = 167;
            } catch (NoSuchFieldError nosuchfielderror509) {
                ;
            }

            try {
                aint1[Material.GLASS_BOTTLE.ordinal()] = 959;
            } catch (NoSuchFieldError nosuchfielderror510) {
                ;
            }

            try {
                aint1[Material.GLASS_PANE.ordinal()] = 336;
            } catch (NoSuchFieldError nosuchfielderror511) {
                ;
            }

            try {
                aint1[Material.GLISTERING_MELON_SLICE.ordinal()] = 967;
            } catch (NoSuchFieldError nosuchfielderror512) {
                ;
            }

            try {
                aint1[Material.GLOBE_BANNER_PATTERN.ordinal()] = 1151;
            } catch (NoSuchFieldError nosuchfielderror513) {
                ;
            }

            try {
                aint1[Material.GLOWSTONE.ordinal()] = 311;
            } catch (NoSuchFieldError nosuchfielderror514) {
                ;
            }

            try {
                aint1[Material.GLOWSTONE_DUST.ordinal()] = 895;
            } catch (NoSuchFieldError nosuchfielderror515) {
                ;
            }

            try {
                aint1[Material.GLOW_BERRIES.ordinal()] = 1167;
            } catch (NoSuchFieldError nosuchfielderror516) {
                ;
            }

            try {
                aint1[Material.GLOW_INK_SAC.ordinal()] = 903;
            } catch (NoSuchFieldError nosuchfielderror517) {
                ;
            }

            try {
                aint1[Material.GLOW_ITEM_FRAME.ordinal()] = 1050;
            } catch (NoSuchFieldError nosuchfielderror518) {
                ;
            }

            try {
                aint1[Material.GLOW_LICHEN.ordinal()] = 339;
            } catch (NoSuchFieldError nosuchfielderror519) {
                ;
            }

            try {
                aint1[Material.GLOW_SQUID_SPAWN_EGG.ordinal()] = 991;
            } catch (NoSuchFieldError nosuchfielderror520) {
                ;
            }

            try {
                aint1[Material.GOAT_HORN.ordinal()] = 1153;
            } catch (NoSuchFieldError nosuchfielderror521) {
                ;
            }

            try {
                aint1[Material.GOAT_SPAWN_EGG.ordinal()] = 992;
            } catch (NoSuchFieldError nosuchfielderror522) {
                ;
            }

            try {
                aint1[Material.GOLDEN_APPLE.ordinal()] = 845;
            } catch (NoSuchFieldError nosuchfielderror523) {
                ;
            }

            try {
                aint1[Material.GOLDEN_AXE.ordinal()] = 791;
            } catch (NoSuchFieldError nosuchfielderror524) {
                ;
            }

            try {
                aint1[Material.GOLDEN_BOOTS.ordinal()] = 836;
            } catch (NoSuchFieldError nosuchfielderror525) {
                ;
            }

            try {
                aint1[Material.GOLDEN_CARROT.ordinal()] = 1057;
            } catch (NoSuchFieldError nosuchfielderror526) {
                ;
            }

            try {
                aint1[Material.GOLDEN_CHESTPLATE.ordinal()] = 834;
            } catch (NoSuchFieldError nosuchfielderror527) {
                ;
            }

            try {
                aint1[Material.GOLDEN_HELMET.ordinal()] = 833;
            } catch (NoSuchFieldError nosuchfielderror528) {
                ;
            }

            try {
                aint1[Material.GOLDEN_HOE.ordinal()] = 792;
            } catch (NoSuchFieldError nosuchfielderror529) {
                ;
            }

            try {
                aint1[Material.GOLDEN_HORSE_ARMOR.ordinal()] = 1080;
            } catch (NoSuchFieldError nosuchfielderror530) {
                ;
            }

            try {
                aint1[Material.GOLDEN_LEGGINGS.ordinal()] = 835;
            } catch (NoSuchFieldError nosuchfielderror531) {
                ;
            }

            try {
                aint1[Material.GOLDEN_PICKAXE.ordinal()] = 790;
            } catch (NoSuchFieldError nosuchfielderror532) {
                ;
            }

            try {
                aint1[Material.GOLDEN_SHOVEL.ordinal()] = 789;
            } catch (NoSuchFieldError nosuchfielderror533) {
                ;
            }

            try {
                aint1[Material.GOLDEN_SWORD.ordinal()] = 788;
            } catch (NoSuchFieldError nosuchfielderror534) {
                ;
            }

            try {
                aint1[Material.GOLD_BLOCK.ordinal()] = 77;
            } catch (NoSuchFieldError nosuchfielderror535) {
                ;
            }

            try {
                aint1[Material.GOLD_INGOT.ordinal()] = 775;
            } catch (NoSuchFieldError nosuchfielderror536) {
                ;
            }

            try {
                aint1[Material.GOLD_NUGGET.ordinal()] = 956;
            } catch (NoSuchFieldError nosuchfielderror537) {
                ;
            }

            try {
                aint1[Material.GOLD_ORE.ordinal()] = 56;
            } catch (NoSuchFieldError nosuchfielderror538) {
                ;
            }

            try {
                aint1[Material.GRANITE.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror539) {
                ;
            }

            try {
                aint1[Material.GRANITE_SLAB.ordinal()] = 626;
            } catch (NoSuchFieldError nosuchfielderror540) {
                ;
            }

            try {
                aint1[Material.GRANITE_STAIRS.ordinal()] = 609;
            } catch (NoSuchFieldError nosuchfielderror541) {
                ;
            }

            try {
                aint1[Material.GRANITE_WALL.ordinal()] = 382;
            } catch (NoSuchFieldError nosuchfielderror542) {
                ;
            }

            try {
                aint1[Material.GRASS.ordinal()] = 174;
            } catch (NoSuchFieldError nosuchfielderror543) {
                ;
            }

            try {
                aint1[Material.GRASS_BLOCK.ordinal()] = 15;
            } catch (NoSuchFieldError nosuchfielderror544) {
                ;
            }

            try {
                aint1[Material.GRAVEL.ordinal()] = 49;
            } catch (NoSuchFieldError nosuchfielderror545) {
                ;
            }

            try {
                aint1[Material.GRAY_BANNER.ordinal()] = 1095;
            } catch (NoSuchFieldError nosuchfielderror546) {
                ;
            }

            try {
                aint1[Material.GRAY_BED.ordinal()] = 932;
            } catch (NoSuchFieldError nosuchfielderror547) {
                ;
            }

            try {
                aint1[Material.GRAY_CANDLE.ordinal()] = 1199;
            } catch (NoSuchFieldError nosuchfielderror548) {
                ;
            }

            try {
                aint1[Material.GRAY_CANDLE_CAKE.ordinal()] = 1388;
            } catch (NoSuchFieldError nosuchfielderror549) {
                ;
            }

            try {
                aint1[Material.GRAY_CARPET.ordinal()] = 432;
            } catch (NoSuchFieldError nosuchfielderror550) {
                ;
            }

            try {
                aint1[Material.GRAY_CONCRETE.ordinal()] = 541;
            } catch (NoSuchFieldError nosuchfielderror551) {
                ;
            }

            try {
                aint1[Material.GRAY_CONCRETE_POWDER.ordinal()] = 557;
            } catch (NoSuchFieldError nosuchfielderror552) {
                ;
            }

            try {
                aint1[Material.GRAY_DYE.ordinal()] = 912;
            } catch (NoSuchFieldError nosuchfielderror553) {
                ;
            }

            try {
                aint1[Material.GRAY_GLAZED_TERRACOTTA.ordinal()] = 525;
            } catch (NoSuchFieldError nosuchfielderror554) {
                ;
            }

            try {
                aint1[Material.GRAY_SHULKER_BOX.ordinal()] = 509;
            } catch (NoSuchFieldError nosuchfielderror555) {
                ;
            }

            try {
                aint1[Material.GRAY_STAINED_GLASS.ordinal()] = 457;
            } catch (NoSuchFieldError nosuchfielderror556) {
                ;
            }

            try {
                aint1[Material.GRAY_STAINED_GLASS_PANE.ordinal()] = 473;
            } catch (NoSuchFieldError nosuchfielderror557) {
                ;
            }

            try {
                aint1[Material.GRAY_TERRACOTTA.ordinal()] = 413;
            } catch (NoSuchFieldError nosuchfielderror558) {
                ;
            }

            try {
                aint1[Material.GRAY_WALL_BANNER.ordinal()] = 1341;
            } catch (NoSuchFieldError nosuchfielderror559) {
                ;
            }

            try {
                aint1[Material.GRAY_WOOL.ordinal()] = 188;
            } catch (NoSuchFieldError nosuchfielderror560) {
                ;
            }

            try {
                aint1[Material.GREEN_BANNER.ordinal()] = 1101;
            } catch (NoSuchFieldError nosuchfielderror561) {
                ;
            }

            try {
                aint1[Material.GREEN_BED.ordinal()] = 938;
            } catch (NoSuchFieldError nosuchfielderror562) {
                ;
            }

            try {
                aint1[Material.GREEN_CANDLE.ordinal()] = 1205;
            } catch (NoSuchFieldError nosuchfielderror563) {
                ;
            }

            try {
                aint1[Material.GREEN_CANDLE_CAKE.ordinal()] = 1394;
            } catch (NoSuchFieldError nosuchfielderror564) {
                ;
            }

            try {
                aint1[Material.GREEN_CARPET.ordinal()] = 438;
            } catch (NoSuchFieldError nosuchfielderror565) {
                ;
            }

            try {
                aint1[Material.GREEN_CONCRETE.ordinal()] = 547;
            } catch (NoSuchFieldError nosuchfielderror566) {
                ;
            }

            try {
                aint1[Material.GREEN_CONCRETE_POWDER.ordinal()] = 563;
            } catch (NoSuchFieldError nosuchfielderror567) {
                ;
            }

            try {
                aint1[Material.GREEN_DYE.ordinal()] = 918;
            } catch (NoSuchFieldError nosuchfielderror568) {
                ;
            }

            try {
                aint1[Material.GREEN_GLAZED_TERRACOTTA.ordinal()] = 531;
            } catch (NoSuchFieldError nosuchfielderror569) {
                ;
            }

            try {
                aint1[Material.GREEN_SHULKER_BOX.ordinal()] = 515;
            } catch (NoSuchFieldError nosuchfielderror570) {
                ;
            }

            try {
                aint1[Material.GREEN_STAINED_GLASS.ordinal()] = 463;
            } catch (NoSuchFieldError nosuchfielderror571) {
                ;
            }

            try {
                aint1[Material.GREEN_STAINED_GLASS_PANE.ordinal()] = 479;
            } catch (NoSuchFieldError nosuchfielderror572) {
                ;
            }

            try {
                aint1[Material.GREEN_TERRACOTTA.ordinal()] = 419;
            } catch (NoSuchFieldError nosuchfielderror573) {
                ;
            }

            try {
                aint1[Material.GREEN_WALL_BANNER.ordinal()] = 1347;
            } catch (NoSuchFieldError nosuchfielderror574) {
                ;
            }

            try {
                aint1[Material.GREEN_WOOL.ordinal()] = 194;
            } catch (NoSuchFieldError nosuchfielderror575) {
                ;
            }

            try {
                aint1[Material.GRINDSTONE.ordinal()] = 1160;
            } catch (NoSuchFieldError nosuchfielderror576) {
                ;
            }

            try {
                aint1[Material.GUARDIAN_SPAWN_EGG.ordinal()] = 993;
            } catch (NoSuchFieldError nosuchfielderror577) {
                ;
            }

            try {
                aint1[Material.GUNPOWDER.ordinal()] = 813;
            } catch (NoSuchFieldError nosuchfielderror578) {
                ;
            }

            try {
                aint1[Material.HANGING_ROOTS.ordinal()] = 227;
            } catch (NoSuchFieldError nosuchfielderror579) {
                ;
            }

            try {
                aint1[Material.HAY_BLOCK.ordinal()] = 424;
            } catch (NoSuchFieldError nosuchfielderror580) {
                ;
            }

            try {
                aint1[Material.HEARTBREAK_POTTERY_SHERD.ordinal()] = 1246;
            } catch (NoSuchFieldError nosuchfielderror581) {
                ;
            }

            try {
                aint1[Material.HEART_OF_THE_SEA.ordinal()] = 1143;
            } catch (NoSuchFieldError nosuchfielderror582) {
                ;
            }

            try {
                aint1[Material.HEART_POTTERY_SHERD.ordinal()] = 1245;
            } catch (NoSuchFieldError nosuchfielderror583) {
                ;
            }

            try {
                aint1[Material.HEAVY_WEIGHTED_PRESSURE_PLATE.ordinal()] = 677;
            } catch (NoSuchFieldError nosuchfielderror584) {
                ;
            }

            try {
                aint1[Material.HOGLIN_SPAWN_EGG.ordinal()] = 994;
            } catch (NoSuchFieldError nosuchfielderror585) {
                ;
            }

            try {
                aint1[Material.HONEYCOMB.ordinal()] = 1171;
            } catch (NoSuchFieldError nosuchfielderror586) {
                ;
            }

            try {
                aint1[Material.HONEYCOMB_BLOCK.ordinal()] = 1175;
            } catch (NoSuchFieldError nosuchfielderror587) {
                ;
            }

            try {
                aint1[Material.HONEY_BLOCK.ordinal()] = 644;
            } catch (NoSuchFieldError nosuchfielderror588) {
                ;
            }

            try {
                aint1[Material.HONEY_BOTTLE.ordinal()] = 1174;
            } catch (NoSuchFieldError nosuchfielderror589) {
                ;
            }

            try {
                aint1[Material.HOPPER.ordinal()] = 646;
            } catch (NoSuchFieldError nosuchfielderror590) {
                ;
            }

            try {
                aint1[Material.HOPPER_MINECART.ordinal()] = 733;
            } catch (NoSuchFieldError nosuchfielderror591) {
                ;
            }

            try {
                aint1[Material.HORN_CORAL.ordinal()] = 582;
            } catch (NoSuchFieldError nosuchfielderror592) {
                ;
            }

            try {
                aint1[Material.HORN_CORAL_BLOCK.ordinal()] = 577;
            } catch (NoSuchFieldError nosuchfielderror593) {
                ;
            }

            try {
                aint1[Material.HORN_CORAL_FAN.ordinal()] = 592;
            } catch (NoSuchFieldError nosuchfielderror594) {
                ;
            }

            try {
                aint1[Material.HORN_CORAL_WALL_FAN.ordinal()] = 1365;
            } catch (NoSuchFieldError nosuchfielderror595) {
                ;
            }

            try {
                aint1[Material.HORSE_SPAWN_EGG.ordinal()] = 995;
            } catch (NoSuchFieldError nosuchfielderror596) {
                ;
            }

            try {
                aint1[Material.HOST_ARMOR_TRIM_SMITHING_TEMPLATE.ordinal()] = 1235;
            } catch (NoSuchFieldError nosuchfielderror597) {
                ;
            }

            try {
                aint1[Material.HOWL_POTTERY_SHERD.ordinal()] = 1247;
            } catch (NoSuchFieldError nosuchfielderror598) {
                ;
            }

            try {
                aint1[Material.HUSK_SPAWN_EGG.ordinal()] = 996;
            } catch (NoSuchFieldError nosuchfielderror599) {
                ;
            }

            try {
                aint1[Material.ICE.ordinal()] = 285;
            } catch (NoSuchFieldError nosuchfielderror600) {
                ;
            }

            try {
                aint1[Material.INFESTED_CHISELED_STONE_BRICKS.ordinal()] = 317;
            } catch (NoSuchFieldError nosuchfielderror601) {
                ;
            }

            try {
                aint1[Material.INFESTED_COBBLESTONE.ordinal()] = 313;
            } catch (NoSuchFieldError nosuchfielderror602) {
                ;
            }

            try {
                aint1[Material.INFESTED_CRACKED_STONE_BRICKS.ordinal()] = 316;
            } catch (NoSuchFieldError nosuchfielderror603) {
                ;
            }

            try {
                aint1[Material.INFESTED_DEEPSLATE.ordinal()] = 318;
            } catch (NoSuchFieldError nosuchfielderror604) {
                ;
            }

            try {
                aint1[Material.INFESTED_MOSSY_STONE_BRICKS.ordinal()] = 315;
            } catch (NoSuchFieldError nosuchfielderror605) {
                ;
            }

            try {
                aint1[Material.INFESTED_STONE.ordinal()] = 312;
            } catch (NoSuchFieldError nosuchfielderror606) {
                ;
            }

            try {
                aint1[Material.INFESTED_STONE_BRICKS.ordinal()] = 314;
            } catch (NoSuchFieldError nosuchfielderror607) {
                ;
            }

            try {
                aint1[Material.INK_SAC.ordinal()] = 902;
            } catch (NoSuchFieldError nosuchfielderror608) {
                ;
            }

            try {
                aint1[Material.IRON_AXE.ordinal()] = 796;
            } catch (NoSuchFieldError nosuchfielderror609) {
                ;
            }

            try {
                aint1[Material.IRON_BARS.ordinal()] = 334;
            } catch (NoSuchFieldError nosuchfielderror610) {
                ;
            }

            try {
                aint1[Material.IRON_BLOCK.ordinal()] = 75;
            } catch (NoSuchFieldError nosuchfielderror611) {
                ;
            }

            try {
                aint1[Material.IRON_BOOTS.ordinal()] = 828;
            } catch (NoSuchFieldError nosuchfielderror612) {
                ;
            }

            try {
                aint1[Material.IRON_CHESTPLATE.ordinal()] = 826;
            } catch (NoSuchFieldError nosuchfielderror613) {
                ;
            }

            try {
                aint1[Material.IRON_DOOR.ordinal()] = 689;
            } catch (NoSuchFieldError nosuchfielderror614) {
                ;
            }

            try {
                aint1[Material.IRON_GOLEM_SPAWN_EGG.ordinal()] = 997;
            } catch (NoSuchFieldError nosuchfielderror615) {
                ;
            }

            try {
                aint1[Material.IRON_HELMET.ordinal()] = 825;
            } catch (NoSuchFieldError nosuchfielderror616) {
                ;
            }

            try {
                aint1[Material.IRON_HOE.ordinal()] = 797;
            } catch (NoSuchFieldError nosuchfielderror617) {
                ;
            }

            try {
                aint1[Material.IRON_HORSE_ARMOR.ordinal()] = 1079;
            } catch (NoSuchFieldError nosuchfielderror618) {
                ;
            }

            try {
                aint1[Material.IRON_INGOT.ordinal()] = 771;
            } catch (NoSuchFieldError nosuchfielderror619) {
                ;
            }

            try {
                aint1[Material.IRON_LEGGINGS.ordinal()] = 827;
            } catch (NoSuchFieldError nosuchfielderror620) {
                ;
            }

            try {
                aint1[Material.IRON_NUGGET.ordinal()] = 1120;
            } catch (NoSuchFieldError nosuchfielderror621) {
                ;
            }

            try {
                aint1[Material.IRON_ORE.ordinal()] = 52;
            } catch (NoSuchFieldError nosuchfielderror622) {
                ;
            }

            try {
                aint1[Material.IRON_PICKAXE.ordinal()] = 795;
            } catch (NoSuchFieldError nosuchfielderror623) {
                ;
            }

            try {
                aint1[Material.IRON_SHOVEL.ordinal()] = 794;
            } catch (NoSuchFieldError nosuchfielderror624) {
                ;
            }

            try {
                aint1[Material.IRON_SWORD.ordinal()] = 793;
            } catch (NoSuchFieldError nosuchfielderror625) {
                ;
            }

            try {
                aint1[Material.IRON_TRAPDOOR.ordinal()] = 701;
            } catch (NoSuchFieldError nosuchfielderror626) {
                ;
            }

            try {
                aint1[Material.ITEM_FRAME.ordinal()] = 1049;
            } catch (NoSuchFieldError nosuchfielderror627) {
                ;
            }

            try {
                aint1[Material.JACK_O_LANTERN.ordinal()] = 303;
            } catch (NoSuchFieldError nosuchfielderror628) {
                ;
            }

            try {
                aint1[Material.JIGSAW.ordinal()] = 756;
            } catch (NoSuchFieldError nosuchfielderror629) {
                ;
            }

            try {
                aint1[Material.JUKEBOX.ordinal()] = 289;
            } catch (NoSuchFieldError nosuchfielderror630) {
                ;
            }

            try {
                aint1[Material.JUNGLE_BOAT.ordinal()] = 743;
            } catch (NoSuchFieldError nosuchfielderror631) {
                ;
            }

            try {
                aint1[Material.JUNGLE_BUTTON.ordinal()] = 666;
            } catch (NoSuchFieldError nosuchfielderror632) {
                ;
            }

            try {
                aint1[Material.JUNGLE_CHEST_BOAT.ordinal()] = 744;
            } catch (NoSuchFieldError nosuchfielderror633) {
                ;
            }

            try {
                aint1[Material.JUNGLE_DOOR.ordinal()] = 693;
            } catch (NoSuchFieldError nosuchfielderror634) {
                ;
            }

            try {
                aint1[Material.JUNGLE_FENCE.ordinal()] = 293;
            } catch (NoSuchFieldError nosuchfielderror635) {
                ;
            }

            try {
                aint1[Material.JUNGLE_FENCE_GATE.ordinal()] = 716;
            } catch (NoSuchFieldError nosuchfielderror636) {
                ;
            }

            try {
                aint1[Material.JUNGLE_HANGING_SIGN.ordinal()] = 861;
            } catch (NoSuchFieldError nosuchfielderror637) {
                ;
            }

            try {
                aint1[Material.JUNGLE_LEAVES.ordinal()] = 158;
            } catch (NoSuchFieldError nosuchfielderror638) {
                ;
            }

            try {
                aint1[Material.JUNGLE_LOG.ordinal()] = 114;
            } catch (NoSuchFieldError nosuchfielderror639) {
                ;
            }

            try {
                aint1[Material.JUNGLE_PLANKS.ordinal()] = 27;
            } catch (NoSuchFieldError nosuchfielderror640) {
                ;
            }

            try {
                aint1[Material.JUNGLE_PRESSURE_PLATE.ordinal()] = 681;
            } catch (NoSuchFieldError nosuchfielderror641) {
                ;
            }

            try {
                aint1[Material.JUNGLE_SAPLING.ordinal()] = 39;
            } catch (NoSuchFieldError nosuchfielderror642) {
                ;
            }

            try {
                aint1[Material.JUNGLE_SIGN.ordinal()] = 850;
            } catch (NoSuchFieldError nosuchfielderror643) {
                ;
            }

            try {
                aint1[Material.JUNGLE_SLAB.ordinal()] = 234;
            } catch (NoSuchFieldError nosuchfielderror644) {
                ;
            }

            try {
                aint1[Material.JUNGLE_STAIRS.ordinal()] = 365;
            } catch (NoSuchFieldError nosuchfielderror645) {
                ;
            }

            try {
                aint1[Material.JUNGLE_TRAPDOOR.ordinal()] = 705;
            } catch (NoSuchFieldError nosuchfielderror646) {
                ;
            }

            try {
                aint1[Material.JUNGLE_WALL_HANGING_SIGN.ordinal()] = 1279;
            } catch (NoSuchFieldError nosuchfielderror647) {
                ;
            }

            try {
                aint1[Material.JUNGLE_WALL_SIGN.ordinal()] = 1270;
            } catch (NoSuchFieldError nosuchfielderror648) {
                ;
            }

            try {
                aint1[Material.JUNGLE_WOOD.ordinal()] = 148;
            } catch (NoSuchFieldError nosuchfielderror649) {
                ;
            }

            try {
                aint1[Material.KELP.ordinal()] = 223;
            } catch (NoSuchFieldError nosuchfielderror650) {
                ;
            }

            try {
                aint1[Material.KELP_PLANT.ordinal()] = 1355;
            } catch (NoSuchFieldError nosuchfielderror651) {
                ;
            }

            try {
                aint1[Material.KNOWLEDGE_BOOK.ordinal()] = 1121;
            } catch (NoSuchFieldError nosuchfielderror652) {
                ;
            }

            try {
                aint1[Material.LADDER.ordinal()] = 282;
            } catch (NoSuchFieldError nosuchfielderror653) {
                ;
            }

            try {
                aint1[Material.LANTERN.ordinal()] = 1164;
            } catch (NoSuchFieldError nosuchfielderror654) {
                ;
            }

            try {
                aint1[Material.LAPIS_BLOCK.ordinal()] = 169;
            } catch (NoSuchFieldError nosuchfielderror655) {
                ;
            }

            try {
                aint1[Material.LAPIS_LAZULI.ordinal()] = 767;
            } catch (NoSuchFieldError nosuchfielderror656) {
                ;
            }

            try {
                aint1[Material.LAPIS_ORE.ordinal()] = 62;
            } catch (NoSuchFieldError nosuchfielderror657) {
                ;
            }

            try {
                aint1[Material.LARGE_AMETHYST_BUD.ordinal()] = 1210;
            } catch (NoSuchFieldError nosuchfielderror658) {
                ;
            }

            try {
                aint1[Material.LARGE_FERN.ordinal()] = 449;
            } catch (NoSuchFieldError nosuchfielderror659) {
                ;
            }

            try {
                aint1[Material.LAVA.ordinal()] = 1257;
            } catch (NoSuchFieldError nosuchfielderror660) {
                ;
            }

            try {
                aint1[Material.LAVA_BUCKET.ordinal()] = 871;
            } catch (NoSuchFieldError nosuchfielderror661) {
                ;
            }

            try {
                aint1[Material.LAVA_CAULDRON.ordinal()] = 1293;
            } catch (NoSuchFieldError nosuchfielderror662) {
                ;
            }

            try {
                aint1[Material.LEAD.ordinal()] = 1083;
            } catch (NoSuchFieldError nosuchfielderror663) {
                ;
            }

            try {
                aint1[Material.LEATHER.ordinal()] = 874;
            } catch (NoSuchFieldError nosuchfielderror664) {
                ;
            }

            try {
                aint1[Material.LEATHER_BOOTS.ordinal()] = 820;
            } catch (NoSuchFieldError nosuchfielderror665) {
                ;
            }

            try {
                aint1[Material.LEATHER_CHESTPLATE.ordinal()] = 818;
            } catch (NoSuchFieldError nosuchfielderror666) {
                ;
            }

            try {
                aint1[Material.LEATHER_HELMET.ordinal()] = 817;
            } catch (NoSuchFieldError nosuchfielderror667) {
                ;
            }

            try {
                aint1[Material.LEATHER_HORSE_ARMOR.ordinal()] = 1082;
            } catch (NoSuchFieldError nosuchfielderror668) {
                ;
            }

            try {
                aint1[Material.LEATHER_LEGGINGS.ordinal()] = 819;
            } catch (NoSuchFieldError nosuchfielderror669) {
                ;
            }

            try {
                aint1[Material.LECTERN.ordinal()] = 649;
            } catch (NoSuchFieldError nosuchfielderror670) {
                ;
            }

            try {
                aint1[Material.LEGACY_ACACIA_DOOR.ordinal()] = 1599;
            } catch (NoSuchFieldError nosuchfielderror671) {
                ;
            }

            try {
                aint1[Material.LEGACY_ACACIA_DOOR_ITEM.ordinal()] = 1831;
            } catch (NoSuchFieldError nosuchfielderror672) {
                ;
            }

            try {
                aint1[Material.LEGACY_ACACIA_FENCE.ordinal()] = 1595;
            } catch (NoSuchFieldError nosuchfielderror673) {
                ;
            }

            try {
                aint1[Material.LEGACY_ACACIA_FENCE_GATE.ordinal()] = 1590;
            } catch (NoSuchFieldError nosuchfielderror674) {
                ;
            }

            try {
                aint1[Material.LEGACY_ACACIA_STAIRS.ordinal()] = 1566;
            } catch (NoSuchFieldError nosuchfielderror675) {
                ;
            }

            try {
                aint1[Material.LEGACY_ACTIVATOR_RAIL.ordinal()] = 1560;
            } catch (NoSuchFieldError nosuchfielderror676) {
                ;
            }

            try {
                aint1[Material.LEGACY_AIR.ordinal()] = 1403;
            } catch (NoSuchFieldError nosuchfielderror677) {
                ;
            }

            try {
                aint1[Material.LEGACY_ANVIL.ordinal()] = 1548;
            } catch (NoSuchFieldError nosuchfielderror678) {
                ;
            }

            try {
                aint1[Material.LEGACY_APPLE.ordinal()] = 1661;
            } catch (NoSuchFieldError nosuchfielderror679) {
                ;
            }

            try {
                aint1[Material.LEGACY_ARMOR_STAND.ordinal()] = 1817;
            } catch (NoSuchFieldError nosuchfielderror680) {
                ;
            }

            try {
                aint1[Material.LEGACY_ARROW.ordinal()] = 1663;
            } catch (NoSuchFieldError nosuchfielderror681) {
                ;
            }

            try {
                aint1[Material.LEGACY_BAKED_POTATO.ordinal()] = 1794;
            } catch (NoSuchFieldError nosuchfielderror682) {
                ;
            }

            try {
                aint1[Material.LEGACY_BANNER.ordinal()] = 1826;
            } catch (NoSuchFieldError nosuchfielderror683) {
                ;
            }

            try {
                aint1[Material.LEGACY_BARRIER.ordinal()] = 1569;
            } catch (NoSuchFieldError nosuchfielderror684) {
                ;
            }

            try {
                aint1[Material.LEGACY_BEACON.ordinal()] = 1541;
            } catch (NoSuchFieldError nosuchfielderror685) {
                ;
            }

            try {
                aint1[Material.LEGACY_BED.ordinal()] = 1756;
            } catch (NoSuchFieldError nosuchfielderror686) {
                ;
            }

            try {
                aint1[Material.LEGACY_BEDROCK.ordinal()] = 1410;
            } catch (NoSuchFieldError nosuchfielderror687) {
                ;
            }

            try {
                aint1[Material.LEGACY_BED_BLOCK.ordinal()] = 1429;
            } catch (NoSuchFieldError nosuchfielderror688) {
                ;
            }

            try {
                aint1[Material.LEGACY_BEETROOT.ordinal()] = 1835;
            } catch (NoSuchFieldError nosuchfielderror689) {
                ;
            }

            try {
                aint1[Material.LEGACY_BEETROOT_BLOCK.ordinal()] = 1610;
            } catch (NoSuchFieldError nosuchfielderror690) {
                ;
            }

            try {
                aint1[Material.LEGACY_BEETROOT_SEEDS.ordinal()] = 1836;
            } catch (NoSuchFieldError nosuchfielderror691) {
                ;
            }

            try {
                aint1[Material.LEGACY_BEETROOT_SOUP.ordinal()] = 1837;
            } catch (NoSuchFieldError nosuchfielderror692) {
                ;
            }

            try {
                aint1[Material.LEGACY_BIRCH_DOOR.ordinal()] = 1597;
            } catch (NoSuchFieldError nosuchfielderror693) {
                ;
            }

            try {
                aint1[Material.LEGACY_BIRCH_DOOR_ITEM.ordinal()] = 1829;
            } catch (NoSuchFieldError nosuchfielderror694) {
                ;
            }

            try {
                aint1[Material.LEGACY_BIRCH_FENCE.ordinal()] = 1592;
            } catch (NoSuchFieldError nosuchfielderror695) {
                ;
            }

            try {
                aint1[Material.LEGACY_BIRCH_FENCE_GATE.ordinal()] = 1587;
            } catch (NoSuchFieldError nosuchfielderror696) {
                ;
            }

            try {
                aint1[Material.LEGACY_BIRCH_WOOD_STAIRS.ordinal()] = 1538;
            } catch (NoSuchFieldError nosuchfielderror697) {
                ;
            }

            try {
                aint1[Material.LEGACY_BLACK_GLAZED_TERRACOTTA.ordinal()] = 1653;
            } catch (NoSuchFieldError nosuchfielderror698) {
                ;
            }

            try {
                aint1[Material.LEGACY_BLACK_SHULKER_BOX.ordinal()] = 1637;
            } catch (NoSuchFieldError nosuchfielderror699) {
                ;
            }

            try {
                aint1[Material.LEGACY_BLAZE_POWDER.ordinal()] = 1778;
            } catch (NoSuchFieldError nosuchfielderror700) {
                ;
            }

            try {
                aint1[Material.LEGACY_BLAZE_ROD.ordinal()] = 1770;
            } catch (NoSuchFieldError nosuchfielderror701) {
                ;
            }

            try {
                aint1[Material.LEGACY_BLUE_GLAZED_TERRACOTTA.ordinal()] = 1649;
            } catch (NoSuchFieldError nosuchfielderror702) {
                ;
            }

            try {
                aint1[Material.LEGACY_BLUE_SHULKER_BOX.ordinal()] = 1633;
            } catch (NoSuchFieldError nosuchfielderror703) {
                ;
            }

            try {
                aint1[Material.LEGACY_BOAT.ordinal()] = 1734;
            } catch (NoSuchFieldError nosuchfielderror704) {
                ;
            }

            try {
                aint1[Material.LEGACY_BOAT_ACACIA.ordinal()] = 1848;
            } catch (NoSuchFieldError nosuchfielderror705) {
                ;
            }

            try {
                aint1[Material.LEGACY_BOAT_BIRCH.ordinal()] = 1846;
            } catch (NoSuchFieldError nosuchfielderror706) {
                ;
            }

            try {
                aint1[Material.LEGACY_BOAT_DARK_OAK.ordinal()] = 1849;
            } catch (NoSuchFieldError nosuchfielderror707) {
                ;
            }

            try {
                aint1[Material.LEGACY_BOAT_JUNGLE.ordinal()] = 1847;
            } catch (NoSuchFieldError nosuchfielderror708) {
                ;
            }

            try {
                aint1[Material.LEGACY_BOAT_SPRUCE.ordinal()] = 1845;
            } catch (NoSuchFieldError nosuchfielderror709) {
                ;
            }

            try {
                aint1[Material.LEGACY_BONE.ordinal()] = 1753;
            } catch (NoSuchFieldError nosuchfielderror710) {
                ;
            }

            try {
                aint1[Material.LEGACY_BONE_BLOCK.ordinal()] = 1619;
            } catch (NoSuchFieldError nosuchfielderror711) {
                ;
            }

            try {
                aint1[Material.LEGACY_BOOK.ordinal()] = 1741;
            } catch (NoSuchFieldError nosuchfielderror712) {
                ;
            }

            try {
                aint1[Material.LEGACY_BOOKSHELF.ordinal()] = 1450;
            } catch (NoSuchFieldError nosuchfielderror713) {
                ;
            }

            try {
                aint1[Material.LEGACY_BOOK_AND_QUILL.ordinal()] = 1787;
            } catch (NoSuchFieldError nosuchfielderror714) {
                ;
            }

            try {
                aint1[Material.LEGACY_BOW.ordinal()] = 1662;
            } catch (NoSuchFieldError nosuchfielderror715) {
                ;
            }

            try {
                aint1[Material.LEGACY_BOWL.ordinal()] = 1682;
            } catch (NoSuchFieldError nosuchfielderror716) {
                ;
            }

            try {
                aint1[Material.LEGACY_BREAD.ordinal()] = 1698;
            } catch (NoSuchFieldError nosuchfielderror717) {
                ;
            }

            try {
                aint1[Material.LEGACY_BREWING_STAND.ordinal()] = 1520;
            } catch (NoSuchFieldError nosuchfielderror718) {
                ;
            }

            try {
                aint1[Material.LEGACY_BREWING_STAND_ITEM.ordinal()] = 1780;
            } catch (NoSuchFieldError nosuchfielderror719) {
                ;
            }

            try {
                aint1[Material.LEGACY_BRICK.ordinal()] = 1448;
            } catch (NoSuchFieldError nosuchfielderror720) {
                ;
            }

            try {
                aint1[Material.LEGACY_BRICK_STAIRS.ordinal()] = 1511;
            } catch (NoSuchFieldError nosuchfielderror721) {
                ;
            }

            try {
                aint1[Material.LEGACY_BROWN_GLAZED_TERRACOTTA.ordinal()] = 1650;
            } catch (NoSuchFieldError nosuchfielderror722) {
                ;
            }

            try {
                aint1[Material.LEGACY_BROWN_MUSHROOM.ordinal()] = 1442;
            } catch (NoSuchFieldError nosuchfielderror723) {
                ;
            }

            try {
                aint1[Material.LEGACY_BROWN_SHULKER_BOX.ordinal()] = 1634;
            } catch (NoSuchFieldError nosuchfielderror724) {
                ;
            }

            try {
                aint1[Material.LEGACY_BUCKET.ordinal()] = 1726;
            } catch (NoSuchFieldError nosuchfielderror725) {
                ;
            }

            try {
                aint1[Material.LEGACY_BURNING_FURNACE.ordinal()] = 1465;
            } catch (NoSuchFieldError nosuchfielderror726) {
                ;
            }

            try {
                aint1[Material.LEGACY_CACTUS.ordinal()] = 1484;
            } catch (NoSuchFieldError nosuchfielderror727) {
                ;
            }

            try {
                aint1[Material.LEGACY_CAKE.ordinal()] = 1755;
            } catch (NoSuchFieldError nosuchfielderror728) {
                ;
            }

            try {
                aint1[Material.LEGACY_CAKE_BLOCK.ordinal()] = 1495;
            } catch (NoSuchFieldError nosuchfielderror729) {
                ;
            }

            try {
                aint1[Material.LEGACY_CARPET.ordinal()] = 1574;
            } catch (NoSuchFieldError nosuchfielderror730) {
                ;
            }

            try {
                aint1[Material.LEGACY_CARROT.ordinal()] = 1544;
            } catch (NoSuchFieldError nosuchfielderror731) {
                ;
            }

            try {
                aint1[Material.LEGACY_CARROT_ITEM.ordinal()] = 1792;
            } catch (NoSuchFieldError nosuchfielderror732) {
                ;
            }

            try {
                aint1[Material.LEGACY_CARROT_STICK.ordinal()] = 1799;
            } catch (NoSuchFieldError nosuchfielderror733) {
                ;
            }

            try {
                aint1[Material.LEGACY_CAULDRON.ordinal()] = 1521;
            } catch (NoSuchFieldError nosuchfielderror734) {
                ;
            }

            try {
                aint1[Material.LEGACY_CAULDRON_ITEM.ordinal()] = 1781;
            } catch (NoSuchFieldError nosuchfielderror735) {
                ;
            }

            try {
                aint1[Material.LEGACY_CHAINMAIL_BOOTS.ordinal()] = 1706;
            } catch (NoSuchFieldError nosuchfielderror736) {
                ;
            }

            try {
                aint1[Material.LEGACY_CHAINMAIL_CHESTPLATE.ordinal()] = 1704;
            } catch (NoSuchFieldError nosuchfielderror737) {
                ;
            }

            try {
                aint1[Material.LEGACY_CHAINMAIL_HELMET.ordinal()] = 1703;
            } catch (NoSuchFieldError nosuchfielderror738) {
                ;
            }

            try {
                aint1[Material.LEGACY_CHAINMAIL_LEGGINGS.ordinal()] = 1705;
            } catch (NoSuchFieldError nosuchfielderror739) {
                ;
            }

            try {
                aint1[Material.LEGACY_CHEST.ordinal()] = 1457;
            } catch (NoSuchFieldError nosuchfielderror740) {
                ;
            }

            try {
                aint1[Material.LEGACY_CHORUS_FLOWER.ordinal()] = 1603;
            } catch (NoSuchFieldError nosuchfielderror741) {
                ;
            }

            try {
                aint1[Material.LEGACY_CHORUS_FRUIT.ordinal()] = 1833;
            } catch (NoSuchFieldError nosuchfielderror742) {
                ;
            }

            try {
                aint1[Material.LEGACY_CHORUS_FRUIT_POPPED.ordinal()] = 1834;
            } catch (NoSuchFieldError nosuchfielderror743) {
                ;
            }

            try {
                aint1[Material.LEGACY_CHORUS_PLANT.ordinal()] = 1602;
            } catch (NoSuchFieldError nosuchfielderror744) {
                ;
            }

            try {
                aint1[Material.LEGACY_CLAY.ordinal()] = 1485;
            } catch (NoSuchFieldError nosuchfielderror745) {
                ;
            }

            try {
                aint1[Material.LEGACY_CLAY_BALL.ordinal()] = 1738;
            } catch (NoSuchFieldError nosuchfielderror746) {
                ;
            }

            try {
                aint1[Material.LEGACY_CLAY_BRICK.ordinal()] = 1737;
            } catch (NoSuchFieldError nosuchfielderror747) {
                ;
            }

            try {
                aint1[Material.LEGACY_COAL.ordinal()] = 1664;
            } catch (NoSuchFieldError nosuchfielderror748) {
                ;
            }

            try {
                aint1[Material.LEGACY_COAL_BLOCK.ordinal()] = 1576;
            } catch (NoSuchFieldError nosuchfielderror749) {
                ;
            }

            try {
                aint1[Material.LEGACY_COAL_ORE.ordinal()] = 1419;
            } catch (NoSuchFieldError nosuchfielderror750) {
                ;
            }

            try {
                aint1[Material.LEGACY_COBBLESTONE.ordinal()] = 1407;
            } catch (NoSuchFieldError nosuchfielderror751) {
                ;
            }

            try {
                aint1[Material.LEGACY_COBBLESTONE_STAIRS.ordinal()] = 1470;
            } catch (NoSuchFieldError nosuchfielderror752) {
                ;
            }

            try {
                aint1[Material.LEGACY_COBBLE_WALL.ordinal()] = 1542;
            } catch (NoSuchFieldError nosuchfielderror753) {
                ;
            }

            try {
                aint1[Material.LEGACY_COCOA.ordinal()] = 1530;
            } catch (NoSuchFieldError nosuchfielderror754) {
                ;
            }

            try {
                aint1[Material.LEGACY_COMMAND.ordinal()] = 1540;
            } catch (NoSuchFieldError nosuchfielderror755) {
                ;
            }

            try {
                aint1[Material.LEGACY_COMMAND_CHAIN.ordinal()] = 1614;
            } catch (NoSuchFieldError nosuchfielderror756) {
                ;
            }

            try {
                aint1[Material.LEGACY_COMMAND_MINECART.ordinal()] = 1823;
            } catch (NoSuchFieldError nosuchfielderror757) {
                ;
            }

            try {
                aint1[Material.LEGACY_COMMAND_REPEATING.ordinal()] = 1613;
            } catch (NoSuchFieldError nosuchfielderror758) {
                ;
            }

            try {
                aint1[Material.LEGACY_COMPASS.ordinal()] = 1746;
            } catch (NoSuchFieldError nosuchfielderror759) {
                ;
            }

            try {
                aint1[Material.LEGACY_CONCRETE.ordinal()] = 1654;
            } catch (NoSuchFieldError nosuchfielderror760) {
                ;
            }

            try {
                aint1[Material.LEGACY_CONCRETE_POWDER.ordinal()] = 1655;
            } catch (NoSuchFieldError nosuchfielderror761) {
                ;
            }

            try {
                aint1[Material.LEGACY_COOKED_BEEF.ordinal()] = 1765;
            } catch (NoSuchFieldError nosuchfielderror762) {
                ;
            }

            try {
                aint1[Material.LEGACY_COOKED_CHICKEN.ordinal()] = 1767;
            } catch (NoSuchFieldError nosuchfielderror763) {
                ;
            }

            try {
                aint1[Material.LEGACY_COOKED_FISH.ordinal()] = 1751;
            } catch (NoSuchFieldError nosuchfielderror764) {
                ;
            }

            try {
                aint1[Material.LEGACY_COOKED_MUTTON.ordinal()] = 1825;
            } catch (NoSuchFieldError nosuchfielderror765) {
                ;
            }

            try {
                aint1[Material.LEGACY_COOKED_RABBIT.ordinal()] = 1813;
            } catch (NoSuchFieldError nosuchfielderror766) {
                ;
            }

            try {
                aint1[Material.LEGACY_COOKIE.ordinal()] = 1758;
            } catch (NoSuchFieldError nosuchfielderror767) {
                ;
            }

            try {
                aint1[Material.LEGACY_CROPS.ordinal()] = 1462;
            } catch (NoSuchFieldError nosuchfielderror768) {
                ;
            }

            try {
                aint1[Material.LEGACY_CYAN_GLAZED_TERRACOTTA.ordinal()] = 1647;
            } catch (NoSuchFieldError nosuchfielderror769) {
                ;
            }

            try {
                aint1[Material.LEGACY_CYAN_SHULKER_BOX.ordinal()] = 1631;
            } catch (NoSuchFieldError nosuchfielderror770) {
                ;
            }

            try {
                aint1[Material.LEGACY_DARK_OAK_DOOR.ordinal()] = 1600;
            } catch (NoSuchFieldError nosuchfielderror771) {
                ;
            }

            try {
                aint1[Material.LEGACY_DARK_OAK_DOOR_ITEM.ordinal()] = 1832;
            } catch (NoSuchFieldError nosuchfielderror772) {
                ;
            }

            try {
                aint1[Material.LEGACY_DARK_OAK_FENCE.ordinal()] = 1594;
            } catch (NoSuchFieldError nosuchfielderror773) {
                ;
            }

            try {
                aint1[Material.LEGACY_DARK_OAK_FENCE_GATE.ordinal()] = 1589;
            } catch (NoSuchFieldError nosuchfielderror774) {
                ;
            }

            try {
                aint1[Material.LEGACY_DARK_OAK_STAIRS.ordinal()] = 1567;
            } catch (NoSuchFieldError nosuchfielderror775) {
                ;
            }

            try {
                aint1[Material.LEGACY_DAYLIGHT_DETECTOR.ordinal()] = 1554;
            } catch (NoSuchFieldError nosuchfielderror776) {
                ;
            }

            try {
                aint1[Material.LEGACY_DAYLIGHT_DETECTOR_INVERTED.ordinal()] = 1581;
            } catch (NoSuchFieldError nosuchfielderror777) {
                ;
            }

            try {
                aint1[Material.LEGACY_DEAD_BUSH.ordinal()] = 1435;
            } catch (NoSuchFieldError nosuchfielderror778) {
                ;
            }

            try {
                aint1[Material.LEGACY_DETECTOR_RAIL.ordinal()] = 1431;
            } catch (NoSuchFieldError nosuchfielderror779) {
                ;
            }

            try {
                aint1[Material.LEGACY_DIAMOND.ordinal()] = 1665;
            } catch (NoSuchFieldError nosuchfielderror780) {
                ;
            }

            try {
                aint1[Material.LEGACY_DIAMOND_AXE.ordinal()] = 1680;
            } catch (NoSuchFieldError nosuchfielderror781) {
                ;
            }

            try {
                aint1[Material.LEGACY_DIAMOND_BARDING.ordinal()] = 1820;
            } catch (NoSuchFieldError nosuchfielderror782) {
                ;
            }

            try {
                aint1[Material.LEGACY_DIAMOND_BLOCK.ordinal()] = 1460;
            } catch (NoSuchFieldError nosuchfielderror783) {
                ;
            }

            try {
                aint1[Material.LEGACY_DIAMOND_BOOTS.ordinal()] = 1714;
            } catch (NoSuchFieldError nosuchfielderror784) {
                ;
            }

            try {
                aint1[Material.LEGACY_DIAMOND_CHESTPLATE.ordinal()] = 1712;
            } catch (NoSuchFieldError nosuchfielderror785) {
                ;
            }

            try {
                aint1[Material.LEGACY_DIAMOND_HELMET.ordinal()] = 1711;
            } catch (NoSuchFieldError nosuchfielderror786) {
                ;
            }

            try {
                aint1[Material.LEGACY_DIAMOND_HOE.ordinal()] = 1694;
            } catch (NoSuchFieldError nosuchfielderror787) {
                ;
            }

            try {
                aint1[Material.LEGACY_DIAMOND_LEGGINGS.ordinal()] = 1713;
            } catch (NoSuchFieldError nosuchfielderror788) {
                ;
            }

            try {
                aint1[Material.LEGACY_DIAMOND_ORE.ordinal()] = 1459;
            } catch (NoSuchFieldError nosuchfielderror789) {
                ;
            }

            try {
                aint1[Material.LEGACY_DIAMOND_PICKAXE.ordinal()] = 1679;
            } catch (NoSuchFieldError nosuchfielderror790) {
                ;
            }

            try {
                aint1[Material.LEGACY_DIAMOND_SPADE.ordinal()] = 1678;
            } catch (NoSuchFieldError nosuchfielderror791) {
                ;
            }

            try {
                aint1[Material.LEGACY_DIAMOND_SWORD.ordinal()] = 1677;
            } catch (NoSuchFieldError nosuchfielderror792) {
                ;
            }

            try {
                aint1[Material.LEGACY_DIODE.ordinal()] = 1757;
            } catch (NoSuchFieldError nosuchfielderror793) {
                ;
            }

            try {
                aint1[Material.LEGACY_DIODE_BLOCK_OFF.ordinal()] = 1496;
            } catch (NoSuchFieldError nosuchfielderror794) {
                ;
            }

            try {
                aint1[Material.LEGACY_DIODE_BLOCK_ON.ordinal()] = 1497;
            } catch (NoSuchFieldError nosuchfielderror795) {
                ;
            }

            try {
                aint1[Material.LEGACY_DIRT.ordinal()] = 1406;
            } catch (NoSuchFieldError nosuchfielderror796) {
                ;
            }

            try {
                aint1[Material.LEGACY_DISPENSER.ordinal()] = 1426;
            } catch (NoSuchFieldError nosuchfielderror797) {
                ;
            }

            try {
                aint1[Material.LEGACY_DOUBLE_PLANT.ordinal()] = 1578;
            } catch (NoSuchFieldError nosuchfielderror798) {
                ;
            }

            try {
                aint1[Material.LEGACY_DOUBLE_STEP.ordinal()] = 1446;
            } catch (NoSuchFieldError nosuchfielderror799) {
                ;
            }

            try {
                aint1[Material.LEGACY_DOUBLE_STONE_SLAB2.ordinal()] = 1584;
            } catch (NoSuchFieldError nosuchfielderror800) {
                ;
            }

            try {
                aint1[Material.LEGACY_DRAGONS_BREATH.ordinal()] = 1838;
            } catch (NoSuchFieldError nosuchfielderror801) {
                ;
            }

            try {
                aint1[Material.LEGACY_DRAGON_EGG.ordinal()] = 1525;
            } catch (NoSuchFieldError nosuchfielderror802) {
                ;
            }

            try {
                aint1[Material.LEGACY_DROPPER.ordinal()] = 1561;
            } catch (NoSuchFieldError nosuchfielderror803) {
                ;
            }

            try {
                aint1[Material.LEGACY_EGG.ordinal()] = 1745;
            } catch (NoSuchFieldError nosuchfielderror804) {
                ;
            }

            try {
                aint1[Material.LEGACY_ELYTRA.ordinal()] = 1844;
            } catch (NoSuchFieldError nosuchfielderror805) {
                ;
            }

            try {
                aint1[Material.LEGACY_EMERALD.ordinal()] = 1789;
            } catch (NoSuchFieldError nosuchfielderror806) {
                ;
            }

            try {
                aint1[Material.LEGACY_EMERALD_BLOCK.ordinal()] = 1536;
            } catch (NoSuchFieldError nosuchfielderror807) {
                ;
            }

            try {
                aint1[Material.LEGACY_EMERALD_ORE.ordinal()] = 1532;
            } catch (NoSuchFieldError nosuchfielderror808) {
                ;
            }

            try {
                aint1[Material.LEGACY_EMPTY_MAP.ordinal()] = 1796;
            } catch (NoSuchFieldError nosuchfielderror809) {
                ;
            }

            try {
                aint1[Material.LEGACY_ENCHANTED_BOOK.ordinal()] = 1804;
            } catch (NoSuchFieldError nosuchfielderror810) {
                ;
            }

            try {
                aint1[Material.LEGACY_ENCHANTMENT_TABLE.ordinal()] = 1519;
            } catch (NoSuchFieldError nosuchfielderror811) {
                ;
            }

            try {
                aint1[Material.LEGACY_ENDER_CHEST.ordinal()] = 1533;
            } catch (NoSuchFieldError nosuchfielderror812) {
                ;
            }

            try {
                aint1[Material.LEGACY_ENDER_PEARL.ordinal()] = 1769;
            } catch (NoSuchFieldError nosuchfielderror813) {
                ;
            }

            try {
                aint1[Material.LEGACY_ENDER_PORTAL.ordinal()] = 1522;
            } catch (NoSuchFieldError nosuchfielderror814) {
                ;
            }

            try {
                aint1[Material.LEGACY_ENDER_PORTAL_FRAME.ordinal()] = 1523;
            } catch (NoSuchFieldError nosuchfielderror815) {
                ;
            }

            try {
                aint1[Material.LEGACY_ENDER_STONE.ordinal()] = 1524;
            } catch (NoSuchFieldError nosuchfielderror816) {
                ;
            }

            try {
                aint1[Material.LEGACY_END_BRICKS.ordinal()] = 1609;
            } catch (NoSuchFieldError nosuchfielderror817) {
                ;
            }

            try {
                aint1[Material.LEGACY_END_CRYSTAL.ordinal()] = 1827;
            } catch (NoSuchFieldError nosuchfielderror818) {
                ;
            }

            try {
                aint1[Material.LEGACY_END_GATEWAY.ordinal()] = 1612;
            } catch (NoSuchFieldError nosuchfielderror819) {
                ;
            }

            try {
                aint1[Material.LEGACY_END_ROD.ordinal()] = 1601;
            } catch (NoSuchFieldError nosuchfielderror820) {
                ;
            }

            try {
                aint1[Material.LEGACY_EXPLOSIVE_MINECART.ordinal()] = 1808;
            } catch (NoSuchFieldError nosuchfielderror821) {
                ;
            }

            try {
                aint1[Material.LEGACY_EXP_BOTTLE.ordinal()] = 1785;
            } catch (NoSuchFieldError nosuchfielderror822) {
                ;
            }

            try {
                aint1[Material.LEGACY_EYE_OF_ENDER.ordinal()] = 1782;
            } catch (NoSuchFieldError nosuchfielderror823) {
                ;
            }

            try {
                aint1[Material.LEGACY_FEATHER.ordinal()] = 1689;
            } catch (NoSuchFieldError nosuchfielderror824) {
                ;
            }

            try {
                aint1[Material.LEGACY_FENCE.ordinal()] = 1488;
            } catch (NoSuchFieldError nosuchfielderror825) {
                ;
            }

            try {
                aint1[Material.LEGACY_FENCE_GATE.ordinal()] = 1510;
            } catch (NoSuchFieldError nosuchfielderror826) {
                ;
            }

            try {
                aint1[Material.LEGACY_FERMENTED_SPIDER_EYE.ordinal()] = 1777;
            } catch (NoSuchFieldError nosuchfielderror827) {
                ;
            }

            try {
                aint1[Material.LEGACY_FIRE.ordinal()] = 1454;
            } catch (NoSuchFieldError nosuchfielderror828) {
                ;
            }

            try {
                aint1[Material.LEGACY_FIREBALL.ordinal()] = 1786;
            } catch (NoSuchFieldError nosuchfielderror829) {
                ;
            }

            try {
                aint1[Material.LEGACY_FIREWORK.ordinal()] = 1802;
            } catch (NoSuchFieldError nosuchfielderror830) {
                ;
            }

            try {
                aint1[Material.LEGACY_FIREWORK_CHARGE.ordinal()] = 1803;
            } catch (NoSuchFieldError nosuchfielderror831) {
                ;
            }

            try {
                aint1[Material.LEGACY_FISHING_ROD.ordinal()] = 1747;
            } catch (NoSuchFieldError nosuchfielderror832) {
                ;
            }

            try {
                aint1[Material.LEGACY_FLINT.ordinal()] = 1719;
            } catch (NoSuchFieldError nosuchfielderror833) {
                ;
            }

            try {
                aint1[Material.LEGACY_FLINT_AND_STEEL.ordinal()] = 1660;
            } catch (NoSuchFieldError nosuchfielderror834) {
                ;
            }

            try {
                aint1[Material.LEGACY_FLOWER_POT.ordinal()] = 1543;
            } catch (NoSuchFieldError nosuchfielderror835) {
                ;
            }

            try {
                aint1[Material.LEGACY_FLOWER_POT_ITEM.ordinal()] = 1791;
            } catch (NoSuchFieldError nosuchfielderror836) {
                ;
            }

            try {
                aint1[Material.LEGACY_FROSTED_ICE.ordinal()] = 1615;
            } catch (NoSuchFieldError nosuchfielderror837) {
                ;
            }

            try {
                aint1[Material.LEGACY_FURNACE.ordinal()] = 1464;
            } catch (NoSuchFieldError nosuchfielderror838) {
                ;
            }

            try {
                aint1[Material.LEGACY_GHAST_TEAR.ordinal()] = 1771;
            } catch (NoSuchFieldError nosuchfielderror839) {
                ;
            }

            try {
                aint1[Material.LEGACY_GLASS.ordinal()] = 1423;
            } catch (NoSuchFieldError nosuchfielderror840) {
                ;
            }

            try {
                aint1[Material.LEGACY_GLASS_BOTTLE.ordinal()] = 1775;
            } catch (NoSuchFieldError nosuchfielderror841) {
                ;
            }

            try {
                aint1[Material.LEGACY_GLOWING_REDSTONE_ORE.ordinal()] = 1477;
            } catch (NoSuchFieldError nosuchfielderror842) {
                ;
            }

            try {
                aint1[Material.LEGACY_GLOWSTONE.ordinal()] = 1492;
            } catch (NoSuchFieldError nosuchfielderror843) {
                ;
            }

            try {
                aint1[Material.LEGACY_GLOWSTONE_DUST.ordinal()] = 1749;
            } catch (NoSuchFieldError nosuchfielderror844) {
                ;
            }

            try {
                aint1[Material.LEGACY_GOLDEN_APPLE.ordinal()] = 1723;
            } catch (NoSuchFieldError nosuchfielderror845) {
                ;
            }

            try {
                aint1[Material.LEGACY_GOLDEN_CARROT.ordinal()] = 1797;
            } catch (NoSuchFieldError nosuchfielderror846) {
                ;
            }

            try {
                aint1[Material.LEGACY_GOLD_AXE.ordinal()] = 1687;
            } catch (NoSuchFieldError nosuchfielderror847) {
                ;
            }

            try {
                aint1[Material.LEGACY_GOLD_BARDING.ordinal()] = 1819;
            } catch (NoSuchFieldError nosuchfielderror848) {
                ;
            }

            try {
                aint1[Material.LEGACY_GOLD_BLOCK.ordinal()] = 1444;
            } catch (NoSuchFieldError nosuchfielderror849) {
                ;
            }

            try {
                aint1[Material.LEGACY_GOLD_BOOTS.ordinal()] = 1718;
            } catch (NoSuchFieldError nosuchfielderror850) {
                ;
            }

            try {
                aint1[Material.LEGACY_GOLD_CHESTPLATE.ordinal()] = 1716;
            } catch (NoSuchFieldError nosuchfielderror851) {
                ;
            }

            try {
                aint1[Material.LEGACY_GOLD_HELMET.ordinal()] = 1715;
            } catch (NoSuchFieldError nosuchfielderror852) {
                ;
            }

            try {
                aint1[Material.LEGACY_GOLD_HOE.ordinal()] = 1695;
            } catch (NoSuchFieldError nosuchfielderror853) {
                ;
            }

            try {
                aint1[Material.LEGACY_GOLD_INGOT.ordinal()] = 1667;
            } catch (NoSuchFieldError nosuchfielderror854) {
                ;
            }

            try {
                aint1[Material.LEGACY_GOLD_LEGGINGS.ordinal()] = 1717;
            } catch (NoSuchFieldError nosuchfielderror855) {
                ;
            }

            try {
                aint1[Material.LEGACY_GOLD_NUGGET.ordinal()] = 1772;
            } catch (NoSuchFieldError nosuchfielderror856) {
                ;
            }

            try {
                aint1[Material.LEGACY_GOLD_ORE.ordinal()] = 1417;
            } catch (NoSuchFieldError nosuchfielderror857) {
                ;
            }

            try {
                aint1[Material.LEGACY_GOLD_PICKAXE.ordinal()] = 1686;
            } catch (NoSuchFieldError nosuchfielderror858) {
                ;
            }

            try {
                aint1[Material.LEGACY_GOLD_PLATE.ordinal()] = 1550;
            } catch (NoSuchFieldError nosuchfielderror859) {
                ;
            }

            try {
                aint1[Material.LEGACY_GOLD_RECORD.ordinal()] = 1854;
            } catch (NoSuchFieldError nosuchfielderror860) {
                ;
            }

            try {
                aint1[Material.LEGACY_GOLD_SPADE.ordinal()] = 1685;
            } catch (NoSuchFieldError nosuchfielderror861) {
                ;
            }

            try {
                aint1[Material.LEGACY_GOLD_SWORD.ordinal()] = 1684;
            } catch (NoSuchFieldError nosuchfielderror862) {
                ;
            }

            try {
                aint1[Material.LEGACY_GRASS.ordinal()] = 1405;
            } catch (NoSuchFieldError nosuchfielderror863) {
                ;
            }

            try {
                aint1[Material.LEGACY_GRASS_PATH.ordinal()] = 1611;
            } catch (NoSuchFieldError nosuchfielderror864) {
                ;
            }

            try {
                aint1[Material.LEGACY_GRAVEL.ordinal()] = 1416;
            } catch (NoSuchFieldError nosuchfielderror865) {
                ;
            }

            try {
                aint1[Material.LEGACY_GRAY_GLAZED_TERRACOTTA.ordinal()] = 1645;
            } catch (NoSuchFieldError nosuchfielderror866) {
                ;
            }

            try {
                aint1[Material.LEGACY_GRAY_SHULKER_BOX.ordinal()] = 1629;
            } catch (NoSuchFieldError nosuchfielderror867) {
                ;
            }

            try {
                aint1[Material.LEGACY_GREEN_GLAZED_TERRACOTTA.ordinal()] = 1651;
            } catch (NoSuchFieldError nosuchfielderror868) {
                ;
            }

            try {
                aint1[Material.LEGACY_GREEN_RECORD.ordinal()] = 1855;
            } catch (NoSuchFieldError nosuchfielderror869) {
                ;
            }

            try {
                aint1[Material.LEGACY_GREEN_SHULKER_BOX.ordinal()] = 1635;
            } catch (NoSuchFieldError nosuchfielderror870) {
                ;
            }

            try {
                aint1[Material.LEGACY_GRILLED_PORK.ordinal()] = 1721;
            } catch (NoSuchFieldError nosuchfielderror871) {
                ;
            }

            try {
                aint1[Material.LEGACY_HARD_CLAY.ordinal()] = 1575;
            } catch (NoSuchFieldError nosuchfielderror872) {
                ;
            }

            try {
                aint1[Material.LEGACY_HAY_BLOCK.ordinal()] = 1573;
            } catch (NoSuchFieldError nosuchfielderror873) {
                ;
            }

            try {
                aint1[Material.LEGACY_HOPPER.ordinal()] = 1557;
            } catch (NoSuchFieldError nosuchfielderror874) {
                ;
            }

            try {
                aint1[Material.LEGACY_HOPPER_MINECART.ordinal()] = 1809;
            } catch (NoSuchFieldError nosuchfielderror875) {
                ;
            }

            try {
                aint1[Material.LEGACY_HUGE_MUSHROOM_1.ordinal()] = 1502;
            } catch (NoSuchFieldError nosuchfielderror876) {
                ;
            }

            try {
                aint1[Material.LEGACY_HUGE_MUSHROOM_2.ordinal()] = 1503;
            } catch (NoSuchFieldError nosuchfielderror877) {
                ;
            }

            try {
                aint1[Material.LEGACY_ICE.ordinal()] = 1482;
            } catch (NoSuchFieldError nosuchfielderror878) {
                ;
            }

            try {
                aint1[Material.LEGACY_INK_SACK.ordinal()] = 1752;
            } catch (NoSuchFieldError nosuchfielderror879) {
                ;
            }

            try {
                aint1[Material.LEGACY_IRON_AXE.ordinal()] = 1659;
            } catch (NoSuchFieldError nosuchfielderror880) {
                ;
            }

            try {
                aint1[Material.LEGACY_IRON_BARDING.ordinal()] = 1818;
            } catch (NoSuchFieldError nosuchfielderror881) {
                ;
            }

            try {
                aint1[Material.LEGACY_IRON_BLOCK.ordinal()] = 1445;
            } catch (NoSuchFieldError nosuchfielderror882) {
                ;
            }

            try {
                aint1[Material.LEGACY_IRON_BOOTS.ordinal()] = 1710;
            } catch (NoSuchFieldError nosuchfielderror883) {
                ;
            }

            try {
                aint1[Material.LEGACY_IRON_CHESTPLATE.ordinal()] = 1708;
            } catch (NoSuchFieldError nosuchfielderror884) {
                ;
            }

            try {
                aint1[Material.LEGACY_IRON_DOOR.ordinal()] = 1731;
            } catch (NoSuchFieldError nosuchfielderror885) {
                ;
            }

            try {
                aint1[Material.LEGACY_IRON_DOOR_BLOCK.ordinal()] = 1474;
            } catch (NoSuchFieldError nosuchfielderror886) {
                ;
            }

            try {
                aint1[Material.LEGACY_IRON_FENCE.ordinal()] = 1504;
            } catch (NoSuchFieldError nosuchfielderror887) {
                ;
            }

            try {
                aint1[Material.LEGACY_IRON_HELMET.ordinal()] = 1707;
            } catch (NoSuchFieldError nosuchfielderror888) {
                ;
            }

            try {
                aint1[Material.LEGACY_IRON_HOE.ordinal()] = 1693;
            } catch (NoSuchFieldError nosuchfielderror889) {
                ;
            }

            try {
                aint1[Material.LEGACY_IRON_INGOT.ordinal()] = 1666;
            } catch (NoSuchFieldError nosuchfielderror890) {
                ;
            }

            try {
                aint1[Material.LEGACY_IRON_LEGGINGS.ordinal()] = 1709;
            } catch (NoSuchFieldError nosuchfielderror891) {
                ;
            }

            try {
                aint1[Material.LEGACY_IRON_NUGGET.ordinal()] = 1852;
            } catch (NoSuchFieldError nosuchfielderror892) {
                ;
            }

            try {
                aint1[Material.LEGACY_IRON_ORE.ordinal()] = 1418;
            } catch (NoSuchFieldError nosuchfielderror893) {
                ;
            }

            try {
                aint1[Material.LEGACY_IRON_PICKAXE.ordinal()] = 1658;
            } catch (NoSuchFieldError nosuchfielderror894) {
                ;
            }

            try {
                aint1[Material.LEGACY_IRON_PLATE.ordinal()] = 1551;
            } catch (NoSuchFieldError nosuchfielderror895) {
                ;
            }

            try {
                aint1[Material.LEGACY_IRON_SPADE.ordinal()] = 1657;
            } catch (NoSuchFieldError nosuchfielderror896) {
                ;
            }

            try {
                aint1[Material.LEGACY_IRON_SWORD.ordinal()] = 1668;
            } catch (NoSuchFieldError nosuchfielderror897) {
                ;
            }

            try {
                aint1[Material.LEGACY_IRON_TRAPDOOR.ordinal()] = 1570;
            } catch (NoSuchFieldError nosuchfielderror898) {
                ;
            }

            try {
                aint1[Material.LEGACY_ITEM_FRAME.ordinal()] = 1790;
            } catch (NoSuchFieldError nosuchfielderror899) {
                ;
            }

            try {
                aint1[Material.LEGACY_JACK_O_LANTERN.ordinal()] = 1494;
            } catch (NoSuchFieldError nosuchfielderror900) {
                ;
            }

            try {
                aint1[Material.LEGACY_JUKEBOX.ordinal()] = 1487;
            } catch (NoSuchFieldError nosuchfielderror901) {
                ;
            }

            try {
                aint1[Material.LEGACY_JUNGLE_DOOR.ordinal()] = 1598;
            } catch (NoSuchFieldError nosuchfielderror902) {
                ;
            }

            try {
                aint1[Material.LEGACY_JUNGLE_DOOR_ITEM.ordinal()] = 1830;
            } catch (NoSuchFieldError nosuchfielderror903) {
                ;
            }

            try {
                aint1[Material.LEGACY_JUNGLE_FENCE.ordinal()] = 1593;
            } catch (NoSuchFieldError nosuchfielderror904) {
                ;
            }

            try {
                aint1[Material.LEGACY_JUNGLE_FENCE_GATE.ordinal()] = 1588;
            } catch (NoSuchFieldError nosuchfielderror905) {
                ;
            }

            try {
                aint1[Material.LEGACY_JUNGLE_WOOD_STAIRS.ordinal()] = 1539;
            } catch (NoSuchFieldError nosuchfielderror906) {
                ;
            }

            try {
                aint1[Material.LEGACY_KNOWLEDGE_BOOK.ordinal()] = 1853;
            } catch (NoSuchFieldError nosuchfielderror907) {
                ;
            }

            try {
                aint1[Material.LEGACY_LADDER.ordinal()] = 1468;
            } catch (NoSuchFieldError nosuchfielderror908) {
                ;
            }

            try {
                aint1[Material.LEGACY_LAPIS_BLOCK.ordinal()] = 1425;
            } catch (NoSuchFieldError nosuchfielderror909) {
                ;
            }

            try {
                aint1[Material.LEGACY_LAPIS_ORE.ordinal()] = 1424;
            } catch (NoSuchFieldError nosuchfielderror910) {
                ;
            }

            try {
                aint1[Material.LEGACY_LAVA.ordinal()] = 1413;
            } catch (NoSuchFieldError nosuchfielderror911) {
                ;
            }

            try {
                aint1[Material.LEGACY_LAVA_BUCKET.ordinal()] = 1728;
            } catch (NoSuchFieldError nosuchfielderror912) {
                ;
            }

            try {
                aint1[Material.LEGACY_LEASH.ordinal()] = 1821;
            } catch (NoSuchFieldError nosuchfielderror913) {
                ;
            }

            try {
                aint1[Material.LEGACY_LEATHER.ordinal()] = 1735;
            } catch (NoSuchFieldError nosuchfielderror914) {
                ;
            }

            try {
                aint1[Material.LEGACY_LEATHER_BOOTS.ordinal()] = 1702;
            } catch (NoSuchFieldError nosuchfielderror915) {
                ;
            }

            try {
                aint1[Material.LEGACY_LEATHER_CHESTPLATE.ordinal()] = 1700;
            } catch (NoSuchFieldError nosuchfielderror916) {
                ;
            }

            try {
                aint1[Material.LEGACY_LEATHER_HELMET.ordinal()] = 1699;
            } catch (NoSuchFieldError nosuchfielderror917) {
                ;
            }

            try {
                aint1[Material.LEGACY_LEATHER_LEGGINGS.ordinal()] = 1701;
            } catch (NoSuchFieldError nosuchfielderror918) {
                ;
            }

            try {
                aint1[Material.LEGACY_LEAVES.ordinal()] = 1421;
            } catch (NoSuchFieldError nosuchfielderror919) {
                ;
            }

            try {
                aint1[Material.LEGACY_LEAVES_2.ordinal()] = 1564;
            } catch (NoSuchFieldError nosuchfielderror920) {
                ;
            }

            try {
                aint1[Material.LEGACY_LEVER.ordinal()] = 1472;
            } catch (NoSuchFieldError nosuchfielderror921) {
                ;
            }

            try {
                aint1[Material.LEGACY_LIGHT_BLUE_GLAZED_TERRACOTTA.ordinal()] = 1641;
            } catch (NoSuchFieldError nosuchfielderror922) {
                ;
            }

            try {
                aint1[Material.LEGACY_LIGHT_BLUE_SHULKER_BOX.ordinal()] = 1625;
            } catch (NoSuchFieldError nosuchfielderror923) {
                ;
            }

            try {
                aint1[Material.LEGACY_LIME_GLAZED_TERRACOTTA.ordinal()] = 1643;
            } catch (NoSuchFieldError nosuchfielderror924) {
                ;
            }

            try {
                aint1[Material.LEGACY_LIME_SHULKER_BOX.ordinal()] = 1627;
            } catch (NoSuchFieldError nosuchfielderror925) {
                ;
            }

            try {
                aint1[Material.LEGACY_LINGERING_POTION.ordinal()] = 1842;
            } catch (NoSuchFieldError nosuchfielderror926) {
                ;
            }

            try {
                aint1[Material.LEGACY_LOG.ordinal()] = 1420;
            } catch (NoSuchFieldError nosuchfielderror927) {
                ;
            }

            try {
                aint1[Material.LEGACY_LOG_2.ordinal()] = 1565;
            } catch (NoSuchFieldError nosuchfielderror928) {
                ;
            }

            try {
                aint1[Material.LEGACY_LONG_GRASS.ordinal()] = 1434;
            } catch (NoSuchFieldError nosuchfielderror929) {
                ;
            }

            try {
                aint1[Material.LEGACY_MAGENTA_GLAZED_TERRACOTTA.ordinal()] = 1640;
            } catch (NoSuchFieldError nosuchfielderror930) {
                ;
            }

            try {
                aint1[Material.LEGACY_MAGENTA_SHULKER_BOX.ordinal()] = 1624;
            } catch (NoSuchFieldError nosuchfielderror931) {
                ;
            }

            try {
                aint1[Material.LEGACY_MAGMA.ordinal()] = 1616;
            } catch (NoSuchFieldError nosuchfielderror932) {
                ;
            }

            try {
                aint1[Material.LEGACY_MAGMA_CREAM.ordinal()] = 1779;
            } catch (NoSuchFieldError nosuchfielderror933) {
                ;
            }

            try {
                aint1[Material.LEGACY_MAP.ordinal()] = 1759;
            } catch (NoSuchFieldError nosuchfielderror934) {
                ;
            }

            try {
                aint1[Material.LEGACY_MELON.ordinal()] = 1761;
            } catch (NoSuchFieldError nosuchfielderror935) {
                ;
            }

            try {
                aint1[Material.LEGACY_MELON_BLOCK.ordinal()] = 1506;
            } catch (NoSuchFieldError nosuchfielderror936) {
                ;
            }

            try {
                aint1[Material.LEGACY_MELON_SEEDS.ordinal()] = 1763;
            } catch (NoSuchFieldError nosuchfielderror937) {
                ;
            }

            try {
                aint1[Material.LEGACY_MELON_STEM.ordinal()] = 1508;
            } catch (NoSuchFieldError nosuchfielderror938) {
                ;
            }

            try {
                aint1[Material.LEGACY_MILK_BUCKET.ordinal()] = 1736;
            } catch (NoSuchFieldError nosuchfielderror939) {
                ;
            }

            try {
                aint1[Material.LEGACY_MINECART.ordinal()] = 1729;
            } catch (NoSuchFieldError nosuchfielderror940) {
                ;
            }

            try {
                aint1[Material.LEGACY_MOB_SPAWNER.ordinal()] = 1455;
            } catch (NoSuchFieldError nosuchfielderror941) {
                ;
            }

            try {
                aint1[Material.LEGACY_MONSTER_EGG.ordinal()] = 1784;
            } catch (NoSuchFieldError nosuchfielderror942) {
                ;
            }

            try {
                aint1[Material.LEGACY_MONSTER_EGGS.ordinal()] = 1500;
            } catch (NoSuchFieldError nosuchfielderror943) {
                ;
            }

            try {
                aint1[Material.LEGACY_MOSSY_COBBLESTONE.ordinal()] = 1451;
            } catch (NoSuchFieldError nosuchfielderror944) {
                ;
            }

            try {
                aint1[Material.LEGACY_MUSHROOM_SOUP.ordinal()] = 1683;
            } catch (NoSuchFieldError nosuchfielderror945) {
                ;
            }

            try {
                aint1[Material.LEGACY_MUTTON.ordinal()] = 1824;
            } catch (NoSuchFieldError nosuchfielderror946) {
                ;
            }

            try {
                aint1[Material.LEGACY_MYCEL.ordinal()] = 1513;
            } catch (NoSuchFieldError nosuchfielderror947) {
                ;
            }

            try {
                aint1[Material.LEGACY_NAME_TAG.ordinal()] = 1822;
            } catch (NoSuchFieldError nosuchfielderror948) {
                ;
            }

            try {
                aint1[Material.LEGACY_NETHERRACK.ordinal()] = 1490;
            } catch (NoSuchFieldError nosuchfielderror949) {
                ;
            }

            try {
                aint1[Material.LEGACY_NETHER_BRICK.ordinal()] = 1515;
            } catch (NoSuchFieldError nosuchfielderror950) {
                ;
            }

            try {
                aint1[Material.LEGACY_NETHER_BRICK_ITEM.ordinal()] = 1806;
            } catch (NoSuchFieldError nosuchfielderror951) {
                ;
            }

            try {
                aint1[Material.LEGACY_NETHER_BRICK_STAIRS.ordinal()] = 1517;
            } catch (NoSuchFieldError nosuchfielderror952) {
                ;
            }

            try {
                aint1[Material.LEGACY_NETHER_FENCE.ordinal()] = 1516;
            } catch (NoSuchFieldError nosuchfielderror953) {
                ;
            }

            try {
                aint1[Material.LEGACY_NETHER_STALK.ordinal()] = 1773;
            } catch (NoSuchFieldError nosuchfielderror954) {
                ;
            }

            try {
                aint1[Material.LEGACY_NETHER_STAR.ordinal()] = 1800;
            } catch (NoSuchFieldError nosuchfielderror955) {
                ;
            }

            try {
                aint1[Material.LEGACY_NETHER_WARTS.ordinal()] = 1518;
            } catch (NoSuchFieldError nosuchfielderror956) {
                ;
            }

            try {
                aint1[Material.LEGACY_NETHER_WART_BLOCK.ordinal()] = 1617;
            } catch (NoSuchFieldError nosuchfielderror957) {
                ;
            }

            try {
                aint1[Material.LEGACY_NOTE_BLOCK.ordinal()] = 1428;
            } catch (NoSuchFieldError nosuchfielderror958) {
                ;
            }

            try {
                aint1[Material.LEGACY_OBSERVER.ordinal()] = 1621;
            } catch (NoSuchFieldError nosuchfielderror959) {
                ;
            }

            try {
                aint1[Material.LEGACY_OBSIDIAN.ordinal()] = 1452;
            } catch (NoSuchFieldError nosuchfielderror960) {
                ;
            }

            try {
                aint1[Material.LEGACY_ORANGE_GLAZED_TERRACOTTA.ordinal()] = 1639;
            } catch (NoSuchFieldError nosuchfielderror961) {
                ;
            }

            try {
                aint1[Material.LEGACY_ORANGE_SHULKER_BOX.ordinal()] = 1623;
            } catch (NoSuchFieldError nosuchfielderror962) {
                ;
            }

            try {
                aint1[Material.LEGACY_PACKED_ICE.ordinal()] = 1577;
            } catch (NoSuchFieldError nosuchfielderror963) {
                ;
            }

            try {
                aint1[Material.LEGACY_PAINTING.ordinal()] = 1722;
            } catch (NoSuchFieldError nosuchfielderror964) {
                ;
            }

            try {
                aint1[Material.LEGACY_PAPER.ordinal()] = 1740;
            } catch (NoSuchFieldError nosuchfielderror965) {
                ;
            }

            try {
                aint1[Material.LEGACY_PINK_GLAZED_TERRACOTTA.ordinal()] = 1644;
            } catch (NoSuchFieldError nosuchfielderror966) {
                ;
            }

            try {
                aint1[Material.LEGACY_PINK_SHULKER_BOX.ordinal()] = 1628;
            } catch (NoSuchFieldError nosuchfielderror967) {
                ;
            }

            try {
                aint1[Material.LEGACY_PISTON_BASE.ordinal()] = 1436;
            } catch (NoSuchFieldError nosuchfielderror968) {
                ;
            }

            try {
                aint1[Material.LEGACY_PISTON_EXTENSION.ordinal()] = 1437;
            } catch (NoSuchFieldError nosuchfielderror969) {
                ;
            }

            try {
                aint1[Material.LEGACY_PISTON_MOVING_PIECE.ordinal()] = 1439;
            } catch (NoSuchFieldError nosuchfielderror970) {
                ;
            }

            try {
                aint1[Material.LEGACY_PISTON_STICKY_BASE.ordinal()] = 1432;
            } catch (NoSuchFieldError nosuchfielderror971) {
                ;
            }

            try {
                aint1[Material.LEGACY_POISONOUS_POTATO.ordinal()] = 1795;
            } catch (NoSuchFieldError nosuchfielderror972) {
                ;
            }

            try {
                aint1[Material.LEGACY_PORK.ordinal()] = 1720;
            } catch (NoSuchFieldError nosuchfielderror973) {
                ;
            }

            try {
                aint1[Material.LEGACY_PORTAL.ordinal()] = 1493;
            } catch (NoSuchFieldError nosuchfielderror974) {
                ;
            }

            try {
                aint1[Material.LEGACY_POTATO.ordinal()] = 1545;
            } catch (NoSuchFieldError nosuchfielderror975) {
                ;
            }

            try {
                aint1[Material.LEGACY_POTATO_ITEM.ordinal()] = 1793;
            } catch (NoSuchFieldError nosuchfielderror976) {
                ;
            }

            try {
                aint1[Material.LEGACY_POTION.ordinal()] = 1774;
            } catch (NoSuchFieldError nosuchfielderror977) {
                ;
            }

            try {
                aint1[Material.LEGACY_POWERED_MINECART.ordinal()] = 1744;
            } catch (NoSuchFieldError nosuchfielderror978) {
                ;
            }

            try {
                aint1[Material.LEGACY_POWERED_RAIL.ordinal()] = 1430;
            } catch (NoSuchFieldError nosuchfielderror979) {
                ;
            }

            try {
                aint1[Material.LEGACY_PRISMARINE.ordinal()] = 1571;
            } catch (NoSuchFieldError nosuchfielderror980) {
                ;
            }

            try {
                aint1[Material.LEGACY_PRISMARINE_CRYSTALS.ordinal()] = 1811;
            } catch (NoSuchFieldError nosuchfielderror981) {
                ;
            }

            try {
                aint1[Material.LEGACY_PRISMARINE_SHARD.ordinal()] = 1810;
            } catch (NoSuchFieldError nosuchfielderror982) {
                ;
            }

            try {
                aint1[Material.LEGACY_PUMPKIN.ordinal()] = 1489;
            } catch (NoSuchFieldError nosuchfielderror983) {
                ;
            }

            try {
                aint1[Material.LEGACY_PUMPKIN_PIE.ordinal()] = 1801;
            } catch (NoSuchFieldError nosuchfielderror984) {
                ;
            }

            try {
                aint1[Material.LEGACY_PUMPKIN_SEEDS.ordinal()] = 1762;
            } catch (NoSuchFieldError nosuchfielderror985) {
                ;
            }

            try {
                aint1[Material.LEGACY_PUMPKIN_STEM.ordinal()] = 1507;
            } catch (NoSuchFieldError nosuchfielderror986) {
                ;
            }

            try {
                aint1[Material.LEGACY_PURPLE_GLAZED_TERRACOTTA.ordinal()] = 1648;
            } catch (NoSuchFieldError nosuchfielderror987) {
                ;
            }

            try {
                aint1[Material.LEGACY_PURPLE_SHULKER_BOX.ordinal()] = 1632;
            } catch (NoSuchFieldError nosuchfielderror988) {
                ;
            }

            try {
                aint1[Material.LEGACY_PURPUR_BLOCK.ordinal()] = 1604;
            } catch (NoSuchFieldError nosuchfielderror989) {
                ;
            }

            try {
                aint1[Material.LEGACY_PURPUR_DOUBLE_SLAB.ordinal()] = 1607;
            } catch (NoSuchFieldError nosuchfielderror990) {
                ;
            }

            try {
                aint1[Material.LEGACY_PURPUR_PILLAR.ordinal()] = 1605;
            } catch (NoSuchFieldError nosuchfielderror991) {
                ;
            }

            try {
                aint1[Material.LEGACY_PURPUR_SLAB.ordinal()] = 1608;
            } catch (NoSuchFieldError nosuchfielderror992) {
                ;
            }

            try {
                aint1[Material.LEGACY_PURPUR_STAIRS.ordinal()] = 1606;
            } catch (NoSuchFieldError nosuchfielderror993) {
                ;
            }

            try {
                aint1[Material.LEGACY_QUARTZ.ordinal()] = 1807;
            } catch (NoSuchFieldError nosuchfielderror994) {
                ;
            }

            try {
                aint1[Material.LEGACY_QUARTZ_BLOCK.ordinal()] = 1558;
            } catch (NoSuchFieldError nosuchfielderror995) {
                ;
            }

            try {
                aint1[Material.LEGACY_QUARTZ_ORE.ordinal()] = 1556;
            } catch (NoSuchFieldError nosuchfielderror996) {
                ;
            }

            try {
                aint1[Material.LEGACY_QUARTZ_STAIRS.ordinal()] = 1559;
            } catch (NoSuchFieldError nosuchfielderror997) {
                ;
            }

            try {
                aint1[Material.LEGACY_RABBIT.ordinal()] = 1812;
            } catch (NoSuchFieldError nosuchfielderror998) {
                ;
            }

            try {
                aint1[Material.LEGACY_RABBIT_FOOT.ordinal()] = 1815;
            } catch (NoSuchFieldError nosuchfielderror999) {
                ;
            }

            try {
                aint1[Material.LEGACY_RABBIT_HIDE.ordinal()] = 1816;
            } catch (NoSuchFieldError nosuchfielderror1000) {
                ;
            }

            try {
                aint1[Material.LEGACY_RABBIT_STEW.ordinal()] = 1814;
            } catch (NoSuchFieldError nosuchfielderror1001) {
                ;
            }

            try {
                aint1[Material.LEGACY_RAILS.ordinal()] = 1469;
            } catch (NoSuchFieldError nosuchfielderror1002) {
                ;
            }

            try {
                aint1[Material.LEGACY_RAW_BEEF.ordinal()] = 1764;
            } catch (NoSuchFieldError nosuchfielderror1003) {
                ;
            }

            try {
                aint1[Material.LEGACY_RAW_CHICKEN.ordinal()] = 1766;
            } catch (NoSuchFieldError nosuchfielderror1004) {
                ;
            }

            try {
                aint1[Material.LEGACY_RAW_FISH.ordinal()] = 1750;
            } catch (NoSuchFieldError nosuchfielderror1005) {
                ;
            }

            try {
                aint1[Material.LEGACY_RECORD_10.ordinal()] = 1863;
            } catch (NoSuchFieldError nosuchfielderror1006) {
                ;
            }

            try {
                aint1[Material.LEGACY_RECORD_11.ordinal()] = 1864;
            } catch (NoSuchFieldError nosuchfielderror1007) {
                ;
            }

            try {
                aint1[Material.LEGACY_RECORD_12.ordinal()] = 1865;
            } catch (NoSuchFieldError nosuchfielderror1008) {
                ;
            }

            try {
                aint1[Material.LEGACY_RECORD_3.ordinal()] = 1856;
            } catch (NoSuchFieldError nosuchfielderror1009) {
                ;
            }

            try {
                aint1[Material.LEGACY_RECORD_4.ordinal()] = 1857;
            } catch (NoSuchFieldError nosuchfielderror1010) {
                ;
            }

            try {
                aint1[Material.LEGACY_RECORD_5.ordinal()] = 1858;
            } catch (NoSuchFieldError nosuchfielderror1011) {
                ;
            }

            try {
                aint1[Material.LEGACY_RECORD_6.ordinal()] = 1859;
            } catch (NoSuchFieldError nosuchfielderror1012) {
                ;
            }

            try {
                aint1[Material.LEGACY_RECORD_7.ordinal()] = 1860;
            } catch (NoSuchFieldError nosuchfielderror1013) {
                ;
            }

            try {
                aint1[Material.LEGACY_RECORD_8.ordinal()] = 1861;
            } catch (NoSuchFieldError nosuchfielderror1014) {
                ;
            }

            try {
                aint1[Material.LEGACY_RECORD_9.ordinal()] = 1862;
            } catch (NoSuchFieldError nosuchfielderror1015) {
                ;
            }

            try {
                aint1[Material.LEGACY_REDSTONE.ordinal()] = 1732;
            } catch (NoSuchFieldError nosuchfielderror1016) {
                ;
            }

            try {
                aint1[Material.LEGACY_REDSTONE_BLOCK.ordinal()] = 1555;
            } catch (NoSuchFieldError nosuchfielderror1017) {
                ;
            }

            try {
                aint1[Material.LEGACY_REDSTONE_COMPARATOR.ordinal()] = 1805;
            } catch (NoSuchFieldError nosuchfielderror1018) {
                ;
            }

            try {
                aint1[Material.LEGACY_REDSTONE_COMPARATOR_OFF.ordinal()] = 1552;
            } catch (NoSuchFieldError nosuchfielderror1019) {
                ;
            }

            try {
                aint1[Material.LEGACY_REDSTONE_COMPARATOR_ON.ordinal()] = 1553;
            } catch (NoSuchFieldError nosuchfielderror1020) {
                ;
            }

            try {
                aint1[Material.LEGACY_REDSTONE_LAMP_OFF.ordinal()] = 1526;
            } catch (NoSuchFieldError nosuchfielderror1021) {
                ;
            }

            try {
                aint1[Material.LEGACY_REDSTONE_LAMP_ON.ordinal()] = 1527;
            } catch (NoSuchFieldError nosuchfielderror1022) {
                ;
            }

            try {
                aint1[Material.LEGACY_REDSTONE_ORE.ordinal()] = 1476;
            } catch (NoSuchFieldError nosuchfielderror1023) {
                ;
            }

            try {
                aint1[Material.LEGACY_REDSTONE_TORCH_OFF.ordinal()] = 1478;
            } catch (NoSuchFieldError nosuchfielderror1024) {
                ;
            }

            try {
                aint1[Material.LEGACY_REDSTONE_TORCH_ON.ordinal()] = 1479;
            } catch (NoSuchFieldError nosuchfielderror1025) {
                ;
            }

            try {
                aint1[Material.LEGACY_REDSTONE_WIRE.ordinal()] = 1458;
            } catch (NoSuchFieldError nosuchfielderror1026) {
                ;
            }

            try {
                aint1[Material.LEGACY_RED_GLAZED_TERRACOTTA.ordinal()] = 1652;
            } catch (NoSuchFieldError nosuchfielderror1027) {
                ;
            }

            try {
                aint1[Material.LEGACY_RED_MUSHROOM.ordinal()] = 1443;
            } catch (NoSuchFieldError nosuchfielderror1028) {
                ;
            }

            try {
                aint1[Material.LEGACY_RED_NETHER_BRICK.ordinal()] = 1618;
            } catch (NoSuchFieldError nosuchfielderror1029) {
                ;
            }

            try {
                aint1[Material.LEGACY_RED_ROSE.ordinal()] = 1441;
            } catch (NoSuchFieldError nosuchfielderror1030) {
                ;
            }

            try {
                aint1[Material.LEGACY_RED_SANDSTONE.ordinal()] = 1582;
            } catch (NoSuchFieldError nosuchfielderror1031) {
                ;
            }

            try {
                aint1[Material.LEGACY_RED_SANDSTONE_STAIRS.ordinal()] = 1583;
            } catch (NoSuchFieldError nosuchfielderror1032) {
                ;
            }

            try {
                aint1[Material.LEGACY_RED_SHULKER_BOX.ordinal()] = 1636;
            } catch (NoSuchFieldError nosuchfielderror1033) {
                ;
            }

            try {
                aint1[Material.LEGACY_ROTTEN_FLESH.ordinal()] = 1768;
            } catch (NoSuchFieldError nosuchfielderror1034) {
                ;
            }

            try {
                aint1[Material.LEGACY_SADDLE.ordinal()] = 1730;
            } catch (NoSuchFieldError nosuchfielderror1035) {
                ;
            }

            try {
                aint1[Material.LEGACY_SAND.ordinal()] = 1415;
            } catch (NoSuchFieldError nosuchfielderror1036) {
                ;
            }

            try {
                aint1[Material.LEGACY_SANDSTONE.ordinal()] = 1427;
            } catch (NoSuchFieldError nosuchfielderror1037) {
                ;
            }

            try {
                aint1[Material.LEGACY_SANDSTONE_STAIRS.ordinal()] = 1531;
            } catch (NoSuchFieldError nosuchfielderror1038) {
                ;
            }

            try {
                aint1[Material.LEGACY_SAPLING.ordinal()] = 1409;
            } catch (NoSuchFieldError nosuchfielderror1039) {
                ;
            }

            try {
                aint1[Material.LEGACY_SEA_LANTERN.ordinal()] = 1572;
            } catch (NoSuchFieldError nosuchfielderror1040) {
                ;
            }

            try {
                aint1[Material.LEGACY_SEEDS.ordinal()] = 1696;
            } catch (NoSuchFieldError nosuchfielderror1041) {
                ;
            }

            try {
                aint1[Material.LEGACY_SHEARS.ordinal()] = 1760;
            } catch (NoSuchFieldError nosuchfielderror1042) {
                ;
            }

            try {
                aint1[Material.LEGACY_SHIELD.ordinal()] = 1843;
            } catch (NoSuchFieldError nosuchfielderror1043) {
                ;
            }

            try {
                aint1[Material.LEGACY_SHULKER_SHELL.ordinal()] = 1851;
            } catch (NoSuchFieldError nosuchfielderror1044) {
                ;
            }

            try {
                aint1[Material.LEGACY_SIGN.ordinal()] = 1724;
            } catch (NoSuchFieldError nosuchfielderror1045) {
                ;
            }

            try {
                aint1[Material.LEGACY_SIGN_POST.ordinal()] = 1466;
            } catch (NoSuchFieldError nosuchfielderror1046) {
                ;
            }

            try {
                aint1[Material.LEGACY_SILVER_GLAZED_TERRACOTTA.ordinal()] = 1646;
            } catch (NoSuchFieldError nosuchfielderror1047) {
                ;
            }

            try {
                aint1[Material.LEGACY_SILVER_SHULKER_BOX.ordinal()] = 1630;
            } catch (NoSuchFieldError nosuchfielderror1048) {
                ;
            }

            try {
                aint1[Material.LEGACY_SKULL.ordinal()] = 1547;
            } catch (NoSuchFieldError nosuchfielderror1049) {
                ;
            }

            try {
                aint1[Material.LEGACY_SKULL_ITEM.ordinal()] = 1798;
            } catch (NoSuchFieldError nosuchfielderror1050) {
                ;
            }

            try {
                aint1[Material.LEGACY_SLIME_BALL.ordinal()] = 1742;
            } catch (NoSuchFieldError nosuchfielderror1051) {
                ;
            }

            try {
                aint1[Material.LEGACY_SLIME_BLOCK.ordinal()] = 1568;
            } catch (NoSuchFieldError nosuchfielderror1052) {
                ;
            }

            try {
                aint1[Material.LEGACY_SMOOTH_BRICK.ordinal()] = 1501;
            } catch (NoSuchFieldError nosuchfielderror1053) {
                ;
            }

            try {
                aint1[Material.LEGACY_SMOOTH_STAIRS.ordinal()] = 1512;
            } catch (NoSuchFieldError nosuchfielderror1054) {
                ;
            }

            try {
                aint1[Material.LEGACY_SNOW.ordinal()] = 1481;
            } catch (NoSuchFieldError nosuchfielderror1055) {
                ;
            }

            try {
                aint1[Material.LEGACY_SNOW_BALL.ordinal()] = 1733;
            } catch (NoSuchFieldError nosuchfielderror1056) {
                ;
            }

            try {
                aint1[Material.LEGACY_SNOW_BLOCK.ordinal()] = 1483;
            } catch (NoSuchFieldError nosuchfielderror1057) {
                ;
            }

            try {
                aint1[Material.LEGACY_SOIL.ordinal()] = 1463;
            } catch (NoSuchFieldError nosuchfielderror1058) {
                ;
            }

            try {
                aint1[Material.LEGACY_SOUL_SAND.ordinal()] = 1491;
            } catch (NoSuchFieldError nosuchfielderror1059) {
                ;
            }

            try {
                aint1[Material.LEGACY_SPECKLED_MELON.ordinal()] = 1783;
            } catch (NoSuchFieldError nosuchfielderror1060) {
                ;
            }

            try {
                aint1[Material.LEGACY_SPECTRAL_ARROW.ordinal()] = 1840;
            } catch (NoSuchFieldError nosuchfielderror1061) {
                ;
            }

            try {
                aint1[Material.LEGACY_SPIDER_EYE.ordinal()] = 1776;
            } catch (NoSuchFieldError nosuchfielderror1062) {
                ;
            }

            try {
                aint1[Material.LEGACY_SPLASH_POTION.ordinal()] = 1839;
            } catch (NoSuchFieldError nosuchfielderror1063) {
                ;
            }

            try {
                aint1[Material.LEGACY_SPONGE.ordinal()] = 1422;
            } catch (NoSuchFieldError nosuchfielderror1064) {
                ;
            }

            try {
                aint1[Material.LEGACY_SPRUCE_DOOR.ordinal()] = 1596;
            } catch (NoSuchFieldError nosuchfielderror1065) {
                ;
            }

            try {
                aint1[Material.LEGACY_SPRUCE_DOOR_ITEM.ordinal()] = 1828;
            } catch (NoSuchFieldError nosuchfielderror1066) {
                ;
            }

            try {
                aint1[Material.LEGACY_SPRUCE_FENCE.ordinal()] = 1591;
            } catch (NoSuchFieldError nosuchfielderror1067) {
                ;
            }

            try {
                aint1[Material.LEGACY_SPRUCE_FENCE_GATE.ordinal()] = 1586;
            } catch (NoSuchFieldError nosuchfielderror1068) {
                ;
            }

            try {
                aint1[Material.LEGACY_SPRUCE_WOOD_STAIRS.ordinal()] = 1537;
            } catch (NoSuchFieldError nosuchfielderror1069) {
                ;
            }

            try {
                aint1[Material.LEGACY_STAINED_CLAY.ordinal()] = 1562;
            } catch (NoSuchFieldError nosuchfielderror1070) {
                ;
            }

            try {
                aint1[Material.LEGACY_STAINED_GLASS.ordinal()] = 1498;
            } catch (NoSuchFieldError nosuchfielderror1071) {
                ;
            }

            try {
                aint1[Material.LEGACY_STAINED_GLASS_PANE.ordinal()] = 1563;
            } catch (NoSuchFieldError nosuchfielderror1072) {
                ;
            }

            try {
                aint1[Material.LEGACY_STANDING_BANNER.ordinal()] = 1579;
            } catch (NoSuchFieldError nosuchfielderror1073) {
                ;
            }

            try {
                aint1[Material.LEGACY_STATIONARY_LAVA.ordinal()] = 1414;
            } catch (NoSuchFieldError nosuchfielderror1074) {
                ;
            }

            try {
                aint1[Material.LEGACY_STATIONARY_WATER.ordinal()] = 1412;
            } catch (NoSuchFieldError nosuchfielderror1075) {
                ;
            }

            try {
                aint1[Material.LEGACY_STEP.ordinal()] = 1447;
            } catch (NoSuchFieldError nosuchfielderror1076) {
                ;
            }

            try {
                aint1[Material.LEGACY_STICK.ordinal()] = 1681;
            } catch (NoSuchFieldError nosuchfielderror1077) {
                ;
            }

            try {
                aint1[Material.LEGACY_STONE.ordinal()] = 1404;
            } catch (NoSuchFieldError nosuchfielderror1078) {
                ;
            }

            try {
                aint1[Material.LEGACY_STONE_AXE.ordinal()] = 1676;
            } catch (NoSuchFieldError nosuchfielderror1079) {
                ;
            }

            try {
                aint1[Material.LEGACY_STONE_BUTTON.ordinal()] = 1480;
            } catch (NoSuchFieldError nosuchfielderror1080) {
                ;
            }

            try {
                aint1[Material.LEGACY_STONE_HOE.ordinal()] = 1692;
            } catch (NoSuchFieldError nosuchfielderror1081) {
                ;
            }

            try {
                aint1[Material.LEGACY_STONE_PICKAXE.ordinal()] = 1675;
            } catch (NoSuchFieldError nosuchfielderror1082) {
                ;
            }

            try {
                aint1[Material.LEGACY_STONE_PLATE.ordinal()] = 1473;
            } catch (NoSuchFieldError nosuchfielderror1083) {
                ;
            }

            try {
                aint1[Material.LEGACY_STONE_SLAB2.ordinal()] = 1585;
            } catch (NoSuchFieldError nosuchfielderror1084) {
                ;
            }

            try {
                aint1[Material.LEGACY_STONE_SPADE.ordinal()] = 1674;
            } catch (NoSuchFieldError nosuchfielderror1085) {
                ;
            }

            try {
                aint1[Material.LEGACY_STONE_SWORD.ordinal()] = 1673;
            } catch (NoSuchFieldError nosuchfielderror1086) {
                ;
            }

            try {
                aint1[Material.LEGACY_STORAGE_MINECART.ordinal()] = 1743;
            } catch (NoSuchFieldError nosuchfielderror1087) {
                ;
            }

            try {
                aint1[Material.LEGACY_STRING.ordinal()] = 1688;
            } catch (NoSuchFieldError nosuchfielderror1088) {
                ;
            }

            try {
                aint1[Material.LEGACY_STRUCTURE_BLOCK.ordinal()] = 1656;
            } catch (NoSuchFieldError nosuchfielderror1089) {
                ;
            }

            try {
                aint1[Material.LEGACY_STRUCTURE_VOID.ordinal()] = 1620;
            } catch (NoSuchFieldError nosuchfielderror1090) {
                ;
            }

            try {
                aint1[Material.LEGACY_SUGAR.ordinal()] = 1754;
            } catch (NoSuchFieldError nosuchfielderror1091) {
                ;
            }

            try {
                aint1[Material.LEGACY_SUGAR_CANE.ordinal()] = 1739;
            } catch (NoSuchFieldError nosuchfielderror1092) {
                ;
            }

            try {
                aint1[Material.LEGACY_SUGAR_CANE_BLOCK.ordinal()] = 1486;
            } catch (NoSuchFieldError nosuchfielderror1093) {
                ;
            }

            try {
                aint1[Material.LEGACY_SULPHUR.ordinal()] = 1690;
            } catch (NoSuchFieldError nosuchfielderror1094) {
                ;
            }

            try {
                aint1[Material.LEGACY_THIN_GLASS.ordinal()] = 1505;
            } catch (NoSuchFieldError nosuchfielderror1095) {
                ;
            }

            try {
                aint1[Material.LEGACY_TIPPED_ARROW.ordinal()] = 1841;
            } catch (NoSuchFieldError nosuchfielderror1096) {
                ;
            }

            try {
                aint1[Material.LEGACY_TNT.ordinal()] = 1449;
            } catch (NoSuchFieldError nosuchfielderror1097) {
                ;
            }

            try {
                aint1[Material.LEGACY_TORCH.ordinal()] = 1453;
            } catch (NoSuchFieldError nosuchfielderror1098) {
                ;
            }

            try {
                aint1[Material.LEGACY_TOTEM.ordinal()] = 1850;
            } catch (NoSuchFieldError nosuchfielderror1099) {
                ;
            }

            try {
                aint1[Material.LEGACY_TRAPPED_CHEST.ordinal()] = 1549;
            } catch (NoSuchFieldError nosuchfielderror1100) {
                ;
            }

            try {
                aint1[Material.LEGACY_TRAP_DOOR.ordinal()] = 1499;
            } catch (NoSuchFieldError nosuchfielderror1101) {
                ;
            }

            try {
                aint1[Material.LEGACY_TRIPWIRE.ordinal()] = 1535;
            } catch (NoSuchFieldError nosuchfielderror1102) {
                ;
            }

            try {
                aint1[Material.LEGACY_TRIPWIRE_HOOK.ordinal()] = 1534;
            } catch (NoSuchFieldError nosuchfielderror1103) {
                ;
            }

            try {
                aint1[Material.LEGACY_VINE.ordinal()] = 1509;
            } catch (NoSuchFieldError nosuchfielderror1104) {
                ;
            }

            try {
                aint1[Material.LEGACY_WALL_BANNER.ordinal()] = 1580;
            } catch (NoSuchFieldError nosuchfielderror1105) {
                ;
            }

            try {
                aint1[Material.LEGACY_WALL_SIGN.ordinal()] = 1471;
            } catch (NoSuchFieldError nosuchfielderror1106) {
                ;
            }

            try {
                aint1[Material.LEGACY_WATCH.ordinal()] = 1748;
            } catch (NoSuchFieldError nosuchfielderror1107) {
                ;
            }

            try {
                aint1[Material.LEGACY_WATER.ordinal()] = 1411;
            } catch (NoSuchFieldError nosuchfielderror1108) {
                ;
            }

            try {
                aint1[Material.LEGACY_WATER_BUCKET.ordinal()] = 1727;
            } catch (NoSuchFieldError nosuchfielderror1109) {
                ;
            }

            try {
                aint1[Material.LEGACY_WATER_LILY.ordinal()] = 1514;
            } catch (NoSuchFieldError nosuchfielderror1110) {
                ;
            }

            try {
                aint1[Material.LEGACY_WEB.ordinal()] = 1433;
            } catch (NoSuchFieldError nosuchfielderror1111) {
                ;
            }

            try {
                aint1[Material.LEGACY_WHEAT.ordinal()] = 1697;
            } catch (NoSuchFieldError nosuchfielderror1112) {
                ;
            }

            try {
                aint1[Material.LEGACY_WHITE_GLAZED_TERRACOTTA.ordinal()] = 1638;
            } catch (NoSuchFieldError nosuchfielderror1113) {
                ;
            }

            try {
                aint1[Material.LEGACY_WHITE_SHULKER_BOX.ordinal()] = 1622;
            } catch (NoSuchFieldError nosuchfielderror1114) {
                ;
            }

            try {
                aint1[Material.LEGACY_WOOD.ordinal()] = 1408;
            } catch (NoSuchFieldError nosuchfielderror1115) {
                ;
            }

            try {
                aint1[Material.LEGACY_WOODEN_DOOR.ordinal()] = 1467;
            } catch (NoSuchFieldError nosuchfielderror1116) {
                ;
            }

            try {
                aint1[Material.LEGACY_WOOD_AXE.ordinal()] = 1672;
            } catch (NoSuchFieldError nosuchfielderror1117) {
                ;
            }

            try {
                aint1[Material.LEGACY_WOOD_BUTTON.ordinal()] = 1546;
            } catch (NoSuchFieldError nosuchfielderror1118) {
                ;
            }

            try {
                aint1[Material.LEGACY_WOOD_DOOR.ordinal()] = 1725;
            } catch (NoSuchFieldError nosuchfielderror1119) {
                ;
            }

            try {
                aint1[Material.LEGACY_WOOD_DOUBLE_STEP.ordinal()] = 1528;
            } catch (NoSuchFieldError nosuchfielderror1120) {
                ;
            }

            try {
                aint1[Material.LEGACY_WOOD_HOE.ordinal()] = 1691;
            } catch (NoSuchFieldError nosuchfielderror1121) {
                ;
            }

            try {
                aint1[Material.LEGACY_WOOD_PICKAXE.ordinal()] = 1671;
            } catch (NoSuchFieldError nosuchfielderror1122) {
                ;
            }

            try {
                aint1[Material.LEGACY_WOOD_PLATE.ordinal()] = 1475;
            } catch (NoSuchFieldError nosuchfielderror1123) {
                ;
            }

            try {
                aint1[Material.LEGACY_WOOD_SPADE.ordinal()] = 1670;
            } catch (NoSuchFieldError nosuchfielderror1124) {
                ;
            }

            try {
                aint1[Material.LEGACY_WOOD_STAIRS.ordinal()] = 1456;
            } catch (NoSuchFieldError nosuchfielderror1125) {
                ;
            }

            try {
                aint1[Material.LEGACY_WOOD_STEP.ordinal()] = 1529;
            } catch (NoSuchFieldError nosuchfielderror1126) {
                ;
            }

            try {
                aint1[Material.LEGACY_WOOD_SWORD.ordinal()] = 1669;
            } catch (NoSuchFieldError nosuchfielderror1127) {
                ;
            }

            try {
                aint1[Material.LEGACY_WOOL.ordinal()] = 1438;
            } catch (NoSuchFieldError nosuchfielderror1128) {
                ;
            }

            try {
                aint1[Material.LEGACY_WORKBENCH.ordinal()] = 1461;
            } catch (NoSuchFieldError nosuchfielderror1129) {
                ;
            }

            try {
                aint1[Material.LEGACY_WRITTEN_BOOK.ordinal()] = 1788;
            } catch (NoSuchFieldError nosuchfielderror1130) {
                ;
            }

            try {
                aint1[Material.LEGACY_YELLOW_FLOWER.ordinal()] = 1440;
            } catch (NoSuchFieldError nosuchfielderror1131) {
                ;
            }

            try {
                aint1[Material.LEGACY_YELLOW_GLAZED_TERRACOTTA.ordinal()] = 1642;
            } catch (NoSuchFieldError nosuchfielderror1132) {
                ;
            }

            try {
                aint1[Material.LEGACY_YELLOW_SHULKER_BOX.ordinal()] = 1626;
            } catch (NoSuchFieldError nosuchfielderror1133) {
                ;
            }

            try {
                aint1[Material.LEVER.ordinal()] = 651;
            } catch (NoSuchFieldError nosuchfielderror1134) {
                ;
            }

            try {
                aint1[Material.LIGHT.ordinal()] = 423;
            } catch (NoSuchFieldError nosuchfielderror1135) {
                ;
            }

            try {
                aint1[Material.LIGHTNING_ROD.ordinal()] = 652;
            } catch (NoSuchFieldError nosuchfielderror1136) {
                ;
            }

            try {
                aint1[Material.LIGHT_BLUE_BANNER.ordinal()] = 1091;
            } catch (NoSuchFieldError nosuchfielderror1137) {
                ;
            }

            try {
                aint1[Material.LIGHT_BLUE_BED.ordinal()] = 928;
            } catch (NoSuchFieldError nosuchfielderror1138) {
                ;
            }

            try {
                aint1[Material.LIGHT_BLUE_CANDLE.ordinal()] = 1195;
            } catch (NoSuchFieldError nosuchfielderror1139) {
                ;
            }

            try {
                aint1[Material.LIGHT_BLUE_CANDLE_CAKE.ordinal()] = 1384;
            } catch (NoSuchFieldError nosuchfielderror1140) {
                ;
            }

            try {
                aint1[Material.LIGHT_BLUE_CARPET.ordinal()] = 428;
            } catch (NoSuchFieldError nosuchfielderror1141) {
                ;
            }

            try {
                aint1[Material.LIGHT_BLUE_CONCRETE.ordinal()] = 537;
            } catch (NoSuchFieldError nosuchfielderror1142) {
                ;
            }

            try {
                aint1[Material.LIGHT_BLUE_CONCRETE_POWDER.ordinal()] = 553;
            } catch (NoSuchFieldError nosuchfielderror1143) {
                ;
            }

            try {
                aint1[Material.LIGHT_BLUE_DYE.ordinal()] = 908;
            } catch (NoSuchFieldError nosuchfielderror1144) {
                ;
            }

            try {
                aint1[Material.LIGHT_BLUE_GLAZED_TERRACOTTA.ordinal()] = 521;
            } catch (NoSuchFieldError nosuchfielderror1145) {
                ;
            }

            try {
                aint1[Material.LIGHT_BLUE_SHULKER_BOX.ordinal()] = 505;
            } catch (NoSuchFieldError nosuchfielderror1146) {
                ;
            }

            try {
                aint1[Material.LIGHT_BLUE_STAINED_GLASS.ordinal()] = 453;
            } catch (NoSuchFieldError nosuchfielderror1147) {
                ;
            }

            try {
                aint1[Material.LIGHT_BLUE_STAINED_GLASS_PANE.ordinal()] = 469;
            } catch (NoSuchFieldError nosuchfielderror1148) {
                ;
            }

            try {
                aint1[Material.LIGHT_BLUE_TERRACOTTA.ordinal()] = 409;
            } catch (NoSuchFieldError nosuchfielderror1149) {
                ;
            }

            try {
                aint1[Material.LIGHT_BLUE_WALL_BANNER.ordinal()] = 1337;
            } catch (NoSuchFieldError nosuchfielderror1150) {
                ;
            }

            try {
                aint1[Material.LIGHT_BLUE_WOOL.ordinal()] = 184;
            } catch (NoSuchFieldError nosuchfielderror1151) {
                ;
            }

            try {
                aint1[Material.LIGHT_GRAY_BANNER.ordinal()] = 1096;
            } catch (NoSuchFieldError nosuchfielderror1152) {
                ;
            }

            try {
                aint1[Material.LIGHT_GRAY_BED.ordinal()] = 933;
            } catch (NoSuchFieldError nosuchfielderror1153) {
                ;
            }

            try {
                aint1[Material.LIGHT_GRAY_CANDLE.ordinal()] = 1200;
            } catch (NoSuchFieldError nosuchfielderror1154) {
                ;
            }

            try {
                aint1[Material.LIGHT_GRAY_CANDLE_CAKE.ordinal()] = 1389;
            } catch (NoSuchFieldError nosuchfielderror1155) {
                ;
            }

            try {
                aint1[Material.LIGHT_GRAY_CARPET.ordinal()] = 433;
            } catch (NoSuchFieldError nosuchfielderror1156) {
                ;
            }

            try {
                aint1[Material.LIGHT_GRAY_CONCRETE.ordinal()] = 542;
            } catch (NoSuchFieldError nosuchfielderror1157) {
                ;
            }

            try {
                aint1[Material.LIGHT_GRAY_CONCRETE_POWDER.ordinal()] = 558;
            } catch (NoSuchFieldError nosuchfielderror1158) {
                ;
            }

            try {
                aint1[Material.LIGHT_GRAY_DYE.ordinal()] = 913;
            } catch (NoSuchFieldError nosuchfielderror1159) {
                ;
            }

            try {
                aint1[Material.LIGHT_GRAY_GLAZED_TERRACOTTA.ordinal()] = 526;
            } catch (NoSuchFieldError nosuchfielderror1160) {
                ;
            }

            try {
                aint1[Material.LIGHT_GRAY_SHULKER_BOX.ordinal()] = 510;
            } catch (NoSuchFieldError nosuchfielderror1161) {
                ;
            }

            try {
                aint1[Material.LIGHT_GRAY_STAINED_GLASS.ordinal()] = 458;
            } catch (NoSuchFieldError nosuchfielderror1162) {
                ;
            }

            try {
                aint1[Material.LIGHT_GRAY_STAINED_GLASS_PANE.ordinal()] = 474;
            } catch (NoSuchFieldError nosuchfielderror1163) {
                ;
            }

            try {
                aint1[Material.LIGHT_GRAY_TERRACOTTA.ordinal()] = 414;
            } catch (NoSuchFieldError nosuchfielderror1164) {
                ;
            }

            try {
                aint1[Material.LIGHT_GRAY_WALL_BANNER.ordinal()] = 1342;
            } catch (NoSuchFieldError nosuchfielderror1165) {
                ;
            }

            try {
                aint1[Material.LIGHT_GRAY_WOOL.ordinal()] = 189;
            } catch (NoSuchFieldError nosuchfielderror1166) {
                ;
            }

            try {
                aint1[Material.LIGHT_WEIGHTED_PRESSURE_PLATE.ordinal()] = 676;
            } catch (NoSuchFieldError nosuchfielderror1167) {
                ;
            }

            try {
                aint1[Material.LILAC.ordinal()] = 445;
            } catch (NoSuchFieldError nosuchfielderror1168) {
                ;
            }

            try {
                aint1[Material.LILY_OF_THE_VALLEY.ordinal()] = 208;
            } catch (NoSuchFieldError nosuchfielderror1169) {
                ;
            }

            try {
                aint1[Material.LILY_PAD.ordinal()] = 344;
            } catch (NoSuchFieldError nosuchfielderror1170) {
                ;
            }

            try {
                aint1[Material.LIME_BANNER.ordinal()] = 1093;
            } catch (NoSuchFieldError nosuchfielderror1171) {
                ;
            }

            try {
                aint1[Material.LIME_BED.ordinal()] = 930;
            } catch (NoSuchFieldError nosuchfielderror1172) {
                ;
            }

            try {
                aint1[Material.LIME_CANDLE.ordinal()] = 1197;
            } catch (NoSuchFieldError nosuchfielderror1173) {
                ;
            }

            try {
                aint1[Material.LIME_CANDLE_CAKE.ordinal()] = 1386;
            } catch (NoSuchFieldError nosuchfielderror1174) {
                ;
            }

            try {
                aint1[Material.LIME_CARPET.ordinal()] = 430;
            } catch (NoSuchFieldError nosuchfielderror1175) {
                ;
            }

            try {
                aint1[Material.LIME_CONCRETE.ordinal()] = 539;
            } catch (NoSuchFieldError nosuchfielderror1176) {
                ;
            }

            try {
                aint1[Material.LIME_CONCRETE_POWDER.ordinal()] = 555;
            } catch (NoSuchFieldError nosuchfielderror1177) {
                ;
            }

            try {
                aint1[Material.LIME_DYE.ordinal()] = 910;
            } catch (NoSuchFieldError nosuchfielderror1178) {
                ;
            }

            try {
                aint1[Material.LIME_GLAZED_TERRACOTTA.ordinal()] = 523;
            } catch (NoSuchFieldError nosuchfielderror1179) {
                ;
            }

            try {
                aint1[Material.LIME_SHULKER_BOX.ordinal()] = 507;
            } catch (NoSuchFieldError nosuchfielderror1180) {
                ;
            }

            try {
                aint1[Material.LIME_STAINED_GLASS.ordinal()] = 455;
            } catch (NoSuchFieldError nosuchfielderror1181) {
                ;
            }

            try {
                aint1[Material.LIME_STAINED_GLASS_PANE.ordinal()] = 471;
            } catch (NoSuchFieldError nosuchfielderror1182) {
                ;
            }

            try {
                aint1[Material.LIME_TERRACOTTA.ordinal()] = 411;
            } catch (NoSuchFieldError nosuchfielderror1183) {
                ;
            }

            try {
                aint1[Material.LIME_WALL_BANNER.ordinal()] = 1339;
            } catch (NoSuchFieldError nosuchfielderror1184) {
                ;
            }

            try {
                aint1[Material.LIME_WOOL.ordinal()] = 186;
            } catch (NoSuchFieldError nosuchfielderror1185) {
                ;
            }

            try {
                aint1[Material.LINGERING_POTION.ordinal()] = 1116;
            } catch (NoSuchFieldError nosuchfielderror1186) {
                ;
            }

            try {
                aint1[Material.LLAMA_SPAWN_EGG.ordinal()] = 998;
            } catch (NoSuchFieldError nosuchfielderror1187) {
                ;
            }

            try {
                aint1[Material.LODESTONE.ordinal()] = 1176;
            } catch (NoSuchFieldError nosuchfielderror1188) {
                ;
            }

            try {
                aint1[Material.LOOM.ordinal()] = 1146;
            } catch (NoSuchFieldError nosuchfielderror1189) {
                ;
            }

            try {
                aint1[Material.MAGENTA_BANNER.ordinal()] = 1090;
            } catch (NoSuchFieldError nosuchfielderror1190) {
                ;
            }

            try {
                aint1[Material.MAGENTA_BED.ordinal()] = 927;
            } catch (NoSuchFieldError nosuchfielderror1191) {
                ;
            }

            try {
                aint1[Material.MAGENTA_CANDLE.ordinal()] = 1194;
            } catch (NoSuchFieldError nosuchfielderror1192) {
                ;
            }

            try {
                aint1[Material.MAGENTA_CANDLE_CAKE.ordinal()] = 1383;
            } catch (NoSuchFieldError nosuchfielderror1193) {
                ;
            }

            try {
                aint1[Material.MAGENTA_CARPET.ordinal()] = 427;
            } catch (NoSuchFieldError nosuchfielderror1194) {
                ;
            }

            try {
                aint1[Material.MAGENTA_CONCRETE.ordinal()] = 536;
            } catch (NoSuchFieldError nosuchfielderror1195) {
                ;
            }

            try {
                aint1[Material.MAGENTA_CONCRETE_POWDER.ordinal()] = 552;
            } catch (NoSuchFieldError nosuchfielderror1196) {
                ;
            }

            try {
                aint1[Material.MAGENTA_DYE.ordinal()] = 907;
            } catch (NoSuchFieldError nosuchfielderror1197) {
                ;
            }

            try {
                aint1[Material.MAGENTA_GLAZED_TERRACOTTA.ordinal()] = 520;
            } catch (NoSuchFieldError nosuchfielderror1198) {
                ;
            }

            try {
                aint1[Material.MAGENTA_SHULKER_BOX.ordinal()] = 504;
            } catch (NoSuchFieldError nosuchfielderror1199) {
                ;
            }

            try {
                aint1[Material.MAGENTA_STAINED_GLASS.ordinal()] = 452;
            } catch (NoSuchFieldError nosuchfielderror1200) {
                ;
            }

            try {
                aint1[Material.MAGENTA_STAINED_GLASS_PANE.ordinal()] = 468;
            } catch (NoSuchFieldError nosuchfielderror1201) {
                ;
            }

            try {
                aint1[Material.MAGENTA_TERRACOTTA.ordinal()] = 408;
            } catch (NoSuchFieldError nosuchfielderror1202) {
                ;
            }

            try {
                aint1[Material.MAGENTA_WALL_BANNER.ordinal()] = 1336;
            } catch (NoSuchFieldError nosuchfielderror1203) {
                ;
            }

            try {
                aint1[Material.MAGENTA_WOOL.ordinal()] = 183;
            } catch (NoSuchFieldError nosuchfielderror1204) {
                ;
            }

            try {
                aint1[Material.MAGMA_BLOCK.ordinal()] = 495;
            } catch (NoSuchFieldError nosuchfielderror1205) {
                ;
            }

            try {
                aint1[Material.MAGMA_CREAM.ordinal()] = 963;
            } catch (NoSuchFieldError nosuchfielderror1206) {
                ;
            }

            try {
                aint1[Material.MAGMA_CUBE_SPAWN_EGG.ordinal()] = 999;
            } catch (NoSuchFieldError nosuchfielderror1207) {
                ;
            }

            try {
                aint1[Material.MANGROVE_BOAT.ordinal()] = 751;
            } catch (NoSuchFieldError nosuchfielderror1208) {
                ;
            }

            try {
                aint1[Material.MANGROVE_BUTTON.ordinal()] = 670;
            } catch (NoSuchFieldError nosuchfielderror1209) {
                ;
            }

            try {
                aint1[Material.MANGROVE_CHEST_BOAT.ordinal()] = 752;
            } catch (NoSuchFieldError nosuchfielderror1210) {
                ;
            }

            try {
                aint1[Material.MANGROVE_DOOR.ordinal()] = 697;
            } catch (NoSuchFieldError nosuchfielderror1211) {
                ;
            }

            try {
                aint1[Material.MANGROVE_FENCE.ordinal()] = 297;
            } catch (NoSuchFieldError nosuchfielderror1212) {
                ;
            }

            try {
                aint1[Material.MANGROVE_FENCE_GATE.ordinal()] = 720;
            } catch (NoSuchFieldError nosuchfielderror1213) {
                ;
            }

            try {
                aint1[Material.MANGROVE_HANGING_SIGN.ordinal()] = 865;
            } catch (NoSuchFieldError nosuchfielderror1214) {
                ;
            }

            try {
                aint1[Material.MANGROVE_LEAVES.ordinal()] = 162;
            } catch (NoSuchFieldError nosuchfielderror1215) {
                ;
            }

            try {
                aint1[Material.MANGROVE_LOG.ordinal()] = 118;
            } catch (NoSuchFieldError nosuchfielderror1216) {
                ;
            }

            try {
                aint1[Material.MANGROVE_PLANKS.ordinal()] = 31;
            } catch (NoSuchFieldError nosuchfielderror1217) {
                ;
            }

            try {
                aint1[Material.MANGROVE_PRESSURE_PLATE.ordinal()] = 685;
            } catch (NoSuchFieldError nosuchfielderror1218) {
                ;
            }

            try {
                aint1[Material.MANGROVE_PROPAGULE.ordinal()] = 43;
            } catch (NoSuchFieldError nosuchfielderror1219) {
                ;
            }

            try {
                aint1[Material.MANGROVE_ROOTS.ordinal()] = 119;
            } catch (NoSuchFieldError nosuchfielderror1220) {
                ;
            }

            try {
                aint1[Material.MANGROVE_SIGN.ordinal()] = 854;
            } catch (NoSuchFieldError nosuchfielderror1221) {
                ;
            }

            try {
                aint1[Material.MANGROVE_SLAB.ordinal()] = 238;
            } catch (NoSuchFieldError nosuchfielderror1222) {
                ;
            }

            try {
                aint1[Material.MANGROVE_STAIRS.ordinal()] = 369;
            } catch (NoSuchFieldError nosuchfielderror1223) {
                ;
            }

            try {
                aint1[Material.MANGROVE_TRAPDOOR.ordinal()] = 709;
            } catch (NoSuchFieldError nosuchfielderror1224) {
                ;
            }

            try {
                aint1[Material.MANGROVE_WALL_HANGING_SIGN.ordinal()] = 1281;
            } catch (NoSuchFieldError nosuchfielderror1225) {
                ;
            }

            try {
                aint1[Material.MANGROVE_WALL_SIGN.ordinal()] = 1272;
            } catch (NoSuchFieldError nosuchfielderror1226) {
                ;
            }

            try {
                aint1[Material.MANGROVE_WOOD.ordinal()] = 152;
            } catch (NoSuchFieldError nosuchfielderror1227) {
                ;
            }

            try {
                aint1[Material.MAP.ordinal()] = 1056;
            } catch (NoSuchFieldError nosuchfielderror1228) {
                ;
            }

            try {
                aint1[Material.MEDIUM_AMETHYST_BUD.ordinal()] = 1209;
            } catch (NoSuchFieldError nosuchfielderror1229) {
                ;
            }

            try {
                aint1[Material.MELON.ordinal()] = 337;
            } catch (NoSuchFieldError nosuchfielderror1230) {
                ;
            }

            try {
                aint1[Material.MELON_SEEDS.ordinal()] = 947;
            } catch (NoSuchFieldError nosuchfielderror1231) {
                ;
            }

            try {
                aint1[Material.MELON_SLICE.ordinal()] = 944;
            } catch (NoSuchFieldError nosuchfielderror1232) {
                ;
            }

            try {
                aint1[Material.MELON_STEM.ordinal()] = 1291;
            } catch (NoSuchFieldError nosuchfielderror1233) {
                ;
            }

            try {
                aint1[Material.MILK_BUCKET.ordinal()] = 875;
            } catch (NoSuchFieldError nosuchfielderror1234) {
                ;
            }

            try {
                aint1[Material.MINECART.ordinal()] = 729;
            } catch (NoSuchFieldError nosuchfielderror1235) {
                ;
            }

            try {
                aint1[Material.MINER_POTTERY_SHERD.ordinal()] = 1248;
            } catch (NoSuchFieldError nosuchfielderror1236) {
                ;
            }

            try {
                aint1[Material.MOJANG_BANNER_PATTERN.ordinal()] = 1150;
            } catch (NoSuchFieldError nosuchfielderror1237) {
                ;
            }

            try {
                aint1[Material.MOOSHROOM_SPAWN_EGG.ordinal()] = 1000;
            } catch (NoSuchFieldError nosuchfielderror1238) {
                ;
            }

            try {
                aint1[Material.MOSSY_COBBLESTONE.ordinal()] = 268;
            } catch (NoSuchFieldError nosuchfielderror1239) {
                ;
            }

            try {
                aint1[Material.MOSSY_COBBLESTONE_SLAB.ordinal()] = 622;
            } catch (NoSuchFieldError nosuchfielderror1240) {
                ;
            }

            try {
                aint1[Material.MOSSY_COBBLESTONE_STAIRS.ordinal()] = 604;
            } catch (NoSuchFieldError nosuchfielderror1241) {
                ;
            }

            try {
                aint1[Material.MOSSY_COBBLESTONE_WALL.ordinal()] = 377;
            } catch (NoSuchFieldError nosuchfielderror1242) {
                ;
            }

            try {
                aint1[Material.MOSSY_STONE_BRICKS.ordinal()] = 320;
            } catch (NoSuchFieldError nosuchfielderror1243) {
                ;
            }

            try {
                aint1[Material.MOSSY_STONE_BRICK_SLAB.ordinal()] = 620;
            } catch (NoSuchFieldError nosuchfielderror1244) {
                ;
            }

            try {
                aint1[Material.MOSSY_STONE_BRICK_STAIRS.ordinal()] = 602;
            } catch (NoSuchFieldError nosuchfielderror1245) {
                ;
            }

            try {
                aint1[Material.MOSSY_STONE_BRICK_WALL.ordinal()] = 381;
            } catch (NoSuchFieldError nosuchfielderror1246) {
                ;
            }

            try {
                aint1[Material.MOSS_BLOCK.ordinal()] = 226;
            } catch (NoSuchFieldError nosuchfielderror1247) {
                ;
            }

            try {
                aint1[Material.MOSS_CARPET.ordinal()] = 224;
            } catch (NoSuchFieldError nosuchfielderror1248) {
                ;
            }

            try {
                aint1[Material.MOURNER_POTTERY_SHERD.ordinal()] = 1249;
            } catch (NoSuchFieldError nosuchfielderror1249) {
                ;
            }

            try {
                aint1[Material.MOVING_PISTON.ordinal()] = 1260;
            } catch (NoSuchFieldError nosuchfielderror1250) {
                ;
            }

            try {
                aint1[Material.MUD.ordinal()] = 20;
            } catch (NoSuchFieldError nosuchfielderror1251) {
                ;
            }

            try {
                aint1[Material.MUDDY_MANGROVE_ROOTS.ordinal()] = 120;
            } catch (NoSuchFieldError nosuchfielderror1252) {
                ;
            }

            try {
                aint1[Material.MUD_BRICKS.ordinal()] = 324;
            } catch (NoSuchFieldError nosuchfielderror1253) {
                ;
            }

            try {
                aint1[Material.MUD_BRICK_SLAB.ordinal()] = 251;
            } catch (NoSuchFieldError nosuchfielderror1254) {
                ;
            }

            try {
                aint1[Material.MUD_BRICK_STAIRS.ordinal()] = 342;
            } catch (NoSuchFieldError nosuchfielderror1255) {
                ;
            }

            try {
                aint1[Material.MUD_BRICK_WALL.ordinal()] = 384;
            } catch (NoSuchFieldError nosuchfielderror1256) {
                ;
            }

            try {
                aint1[Material.MULE_SPAWN_EGG.ordinal()] = 1001;
            } catch (NoSuchFieldError nosuchfielderror1257) {
                ;
            }

            try {
                aint1[Material.MUSHROOM_STEM.ordinal()] = 333;
            } catch (NoSuchFieldError nosuchfielderror1258) {
                ;
            }

            try {
                aint1[Material.MUSHROOM_STEW.ordinal()] = 810;
            } catch (NoSuchFieldError nosuchfielderror1259) {
                ;
            }

            try {
                aint1[Material.MUSIC_DISC_11.ordinal()] = 1133;
            } catch (NoSuchFieldError nosuchfielderror1260) {
                ;
            }

            try {
                aint1[Material.MUSIC_DISC_13.ordinal()] = 1123;
            } catch (NoSuchFieldError nosuchfielderror1261) {
                ;
            }

            try {
                aint1[Material.MUSIC_DISC_5.ordinal()] = 1137;
            } catch (NoSuchFieldError nosuchfielderror1262) {
                ;
            }

            try {
                aint1[Material.MUSIC_DISC_BLOCKS.ordinal()] = 1125;
            } catch (NoSuchFieldError nosuchfielderror1263) {
                ;
            }

            try {
                aint1[Material.MUSIC_DISC_CAT.ordinal()] = 1124;
            } catch (NoSuchFieldError nosuchfielderror1264) {
                ;
            }

            try {
                aint1[Material.MUSIC_DISC_CHIRP.ordinal()] = 1126;
            } catch (NoSuchFieldError nosuchfielderror1265) {
                ;
            }

            try {
                aint1[Material.MUSIC_DISC_FAR.ordinal()] = 1127;
            } catch (NoSuchFieldError nosuchfielderror1266) {
                ;
            }

            try {
                aint1[Material.MUSIC_DISC_MALL.ordinal()] = 1128;
            } catch (NoSuchFieldError nosuchfielderror1267) {
                ;
            }

            try {
                aint1[Material.MUSIC_DISC_MELLOHI.ordinal()] = 1129;
            } catch (NoSuchFieldError nosuchfielderror1268) {
                ;
            }

            try {
                aint1[Material.MUSIC_DISC_OTHERSIDE.ordinal()] = 1135;
            } catch (NoSuchFieldError nosuchfielderror1269) {
                ;
            }

            try {
                aint1[Material.MUSIC_DISC_PIGSTEP.ordinal()] = 1138;
            } catch (NoSuchFieldError nosuchfielderror1270) {
                ;
            }

            try {
                aint1[Material.MUSIC_DISC_RELIC.ordinal()] = 1136;
            } catch (NoSuchFieldError nosuchfielderror1271) {
                ;
            }

            try {
                aint1[Material.MUSIC_DISC_STAL.ordinal()] = 1130;
            } catch (NoSuchFieldError nosuchfielderror1272) {
                ;
            }

            try {
                aint1[Material.MUSIC_DISC_STRAD.ordinal()] = 1131;
            } catch (NoSuchFieldError nosuchfielderror1273) {
                ;
            }

            try {
                aint1[Material.MUSIC_DISC_WAIT.ordinal()] = 1134;
            } catch (NoSuchFieldError nosuchfielderror1274) {
                ;
            }

            try {
                aint1[Material.MUSIC_DISC_WARD.ordinal()] = 1132;
            } catch (NoSuchFieldError nosuchfielderror1275) {
                ;
            }

            try {
                aint1[Material.MUTTON.ordinal()] = 1086;
            } catch (NoSuchFieldError nosuchfielderror1276) {
                ;
            }

            try {
                aint1[Material.MYCELIUM.ordinal()] = 343;
            } catch (NoSuchFieldError nosuchfielderror1277) {
                ;
            }

            try {
                aint1[Material.NAME_TAG.ordinal()] = 1084;
            } catch (NoSuchFieldError nosuchfielderror1278) {
                ;
            }

            try {
                aint1[Material.NAUTILUS_SHELL.ordinal()] = 1142;
            } catch (NoSuchFieldError nosuchfielderror1279) {
                ;
            }

            try {
                aint1[Material.NETHERITE_AXE.ordinal()] = 806;
            } catch (NoSuchFieldError nosuchfielderror1280) {
                ;
            }

            try {
                aint1[Material.NETHERITE_BLOCK.ordinal()] = 79;
            } catch (NoSuchFieldError nosuchfielderror1281) {
                ;
            }

            try {
                aint1[Material.NETHERITE_BOOTS.ordinal()] = 840;
            } catch (NoSuchFieldError nosuchfielderror1282) {
                ;
            }

            try {
                aint1[Material.NETHERITE_CHESTPLATE.ordinal()] = 838;
            } catch (NoSuchFieldError nosuchfielderror1283) {
                ;
            }

            try {
                aint1[Material.NETHERITE_HELMET.ordinal()] = 837;
            } catch (NoSuchFieldError nosuchfielderror1284) {
                ;
            }

            try {
                aint1[Material.NETHERITE_HOE.ordinal()] = 807;
            } catch (NoSuchFieldError nosuchfielderror1285) {
                ;
            }

            try {
                aint1[Material.NETHERITE_INGOT.ordinal()] = 776;
            } catch (NoSuchFieldError nosuchfielderror1286) {
                ;
            }

            try {
                aint1[Material.NETHERITE_LEGGINGS.ordinal()] = 839;
            } catch (NoSuchFieldError nosuchfielderror1287) {
                ;
            }

            try {
                aint1[Material.NETHERITE_PICKAXE.ordinal()] = 805;
            } catch (NoSuchFieldError nosuchfielderror1288) {
                ;
            }

            try {
                aint1[Material.NETHERITE_SCRAP.ordinal()] = 777;
            } catch (NoSuchFieldError nosuchfielderror1289) {
                ;
            }

            try {
                aint1[Material.NETHERITE_SHOVEL.ordinal()] = 804;
            } catch (NoSuchFieldError nosuchfielderror1290) {
                ;
            }

            try {
                aint1[Material.NETHERITE_SWORD.ordinal()] = 803;
            } catch (NoSuchFieldError nosuchfielderror1291) {
                ;
            }

            try {
                aint1[Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE.ordinal()] = 1219;
            } catch (NoSuchFieldError nosuchfielderror1292) {
                ;
            }

            try {
                aint1[Material.NETHERRACK.ordinal()] = 304;
            } catch (NoSuchFieldError nosuchfielderror1293) {
                ;
            }

            try {
                aint1[Material.NETHER_BRICK.ordinal()] = 1070;
            } catch (NoSuchFieldError nosuchfielderror1294) {
                ;
            }

            try {
                aint1[Material.NETHER_BRICKS.ordinal()] = 345;
            } catch (NoSuchFieldError nosuchfielderror1295) {
                ;
            }

            try {
                aint1[Material.NETHER_BRICK_FENCE.ordinal()] = 348;
            } catch (NoSuchFieldError nosuchfielderror1296) {
                ;
            }

            try {
                aint1[Material.NETHER_BRICK_SLAB.ordinal()] = 252;
            } catch (NoSuchFieldError nosuchfielderror1297) {
                ;
            }

            try {
                aint1[Material.NETHER_BRICK_STAIRS.ordinal()] = 349;
            } catch (NoSuchFieldError nosuchfielderror1298) {
                ;
            }

            try {
                aint1[Material.NETHER_BRICK_WALL.ordinal()] = 385;
            } catch (NoSuchFieldError nosuchfielderror1299) {
                ;
            }

            try {
                aint1[Material.NETHER_GOLD_ORE.ordinal()] = 66;
            } catch (NoSuchFieldError nosuchfielderror1300) {
                ;
            }

            try {
                aint1[Material.NETHER_PORTAL.ordinal()] = 1287;
            } catch (NoSuchFieldError nosuchfielderror1301) {
                ;
            }

            try {
                aint1[Material.NETHER_QUARTZ_ORE.ordinal()] = 67;
            } catch (NoSuchFieldError nosuchfielderror1302) {
                ;
            }

            try {
                aint1[Material.NETHER_SPROUTS.ordinal()] = 219;
            } catch (NoSuchFieldError nosuchfielderror1303) {
                ;
            }

            try {
                aint1[Material.NETHER_STAR.ordinal()] = 1065;
            } catch (NoSuchFieldError nosuchfielderror1304) {
                ;
            }

            try {
                aint1[Material.NETHER_WART.ordinal()] = 957;
            } catch (NoSuchFieldError nosuchfielderror1305) {
                ;
            }

            try {
                aint1[Material.NETHER_WART_BLOCK.ordinal()] = 496;
            } catch (NoSuchFieldError nosuchfielderror1306) {
                ;
            }

            try {
                aint1[Material.NOTE_BLOCK.ordinal()] = 660;
            } catch (NoSuchFieldError nosuchfielderror1307) {
                ;
            }

            try {
                aint1[Material.OAK_BOAT.ordinal()] = 737;
            } catch (NoSuchFieldError nosuchfielderror1308) {
                ;
            }

            try {
                aint1[Material.OAK_BUTTON.ordinal()] = 663;
            } catch (NoSuchFieldError nosuchfielderror1309) {
                ;
            }

            try {
                aint1[Material.OAK_CHEST_BOAT.ordinal()] = 738;
            } catch (NoSuchFieldError nosuchfielderror1310) {
                ;
            }

            try {
                aint1[Material.OAK_DOOR.ordinal()] = 690;
            } catch (NoSuchFieldError nosuchfielderror1311) {
                ;
            }

            try {
                aint1[Material.OAK_FENCE.ordinal()] = 290;
            } catch (NoSuchFieldError nosuchfielderror1312) {
                ;
            }

            try {
                aint1[Material.OAK_FENCE_GATE.ordinal()] = 713;
            } catch (NoSuchFieldError nosuchfielderror1313) {
                ;
            }

            try {
                aint1[Material.OAK_HANGING_SIGN.ordinal()] = 858;
            } catch (NoSuchFieldError nosuchfielderror1314) {
                ;
            }

            try {
                aint1[Material.OAK_LEAVES.ordinal()] = 155;
            } catch (NoSuchFieldError nosuchfielderror1315) {
                ;
            }

            try {
                aint1[Material.OAK_LOG.ordinal()] = 111;
            } catch (NoSuchFieldError nosuchfielderror1316) {
                ;
            }

            try {
                aint1[Material.OAK_PLANKS.ordinal()] = 24;
            } catch (NoSuchFieldError nosuchfielderror1317) {
                ;
            }

            try {
                aint1[Material.OAK_PRESSURE_PLATE.ordinal()] = 678;
            } catch (NoSuchFieldError nosuchfielderror1318) {
                ;
            }

            try {
                aint1[Material.OAK_SAPLING.ordinal()] = 36;
            } catch (NoSuchFieldError nosuchfielderror1319) {
                ;
            }

            try {
                aint1[Material.OAK_SIGN.ordinal()] = 847;
            } catch (NoSuchFieldError nosuchfielderror1320) {
                ;
            }

            try {
                aint1[Material.OAK_SLAB.ordinal()] = 231;
            } catch (NoSuchFieldError nosuchfielderror1321) {
                ;
            }

            try {
                aint1[Material.OAK_STAIRS.ordinal()] = 362;
            } catch (NoSuchFieldError nosuchfielderror1322) {
                ;
            }

            try {
                aint1[Material.OAK_TRAPDOOR.ordinal()] = 702;
            } catch (NoSuchFieldError nosuchfielderror1323) {
                ;
            }

            try {
                aint1[Material.OAK_WALL_HANGING_SIGN.ordinal()] = 1274;
            } catch (NoSuchFieldError nosuchfielderror1324) {
                ;
            }

            try {
                aint1[Material.OAK_WALL_SIGN.ordinal()] = 1265;
            } catch (NoSuchFieldError nosuchfielderror1325) {
                ;
            }

            try {
                aint1[Material.OAK_WOOD.ordinal()] = 145;
            } catch (NoSuchFieldError nosuchfielderror1326) {
                ;
            }

            try {
                aint1[Material.OBSERVER.ordinal()] = 645;
            } catch (NoSuchFieldError nosuchfielderror1327) {
                ;
            }

            try {
                aint1[Material.OBSIDIAN.ordinal()] = 269;
            } catch (NoSuchFieldError nosuchfielderror1328) {
                ;
            }

            try {
                aint1[Material.OCELOT_SPAWN_EGG.ordinal()] = 1002;
            } catch (NoSuchFieldError nosuchfielderror1329) {
                ;
            }

            try {
                aint1[Material.OCHRE_FROGLIGHT.ordinal()] = 1213;
            } catch (NoSuchFieldError nosuchfielderror1330) {
                ;
            }

            try {
                aint1[Material.ORANGE_BANNER.ordinal()] = 1089;
            } catch (NoSuchFieldError nosuchfielderror1331) {
                ;
            }

            try {
                aint1[Material.ORANGE_BED.ordinal()] = 926;
            } catch (NoSuchFieldError nosuchfielderror1332) {
                ;
            }

            try {
                aint1[Material.ORANGE_CANDLE.ordinal()] = 1193;
            } catch (NoSuchFieldError nosuchfielderror1333) {
                ;
            }

            try {
                aint1[Material.ORANGE_CANDLE_CAKE.ordinal()] = 1382;
            } catch (NoSuchFieldError nosuchfielderror1334) {
                ;
            }

            try {
                aint1[Material.ORANGE_CARPET.ordinal()] = 426;
            } catch (NoSuchFieldError nosuchfielderror1335) {
                ;
            }

            try {
                aint1[Material.ORANGE_CONCRETE.ordinal()] = 535;
            } catch (NoSuchFieldError nosuchfielderror1336) {
                ;
            }

            try {
                aint1[Material.ORANGE_CONCRETE_POWDER.ordinal()] = 551;
            } catch (NoSuchFieldError nosuchfielderror1337) {
                ;
            }

            try {
                aint1[Material.ORANGE_DYE.ordinal()] = 906;
            } catch (NoSuchFieldError nosuchfielderror1338) {
                ;
            }

            try {
                aint1[Material.ORANGE_GLAZED_TERRACOTTA.ordinal()] = 519;
            } catch (NoSuchFieldError nosuchfielderror1339) {
                ;
            }

            try {
                aint1[Material.ORANGE_SHULKER_BOX.ordinal()] = 503;
            } catch (NoSuchFieldError nosuchfielderror1340) {
                ;
            }

            try {
                aint1[Material.ORANGE_STAINED_GLASS.ordinal()] = 451;
            } catch (NoSuchFieldError nosuchfielderror1341) {
                ;
            }

            try {
                aint1[Material.ORANGE_STAINED_GLASS_PANE.ordinal()] = 467;
            } catch (NoSuchFieldError nosuchfielderror1342) {
                ;
            }

            try {
                aint1[Material.ORANGE_TERRACOTTA.ordinal()] = 407;
            } catch (NoSuchFieldError nosuchfielderror1343) {
                ;
            }

            try {
                aint1[Material.ORANGE_TULIP.ordinal()] = 203;
            } catch (NoSuchFieldError nosuchfielderror1344) {
                ;
            }

            try {
                aint1[Material.ORANGE_WALL_BANNER.ordinal()] = 1335;
            } catch (NoSuchFieldError nosuchfielderror1345) {
                ;
            }

            try {
                aint1[Material.ORANGE_WOOL.ordinal()] = 182;
            } catch (NoSuchFieldError nosuchfielderror1346) {
                ;
            }

            try {
                aint1[Material.OXEYE_DAISY.ordinal()] = 206;
            } catch (NoSuchFieldError nosuchfielderror1347) {
                ;
            }

            try {
                aint1[Material.OXIDIZED_COPPER.ordinal()] = 82;
            } catch (NoSuchFieldError nosuchfielderror1348) {
                ;
            }

            try {
                aint1[Material.OXIDIZED_CUT_COPPER.ordinal()] = 86;
            } catch (NoSuchFieldError nosuchfielderror1349) {
                ;
            }

            try {
                aint1[Material.OXIDIZED_CUT_COPPER_SLAB.ordinal()] = 94;
            } catch (NoSuchFieldError nosuchfielderror1350) {
                ;
            }

            try {
                aint1[Material.OXIDIZED_CUT_COPPER_STAIRS.ordinal()] = 90;
            } catch (NoSuchFieldError nosuchfielderror1351) {
                ;
            }

            try {
                aint1[Material.PACKED_ICE.ordinal()] = 442;
            } catch (NoSuchFieldError nosuchfielderror1352) {
                ;
            }

            try {
                aint1[Material.PACKED_MUD.ordinal()] = 323;
            } catch (NoSuchFieldError nosuchfielderror1353) {
                ;
            }

            try {
                aint1[Material.PAINTING.ordinal()] = 844;
            } catch (NoSuchFieldError nosuchfielderror1354) {
                ;
            }

            try {
                aint1[Material.PANDA_SPAWN_EGG.ordinal()] = 1003;
            } catch (NoSuchFieldError nosuchfielderror1355) {
                ;
            }

            try {
                aint1[Material.PAPER.ordinal()] = 885;
            } catch (NoSuchFieldError nosuchfielderror1356) {
                ;
            }

            try {
                aint1[Material.PARROT_SPAWN_EGG.ordinal()] = 1004;
            } catch (NoSuchFieldError nosuchfielderror1357) {
                ;
            }

            try {
                aint1[Material.PEARLESCENT_FROGLIGHT.ordinal()] = 1215;
            } catch (NoSuchFieldError nosuchfielderror1358) {
                ;
            }

            try {
                aint1[Material.PEONY.ordinal()] = 447;
            } catch (NoSuchFieldError nosuchfielderror1359) {
                ;
            }

            try {
                aint1[Material.PETRIFIED_OAK_SLAB.ordinal()] = 247;
            } catch (NoSuchFieldError nosuchfielderror1360) {
                ;
            }

            try {
                aint1[Material.PHANTOM_MEMBRANE.ordinal()] = 1141;
            } catch (NoSuchFieldError nosuchfielderror1361) {
                ;
            }

            try {
                aint1[Material.PHANTOM_SPAWN_EGG.ordinal()] = 1005;
            } catch (NoSuchFieldError nosuchfielderror1362) {
                ;
            }

            try {
                aint1[Material.PIGLIN_BANNER_PATTERN.ordinal()] = 1152;
            } catch (NoSuchFieldError nosuchfielderror1363) {
                ;
            }

            try {
                aint1[Material.PIGLIN_BRUTE_SPAWN_EGG.ordinal()] = 1008;
            } catch (NoSuchFieldError nosuchfielderror1364) {
                ;
            }

            try {
                aint1[Material.PIGLIN_HEAD.ordinal()] = 1064;
            } catch (NoSuchFieldError nosuchfielderror1365) {
                ;
            }

            try {
                aint1[Material.PIGLIN_SPAWN_EGG.ordinal()] = 1007;
            } catch (NoSuchFieldError nosuchfielderror1366) {
                ;
            }

            try {
                aint1[Material.PIGLIN_WALL_HEAD.ordinal()] = 1333;
            } catch (NoSuchFieldError nosuchfielderror1367) {
                ;
            }

            try {
                aint1[Material.PIG_SPAWN_EGG.ordinal()] = 1006;
            } catch (NoSuchFieldError nosuchfielderror1368) {
                ;
            }

            try {
                aint1[Material.PILLAGER_SPAWN_EGG.ordinal()] = 1009;
            } catch (NoSuchFieldError nosuchfielderror1369) {
                ;
            }

            try {
                aint1[Material.PINK_BANNER.ordinal()] = 1094;
            } catch (NoSuchFieldError nosuchfielderror1370) {
                ;
            }

            try {
                aint1[Material.PINK_BED.ordinal()] = 931;
            } catch (NoSuchFieldError nosuchfielderror1371) {
                ;
            }

            try {
                aint1[Material.PINK_CANDLE.ordinal()] = 1198;
            } catch (NoSuchFieldError nosuchfielderror1372) {
                ;
            }

            try {
                aint1[Material.PINK_CANDLE_CAKE.ordinal()] = 1387;
            } catch (NoSuchFieldError nosuchfielderror1373) {
                ;
            }

            try {
                aint1[Material.PINK_CARPET.ordinal()] = 431;
            } catch (NoSuchFieldError nosuchfielderror1374) {
                ;
            }

            try {
                aint1[Material.PINK_CONCRETE.ordinal()] = 540;
            } catch (NoSuchFieldError nosuchfielderror1375) {
                ;
            }

            try {
                aint1[Material.PINK_CONCRETE_POWDER.ordinal()] = 556;
            } catch (NoSuchFieldError nosuchfielderror1376) {
                ;
            }

            try {
                aint1[Material.PINK_DYE.ordinal()] = 911;
            } catch (NoSuchFieldError nosuchfielderror1377) {
                ;
            }

            try {
                aint1[Material.PINK_GLAZED_TERRACOTTA.ordinal()] = 524;
            } catch (NoSuchFieldError nosuchfielderror1378) {
                ;
            }

            try {
                aint1[Material.PINK_PETALS.ordinal()] = 225;
            } catch (NoSuchFieldError nosuchfielderror1379) {
                ;
            }

            try {
                aint1[Material.PINK_SHULKER_BOX.ordinal()] = 508;
            } catch (NoSuchFieldError nosuchfielderror1380) {
                ;
            }

            try {
                aint1[Material.PINK_STAINED_GLASS.ordinal()] = 456;
            } catch (NoSuchFieldError nosuchfielderror1381) {
                ;
            }

            try {
                aint1[Material.PINK_STAINED_GLASS_PANE.ordinal()] = 472;
            } catch (NoSuchFieldError nosuchfielderror1382) {
                ;
            }

            try {
                aint1[Material.PINK_TERRACOTTA.ordinal()] = 412;
            } catch (NoSuchFieldError nosuchfielderror1383) {
                ;
            }

            try {
                aint1[Material.PINK_TULIP.ordinal()] = 205;
            } catch (NoSuchFieldError nosuchfielderror1384) {
                ;
            }

            try {
                aint1[Material.PINK_WALL_BANNER.ordinal()] = 1340;
            } catch (NoSuchFieldError nosuchfielderror1385) {
                ;
            }

            try {
                aint1[Material.PINK_WOOL.ordinal()] = 187;
            } catch (NoSuchFieldError nosuchfielderror1386) {
                ;
            }

            try {
                aint1[Material.PISTON.ordinal()] = 641;
            } catch (NoSuchFieldError nosuchfielderror1387) {
                ;
            }

            try {
                aint1[Material.PISTON_HEAD.ordinal()] = 1259;
            } catch (NoSuchFieldError nosuchfielderror1388) {
                ;
            }

            try {
                aint1[Material.PITCHER_CROP.ordinal()] = 1351;
            } catch (NoSuchFieldError nosuchfielderror1389) {
                ;
            }

            try {
                aint1[Material.PITCHER_PLANT.ordinal()] = 211;
            } catch (NoSuchFieldError nosuchfielderror1390) {
                ;
            }

            try {
                aint1[Material.PITCHER_POD.ordinal()] = 1108;
            } catch (NoSuchFieldError nosuchfielderror1391) {
                ;
            }

            try {
                aint1[Material.PLAYER_HEAD.ordinal()] = 1060;
            } catch (NoSuchFieldError nosuchfielderror1392) {
                ;
            }

            try {
                aint1[Material.PLAYER_WALL_HEAD.ordinal()] = 1330;
            } catch (NoSuchFieldError nosuchfielderror1393) {
                ;
            }

            try {
                aint1[Material.PLENTY_POTTERY_SHERD.ordinal()] = 1250;
            } catch (NoSuchFieldError nosuchfielderror1394) {
                ;
            }

            try {
                aint1[Material.PODZOL.ordinal()] = 18;
            } catch (NoSuchFieldError nosuchfielderror1395) {
                ;
            }

            try {
                aint1[Material.POINTED_DRIPSTONE.ordinal()] = 1212;
            } catch (NoSuchFieldError nosuchfielderror1396) {
                ;
            }

            try {
                aint1[Material.POISONOUS_POTATO.ordinal()] = 1055;
            } catch (NoSuchFieldError nosuchfielderror1397) {
                ;
            }

            try {
                aint1[Material.POLAR_BEAR_SPAWN_EGG.ordinal()] = 1010;
            } catch (NoSuchFieldError nosuchfielderror1398) {
                ;
            }

            try {
                aint1[Material.POLISHED_ANDESITE.ordinal()] = 8;
            } catch (NoSuchFieldError nosuchfielderror1399) {
                ;
            }

            try {
                aint1[Material.POLISHED_ANDESITE_SLAB.ordinal()] = 629;
            } catch (NoSuchFieldError nosuchfielderror1400) {
                ;
            }

            try {
                aint1[Material.POLISHED_ANDESITE_STAIRS.ordinal()] = 612;
            } catch (NoSuchFieldError nosuchfielderror1401) {
                ;
            }

            try {
                aint1[Material.POLISHED_BASALT.ordinal()] = 308;
            } catch (NoSuchFieldError nosuchfielderror1402) {
                ;
            }

            try {
                aint1[Material.POLISHED_BLACKSTONE.ordinal()] = 1182;
            } catch (NoSuchFieldError nosuchfielderror1403) {
                ;
            }

            try {
                aint1[Material.POLISHED_BLACKSTONE_BRICKS.ordinal()] = 1186;
            } catch (NoSuchFieldError nosuchfielderror1404) {
                ;
            }

            try {
                aint1[Material.POLISHED_BLACKSTONE_BRICK_SLAB.ordinal()] = 1187;
            } catch (NoSuchFieldError nosuchfielderror1405) {
                ;
            }

            try {
                aint1[Material.POLISHED_BLACKSTONE_BRICK_STAIRS.ordinal()] = 1188;
            } catch (NoSuchFieldError nosuchfielderror1406) {
                ;
            }

            try {
                aint1[Material.POLISHED_BLACKSTONE_BRICK_WALL.ordinal()] = 393;
            } catch (NoSuchFieldError nosuchfielderror1407) {
                ;
            }

            try {
                aint1[Material.POLISHED_BLACKSTONE_BUTTON.ordinal()] = 662;
            } catch (NoSuchFieldError nosuchfielderror1408) {
                ;
            }

            try {
                aint1[Material.POLISHED_BLACKSTONE_PRESSURE_PLATE.ordinal()] = 675;
            } catch (NoSuchFieldError nosuchfielderror1409) {
                ;
            }

            try {
                aint1[Material.POLISHED_BLACKSTONE_SLAB.ordinal()] = 1183;
            } catch (NoSuchFieldError nosuchfielderror1410) {
                ;
            }

            try {
                aint1[Material.POLISHED_BLACKSTONE_STAIRS.ordinal()] = 1184;
            } catch (NoSuchFieldError nosuchfielderror1411) {
                ;
            }

            try {
                aint1[Material.POLISHED_BLACKSTONE_WALL.ordinal()] = 392;
            } catch (NoSuchFieldError nosuchfielderror1412) {
                ;
            }

            try {
                aint1[Material.POLISHED_DEEPSLATE.ordinal()] = 11;
            } catch (NoSuchFieldError nosuchfielderror1413) {
                ;
            }

            try {
                aint1[Material.POLISHED_DEEPSLATE_SLAB.ordinal()] = 632;
            } catch (NoSuchFieldError nosuchfielderror1414) {
                ;
            }

            try {
                aint1[Material.POLISHED_DEEPSLATE_STAIRS.ordinal()] = 615;
            } catch (NoSuchFieldError nosuchfielderror1415) {
                ;
            }

            try {
                aint1[Material.POLISHED_DEEPSLATE_WALL.ordinal()] = 395;
            } catch (NoSuchFieldError nosuchfielderror1416) {
                ;
            }

            try {
                aint1[Material.POLISHED_DIORITE.ordinal()] = 6;
            } catch (NoSuchFieldError nosuchfielderror1417) {
                ;
            }

            try {
                aint1[Material.POLISHED_DIORITE_SLAB.ordinal()] = 621;
            } catch (NoSuchFieldError nosuchfielderror1418) {
                ;
            }

            try {
                aint1[Material.POLISHED_DIORITE_STAIRS.ordinal()] = 603;
            } catch (NoSuchFieldError nosuchfielderror1419) {
                ;
            }

            try {
                aint1[Material.POLISHED_GRANITE.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror1420) {
                ;
            }

            try {
                aint1[Material.POLISHED_GRANITE_SLAB.ordinal()] = 618;
            } catch (NoSuchFieldError nosuchfielderror1421) {
                ;
            }

            try {
                aint1[Material.POLISHED_GRANITE_STAIRS.ordinal()] = 600;
            } catch (NoSuchFieldError nosuchfielderror1422) {
                ;
            }

            try {
                aint1[Material.POPPED_CHORUS_FRUIT.ordinal()] = 1106;
            } catch (NoSuchFieldError nosuchfielderror1423) {
                ;
            }

            try {
                aint1[Material.POPPY.ordinal()] = 198;
            } catch (NoSuchFieldError nosuchfielderror1424) {
                ;
            }

            try {
                aint1[Material.PORKCHOP.ordinal()] = 842;
            } catch (NoSuchFieldError nosuchfielderror1425) {
                ;
            }

            try {
                aint1[Material.POTATO.ordinal()] = 1053;
            } catch (NoSuchFieldError nosuchfielderror1426) {
                ;
            }

            try {
                aint1[Material.POTATOES.ordinal()] = 1326;
            } catch (NoSuchFieldError nosuchfielderror1427) {
                ;
            }

            try {
                aint1[Material.POTION.ordinal()] = 958;
            } catch (NoSuchFieldError nosuchfielderror1428) {
                ;
            }

            try {
                aint1[Material.POTTED_ACACIA_SAPLING.ordinal()] = 1303;
            } catch (NoSuchFieldError nosuchfielderror1429) {
                ;
            }

            try {
                aint1[Material.POTTED_ALLIUM.ordinal()] = 1311;
            } catch (NoSuchFieldError nosuchfielderror1430) {
                ;
            }

            try {
                aint1[Material.POTTED_AZALEA_BUSH.ordinal()] = 1401;
            } catch (NoSuchFieldError nosuchfielderror1431) {
                ;
            }

            try {
                aint1[Material.POTTED_AZURE_BLUET.ordinal()] = 1312;
            } catch (NoSuchFieldError nosuchfielderror1432) {
                ;
            }

            try {
                aint1[Material.POTTED_BAMBOO.ordinal()] = 1367;
            } catch (NoSuchFieldError nosuchfielderror1433) {
                ;
            }

            try {
                aint1[Material.POTTED_BIRCH_SAPLING.ordinal()] = 1301;
            } catch (NoSuchFieldError nosuchfielderror1434) {
                ;
            }

            try {
                aint1[Material.POTTED_BLUE_ORCHID.ordinal()] = 1310;
            } catch (NoSuchFieldError nosuchfielderror1435) {
                ;
            }

            try {
                aint1[Material.POTTED_BROWN_MUSHROOM.ordinal()] = 1322;
            } catch (NoSuchFieldError nosuchfielderror1436) {
                ;
            }

            try {
                aint1[Material.POTTED_CACTUS.ordinal()] = 1324;
            } catch (NoSuchFieldError nosuchfielderror1437) {
                ;
            }

            try {
                aint1[Material.POTTED_CHERRY_SAPLING.ordinal()] = 1304;
            } catch (NoSuchFieldError nosuchfielderror1438) {
                ;
            }

            try {
                aint1[Material.POTTED_CORNFLOWER.ordinal()] = 1318;
            } catch (NoSuchFieldError nosuchfielderror1439) {
                ;
            }

            try {
                aint1[Material.POTTED_CRIMSON_FUNGUS.ordinal()] = 1376;
            } catch (NoSuchFieldError nosuchfielderror1440) {
                ;
            }

            try {
                aint1[Material.POTTED_CRIMSON_ROOTS.ordinal()] = 1378;
            } catch (NoSuchFieldError nosuchfielderror1441) {
                ;
            }

            try {
                aint1[Material.POTTED_DANDELION.ordinal()] = 1308;
            } catch (NoSuchFieldError nosuchfielderror1442) {
                ;
            }

            try {
                aint1[Material.POTTED_DARK_OAK_SAPLING.ordinal()] = 1305;
            } catch (NoSuchFieldError nosuchfielderror1443) {
                ;
            }

            try {
                aint1[Material.POTTED_DEAD_BUSH.ordinal()] = 1323;
            } catch (NoSuchFieldError nosuchfielderror1444) {
                ;
            }

            try {
                aint1[Material.POTTED_FERN.ordinal()] = 1307;
            } catch (NoSuchFieldError nosuchfielderror1445) {
                ;
            }

            try {
                aint1[Material.POTTED_FLOWERING_AZALEA_BUSH.ordinal()] = 1402;
            } catch (NoSuchFieldError nosuchfielderror1446) {
                ;
            }

            try {
                aint1[Material.POTTED_JUNGLE_SAPLING.ordinal()] = 1302;
            } catch (NoSuchFieldError nosuchfielderror1447) {
                ;
            }

            try {
                aint1[Material.POTTED_LILY_OF_THE_VALLEY.ordinal()] = 1319;
            } catch (NoSuchFieldError nosuchfielderror1448) {
                ;
            }

            try {
                aint1[Material.POTTED_MANGROVE_PROPAGULE.ordinal()] = 1306;
            } catch (NoSuchFieldError nosuchfielderror1449) {
                ;
            }

            try {
                aint1[Material.POTTED_OAK_SAPLING.ordinal()] = 1299;
            } catch (NoSuchFieldError nosuchfielderror1450) {
                ;
            }

            try {
                aint1[Material.POTTED_ORANGE_TULIP.ordinal()] = 1314;
            } catch (NoSuchFieldError nosuchfielderror1451) {
                ;
            }

            try {
                aint1[Material.POTTED_OXEYE_DAISY.ordinal()] = 1317;
            } catch (NoSuchFieldError nosuchfielderror1452) {
                ;
            }

            try {
                aint1[Material.POTTED_PINK_TULIP.ordinal()] = 1316;
            } catch (NoSuchFieldError nosuchfielderror1453) {
                ;
            }

            try {
                aint1[Material.POTTED_POPPY.ordinal()] = 1309;
            } catch (NoSuchFieldError nosuchfielderror1454) {
                ;
            }

            try {
                aint1[Material.POTTED_RED_MUSHROOM.ordinal()] = 1321;
            } catch (NoSuchFieldError nosuchfielderror1455) {
                ;
            }

            try {
                aint1[Material.POTTED_RED_TULIP.ordinal()] = 1313;
            } catch (NoSuchFieldError nosuchfielderror1456) {
                ;
            }

            try {
                aint1[Material.POTTED_SPRUCE_SAPLING.ordinal()] = 1300;
            } catch (NoSuchFieldError nosuchfielderror1457) {
                ;
            }

            try {
                aint1[Material.POTTED_TORCHFLOWER.ordinal()] = 1298;
            } catch (NoSuchFieldError nosuchfielderror1458) {
                ;
            }

            try {
                aint1[Material.POTTED_WARPED_FUNGUS.ordinal()] = 1377;
            } catch (NoSuchFieldError nosuchfielderror1459) {
                ;
            }

            try {
                aint1[Material.POTTED_WARPED_ROOTS.ordinal()] = 1379;
            } catch (NoSuchFieldError nosuchfielderror1460) {
                ;
            }

            try {
                aint1[Material.POTTED_WHITE_TULIP.ordinal()] = 1315;
            } catch (NoSuchFieldError nosuchfielderror1461) {
                ;
            }

            try {
                aint1[Material.POTTED_WITHER_ROSE.ordinal()] = 1320;
            } catch (NoSuchFieldError nosuchfielderror1462) {
                ;
            }

            try {
                aint1[Material.POWDER_SNOW.ordinal()] = 1397;
            } catch (NoSuchFieldError nosuchfielderror1463) {
                ;
            }

            try {
                aint1[Material.POWDER_SNOW_BUCKET.ordinal()] = 872;
            } catch (NoSuchFieldError nosuchfielderror1464) {
                ;
            }

            try {
                aint1[Material.POWDER_SNOW_CAULDRON.ordinal()] = 1294;
            } catch (NoSuchFieldError nosuchfielderror1465) {
                ;
            }

            try {
                aint1[Material.POWERED_RAIL.ordinal()] = 724;
            } catch (NoSuchFieldError nosuchfielderror1466) {
                ;
            }

            try {
                aint1[Material.PRISMARINE.ordinal()] = 482;
            } catch (NoSuchFieldError nosuchfielderror1467) {
                ;
            }

            try {
                aint1[Material.PRISMARINE_BRICKS.ordinal()] = 483;
            } catch (NoSuchFieldError nosuchfielderror1468) {
                ;
            }

            try {
                aint1[Material.PRISMARINE_BRICK_SLAB.ordinal()] = 258;
            } catch (NoSuchFieldError nosuchfielderror1469) {
                ;
            }

            try {
                aint1[Material.PRISMARINE_BRICK_STAIRS.ordinal()] = 486;
            } catch (NoSuchFieldError nosuchfielderror1470) {
                ;
            }

            try {
                aint1[Material.PRISMARINE_CRYSTALS.ordinal()] = 1072;
            } catch (NoSuchFieldError nosuchfielderror1471) {
                ;
            }

            try {
                aint1[Material.PRISMARINE_SHARD.ordinal()] = 1071;
            } catch (NoSuchFieldError nosuchfielderror1472) {
                ;
            }

            try {
                aint1[Material.PRISMARINE_SLAB.ordinal()] = 257;
            } catch (NoSuchFieldError nosuchfielderror1473) {
                ;
            }

            try {
                aint1[Material.PRISMARINE_STAIRS.ordinal()] = 485;
            } catch (NoSuchFieldError nosuchfielderror1474) {
                ;
            }

            try {
                aint1[Material.PRISMARINE_WALL.ordinal()] = 379;
            } catch (NoSuchFieldError nosuchfielderror1475) {
                ;
            }

            try {
                aint1[Material.PRIZE_POTTERY_SHERD.ordinal()] = 1251;
            } catch (NoSuchFieldError nosuchfielderror1476) {
                ;
            }

            try {
                aint1[Material.PUFFERFISH.ordinal()] = 899;
            } catch (NoSuchFieldError nosuchfielderror1477) {
                ;
            }

            try {
                aint1[Material.PUFFERFISH_BUCKET.ordinal()] = 876;
            } catch (NoSuchFieldError nosuchfielderror1478) {
                ;
            }

            try {
                aint1[Material.PUFFERFISH_SPAWN_EGG.ordinal()] = 1011;
            } catch (NoSuchFieldError nosuchfielderror1479) {
                ;
            }

            try {
                aint1[Material.PUMPKIN.ordinal()] = 301;
            } catch (NoSuchFieldError nosuchfielderror1480) {
                ;
            }

            try {
                aint1[Material.PUMPKIN_PIE.ordinal()] = 1066;
            } catch (NoSuchFieldError nosuchfielderror1481) {
                ;
            }

            try {
                aint1[Material.PUMPKIN_SEEDS.ordinal()] = 946;
            } catch (NoSuchFieldError nosuchfielderror1482) {
                ;
            }

            try {
                aint1[Material.PUMPKIN_STEM.ordinal()] = 1290;
            } catch (NoSuchFieldError nosuchfielderror1483) {
                ;
            }

            try {
                aint1[Material.PURPLE_BANNER.ordinal()] = 1098;
            } catch (NoSuchFieldError nosuchfielderror1484) {
                ;
            }

            try {
                aint1[Material.PURPLE_BED.ordinal()] = 935;
            } catch (NoSuchFieldError nosuchfielderror1485) {
                ;
            }

            try {
                aint1[Material.PURPLE_CANDLE.ordinal()] = 1202;
            } catch (NoSuchFieldError nosuchfielderror1486) {
                ;
            }

            try {
                aint1[Material.PURPLE_CANDLE_CAKE.ordinal()] = 1391;
            } catch (NoSuchFieldError nosuchfielderror1487) {
                ;
            }

            try {
                aint1[Material.PURPLE_CARPET.ordinal()] = 435;
            } catch (NoSuchFieldError nosuchfielderror1488) {
                ;
            }

            try {
                aint1[Material.PURPLE_CONCRETE.ordinal()] = 544;
            } catch (NoSuchFieldError nosuchfielderror1489) {
                ;
            }

            try {
                aint1[Material.PURPLE_CONCRETE_POWDER.ordinal()] = 560;
            } catch (NoSuchFieldError nosuchfielderror1490) {
                ;
            }

            try {
                aint1[Material.PURPLE_DYE.ordinal()] = 915;
            } catch (NoSuchFieldError nosuchfielderror1491) {
                ;
            }

            try {
                aint1[Material.PURPLE_GLAZED_TERRACOTTA.ordinal()] = 528;
            } catch (NoSuchFieldError nosuchfielderror1492) {
                ;
            }

            try {
                aint1[Material.PURPLE_SHULKER_BOX.ordinal()] = 512;
            } catch (NoSuchFieldError nosuchfielderror1493) {
                ;
            }

            try {
                aint1[Material.PURPLE_STAINED_GLASS.ordinal()] = 460;
            } catch (NoSuchFieldError nosuchfielderror1494) {
                ;
            }

            try {
                aint1[Material.PURPLE_STAINED_GLASS_PANE.ordinal()] = 476;
            } catch (NoSuchFieldError nosuchfielderror1495) {
                ;
            }

            try {
                aint1[Material.PURPLE_TERRACOTTA.ordinal()] = 416;
            } catch (NoSuchFieldError nosuchfielderror1496) {
                ;
            }

            try {
                aint1[Material.PURPLE_WALL_BANNER.ordinal()] = 1344;
            } catch (NoSuchFieldError nosuchfielderror1497) {
                ;
            }

            try {
                aint1[Material.PURPLE_WOOL.ordinal()] = 191;
            } catch (NoSuchFieldError nosuchfielderror1498) {
                ;
            }

            try {
                aint1[Material.PURPUR_BLOCK.ordinal()] = 274;
            } catch (NoSuchFieldError nosuchfielderror1499) {
                ;
            }

            try {
                aint1[Material.PURPUR_PILLAR.ordinal()] = 275;
            } catch (NoSuchFieldError nosuchfielderror1500) {
                ;
            }

            try {
                aint1[Material.PURPUR_SLAB.ordinal()] = 256;
            } catch (NoSuchFieldError nosuchfielderror1501) {
                ;
            }

            try {
                aint1[Material.PURPUR_STAIRS.ordinal()] = 276;
            } catch (NoSuchFieldError nosuchfielderror1502) {
                ;
            }

            try {
                aint1[Material.QUARTZ.ordinal()] = 768;
            } catch (NoSuchFieldError nosuchfielderror1503) {
                ;
            }

            try {
                aint1[Material.QUARTZ_BLOCK.ordinal()] = 402;
            } catch (NoSuchFieldError nosuchfielderror1504) {
                ;
            }

            try {
                aint1[Material.QUARTZ_BRICKS.ordinal()] = 403;
            } catch (NoSuchFieldError nosuchfielderror1505) {
                ;
            }

            try {
                aint1[Material.QUARTZ_PILLAR.ordinal()] = 404;
            } catch (NoSuchFieldError nosuchfielderror1506) {
                ;
            }

            try {
                aint1[Material.QUARTZ_SLAB.ordinal()] = 253;
            } catch (NoSuchFieldError nosuchfielderror1507) {
                ;
            }

            try {
                aint1[Material.QUARTZ_STAIRS.ordinal()] = 405;
            } catch (NoSuchFieldError nosuchfielderror1508) {
                ;
            }

            try {
                aint1[Material.RABBIT.ordinal()] = 1073;
            } catch (NoSuchFieldError nosuchfielderror1509) {
                ;
            }

            try {
                aint1[Material.RABBIT_FOOT.ordinal()] = 1076;
            } catch (NoSuchFieldError nosuchfielderror1510) {
                ;
            }

            try {
                aint1[Material.RABBIT_HIDE.ordinal()] = 1077;
            } catch (NoSuchFieldError nosuchfielderror1511) {
                ;
            }

            try {
                aint1[Material.RABBIT_SPAWN_EGG.ordinal()] = 1012;
            } catch (NoSuchFieldError nosuchfielderror1512) {
                ;
            }

            try {
                aint1[Material.RABBIT_STEW.ordinal()] = 1075;
            } catch (NoSuchFieldError nosuchfielderror1513) {
                ;
            }

            try {
                aint1[Material.RAIL.ordinal()] = 726;
            } catch (NoSuchFieldError nosuchfielderror1514) {
                ;
            }

            try {
                aint1[Material.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE.ordinal()] = 1234;
            } catch (NoSuchFieldError nosuchfielderror1515) {
                ;
            }

            try {
                aint1[Material.RAVAGER_SPAWN_EGG.ordinal()] = 1013;
            } catch (NoSuchFieldError nosuchfielderror1516) {
                ;
            }

            try {
                aint1[Material.RAW_COPPER.ordinal()] = 772;
            } catch (NoSuchFieldError nosuchfielderror1517) {
                ;
            }

            try {
                aint1[Material.RAW_COPPER_BLOCK.ordinal()] = 71;
            } catch (NoSuchFieldError nosuchfielderror1518) {
                ;
            }

            try {
                aint1[Material.RAW_GOLD.ordinal()] = 774;
            } catch (NoSuchFieldError nosuchfielderror1519) {
                ;
            }

            try {
                aint1[Material.RAW_GOLD_BLOCK.ordinal()] = 72;
            } catch (NoSuchFieldError nosuchfielderror1520) {
                ;
            }

            try {
                aint1[Material.RAW_IRON.ordinal()] = 770;
            } catch (NoSuchFieldError nosuchfielderror1521) {
                ;
            }

            try {
                aint1[Material.RAW_IRON_BLOCK.ordinal()] = 70;
            } catch (NoSuchFieldError nosuchfielderror1522) {
                ;
            }

            try {
                aint1[Material.RECOVERY_COMPASS.ordinal()] = 890;
            } catch (NoSuchFieldError nosuchfielderror1523) {
                ;
            }

            try {
                aint1[Material.REDSTONE.ordinal()] = 636;
            } catch (NoSuchFieldError nosuchfielderror1524) {
                ;
            }

            try {
                aint1[Material.REDSTONE_BLOCK.ordinal()] = 638;
            } catch (NoSuchFieldError nosuchfielderror1525) {
                ;
            }

            try {
                aint1[Material.REDSTONE_LAMP.ordinal()] = 659;
            } catch (NoSuchFieldError nosuchfielderror1526) {
                ;
            }

            try {
                aint1[Material.REDSTONE_ORE.ordinal()] = 58;
            } catch (NoSuchFieldError nosuchfielderror1527) {
                ;
            }

            try {
                aint1[Material.REDSTONE_TORCH.ordinal()] = 637;
            } catch (NoSuchFieldError nosuchfielderror1528) {
                ;
            }

            try {
                aint1[Material.REDSTONE_WALL_TORCH.ordinal()] = 1285;
            } catch (NoSuchFieldError nosuchfielderror1529) {
                ;
            }

            try {
                aint1[Material.REDSTONE_WIRE.ordinal()] = 1264;
            } catch (NoSuchFieldError nosuchfielderror1530) {
                ;
            }

            try {
                aint1[Material.RED_BANNER.ordinal()] = 1102;
            } catch (NoSuchFieldError nosuchfielderror1531) {
                ;
            }

            try {
                aint1[Material.RED_BED.ordinal()] = 939;
            } catch (NoSuchFieldError nosuchfielderror1532) {
                ;
            }

            try {
                aint1[Material.RED_CANDLE.ordinal()] = 1206;
            } catch (NoSuchFieldError nosuchfielderror1533) {
                ;
            }

            try {
                aint1[Material.RED_CANDLE_CAKE.ordinal()] = 1395;
            } catch (NoSuchFieldError nosuchfielderror1534) {
                ;
            }

            try {
                aint1[Material.RED_CARPET.ordinal()] = 439;
            } catch (NoSuchFieldError nosuchfielderror1535) {
                ;
            }

            try {
                aint1[Material.RED_CONCRETE.ordinal()] = 548;
            } catch (NoSuchFieldError nosuchfielderror1536) {
                ;
            }

            try {
                aint1[Material.RED_CONCRETE_POWDER.ordinal()] = 564;
            } catch (NoSuchFieldError nosuchfielderror1537) {
                ;
            }

            try {
                aint1[Material.RED_DYE.ordinal()] = 919;
            } catch (NoSuchFieldError nosuchfielderror1538) {
                ;
            }

            try {
                aint1[Material.RED_GLAZED_TERRACOTTA.ordinal()] = 532;
            } catch (NoSuchFieldError nosuchfielderror1539) {
                ;
            }

            try {
                aint1[Material.RED_MUSHROOM.ordinal()] = 214;
            } catch (NoSuchFieldError nosuchfielderror1540) {
                ;
            }

            try {
                aint1[Material.RED_MUSHROOM_BLOCK.ordinal()] = 332;
            } catch (NoSuchFieldError nosuchfielderror1541) {
                ;
            }

            try {
                aint1[Material.RED_NETHER_BRICKS.ordinal()] = 498;
            } catch (NoSuchFieldError nosuchfielderror1542) {
                ;
            }

            try {
                aint1[Material.RED_NETHER_BRICK_SLAB.ordinal()] = 628;
            } catch (NoSuchFieldError nosuchfielderror1543) {
                ;
            }

            try {
                aint1[Material.RED_NETHER_BRICK_STAIRS.ordinal()] = 611;
            } catch (NoSuchFieldError nosuchfielderror1544) {
                ;
            }

            try {
                aint1[Material.RED_NETHER_BRICK_WALL.ordinal()] = 387;
            } catch (NoSuchFieldError nosuchfielderror1545) {
                ;
            }

            try {
                aint1[Material.RED_SAND.ordinal()] = 48;
            } catch (NoSuchFieldError nosuchfielderror1546) {
                ;
            }

            try {
                aint1[Material.RED_SANDSTONE.ordinal()] = 489;
            } catch (NoSuchFieldError nosuchfielderror1547) {
                ;
            }

            try {
                aint1[Material.RED_SANDSTONE_SLAB.ordinal()] = 254;
            } catch (NoSuchFieldError nosuchfielderror1548) {
                ;
            }

            try {
                aint1[Material.RED_SANDSTONE_STAIRS.ordinal()] = 492;
            } catch (NoSuchFieldError nosuchfielderror1549) {
                ;
            }

            try {
                aint1[Material.RED_SANDSTONE_WALL.ordinal()] = 380;
            } catch (NoSuchFieldError nosuchfielderror1550) {
                ;
            }

            try {
                aint1[Material.RED_SHULKER_BOX.ordinal()] = 516;
            } catch (NoSuchFieldError nosuchfielderror1551) {
                ;
            }

            try {
                aint1[Material.RED_STAINED_GLASS.ordinal()] = 464;
            } catch (NoSuchFieldError nosuchfielderror1552) {
                ;
            }

            try {
                aint1[Material.RED_STAINED_GLASS_PANE.ordinal()] = 480;
            } catch (NoSuchFieldError nosuchfielderror1553) {
                ;
            }

            try {
                aint1[Material.RED_TERRACOTTA.ordinal()] = 420;
            } catch (NoSuchFieldError nosuchfielderror1554) {
                ;
            }

            try {
                aint1[Material.RED_TULIP.ordinal()] = 202;
            } catch (NoSuchFieldError nosuchfielderror1555) {
                ;
            }

            try {
                aint1[Material.RED_WALL_BANNER.ordinal()] = 1348;
            } catch (NoSuchFieldError nosuchfielderror1556) {
                ;
            }

            try {
                aint1[Material.RED_WOOL.ordinal()] = 195;
            } catch (NoSuchFieldError nosuchfielderror1557) {
                ;
            }

            try {
                aint1[Material.REINFORCED_DEEPSLATE.ordinal()] = 330;
            } catch (NoSuchFieldError nosuchfielderror1558) {
                ;
            }

            try {
                aint1[Material.REPEATER.ordinal()] = 639;
            } catch (NoSuchFieldError nosuchfielderror1559) {
                ;
            }

            try {
                aint1[Material.REPEATING_COMMAND_BLOCK.ordinal()] = 493;
            } catch (NoSuchFieldError nosuchfielderror1560) {
                ;
            }

            try {
                aint1[Material.RESPAWN_ANCHOR.ordinal()] = 1190;
            } catch (NoSuchFieldError nosuchfielderror1561) {
                ;
            }

            try {
                aint1[Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE.ordinal()] = 1229;
            } catch (NoSuchFieldError nosuchfielderror1562) {
                ;
            }

            try {
                aint1[Material.ROOTED_DIRT.ordinal()] = 19;
            } catch (NoSuchFieldError nosuchfielderror1563) {
                ;
            }

            try {
                aint1[Material.ROSE_BUSH.ordinal()] = 446;
            } catch (NoSuchFieldError nosuchfielderror1564) {
                ;
            }

            try {
                aint1[Material.ROTTEN_FLESH.ordinal()] = 952;
            } catch (NoSuchFieldError nosuchfielderror1565) {
                ;
            }

            try {
                aint1[Material.SADDLE.ordinal()] = 728;
            } catch (NoSuchFieldError nosuchfielderror1566) {
                ;
            }

            try {
                aint1[Material.SALMON.ordinal()] = 897;
            } catch (NoSuchFieldError nosuchfielderror1567) {
                ;
            }

            try {
                aint1[Material.SALMON_BUCKET.ordinal()] = 877;
            } catch (NoSuchFieldError nosuchfielderror1568) {
                ;
            }

            try {
                aint1[Material.SALMON_SPAWN_EGG.ordinal()] = 1014;
            } catch (NoSuchFieldError nosuchfielderror1569) {
                ;
            }

            try {
                aint1[Material.SAND.ordinal()] = 45;
            } catch (NoSuchFieldError nosuchfielderror1570) {
                ;
            }

            try {
                aint1[Material.SANDSTONE.ordinal()] = 170;
            } catch (NoSuchFieldError nosuchfielderror1571) {
                ;
            }

            try {
                aint1[Material.SANDSTONE_SLAB.ordinal()] = 245;
            } catch (NoSuchFieldError nosuchfielderror1572) {
                ;
            }

            try {
                aint1[Material.SANDSTONE_STAIRS.ordinal()] = 359;
            } catch (NoSuchFieldError nosuchfielderror1573) {
                ;
            }

            try {
                aint1[Material.SANDSTONE_WALL.ordinal()] = 388;
            } catch (NoSuchFieldError nosuchfielderror1574) {
                ;
            }

            try {
                aint1[Material.SCAFFOLDING.ordinal()] = 635;
            } catch (NoSuchFieldError nosuchfielderror1575) {
                ;
            }

            try {
                aint1[Material.SCULK.ordinal()] = 350;
            } catch (NoSuchFieldError nosuchfielderror1576) {
                ;
            }

            try {
                aint1[Material.SCULK_CATALYST.ordinal()] = 352;
            } catch (NoSuchFieldError nosuchfielderror1577) {
                ;
            }

            try {
                aint1[Material.SCULK_SENSOR.ordinal()] = 654;
            } catch (NoSuchFieldError nosuchfielderror1578) {
                ;
            }

            try {
                aint1[Material.SCULK_SHRIEKER.ordinal()] = 353;
            } catch (NoSuchFieldError nosuchfielderror1579) {
                ;
            }

            try {
                aint1[Material.SCULK_VEIN.ordinal()] = 351;
            } catch (NoSuchFieldError nosuchfielderror1580) {
                ;
            }

            try {
                aint1[Material.SCUTE.ordinal()] = 758;
            } catch (NoSuchFieldError nosuchfielderror1581) {
                ;
            }

            try {
                aint1[Material.SEAGRASS.ordinal()] = 179;
            } catch (NoSuchFieldError nosuchfielderror1582) {
                ;
            }

            try {
                aint1[Material.SEA_LANTERN.ordinal()] = 488;
            } catch (NoSuchFieldError nosuchfielderror1583) {
                ;
            }

            try {
                aint1[Material.SEA_PICKLE.ordinal()] = 180;
            } catch (NoSuchFieldError nosuchfielderror1584) {
                ;
            }

            try {
                aint1[Material.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE.ordinal()] = 1220;
            } catch (NoSuchFieldError nosuchfielderror1585) {
                ;
            }

            try {
                aint1[Material.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE.ordinal()] = 1232;
            } catch (NoSuchFieldError nosuchfielderror1586) {
                ;
            }

            try {
                aint1[Material.SHEAF_POTTERY_SHERD.ordinal()] = 1252;
            } catch (NoSuchFieldError nosuchfielderror1587) {
                ;
            }

            try {
                aint1[Material.SHEARS.ordinal()] = 943;
            } catch (NoSuchFieldError nosuchfielderror1588) {
                ;
            }

            try {
                aint1[Material.SHEEP_SPAWN_EGG.ordinal()] = 1015;
            } catch (NoSuchFieldError nosuchfielderror1589) {
                ;
            }

            try {
                aint1[Material.SHELTER_POTTERY_SHERD.ordinal()] = 1253;
            } catch (NoSuchFieldError nosuchfielderror1590) {
                ;
            }

            try {
                aint1[Material.SHIELD.ordinal()] = 1117;
            } catch (NoSuchFieldError nosuchfielderror1591) {
                ;
            }

            try {
                aint1[Material.SHROOMLIGHT.ordinal()] = 1170;
            } catch (NoSuchFieldError nosuchfielderror1592) {
                ;
            }

            try {
                aint1[Material.SHULKER_BOX.ordinal()] = 501;
            } catch (NoSuchFieldError nosuchfielderror1593) {
                ;
            }

            try {
                aint1[Material.SHULKER_SHELL.ordinal()] = 1119;
            } catch (NoSuchFieldError nosuchfielderror1594) {
                ;
            }

            try {
                aint1[Material.SHULKER_SPAWN_EGG.ordinal()] = 1016;
            } catch (NoSuchFieldError nosuchfielderror1595) {
                ;
            }

            try {
                aint1[Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE.ordinal()] = 1233;
            } catch (NoSuchFieldError nosuchfielderror1596) {
                ;
            }

            try {
                aint1[Material.SILVERFISH_SPAWN_EGG.ordinal()] = 1017;
            } catch (NoSuchFieldError nosuchfielderror1597) {
                ;
            }

            try {
                aint1[Material.SKELETON_HORSE_SPAWN_EGG.ordinal()] = 1019;
            } catch (NoSuchFieldError nosuchfielderror1598) {
                ;
            }

            try {
                aint1[Material.SKELETON_SKULL.ordinal()] = 1058;
            } catch (NoSuchFieldError nosuchfielderror1599) {
                ;
            }

            try {
                aint1[Material.SKELETON_SPAWN_EGG.ordinal()] = 1018;
            } catch (NoSuchFieldError nosuchfielderror1600) {
                ;
            }

            try {
                aint1[Material.SKELETON_WALL_SKULL.ordinal()] = 1327;
            } catch (NoSuchFieldError nosuchfielderror1601) {
                ;
            }

            try {
                aint1[Material.SKULL_BANNER_PATTERN.ordinal()] = 1149;
            } catch (NoSuchFieldError nosuchfielderror1602) {
                ;
            }

            try {
                aint1[Material.SKULL_POTTERY_SHERD.ordinal()] = 1254;
            } catch (NoSuchFieldError nosuchfielderror1603) {
                ;
            }

            try {
                aint1[Material.SLIME_BALL.ordinal()] = 887;
            } catch (NoSuchFieldError nosuchfielderror1604) {
                ;
            }

            try {
                aint1[Material.SLIME_BLOCK.ordinal()] = 643;
            } catch (NoSuchFieldError nosuchfielderror1605) {
                ;
            }

            try {
                aint1[Material.SLIME_SPAWN_EGG.ordinal()] = 1020;
            } catch (NoSuchFieldError nosuchfielderror1606) {
                ;
            }

            try {
                aint1[Material.SMALL_AMETHYST_BUD.ordinal()] = 1208;
            } catch (NoSuchFieldError nosuchfielderror1607) {
                ;
            }

            try {
                aint1[Material.SMALL_DRIPLEAF.ordinal()] = 229;
            } catch (NoSuchFieldError nosuchfielderror1608) {
                ;
            }

            try {
                aint1[Material.SMITHING_TABLE.ordinal()] = 1161;
            } catch (NoSuchFieldError nosuchfielderror1609) {
                ;
            }

            try {
                aint1[Material.SMOKER.ordinal()] = 1156;
            } catch (NoSuchFieldError nosuchfielderror1610) {
                ;
            }

            try {
                aint1[Material.SMOOTH_BASALT.ordinal()] = 309;
            } catch (NoSuchFieldError nosuchfielderror1611) {
                ;
            }

            try {
                aint1[Material.SMOOTH_QUARTZ.ordinal()] = 260;
            } catch (NoSuchFieldError nosuchfielderror1612) {
                ;
            }

            try {
                aint1[Material.SMOOTH_QUARTZ_SLAB.ordinal()] = 625;
            } catch (NoSuchFieldError nosuchfielderror1613) {
                ;
            }

            try {
                aint1[Material.SMOOTH_QUARTZ_STAIRS.ordinal()] = 608;
            } catch (NoSuchFieldError nosuchfielderror1614) {
                ;
            }

            try {
                aint1[Material.SMOOTH_RED_SANDSTONE.ordinal()] = 261;
            } catch (NoSuchFieldError nosuchfielderror1615) {
                ;
            }

            try {
                aint1[Material.SMOOTH_RED_SANDSTONE_SLAB.ordinal()] = 619;
            } catch (NoSuchFieldError nosuchfielderror1616) {
                ;
            }

            try {
                aint1[Material.SMOOTH_RED_SANDSTONE_STAIRS.ordinal()] = 601;
            } catch (NoSuchFieldError nosuchfielderror1617) {
                ;
            }

            try {
                aint1[Material.SMOOTH_SANDSTONE.ordinal()] = 262;
            } catch (NoSuchFieldError nosuchfielderror1618) {
                ;
            }

            try {
                aint1[Material.SMOOTH_SANDSTONE_SLAB.ordinal()] = 624;
            } catch (NoSuchFieldError nosuchfielderror1619) {
                ;
            }

            try {
                aint1[Material.SMOOTH_SANDSTONE_STAIRS.ordinal()] = 607;
            } catch (NoSuchFieldError nosuchfielderror1620) {
                ;
            }

            try {
                aint1[Material.SMOOTH_STONE.ordinal()] = 263;
            } catch (NoSuchFieldError nosuchfielderror1621) {
                ;
            }

            try {
                aint1[Material.SMOOTH_STONE_SLAB.ordinal()] = 244;
            } catch (NoSuchFieldError nosuchfielderror1622) {
                ;
            }

            try {
                aint1[Material.SNIFFER_EGG.ordinal()] = 567;
            } catch (NoSuchFieldError nosuchfielderror1623) {
                ;
            }

            try {
                aint1[Material.SNIFFER_SPAWN_EGG.ordinal()] = 1021;
            } catch (NoSuchFieldError nosuchfielderror1624) {
                ;
            }

            try {
                aint1[Material.SNORT_POTTERY_SHERD.ordinal()] = 1255;
            } catch (NoSuchFieldError nosuchfielderror1625) {
                ;
            }

            try {
                aint1[Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE.ordinal()] = 1228;
            } catch (NoSuchFieldError nosuchfielderror1626) {
                ;
            }

            try {
                aint1[Material.SNOW.ordinal()] = 284;
            } catch (NoSuchFieldError nosuchfielderror1627) {
                ;
            }

            try {
                aint1[Material.SNOWBALL.ordinal()] = 873;
            } catch (NoSuchFieldError nosuchfielderror1628) {
                ;
            }

            try {
                aint1[Material.SNOW_BLOCK.ordinal()] = 286;
            } catch (NoSuchFieldError nosuchfielderror1629) {
                ;
            }

            try {
                aint1[Material.SNOW_GOLEM_SPAWN_EGG.ordinal()] = 1022;
            } catch (NoSuchFieldError nosuchfielderror1630) {
                ;
            }

            try {
                aint1[Material.SOUL_CAMPFIRE.ordinal()] = 1169;
            } catch (NoSuchFieldError nosuchfielderror1631) {
                ;
            }

            try {
                aint1[Material.SOUL_FIRE.ordinal()] = 1263;
            } catch (NoSuchFieldError nosuchfielderror1632) {
                ;
            }

            try {
                aint1[Material.SOUL_LANTERN.ordinal()] = 1165;
            } catch (NoSuchFieldError nosuchfielderror1633) {
                ;
            }

            try {
                aint1[Material.SOUL_SAND.ordinal()] = 305;
            } catch (NoSuchFieldError nosuchfielderror1634) {
                ;
            }

            try {
                aint1[Material.SOUL_SOIL.ordinal()] = 306;
            } catch (NoSuchFieldError nosuchfielderror1635) {
                ;
            }

            try {
                aint1[Material.SOUL_TORCH.ordinal()] = 310;
            } catch (NoSuchFieldError nosuchfielderror1636) {
                ;
            }

            try {
                aint1[Material.SOUL_WALL_TORCH.ordinal()] = 1286;
            } catch (NoSuchFieldError nosuchfielderror1637) {
                ;
            }

            try {
                aint1[Material.SPAWNER.ordinal()] = 277;
            } catch (NoSuchFieldError nosuchfielderror1638) {
                ;
            }

            try {
                aint1[Material.SPECTRAL_ARROW.ordinal()] = 1114;
            } catch (NoSuchFieldError nosuchfielderror1639) {
                ;
            }

            try {
                aint1[Material.SPIDER_EYE.ordinal()] = 960;
            } catch (NoSuchFieldError nosuchfielderror1640) {
                ;
            }

            try {
                aint1[Material.SPIDER_SPAWN_EGG.ordinal()] = 1023;
            } catch (NoSuchFieldError nosuchfielderror1641) {
                ;
            }

            try {
                aint1[Material.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE.ordinal()] = 1230;
            } catch (NoSuchFieldError nosuchfielderror1642) {
                ;
            }

            try {
                aint1[Material.SPLASH_POTION.ordinal()] = 1113;
            } catch (NoSuchFieldError nosuchfielderror1643) {
                ;
            }

            try {
                aint1[Material.SPONGE.ordinal()] = 165;
            } catch (NoSuchFieldError nosuchfielderror1644) {
                ;
            }

            try {
                aint1[Material.SPORE_BLOSSOM.ordinal()] = 212;
            } catch (NoSuchFieldError nosuchfielderror1645) {
                ;
            }

            try {
                aint1[Material.SPRUCE_BOAT.ordinal()] = 739;
            } catch (NoSuchFieldError nosuchfielderror1646) {
                ;
            }

            try {
                aint1[Material.SPRUCE_BUTTON.ordinal()] = 664;
            } catch (NoSuchFieldError nosuchfielderror1647) {
                ;
            }

            try {
                aint1[Material.SPRUCE_CHEST_BOAT.ordinal()] = 740;
            } catch (NoSuchFieldError nosuchfielderror1648) {
                ;
            }

            try {
                aint1[Material.SPRUCE_DOOR.ordinal()] = 691;
            } catch (NoSuchFieldError nosuchfielderror1649) {
                ;
            }

            try {
                aint1[Material.SPRUCE_FENCE.ordinal()] = 291;
            } catch (NoSuchFieldError nosuchfielderror1650) {
                ;
            }

            try {
                aint1[Material.SPRUCE_FENCE_GATE.ordinal()] = 714;
            } catch (NoSuchFieldError nosuchfielderror1651) {
                ;
            }

            try {
                aint1[Material.SPRUCE_HANGING_SIGN.ordinal()] = 859;
            } catch (NoSuchFieldError nosuchfielderror1652) {
                ;
            }

            try {
                aint1[Material.SPRUCE_LEAVES.ordinal()] = 156;
            } catch (NoSuchFieldError nosuchfielderror1653) {
                ;
            }

            try {
                aint1[Material.SPRUCE_LOG.ordinal()] = 112;
            } catch (NoSuchFieldError nosuchfielderror1654) {
                ;
            }

            try {
                aint1[Material.SPRUCE_PLANKS.ordinal()] = 25;
            } catch (NoSuchFieldError nosuchfielderror1655) {
                ;
            }

            try {
                aint1[Material.SPRUCE_PRESSURE_PLATE.ordinal()] = 679;
            } catch (NoSuchFieldError nosuchfielderror1656) {
                ;
            }

            try {
                aint1[Material.SPRUCE_SAPLING.ordinal()] = 37;
            } catch (NoSuchFieldError nosuchfielderror1657) {
                ;
            }

            try {
                aint1[Material.SPRUCE_SIGN.ordinal()] = 848;
            } catch (NoSuchFieldError nosuchfielderror1658) {
                ;
            }

            try {
                aint1[Material.SPRUCE_SLAB.ordinal()] = 232;
            } catch (NoSuchFieldError nosuchfielderror1659) {
                ;
            }

            try {
                aint1[Material.SPRUCE_STAIRS.ordinal()] = 363;
            } catch (NoSuchFieldError nosuchfielderror1660) {
                ;
            }

            try {
                aint1[Material.SPRUCE_TRAPDOOR.ordinal()] = 703;
            } catch (NoSuchFieldError nosuchfielderror1661) {
                ;
            }

            try {
                aint1[Material.SPRUCE_WALL_HANGING_SIGN.ordinal()] = 1275;
            } catch (NoSuchFieldError nosuchfielderror1662) {
                ;
            }

            try {
                aint1[Material.SPRUCE_WALL_SIGN.ordinal()] = 1266;
            } catch (NoSuchFieldError nosuchfielderror1663) {
                ;
            }

            try {
                aint1[Material.SPRUCE_WOOD.ordinal()] = 146;
            } catch (NoSuchFieldError nosuchfielderror1664) {
                ;
            }

            try {
                aint1[Material.SPYGLASS.ordinal()] = 894;
            } catch (NoSuchFieldError nosuchfielderror1665) {
                ;
            }

            try {
                aint1[Material.SQUID_SPAWN_EGG.ordinal()] = 1024;
            } catch (NoSuchFieldError nosuchfielderror1666) {
                ;
            }

            try {
                aint1[Material.STICK.ordinal()] = 808;
            } catch (NoSuchFieldError nosuchfielderror1667) {
                ;
            }

            try {
                aint1[Material.STICKY_PISTON.ordinal()] = 642;
            } catch (NoSuchFieldError nosuchfielderror1668) {
                ;
            }

            try {
                aint1[Material.STONE.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1669) {
                ;
            }

            try {
                aint1[Material.STONECUTTER.ordinal()] = 1162;
            } catch (NoSuchFieldError nosuchfielderror1670) {
                ;
            }

            try {
                aint1[Material.STONE_AXE.ordinal()] = 786;
            } catch (NoSuchFieldError nosuchfielderror1671) {
                ;
            }

            try {
                aint1[Material.STONE_BRICKS.ordinal()] = 319;
            } catch (NoSuchFieldError nosuchfielderror1672) {
                ;
            }

            try {
                aint1[Material.STONE_BRICK_SLAB.ordinal()] = 250;
            } catch (NoSuchFieldError nosuchfielderror1673) {
                ;
            }

            try {
                aint1[Material.STONE_BRICK_STAIRS.ordinal()] = 341;
            } catch (NoSuchFieldError nosuchfielderror1674) {
                ;
            }

            try {
                aint1[Material.STONE_BRICK_WALL.ordinal()] = 383;
            } catch (NoSuchFieldError nosuchfielderror1675) {
                ;
            }

            try {
                aint1[Material.STONE_BUTTON.ordinal()] = 661;
            } catch (NoSuchFieldError nosuchfielderror1676) {
                ;
            }

            try {
                aint1[Material.STONE_HOE.ordinal()] = 787;
            } catch (NoSuchFieldError nosuchfielderror1677) {
                ;
            }

            try {
                aint1[Material.STONE_PICKAXE.ordinal()] = 785;
            } catch (NoSuchFieldError nosuchfielderror1678) {
                ;
            }

            try {
                aint1[Material.STONE_PRESSURE_PLATE.ordinal()] = 674;
            } catch (NoSuchFieldError nosuchfielderror1679) {
                ;
            }

            try {
                aint1[Material.STONE_SHOVEL.ordinal()] = 784;
            } catch (NoSuchFieldError nosuchfielderror1680) {
                ;
            }

            try {
                aint1[Material.STONE_SLAB.ordinal()] = 243;
            } catch (NoSuchFieldError nosuchfielderror1681) {
                ;
            }

            try {
                aint1[Material.STONE_STAIRS.ordinal()] = 606;
            } catch (NoSuchFieldError nosuchfielderror1682) {
                ;
            }

            try {
                aint1[Material.STONE_SWORD.ordinal()] = 783;
            } catch (NoSuchFieldError nosuchfielderror1683) {
                ;
            }

            try {
                aint1[Material.STRAY_SPAWN_EGG.ordinal()] = 1025;
            } catch (NoSuchFieldError nosuchfielderror1684) {
                ;
            }

            try {
                aint1[Material.STRIDER_SPAWN_EGG.ordinal()] = 1026;
            } catch (NoSuchFieldError nosuchfielderror1685) {
                ;
            }

            try {
                aint1[Material.STRING.ordinal()] = 811;
            } catch (NoSuchFieldError nosuchfielderror1686) {
                ;
            }

            try {
                aint1[Material.STRIPPED_ACACIA_LOG.ordinal()] = 128;
            } catch (NoSuchFieldError nosuchfielderror1687) {
                ;
            }

            try {
                aint1[Material.STRIPPED_ACACIA_WOOD.ordinal()] = 138;
            } catch (NoSuchFieldError nosuchfielderror1688) {
                ;
            }

            try {
                aint1[Material.STRIPPED_BAMBOO_BLOCK.ordinal()] = 144;
            } catch (NoSuchFieldError nosuchfielderror1689) {
                ;
            }

            try {
                aint1[Material.STRIPPED_BIRCH_LOG.ordinal()] = 126;
            } catch (NoSuchFieldError nosuchfielderror1690) {
                ;
            }

            try {
                aint1[Material.STRIPPED_BIRCH_WOOD.ordinal()] = 136;
            } catch (NoSuchFieldError nosuchfielderror1691) {
                ;
            }

            try {
                aint1[Material.STRIPPED_CHERRY_LOG.ordinal()] = 129;
            } catch (NoSuchFieldError nosuchfielderror1692) {
                ;
            }

            try {
                aint1[Material.STRIPPED_CHERRY_WOOD.ordinal()] = 139;
            } catch (NoSuchFieldError nosuchfielderror1693) {
                ;
            }

            try {
                aint1[Material.STRIPPED_CRIMSON_HYPHAE.ordinal()] = 142;
            } catch (NoSuchFieldError nosuchfielderror1694) {
                ;
            }

            try {
                aint1[Material.STRIPPED_CRIMSON_STEM.ordinal()] = 132;
            } catch (NoSuchFieldError nosuchfielderror1695) {
                ;
            }

            try {
                aint1[Material.STRIPPED_DARK_OAK_LOG.ordinal()] = 130;
            } catch (NoSuchFieldError nosuchfielderror1696) {
                ;
            }

            try {
                aint1[Material.STRIPPED_DARK_OAK_WOOD.ordinal()] = 140;
            } catch (NoSuchFieldError nosuchfielderror1697) {
                ;
            }

            try {
                aint1[Material.STRIPPED_JUNGLE_LOG.ordinal()] = 127;
            } catch (NoSuchFieldError nosuchfielderror1698) {
                ;
            }

            try {
                aint1[Material.STRIPPED_JUNGLE_WOOD.ordinal()] = 137;
            } catch (NoSuchFieldError nosuchfielderror1699) {
                ;
            }

            try {
                aint1[Material.STRIPPED_MANGROVE_LOG.ordinal()] = 131;
            } catch (NoSuchFieldError nosuchfielderror1700) {
                ;
            }

            try {
                aint1[Material.STRIPPED_MANGROVE_WOOD.ordinal()] = 141;
            } catch (NoSuchFieldError nosuchfielderror1701) {
                ;
            }

            try {
                aint1[Material.STRIPPED_OAK_LOG.ordinal()] = 124;
            } catch (NoSuchFieldError nosuchfielderror1702) {
                ;
            }

            try {
                aint1[Material.STRIPPED_OAK_WOOD.ordinal()] = 134;
            } catch (NoSuchFieldError nosuchfielderror1703) {
                ;
            }

            try {
                aint1[Material.STRIPPED_SPRUCE_LOG.ordinal()] = 125;
            } catch (NoSuchFieldError nosuchfielderror1704) {
                ;
            }

            try {
                aint1[Material.STRIPPED_SPRUCE_WOOD.ordinal()] = 135;
            } catch (NoSuchFieldError nosuchfielderror1705) {
                ;
            }

            try {
                aint1[Material.STRIPPED_WARPED_HYPHAE.ordinal()] = 143;
            } catch (NoSuchFieldError nosuchfielderror1706) {
                ;
            }

            try {
                aint1[Material.STRIPPED_WARPED_STEM.ordinal()] = 133;
            } catch (NoSuchFieldError nosuchfielderror1707) {
                ;
            }

            try {
                aint1[Material.STRUCTURE_BLOCK.ordinal()] = 755;
            } catch (NoSuchFieldError nosuchfielderror1708) {
                ;
            }

            try {
                aint1[Material.STRUCTURE_VOID.ordinal()] = 500;
            } catch (NoSuchFieldError nosuchfielderror1709) {
                ;
            }

            try {
                aint1[Material.SUGAR.ordinal()] = 923;
            } catch (NoSuchFieldError nosuchfielderror1710) {
                ;
            }

            try {
                aint1[Material.SUGAR_CANE.ordinal()] = 222;
            } catch (NoSuchFieldError nosuchfielderror1711) {
                ;
            }

            try {
                aint1[Material.SUNFLOWER.ordinal()] = 444;
            } catch (NoSuchFieldError nosuchfielderror1712) {
                ;
            }

            try {
                aint1[Material.SUSPICIOUS_GRAVEL.ordinal()] = 47;
            } catch (NoSuchFieldError nosuchfielderror1713) {
                ;
            }

            try {
                aint1[Material.SUSPICIOUS_SAND.ordinal()] = 46;
            } catch (NoSuchFieldError nosuchfielderror1714) {
                ;
            }

            try {
                aint1[Material.SUSPICIOUS_STEW.ordinal()] = 1145;
            } catch (NoSuchFieldError nosuchfielderror1715) {
                ;
            }

            try {
                aint1[Material.SWEET_BERRIES.ordinal()] = 1166;
            } catch (NoSuchFieldError nosuchfielderror1716) {
                ;
            }

            try {
                aint1[Material.SWEET_BERRY_BUSH.ordinal()] = 1371;
            } catch (NoSuchFieldError nosuchfielderror1717) {
                ;
            }

            try {
                aint1[Material.TADPOLE_BUCKET.ordinal()] = 881;
            } catch (NoSuchFieldError nosuchfielderror1718) {
                ;
            }

            try {
                aint1[Material.TADPOLE_SPAWN_EGG.ordinal()] = 1027;
            } catch (NoSuchFieldError nosuchfielderror1719) {
                ;
            }

            try {
                aint1[Material.TALL_GRASS.ordinal()] = 448;
            } catch (NoSuchFieldError nosuchfielderror1720) {
                ;
            }

            try {
                aint1[Material.TALL_SEAGRASS.ordinal()] = 1258;
            } catch (NoSuchFieldError nosuchfielderror1721) {
                ;
            }

            try {
                aint1[Material.TARGET.ordinal()] = 650;
            } catch (NoSuchFieldError nosuchfielderror1722) {
                ;
            }

            try {
                aint1[Material.TERRACOTTA.ordinal()] = 441;
            } catch (NoSuchFieldError nosuchfielderror1723) {
                ;
            }

            try {
                aint1[Material.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE.ordinal()] = 1227;
            } catch (NoSuchFieldError nosuchfielderror1724) {
                ;
            }

            try {
                aint1[Material.TINTED_GLASS.ordinal()] = 168;
            } catch (NoSuchFieldError nosuchfielderror1725) {
                ;
            }

            try {
                aint1[Material.TIPPED_ARROW.ordinal()] = 1115;
            } catch (NoSuchFieldError nosuchfielderror1726) {
                ;
            }

            try {
                aint1[Material.TNT.ordinal()] = 658;
            } catch (NoSuchFieldError nosuchfielderror1727) {
                ;
            }

            try {
                aint1[Material.TNT_MINECART.ordinal()] = 732;
            } catch (NoSuchFieldError nosuchfielderror1728) {
                ;
            }

            try {
                aint1[Material.TORCH.ordinal()] = 270;
            } catch (NoSuchFieldError nosuchfielderror1729) {
                ;
            }

            try {
                aint1[Material.TORCHFLOWER.ordinal()] = 210;
            } catch (NoSuchFieldError nosuchfielderror1730) {
                ;
            }

            try {
                aint1[Material.TORCHFLOWER_CROP.ordinal()] = 1350;
            } catch (NoSuchFieldError nosuchfielderror1731) {
                ;
            }

            try {
                aint1[Material.TORCHFLOWER_SEEDS.ordinal()] = 1107;
            } catch (NoSuchFieldError nosuchfielderror1732) {
                ;
            }

            try {
                aint1[Material.TOTEM_OF_UNDYING.ordinal()] = 1118;
            } catch (NoSuchFieldError nosuchfielderror1733) {
                ;
            }

            try {
                aint1[Material.TRADER_LLAMA_SPAWN_EGG.ordinal()] = 1028;
            } catch (NoSuchFieldError nosuchfielderror1734) {
                ;
            }

            try {
                aint1[Material.TRAPPED_CHEST.ordinal()] = 657;
            } catch (NoSuchFieldError nosuchfielderror1735) {
                ;
            }

            try {
                aint1[Material.TRIDENT.ordinal()] = 1140;
            } catch (NoSuchFieldError nosuchfielderror1736) {
                ;
            }

            try {
                aint1[Material.TRIPWIRE.ordinal()] = 1297;
            } catch (NoSuchFieldError nosuchfielderror1737) {
                ;
            }

            try {
                aint1[Material.TRIPWIRE_HOOK.ordinal()] = 656;
            } catch (NoSuchFieldError nosuchfielderror1738) {
                ;
            }

            try {
                aint1[Material.TROPICAL_FISH.ordinal()] = 898;
            } catch (NoSuchFieldError nosuchfielderror1739) {
                ;
            }

            try {
                aint1[Material.TROPICAL_FISH_BUCKET.ordinal()] = 879;
            } catch (NoSuchFieldError nosuchfielderror1740) {
                ;
            }

            try {
                aint1[Material.TROPICAL_FISH_SPAWN_EGG.ordinal()] = 1029;
            } catch (NoSuchFieldError nosuchfielderror1741) {
                ;
            }

            try {
                aint1[Material.TUBE_CORAL.ordinal()] = 578;
            } catch (NoSuchFieldError nosuchfielderror1742) {
                ;
            }

            try {
                aint1[Material.TUBE_CORAL_BLOCK.ordinal()] = 573;
            } catch (NoSuchFieldError nosuchfielderror1743) {
                ;
            }

            try {
                aint1[Material.TUBE_CORAL_FAN.ordinal()] = 588;
            } catch (NoSuchFieldError nosuchfielderror1744) {
                ;
            }

            try {
                aint1[Material.TUBE_CORAL_WALL_FAN.ordinal()] = 1361;
            } catch (NoSuchFieldError nosuchfielderror1745) {
                ;
            }

            try {
                aint1[Material.TUFF.ordinal()] = 13;
            } catch (NoSuchFieldError nosuchfielderror1746) {
                ;
            }

            try {
                aint1[Material.TURTLE_EGG.ordinal()] = 566;
            } catch (NoSuchFieldError nosuchfielderror1747) {
                ;
            }

            try {
                aint1[Material.TURTLE_HELMET.ordinal()] = 757;
            } catch (NoSuchFieldError nosuchfielderror1748) {
                ;
            }

            try {
                aint1[Material.TURTLE_SPAWN_EGG.ordinal()] = 1030;
            } catch (NoSuchFieldError nosuchfielderror1749) {
                ;
            }

            try {
                aint1[Material.TWISTING_VINES.ordinal()] = 221;
            } catch (NoSuchFieldError nosuchfielderror1750) {
                ;
            }

            try {
                aint1[Material.TWISTING_VINES_PLANT.ordinal()] = 1373;
            } catch (NoSuchFieldError nosuchfielderror1751) {
                ;
            }

            try {
                aint1[Material.VERDANT_FROGLIGHT.ordinal()] = 1214;
            } catch (NoSuchFieldError nosuchfielderror1752) {
                ;
            }

            try {
                aint1[Material.VEX_ARMOR_TRIM_SMITHING_TEMPLATE.ordinal()] = 1226;
            } catch (NoSuchFieldError nosuchfielderror1753) {
                ;
            }

            try {
                aint1[Material.VEX_SPAWN_EGG.ordinal()] = 1031;
            } catch (NoSuchFieldError nosuchfielderror1754) {
                ;
            }

            try {
                aint1[Material.VILLAGER_SPAWN_EGG.ordinal()] = 1032;
            } catch (NoSuchFieldError nosuchfielderror1755) {
                ;
            }

            try {
                aint1[Material.VINDICATOR_SPAWN_EGG.ordinal()] = 1033;
            } catch (NoSuchFieldError nosuchfielderror1756) {
                ;
            }

            try {
                aint1[Material.VINE.ordinal()] = 338;
            } catch (NoSuchFieldError nosuchfielderror1757) {
                ;
            }

            try {
                aint1[Material.VOID_AIR.ordinal()] = 1368;
            } catch (NoSuchFieldError nosuchfielderror1758) {
                ;
            }

            try {
                aint1[Material.WALL_TORCH.ordinal()] = 1261;
            } catch (NoSuchFieldError nosuchfielderror1759) {
                ;
            }

            try {
                aint1[Material.WANDERING_TRADER_SPAWN_EGG.ordinal()] = 1034;
            } catch (NoSuchFieldError nosuchfielderror1760) {
                ;
            }

            try {
                aint1[Material.WARDEN_SPAWN_EGG.ordinal()] = 1035;
            } catch (NoSuchFieldError nosuchfielderror1761) {
                ;
            }

            try {
                aint1[Material.WARD_ARMOR_TRIM_SMITHING_TEMPLATE.ordinal()] = 1224;
            } catch (NoSuchFieldError nosuchfielderror1762) {
                ;
            }

            try {
                aint1[Material.WARPED_BUTTON.ordinal()] = 673;
            } catch (NoSuchFieldError nosuchfielderror1763) {
                ;
            }

            try {
                aint1[Material.WARPED_DOOR.ordinal()] = 700;
            } catch (NoSuchFieldError nosuchfielderror1764) {
                ;
            }

            try {
                aint1[Material.WARPED_FENCE.ordinal()] = 300;
            } catch (NoSuchFieldError nosuchfielderror1765) {
                ;
            }

            try {
                aint1[Material.WARPED_FENCE_GATE.ordinal()] = 723;
            } catch (NoSuchFieldError nosuchfielderror1766) {
                ;
            }

            try {
                aint1[Material.WARPED_FUNGUS.ordinal()] = 216;
            } catch (NoSuchFieldError nosuchfielderror1767) {
                ;
            }

            try {
                aint1[Material.WARPED_FUNGUS_ON_A_STICK.ordinal()] = 735;
            } catch (NoSuchFieldError nosuchfielderror1768) {
                ;
            }

            try {
                aint1[Material.WARPED_HANGING_SIGN.ordinal()] = 868;
            } catch (NoSuchFieldError nosuchfielderror1769) {
                ;
            }

            try {
                aint1[Material.WARPED_HYPHAE.ordinal()] = 154;
            } catch (NoSuchFieldError nosuchfielderror1770) {
                ;
            }

            try {
                aint1[Material.WARPED_NYLIUM.ordinal()] = 22;
            } catch (NoSuchFieldError nosuchfielderror1771) {
                ;
            }

            try {
                aint1[Material.WARPED_PLANKS.ordinal()] = 34;
            } catch (NoSuchFieldError nosuchfielderror1772) {
                ;
            }

            try {
                aint1[Material.WARPED_PRESSURE_PLATE.ordinal()] = 688;
            } catch (NoSuchFieldError nosuchfielderror1773) {
                ;
            }

            try {
                aint1[Material.WARPED_ROOTS.ordinal()] = 218;
            } catch (NoSuchFieldError nosuchfielderror1774) {
                ;
            }

            try {
                aint1[Material.WARPED_SIGN.ordinal()] = 857;
            } catch (NoSuchFieldError nosuchfielderror1775) {
                ;
            }

            try {
                aint1[Material.WARPED_SLAB.ordinal()] = 242;
            } catch (NoSuchFieldError nosuchfielderror1776) {
                ;
            }

            try {
                aint1[Material.WARPED_STAIRS.ordinal()] = 373;
            } catch (NoSuchFieldError nosuchfielderror1777) {
                ;
            }

            try {
                aint1[Material.WARPED_STEM.ordinal()] = 122;
            } catch (NoSuchFieldError nosuchfielderror1778) {
                ;
            }

            try {
                aint1[Material.WARPED_TRAPDOOR.ordinal()] = 712;
            } catch (NoSuchFieldError nosuchfielderror1779) {
                ;
            }

            try {
                aint1[Material.WARPED_WALL_HANGING_SIGN.ordinal()] = 1283;
            } catch (NoSuchFieldError nosuchfielderror1780) {
                ;
            }

            try {
                aint1[Material.WARPED_WALL_SIGN.ordinal()] = 1375;
            } catch (NoSuchFieldError nosuchfielderror1781) {
                ;
            }

            try {
                aint1[Material.WARPED_WART_BLOCK.ordinal()] = 497;
            } catch (NoSuchFieldError nosuchfielderror1782) {
                ;
            }

            try {
                aint1[Material.WATER.ordinal()] = 1256;
            } catch (NoSuchFieldError nosuchfielderror1783) {
                ;
            }

            try {
                aint1[Material.WATER_BUCKET.ordinal()] = 870;
            } catch (NoSuchFieldError nosuchfielderror1784) {
                ;
            }

            try {
                aint1[Material.WATER_CAULDRON.ordinal()] = 1292;
            } catch (NoSuchFieldError nosuchfielderror1785) {
                ;
            }

            try {
                aint1[Material.WAXED_COPPER_BLOCK.ordinal()] = 95;
            } catch (NoSuchFieldError nosuchfielderror1786) {
                ;
            }

            try {
                aint1[Material.WAXED_CUT_COPPER.ordinal()] = 99;
            } catch (NoSuchFieldError nosuchfielderror1787) {
                ;
            }

            try {
                aint1[Material.WAXED_CUT_COPPER_SLAB.ordinal()] = 107;
            } catch (NoSuchFieldError nosuchfielderror1788) {
                ;
            }

            try {
                aint1[Material.WAXED_CUT_COPPER_STAIRS.ordinal()] = 103;
            } catch (NoSuchFieldError nosuchfielderror1789) {
                ;
            }

            try {
                aint1[Material.WAXED_EXPOSED_COPPER.ordinal()] = 96;
            } catch (NoSuchFieldError nosuchfielderror1790) {
                ;
            }

            try {
                aint1[Material.WAXED_EXPOSED_CUT_COPPER.ordinal()] = 100;
            } catch (NoSuchFieldError nosuchfielderror1791) {
                ;
            }

            try {
                aint1[Material.WAXED_EXPOSED_CUT_COPPER_SLAB.ordinal()] = 108;
            } catch (NoSuchFieldError nosuchfielderror1792) {
                ;
            }

            try {
                aint1[Material.WAXED_EXPOSED_CUT_COPPER_STAIRS.ordinal()] = 104;
            } catch (NoSuchFieldError nosuchfielderror1793) {
                ;
            }

            try {
                aint1[Material.WAXED_OXIDIZED_COPPER.ordinal()] = 98;
            } catch (NoSuchFieldError nosuchfielderror1794) {
                ;
            }

            try {
                aint1[Material.WAXED_OXIDIZED_CUT_COPPER.ordinal()] = 102;
            } catch (NoSuchFieldError nosuchfielderror1795) {
                ;
            }

            try {
                aint1[Material.WAXED_OXIDIZED_CUT_COPPER_SLAB.ordinal()] = 110;
            } catch (NoSuchFieldError nosuchfielderror1796) {
                ;
            }

            try {
                aint1[Material.WAXED_OXIDIZED_CUT_COPPER_STAIRS.ordinal()] = 106;
            } catch (NoSuchFieldError nosuchfielderror1797) {
                ;
            }

            try {
                aint1[Material.WAXED_WEATHERED_COPPER.ordinal()] = 97;
            } catch (NoSuchFieldError nosuchfielderror1798) {
                ;
            }

            try {
                aint1[Material.WAXED_WEATHERED_CUT_COPPER.ordinal()] = 101;
            } catch (NoSuchFieldError nosuchfielderror1799) {
                ;
            }

            try {
                aint1[Material.WAXED_WEATHERED_CUT_COPPER_SLAB.ordinal()] = 109;
            } catch (NoSuchFieldError nosuchfielderror1800) {
                ;
            }

            try {
                aint1[Material.WAXED_WEATHERED_CUT_COPPER_STAIRS.ordinal()] = 105;
            } catch (NoSuchFieldError nosuchfielderror1801) {
                ;
            }

            try {
                aint1[Material.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE.ordinal()] = 1231;
            } catch (NoSuchFieldError nosuchfielderror1802) {
                ;
            }

            try {
                aint1[Material.WEATHERED_COPPER.ordinal()] = 81;
            } catch (NoSuchFieldError nosuchfielderror1803) {
                ;
            }

            try {
                aint1[Material.WEATHERED_CUT_COPPER.ordinal()] = 85;
            } catch (NoSuchFieldError nosuchfielderror1804) {
                ;
            }

            try {
                aint1[Material.WEATHERED_CUT_COPPER_SLAB.ordinal()] = 93;
            } catch (NoSuchFieldError nosuchfielderror1805) {
                ;
            }

            try {
                aint1[Material.WEATHERED_CUT_COPPER_STAIRS.ordinal()] = 89;
            } catch (NoSuchFieldError nosuchfielderror1806) {
                ;
            }

            try {
                aint1[Material.WEEPING_VINES.ordinal()] = 220;
            } catch (NoSuchFieldError nosuchfielderror1807) {
                ;
            }

            try {
                aint1[Material.WEEPING_VINES_PLANT.ordinal()] = 1372;
            } catch (NoSuchFieldError nosuchfielderror1808) {
                ;
            }

            try {
                aint1[Material.WET_SPONGE.ordinal()] = 166;
            } catch (NoSuchFieldError nosuchfielderror1809) {
                ;
            }

            try {
                aint1[Material.WHEAT.ordinal()] = 815;
            } catch (NoSuchFieldError nosuchfielderror1810) {
                ;
            }

            try {
                aint1[Material.WHEAT_SEEDS.ordinal()] = 814;
            } catch (NoSuchFieldError nosuchfielderror1811) {
                ;
            }

            try {
                aint1[Material.WHITE_BANNER.ordinal()] = 1088;
            } catch (NoSuchFieldError nosuchfielderror1812) {
                ;
            }

            try {
                aint1[Material.WHITE_BED.ordinal()] = 925;
            } catch (NoSuchFieldError nosuchfielderror1813) {
                ;
            }

            try {
                aint1[Material.WHITE_CANDLE.ordinal()] = 1192;
            } catch (NoSuchFieldError nosuchfielderror1814) {
                ;
            }

            try {
                aint1[Material.WHITE_CANDLE_CAKE.ordinal()] = 1381;
            } catch (NoSuchFieldError nosuchfielderror1815) {
                ;
            }

            try {
                aint1[Material.WHITE_CARPET.ordinal()] = 425;
            } catch (NoSuchFieldError nosuchfielderror1816) {
                ;
            }

            try {
                aint1[Material.WHITE_CONCRETE.ordinal()] = 534;
            } catch (NoSuchFieldError nosuchfielderror1817) {
                ;
            }

            try {
                aint1[Material.WHITE_CONCRETE_POWDER.ordinal()] = 550;
            } catch (NoSuchFieldError nosuchfielderror1818) {
                ;
            }

            try {
                aint1[Material.WHITE_DYE.ordinal()] = 905;
            } catch (NoSuchFieldError nosuchfielderror1819) {
                ;
            }

            try {
                aint1[Material.WHITE_GLAZED_TERRACOTTA.ordinal()] = 518;
            } catch (NoSuchFieldError nosuchfielderror1820) {
                ;
            }

            try {
                aint1[Material.WHITE_SHULKER_BOX.ordinal()] = 502;
            } catch (NoSuchFieldError nosuchfielderror1821) {
                ;
            }

            try {
                aint1[Material.WHITE_STAINED_GLASS.ordinal()] = 450;
            } catch (NoSuchFieldError nosuchfielderror1822) {
                ;
            }

            try {
                aint1[Material.WHITE_STAINED_GLASS_PANE.ordinal()] = 466;
            } catch (NoSuchFieldError nosuchfielderror1823) {
                ;
            }

            try {
                aint1[Material.WHITE_TERRACOTTA.ordinal()] = 406;
            } catch (NoSuchFieldError nosuchfielderror1824) {
                ;
            }

            try {
                aint1[Material.WHITE_TULIP.ordinal()] = 204;
            } catch (NoSuchFieldError nosuchfielderror1825) {
                ;
            }

            try {
                aint1[Material.WHITE_WALL_BANNER.ordinal()] = 1334;
            } catch (NoSuchFieldError nosuchfielderror1826) {
                ;
            }

            try {
                aint1[Material.WHITE_WOOL.ordinal()] = 181;
            } catch (NoSuchFieldError nosuchfielderror1827) {
                ;
            }

            try {
                aint1[Material.WILD_ARMOR_TRIM_SMITHING_TEMPLATE.ordinal()] = 1223;
            } catch (NoSuchFieldError nosuchfielderror1828) {
                ;
            }

            try {
                aint1[Material.WITCH_SPAWN_EGG.ordinal()] = 1036;
            } catch (NoSuchFieldError nosuchfielderror1829) {
                ;
            }

            try {
                aint1[Material.WITHER_ROSE.ordinal()] = 209;
            } catch (NoSuchFieldError nosuchfielderror1830) {
                ;
            }

            try {
                aint1[Material.WITHER_SKELETON_SKULL.ordinal()] = 1059;
            } catch (NoSuchFieldError nosuchfielderror1831) {
                ;
            }

            try {
                aint1[Material.WITHER_SKELETON_SPAWN_EGG.ordinal()] = 1038;
            } catch (NoSuchFieldError nosuchfielderror1832) {
                ;
            }

            try {
                aint1[Material.WITHER_SKELETON_WALL_SKULL.ordinal()] = 1328;
            } catch (NoSuchFieldError nosuchfielderror1833) {
                ;
            }

            try {
                aint1[Material.WITHER_SPAWN_EGG.ordinal()] = 1037;
            } catch (NoSuchFieldError nosuchfielderror1834) {
                ;
            }

            try {
                aint1[Material.WOLF_SPAWN_EGG.ordinal()] = 1039;
            } catch (NoSuchFieldError nosuchfielderror1835) {
                ;
            }

            try {
                aint1[Material.WOODEN_AXE.ordinal()] = 781;
            } catch (NoSuchFieldError nosuchfielderror1836) {
                ;
            }

            try {
                aint1[Material.WOODEN_HOE.ordinal()] = 782;
            } catch (NoSuchFieldError nosuchfielderror1837) {
                ;
            }

            try {
                aint1[Material.WOODEN_PICKAXE.ordinal()] = 780;
            } catch (NoSuchFieldError nosuchfielderror1838) {
                ;
            }

            try {
                aint1[Material.WOODEN_SHOVEL.ordinal()] = 779;
            } catch (NoSuchFieldError nosuchfielderror1839) {
                ;
            }

            try {
                aint1[Material.WOODEN_SWORD.ordinal()] = 778;
            } catch (NoSuchFieldError nosuchfielderror1840) {
                ;
            }

            try {
                aint1[Material.WRITABLE_BOOK.ordinal()] = 1047;
            } catch (NoSuchFieldError nosuchfielderror1841) {
                ;
            }

            try {
                aint1[Material.WRITTEN_BOOK.ordinal()] = 1048;
            } catch (NoSuchFieldError nosuchfielderror1842) {
                ;
            }

            try {
                aint1[Material.YELLOW_BANNER.ordinal()] = 1092;
            } catch (NoSuchFieldError nosuchfielderror1843) {
                ;
            }

            try {
                aint1[Material.YELLOW_BED.ordinal()] = 929;
            } catch (NoSuchFieldError nosuchfielderror1844) {
                ;
            }

            try {
                aint1[Material.YELLOW_CANDLE.ordinal()] = 1196;
            } catch (NoSuchFieldError nosuchfielderror1845) {
                ;
            }

            try {
                aint1[Material.YELLOW_CANDLE_CAKE.ordinal()] = 1385;
            } catch (NoSuchFieldError nosuchfielderror1846) {
                ;
            }

            try {
                aint1[Material.YELLOW_CARPET.ordinal()] = 429;
            } catch (NoSuchFieldError nosuchfielderror1847) {
                ;
            }

            try {
                aint1[Material.YELLOW_CONCRETE.ordinal()] = 538;
            } catch (NoSuchFieldError nosuchfielderror1848) {
                ;
            }

            try {
                aint1[Material.YELLOW_CONCRETE_POWDER.ordinal()] = 554;
            } catch (NoSuchFieldError nosuchfielderror1849) {
                ;
            }

            try {
                aint1[Material.YELLOW_DYE.ordinal()] = 909;
            } catch (NoSuchFieldError nosuchfielderror1850) {
                ;
            }

            try {
                aint1[Material.YELLOW_GLAZED_TERRACOTTA.ordinal()] = 522;
            } catch (NoSuchFieldError nosuchfielderror1851) {
                ;
            }

            try {
                aint1[Material.YELLOW_SHULKER_BOX.ordinal()] = 506;
            } catch (NoSuchFieldError nosuchfielderror1852) {
                ;
            }

            try {
                aint1[Material.YELLOW_STAINED_GLASS.ordinal()] = 454;
            } catch (NoSuchFieldError nosuchfielderror1853) {
                ;
            }

            try {
                aint1[Material.YELLOW_STAINED_GLASS_PANE.ordinal()] = 470;
            } catch (NoSuchFieldError nosuchfielderror1854) {
                ;
            }

            try {
                aint1[Material.YELLOW_TERRACOTTA.ordinal()] = 410;
            } catch (NoSuchFieldError nosuchfielderror1855) {
                ;
            }

            try {
                aint1[Material.YELLOW_WALL_BANNER.ordinal()] = 1338;
            } catch (NoSuchFieldError nosuchfielderror1856) {
                ;
            }

            try {
                aint1[Material.YELLOW_WOOL.ordinal()] = 185;
            } catch (NoSuchFieldError nosuchfielderror1857) {
                ;
            }

            try {
                aint1[Material.ZOGLIN_SPAWN_EGG.ordinal()] = 1040;
            } catch (NoSuchFieldError nosuchfielderror1858) {
                ;
            }

            try {
                aint1[Material.ZOMBIE_HEAD.ordinal()] = 1061;
            } catch (NoSuchFieldError nosuchfielderror1859) {
                ;
            }

            try {
                aint1[Material.ZOMBIE_HORSE_SPAWN_EGG.ordinal()] = 1042;
            } catch (NoSuchFieldError nosuchfielderror1860) {
                ;
            }

            try {
                aint1[Material.ZOMBIE_SPAWN_EGG.ordinal()] = 1041;
            } catch (NoSuchFieldError nosuchfielderror1861) {
                ;
            }

            try {
                aint1[Material.ZOMBIE_VILLAGER_SPAWN_EGG.ordinal()] = 1043;
            } catch (NoSuchFieldError nosuchfielderror1862) {
                ;
            }

            try {
                aint1[Material.ZOMBIE_WALL_HEAD.ordinal()] = 1329;
            } catch (NoSuchFieldError nosuchfielderror1863) {
                ;
            }

            try {
                aint1[Material.ZOMBIFIED_PIGLIN_SPAWN_EGG.ordinal()] = 1044;
            } catch (NoSuchFieldError nosuchfielderror1864) {
                ;
            }

            CraftEventFactory.$SWITCH_TABLE$org$bukkit$Material = aint1;
            return aint1;
        }
    }

    static int[] $SWITCH_TABLE$org$bukkit$entity$EntityType() {
        int[] aint = CraftEventFactory.$SWITCH_TABLE$org$bukkit$entity$EntityType;

        if (aint != null) {
            return aint;
        } else {
            int[] aint1 = new int[EntityType.values().length];

            try {
                aint1[EntityType.ALLAY.ordinal()] = 111;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                aint1[EntityType.AREA_EFFECT_CLOUD.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            try {
                aint1[EntityType.ARMOR_STAND.ordinal()] = 30;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            try {
                aint1[EntityType.ARROW.ordinal()] = 10;
            } catch (NoSuchFieldError nosuchfielderror3) {
                ;
            }

            try {
                aint1[EntityType.AXOLOTL.ordinal()] = 106;
            } catch (NoSuchFieldError nosuchfielderror4) {
                ;
            }

            try {
                aint1[EntityType.BAT.ordinal()] = 61;
            } catch (NoSuchFieldError nosuchfielderror5) {
                ;
            }

            try {
                aint1[EntityType.BEE.ordinal()] = 100;
            } catch (NoSuchFieldError nosuchfielderror6) {
                ;
            }

            try {
                aint1[EntityType.BLAZE.ordinal()] = 57;
            } catch (NoSuchFieldError nosuchfielderror7) {
                ;
            }

            try {
                aint1[EntityType.BLOCK_DISPLAY.ordinal()] = 117;
            } catch (NoSuchFieldError nosuchfielderror8) {
                ;
            }

            try {
                aint1[EntityType.BOAT.ordinal()] = 39;
            } catch (NoSuchFieldError nosuchfielderror9) {
                ;
            }

            try {
                aint1[EntityType.CAMEL.ordinal()] = 116;
            } catch (NoSuchFieldError nosuchfielderror10) {
                ;
            }

            try {
                aint1[EntityType.CAT.ordinal()] = 93;
            } catch (NoSuchFieldError nosuchfielderror11) {
                ;
            }

            try {
                aint1[EntityType.CAVE_SPIDER.ordinal()] = 55;
            } catch (NoSuchFieldError nosuchfielderror12) {
                ;
            }

            try {
                aint1[EntityType.CHEST_BOAT.ordinal()] = 112;
            } catch (NoSuchFieldError nosuchfielderror13) {
                ;
            }

            try {
                aint1[EntityType.CHICKEN.ordinal()] = 69;
            } catch (NoSuchFieldError nosuchfielderror14) {
                ;
            }

            try {
                aint1[EntityType.COD.ordinal()] = 87;
            } catch (NoSuchFieldError nosuchfielderror15) {
                ;
            }

            try {
                aint1[EntityType.COW.ordinal()] = 68;
            } catch (NoSuchFieldError nosuchfielderror16) {
                ;
            }

            try {
                aint1[EntityType.CREEPER.ordinal()] = 46;
            } catch (NoSuchFieldError nosuchfielderror17) {
                ;
            }

            try {
                aint1[EntityType.DOLPHIN.ordinal()] = 92;
            } catch (NoSuchFieldError nosuchfielderror18) {
                ;
            }

            try {
                aint1[EntityType.DONKEY.ordinal()] = 31;
            } catch (NoSuchFieldError nosuchfielderror19) {
                ;
            }

            try {
                aint1[EntityType.DRAGON_FIREBALL.ordinal()] = 26;
            } catch (NoSuchFieldError nosuchfielderror20) {
                ;
            }

            try {
                aint1[EntityType.DROPPED_ITEM.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror21) {
                ;
            }

            try {
                aint1[EntityType.DROWNED.ordinal()] = 91;
            } catch (NoSuchFieldError nosuchfielderror22) {
                ;
            }

            try {
                aint1[EntityType.EGG.ordinal()] = 7;
            } catch (NoSuchFieldError nosuchfielderror23) {
                ;
            }

            try {
                aint1[EntityType.ELDER_GUARDIAN.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror24) {
                ;
            }

            try {
                aint1[EntityType.ENDERMAN.ordinal()] = 54;
            } catch (NoSuchFieldError nosuchfielderror25) {
                ;
            }

            try {
                aint1[EntityType.ENDERMITE.ordinal()] = 63;
            } catch (NoSuchFieldError nosuchfielderror26) {
                ;
            }

            try {
                aint1[EntityType.ENDER_CRYSTAL.ordinal()] = 83;
            } catch (NoSuchFieldError nosuchfielderror27) {
                ;
            }

            try {
                aint1[EntityType.ENDER_DRAGON.ordinal()] = 59;
            } catch (NoSuchFieldError nosuchfielderror28) {
                ;
            }

            try {
                aint1[EntityType.ENDER_PEARL.ordinal()] = 14;
            } catch (NoSuchFieldError nosuchfielderror29) {
                ;
            }

            try {
                aint1[EntityType.ENDER_SIGNAL.ordinal()] = 15;
            } catch (NoSuchFieldError nosuchfielderror30) {
                ;
            }

            try {
                aint1[EntityType.EVOKER.ordinal()] = 34;
            } catch (NoSuchFieldError nosuchfielderror31) {
                ;
            }

            try {
                aint1[EntityType.EVOKER_FANGS.ordinal()] = 33;
            } catch (NoSuchFieldError nosuchfielderror32) {
                ;
            }

            try {
                aint1[EntityType.EXPERIENCE_ORB.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror33) {
                ;
            }

            try {
                aint1[EntityType.FALLING_BLOCK.ordinal()] = 21;
            } catch (NoSuchFieldError nosuchfielderror34) {
                ;
            }

            try {
                aint1[EntityType.FIREBALL.ordinal()] = 12;
            } catch (NoSuchFieldError nosuchfielderror35) {
                ;
            }

            try {
                aint1[EntityType.FIREWORK.ordinal()] = 22;
            } catch (NoSuchFieldError nosuchfielderror36) {
                ;
            }

            try {
                aint1[EntityType.FISHING_HOOK.ordinal()] = 122;
            } catch (NoSuchFieldError nosuchfielderror37) {
                ;
            }

            try {
                aint1[EntityType.FOX.ordinal()] = 99;
            } catch (NoSuchFieldError nosuchfielderror38) {
                ;
            }

            try {
                aint1[EntityType.FROG.ordinal()] = 113;
            } catch (NoSuchFieldError nosuchfielderror39) {
                ;
            }

            try {
                aint1[EntityType.GHAST.ordinal()] = 52;
            } catch (NoSuchFieldError nosuchfielderror40) {
                ;
            }

            try {
                aint1[EntityType.GIANT.ordinal()] = 49;
            } catch (NoSuchFieldError nosuchfielderror41) {
                ;
            }

            try {
                aint1[EntityType.GLOW_ITEM_FRAME.ordinal()] = 107;
            } catch (NoSuchFieldError nosuchfielderror42) {
                ;
            }

            try {
                aint1[EntityType.GLOW_SQUID.ordinal()] = 108;
            } catch (NoSuchFieldError nosuchfielderror43) {
                ;
            }

            try {
                aint1[EntityType.GOAT.ordinal()] = 109;
            } catch (NoSuchFieldError nosuchfielderror44) {
                ;
            }

            try {
                aint1[EntityType.GUARDIAN.ordinal()] = 64;
            } catch (NoSuchFieldError nosuchfielderror45) {
                ;
            }

            try {
                aint1[EntityType.HOGLIN.ordinal()] = 101;
            } catch (NoSuchFieldError nosuchfielderror46) {
                ;
            }

            try {
                aint1[EntityType.HORSE.ordinal()] = 76;
            } catch (NoSuchFieldError nosuchfielderror47) {
                ;
            }

            try {
                aint1[EntityType.HUSK.ordinal()] = 23;
            } catch (NoSuchFieldError nosuchfielderror48) {
                ;
            }

            try {
                aint1[EntityType.ILLUSIONER.ordinal()] = 37;
            } catch (NoSuchFieldError nosuchfielderror49) {
                ;
            }

            try {
                aint1[EntityType.INTERACTION.ordinal()] = 118;
            } catch (NoSuchFieldError nosuchfielderror50) {
                ;
            }

            try {
                aint1[EntityType.IRON_GOLEM.ordinal()] = 75;
            } catch (NoSuchFieldError nosuchfielderror51) {
                ;
            }

            try {
                aint1[EntityType.ITEM_DISPLAY.ordinal()] = 119;
            } catch (NoSuchFieldError nosuchfielderror52) {
                ;
            }

            try {
                aint1[EntityType.ITEM_FRAME.ordinal()] = 18;
            } catch (NoSuchFieldError nosuchfielderror53) {
                ;
            }

            try {
                aint1[EntityType.LEASH_HITCH.ordinal()] = 8;
            } catch (NoSuchFieldError nosuchfielderror54) {
                ;
            }

            try {
                aint1[EntityType.LIGHTNING.ordinal()] = 123;
            } catch (NoSuchFieldError nosuchfielderror55) {
                ;
            }

            try {
                aint1[EntityType.LLAMA.ordinal()] = 79;
            } catch (NoSuchFieldError nosuchfielderror56) {
                ;
            }

            try {
                aint1[EntityType.LLAMA_SPIT.ordinal()] = 80;
            } catch (NoSuchFieldError nosuchfielderror57) {
                ;
            }

            try {
                aint1[EntityType.MAGMA_CUBE.ordinal()] = 58;
            } catch (NoSuchFieldError nosuchfielderror58) {
                ;
            }

            try {
                aint1[EntityType.MARKER.ordinal()] = 110;
            } catch (NoSuchFieldError nosuchfielderror59) {
                ;
            }

            try {
                aint1[EntityType.MINECART.ordinal()] = 40;
            } catch (NoSuchFieldError nosuchfielderror60) {
                ;
            }

            try {
                aint1[EntityType.MINECART_CHEST.ordinal()] = 41;
            } catch (NoSuchFieldError nosuchfielderror61) {
                ;
            }

            try {
                aint1[EntityType.MINECART_COMMAND.ordinal()] = 38;
            } catch (NoSuchFieldError nosuchfielderror62) {
                ;
            }

            try {
                aint1[EntityType.MINECART_FURNACE.ordinal()] = 42;
            } catch (NoSuchFieldError nosuchfielderror63) {
                ;
            }

            try {
                aint1[EntityType.MINECART_HOPPER.ordinal()] = 44;
            } catch (NoSuchFieldError nosuchfielderror64) {
                ;
            }

            try {
                aint1[EntityType.MINECART_MOB_SPAWNER.ordinal()] = 45;
            } catch (NoSuchFieldError nosuchfielderror65) {
                ;
            }

            try {
                aint1[EntityType.MINECART_TNT.ordinal()] = 43;
            } catch (NoSuchFieldError nosuchfielderror66) {
                ;
            }

            try {
                aint1[EntityType.MULE.ordinal()] = 32;
            } catch (NoSuchFieldError nosuchfielderror67) {
                ;
            }

            try {
                aint1[EntityType.MUSHROOM_COW.ordinal()] = 72;
            } catch (NoSuchFieldError nosuchfielderror68) {
                ;
            }

            try {
                aint1[EntityType.OCELOT.ordinal()] = 74;
            } catch (NoSuchFieldError nosuchfielderror69) {
                ;
            }

            try {
                aint1[EntityType.PAINTING.ordinal()] = 9;
            } catch (NoSuchFieldError nosuchfielderror70) {
                ;
            }

            try {
                aint1[EntityType.PANDA.ordinal()] = 94;
            } catch (NoSuchFieldError nosuchfielderror71) {
                ;
            }

            try {
                aint1[EntityType.PARROT.ordinal()] = 81;
            } catch (NoSuchFieldError nosuchfielderror72) {
                ;
            }

            try {
                aint1[EntityType.PHANTOM.ordinal()] = 85;
            } catch (NoSuchFieldError nosuchfielderror73) {
                ;
            }

            try {
                aint1[EntityType.PIG.ordinal()] = 66;
            } catch (NoSuchFieldError nosuchfielderror74) {
                ;
            }

            try {
                aint1[EntityType.PIGLIN.ordinal()] = 102;
            } catch (NoSuchFieldError nosuchfielderror75) {
                ;
            }

            try {
                aint1[EntityType.PIGLIN_BRUTE.ordinal()] = 105;
            } catch (NoSuchFieldError nosuchfielderror76) {
                ;
            }

            try {
                aint1[EntityType.PILLAGER.ordinal()] = 95;
            } catch (NoSuchFieldError nosuchfielderror77) {
                ;
            }

            try {
                aint1[EntityType.PLAYER.ordinal()] = 124;
            } catch (NoSuchFieldError nosuchfielderror78) {
                ;
            }

            try {
                aint1[EntityType.POLAR_BEAR.ordinal()] = 78;
            } catch (NoSuchFieldError nosuchfielderror79) {
                ;
            }

            try {
                aint1[EntityType.PRIMED_TNT.ordinal()] = 20;
            } catch (NoSuchFieldError nosuchfielderror80) {
                ;
            }

            try {
                aint1[EntityType.PUFFERFISH.ordinal()] = 89;
            } catch (NoSuchFieldError nosuchfielderror81) {
                ;
            }

            try {
                aint1[EntityType.RABBIT.ordinal()] = 77;
            } catch (NoSuchFieldError nosuchfielderror82) {
                ;
            }

            try {
                aint1[EntityType.RAVAGER.ordinal()] = 96;
            } catch (NoSuchFieldError nosuchfielderror83) {
                ;
            }

            try {
                aint1[EntityType.SALMON.ordinal()] = 88;
            } catch (NoSuchFieldError nosuchfielderror84) {
                ;
            }

            try {
                aint1[EntityType.SHEEP.ordinal()] = 67;
            } catch (NoSuchFieldError nosuchfielderror85) {
                ;
            }

            try {
                aint1[EntityType.SHULKER.ordinal()] = 65;
            } catch (NoSuchFieldError nosuchfielderror86) {
                ;
            }

            try {
                aint1[EntityType.SHULKER_BULLET.ordinal()] = 25;
            } catch (NoSuchFieldError nosuchfielderror87) {
                ;
            }

            try {
                aint1[EntityType.SILVERFISH.ordinal()] = 56;
            } catch (NoSuchFieldError nosuchfielderror88) {
                ;
            }

            try {
                aint1[EntityType.SKELETON.ordinal()] = 47;
            } catch (NoSuchFieldError nosuchfielderror89) {
                ;
            }

            try {
                aint1[EntityType.SKELETON_HORSE.ordinal()] = 28;
            } catch (NoSuchFieldError nosuchfielderror90) {
                ;
            }

            try {
                aint1[EntityType.SLIME.ordinal()] = 51;
            } catch (NoSuchFieldError nosuchfielderror91) {
                ;
            }

            try {
                aint1[EntityType.SMALL_FIREBALL.ordinal()] = 13;
            } catch (NoSuchFieldError nosuchfielderror92) {
                ;
            }

            try {
                aint1[EntityType.SNIFFER.ordinal()] = 120;
            } catch (NoSuchFieldError nosuchfielderror93) {
                ;
            }

            try {
                aint1[EntityType.SNOWBALL.ordinal()] = 11;
            } catch (NoSuchFieldError nosuchfielderror94) {
                ;
            }

            try {
                aint1[EntityType.SNOWMAN.ordinal()] = 73;
            } catch (NoSuchFieldError nosuchfielderror95) {
                ;
            }

            try {
                aint1[EntityType.SPECTRAL_ARROW.ordinal()] = 24;
            } catch (NoSuchFieldError nosuchfielderror96) {
                ;
            }

            try {
                aint1[EntityType.SPIDER.ordinal()] = 48;
            } catch (NoSuchFieldError nosuchfielderror97) {
                ;
            }

            try {
                aint1[EntityType.SPLASH_POTION.ordinal()] = 16;
            } catch (NoSuchFieldError nosuchfielderror98) {
                ;
            }

            try {
                aint1[EntityType.SQUID.ordinal()] = 70;
            } catch (NoSuchFieldError nosuchfielderror99) {
                ;
            }

            try {
                aint1[EntityType.STRAY.ordinal()] = 6;
            } catch (NoSuchFieldError nosuchfielderror100) {
                ;
            }

            try {
                aint1[EntityType.STRIDER.ordinal()] = 103;
            } catch (NoSuchFieldError nosuchfielderror101) {
                ;
            }

            try {
                aint1[EntityType.TADPOLE.ordinal()] = 114;
            } catch (NoSuchFieldError nosuchfielderror102) {
                ;
            }

            try {
                aint1[EntityType.TEXT_DISPLAY.ordinal()] = 121;
            } catch (NoSuchFieldError nosuchfielderror103) {
                ;
            }

            try {
                aint1[EntityType.THROWN_EXP_BOTTLE.ordinal()] = 17;
            } catch (NoSuchFieldError nosuchfielderror104) {
                ;
            }

            try {
                aint1[EntityType.TRADER_LLAMA.ordinal()] = 97;
            } catch (NoSuchFieldError nosuchfielderror105) {
                ;
            }

            try {
                aint1[EntityType.TRIDENT.ordinal()] = 86;
            } catch (NoSuchFieldError nosuchfielderror106) {
                ;
            }

            try {
                aint1[EntityType.TROPICAL_FISH.ordinal()] = 90;
            } catch (NoSuchFieldError nosuchfielderror107) {
                ;
            }

            try {
                aint1[EntityType.TURTLE.ordinal()] = 84;
            } catch (NoSuchFieldError nosuchfielderror108) {
                ;
            }

            try {
                aint1[EntityType.UNKNOWN.ordinal()] = 125;
            } catch (NoSuchFieldError nosuchfielderror109) {
                ;
            }

            try {
                aint1[EntityType.VEX.ordinal()] = 35;
            } catch (NoSuchFieldError nosuchfielderror110) {
                ;
            }

            try {
                aint1[EntityType.VILLAGER.ordinal()] = 82;
            } catch (NoSuchFieldError nosuchfielderror111) {
                ;
            }

            try {
                aint1[EntityType.VINDICATOR.ordinal()] = 36;
            } catch (NoSuchFieldError nosuchfielderror112) {
                ;
            }

            try {
                aint1[EntityType.WANDERING_TRADER.ordinal()] = 98;
            } catch (NoSuchFieldError nosuchfielderror113) {
                ;
            }

            try {
                aint1[EntityType.WARDEN.ordinal()] = 115;
            } catch (NoSuchFieldError nosuchfielderror114) {
                ;
            }

            try {
                aint1[EntityType.WITCH.ordinal()] = 62;
            } catch (NoSuchFieldError nosuchfielderror115) {
                ;
            }

            try {
                aint1[EntityType.WITHER.ordinal()] = 60;
            } catch (NoSuchFieldError nosuchfielderror116) {
                ;
            }

            try {
                aint1[EntityType.WITHER_SKELETON.ordinal()] = 5;
            } catch (NoSuchFieldError nosuchfielderror117) {
                ;
            }

            try {
                aint1[EntityType.WITHER_SKULL.ordinal()] = 19;
            } catch (NoSuchFieldError nosuchfielderror118) {
                ;
            }

            try {
                aint1[EntityType.WOLF.ordinal()] = 71;
            } catch (NoSuchFieldError nosuchfielderror119) {
                ;
            }

            try {
                aint1[EntityType.ZOGLIN.ordinal()] = 104;
            } catch (NoSuchFieldError nosuchfielderror120) {
                ;
            }

            try {
                aint1[EntityType.ZOMBIE.ordinal()] = 50;
            } catch (NoSuchFieldError nosuchfielderror121) {
                ;
            }

            try {
                aint1[EntityType.ZOMBIE_HORSE.ordinal()] = 29;
            } catch (NoSuchFieldError nosuchfielderror122) {
                ;
            }

            try {
                aint1[EntityType.ZOMBIE_VILLAGER.ordinal()] = 27;
            } catch (NoSuchFieldError nosuchfielderror123) {
                ;
            }

            try {
                aint1[EntityType.ZOMBIFIED_PIGLIN.ordinal()] = 53;
            } catch (NoSuchFieldError nosuchfielderror124) {
                ;
            }

            CraftEventFactory.$SWITCH_TABLE$org$bukkit$entity$EntityType = aint1;
            return aint1;
        }
    }

    static int[] $SWITCH_TABLE$org$bukkit$Statistic() {
        int[] aint = CraftEventFactory.$SWITCH_TABLE$org$bukkit$Statistic;

        if (aint != null) {
            return aint;
        } else {
            int[] aint1 = new int[Statistic.values().length];

            try {
                aint1[Statistic.ANIMALS_BRED.ordinal()] = 7;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                aint1[Statistic.ARMOR_CLEANED.ordinal()] = 41;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

            try {
                aint1[Statistic.AVIATE_ONE_CM.ordinal()] = 28;
            } catch (NoSuchFieldError nosuchfielderror2) {
                ;
            }

            try {
                aint1[Statistic.BANNER_CLEANED.ordinal()] = 42;
            } catch (NoSuchFieldError nosuchfielderror3) {
                ;
            }

            try {
                aint1[Statistic.BEACON_INTERACTION.ordinal()] = 44;
            } catch (NoSuchFieldError nosuchfielderror4) {
                ;
            }

            try {
                aint1[Statistic.BELL_RING.ordinal()] = 76;
            } catch (NoSuchFieldError nosuchfielderror5) {
                ;
            }

            try {
                aint1[Statistic.BOAT_ONE_CM.ordinal()] = 23;
            } catch (NoSuchFieldError nosuchfielderror6) {
                ;
            }

            try {
                aint1[Statistic.BREAK_ITEM.ordinal()] = 31;
            } catch (NoSuchFieldError nosuchfielderror7) {
                ;
            }

            try {
                aint1[Statistic.BREWINGSTAND_INTERACTION.ordinal()] = 43;
            } catch (NoSuchFieldError nosuchfielderror8) {
                ;
            }

            try {
                aint1[Statistic.CAKE_SLICES_EATEN.ordinal()] = 38;
            } catch (NoSuchFieldError nosuchfielderror9) {
                ;
            }

            try {
                aint1[Statistic.CAULDRON_FILLED.ordinal()] = 39;
            } catch (NoSuchFieldError nosuchfielderror10) {
                ;
            }

            try {
                aint1[Statistic.CAULDRON_USED.ordinal()] = 40;
            } catch (NoSuchFieldError nosuchfielderror11) {
                ;
            }

            try {
                aint1[Statistic.CHEST_OPENED.ordinal()] = 57;
            } catch (NoSuchFieldError nosuchfielderror12) {
                ;
            }

            try {
                aint1[Statistic.CLEAN_SHULKER_BOX.ordinal()] = 67;
            } catch (NoSuchFieldError nosuchfielderror13) {
                ;
            }

            try {
                aint1[Statistic.CLIMB_ONE_CM.ordinal()] = 19;
            } catch (NoSuchFieldError nosuchfielderror14) {
                ;
            }

            try {
                aint1[Statistic.CRAFTING_TABLE_INTERACTION.ordinal()] = 56;
            } catch (NoSuchFieldError nosuchfielderror15) {
                ;
            }

            try {
                aint1[Statistic.CRAFT_ITEM.ordinal()] = 32;
            } catch (NoSuchFieldError nosuchfielderror16) {
                ;
            }

            try {
                aint1[Statistic.CROUCH_ONE_CM.ordinal()] = 27;
            } catch (NoSuchFieldError nosuchfielderror17) {
                ;
            }

            try {
                aint1[Statistic.DAMAGE_ABSORBED.ordinal()] = 65;
            } catch (NoSuchFieldError nosuchfielderror18) {
                ;
            }

            try {
                aint1[Statistic.DAMAGE_BLOCKED_BY_SHIELD.ordinal()] = 64;
            } catch (NoSuchFieldError nosuchfielderror19) {
                ;
            }

            try {
                aint1[Statistic.DAMAGE_DEALT.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror20) {
                ;
            }

            try {
                aint1[Statistic.DAMAGE_DEALT_ABSORBED.ordinal()] = 62;
            } catch (NoSuchFieldError nosuchfielderror21) {
                ;
            }

            try {
                aint1[Statistic.DAMAGE_DEALT_RESISTED.ordinal()] = 63;
            } catch (NoSuchFieldError nosuchfielderror22) {
                ;
            }

            try {
                aint1[Statistic.DAMAGE_RESISTED.ordinal()] = 66;
            } catch (NoSuchFieldError nosuchfielderror23) {
                ;
            }

            try {
                aint1[Statistic.DAMAGE_TAKEN.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror24) {
                ;
            }

            try {
                aint1[Statistic.DEATHS.ordinal()] = 3;
            } catch (NoSuchFieldError nosuchfielderror25) {
                ;
            }

            try {
                aint1[Statistic.DISPENSER_INSPECTED.ordinal()] = 47;
            } catch (NoSuchFieldError nosuchfielderror26) {
                ;
            }

            try {
                aint1[Statistic.DROP.ordinal()] = 11;
            } catch (NoSuchFieldError nosuchfielderror27) {
                ;
            }

            try {
                aint1[Statistic.DROPPER_INSPECTED.ordinal()] = 45;
            } catch (NoSuchFieldError nosuchfielderror28) {
                ;
            }

            try {
                aint1[Statistic.DROP_COUNT.ordinal()] = 10;
            } catch (NoSuchFieldError nosuchfielderror29) {
                ;
            }

            try {
                aint1[Statistic.ENDERCHEST_OPENED.ordinal()] = 52;
            } catch (NoSuchFieldError nosuchfielderror30) {
                ;
            }

            try {
                aint1[Statistic.ENTITY_KILLED_BY.ordinal()] = 34;
            } catch (NoSuchFieldError nosuchfielderror31) {
                ;
            }

            try {
                aint1[Statistic.FALL_ONE_CM.ordinal()] = 17;
            } catch (NoSuchFieldError nosuchfielderror32) {
                ;
            }

            try {
                aint1[Statistic.FISH_CAUGHT.ordinal()] = 6;
            } catch (NoSuchFieldError nosuchfielderror33) {
                ;
            }

            try {
                aint1[Statistic.FLOWER_POTTED.ordinal()] = 50;
            } catch (NoSuchFieldError nosuchfielderror34) {
                ;
            }

            try {
                aint1[Statistic.FLY_ONE_CM.ordinal()] = 20;
            } catch (NoSuchFieldError nosuchfielderror35) {
                ;
            }

            try {
                aint1[Statistic.FURNACE_INTERACTION.ordinal()] = 55;
            } catch (NoSuchFieldError nosuchfielderror36) {
                ;
            }

            try {
                aint1[Statistic.HOPPER_INSPECTED.ordinal()] = 46;
            } catch (NoSuchFieldError nosuchfielderror37) {
                ;
            }

            try {
                aint1[Statistic.HORSE_ONE_CM.ordinal()] = 25;
            } catch (NoSuchFieldError nosuchfielderror38) {
                ;
            }

            try {
                aint1[Statistic.INTERACT_WITH_ANVIL.ordinal()] = 79;
            } catch (NoSuchFieldError nosuchfielderror39) {
                ;
            }

            try {
                aint1[Statistic.INTERACT_WITH_BLAST_FURNACE.ordinal()] = 69;
            } catch (NoSuchFieldError nosuchfielderror40) {
                ;
            }

            try {
                aint1[Statistic.INTERACT_WITH_CAMPFIRE.ordinal()] = 72;
            } catch (NoSuchFieldError nosuchfielderror41) {
                ;
            }

            try {
                aint1[Statistic.INTERACT_WITH_CARTOGRAPHY_TABLE.ordinal()] = 73;
            } catch (NoSuchFieldError nosuchfielderror42) {
                ;
            }

            try {
                aint1[Statistic.INTERACT_WITH_GRINDSTONE.ordinal()] = 80;
            } catch (NoSuchFieldError nosuchfielderror43) {
                ;
            }

            try {
                aint1[Statistic.INTERACT_WITH_LECTERN.ordinal()] = 71;
            } catch (NoSuchFieldError nosuchfielderror44) {
                ;
            }

            try {
                aint1[Statistic.INTERACT_WITH_LOOM.ordinal()] = 74;
            } catch (NoSuchFieldError nosuchfielderror45) {
                ;
            }

            try {
                aint1[Statistic.INTERACT_WITH_SMITHING_TABLE.ordinal()] = 82;
            } catch (NoSuchFieldError nosuchfielderror46) {
                ;
            }

            try {
                aint1[Statistic.INTERACT_WITH_SMOKER.ordinal()] = 70;
            } catch (NoSuchFieldError nosuchfielderror47) {
                ;
            }

            try {
                aint1[Statistic.INTERACT_WITH_STONECUTTER.ordinal()] = 75;
            } catch (NoSuchFieldError nosuchfielderror48) {
                ;
            }

            try {
                aint1[Statistic.ITEM_ENCHANTED.ordinal()] = 53;
            } catch (NoSuchFieldError nosuchfielderror49) {
                ;
            }

            try {
                aint1[Statistic.JUMP.ordinal()] = 9;
            } catch (NoSuchFieldError nosuchfielderror50) {
                ;
            }

            try {
                aint1[Statistic.KILL_ENTITY.ordinal()] = 33;
            } catch (NoSuchFieldError nosuchfielderror51) {
                ;
            }

            try {
                aint1[Statistic.LEAVE_GAME.ordinal()] = 8;
            } catch (NoSuchFieldError nosuchfielderror52) {
                ;
            }

            try {
                aint1[Statistic.MINECART_ONE_CM.ordinal()] = 22;
            } catch (NoSuchFieldError nosuchfielderror53) {
                ;
            }

            try {
                aint1[Statistic.MINE_BLOCK.ordinal()] = 29;
            } catch (NoSuchFieldError nosuchfielderror54) {
                ;
            }

            try {
                aint1[Statistic.MOB_KILLS.ordinal()] = 4;
            } catch (NoSuchFieldError nosuchfielderror55) {
                ;
            }

            try {
                aint1[Statistic.NOTEBLOCK_PLAYED.ordinal()] = 48;
            } catch (NoSuchFieldError nosuchfielderror56) {
                ;
            }

            try {
                aint1[Statistic.NOTEBLOCK_TUNED.ordinal()] = 49;
            } catch (NoSuchFieldError nosuchfielderror57) {
                ;
            }

            try {
                aint1[Statistic.OPEN_BARREL.ordinal()] = 68;
            } catch (NoSuchFieldError nosuchfielderror58) {
                ;
            }

            try {
                aint1[Statistic.PICKUP.ordinal()] = 12;
            } catch (NoSuchFieldError nosuchfielderror59) {
                ;
            }

            try {
                aint1[Statistic.PIG_ONE_CM.ordinal()] = 24;
            } catch (NoSuchFieldError nosuchfielderror60) {
                ;
            }

            try {
                aint1[Statistic.PLAYER_KILLS.ordinal()] = 5;
            } catch (NoSuchFieldError nosuchfielderror61) {
                ;
            }

            try {
                aint1[Statistic.PLAY_ONE_MINUTE.ordinal()] = 13;
            } catch (NoSuchFieldError nosuchfielderror62) {
                ;
            }

            try {
                aint1[Statistic.RAID_TRIGGER.ordinal()] = 77;
            } catch (NoSuchFieldError nosuchfielderror63) {
                ;
            }

            try {
                aint1[Statistic.RAID_WIN.ordinal()] = 78;
            } catch (NoSuchFieldError nosuchfielderror64) {
                ;
            }

            try {
                aint1[Statistic.RECORD_PLAYED.ordinal()] = 54;
            } catch (NoSuchFieldError nosuchfielderror65) {
                ;
            }

            try {
                aint1[Statistic.SHULKER_BOX_OPENED.ordinal()] = 59;
            } catch (NoSuchFieldError nosuchfielderror66) {
                ;
            }

            try {
                aint1[Statistic.SLEEP_IN_BED.ordinal()] = 58;
            } catch (NoSuchFieldError nosuchfielderror67) {
                ;
            }

            try {
                aint1[Statistic.SNEAK_TIME.ordinal()] = 18;
            } catch (NoSuchFieldError nosuchfielderror68) {
                ;
            }

            try {
                aint1[Statistic.SPRINT_ONE_CM.ordinal()] = 26;
            } catch (NoSuchFieldError nosuchfielderror69) {
                ;
            }

            try {
                aint1[Statistic.STRIDER_ONE_CM.ordinal()] = 83;
            } catch (NoSuchFieldError nosuchfielderror70) {
                ;
            }

            try {
                aint1[Statistic.SWIM_ONE_CM.ordinal()] = 61;
            } catch (NoSuchFieldError nosuchfielderror71) {
                ;
            }

            try {
                aint1[Statistic.TALKED_TO_VILLAGER.ordinal()] = 36;
            } catch (NoSuchFieldError nosuchfielderror72) {
                ;
            }

            try {
                aint1[Statistic.TARGET_HIT.ordinal()] = 81;
            } catch (NoSuchFieldError nosuchfielderror73) {
                ;
            }

            try {
                aint1[Statistic.TIME_SINCE_DEATH.ordinal()] = 35;
            } catch (NoSuchFieldError nosuchfielderror74) {
                ;
            }

            try {
                aint1[Statistic.TIME_SINCE_REST.ordinal()] = 60;
            } catch (NoSuchFieldError nosuchfielderror75) {
                ;
            }

            try {
                aint1[Statistic.TOTAL_WORLD_TIME.ordinal()] = 14;
            } catch (NoSuchFieldError nosuchfielderror76) {
                ;
            }

            try {
                aint1[Statistic.TRADED_WITH_VILLAGER.ordinal()] = 37;
            } catch (NoSuchFieldError nosuchfielderror77) {
                ;
            }

            try {
                aint1[Statistic.TRAPPED_CHEST_TRIGGERED.ordinal()] = 51;
            } catch (NoSuchFieldError nosuchfielderror78) {
                ;
            }

            try {
                aint1[Statistic.USE_ITEM.ordinal()] = 30;
            } catch (NoSuchFieldError nosuchfielderror79) {
                ;
            }

            try {
                aint1[Statistic.WALK_ONE_CM.ordinal()] = 15;
            } catch (NoSuchFieldError nosuchfielderror80) {
                ;
            }

            try {
                aint1[Statistic.WALK_ON_WATER_ONE_CM.ordinal()] = 16;
            } catch (NoSuchFieldError nosuchfielderror81) {
                ;
            }

            try {
                aint1[Statistic.WALK_UNDER_WATER_ONE_CM.ordinal()] = 21;
            } catch (NoSuchFieldError nosuchfielderror82) {
                ;
            }

            CraftEventFactory.$SWITCH_TABLE$org$bukkit$Statistic = aint1;
            return aint1;
        }
    }
}
