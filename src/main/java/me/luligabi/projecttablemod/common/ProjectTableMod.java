package me.luligabi.projecttablemod.common;

import me.luligabi.projecttablemod.common.block.BlockRegistry;
import me.luligabi.projecttablemod.common.screenhandler.ScreenHandlingRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ProjectTableMod implements ModInitializer {

    @Override
    public void onInitialize() {
        BlockRegistry.init();
        ScreenHandlingRegistry.init();
    }

    public static Identifier id(String id) {
        return new Identifier(IDENTIFIER, id);
    }

    public static final String IDENTIFIER = "projecttablemod";

    public static final ItemGroup ITEM_GROUP = FabricItemGroup.builder(id("item_group"))
            .displayName(Text.translatable("itemGroup.projecttablemod.item_group"))
            .icon(() -> new ItemStack(BlockRegistry.PROJECT_TABLE))
            .entries((ctx, entries) ->
                    entries.addAll(ProjectTableMod.ITEMS)
            )
            .build();

    public static final List<ItemStack> ITEMS = new ArrayList<>();
}
