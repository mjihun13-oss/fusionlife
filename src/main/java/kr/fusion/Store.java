package kr.fusion;

import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/** 청크 PDC에 작물/기계 위치를 저장하는 저장소. 로드된 청크는 메모리에 캐시하고 변경 시 즉시 기록(write-through). */
public final class Store implements Listener {
    private static NamespacedKey KEY;
    private static FusionLife plugin;
    /** chunkKey -> (blockKey -> value) */
    private static final Map<Long, Map<Long, String>> CACHE = new HashMap<>();

    public static void init(FusionLife p) {
        plugin = p;
        KEY = new NamespacedKey(p, "blocks");
        for (World w : p.getServer().getWorlds())
            if (w.getEnvironment() == World.Environment.NORMAL)
                for (Chunk c : w.getLoadedChunks()) load(c);
    }

    public static boolean isOverworld(World w) { return w.getEnvironment() == World.Environment.NORMAL; }

    private static long ck(Chunk c) { return c.getChunkKey(); }

    private static Map<Long, String> load(Chunk c) {
        Map<Long, String> m = new HashMap<>();
        String raw = c.getPersistentDataContainer().get(KEY, PersistentDataType.STRING);
        if (raw != null && !raw.isEmpty()) {
            for (String e : raw.split(";")) {
                int i = e.indexOf('=');
                if (i <= 0) continue;
                try { m.put(Long.parseLong(e.substring(0, i)), e.substring(i + 1)); } catch (NumberFormatException ignored) {}
            }
        }
        CACHE.put(ck(c), m);
        return m;
    }

    private static Map<Long, String> map(Chunk c) {
        Map<Long, String> m = CACHE.get(ck(c));
        return m != null ? m : load(c);
    }

    private static void save(Chunk c, Map<Long, String> m) {
        PersistentDataContainer pdc = c.getPersistentDataContainer();
        if (m.isEmpty()) { pdc.remove(KEY); return; }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Long, String> e : m.entrySet()) sb.append(e.getKey()).append('=').append(e.getValue()).append(';');
        pdc.set(KEY, PersistentDataType.STRING, sb.toString());
    }

    public static String get(Block b) {
        if (!isOverworld(b.getWorld())) return null;
        return map(b.getChunk()).get(b.getBlockKey());
    }

    public static void set(Block b, String value) {
        if (!isOverworld(b.getWorld())) return;
        Chunk c = b.getChunk();
        Map<Long, String> m = map(c);
        m.put(b.getBlockKey(), value);
        save(c, m);
    }

    public static void remove(Block b) {
        if (!isOverworld(b.getWorld())) return;
        Chunk c = b.getChunk();
        Map<Long, String> m = map(c);
        if (m.remove(b.getBlockKey()) != null) save(c, m);
    }

    /** 로드된 오버월드 청크 전체에서 접두사로 시작하는 항목을 순회 (스프링클러 등) */
    public static List<Block> findLoaded(String prefix) {
        List<Block> out = new ArrayList<>();
        for (World w : plugin.getServer().getWorlds()) {
            if (!isOverworld(w)) continue;
            for (Chunk c : w.getLoadedChunks()) {
                Map<Long, String> m = CACHE.get(ck(c));
                if (m == null || m.isEmpty()) continue;
                for (Map.Entry<Long, String> e : new ArrayList<>(m.entrySet()))
                    if (e.getValue().startsWith(prefix)) out.add(w.getBlockAtKey(e.getKey()));
            }
        }
        return out;
    }

    /** 반경 내(정육면체) 작물 블록 수집 */
    public static List<Block> cropsAround(Block center, int r) {
        List<Block> out = new ArrayList<>();
        World w = center.getWorld();
        int cx = center.getX(), cy = center.getY(), cz = center.getZ();
        Set<Long> seen = new HashSet<>();
        for (int x = cx - r; x <= cx + r; x += 16)
            for (int z = cz - r; z <= cz + r; z += 16) { collect(w, x >> 4, z >> 4, seen, out, cx, cy, cz, r); }
        collect(w, (cx + r) >> 4, (cz + r) >> 4, seen, out, cx, cy, cz, r);
        collect(w, (cx - r) >> 4, (cz - r) >> 4, seen, out, cx, cy, cz, r);
        collect(w, (cx + r) >> 4, (cz - r) >> 4, seen, out, cx, cy, cz, r);
        collect(w, (cx - r) >> 4, (cz + r) >> 4, seen, out, cx, cy, cz, r);
        return out;
    }

    private static void collect(World w, int chx, int chz, Set<Long> seen, List<Block> out, int cx, int cy, int cz, int r) {
        if (!w.isChunkLoaded(chx, chz)) return;
        Chunk c = w.getChunkAt(chx, chz);
        if (!seen.add(ck(c))) return;
        Map<Long, String> m = CACHE.get(ck(c));
        if (m == null) return;
        for (Map.Entry<Long, String> e : m.entrySet()) {
            if (!e.getValue().startsWith("crop:")) continue;
            Block b = w.getBlockAtKey(e.getKey());
            if (Math.abs(b.getX() - cx) <= r && Math.abs(b.getY() - cy) <= r && Math.abs(b.getZ() - cz) <= r) out.add(b);
        }
    }

    @EventHandler public void onLoad(ChunkLoadEvent e) { if (isOverworld(e.getWorld())) load(e.getChunk()); }
    @EventHandler public void onUnload(ChunkUnloadEvent e) { CACHE.remove(ck(e.getChunk())); }

    // ---- 플레이어 숙련도 (농사/낚시/요리) ----
    public static int xp(Player p, String skill) {
        Integer v = p.getPersistentDataContainer().get(new NamespacedKey(plugin, "xp_" + skill), PersistentDataType.INTEGER);
        return v == null ? 0 : v;
    }

    public static int level(Player p, String skill) { return levelFor(xp(p, skill)); }
    public static int levelFor(int xp) { return (int) Math.floor(Math.sqrt(xp / 20.0)) + 1; }

    /** 경험치 추가. 레벨업 시 true */
    public static boolean addXp(Player p, String skill, int amount) {
        int before = level(p, skill);
        int now = xp(p, skill) + amount;
        p.getPersistentDataContainer().set(new NamespacedKey(plugin, "xp_" + skill), PersistentDataType.INTEGER, now);
        return levelFor(now) > before;
    }
}
