package com.thaumcraftmodern.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;

/**
 * Exact static block payload extracted from TC4 4.2.3.5 WorldGenMound.
 *
 * <p>A space means that the old generator left the cell untouched, while a
 * dot is an explicit air placement. Stair digits are the original metadata
 * values 0..7.</p>
 */
final class AncientMoundBlueprint {
    static final int WIDTH = 19;
    static final int DEPTH = 19;
    static final int HEIGHT = 16;
    static final int STATIC_PLACEMENT_COUNT = 2441;

    private static final String[][] LAYERS = {
            {
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "      CCCCCCC      ",
                    "      CMCCMCC      ",
                    "      CCCMMCC      ",
                    "      CCCCCCC      ",
                    "      CMCMCMC      ",
                    "      CMCMMCC      ",
                    "      CCCCCCC      ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   "
            },
            {
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "      MMCMCCM      ",
                    "      C.. .CC      ",
                    "      C....CC      ",
                    "      MT.. CM      ",
                    "      M....CC      ",
                    "      C.. .CM      ",
                    "      CCMMCCM      ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   "
            },
            {
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "      CCCCCCC      ",
                    "      C....CC      ",
                    "      C....CC      ",
                    "      CT...CC      ",
                    "      C....MC      ",
                    "      C....CC      ",
                    "      CCCCCCC      ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   "
            },
            {
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "      CCCCCCC      ",
                    "      C....CC      ",
                    "      M....CC      ",
                    "      CT...MC      ",
                    "      C....CC      ",
                    "      C....CC      ",
                    "      CCCMCCC      ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   "
            },
            {
                    "                   ",
                    "                   ",
                    "                   ",
                    "   CCCCCCCCCCCCC   ",
                    "   CCCCCCCMCCCCC   ",
                    "   CCCCCMCCCCCCC   ",
                    "   CCCC3333CCCCC   ",
                    "   CCC1....0CCCC   ",
                    "   CCC1....0MMCC   ",
                    "   CCCCT...0CCCC   ",
                    "   CCM1....0CCCC   ",
                    "   CCC1....0CCCC   ",
                    "   CCCC2222CCCCC   ",
                    "   CCMCCCCCCMCCC   ",
                    "   CCCCCCCCCCCMC   ",
                    "   CCCCCCCCCCCCC   ",
                    "                   ",
                    "                   ",
                    "                   "
            },
            {
                    "                   ",
                    "                   ",
                    "                   ",
                    "   CCCCCCCCCCCCC   ",
                    "   C .........CC   ",
                    "   C..........CC   ",
                    "   C..M....C..CC   ",
                    "   M..........CC   ",
                    "   M..........3C   ",
                    "   C...........C   ",
                    "   C...........C   ",
                    "   C...........C   ",
                    "   C..C....C...C   ",
                    "   C...........C   ",
                    "   C ..........C   ",
                    "   CCCMCCCCCCCCC   ",
                    "                   ",
                    "                   ",
                    "                   "
            },
            {
                    "                   ",
                    "                   ",
                    "                   ",
                    "   CCMCCMCCMCCCC   ",
                    "   C..........CC   ",
                    "   C..........MC   ",
                    "   C..M....M..CC   ",
                    "   C..........3C   ",
                    "   C...........C   ",
                    "   C...........C   ",
                    "   C...........M   ",
                    "   C...........C   ",
                    "   C..C....C...C   ",
                    "   M...........C   ",
                    "   C...........C   ",
                    "   CCCCMCCMCCCMC   ",
                    "                   ",
                    "                   ",
                    "                   "
            },
            {
                    "                   ",
                    "                   ",
                    "                   ",
                    "   CMCCCCCCCCCCC   ",
                    "   C..........CC   ",
                    "   C..........CC   ",
                    "   C..C....C..3C   ",
                    "   C...........C   ",
                    "   C...........C   ",
                    "   C...........C   ",
                    "   C...........C   ",
                    "   M...........M   ",
                    "   C..C....M...C   ",
                    "   C...........C   ",
                    "   C...........C   ",
                    "   CCCCCCCCCCCCC   ",
                    "                   ",
                    "                   ",
                    "                   "
            },
            {
                    "    DDDDDDDDDDD    ",
                    "   DDDDDDDDDDDDD   ",
                    "  DDDDDDDDDDDDDDD  ",
                    " DDCCCCCCCCCCCCCDD ",
                    "DDDCMMCCCCMCMCCCDDD",
                    "DDDCMCCCMCCCCC3CDDD",
                    "DDDCCCC7777CCC.CDDD",
                    "DDCMCC5....4MC.CDDD",
                    "CCCCCC5....4MC.CDDD",
                    "CCMCMC5....4CC.CDDD",
                    "CCCCMC5....4CCCCDDD",
                    "DDCCCC5....4CMMCDDD",
                    "DDDCCCM6666CCCMCDDD",
                    "DDDCCMCCCMCCCCCCDDD",
                    "DDDCCCCMCCCCMMCCDDD",
                    " DDCCCCCCCCCCCCCDD ",
                    "  DDDDDDDDDDDDDDD  ",
                    "   DDDDDDDDDDDDD   ",
                    "    DDDDDDDDDDD    "
            },
            {
                    "    GGGGGDDGGGG    ",
                    "   GDDDDDDDDDDDG   ",
                    "  GDDDDDDDDDDDDDG  ",
                    " GDCCCCCCCCCCCCCDG ",
                    "GDDC...........CDDG",
                    "GDDC...........CDDG",
                    "GDDC..M....C...CDDG",
                    "GDDC...........MDDD",
                    "0CCC...........CDDD",
                    "...I...........CDDG",
                    "0CCC...........CDDG",
                    "GDDC...........CDDG",
                    "GDDM..C....M...CDDG",
                    "GDDC...........CDDG",
                    "GDDC...........CDDG",
                    " GDCCCCCCMCCCCCCDG ",
                    "  GDDDDDDDDDDDDDG  ",
                    "   GDDDDDDDDDDDD   ",
                    "    GGGGGGGGGGG    "
            },
            {
                    "         GG  g     ",
                    "    GGGDGDDDGGG    ",
                    "   GDDDDDDDDDDDD   ",
                    " gGCCCCCMMCCCMCCD  ",
                    "gGDM...........CDG ",
                    "gGDM...........MDGg",
                    " DDC..C....C...CDG ",
                    " DDM...........CDGG",
                    ".0CC...........MDGG",
                    "...I...........CDG ",
                    ".0MC...........MDG ",
                    " GDC...........MDD ",
                    " GDC..M....M...CDGg",
                    " GDC...........CDGg",
                    " GDC.......... MDGg",
                    " gGCMCCCMMCMCCCCG  ",
                    "   GDDDDDDDDDDDD   ",
                    "    GGGDDGGDDDGG   ",
                    "         gg        "
            },
            {
                    "                   ",
                    "       G GGG gg    ",
                    "    GDGGGDGGDDGG   ",
                    "  gGDCCMCCCMCCDDG  ",
                    " gGDC.........CDD  ",
                    " gGC..6....6...MD  ",
                    " GDC.4C5..4C5..CG  ",
                    " GDC..7... 7...CGg ",
                    " .CC...........CG  ",
                    " .BM...........CG  ",
                    " .MC...........MG  ",
                    "  DC..6....6...CDG ",
                    " gGC.4C5..4C5..CD  ",
                    " gGC..7....7...CG  ",
                    " gGDM.........CDG  ",
                    "   GDCMCCCCCCCGG   ",
                    "    GGGGGDDGGGGG   ",
                    "      gGG gGGG     ",
                    "                   "
            },
            {
                    "                   ",
                    "                   ",
                    "     G g Gg GG g   ",
                    "    GGGGGGGGGGGGg  ",
                    "   GGCCCCCCCCCGGG  ",
                    "   GCCCCCCCCCCCGG  ",
                    "  GGCCMCCCCCMCCGg  ",
                    "  GGCCCCMMCCCCCGg  ",
                    "  GGCCCCCCCCCCCGg  ",
                    "  GGCCMMCCMCCCCD   ",
                    "  GGCCCCCCCCCCCDg  ",
                    "  GGCCCCCCCCMCCGG  ",
                    "   GCMCCCCMCCCCGG  ",
                    "   GCCCCCCCCCCCG   ",
                    "  gGDCCCCCCCCCGG   ",
                    "    GDGGGGGGGGgg   ",
                    "         GG   gg   ",
                    "             g     ",
                    "                   "
            },
            {
                    "                   ",
                    "                   ",
                    "     g   g         ",
                    "    gg   ggg   g   ",
                    "    gGGGGGGGGG g   ",
                    "    GGGGGGDGGGG    ",
                    "    GGGGDDDDGGG    ",
                    "   gGGGDDDGDDGGg   ",
                    "   gGGGDDDDDDGG    ",
                    "    GGGDDDDDGGGG   ",
                    "    GGGGDDDDDGGG   ",
                    "    GGGGGDDGGGG    ",
                    "    GGGGGGGGGGGg   ",
                    "    GGGGGGGGGGGg   ",
                    "    GGGGGGGGGG g   ",
                    "     G             ",
                    "                   ",
                    "                   ",
                    "                   "
            },
            {
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "         g         ",
                    "          G        ",
                    "    g   GGGG       ",
                    "    g  GGG GG      ",
                    "       GGGGGG      ",
                    "    g  GGGGG       ",
                    "    g   GGGGG      ",
                    "     g   GG ggg    ",
                    "    ggggg ggggg    ",
                    "    ggg gg gggg    ",
                    "    ggggg   g      ",
                    "     g             ",
                    "                   ",
                    "                   ",
                    "                   "
            },
            {
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "          g        ",
                    "                   ",
                    "        gggg       ",
                    "        gggg       ",
                    "         gggg      ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   ",
                    "                   "
            }
    };

    private AncientMoundBlueprint() {
    }

    static void place(WorldGenLevel level, BlockPos origin) {
        for (int y = 0; y < LAYERS.length; y++) {
            for (int z = 0; z < DEPTH; z++) {
                String row = LAYERS[y][z];
                for (int x = 0; x < WIDTH; x++) {
                    char symbol = row.charAt(x);
                    if (symbol == ' ') {
                        continue;
                    }
                    level.setBlock(
                            origin.offset(x, y, z),
                            state(symbol),
                            2
                    );
                }
            }
        }
        /*
         * Worldgen uses update flag 2. Just like the explicit pane states in
         * the wizard tower and banker home, persist the horizontal iron-bar
         * connections after every neighbour exists. Without this second pass
         * the mound gate is saved as two skinny isolated posts.
         */
        for (int y = 0; y < LAYERS.length; y++) {
            for (int z = 0; z < DEPTH; z++) {
                String row = LAYERS[y][z];
                for (int x = 0; x < WIDTH; x++) {
                    if (row.charAt(x) != 'I') {
                        continue;
                    }
                    BlockPos position = origin.offset(x, y, z);
                    level.setBlock(
                            position,
                            connectedIronBars(level, position),
                            2
                    );
                }
            }
        }
    }

    private static BlockState connectedIronBars(
            WorldGenLevel level,
            BlockPos position
    ) {
        return Blocks.IRON_BARS.defaultBlockState()
                .setValue(
                        BlockStateProperties.NORTH,
                        connectsToBars(level, position, Direction.NORTH)
                )
                .setValue(
                        BlockStateProperties.EAST,
                        connectsToBars(level, position, Direction.EAST)
                )
                .setValue(
                        BlockStateProperties.SOUTH,
                        connectsToBars(level, position, Direction.SOUTH)
                )
                .setValue(
                        BlockStateProperties.WEST,
                        connectsToBars(level, position, Direction.WEST)
                );
    }

    private static boolean connectsToBars(
            WorldGenLevel level,
            BlockPos position,
            Direction direction
    ) {
        BlockPos neighbourPosition = position.relative(direction);
        BlockState neighbour = level.getBlockState(neighbourPosition);
        return neighbour.is(Blocks.IRON_BARS)
                || neighbour.isFaceSturdy(
                        level,
                        neighbourPosition,
                        direction.getOpposite()
                );
    }

    static int encodedPlacementCount() {
        int count = 0;
        for (String[] layer : LAYERS) {
            for (String row : layer) {
                for (int index = 0; index < row.length(); index++) {
                    if (row.charAt(index) != ' ') {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    static boolean dimensionsAreValid() {
        if (LAYERS.length != HEIGHT) {
            return false;
        }
        for (String[] layer : LAYERS) {
            if (layer.length != DEPTH) {
                return false;
            }
            for (String row : layer) {
                if (row.length() != WIDTH) {
                    return false;
                }
            }
        }
        return true;
    }

    private static BlockState state(char symbol) {
        return switch (symbol) {
            case '.' -> Blocks.AIR.defaultBlockState();
            case 'D' -> Blocks.DIRT.defaultBlockState();
            case 'C' -> Blocks.COBBLESTONE.defaultBlockState();
            case 'G' -> Blocks.GRASS_BLOCK.defaultBlockState();
            case 'M' -> Blocks.MOSSY_COBBLESTONE.defaultBlockState();
            case 'B' -> Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
            case 'I' -> Blocks.IRON_BARS.defaultBlockState();
            case 'g' -> Blocks.GRASS.defaultBlockState();
            /*
             * The modern mound intentionally has no permanent light sources.
             * Keep the extracted marker in the immutable blueprint so its
             * provenance and coordinates remain auditable, but replace every
             * original torch with air during placement.
             */
            case 'T' -> Blocks.AIR.defaultBlockState();
            case '0', '1', '2', '3', '4', '5', '6', '7' ->
                    stairState(symbol - '0');
            default -> throw new IllegalArgumentException(
                    "Unknown ancient mound symbol: " + symbol
            );
        };
    }

    private static BlockState stairState(int metadata) {
        Direction facing = switch (metadata & 3) {
            case 0 -> Direction.EAST;
            case 1 -> Direction.WEST;
            case 2 -> Direction.SOUTH;
            case 3 -> Direction.NORTH;
            default -> throw new IllegalStateException();
        };
        return Blocks.COBBLESTONE_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, facing)
                .setValue(
                        StairBlock.HALF,
                        (metadata & 4) == 0 ? Half.BOTTOM : Half.TOP
                );
    }
}
