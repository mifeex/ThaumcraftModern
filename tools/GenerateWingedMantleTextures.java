import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;

public final class GenerateWingedMantleTextures {
    private static final int VOID = 0xFF0C0917;
    private static final int DEEP = 0xFF151025;
    private static final int MID = 0xFF211638;
    private static final int EDGE = 0xFF332250;
    private static final int RUNE = 0xFF7750A1;
    private static final int GOLD = 0xFF9E7421;
    private static final int GOLD_HI = 0xFFD1A44B;
    private static final int LEATHER = 0xFF4B2D23;
    private static final int PARCHMENT = 0xFFD1C08C;
    private static final int GREEN = 0xFF3B783E;

    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]);
        writeArmor(root.resolve("entity/models/winged_mantle_armor.png"));
        writeIcon(root.resolve("item/winged_mantle_hood.png"), 0);
        writeIcon(root.resolve("item/winged_mantle_chestplate.png"), 1);
        writeIcon(root.resolve("item/winged_mantle_leggings.png"), 2);
        writeIcon(root.resolve("item/winged_mantle_boots.png"), 3);
    }

    private static void writeArmor(Path path) throws Exception {
        int[][] p = new int[128][128];
        fill(p, 0, 0, 128, 128, DEEP);
        for (int y = 0; y < 128; y += 4)
            for (int x = 0; x < 128; x += 4)
                fill(p, x, y, 2, 2, ((x + y) / 4 & 1) == 0 ? MID : VOID);
        // Hood and chest islands: deep borders with restrained antique-gold fittings.
        border(p, 0, 0, 80, 32, EDGE, VOID);
        border(p, 0, 34, 82, 30, EDGE, DEEP);
        border(p, 34, 34, 14, 14, GOLD, MID);
        fill(p, 39, 39, 4, 4, GREEN);
        fill(p, 40, 38, 2, 6, 0xFF69A955);
        border(p, 80, 34, 12, 12, GOLD, LEATHER);
        // Cloth tails, book, parchment, pouch.
        border(p, 0, 50, 62, 20, EDGE, MID);
        border(p, 64, 50, 18, 18, GOLD, LEATHER);
        line(p, 68, 55, 10, 0, GOLD_HI);
        line(p, 72, 53, 0, 12, GOLD);
        border(p, 84, 50, 10, 14, GOLD_HI, PARCHMENT);
        border(p, 96, 50, 12, 12, GOLD, LEATHER);
        // Wing panels with stepped seams and Thaumcraft-like pixel runes.
        wingPanel(p, 0, 72, 78, 22);
        wingPanel(p, 0, 96, 78, 22);
        // Layered pauldrons, bracers and greaves.
        for (int y = 0; y < 96; y += 12) {
            border(p, 82, y, 20, 11, EDGE, MID);
            border(p, 102, y, 20, 11, EDGE, MID);
            fill(p, 84, y + 2, 2, 2, GOLD);
            fill(p, 116, y + 2, 2, 2, GOLD);
        }
        writePng(path, p);
    }

    private static void wingPanel(int[][] p, int x, int y, int w, int h) {
        border(p, x, y, w, h, EDGE, DEEP);
        for (int sx = x + 10; sx < x + w - 4; sx += 12) {
            fill(p, sx, y + 2, 1, h - 4, 0xFF291B43);
        }
        // Angular void rune, intentionally 1px/2px wide for nearest-neighbour clarity.
        int cx = x + w - 14, cy = y + 7;
        line(p, cx, cy, 0, 8, RUNE);
        line(p, cx - 3, cy + 3, 7, 0, RUNE);
        fill(p, cx + 3, cy + 1, 2, 2, 0xFF9B70C2);
        fill(p, x + 2, y + h - 3, 2, 2, GOLD);
        fill(p, x + w - 4, y + h - 3, 2, 2, GOLD);
    }

    private static void writeIcon(Path path, int kind) throws Exception {
        int[][] p = new int[16][16];
        if (kind == 0) {
            fill(p, 4, 2, 8, 2, EDGE); fill(p, 3, 4, 10, 8, MID);
            fill(p, 5, 6, 6, 5, VOID); fill(p, 3, 12, 10, 2, DEEP);
        } else if (kind == 1) {
            fill(p, 5, 2, 6, 12, MID); fill(p, 2, 4, 3, 8, EDGE); fill(p, 11, 4, 3, 8, EDGE);
            fill(p, 0, 3, 3, 11, DEEP); fill(p, 13, 3, 3, 11, DEEP);
            fill(p, 7, 5, 2, 2, GREEN); fill(p, 7, 8, 2, 2, RUNE);
        } else if (kind == 2) {
            fill(p, 4, 2, 8, 5, MID); fill(p, 4, 7, 3, 7, DEEP); fill(p, 9, 7, 3, 7, DEEP);
            fill(p, 4, 5, 8, 2, EDGE); fill(p, 5, 12, 2, 2, RUNE); fill(p, 9, 12, 2, 2, RUNE);
        } else {
            fill(p, 3, 5, 4, 7, DEEP); fill(p, 9, 5, 4, 7, DEEP);
            fill(p, 2, 11, 6, 3, MID); fill(p, 8, 11, 6, 3, MID);
            fill(p, 3, 12, 4, 1, GOLD); fill(p, 9, 12, 4, 1, GOLD);
        }
        writePng(path, p);
    }

    private static void border(int[][] p, int x, int y, int w, int h, int edge, int inside) {
        fill(p, x, y, w, h, edge); if (w > 2 && h > 2) fill(p, x + 1, y + 1, w - 2, h - 2, inside);
    }
    private static void line(int[][] p, int x, int y, int dx, int dy, int color) {
        int count = Math.max(Math.abs(dx), Math.abs(dy)) + 1;
        for (int i = 0; i < count; i++) {
            int px = x + (count == 1 ? 0 : Math.round((float) dx * i / (count - 1)));
            int py = y + (count == 1 ? 0 : Math.round((float) dy * i / (count - 1)));
            if (px >= 0 && py >= 0 && py < p.length && px < p[0].length) p[py][px] = color;
        }
    }
    private static void fill(int[][] p, int x, int y, int w, int h, int color) {
        for (int yy = Math.max(0, y); yy < Math.min(p.length, y + h); yy++)
            for (int xx = Math.max(0, x); xx < Math.min(p[0].length, x + w); xx++) p[yy][xx] = color;
    }
    private static void writePng(Path path, int[][] pixels) throws Exception {
        int height = pixels.length, width = pixels[0].length;
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        for (int[] row : pixels) {
            raw.write(0);
            for (int argb : row) { raw.write(argb >>> 16); raw.write(argb >>> 8); raw.write(argb); raw.write(argb >>> 24); }
        }
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (DeflaterOutputStream zip = new DeflaterOutputStream(compressed)) { raw.writeTo(zip); }
        Files.createDirectories(path.getParent());
        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(path))) {
            out.writeLong(0x89504E470D0A1A0AL);
            ByteArrayOutputStream header = new ByteArrayOutputStream(); DataOutputStream h = new DataOutputStream(header);
            h.writeInt(width); h.writeInt(height); h.writeByte(8); h.writeByte(6); h.writeByte(0); h.writeByte(0); h.writeByte(0);
            chunk(out, "IHDR", header.toByteArray()); chunk(out, "IDAT", compressed.toByteArray()); chunk(out, "IEND", new byte[0]);
        }
    }
    private static void chunk(DataOutputStream out, String type, byte[] data) throws Exception {
        byte[] name = type.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        out.writeInt(data.length); out.write(name); out.write(data);
        CRC32 crc = new CRC32(); crc.update(name); crc.update(data); out.writeInt((int) crc.getValue());
    }
}
