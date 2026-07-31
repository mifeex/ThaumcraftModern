package com.thaumcraftmodern.research;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ResearchConditionCodec {
    private static final String TYPE = "type";
    private static final String CONDITIONS = "conditions";

    private ResearchConditionCodec() {
    }

    public static ResearchCondition fromJson(JsonElement raw, String label) {
        if (raw == null || raw.isJsonNull()) {
            return ResearchCondition.ALWAYS;
        }
        JsonObject json = GsonHelper.convertToJsonObject(raw, label);
        String type = GsonHelper.getAsString(json, TYPE).toLowerCase(Locale.ROOT);
        return switch (type) {
            case "always" -> ResearchCondition.ALWAYS;
            case "all_of" -> new ResearchCondition.AllOf(
                    conditionList(json, label, false)
            );
            case "any_of" -> new ResearchCondition.AnyOf(
                    conditionList(json, label, true)
            );
            case "not" -> new ResearchCondition.Not(
                    fromJson(json.get("condition"), label + ".condition")
            );
            case "research_completed" -> new ResearchCondition.ResearchCompleted(
                    GsonHelper.getAsString(json, "id")
            );
            case "research_revealed" -> new ResearchCondition.ResearchRevealed(
                    GsonHelper.getAsString(json, "id")
            );
            case "scan" -> new ResearchCondition.ScanCompleted(
                    GsonHelper.getAsString(json, "id")
            );
            case "scan_aspect" -> new ResearchCondition.ScannedAspect(
                    GsonHelper.getAsString(json, "id")
            );
            case "aspect_known" -> new ResearchCondition.AspectKnown(
                    GsonHelper.getAsString(json, "id")
            );
            case "aspect_amount" -> new ResearchCondition.AspectAmount(
                    GsonHelper.getAsString(json, "id"),
                    GsonHelper.getAsInt(json, "minimum", 1)
            );
            case "warp" -> new ResearchCondition.WarpAtLeast(
                    ResearchCondition.WarpMeasure.valueOf(
                            GsonHelper.getAsString(json, "measure", "non_temporary")
                                    .toUpperCase(Locale.ROOT)
                    ),
                    GsonHelper.getAsInt(json, "minimum", 1)
            );
            case "criterion" -> new ResearchCondition.CriterionRecorded(
                    GsonHelper.getAsString(json, "id")
            );
            default -> throw new IllegalArgumentException(
                    "unknown research condition type '" + type + "' in " + label
            );
        };
    }

    public static CompoundTag toNbt(ResearchCondition condition) {
        CompoundTag result = new CompoundTag();
        if (condition instanceof ResearchCondition.Always) {
            result.putString(TYPE, "always");
        } else if (condition instanceof ResearchCondition.AllOf all) {
            result.putString(TYPE, "all_of");
            result.put(CONDITIONS, conditionTags(all.conditions()));
        } else if (condition instanceof ResearchCondition.AnyOf any) {
            result.putString(TYPE, "any_of");
            result.put(CONDITIONS, conditionTags(any.conditions()));
        } else if (condition instanceof ResearchCondition.Not not) {
            result.putString(TYPE, "not");
            result.put("condition", toNbt(not.condition()));
        } else if (condition instanceof ResearchCondition.ResearchCompleted completed) {
            result.putString(TYPE, "research_completed");
            result.putString("id", completed.researchId());
        } else if (condition instanceof ResearchCondition.ResearchRevealed revealed) {
            result.putString(TYPE, "research_revealed");
            result.putString("id", revealed.researchId());
        } else if (condition instanceof ResearchCondition.ScanCompleted scan) {
            result.putString(TYPE, "scan");
            result.putString("id", scan.scanId());
        } else if (condition instanceof ResearchCondition.ScannedAspect aspect) {
            result.putString(TYPE, "scan_aspect");
            result.putString("id", aspect.aspectId());
        } else if (condition instanceof ResearchCondition.AspectKnown aspect) {
            result.putString(TYPE, "aspect_known");
            result.putString("id", aspect.aspectId());
        } else if (condition instanceof ResearchCondition.AspectAmount aspect) {
            result.putString(TYPE, "aspect_amount");
            result.putString("id", aspect.aspectId());
            result.putInt("minimum", aspect.minimum());
        } else if (condition instanceof ResearchCondition.WarpAtLeast warp) {
            result.putString(TYPE, "warp");
            result.putString("measure", warp.measure().name());
            result.putInt("minimum", warp.minimum());
        } else if (condition instanceof ResearchCondition.CriterionRecorded criterion) {
            result.putString(TYPE, "criterion");
            result.putString("id", criterion.criterionId());
        } else {
            throw new IllegalArgumentException(
                    "unsupported research condition " + condition.getClass().getName()
            );
        }
        return result;
    }

    public static ResearchCondition fromNbt(CompoundTag tag) {
        String type = tag.getString(TYPE);
        return switch (type) {
            case "", "always" -> ResearchCondition.ALWAYS;
            case "all_of" -> new ResearchCondition.AllOf(readConditionTags(tag));
            case "any_of" -> new ResearchCondition.AnyOf(readConditionTags(tag));
            case "not" -> new ResearchCondition.Not(
                    fromNbt(tag.getCompound("condition"))
            );
            case "research_completed" -> new ResearchCondition.ResearchCompleted(
                    tag.getString("id")
            );
            case "research_revealed" -> new ResearchCondition.ResearchRevealed(
                    tag.getString("id")
            );
            case "scan" -> new ResearchCondition.ScanCompleted(tag.getString("id"));
            case "scan_aspect" -> new ResearchCondition.ScannedAspect(tag.getString("id"));
            case "aspect_known" -> new ResearchCondition.AspectKnown(tag.getString("id"));
            case "aspect_amount" -> new ResearchCondition.AspectAmount(
                    tag.getString("id"),
                    tag.getInt("minimum")
            );
            case "warp" -> new ResearchCondition.WarpAtLeast(
                    ResearchCondition.WarpMeasure.valueOf(tag.getString("measure")),
                    tag.getInt("minimum")
            );
            case "criterion" -> new ResearchCondition.CriterionRecorded(tag.getString("id"));
            default -> throw new IllegalArgumentException(
                    "unknown synchronized research condition type '" + type + "'"
            );
        };
    }

    private static List<ResearchCondition> conditionList(
            JsonObject json,
            String label,
            boolean requireNonEmpty
    ) {
        JsonArray array = GsonHelper.getAsJsonArray(json, CONDITIONS);
        if (requireNonEmpty && array.isEmpty()) {
            throw new IllegalArgumentException(label + ".conditions cannot be empty");
        }
        List<ResearchCondition> result = new ArrayList<>();
        for (int index = 0; index < array.size(); index++) {
            result.add(fromJson(array.get(index), label + ".conditions[" + index + "]"));
        }
        return result;
    }

    private static ListTag conditionTags(List<ResearchCondition> conditions) {
        ListTag result = new ListTag();
        conditions.stream().map(ResearchConditionCodec::toNbt).forEach(result::add);
        return result;
    }

    private static List<ResearchCondition> readConditionTags(CompoundTag tag) {
        List<ResearchCondition> result = new ArrayList<>();
        ListTag conditions = tag.getList(CONDITIONS, Tag.TAG_COMPOUND);
        for (Tag raw : conditions) {
            result.add(fromNbt((CompoundTag) raw));
        }
        return result;
    }
}
