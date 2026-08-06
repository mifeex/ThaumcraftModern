package com.thaumcraftmodern.item;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class WingedMantleArmorFidelityTest {
    @Test
    void chestPieceUsesForgeElytraContract() throws Exception {
        String item = source("com/thaumcraftmodern/item/WingedMantleArmorItem.java");
        assertTrue(item.contains("canElytraFly"));
        assertTrue(item.contains("elytraFlightTick"));
        assertTrue(item.contains("(flightTicks + 1) % 20 == 0"));
        assertTrue(item.contains("EquipmentSlot.CHEST"));
    }

    @Test
    void modelKeepsApprovedArmorMantleAndWingDetails() throws Exception {
        String model = source("com/thaumcraftmodern/client/render/WingedMantleArmorModel.java");
        for (String part : new String[]{"hood1", "hood2", "hood3", "hood4", "pauldron_top",
                "pauldron_stud", "bracer",
                "chest_yoke", "chest_strap_top", "chest_strap_low",
                "chest_focus", "focus_core", "focus_crown",
                "praetor_collar_front", "praetor_collar_back",
                "praetor_collar_left", "praetor_collar_right",
                "praetor_chestplate", "praetor_chestcloth_left",
                "praetor_chestcloth_right", "praetor_backplate",
                "praetor_belt_left", "praetor_belt_right", "raised_chest_focus",
                "back_focus", "back_focus_core",
                "left_tail", "right_tail", "back_mantle", "elytra_bridge",
                "book", "book_clasp", "scroll", "pouch",
                "left_wing", "right_wing", "glyph", "upper_stud", "middle_stud"}) {
            assertTrue(model.contains("\"" + part + "\""), part);
        }
        assertTrue(model.contains("LayerDefinition.create(mesh, 4096, 4096)"));
        assertTrue(model.contains("-4.5F, -9.0F, -4.6F, 9.0F, 9.0F, 9.0F"));
        assertTrue(model.contains("-0.2268928F"));
        assertTrue(model.contains("-0.3490659F"));
        assertTrue(model.contains("-0.5759587F"));
        assertTrue(model.contains("-4.5F, -1.5F, -3.0F, 9.0F, 4.0F, 1.0F"));
        assertTrue(model.contains("-4.0F, 1.0F, -3.8F, 8.0F, 7.0F, 2.0F"));
        assertTrue(model.contains("-2.5F, 3.0F, -4.8F, 5.0F, 5.0F, 1.0F"));
        assertFalse(model.contains("praetor_cloak"));
        assertTrue(model.contains("WingedMantleElytraLayer"));
        assertFalse(model.contains("texOffs(22, 128)"));
        assertTrue(model.contains("mirror ? -1.5F : -3.5F"));
        assertTrue(model.contains("5.0F, 13.0F, 5.0F"));
        assertFalse(model.contains("fitArmsToBody"));
        assertTrue(model.contains("configureForSlot"));
        assertTrue(model.contains("rightLeg.getChild(\"greave\").visible = true"));
        assertTrue(model.contains("rightLeg.getChild(\"boot\").visible = true"));
        assertTrue(model.contains("body.getChild(\"left_tail\").visible = false"));
        assertTrue(model.contains("empty(arm, \"bracer\""));
        assertTrue(model.contains("0.4363323F"));
        assertTrue(model.contains("238, 37"));
        assertTrue(model.contains("empty(body, \"buckle\""));
        assertTrue(model.contains("empty(body, \"scroll\""));
        String extensions = source("com/thaumcraftmodern/client/render/WingedMantleClientExtensions.java");
        assertFalse(extensions.contains("model.animateWings"));
        assertFalse(extensions.contains("fitArmsToBody"));
        assertTrue(extensions.contains("EnumMap<EquipmentSlot"));
        assertTrue(extensions.contains("model.configureForSlot(slot)"));
        String layer = source("com/thaumcraftmodern/client/render/WingedMantleElytraLayer.java");
        assertTrue(layer.contains("new ElytraModel<>(root)"));
        assertTrue(layer.contains("ModelLayers.ELYTRA"));
        assertTrue(layer.contains("RenderType.armorCutoutNoCull(TEXTURE)"));
        assertTrue(layer.contains("limbSwing * 0.6662F"));
        assertTrue(layer.contains("WingedMantleArmorItem"));
        String generator = Files.readString(Path.of("tools/generate_winged_mantle_textures.py"));
        assertTrue(generator.contains("cultist_robe_armor.png"));
        assertTrue(generator.contains("paste_recolored_cultist_hood"));
        assertTrue(generator.contains("light front to dark tail"));
        assertTrue(generator.contains("paint_praetor_gorget"));
        assertTrue(generator.contains("paint_raised_focus"));
        assertTrue(generator.contains("paste_recolored_praetor_armor"));
        assertTrue(generator.contains("cultist_leader_armor.png"));
        assertTrue(generator.contains("recolored.resize((128 * SCALE, 64 * SCALE)"));
    }

    @Test
    void exactPixelAssetsAndAllFourItemsArePackaged() throws Exception {
        var armor = ImageIO.read(Path.of("src/main/resources/assets/thaumcraftmodern/textures/entity/models/winged_mantle_armor.png").toFile());
        assertEquals(4096, armor.getWidth());
        assertEquals(4096, armor.getHeight());
        var elytra = ImageIO.read(Path.of(
                "src/main/resources/assets/thaumcraftmodern/textures/entity/winged_mantle_elytra.png").toFile());
        assertEquals(64, elytra.getWidth());
        assertEquals(32, elytra.getHeight());
        var colors = new HashSet<Integer>();
        boolean hasGold = false;
        boolean hasEmerald = false;
        boolean hasLavender = false;
        for (int y = 0; y < armor.getHeight(); y++) {
            for (int x = 0; x < armor.getWidth(); x++) {
                int argb = armor.getRGB(x, y);
                Color color = new Color(argb, true);
                if (color.getAlpha() == 0) {
                    continue;
                }
                colors.add(argb);
                hasGold |= color.getRed() > 170 && color.getGreen() > 90
                        && color.getBlue() < 70;
                hasEmerald |= color.getGreen() > 110
                        && color.getGreen() > color.getRed() * 2;
                hasLavender |= color.getBlue() > 130 && color.getRed() > 90;
            }
        }
        assertTrue(colors.size() >= 14, "armor atlas needs readable material separation");
        assertTrue(hasGold, "armor atlas needs gold trim");
        assertTrue(hasEmerald, "armor atlas needs an emerald focus");
        assertTrue(hasLavender, "armor atlas needs visible arcane glyphs");
        for (String item : new String[]{"hood", "chestplate", "leggings", "boots"}) {
            Path texture = Path.of("src/main/resources/assets/thaumcraftmodern/textures/item/winged_mantle_" + item + ".png");
            Path model = Path.of("src/main/resources/assets/thaumcraftmodern/models/item/winged_mantle_" + item + ".json");
            assertTrue(Files.isRegularFile(texture), texture.toString());
            assertTrue(Files.isRegularFile(model), model.toString());
        }
        String registry = source("com/thaumcraftmodern/registry/ModItems.java");
        assertTrue(registry.contains("WINGED_MANTLE_HOOD"));
        assertTrue(registry.contains("WINGED_MANTLE_CHESTPLATE"));
        assertTrue(registry.contains("WINGED_MANTLE_LEGGINGS"));
        assertTrue(registry.contains("WINGED_MANTLE_BOOTS"));
    }

    private static String source(String relative) throws Exception {
        return Files.readString(Path.of("src/main/java").resolve(relative));
    }
}
