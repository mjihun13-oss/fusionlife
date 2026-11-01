package kr.fusion;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/** 모든 커스텀 아이템의 레지스트리. PDC 태그("fid")로 식별한다 (리소스팩 불필요). */
public final class Items {
    public record Def(String id, String name, Material mat, int cmd, String cat, int sell, TextColor color, List<String> lore) {}

    public static NamespacedKey FID;
    private static final Map<String, Def> DEFS = new LinkedHashMap<>();
    private static int cmdCounter = 7000;

    private Items() {}

    public static void init(FusionLife plugin) {
        FID = new NamespacedKey(plugin, "fid");
    }

    public static Def reg(String id, String name, Material mat, String cat, int sell, TextColor color, String... lore) {
        Def d = new Def(id, name, mat, cmdCounter++, cat, sell, color, List.of(lore));
        DEFS.put(id, d);
        return d;
    }

    public static Def def(String id) { return DEFS.get(id); }
    public static Collection<Def> all() { return DEFS.values(); }

    public static ItemStack create(String id, int amount) {
        Def d = DEFS.get(id);
        if (d == null) throw new IllegalArgumentException("unknown item " + id);
        ItemStack it = new ItemStack(d.mat(), Math.max(1, amount));
        ItemMeta m = it.getItemMeta();
        m.displayName(Component.text(d.name(), d.color()).decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        for (String l : d.lore()) lore.add(Component.text(l, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        m.lore(lore);
        m.setCustomModelData(d.cmd());
        m.setItemModel(new NamespacedKey("fusion", id)); // 리소스팩: assets/fusion/items/<id>.json
        m.getPersistentDataContainer().set(FID, PersistentDataType.STRING, id);
        it.setItemMeta(m);
        return it;
    }

    public static ItemStack create(String id) { return create(id, 1); }

    /** 커스텀 id, 없으면 null */
    public static String idOf(ItemStack it) {
        if (it == null || it.getType().isAir() || !it.hasItemMeta()) return null;
        return it.getItemMeta().getPersistentDataContainer().get(FID, PersistentDataType.STRING);
    }

    /** 재료 키: 커스텀이면 id, 바닐라면 "V:MATERIAL" (이름이 바뀐 일반 아이템은 바닐라로 취급하지 않음) */
    public static String ingKey(ItemStack it) {
        if (it == null || it.getType().isAir()) return null;
        String id = idOf(it);
        if (id != null) return id;
        if (it.hasItemMeta() && it.getItemMeta().hasDisplayName()) return null;
        return "V:" + it.getType().name();
    }

    public static int sellValue(ItemStack it) {
        String id = idOf(it);
        if (id == null) return 0;
        Def d = DEFS.get(id);
        return d == null ? 0 : d.sell();
    }
}
