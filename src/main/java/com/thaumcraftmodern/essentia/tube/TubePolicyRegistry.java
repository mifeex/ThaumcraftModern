package com.thaumcraftmodern.essentia.tube;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TubePolicyRegistry {
    public static final ResourceLocation PLAIN = id("plain");
    public static final ResourceLocation FILTERED = id("filtered");
    public static final ResourceLocation RESTRICTED = id("restricted");
    public static final ResourceLocation ONE_WAY = id("one_way");
    public static final ResourceLocation VALVE = id("valve");

    private static final Map<ResourceLocation, TubePolicy> POLICIES =
            new ConcurrentHashMap<>();

    static {
        register(PLAIN, new TubePolicy(false, false, false, false));
        register(FILTERED, new TubePolicy(true, false, false, false));
        register(RESTRICTED, new TubePolicy(false, true, false, false));
        register(ONE_WAY, new TubePolicy(false, false, true, false));
        register(VALVE, new TubePolicy(false, false, false, true));
    }

    private TubePolicyRegistry() {
    }

    public static void register(ResourceLocation id, TubePolicy policy) {
        TubePolicy previous = POLICIES.putIfAbsent(id, policy);
        if (previous != null && !previous.equals(policy)) {
            throw new IllegalStateException("duplicate tube policy: " + id);
        }
    }

    public static TubePolicy require(ResourceLocation id) {
        TubePolicy policy = POLICIES.get(id);
        if (policy == null) {
            throw new IllegalArgumentException("unknown tube policy: " + id);
        }
        return policy;
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(ThaumcraftModern.MOD_ID, path);
    }
}
