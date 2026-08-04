package com.thaumcraftmodern.registry;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.world.menu.ArcaneWorkbenchMenu;
import com.thaumcraftmodern.world.menu.AlchemicalFurnaceMenu;
import com.thaumcraftmodern.world.menu.DeconstructionTableMenu;
import com.thaumcraftmodern.world.menu.ResearchTableMenu;
import com.thaumcraftmodern.world.menu.PechMenu;
import com.thaumcraftmodern.world.menu.ThaumatoriumMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, ThaumcraftModern.MOD_ID);

    public static final RegistryObject<MenuType<ResearchTableMenu>> RESEARCH_TABLE =
            MENUS.register("research_table", () -> IForgeMenuType.create(ResearchTableMenu::fromNetwork));
    public static final RegistryObject<MenuType<ArcaneWorkbenchMenu>> ARCANE_WORKBENCH =
            MENUS.register(
                    "arcane_workbench",
                    () -> IForgeMenuType.create(ArcaneWorkbenchMenu::fromNetwork)
            );
    public static final RegistryObject<MenuType<DeconstructionTableMenu>>
            DECONSTRUCTION_TABLE = MENUS.register(
                    "deconstruction_table",
                    () -> IForgeMenuType.create(
                            DeconstructionTableMenu::fromNetwork
                    )
            );
    public static final RegistryObject<MenuType<PechMenu>> PECH =
            MENUS.register(
                    "pech",
                    () -> IForgeMenuType.create(PechMenu::fromNetwork)
            );
    public static final RegistryObject<MenuType<AlchemicalFurnaceMenu>>
            ALCHEMICAL_FURNACE = MENUS.register(
                    "alchemical_furnace",
                    () -> IForgeMenuType.create(
                            AlchemicalFurnaceMenu::fromNetwork
                    )
            );
    public static final RegistryObject<MenuType<ThaumatoriumMenu>> THAUMATORIUM =
            MENUS.register("thaumatorium",
                    () -> IForgeMenuType.create(ThaumatoriumMenu::fromNetwork));

    private ModMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
