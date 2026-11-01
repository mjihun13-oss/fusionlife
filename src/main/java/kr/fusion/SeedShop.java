package kr.fusion;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/** 다이아몬드로 씨앗을 사는 GUI. 가격은 config.yml 에서 조절. */
public final class SeedShop implements Listener {
    static final class Holder implements InventoryHolder {
        Inventory inv;
        @Override public Inventory getInventory() { return inv; }
    }
    static final int FIRST = 9;

    static int price(String crop) { return Math.max(1, FusionLife.inst.getConfig().getInt("seed-shop." + crop + ".diamonds", 1)); }
    static int amount(String crop) { return Math.max(1, FusionLife.inst.getConfig().getInt("seed-shop." + crop + ".amount", 4)); }

    private static Component t(String s, NamedTextColor c) { return Component.text(s, c).decoration(TextDecoration.ITALIC, false); }

    static void open(Player p) {
        Holder h = new Holder();
        h.inv = Bukkit.createInventory(h, 27, Component.text("씨앗 상점 (다이아몬드)", NamedTextColor.DARK_GREEN));
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta pm = pane.getItemMeta(); pm.displayName(Component.text(" ")); pane.setItemMeta(pm);
        for (int i = 0; i < 27; i++) h.inv.setItem(i, pane);
        int slot = FIRST;
        for (String crop : Farming.CROPS.keySet()) {
            ItemStack it = Items.create("seed_" + crop);
            ItemMeta m = it.getItemMeta();
            List<Component> lore = new ArrayList<>(m.lore());
            lore.add(Component.empty());
            lore.add(t("가격: 다이아몬드 " + price(crop) + "개 → 씨앗 " + amount(crop) + "개", NamedTextColor.AQUA));
            lore.add(t("클릭: 1회 구매 / Shift+클릭: 8회 구매", NamedTextColor.YELLOW));
            m.lore(lore);
            it.setItemMeta(m);
            h.inv.setItem(slot++, it);
        }
        p.openInventory(h.inv);
    }

    private static int countDiamonds(Player p) {
        int n = 0;
        for (ItemStack s : p.getInventory().getStorageContents())
            if (s != null && "V:DIAMOND".equals(Items.ingKey(s))) n += s.getAmount();
        return n;
    }

    private static void takeDiamonds(Player p, int n) {
        ItemStack[] c = p.getInventory().getStorageContents();
        for (int i = 0; i < c.length && n > 0; i++) {
            ItemStack s = c[i];
            if (s == null || !"V:DIAMOND".equals(Items.ingKey(s))) continue;
            int take = Math.min(n, s.getAmount());
            n -= take;
            if (s.getAmount() - take <= 0) c[i] = null; else s.setAmount(s.getAmount() - take);
        }
        p.getInventory().setStorageContents(c);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getView().getTopInventory().getHolder() instanceof Holder)) return;
        e.setCancelled(true); // 상점 화면에서는 아이템을 옮길 수 없음
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (e.getClickedInventory() != e.getView().getTopInventory()) return;
        int idx = e.getRawSlot() - FIRST;
        List<String> crops = new ArrayList<>(Farming.CROPS.keySet());
        if (idx < 0 || idx >= crops.size()) return;
        ClickType ct = e.getClick();
        if (ct != ClickType.LEFT && ct != ClickType.SHIFT_LEFT && ct != ClickType.RIGHT && ct != ClickType.SHIFT_RIGHT) return;
        int times = e.isShiftClick() ? 8 : 1;
        String crop = crops.get(idx);
        int cost = price(crop) * times;
        if (countDiamonds(p) < cost) {
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            FusionLife.msg(p, "다이아몬드가 부족해요. (필요: " + cost + "개)");
            return;
        }
        takeDiamonds(p, cost);
        int total = amount(crop) * times;
        int left = total;
        while (left > 0) { // 한 스택(64)을 넘는 구매 대비
            int n = Math.min(64, left);
            Machines.give(p, Items.create("seed_" + crop, n));
            left -= n;
        }
        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
        FusionLife.msg(p, Items.def("seed_" + crop).name() + " " + total + "개 구매 (다이아 " + cost + "개)");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent e) {
        if (e.getView().getTopInventory().getHolder() instanceof Holder) e.setCancelled(true);
    }
}
