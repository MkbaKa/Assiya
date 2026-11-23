package scripts.feature

import github.mkbaka.assiya.platform.bukkit.api.AssiyaAPI
import github.mkbaka.assiya.platform.bukkit.api.session.AssiyaSession
import github.mkbaka.assiya.platform.bukkit.internal.equipment.EquipmentPipeline
import github.mkbaka.assiya.platform.bukkit.internal.equipment.slot.BukkitEquipmentSlot
import github.mkbaka.assiya.platform.bukkit.internal.equipment.slot.WearableSlotResolver
import github.mkbaka.assiya.platform.bukkit.internal.script.tools.ScriptListener
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

import static github.mkbaka.assiya.platform.bukkit.internal.equipment.slot.BukkitEquipmentSlot.MAIN_HAND
import static github.mkbaka.assiya.platform.bukkit.internal.equipment.slot.BukkitEquipmentSlot.OFF_HAND
import static github.mkbaka.assiya.platform.bukkit.internal.util.ItemUtilsKt.isAir
import static github.mkbaka.assiya.platform.bukkit.internal.util.SchedulerUtils.callSyncLater

@CompileStatic
class EquipmentListener {

    // 右键会打开 GUI 的可交互方块
    // 玩家右键这些方块不会触发穿戴
    private static final Set<Material> GUI_BLOCKS = createGuiBlocks()

    private static Set<Material> createGuiBlocks() {
        Set<Material> blocks = new HashSet<>()
        List<String> names = [
                "CHEST", "TRAPPED_CHEST", "ENDER_CHEST", "FURNACE", "BLAST_FURNACE", "SMOKER",
                "BARREL", "HOPPER", "DISPENSER", "DROPPER", "BREWING_STAND", "BEACON",
                "WORKBENCH", "CRAFTING_TABLE", "ENCHANTMENT_TABLE", "ENCHANTING_TABLE",
                "ANVIL", "CHIPPED_ANVIL", "DAMAGED_ANVIL", "GRINDSTONE", "LOOM",
                "CARTOGRAPHY_TABLE", "SMITHING_TABLE", "STONECUTTER", "LECTERN", "BELL",
                "BEEHIVE", "BEE_NEST", "JUKEBOX", "NOTE_BLOCK"
        ]
        for (String name : names) {
            Material material = Material.matchMaterial(name)
            if (material != null) {
                blocks.add(material)
            }
        }
        for (Material material : Material.values()) {
            if (material.name().endsWith("SHULKER_BOX")) {
                blocks.add(material)
            }
        }
        return blocks
    }

    static void onEnable() {
        ScriptListener.create(PlayerItemHeldEvent)
                .setEventPriority(EventPriority.MONITOR)
                .setIgnoreCancelled(true)
                .setHandler { PlayerItemHeldEvent event ->
                    ItemStack previous = event.player.inventory.getItem(event.previousSlot)
                    ItemStack next = event.player.inventory.getItem(event.newSlot)

                    if (!isAir(previous) || !isAir(next)) {
                        EquipmentPipeline.INSTANCE.refresh(event.player, MAIN_HAND)
                    }
                }
                .register()

        ScriptListener.create(InventoryCloseEvent)
                .setEventPriority(EventPriority.MONITOR)
                .setIgnoreCancelled(true)
                .setHandler { InventoryCloseEvent event ->
                    if (!(event.player instanceof Player)) return
                    Player player = (Player) event.player

                    callSyncLater(1L, {
                        // 玩家离线也会触发一次 InventoryCloseEvent
                        if (!player.isOnline()) return

                        EquipmentPipeline.INSTANCE.refresh(player, BukkitEquipmentSlot.values())
                    })
                }
                .register()

        ScriptListener.create(PlayerInteractEvent)
                .setEventPriority(EventPriority.MONITOR)
                .setIgnoreCancelled(false)
                .setHandler { PlayerInteractEvent event ->
                    if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return

                    if (event.useItemInHand() == Event.Result.DENY) return

                    Player player = event.player

                    // 右键会打开 GUI 的可交互方块(箱子/熔炉/工作台等)不会触发穿戴
                    // 潜行时方块交互被抑制不会打开 GUI 因此只在非潜行时按黑名单过滤
                    if (event.action == Action.RIGHT_CLICK_BLOCK
                            && !player.isSneaking()
                            && event.useInteractedBlock() != Event.Result.DENY
                            && event.clickedBlock != null
                            && GUI_BLOCKS.contains(event.clickedBlock.type)) {
                        return
                    }

                    ItemStack item = event.item
                    if (isAir(item)) return

                    // 尝试获取穿戴后的目标槽位
                    BukkitEquipmentSlot targetSlot = WearableSlotResolver.resolveForInteraction(item, player)
                    if (targetSlot == null) return

                    BukkitEquipmentSlot sourceHand = event.hand == EquipmentSlot.HAND ? MAIN_HAND : OFF_HAND

                    callSyncLater(1L, {
                        if (!player.isOnline()) return
                        EquipmentPipeline.INSTANCE.refresh(player, sourceHand, targetSlot)
                    })
                }
                .register()

        ScriptListener.create(PlayerSwapHandItemsEvent)
                .setEventPriority(EventPriority.MONITOR)
                .setIgnoreCancelled(true)
                .setHandler { PlayerSwapHandItemsEvent event ->
                    EquipmentPipeline.INSTANCE.refresh(event.player, MAIN_HAND, OFF_HAND)
                }
                .register()

        ScriptListener.create(PlayerDropItemEvent)
                .setEventPriority(EventPriority.MONITOR)
                .setIgnoreCancelled(true)
                .setHandler { PlayerDropItemEvent event ->
                    // 丢弃后手上物品为空才刷新
                    // 避免可堆叠物品的重复刷新开销
                    if (isAir(event.player.inventory.itemInMainHand)) {
                        EquipmentPipeline.INSTANCE.refresh(event.player, MAIN_HAND)
                    }
                }
                .register()

        ScriptListener.create(EntityPickupItemEvent)
                .setEventPriority(EventPriority.MONITOR)
                .setIgnoreCancelled(true)
                .setHandler { EntityPickupItemEvent event ->
                    if (!(event.entity instanceof Player)) return
                    Player player = (Player) event.entity
                    // 若拾取物品时手上不为空则不刷新
                    if (!isAir(player.inventory.itemInMainHand)) return

                    AssiyaSession session = AssiyaAPI.getSession(player)
                    if (session == null || !session.getHasAttribute()) return

                    // 将掉落物捡到手上
                    callSyncLater(1L, {
                        if (player.isOnline() && !isAir(player.inventory.itemInMainHand)) {
                            EquipmentPipeline.INSTANCE.refresh(player, MAIN_HAND)
                        }
                    })
                }
                .register()

        ScriptListener.create(PlayerItemBreakEvent)
                .setEventPriority(EventPriority.MONITOR)
                .setHandler { PlayerItemBreakEvent event ->
                    // 没法准确得知损坏的物品所在的槽位
                    // 因为可能是装备 也可能是盾牌或手上的工具损坏
                    // 干脆全部重读
                    EquipmentPipeline.INSTANCE.refresh(event.player, BukkitEquipmentSlot.values())
                }
                .register()

        ScriptListener.create(PlayerItemConsumeEvent)
                .setEventPriority(EventPriority.MONITOR)
                .setIgnoreCancelled(true)
                .setHandler { PlayerItemConsumeEvent event ->
                    Player player = event.player

                    // 吃完手上的物品
                    callSyncLater(1L, {
                        if (player.isOnline()) {
                            EquipmentPipeline.INSTANCE.refresh(player, MAIN_HAND, OFF_HAND)
                        }
                    })
                }
                .register()

        ScriptListener.create(PlayerDeathEvent)
                .setEventPriority(EventPriority.MONITOR)
                .setHandler { PlayerDeathEvent event ->
                    Player player = event.entity
                    if (event.getKeepInventory()) return

                    // 死亡掉落的装备不会触发 PlayerDropItemEvent
                    callSyncLater(1L, {
                        if (!player.isOnline()) return
                        EquipmentPipeline.INSTANCE.refresh(player, BukkitEquipmentSlot.values())
                    })
                }
                .register()

        registerDispenserArmor()
    }

    // BlockDispenseArmorEvent 于 1.13 引入
    @CompileDynamic
    private static void registerDispenserArmor() {
        Class dispenseArmor
        try {
            dispenseArmor = Class.forName("org.bukkit.event.block.BlockDispenseArmorEvent")
        } catch (ClassNotFoundException ignored) {
            return
        }

        ScriptListener.create(dispenseArmor)
                .setEventPriority(EventPriority.MONITOR)
                .setIgnoreCancelled(true)
                .setHandler { event ->
                    if (!(event.targetEntity instanceof Player)) return
                    Player player = (Player) event.targetEntity
                    ItemStack item = event.item
                    if (isAir(item)) return
                    BukkitEquipmentSlot slot = WearableSlotResolver.resolve(item)
                    if (slot == null) return
                    // 事件派发前原版已筛选可被装备的目标, 这里只解析槽位.
                    callSyncLater(1L, {
                        if (!player.isOnline()) return
                        EquipmentPipeline.INSTANCE.refresh(player, slot)
                    })
                }
                .register()
    }

}
