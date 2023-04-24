package me.luligabi.projecttablemod.common.screenhandler;

import me.luligabi.projecttablemod.common.block.SimpleCraftingInventory;
import me.luligabi.projecttablemod.mixin.CraftingInventoryAccessor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import java.util.Optional;

public class ProjectTableScreenHandler extends ScreenHandler {


    public ProjectTableScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleCraftingInventory(9, 3), new SimpleInventory(2*9), ScreenHandlerContext.EMPTY);
    }

    public ProjectTableScreenHandler(int syncId, PlayerInventory playerInventory, SimpleCraftingInventory input, Inventory inventory, ScreenHandlerContext context) {
        super(ScreenHandlingRegistry.PROJECT_TABLE_SCREEN_HANDLER, syncId);
        this.input = input;
        this.inventory = inventory;
        this.result = new CraftingResultInventory();
        this.context = context;
        this.player = playerInventory.player;
        checkSize(input, 9);
        checkSize(inventory, 18);
        input.onOpen(player);
        inventory.onOpen(player);

        addSlot(new CraftingResultSlot(player, input, result, 0, 124, 35) {

            @Override
            public void setStack(ItemStack stack) {
                super.setStack(stack);
                onContentChanged(inventory);
            }

            @Override
            public ItemStack takeStack(int amount) {
                onContentChanged(inventory);
                return super.takeStack(amount);
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                /*onCrafted(stack);
                DefaultedList<ItemStack> remainingStacks = player.world.getRecipeManager().getRemainingStacks(RecipeType.CRAFTING, ProjectTableScreenHandler.this.input, player.world);

                for(int i = 0; i < 9; ++i) {
                    ItemStack invStack = ProjectTableScreenHandler.this.input.getStack(i);
                    ItemStack remainingStack = remainingStacks.get(i);
                    if(!invStack.isEmpty()) {
                        ProjectTableScreenHandler.this.input.removeStack(i, 1);
                        invStack = ProjectTableScreenHandler.this.input.getStack(i);
                    }

                    if(!remainingStack.isEmpty()) {
                        if(invStack.isEmpty()) {
                            ProjectTableScreenHandler.this.input.setStack(i, remainingStack);
                        } else if(ItemStack.areItemsEqual(invStack, remainingStack) && ItemStack.areNbtEqual(invStack, remainingStack)) {
                            remainingStack.increment(invStack.getCount());
                            ProjectTableScreenHandler.this.input.setStack(i, remainingStack);
                        } else if(!player.getInventory().insertStack(remainingStack)) {
                            player.dropItem(remainingStack, false);
                        }
                    }
                }*/
                super.onTakeItem(player, stack);
                onContentChanged(inventory);
            }


        });

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 3; ++j) {
                addSlot(new Slot(input, j + i * 3, 30 + j * 18, 17 + i * 18) {

                    @Override
                    public void setStack(ItemStack stack) {
                        super.setStack(stack);
                        onContentChanged(inventory);
                    }

                    @Override
                    public ItemStack takeStack(int amount) {
                        onContentChanged(inventory);
                        return super.takeStack(amount);
                    }

                });
            }
        }

        for(int i = 0; i < 2; ++i) {
            for(int j = 0; j < 9; ++j) {
                addSlot(new Slot(inventory, j + i * 9, 8 + j * 18, 77 + i * 18));
            }
        }


        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 126 + i * 18));
            }
        }

        for(int i = 0; i < 9; ++i) {
            addSlot(new Slot(playerInventory, i, 8 + i * 18, 184));
        }

        onContentChanged(input);
    }

    protected static void updateResult(ScreenHandler handler, World world, PlayerEntity player, CraftingInventory inventory, CraftingResultInventory resultInventory) {
        if(!world.isClient()) {
            System.out.println(((CraftingInventoryAccessor) inventory).getStacks());
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
            ItemStack itemStack = ItemStack.EMPTY;
            Optional<CraftingRecipe> optional = world.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, inventory, world);
            if(optional.isPresent()) {
                CraftingRecipe craftingRecipe = optional.get();
                if(resultInventory.shouldCraftRecipe(world, serverPlayerEntity, craftingRecipe)) {
                    ItemStack itemStack2 = craftingRecipe.craft(inventory, world.getRegistryManager());
                    if(itemStack2.isItemEnabled(world.getEnabledFeatures())) {
                        itemStack = itemStack2;
                    }
                }
            }

            resultInventory.setStack(0, itemStack);
            handler.setPreviousTrackedSlot(0, itemStack);
            serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), 0, itemStack));
        }
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        context.run((world, pos) -> {
            updateResult(this, world, player, this.input, result);
        });
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return input.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        return ItemStack.EMPTY;
    }

    public void provideRecipeInputs(RecipeMatcher matcher) {
        input.provideRecipeInputs(matcher);
    }

    public SimpleCraftingInventory getInput() {
        return input;
    }

    private final PlayerEntity player;
    private final ScreenHandlerContext context;
    private final CraftingResultInventory result;
    private final SimpleCraftingInventory input;
    private final Inventory inventory;
}
