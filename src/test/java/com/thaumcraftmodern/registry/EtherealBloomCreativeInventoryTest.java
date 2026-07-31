package com.thaumcraftmodern.registry;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EtherealBloomCreativeInventoryTest {
    @Test
    void bloomHasBlockItemAndProminentCreativeTabEntry() throws Exception {
        String items = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/registry/ModItems.java"
        ));
        String tab = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/registry/ModCreativeTabs.java"
        ));

        assertTrue(items.contains(
                "blockItem(\"ethereal_bloom\", ModBlocks.ETHEREAL_BLOOM)"
        ));
        int thaumometer = tab.indexOf(
                "output.accept(ModItems.THAUMOMETER.get());"
        );
        int bloom = tab.indexOf(
                "output.accept(ModItems.ETHEREAL_BLOOM.get());"
        );
        int sanityChecker = tab.indexOf(
                "output.accept(ModItems.SANITY_CHECKER.get());"
        );
        assertTrue(
                thaumometer >= 0
                        && bloom > thaumometer
                        && sanityChecker > bloom,
                "Ethereal Bloom must stay visible near the start of the "
                        + "Thaumcraft creative tab"
        );
    }
}
