import java.io.ByteArrayInputStream;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Path;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.stb.STBVorbis;

public final class DecodeOgg {
    private DecodeOgg() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("usage: DecodeOgg input.ogg output.wav");
        }
        IntBuffer channels = BufferUtils.createIntBuffer(1);
        IntBuffer sampleRate = BufferUtils.createIntBuffer(1);
        ShortBuffer samples = STBVorbis.stb_vorbis_decode_filename(args[0], channels, sampleRate);
        if (samples == null) {
            throw new IllegalStateException("Unable to decode " + args[0]);
        }
        try {
            int channelCount = channels.get(0);
            int rate = sampleRate.get(0);
            byte[] pcm = new byte[samples.remaining() * 2];
            for (int index = 0; samples.hasRemaining(); index += 2) {
                short sample = samples.get();
                pcm[index] = (byte) sample;
                pcm[index + 1] = (byte) (sample >>> 8);
            }
            AudioFormat format = new AudioFormat(rate, 16, channelCount, true, false);
            try (AudioInputStream stream = new AudioInputStream(
                    new ByteArrayInputStream(pcm), format, pcm.length / (2L * channelCount))) {
                AudioSystem.write(stream, AudioFileFormat.Type.WAVE, Path.of(args[1]).toFile());
            }
        } finally {
            MemoryUtil.memFree(samples);
        }
    }
}
