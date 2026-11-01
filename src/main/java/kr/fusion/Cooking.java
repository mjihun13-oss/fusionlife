package kr.fusion;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/** 요리: 레시피 데이터 + 음식 효과. 조리기구(Machines)가 이 레시피를 사용한다. */
public final class Cooking implements Listener {
    public record Recipe(String machine, Map<String, Integer> in, String out, int outN) {}

    static final List<Recipe> RECIPES = new ArrayList<>();
    /** 음식 id -> 효과 [타입, 초, 증폭] 목록 */
    static final Map<String, List<Object[]>> EFFECTS = new HashMap<>();
    static final Map<String, Integer> BONUS_SAT = new HashMap<>();

    /** 와일드카드 재료 그룹 */
    static final Set<String> VEG = Set.of("lettuce", "cabbage", "onion", "garlic", "pepper", "tomato");
    static boolean inGroup(String group, String key) {
        return switch (group) {
            case "#fish" -> key.startsWith("fish_");
            case "#veg" -> VEG.contains(key);
            case "#meat" -> key.equals("V:BEEF") || key.equals("V:PORKCHOP") || key.equals("V:CHICKEN") || key.equals("V:MUTTON");
            default -> false;
        };
    }
    static String groupName(String g) { return switch (g) { case "#fish" -> "아무 물고기"; case "#veg" -> "아무 채소"; case "#meat" -> "아무 고기"; default -> g; }; }

    private static void food(String id, String name, Material m, int sell, String fx, String... lore) {
        Items.reg(id, name, m, "food", sell, NamedTextColor.YELLOW, lore);
        if (fx != null && !fx.isEmpty()) {
            List<Object[]> l = new ArrayList<>();
            for (String s : fx.split(",")) {
                String[] p = s.split(":");
                l.add(new Object[]{p[0], Integer.parseInt(p[1]), Integer.parseInt(p[2])});
            }
            EFFECTS.put(id, l);
        }
    }

    private static void ing(String id, String name, Material m, int sell, String lore) {
        Items.reg(id, name, m, "ingredient", sell, NamedTextColor.WHITE, lore);
    }

    private static void r(String machine, String out, int n, Object... in) {
        Map<String, Integer> m = new LinkedHashMap<>();
        for (int i = 0; i < in.length; i += 2) m.put((String) in[i], (Integer) in[i + 1]);
        RECIPES.add(new Recipe(machine, m, out, n));
    }

    public static void registerItems() {
        // 가공 재료
        ing("flour", "밀가루", Material.SUGAR, 4, "제분기로 만든 가루.");
        ing("rice_grain", "쌀", Material.WHEAT_SEEDS, 5, "벼를 찧은 쌀.");
        ing("corn_flour", "옥수수가루", Material.SUGAR, 5, "제분기로 만든 가루.");
        ing("oil", "식용유", Material.GLASS_BOTTLE, 8, "압착기로 짜낸 기름.");
        ing("noodle", "생면", Material.STRING, 6, "밀가루로 뽑은 면.");

        food("cooked_rice", "밥", Material.BAKED_POTATO, 8, "saturation:3:0", "갓 지은 따끈한 밥.");
        food("salad", "신선 샐러드", Material.BEETROOT_SOUP, 14, "regeneration:6:0", "상큼한 채소 샐러드.");
        food("sashimi", "생선회", Material.COOKED_COD, 30, "night_vision:60:0", "싱싱한 생선회.");
        food("sushi", "초밥", Material.COOKED_SALMON, 40, "speed:60:0,saturation:2:0", "밥과 생선의 만남.");
        food("kimchi", "김치", Material.DRIED_KELP, 18, "regeneration:5:0", "잘 익은 김치.");
        food("tomato_soup", "토마토 수프", Material.MUSHROOM_STEW, 24, "absorption:60:0", "따뜻한 수프.");
        food("corn_soup", "옥수수 수프", Material.MUSHROOM_STEW, 22, "haste:60:0", "고소한 옥수수 수프.");
        food("popcorn", "팝콘", Material.COOKIE, 16, "speed:40:0", "바삭바삭 고소한 팝콘.");
        food("fried_rice", "볶음밥", Material.RABBIT_STEW, 30, "strength:60:0", "든든한 볶음밥.");
        food("tomato_egg", "토마토 계란볶음", Material.RABBIT_STEW, 28, "resistance:60:0", "집밥의 정석.");
        food("bulgogi", "불고기", Material.COOKED_BEEF, 45, "strength:90:0,saturation:2:0", "달콤짭짤한 불고기.");
        food("kimchi_stew", "김치찌개", Material.RABBIT_STEW, 42, "resistance:90:0,regeneration:8:0", "얼큰한 김치찌개.");
        food("spicy_stew", "매운탕", Material.RABBIT_STEW, 46, "fire_resistance:120:0,water_breathing:120:0", "얼큰한 생선 매운탕.");
        food("grilled_fish", "생선구이", Material.COOKED_COD, 34, "jump_boost:60:0", "노릇노릇 구운 생선.");
        food("ramen", "라면", Material.MUSHROOM_STEW, 38, "speed:90:1", "후루룩 라면.");
        food("bread_loaf", "식빵", Material.BREAD, 14, null, "폭신한 식빵.");
        food("garlic_bread", "마늘빵", Material.BREAD, 26, "night_vision:60:0", "향긋한 마늘빵.");
        food("pizza", "피자", Material.PUMPKIN_PIE, 52, "health_boost:120:1,regeneration:5:0", "토핑이 가득한 피자.");
        food("strawberry_cake", "딸기 케이크", Material.PUMPKIN_PIE, 70, "luck:180:0,regeneration:6:0", "달콤한 딸기 케이크.");
        food("tomato_juice", "토마토 주스", Material.HONEY_BOTTLE, 20, "regeneration:4:0", "신선한 토마토 주스.");
        food("grape_juice", "포도 주스", Material.HONEY_BOTTLE, 24, "haste:45:0", "달콤한 포도 주스.");
        food("strawberry_smoothie", "딸기 스무디", Material.HONEY_BOTTLE, 34, "speed:60:1", "시원한 딸기 스무디.");
        food("wine", "와인", Material.HONEY_BOTTLE, 90, "strength:60:0,nausea:6:0", "잘 숙성된 와인.");
        food("makgeolli", "막걸리", Material.HONEY_BOTTLE, 60, "resistance:60:1,nausea:5:0", "시원한 막걸리.");

        // 제분기
        r("mill", "flour", 2, "V:WHEAT", 1);
        r("mill", "rice_grain", 2, "rice", 1);
        r("mill", "corn_flour", 2, "corn", 1);
        // 압착기
        r("press", "oil", 1, "corn", 2);
        r("press", "tomato_juice", 1, "tomato", 2);
        r("press", "grape_juice", 1, "grape", 3);
        // 도마
        r("cutting_board", "noodle", 2, "flour", 2);
        r("cutting_board", "salad", 1, "lettuce", 1, "tomato", 1, "onion", 1);
        r("cutting_board", "sashimi", 1, "#fish", 1, "onion", 1);
        r("cutting_board", "sushi", 1, "cooked_rice", 1, "#fish", 1);
        // 냄비
        r("pot", "cooked_rice", 2, "rice_grain", 2);
        r("pot", "tomato_soup", 1, "tomato", 2, "onion", 1);
        r("pot", "corn_soup", 1, "corn", 2, "salt", 1);
        r("pot", "spicy_stew", 1, "#fish", 1, "pepper", 1, "onion", 1, "garlic", 1);
        r("pot", "kimchi_stew", 1, "kimchi", 1, "V:PORKCHOP", 1);
        r("pot", "ramen", 1, "noodle", 1, "V:EGG", 1, "pepper", 1);
        // 프라이팬
        r("fry_pan", "popcorn", 2, "corn_flour", 1, "oil", 1);
        r("fry_pan", "tomato_egg", 1, "tomato", 1, "V:EGG", 2);
        r("fry_pan", "fried_rice", 1, "cooked_rice", 1, "V:EGG", 1, "onion", 1);
        r("fry_pan", "bulgogi", 1, "#meat", 1, "onion", 1, "garlic", 1);
        // 오븐
        r("oven", "bread_loaf", 2, "flour", 2, "salt", 1);
        r("oven", "garlic_bread", 2, "bread_loaf", 1, "garlic", 1, "oil", 1);
        r("oven", "grilled_fish", 1, "#fish", 1, "salt", 1);
        r("oven", "pizza", 1, "flour", 2, "tomato", 2, "onion", 1, "pepper", 1);
        r("oven", "strawberry_cake", 1, "flour", 2, "V:SUGAR", 2, "V:EGG", 1, "strawberry", 2);
        // 믹서
        r("mixer", "strawberry_smoothie", 1, "strawberry", 3, "V:SUGAR", 1);
        // 발효통
        r("ferment", "kimchi", 2, "cabbage", 3, "pepper", 1, "garlic", 1, "salt", 1);
        r("ferment", "wine", 1, "grape_juice", 2, "V:SUGAR", 1);
        r("ferment", "makgeolli", 1, "rice_grain", 3, "V:SUGAR", 1);
    }

    // ---------- 레시피 매칭 ----------
    /** 투입 총량(키→수량)에서 레시피를 최대 몇 번 만들 수 있는지. 불일치(남는 재료/모자람)면 0 */
    static int times(Recipe rc, Map<String, Integer> have) {
        Map<String, Integer> left = new HashMap<>(have);
        int best = Integer.MAX_VALUE;
        // 정확 키 먼저
        Set<String> used = new HashSet<>();
        for (Map.Entry<String, Integer> e : rc.in().entrySet()) {
            if (e.getKey().startsWith("#")) continue;
            int h = left.getOrDefault(e.getKey(), 0);
            if (h < e.getValue()) return 0;
            best = Math.min(best, h / e.getValue());
            used.add(e.getKey());
        }
        // 그룹 재료: 남은 것 중 그룹에 속하는 키 합계
        for (Map.Entry<String, Integer> e : rc.in().entrySet()) {
            if (!e.getKey().startsWith("#")) continue;
            int sum = 0;
            for (Map.Entry<String, Integer> h : have.entrySet())
                if (!rc.in().containsKey(h.getKey()) && inGroup(e.getKey(), h.getKey())) { sum += h.getValue(); used.add(h.getKey()); }
            if (sum < e.getValue()) return 0;
            best = Math.min(best, sum / e.getValue());
        }
        // 레시피에 없는 재료가 섞여 있으면 거부
        for (String k : have.keySet()) if (!used.contains(k)) return 0;
        return best == Integer.MAX_VALUE ? 0 : best;
    }

    static Recipe find(String machine, Map<String, Integer> have) {
        for (Recipe rc : RECIPES) if (rc.machine().equals(machine) && times(rc, have) > 0) return rc;
        return null;
    }

    /** 재료 차감 계획: 슬롯 아이템에서 실제 소비 (times 회분). 그룹은 슬롯 순서대로 소모 */
    static void consume(Recipe rc, int times, ItemStack[] slots) {
        for (Map.Entry<String, Integer> e : rc.in().entrySet()) {
            int need = e.getValue() * times;
            for (int i = 0; i < slots.length && need > 0; i++) {
                ItemStack s = slots[i];
                String k = Items.ingKey(s);
                if (k == null) continue;
                boolean ok = e.getKey().startsWith("#") ? (!rc.in().containsKey(k) && inGroup(e.getKey(), k)) : k.equals(e.getKey());
                if (!ok) continue;
                int take = Math.min(need, s.getAmount());
                s.setAmount(s.getAmount() - take);
                need -= take;
            }
        }
    }

    // ---------- 섭취 ----------
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEat(PlayerItemConsumeEvent e) {
        String id = Items.idOf(e.getItem());
        if (id == null) return;
        Items.Def d = Items.def(id);
        if (d == null) return;
        Player p = e.getPlayer();
        if (!d.cat().equals("food")) {
            e.setCancelled(true); // 날재료/도구는 먹을 수 없음
            FusionLife.msg(p, "그대로는 먹을 수 없어요. 조리기구로 요리해 보세요.");
            return;
        }
        List<Object[]> fx = EFFECTS.get(id);
        if (fx == null) return;
        for (Object[] o : fx) {
            PotionEffectType t = Registry.EFFECT.get(NamespacedKey.minecraft((String) o[0]));
            if (t == null) continue;
            if (((String) o[0]).equals("saturation")) { p.setSaturation(Math.min(20f, p.getSaturation() + (int) o[1] * 2f)); continue; }
            p.addPotionEffect(new PotionEffect(t, (int) o[1] * 20, (int) o[2]));
        }
    }
}
