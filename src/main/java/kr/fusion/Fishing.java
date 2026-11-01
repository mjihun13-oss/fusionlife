package kr.fusion;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** 낚시: 물고기 36종(서식지/등급/시간대), 낚싯대 5종, 크기(cm) 랜덤. 오버월드 전역에서 자동 적용. */
public final class Fishing implements Listener {
    enum Hab { ANY, OCEAN, RIVER, SWAMP, COLD, WARM, DEEP }
    record Fish(String id, String name, Material mat, int rarity, Hab hab, int minCm, int maxCm, int sell, boolean night) {}

    static final String[] RARITY = {"", "일반", "고급", "희귀", "영웅", "전설"};
    static final TextColor[] RCOL = {NamedTextColor.WHITE, NamedTextColor.WHITE, NamedTextColor.GREEN, NamedTextColor.AQUA, NamedTextColor.LIGHT_PURPLE, NamedTextColor.GOLD};
    static final List<Fish> FISH = new ArrayList<>();
    /** tier -> {대기시간 최소tick, 최대tick, 희귀도 보너스} */
    static final int[][] ROD = {{}, {100, 500, 0}, {80, 380, 1}, {60, 280, 2}, {40, 200, 3}, {20, 120, 5}};
    static NamespacedKey SIZE_KEY;

    private static void f(String id, String name, Material m, int rar, Hab h, int min, int max, int sell, boolean night) {
        FISH.add(new Fish(id, name, m, rar, h, min, max, sell, night));
        Items.reg("fish_" + id, name, m, "fish", sell, RCOL[rar], "등급: " + RARITY[rar], "조리기구로 요리할 수 있어요.");
    }

    public static void registerItems(FusionLife pl) {
        SIZE_KEY = new NamespacedKey(pl, "fish_cm");
        // 일반
        f("carp", "잉어", Material.COD, 1, Hab.RIVER, 25, 80, 8, false);
        f("crucian", "붕어", Material.COD, 1, Hab.RIVER, 12, 40, 6, false);
        f("anchovy", "멸치", Material.COD, 1, Hab.OCEAN, 5, 14, 3, false);
        f("mackerel", "고등어", Material.COD, 1, Hab.OCEAN, 20, 45, 8, false);
        f("sardine", "정어리", Material.COD, 1, Hab.OCEAN, 10, 25, 4, false);
        f("loach", "미꾸라지", Material.COD, 1, Hab.SWAMP, 8, 22, 5, false);
        f("catfish", "메기", Material.COD, 1, Hab.SWAMP, 30, 90, 9, true);
        f("pollock", "명태", Material.COD, 1, Hab.COLD, 30, 60, 8, false);
        f("herring", "청어", Material.COD, 1, Hab.COLD, 15, 35, 6, false);
        f("tilapia", "틸라피아", Material.COD, 1, Hab.WARM, 15, 40, 6, false);
        f("mullet", "숭어", Material.COD, 1, Hab.ANY, 25, 70, 8, false);
        f("bluegill", "블루길", Material.COD, 1, Hab.ANY, 10, 25, 5, false);
        // 고급
        f("sweetfish", "은어", Material.SALMON, 2, Hab.RIVER, 15, 30, 16, false);
        f("trout", "송어", Material.SALMON, 2, Hab.COLD, 25, 70, 18, false);
        f("snapper", "참돔", Material.SALMON, 2, Hab.OCEAN, 30, 80, 22, false);
        f("flounder", "광어", Material.COD, 2, Hab.OCEAN, 30, 70, 20, false);
        f("eel", "장어", Material.SALMON, 2, Hab.SWAMP, 40, 100, 24, true);
        f("clownfish", "흰동가리", Material.TROPICAL_FISH, 2, Hab.WARM, 5, 12, 20, false);
        f("squid_fish", "한치", Material.COD, 2, Hab.OCEAN, 20, 45, 18, true);
        f("snakehead", "가물치", Material.SALMON, 2, Hab.RIVER, 40, 100, 22, true);
        // 희귀
        f("salmon", "연어", Material.SALMON, 3, Hab.COLD, 50, 100, 40, false);
        f("seabass", "농어", Material.SALMON, 3, Hab.OCEAN, 45, 110, 42, false);
        f("blowfish", "복어", Material.PUFFERFISH, 3, Hab.WARM, 20, 45, 44, false);
        f("yellowtail", "방어", Material.SALMON, 3, Hab.OCEAN, 60, 120, 48, false);
        f("angelfish", "엔젤피시", Material.TROPICAL_FISH, 3, Hab.WARM, 10, 25, 38, false);
        f("mandarin", "쏘가리", Material.SALMON, 3, Hab.RIVER, 30, 60, 46, true);
        // 영웅
        f("tuna", "참다랑어", Material.SALMON, 4, Hab.DEEP, 120, 300, 120, false);
        f("sturgeon", "철갑상어", Material.SALMON, 4, Hab.RIVER, 150, 350, 130, false);
        f("anglerfish", "아귀", Material.COD, 4, Hab.DEEP, 40, 120, 110, true);
        f("moonfish", "달빛물고기", Material.TROPICAL_FISH, 4, Hab.ANY, 30, 60, 100, true);
        f("icefish", "얼음빙어", Material.SALMON, 4, Hab.COLD, 30, 70, 105, false);
        // 전설
        f("golden_carp", "황금잉어", Material.SALMON, 5, Hab.RIVER, 80, 150, 350, false);
        f("coelacanth", "실러캔스", Material.COD, 5, Hab.DEEP, 100, 200, 450, true);
        f("dragonfish", "용왕어", Material.PUFFERFISH, 5, Hab.DEEP, 150, 300, 600, false);
        f("rainbow_trout", "무지개송어왕", Material.SALMON, 5, Hab.COLD, 90, 160, 380, false);
        f("kraken_baby", "아기 크라켄", Material.PUFFERFISH, 5, Hab.OCEAN, 100, 250, 520, true);
        f("swamp_king", "늪의 왕", Material.COD, 5, Hab.SWAMP, 120, 240, 400, true);

        // 해양 부산물(요리 재료)
        Items.reg("seaweed", "청각", Material.KELP, "ingredient", 3, NamedTextColor.DARK_GREEN, "바다에서 건져 올린 해조류.");
        Items.reg("salt", "소금 결정", Material.SUGAR, "ingredient", 3, NamedTextColor.WHITE, "요리의 기본 재료.");
        Items.reg("shell", "조개", Material.NAUTILUS_SHELL, "ingredient", 10, NamedTextColor.AQUA, "요리 재료로 쓰는 조개.");
        Items.reg("pearl", "진주", Material.HEART_OF_THE_SEA, "ingredient", 150, NamedTextColor.LIGHT_PURPLE, "바다에서 건진 귀한 보물.");
        Items.reg("bait", "지렁이 미끼", Material.STRING, "tool", 0, NamedTextColor.GRAY, "낚시 중 오프핸드에 들고 있으면 입질이 빨라져요.");

        rod("rod_bamboo", "대나무 낚싯대", NamedTextColor.GREEN, 1);
        rod("rod_iron", "강철 낚싯대", NamedTextColor.WHITE, 2);
        rod("rod_gold", "황금 낚싯대", NamedTextColor.GOLD, 3);
        rod("rod_diamond", "다이아 낚싯대", NamedTextColor.AQUA, 4);
        rod("rod_legend", "용왕의 낚싯대", NamedTextColor.LIGHT_PURPLE, 5);
    }

    private static void rod(String id, String name, TextColor c, int tier) {
        Items.reg(id, name, Material.FISHING_ROD, "tool", 0, c, "낚싯대 등급 " + tier + "/5", "등급이 높을수록 입질이 빠르고 희귀한 물고기가 잘 낚여요.");
    }

    static int rodTier(ItemStack it) {
        String id = Items.idOf(it);
        if (id == null) return 0;
        return switch (id) { case "rod_bamboo" -> 1; case "rod_iron" -> 2; case "rod_gold" -> 3; case "rod_diamond" -> 4; case "rod_legend" -> 5; default -> 0; };
    }

    static Hab habitat(org.bukkit.block.Block b) {
        Biome bi = b.getBiome();
        String n = bi.getKey().getKey();
        if (n.contains("swamp") || n.contains("mangrove")) return Hab.SWAMP;
        if (n.contains("frozen") || n.contains("snowy") || n.contains("cold") || n.contains("ice") || n.contains("taiga") || n.contains("grove")) return Hab.COLD;
        if (n.contains("deep")) return Hab.DEEP;
        if (n.contains("warm") || n.contains("lukewarm") || n.contains("jungle") || n.contains("desert") || n.contains("savanna") || n.contains("badlands")) return Hab.WARM;
        if (n.contains("river")) return Hab.RIVER;
        if (n.contains("ocean") || n.contains("beach")) return Hab.OCEAN;
        return Hab.RIVER; // 육지 호수/연못은 민물
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFish(PlayerFishEvent e) {
        Player p = e.getPlayer();
        if (!Store.isOverworld(p.getWorld())) return;
        ItemStack rod = p.getInventory().getItemInMainHand();
        if (rod.getType() != Material.FISHING_ROD) rod = p.getInventory().getItemInOffHand();
        int tier = rodTier(rod);

        if (e.getState() == PlayerFishEvent.State.FISHING) {
            FishHook h = e.getHook();
            int[] t = tier > 0 ? ROD[tier] : new int[]{100, 600, 0};
            int min = t[0], max = t[1];
            // 오프핸드 미끼: 입질 20% 단축 (소모는 낚을 때)
            if ("bait".equals(Items.idOf(p.getInventory().getItemInOffHand()))) { min = (int) (min * 0.8); max = (int) (max * 0.8); }
            h.setMinWaitTime(Math.max(20, min));
            h.setMaxWaitTime(Math.max(min + 20, max));
            return;
        }
        if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH && e.getState() != PlayerFishEvent.State.CAUGHT_ENTITY) return;
        if (e.getState() == PlayerFishEvent.State.CAUGHT_ENTITY || !(e.getCaught() instanceof Item caught)) return;

        ThreadLocalRandom r = ThreadLocalRandom.current();
        int fishLv = Store.level(p, "fishing");
        boolean baited = false;
        ItemStack off = p.getInventory().getItemInOffHand();
        if ("bait".equals(Items.idOf(off)) && p.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            off.setAmount(off.getAmount() - 1);
            baited = true;
        }
        int bonus = (tier > 0 ? ROD[tier][2] : 0) + fishLv / 8 + (baited ? 1 : 0);

        ItemStack result;
        double roll = r.nextDouble();
        if (roll < 0.06) {
            String[] junk = {"seaweed", "salt", "shell"};
            result = Items.create(junk[r.nextInt(junk.length)]);
        } else if (roll < 0.06 + 0.004 * (1 + bonus)) {
            result = Items.create("pearl");
        } else {
            result = rollFish(p, e.getHook().getLocation().getBlock(), bonus);
        }
        caught.setItemStack(result);
        e.setExpToDrop(r.nextInt(1, 4));
        if (Items.idOf(result) != null && Items.idOf(result).startsWith("fish_")) {
            Items.Def d = Items.def(Items.idOf(result));
            Fish fs = byId(d.id().substring(5));
            int rar = fs == null ? 1 : fs.rarity();
            if (rar >= 4) {
                p.getServer().broadcast(Component.text("★ " + p.getName() + "님이 ", NamedTextColor.YELLOW)
                        .append(Component.text(d.name(), d.color(), TextDecoration.BOLD)).append(Component.text("을(를) 낚았습니다!", NamedTextColor.YELLOW)));
            }
            Integer cmv = result.getItemMeta().getPersistentDataContainer().get(SIZE_KEY, PersistentDataType.INTEGER);
            Dex.record(p, 'f', d.id().substring(5), cmv == null ? 0 : cmv);
            FusionLife.gain(p, "fishing", 2 + rar * 3);
        } else FusionLife.gain(p, "fishing", 1);
    }

    static Fish byId(String id) { for (Fish f : FISH) if (f.id().equals(id)) return f; return null; }

    static ItemStack rollFish(Player p, org.bukkit.block.Block at, int bonus) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        Hab hab = habitat(at);
        World w = at.getWorld();
        long time = w.getTime();
        boolean night = time >= 13000 && time <= 23000;
        boolean rain = w.hasStorm();
        // 등급별 가중치 (보너스가 높을수록 희귀 가중 증가)
        double[] rw = {0, 60, 28 + bonus * 2, 9 + bonus * 1.6, 2.5 + bonus * 0.7, 0.4 + bonus * 0.25};
        if (rain) { rw[3] *= 1.3; rw[4] *= 1.2; }
        List<Fish> pool = new ArrayList<>();
        List<Double> wt = new ArrayList<>();
        double total = 0;
        int cnt = 0;
        for (Fish f : FISH) {
            boolean hOk = f.hab() == Hab.ANY || f.hab() == hab || (f.hab() == Hab.DEEP && hab == Hab.OCEAN && at.getY() < 55)
                    || (f.hab() == Hab.OCEAN && hab == Hab.DEEP);
            if (!hOk) continue;
            if (f.night() && !night) continue;
            double wgt = rw[f.rarity()] / Math.max(1, countRar(hab, night, f.rarity()));
            pool.add(f); wt.add(wgt); total += wgt;
        }
        if (pool.isEmpty()) return Items.create("fish_bluegill");
        double x = r.nextDouble() * total;
        Fish pick = pool.get(pool.size() - 1);
        for (int i = 0; i < pool.size(); i++) { x -= wt.get(i); if (x <= 0) { pick = pool.get(i); break; } }
        ItemStack it = Items.create("fish_" + pick.id());
        int cm = sizeOf(pick, r);
        ItemMeta m = it.getItemMeta();
        List<Component> lore = new ArrayList<>(m.lore());
        lore.add(0, Component.text("크기: " + cm + "cm", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        m.lore(lore);
        m.getPersistentDataContainer().set(SIZE_KEY, PersistentDataType.INTEGER, cm);
        it.setItemMeta(m);
        return it;
    }

    private static int countRar(Hab hab, boolean night, int rar) {
        int n = 0;
        for (Fish f : FISH) {
            if (f.rarity() != rar) continue;
            boolean hOk = f.hab() == Hab.ANY || f.hab() == hab || f.hab() == Hab.DEEP && hab == Hab.OCEAN || f.hab() == Hab.OCEAN && hab == Hab.DEEP;
            if (hOk && (!f.night() || night)) n++;
        }
        return n;
    }

    private static int sizeOf(Fish f, ThreadLocalRandom r) {
        double g = Math.min(1, Math.max(0, (r.nextDouble() + r.nextDouble()) / 2)); // 중간값에 몰리는 분포
        return (int) Math.round(f.minCm() + (f.maxCm() - f.minCm()) * g);
    }
}
