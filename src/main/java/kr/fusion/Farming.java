package kr.fusion;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** 농사: 추가 작물 10종, 씨앗, 비료, 농부의 괭이. 바닐라 작물 블록 위에 올려 서버 단독(리소스팩 없이)으로 동작한다. */
public final class Farming implements Listener {
    record Crop(String id, String name, String seedName, Material icon, Material block, int sell) {}

    static final Map<String, Crop> CROPS = new LinkedHashMap<>();
    static final List<String> WILD_SEEDS = new ArrayList<>();

    private static void crop(String id, String name, Material icon, Material block, int sell) {
        Crop c = new Crop(id, name, name + " 씨앗", icon, block, sell);
        CROPS.put(id, c);
        WILD_SEEDS.add(id);
        Items.reg("seed_" + id, c.seedName(), Material.MELON_SEEDS, "seed", 0, NamedTextColor.GREEN, "경작지에 우클릭해서 심으세요.");
        Items.reg(id, name, icon, "crop", sell, NamedTextColor.GOLD, "신선한 농작물. 조리기구로 요리할 수 있어요.");
    }

    public static void registerItems() {
        crop("tomato", "토마토", Material.APPLE, Material.WHEAT, 6);
        crop("corn", "옥수수", Material.GOLDEN_CARROT, Material.WHEAT, 6);
        crop("rice", "벼", Material.DRIED_KELP, Material.WHEAT, 4);
        crop("lettuce", "상추", Material.BEETROOT, Material.BEETROOTS, 4);
        crop("pepper", "고추", Material.POISONOUS_POTATO, Material.BEETROOTS, 7);
        crop("onion", "양파", Material.MELON_SLICE, Material.CARROTS, 5);
        crop("garlic", "마늘", Material.BREAD, Material.CARROTS, 7);
        crop("strawberry", "딸기", Material.SWEET_BERRIES, Material.POTATOES, 9);
        crop("cabbage", "양배추", Material.BAKED_POTATO, Material.POTATOES, 5);
        crop("grape", "포도", Material.GLOW_BERRIES, Material.POTATOES, 10);
        Items.reg("fertilizer", "비료", Material.GLOWSTONE_DUST, "tool", 0, NamedTextColor.YELLOW, "작물에 우클릭: 성장 2단계 촉진");
        Items.reg("hoe_farmer", "농부의 괭이", Material.GOLDEN_HOE, "tool", 0, NamedTextColor.GOLD,
                "다 자란 작물을 부수면 3x3 범위를 수확하고", "자동으로 다시 심어줍니다.");
    }

    // ---------- 유틸 ----------
    static Crop cropAt(Block b) {
        String v = Store.get(b);
        if (v == null || !v.startsWith("crop:")) return null;
        Crop c = CROPS.get(v.substring(5));
        if (c == null || b.getType() != c.block()) { Store.remove(b); return null; }
        return c;
    }

    static boolean mature(Block b) {
        return b.getBlockData() instanceof Ageable a && a.getAge() >= a.getMaximumAge();
    }

    /** 성장 단계 증가 (최대치 클램프). 증가했으면 true */
    public static boolean grow(Block b, int steps) {
        if (!(b.getBlockData() instanceof Ageable a) || a.getAge() >= a.getMaximumAge()) return false;
        a.setAge(Math.min(a.getMaximumAge(), a.getAge() + steps));
        b.setBlockData(a, true);
        return true;
    }

    private static void drop(Location loc, ItemStack it) { loc.getWorld().dropItemNaturally(loc, it); }

    /** 수확: 아이템을 드롭. replant=true면 0단계로 되돌림(태그 유지), 아니면 공기로 */
    static void harvest(Block b, Crop c, Player p, boolean replant) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        Location loc = b.getLocation().add(0.5, 0.3, 0.5);
        boolean ripe = mature(b);
        int lv = p == null ? 1 : Store.level(p, "farming");
        if (ripe) {
            int n = 1 + r.nextInt(2);
            if (r.nextDouble() < Math.min(0.5, lv * 0.04)) n++;
            drop(loc, Items.create(c.id(), n));
            if (r.nextDouble() < 0.6) drop(loc, Items.create("seed_" + c.id(), 1));
            if (p != null) { Dex.record(p, 'c', c.id(), 0); FusionLife.gain(p, "farming", 4); }
        } else {
            drop(loc, Items.create("seed_" + c.id(), 1));
        }
        if (replant && ripe) {
            BlockData d = b.getBlockData();
            if (d instanceof Ageable a) { a.setAge(0); b.setBlockData(a, false); }
        } else {
            b.setType(Material.AIR, false);
            Store.remove(b);
        }
    }

    // ---------- 씨앗 심기 ----------
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlant(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getHand() != EquipmentSlot.HAND) return;
        ItemStack it = e.getItem();
        String id = Items.idOf(it);
        if (id == null) return;
        Player p = e.getPlayer();
        Block clicked = e.getClickedBlock();
        if (clicked == null) return;

        if (id.startsWith("seed_")) {
            e.setCancelled(true); // 바닐라 수박/호박 줄기 심기 방지
            if (!Store.isOverworld(clicked.getWorld())) { FusionLife.msg(p, "오버월드에서만 심을 수 있어요."); return; }
            Crop c = CROPS.get(id.substring(5));
            Block above = clicked.getRelative(BlockFace.UP);
            if (c == null || clicked.getType() != Material.FARMLAND || e.getBlockFace() != BlockFace.UP || !above.getType().isAir()) return;
            above.setType(c.block(), false);
            if (above.getBlockData() instanceof Ageable a) { a.setAge(0); above.setBlockData(a, false); }
            Store.set(above, "crop:" + c.id());
            clicked.getWorld().playSound(above.getLocation(), Sound.ITEM_CROP_PLANT, 1f, 1f);
            if (p.getGameMode() != GameMode.CREATIVE) it.setAmount(it.getAmount() - 1);
            return;
        }

        if (id.equals("fertilizer")) {
            e.setCancelled(true);
            if (!(clicked.getBlockData() instanceof Ageable)) return;
            if (grow(clicked, 2)) {
                clicked.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, clicked.getLocation().add(0.5, 0.5, 0.5), 8, 0.3, 0.3, 0.3);
                if (p.getGameMode() != GameMode.CREATIVE) it.setAmount(it.getAmount() - 1);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent e) {
        String id = Items.idOf(e.getItem());
        if (id != null) e.setCancelled(true); // 발사기가 커스텀 아이템을 바닐라처럼 쓰는 것 방지
    }

    // ---------- 수확 / 드롭 ----------
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        if (!Store.isOverworld(b.getWorld())) return;
        Player p = e.getPlayer();

        Crop c = cropAt(b);
        if (c != null) {
            e.setDropItems(false);
            e.setExpToDrop(0);
            ItemStack hand = p.getInventory().getItemInMainHand();
            if ("hoe_farmer".equals(Items.idOf(hand)) && mature(b)) {
                e.setCancelled(true); // 블록은 직접 처리 (자동 재식)
                harvest(b, c, p, true);
                int used = 1;
                for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    Block n = b.getRelative(dx, 0, dz);
                    Crop nc = cropAt(n);
                    if (nc != null && mature(n)) { harvest(n, nc, p, true); }
                }
                damage(p, hand, used);
            } else {
                harvest(b, c, p, false);
            }
            return;
        }

        // 풀 베기 → 야생 씨앗 (오버월드 전역, 명령어 불필요)
        Material t = b.getType();
        if ((t == Material.SHORT_GRASS || t == Material.TALL_GRASS || t == Material.FERN) && ThreadLocalRandom.current().nextDouble() < 0.04) {
            String id = WILD_SEEDS.get(ThreadLocalRandom.current().nextInt(WILD_SEEDS.size()));
            drop(b.getLocation().add(0.5, 0.3, 0.5), Items.create("seed_" + id, 1));
        }
    }

    private static void damage(Player p, ItemStack hand, int amt) {
        if (p.getGameMode() == GameMode.CREATIVE) return;
        ItemMeta m = hand.getItemMeta();
        if (m instanceof Damageable d) {
            d.setDamage(d.getDamage() + amt);
            hand.setItemMeta(d);
            if (d.getDamage() >= hand.getType().getMaxDurability()) {
                p.getInventory().setItemInMainHand(null);
                p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
            }
        }
    }

    /** 열매/씨앗 등 블록으로 설치 가능한 재료형 커스텀 아이템은 설치 금지 (기계 아이템만 설치 허용) */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onNoPlace(BlockPlaceEvent e) {
        String id = Items.idOf(e.getItemInHand());
        if (id == null) return;
        Items.Def d = Items.def(id);
        if (d != null && !d.cat().equals("machine")) e.setCancelled(true);
    }

    /** 바닐라로 같은 자리에 다른 작물/블록을 놓으면 오래된 태그 제거 */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlaceCleanup(BlockPlaceEvent e) {
        String v = Store.get(e.getBlockPlaced());
        if (v != null && v.startsWith("crop:")) Store.remove(e.getBlockPlaced());
    }
}
