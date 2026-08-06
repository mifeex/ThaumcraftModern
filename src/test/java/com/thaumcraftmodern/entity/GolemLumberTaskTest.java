package com.thaumcraftmodern.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

class GolemLumberTaskTest {
    @Test
    void ordersAnArbitrarilyTallRememberedTreeFromBottomToTop() {
        BlockPos base = new BlockPos(4, 20, -3);
        List<BlockPos> logs = new ArrayList<>();
        for (int height = 0; height <= 100; height++) {
            logs.add(base.above(height));
        }
        logs.add(base.offset(1, 50, 0));
        logs.add(base.offset(-1, 50, 0));
        Collections.reverse(logs);

        List<BlockPos> ordered = GolemLumberTask.orderedLogs(base, logs);

        assertEquals(103, ordered.size());
        assertEquals(base, ordered.get(0));
        assertEquals(120, ordered.get(ordered.size() - 1).getY());
        for (int index = 1; index < ordered.size(); index++) {
            assertTrue(ordered.get(index - 1).getY() <= ordered.get(index).getY());
        }
    }

    @Test
    void lumberGoalUsesTaggedTreeDiscoveryAndPersistsTheWholeTask() throws Exception {
        String task = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/entity/GolemLumberTask.java"));
        String goals = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/entity/GolemCoreGoals.java"));
        String entity = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/entity/ClassicGolemEntity.java"));

        assertTrue(task.contains("BlockTags.LOGS"));
        assertTrue(task.contains("BlockTags.LEAVES"));
        assertTrue(task.contains(
                "Comparator.comparingInt((BlockPos pos) -> pos.getY())"));
        assertTrue(goals.contains("GolemLumberTask.discover("));
        assertTrue(goals.contains("moveNearRememberedTree()"));
        assertTrue(goals.contains("golem.forgetLumberLog(breakTarget)"));
        assertFalse(goals.contains(
                "findBlock(Math.max(1, golem.workRange() / 3)"));
        assertTrue(entity.contains("putLong(\"LumberTreeBase\""));
        assertTrue(entity.contains("putLongArray(\"LumberTreeLogs\""));
        assertTrue(entity.contains("getLongArray(\"LumberTreeLogs\")"));
    }
}
