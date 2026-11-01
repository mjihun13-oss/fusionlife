package kr.fusion;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.inventory.*;

import java.util.*;

/** 제작 레시피 (바닐라 작업대). 재료는 모두 바닐라 아이템이라 별도 설정 없이 바로 제작 가능. */
public final class Crafts {
    static final List<NamespacedKey> KEYS = new ArrayList<>();
    private static FusionLife pl;

    private static NamespacedKey key(String n) { NamespacedKey k = new NamespacedKey(pl, n); KEYS.add(k); return k; }

    private static RecipeChoice ch(Object o) {
        if (o instanceof Material m) return new RecipeChoice.MaterialChoice(m);
        if (o instanceof Tag<?> t) return new RecipeChoice.MaterialChoice((Tag<Material>) t);
        if (o instanceof ItemStack s) return new RecipeChoice.ExactChoice(s);
        throw new IllegalArgumentException();
    }

    private static void shaped(String id, int n, String[] shape, Object... map) {
        ShapedRecipe r = new ShapedRecipe(key(id), Items.create(id, n));
        r.shape(shape);
        for (int i = 0; i < map.length; i += 2) r.setIngredient((Character) map[i], ch(map[i + 1]));
        Bukkit.addRecipe(r);
    }

    private static void shapeless(String id, int n, Object... ing) {
        ShapelessRecipe r = new ShapelessRecipe(key(id), Items.create(id, n));
        for (Object o : ing) {
            if (o instanceof Object[] pair) { for (int i = 0; i < (int) pair[1]; i++) r.addIngredient(ch(pair[0])); }
            else r.addIngredient(ch(o));
        }
        Bukkit.addRecipe(r);
    }

    public static void register(FusionLife plugin) {
        pl = plugin;
        // 조리기구 / 기계
        shaped("m_cutting_board", 1, new String[]{"PPP", "SSS"}, 'P', Tag.PLANKS, 'S', Material.STICK);
        shaped("m_fry_pan", 1, new String[]{"III", "III", "  S"}, 'I', Material.IRON_INGOT, 'S', Material.STICK);
        shaped("m_pot", 1, new String[]{"I I", "I I", "III"}, 'I', Material.IRON_INGOT);
        shaped("m_oven", 1, new String[]{"III", "IFI", "BBB"}, 'I', Material.IRON_INGOT, 'F', Material.FURNACE, 'B', Material.BRICKS);
        shaped("m_mixer", 1, new String[]{"GGG", " R ", "III"}, 'G', Material.GLASS, 'R', Material.REDSTONE, 'I', Material.IRON_INGOT);
        shaped("m_mill", 1, new String[]{"CCC", "CWC", "CCC"}, 'C', Material.COBBLESTONE, 'W', Material.WHEAT);
        shaped("m_press", 1, new String[]{"III", "IPI", "CCC"}, 'I', Material.IRON_INGOT, 'P', Material.PISTON, 'C', Material.COBBLESTONE);
        shaped("m_ferment", 1, new String[]{"PCP", "P P", "PPP"}, 'P', Tag.PLANKS, 'C', Material.CAULDRON);
        shaped("m_sprinkler", 1, new String[]{"CCC", " I ", " I "}, 'C', Material.COPPER_INGOT, 'I', Material.IRON_INGOT);
        shaped("m_seedshop", 1, new String[]{"PPP", "PDP", "PPP"}, 'P', Tag.PLANKS, 'D', Material.DIAMOND);
        shapeless("dex", 1, Material.BOOK, Material.COD, Material.WHEAT_SEEDS);
        // 농사 도구
        shaped("hoe_farmer", 1, new String[]{"GD", " S", " S"}, 'G', Material.GOLD_INGOT, 'D', Material.DIAMOND, 'S', Material.STICK);
        shapeless("fertilizer", 4, Material.BONE_MEAL, Material.BONE_MEAL, Material.ROTTEN_FLESH);
        // 낚시
        shapeless("bait", 4, Material.DIRT, Material.WHEAT_SEEDS);
        shaped("rod_bamboo", 1, new String[]{"  B", " BT", "B T"}, 'B', Material.BAMBOO, 'T', Material.STRING);
        shapeless("rod_iron", 1, Items.create("rod_bamboo"), new Object[]{Material.IRON_INGOT, 3});
        shapeless("rod_gold", 1, Items.create("rod_iron"), new Object[]{Material.GOLD_INGOT, 3});
        shapeless("rod_diamond", 1, Items.create("rod_gold"), new Object[]{Material.DIAMOND, 3});
        shapeless("rod_legend", 1, Items.create("rod_diamond"), Material.NETHERITE_INGOT, Items.create("pearl"));
        // 요리책
        shapeless("cookbook", 1, Material.BOOK, Material.WHEAT);
    }

    public static void unregister() {
        for (NamespacedKey k : KEYS) Bukkit.removeRecipe(k);
        KEYS.clear();
    }
}
