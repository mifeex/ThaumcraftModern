package com.thaumcraftmodern.client;

import com.thaumcraftmodern.scan.AspectReward;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientNodeAspectMaskTest {
    @Test
    void unknownNodeAspectKeepsAmountButUsesMaskedPresentation() {
        List<ClientNodeAspectMask.DisplayAspect> result =
                ClientNodeAspectMask.apply(
                        List.of(
                                new AspectReward("aer", 23),
                                new AspectReward("alienis", 11)
                        ),
                        Set.of("aer")::contains
                );

        assertEquals("aer", result.get(0).aspectId());
        assertEquals(23, result.get(0).amount());
        assertTrue(result.get(0).known());
        assertEquals("alienis", result.get(1).aspectId());
        assertEquals(11, result.get(1).amount());
        assertFalse(result.get(1).known());
    }
}
