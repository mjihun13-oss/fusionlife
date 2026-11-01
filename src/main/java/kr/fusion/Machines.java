package kr.fusion;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** 설치형 기계: 조리기구 7종 + 스프링클러 + 출하상자. 위치는 청크 PDC에 저장되어 재시작 후에도 유지된다. */
public final class Machines implements Listener {
    record MType(String id, String name, Material base, String desc) {}
    static final Map<String, MType> TYPES = new LinkedHashMap<>();
    static final int[] INPUT = {10, 11, 12, 13, 14};
    static final int BUTTON = 16;
    static final String[] COOK = {"cutting_board", "fry_pan", "pot", "oven", "mixer", "mill", "press", "ferment"};

    private static void m(String id, String name, Material base, String desc) {
        TYPES.put(id, new MType(id, name, base, desc));
        Items.reg("m_" + id, name, base, "machine", 0, NamedTextColor.AQUA, desc, "설치 후 우클릭으로 사용합니다.");
    }

    public static void registerItems() {
        m("cutting_board", "도마", Material.CARTOGRAPHY_TABLE, "썰기 · 면 뽑기 · 회 뜨기");
        m("fry_pan", "프라이팬", Material.SMOKER, "볶음 · 부침 요리");
        m("pot", "냄비", Material.CAULDRON, "밥 · 수프 · 찌개 · 라면");
        m("oven", "오븐", Material.BLAST_FURNACE, "빵 · 피자 · 케이크 · 구이");
        m("mixer", "믹서", Material.GRINDSTONE, "스무디 · 갈기");
        m("mill", "제분기", Material.STONECUTTER, "밀 · 벼 · 옥수수 가공");
        m("press", "압착기", Material.SMITHING_TABLE, "기름 · 주스 짜기");
        m("ferment", "발효통", Material.BARREL, "김치 · 와인 · 막걸리 발효");
        m("sprinkler", "스프링클러", Material.LIGHTNING_ROD, "반경 4칸 작물 성장 촉진 (자동)");
        m("seedshop", "씨앗 상점", Material.LECTERN, "다이아몬드로 씨앗 구매");
    }

    // ---------- GUI 홀더 ----------
    static final class MachineGui implements InventoryHolder {
        final MType type; Inventory inv;
        MachineGui(MType t) { type = t; }
        @Override public Inventory getInventory() { return inv; }
    }

    private static ItemStack filler() {
        ItemStack it = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta m = it.getItemMeta(); m.displayName(Component.text(" ")); it.setItemMeta(m);
        return it;
    }

    static void openCook(Player p, MType t) {
        MachineGui h = new MachineGui(t);
        Inventory inv = Bukkit.createInventory(h, 27, Component.text(t.name(), NamedTextColor.DARK_GRAY));
        h.inv = inv;
        ItemStack f = filler();
        for (int i = 0; i < 27; i++) inv.setItem(i, f);
        for (int s : INPUT) inv.setItem(s, null);
        ItemStack b = new ItemStack(Material.LIME_DYE);
        ItemMeta bm = b.getItemMeta();
        bm.displayName(Component.text("▶ 조리 시작", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        bm.lore(List.of(Component.text("재료를 넣고 클릭하세요.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("재료가 충분하면 한 번에 여러 개 만들어요.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
        b.setItemMeta(bm);
        inv.setItem(BUTTON, b);
        p.openInventory(inv);
    }

    static boolean isInput(int slot) { for (int s : INPUT) if (s == slot) return true; return false; }

    static void give(Player p, ItemStack it) {
        if (it == null || it.getType().isAir() || it.getAmount() <= 0) return;
        Map<Integer, ItemStack> left = p.getInventory().addItem(it);
        for (ItemStack l : left.values()) p.getWorld().dropItemNaturally(p.getLocation(), l);
    }

    // ---------- 설치 / 파괴 / 보호 ----------
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        String id = Items.idOf(e.getItemInHand());
        if (id == null || !id.startsWith("m_")) return;
        if (!Store.isOverworld(e.getBlockPlaced().getWorld())) {
            e.setCancelled(true);
            FusionLife.msg(e.getPlayer(), "기계는 오버월드에서만 설치할 수 있어요.");
            return;
        }
        Store.set(e.getBlockPlaced(), "mach:" + id.substring(2));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        String v = Store.get(e.getBlock());
        if (v == null || !v.startsWith("mach:")) return;
        MType t = TYPES.get(v.substring(5));
        Store.remove(e.getBlock());
        e.setDropItems(false);
        if (t != null && e.getPlayer().getGameMode() != GameMode.CREATIVE)
            e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation().add(0.5, 0.5, 0.5), Items.create("m_" + t.id()));
    }

    private static void guard(List<Block> blocks, boolean drop) {
        Iterator<Block> it = blocks.iterator();
        while (it.hasNext()) {
            Block b = it.next();
            String v = Store.get(b);
            if (v != null && v.startsWith("mach:")) it.remove(); // 폭발로부터 기계 보호
        }
    }
    @EventHandler(ignoreCancelled = true) public void onExplode(EntityExplodeEvent e) { guard(e.blockList(), false); }
    @EventHandler(ignoreCancelled = true) public void onBlockExplode(BlockExplodeEvent e) { guard(e.blockList(), false); }

    private static boolean tracked(Block b) { String v = Store.get(b); return v != null && v.startsWith("mach:"); }
    @EventHandler(ignoreCancelled = true)
    public void onPistonExt(BlockPistonExtendEvent e) { for (Block b : e.getBlocks()) if (tracked(b)) { e.setCancelled(true); return; } }
    @EventHandler(ignoreCancelled = true)
    public void onPistonRet(BlockPistonRetractEvent e) { for (Block b : e.getBlocks()) if (tracked(b)) { e.setCancelled(true); return; } }

    /** 깔때기/호퍼가 기계 블록(훈연기·용광로·통)에 아이템을 넣거나 빼는 것 차단 */
    @EventHandler(ignoreCancelled = true)
    public void onHopper(InventoryMoveItemEvent e) {
        Location a = e.getDestination().getLocation(), b = e.getSource().getLocation();
        if (a != null && tracked(a.getBlock())) { e.setCancelled(true); return; }
        if (b != null && tracked(b.getBlock())) e.setCancelled(true);
    }

    // ---------- 사용 ----------
    @EventHandler(priority = EventPriority.HIGH)
    public void onUse(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getHand() != EquipmentSlot.HAND) return;
        Block b = e.getClickedBlock();
        if (b == null) return;
        String v = Store.get(b);
        if (v == null || !v.startsWith("mach:")) return;
        if (e.useInteractedBlock() == org.bukkit.event.Event.Result.DENY) return;
        Player p = e.getPlayer();
        if (p.isSneaking() && e.getItem() != null && e.getItem().getType().isBlock()) return; // 웅크리고 블록을 들면 설치 우선
        MType t = TYPES.get(v.substring(5));
        e.setCancelled(true);
        if (t == null) return;
        switch (t.id()) {
            case "sprinkler" -> FusionLife.msg(p, "스프링클러가 주변 작물을 자동으로 키워줍니다.");
            case "seedshop" -> SeedShop.open(p);
            default -> openCook(p, t);
        }
    }

    // ---------- GUI 클릭 ----------
    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent e) {
        Inventory top = e.getView().getTopInventory();
        if (!(top.getHolder() instanceof MachineGui g)) return;
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (e.getClick() == ClickType.DOUBLE_CLICK) { e.setCancelled(true); return; }
        int raw = e.getRawSlot();
        if (raw >= 0 && raw < 27) {
            if (raw == BUTTON) { e.setCancelled(true); cook(p, g, top); return; }
            if (!isInput(raw)) e.setCancelled(true);
        } else if (e.isShiftClick()) {
            // 플레이어 인벤토리에서 shift-클릭 → 입력 슬롯에만 들어가도록 직접 처리
            e.setCancelled(true);
            ItemStack cur = e.getCurrentItem();
            if (cur == null || cur.getType().isAir()) return;
            for (int s : INPUT) {
                ItemStack in = top.getItem(s);
                if (in == null || in.getType().isAir()) { top.setItem(s, cur.clone()); e.setCurrentItem(null); return; }
                if (in.isSimilar(cur) && in.getAmount() < in.getMaxStackSize()) {
                    int mv = Math.min(cur.getAmount(), in.getMaxStackSize() - in.getAmount());
                    in.setAmount(in.getAmount() + mv); cur.setAmount(cur.getAmount() - mv);
                    if (cur.getAmount() <= 0) { e.setCurrentItem(null); return; }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getView().getTopInventory().getHolder() instanceof MachineGui)) return;
        for (int raw : e.getRawSlots()) if (raw < 27 && !isInput(raw)) { e.setCancelled(true); return; }
    }

    private void cook(Player p, MachineGui g, Inventory inv) {
        Map<String, Integer> have = new HashMap<>();
        ItemStack[] slots = new ItemStack[INPUT.length];
        for (int i = 0; i < INPUT.length; i++) {
            ItemStack s = inv.getItem(INPUT[i]);
            slots[i] = s;
            if (s == null || s.getType().isAir()) continue;
            String k = Items.ingKey(s);
            if (k == null) { FusionLife.msg(p, "이름이 바뀐 아이템은 재료로 쓸 수 없어요."); return; }
            have.merge(k, s.getAmount(), Integer::sum);
        }
        if (have.isEmpty()) { FusionLife.msg(p, "재료를 넣어주세요."); return; }
        Cooking.Recipe rc = Cooking.find(g.type.id(), have);
        if (rc == null) { p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f); FusionLife.msg(p, "맞는 레시피가 없어요. 요리책을 확인해 보세요."); return; }
        int times = Math.min(Cooking.times(rc, have), 16);
        Cooking.consume(rc, times, slots);
        for (int i = 0; i < INPUT.length; i++) {
            if (slots[i] == null) continue;
            inv.setItem(INPUT[i], slots[i].getAmount() <= 0 ? null : slots[i]);
        }
        int n = rc.outN() * times;
        Items.Def od = Items.def(rc.out());
        if (od != null && od.cat().equals("food") && ThreadLocalRandom.current().nextDouble() < Math.min(0.4, Store.level(p, "cooking") * 0.02)) {
            n += rc.outN() * times; // 숙련도 보너스: 대성공!
            FusionLife.msg(p, "대성공! 요리가 두 배로 나왔어요.");
        }
        give(p, Items.create(rc.out(), n));
        p.playSound(p.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1f, 1.3f);
        Dex.record(p, 'd', rc.out(), 0);
        FusionLife.gain(p, "cooking", 3 * times);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;
        Inventory inv = e.getInventory();
        if (inv.getHolder() instanceof MachineGui) {
            for (int s : INPUT) { ItemStack it = inv.getItem(s); if (it != null) { give(p, it); inv.setItem(s, null); } }
        }
    }

    // ---------- 스프링클러 ----------
    public static void tickSprinklers() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (Block s : Store.findLoaded("mach:sprinkler")) {
            if (s.getType() != Material.LIGHTNING_ROD) { Store.remove(s); continue; }
            Location c = s.getLocation().add(0.5, 1.1, 0.5);
            s.getWorld().spawnParticle(Particle.FALLING_WATER, c, 12, 1.5, 0.3, 1.5);
            for (Block crop : Store.cropsAround(s, 4)) {
                if (Farming.cropAt(crop) == null) continue;
                if (r.nextDouble() < 0.35) Farming.grow(crop, 1);
            }
        }
    }
}
