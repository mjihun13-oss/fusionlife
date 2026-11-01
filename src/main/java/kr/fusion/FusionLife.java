package kr.fusion;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class FusionLife extends JavaPlugin implements Listener {
    static FusionLife inst;
    static final Map<String, String> SKILL_NAME = Map.of("farming", "농사", "fishing", "낚시", "cooking", "요리");
    static final Map<String, String> VANILLA = Map.of("V:EGG", "달걀", "V:SUGAR", "설탕", "V:WHEAT", "밀",
            "V:PORKCHOP", "돼지고기", "V:BEEF", "소고기", "V:CHICKEN", "닭고기", "V:MUTTON", "양고기");

    @Override public void onEnable() {
        inst = this;
        Items.init(this);
        Farming.registerItems();
        Fishing.registerItems(this);
        Cooking.registerItems();
        Machines.registerItems();
        Items.reg("dex", "도감", Material.KNOWLEDGE_BOOK, "tool", 0, NamedTextColor.LIGHT_PURPLE, "우클릭: 물고기 · 작물 · 요리 도감을 봅니다.");
        Items.reg("cookbook", "요리책", Material.BOOK, "tool", 0, NamedTextColor.GOLD, "우클릭: 모든 레시피와 가이드를 봅니다.");
        saveDefaultConfig();
        Store.init(this);
        Crafts.register(this);
        var pm = getServer().getPluginManager();
        pm.registerEvents(new Store(), this);
        pm.registerEvents(new Farming(), this);
        pm.registerEvents(new Fishing(), this);
        pm.registerEvents(new Cooking(), this);
        pm.registerEvents(new Machines(), this);
        pm.registerEvents(new SeedShop(), this);
        pm.registerEvents(new Dex(), this);
        pm.registerEvents(this, this);
        getServer().getScheduler().runTaskTimer(this, () -> {
            try { Machines.tickSprinklers(); } catch (Exception ex) { getLogger().warning("sprinkler tick: " + ex); }
        }, 100L, 100L);
        for (Player p : getServer().getOnlinePlayers()) p.discoverRecipes(Crafts.KEYS);
        getLogger().info("FusionLife 활성화: 아이템 " + Items.all().size() + "종");
    }

    @Override public void onDisable() {
        for (Player p : getServer().getOnlinePlayers()) p.closeInventory(); // GUI 안 아이템 반환/정산
        Crafts.unregister();
    }

    // ---------- 공용 유틸 ----------
    static void msg(Player p, String s) { p.sendActionBar(Component.text(s, NamedTextColor.YELLOW)); }

    static void gain(Player p, String skill, int xp) {
        boolean up = Store.addXp(p, skill, xp);
        String n = SKILL_NAME.getOrDefault(skill, skill);
        if (up) {
            p.sendMessage(Component.text("★ " + n + " 숙련도 Lv." + Store.level(p, skill) + " 달성!", NamedTextColor.GOLD));
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);
        } else {
            p.sendActionBar(Component.text(n + " Lv." + Store.level(p, skill) + "  +" + xp + "xp", NamedTextColor.GREEN));
        }
    }

    // ---------- 접속 / 요리책 ----------
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        p.discoverRecipes(Crafts.KEYS);
        NamespacedKey k = new NamespacedKey(this, "got_book");
        if (!p.getPersistentDataContainer().has(k, PersistentDataType.BYTE)) {
            p.getPersistentDataContainer().set(k, PersistentDataType.BYTE, (byte) 1);
            Machines.give(p, Items.create("cookbook"));
        }
        NamespacedKey k2 = new NamespacedKey(this, "got_dex");
        if (!p.getPersistentDataContainer().has(k2, PersistentDataType.BYTE)) {
            p.getPersistentDataContainer().set(k2, PersistentDataType.BYTE, (byte) 1);
            Machines.give(p, Items.create("dex"));
        }
        String url = getConfig().getString("resource-pack.url", "");
        if (url != null && !url.isBlank()) {
            String sha = getConfig().getString("resource-pack.sha1", "");
            boolean req = getConfig().getBoolean("resource-pack.required", false);
            getServer().getScheduler().runTaskLater(this, () -> {
                if (!p.isOnline()) return;
                try {
                    if (sha != null && sha.length() == 40) p.setResourcePack(url, sha, req);
                    else p.setResourcePack(url);
                } catch (Exception ex) { getLogger().warning("resource pack: " + ex); }
            }, 20L);
        }
    }

    @EventHandler
    public void onBook(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!"cookbook".equals(Items.idOf(e.getItem()))) return;
        e.setCancelled(true);
        e.getPlayer().openBook(buildBook());
    }

    static String nameOf(String key) {
        if (key.startsWith("#")) return Cooking.groupName(key);
        if (key.startsWith("V:")) return VANILLA.getOrDefault(key, key.substring(2));
        Items.Def d = Items.def(key);
        return d == null ? key : d.name();
    }

    static Component leg(String s) {
        return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(s);
    }

    static Book buildBook() {
        List<Component> pages = new ArrayList<>();
        pages.add(leg("§l융합 라이프 가이드\n\n§r§2농사§0: 풀을 베면 씨앗이 나와요. 경작지에 심고 비료·스프링클러로 키우세요.\n§1낚시§0: 낚싯대 5단계. 오프핸드 미끼로 입질↑\n§6요리§0: 조리기구를 설치해 우클릭!\n§8씨앗은 씨앗 상점에서 다이아로도 살 수 있어요.\n도감에 기록을 채워보세요."));
        pages.add(leg("§l숙련도\n\n§r§0농사: 수확량 보너스\n낚시: 희귀 확률↑\n요리: 대성공(2배)\n\n전설 물고기는 낚싯대 등급과 밤·비·깊은 바다가 중요해요."));
        for (String m : Machines.COOK) {
            Machines.MType t = Machines.TYPES.get(m);
            StringBuilder sb = new StringBuilder("§l" + t.name() + "\n§r§0");
            int used = 0;
            for (Cooking.Recipe rc : Cooking.RECIPES) {
                if (!rc.machine().equals(m)) continue;
                StringBuilder line = new StringBuilder();
                for (Map.Entry<String, Integer> e : rc.in().entrySet()) {
                    if (line.length() > 0) line.append("+");
                    line.append(nameOf(e.getKey())); if (e.getValue() > 1) line.append("x").append(e.getValue());
                }
                line.append("→").append(nameOf(rc.out())).append(rc.outN() > 1 ? "x" + rc.outN() : "").append("\n");
                if (sb.length() + line.length() > 230) { pages.add(leg(sb.toString())); sb = new StringBuilder("§l" + t.name() + " (계속)\n§r§0"); }
                sb.append(line);
            }
            pages.add(leg(sb.toString()));
        }
        return Book.book(Component.text("요리책"), Component.text("FusionLife"), pages);
    }
}
