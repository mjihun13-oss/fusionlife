package kr.fusion;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/** 도감: 물고기(f) / 작물(c) / 요리·가공품(d). 플레이어 PDC에 기록되어 서버 재시작 후에도 유지. */
public final class Dex implements Listener {
    static final class Holder implements InventoryHolder {
        final char cat; Inventory inv;
        Holder(char c) { cat = c; }
        @Override public Inventory getInventory() { return inv; }
    }
    static final int[] TAB_SLOT = {47, 49, 51};
    static final char[] TABS = {'f', 'c', 'd'};

    private static NamespacedKey key() { return new NamespacedKey(FusionLife.inst, "dex"); }

    /** "f:carp" -> {최대cm, 횟수} */
    private static Map<String, int[]> load(Player p) {
        Map<String, int[]> m = new LinkedHashMap<>();
        String raw = p.getPersistentDataContainer().get(key(), PersistentDataType.STRING);
        if (raw == null || raw.isEmpty()) return m;
        for (String e : raw.split(";")) {
            String[] a = e.split("=");
            if (a.length != 3) continue;
            try { m.put(a[0], new int[]{Integer.parseInt(a[1]), Integer.parseInt(a[2])}); } catch (NumberFormatException ignored) {}
        }
        return m;
    }

    private static void save(Player p, Map<String, int[]> m) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, int[]> e : m.entrySet()) sb.append(e.getKey()).append('=').append(e.getValue()[0]).append('=').append(e.getValue()[1]).append(';');
        p.getPersistentDataContainer().set(key(), PersistentDataType.STRING, sb.toString());
    }

    static List<String> ids(char cat) {
        List<String> out = new ArrayList<>();
        switch (cat) {
            case 'f' -> { for (Fishing.Fish f : Fishing.FISH) out.add(f.id()); }
            case 'c' -> out.addAll(Farming.CROPS.keySet());
            default -> { for (Cooking.Recipe r : Cooking.RECIPES) if (!out.contains(r.out())) out.add(r.out()); }
        }
        return out;
    }

    static String itemId(char cat, String id) { return cat == 'f' ? "fish_" + id : id; }

    static int found(Map<String, int[]> m, char cat) {
        int n = 0;
        for (String id : ids(cat)) if (m.containsKey(cat + ":" + id)) n++;
        return n;
    }

    /** 기록. cm은 물고기만 의미 있음(최대치 갱신). 새로 발견하면 알림. */
    public static void record(Player p, char cat, String id, int cm) {
        try {
            Map<String, int[]> m = load(p);
            String k = cat + ":" + id;
            int[] v = m.get(k);
            boolean isNew = v == null;
            if (isNew) { v = new int[]{0, 0}; m.put(k, v); }
            v[1]++;
            boolean record = cm > v[0];
            if (record) v[0] = cm;
            save(p, m);
            Items.Def d = Items.def(itemId(cat, id));
            String name = d == null ? id : d.name();
            if (isNew) {
                p.sendMessage(Component.text("📖 도감 등록: ", NamedTextColor.LIGHT_PURPLE).append(Component.text(name, NamedTextColor.WHITE))
                        .append(Component.text(" (" + found(m, cat) + "/" + ids(cat).size() + ")", NamedTextColor.GRAY)));
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.6f);
                if (found(m, cat) == ids(cat).size())
                    Bukkit.getServer().broadcast(Component.text("★ " + p.getName() + "님이 " + catName(cat) + " 도감을 완성했습니다!", NamedTextColor.GOLD));
            } else if (record && cat == 'f') {
                FusionLife.msg(p, name + " 최대 크기 갱신! " + cm + "cm");
            }
        } catch (Exception ex) {
            FusionLife.inst.getLogger().warning("dex record: " + ex);
        }
    }

    static String catName(char c) { return c == 'f' ? "물고기" : c == 'c' ? "작물" : "요리"; }

    static String habName(Fishing.Fish f) {
        String h = switch (f.hab()) {
            case ANY -> "어디서나"; case OCEAN -> "바다"; case RIVER -> "강·호수"; case SWAMP -> "늪";
            case COLD -> "추운 지역"; case WARM -> "따뜻한 지역"; case DEEP -> "깊은 바다";
        };
        return h + (f.night() ? " (밤)" : "");
    }

    private static Component t(String s, NamedTextColor c) { return Component.text(s, c).decoration(TextDecoration.ITALIC, false); }

    static void open(Player p, char cat) {
        Holder h = new Holder(cat);
        Map<String, int[]> m = load(p);
        List<String> ids = ids(cat);
        h.inv = Bukkit.createInventory(h, 54, Component.text("도감 - " + catName(cat) + " (" + found(m, cat) + "/" + ids.size() + ")", NamedTextColor.DARK_PURPLE));
        for (int i = 0; i < ids.size() && i < 45; i++) {
            String id = ids.get(i);
            int[] v = m.get(cat + ":" + id);
            ItemStack it;
            if (v != null) {
                it = Items.create(itemId(cat, id));
                ItemMeta im = it.getItemMeta();
                List<Component> lore = new ArrayList<>();
                if (cat == 'f') {
                    lore.add(t("최대 크기: " + v[0] + "cm", NamedTextColor.YELLOW));
                    lore.add(t("낚은 횟수: " + v[1] + "회", NamedTextColor.GRAY));
                } else lore.add(t((cat == 'c' ? "수확 " : "조리 ") + v[1] + "회", NamedTextColor.GRAY));
                im.lore(lore);
                it.setItemMeta(im);
            } else {
                it = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                ItemMeta im = it.getItemMeta();
                im.displayName(t("???", NamedTextColor.DARK_GRAY));
                List<Component> lore = new ArrayList<>();
                if (cat == 'f') {
                    Fishing.Fish f = Fishing.byId(id);
                    lore.add(t("등급: " + Fishing.RARITY[f.rarity()], NamedTextColor.GRAY));
                    lore.add(t("서식지: " + habName(f), NamedTextColor.GRAY));
                } else if (cat == 'c') {
                    lore.add(t("씨앗을 심어 수확해 보세요.", NamedTextColor.GRAY));
                } else {
                    for (Cooking.Recipe r : Cooking.RECIPES) if (r.out().equals(id)) {
                        Machines.MType mt = Machines.TYPES.get(r.machine());
                        lore.add(t("조리기구: " + (mt == null ? r.machine() : mt.name()), NamedTextColor.GRAY));
                        break;
                    }
                }
                im.lore(lore);
                it.setItemMeta(im);
            }
            h.inv.setItem(i, it);
        }
        Material[] icons = {Material.FISHING_ROD, Material.WHEAT, Material.COOKED_BEEF};
        for (int i = 0; i < 3; i++) {
            ItemStack b = new ItemStack(icons[i]);
            ItemMeta bm = b.getItemMeta();
            bm.displayName(t((TABS[i] == cat ? "▶ " : "") + catName(TABS[i]) + " 도감 (" + found(m, TABS[i]) + "/" + ids(TABS[i]).size() + ")",
                    TABS[i] == cat ? NamedTextColor.GREEN : NamedTextColor.WHITE));
            b.setItemMeta(bm);
            h.inv.setItem(TAB_SLOT[i], b);
        }
        p.openInventory(h.inv);
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!"dex".equals(Items.idOf(e.getItem()))) return;
        e.setCancelled(true);
        open(e.getPlayer(), 'f');
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getView().getTopInventory().getHolder() instanceof Holder)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (e.getClickedInventory() != e.getView().getTopInventory()) return;
        for (int i = 0; i < 3; i++) if (e.getRawSlot() == TAB_SLOT[i]) {
            char c = TABS[i];
            Bukkit.getScheduler().runTask(FusionLife.inst, () -> open(p, c));
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent e) {
        if (e.getView().getTopInventory().getHolder() instanceof Holder) e.setCancelled(true);
    }
}
