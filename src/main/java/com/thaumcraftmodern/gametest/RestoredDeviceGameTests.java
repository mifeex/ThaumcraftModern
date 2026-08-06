package com.thaumcraftmodern.gametest;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aura.AuraNodeModifier;
import com.thaumcraftmodern.aura.AuraNodeState;
import com.thaumcraftmodern.aura.AuraNodeType;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.visnet.EnergizedAuraNodeBlockEntity;
import com.thaumcraftmodern.visnet.VisNetworkNodeBlockEntity;
import com.thaumcraftmodern.visnet.VisRelayBlockEntity;
import com.thaumcraftmodern.world.block.entity.ArcaneDoorBlockEntity;
import com.thaumcraftmodern.world.block.entity.BrainJarBlockEntity;
import com.thaumcraftmodern.world.block.entity.FluxScrubberBlockEntity;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ThaumcraftModern.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RestoredDeviceGameTests {
    private RestoredDeviceGameTests(){}
    @GameTest(template="empty",batch="restoredDevices")
    public static void fluxScrubberConsumesRealAerThroughSeparateRelay(GameTestHelper helper){
        BlockPos sourcePos=new BlockPos(1,2,1),relayPos=new BlockPos(3,2,1),scrubberPos=new BlockPos(3,1,1);
        helper.setBlock(sourcePos,ModBlocks.ENERGIZED_AURA_NODE.get());helper.setBlock(relayPos,ModBlocks.VIS_RELAY.get());helper.setBlock(scrubberPos,ModBlocks.FLUX_SCRUBBER.get());
        EnergizedAuraNodeBlockEntity source=(EnergizedAuraNodeBlockEntity)helper.getBlockEntity(sourcePos);source.initialize(AuraNodeState.withAspects(UUID.randomUUID(),AuraNodeType.NORMAL,AuraNodeModifier.NORMAL,Map.of("aer",100),Map.of("aer",100),0));
        VisRelayBlockEntity relay=(VisRelayBlockEntity)helper.getBlockEntity(relayPos);var level=helper.getLevel();
        VisNetworkNodeBlockEntity.serverTick(level,helper.absolutePos(sourcePos),level.getBlockState(helper.absolutePos(sourcePos)),source);
        VisNetworkNodeBlockEntity.serverTick(level,helper.absolutePos(relayPos),level.getBlockState(helper.absolutePos(relayPos)),relay);
        FluxScrubberBlockEntity scrubber=(FluxScrubberBlockEntity)helper.getBlockEntity(scrubberPos);FluxScrubberBlockEntity.serverTick(level,helper.absolutePos(scrubberPos),level.getBlockState(helper.absolutePos(scrubberPos)),scrubber);
        helper.assertTrue(scrubber.power()>=5,"Flux scrubber did not consume Aer from the separate relay chain");helper.succeed();
    }
    @GameTest(template="empty",batch="restoredDevices")
    public static void brainJarStoresAndReleasesExperience(GameTestHelper helper){
        BlockPos pos=new BlockPos(2,1,2);helper.setBlock(pos,ModBlocks.BRAIN_JAR.get());BrainJarBlockEntity jar=(BrainJarBlockEntity)helper.getBlockEntity(pos);BlockPos absolute=helper.absolutePos(pos);
        helper.getLevel().addFreshEntity(new ExperienceOrb(helper.getLevel(),absolute.getX()+.5,absolute.getY()+.5,absolute.getZ()+.5,7));BrainJarBlockEntity.serverTick(helper.getLevel(),absolute,helper.getLevel().getBlockState(absolute),jar);
        helper.assertTrue(jar.storedExperience()==7&&jar.releaseExperience()==7&&jar.storedExperience()==0,"Brain jar XP cycle differs from TC4");helper.succeed();
    }
    @GameTest(template="empty",batch="restoredDevices")
    public static void arcaneDoorPersistsOwnerAndTieredAccess(GameTestHelper helper){
        BlockPos pos=new BlockPos(2,1,2);helper.setBlock(pos,ModBlocks.ARCANE_DOOR.get().defaultBlockState());ArcaneDoorBlockEntity door=(ArcaneDoorBlockEntity)helper.getBlockEntity(pos);door.setOwner("owner");
        helper.assertTrue(door.canOpen("owner")&&!door.canOpen("guest"),"Arcane door ignored its owner");door.grant("guest",false);door.grant("delegate",true);
        helper.assertTrue(door.canOpen("guest")&&!door.canMintIron("guest")&&door.canMintIron("delegate"),"Arcane door key tiers differ from TC4");helper.succeed();
    }
}
