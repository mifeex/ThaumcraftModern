package com.thaumcraftmodern.nodejar;

import com.thaumcraftmodern.aura.AuraNodeFactory;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeJarCodecLedgerTest {
    @Test
    void survivalJarRoundTripPreservesFullNodeState() {
        NodeJarData original = NodeJarFactory.captured(
                UUID.randomUUID(),
                AuraNodeFactory.newWorldNode()
        );
        NodeJarData restored = NodeJarCodec.decode(NodeJarCodec.encode(original));

        assertEquals(original.payloadId(), restored.payloadId());
        assertEquals(original.origin(), restored.origin());
        assertEquals(original.node().snapshot(), restored.node().snapshot());
    }

    @Test
    void creativeFactoryIsDeterministicAndUsesTheSameCodec() {
        NodeJarData first = NodeJarFactory.deterministicCreativeData();
        NodeJarData second = NodeJarFactory.deterministicCreativeData();

        assertEquals(NodeJarCodec.encode(first), NodeJarCodec.encode(second));
        NodeJarData data = NodeJarCodec.decode(NodeJarCodec.encode(first));
        assertEquals(NodeJarData.Origin.CREATIVE_TEMPLATE, data.origin());
        assertEquals(
                AuraNodeFactory.SAFE_PRIMAL_VIS,
                data.node().current(com.thaumcraftmodern.aura.PrimalAspect.AER)
        );
    }

    @Test
    void ledgerRejectsStaleCopiedPayloadAndPersistsPlacement() {
        NodeJarData data = NodeJarFactory.captured(
                UUID.randomUUID(),
                AuraNodeFactory.newWorldNode()
        );
        NodeJarLedger ledger = new NodeJarLedger();
        assertTrue(ledger.registerCaptured(data));
        assertFalse(ledger.registerCaptured(data));
        assertTrue(ledger.claimPlacement(data, "minecraft:overworld@1,64,1"));
        assertFalse(ledger.claimPlacement(data, "minecraft:overworld@2,64,2"));

        NodeJarLedger restored = NodeJarLedger.deserialize(ledger.serialize());
        assertEquals(NodeJarLedger.Status.PLACED, restored.status(data.payloadId()));
        assertFalse(restored.claimPlacement(data, "minecraft:overworld@3,64,3"));
        assertTrue(restored.returnToJar(data, "minecraft:overworld@1,64,1"));
        assertEquals(NodeJarLedger.Status.JARRED, restored.status(data.payloadId()));
    }

    @Test
    void legacyCapturedJarCanDropWhenOldLedgerStillSaysJarred() {
        NodeJarData data = NodeJarFactory.captured(
                UUID.randomUUID(),
                AuraNodeFactory.newWorldNode()
        );
        NodeJarLedger ledger = new NodeJarLedger();
        assertTrue(ledger.registerCaptured(data));

        assertTrue(ledger.returnToJarOrRecoverLegacyCapture(
                data,
                "minecraft:overworld@1,64,1"
        ));
        assertEquals(NodeJarLedger.Status.JARRED, ledger.status(data.payloadId()));

        NodeJarData mismatched = NodeJarFactory.captured(
                data.payloadId(),
                AuraNodeFactory.newWorldNode()
        );
        assertFalse(ledger.returnToJarOrRecoverLegacyCapture(
                mismatched,
                "minecraft:overworld@1,64,1"
        ));
    }

    @Test
    void corruptVersionsAreRejectedInsteadOfMintingANewNode() {
        CompoundTag tag = NodeJarCodec.encode(
                NodeJarFactory.deterministicCreativeData()
        );
        tag.putInt("version", 42);
        assertThrows(IllegalArgumentException.class, () -> NodeJarCodec.decode(tag));

        CompoundTag ledger = new NodeJarLedger().serialize();
        ledger.putInt("version", 42);
        assertThrows(
                IllegalArgumentException.class,
                () -> NodeJarLedger.deserialize(ledger)
        );
    }
}
