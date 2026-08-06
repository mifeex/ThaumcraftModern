import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;

public final class GenerateConvertedVillagerGarments {
    private static final int SIZE = 64;

    public static void main(String[] args) throws Exception {
        int[][] pixels = new int[SIZE][SIZE];
        fill(pixels, 0, 0, 64, 64, 0xFF575957);
        for (int y = 1; y < 64; y += 4) {
            fill(pixels, 0, y, 64, 2, 0xFF92958F);
        }
        writePng(Path.of(args[0]), pixels);
    }

    private static void fill(int[][] p, int x, int y, int w, int h, int argb) {
        for (int yy = y; yy < y + h; yy++)
            for (int xx = x; xx < x + w; xx++) p[yy][xx] = argb;
    }

    private static void writePng(Path path, int[][] pixels) throws Exception {
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        for (int[] row : pixels) {
            raw.write(0);
            for (int argb : row) {
                raw.write(argb >>> 16); raw.write(argb >>> 8);
                raw.write(argb); raw.write(argb >>> 24);
            }
        }
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (DeflaterOutputStream zip = new DeflaterOutputStream(compressed)) {
            raw.writeTo(zip);
        }
        Files.createDirectories(path.getParent());
        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(path))) {
            out.writeLong(0x89504E470D0A1A0AL);
            ByteArrayOutputStream header = new ByteArrayOutputStream();
            DataOutputStream h = new DataOutputStream(header);
            h.writeInt(SIZE); h.writeInt(SIZE); h.writeByte(8); h.writeByte(6);
            h.writeByte(0); h.writeByte(0); h.writeByte(0);
            chunk(out, "IHDR", header.toByteArray());
            chunk(out, "IDAT", compressed.toByteArray());
            chunk(out, "IEND", new byte[0]);
        }
    }

    private static void chunk(DataOutputStream out, String type, byte[] data) throws Exception {
        byte[] name = type.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        out.writeInt(data.length); out.write(name); out.write(data);
        CRC32 crc = new CRC32(); crc.update(name); crc.update(data);
        out.writeInt((int) crc.getValue());
    }
}
