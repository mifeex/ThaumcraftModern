package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomBreakParticleModelTest {
    private static final Path ASSETS = Path.of(
            "src/main/resources/assets/thaumcraftmodern"
    );

    @Test
    void auraNodeUsesItsOwnParticleSpriteInsteadOfAir() throws IOException {
        String blockState = read("blockstates/aura_node.json");
        String model = read("models/block/aura_node_particles.json");

        assertFalse(blockState.contains("minecraft:block/air"));
        assertTrue(blockState.contains(
                "thaumcraftmodern:block/aura_node_particles"
        ));
        assertTrue(model.contains(
                "thaumcraftmodern:block/aura_node_particle"
        ));
    }

    @Test
    void bothResearchTablePartsUseWoodParticlesInsteadOfAir()
            throws IOException {
        String blockState = read("blockstates/research_table.json");
        String model = read("models/block/research_table_particles.json");

        assertFalse(blockState.contains("minecraft:block/air"));
        assertTrue(blockState.contains(
                "thaumcraftmodern:block/research_table_particles"
        ));
        assertTrue(model.contains("thaumcraftmodern:block/woodplain"));
    }

    @Test
    void everyEldritchAltarPartUsesItsRenderedTextureForParticles()
            throws IOException {
        String blockState = read("blockstates/eldritch_altar_part.json");
        String altar = read(
                "models/block/eldritch_altar_cap_particles.json"
        );
        String cap = read(
                "models/block/eldritch_obelisk_cap_particles.json"
        );
        String side = read(
                "models/block/eldritch_obelisk_side_particles.json"
        );

        assertFalse(blockState.contains("minecraft:block/air"));
        assertTrue(blockState.contains("\"part=0\""));
        assertTrue(blockState.contains("\"part=1\""));
        assertTrue(blockState.contains("\"part=2\""));
        assertTrue(blockState.contains("\"part=3\""));
        assertTrue(blockState.contains("\"part=4\""));
        assertTrue(altar.contains(
                "thaumcraftmodern:block/obelisk_cap_altar"
        ));
        assertTrue(cap.contains("thaumcraftmodern:block/obelisk_cap"));
        assertTrue(side.contains("thaumcraftmodern:block/obelisk_side"));
        assertTrue(Files.exists(
                ASSETS.resolve("textures/block/obelisk_cap_altar.png")
        ));
        assertTrue(Files.exists(
                ASSETS.resolve("textures/block/obelisk_cap.png")
        ));
        assertTrue(Files.exists(
                ASSETS.resolve("textures/block/obelisk_side.png")
        ));
    }

    @Test
    void blockModelsUseAtlasTexturesForTheirBreakParticles()
            throws IOException {
        List<String> models = List.of(
                "infusion_pillar.json",
                "infusion_pillar_cap.json",
                "thaumatorium_lower.json",
                "advanced_alchemical_furnace_tank.json",
                "advanced_alchemical_furnace_core.json",
                "advanced_alchemical_furnace_upper.json"
        );

        for (String model : models) {
            String source = read("models/block/" + model);
            assertFalse(source.contains("thaumcraftmodern:models/"));
            assertTrue(source.contains("thaumcraftmodern:block/"));
        }
        assertTrue(Files.exists(ASSETS.resolve("textures/block/pillar.png")));
        assertTrue(Files.exists(ASSETS.resolve("textures/block/thaumatorium.png")));
        assertTrue(Files.exists(ASSETS.resolve("textures/block/alch_furnace.png")));
        assertTrue(Files.exists(ASSETS.resolve("textures/block/alch_furnace_on.png")));
        assertTrue(Files.exists(ASSETS.resolve("textures/block/alch_furnace_tank.png")));
    }

    private static String read(String relativePath) throws IOException {
        return Files.readString(ASSETS.resolve(relativePath));
    }
}
