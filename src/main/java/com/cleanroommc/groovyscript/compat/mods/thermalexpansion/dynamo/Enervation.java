package com.cleanroommc.groovyscript.compat.mods.thermalexpansion.dynamo;

import cofh.core.inventory.ComparableItemStack;
import com.cleanroommc.groovyscript.api.GroovyBlacklist;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.Example;
import com.cleanroommc.groovyscript.api.documentation.annotations.MethodDescription;
import com.cleanroommc.groovyscript.api.documentation.annotations.RegistryDescription;
import com.cleanroommc.groovyscript.core.mixin.thermalexpansion.EnervationManagerAccessor;
import com.cleanroommc.groovyscript.helper.SimpleObjectStream;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

@RegistryDescription
public class Enervation extends VirtualizedRegistry<Enervation.EnervationRecipe> {

    @Override
    @GroovyBlacklist
    @ApiStatus.Internal
    public void onReload() {
        removeScripted().forEach(recipe -> EnervationManagerAccessor.getFuelMap().keySet().removeIf(r -> r.equals(recipe.comparableItemStack())));
        restoreFromBackup().forEach(r -> EnervationManagerAccessor.getFuelMap().put(r.comparableItemStack(), r.energy()));
    }

    public void add(EnervationRecipe recipe) {
        EnervationManagerAccessor.getFuelMap().put(recipe.comparableItemStack(), recipe.energy());
        addScripted(recipe);
    }

    @MethodDescription(type = MethodDescription.Type.ADDITION, example = @Example("item('minecraft:clay'), 100"))
    public void add(ItemStack itemStack, int energy) {
        add(new EnervationRecipe(new ComparableItemStack(itemStack), energy));
    }

    public boolean remove(ComparableItemStack recipe) {
        return EnervationManagerAccessor.getFuelMap().keySet().removeIf(r -> {
            if (r.equals(recipe)) {
                addBackup(new EnervationRecipe(r, EnervationManagerAccessor.getFuelMap().get(r)));
                return true;
            }
            return false;
        });
    }

    public boolean remove(EnervationRecipe recipe) {
        return EnervationManagerAccessor.getFuelMap().keySet().removeIf(r -> {
            if (r.equals(recipe.comparableItemStack())) {
                addBackup(recipe);
                return true;
            }
            return false;
        });
    }

    @MethodDescription(example = @Example("item('minecraft:redstone')"))
    public boolean removeByInput(IIngredient input) {
        return EnervationManagerAccessor.getFuelMap().keySet().removeIf(r -> {
            if (input.test(r.toItemStack())) {
                addBackup(new EnervationRecipe(r, EnervationManagerAccessor.getFuelMap().get(r)));
                return true;
            }
            return false;
        });
    }

    @MethodDescription(type = MethodDescription.Type.QUERY)
    public SimpleObjectStream<ComparableItemStack> streamRecipes() {
        return new SimpleObjectStream<>(EnervationManagerAccessor.getFuelMap().keySet()).setRemover(this::remove);
    }

    @MethodDescription(priority = 2000, example = @Example(commented = true))
    public void removeAll() {
        EnervationManagerAccessor.getFuelMap().keySet().forEach(x -> addBackup(new EnervationRecipe(x, EnervationManagerAccessor.getFuelMap().get(x))));
        EnervationManagerAccessor.getFuelMap().clear();
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static final class EnervationRecipe {

        private final ComparableItemStack comparableItemStack;
        private final int energy;

        public EnervationRecipe(ComparableItemStack comparableItemStack, int energy) {
            this.comparableItemStack = comparableItemStack;
            this.energy = energy;
        }

        public ComparableItemStack comparableItemStack() {
            return comparableItemStack;
        }

        public int energy() {
            return energy;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (EnervationRecipe) obj;
            return Objects.equals(this.comparableItemStack, that.comparableItemStack) && this.energy == that.energy;
        }

        @Override
        public int hashCode() {
            return Objects.hash(comparableItemStack, energy);
        }

        @Override
        public String toString() {
            return "EnervationRecipe[" + "comparableItemStack=" + comparableItemStack + ", " + "energy=" + energy + ']';
        }
    }
}
