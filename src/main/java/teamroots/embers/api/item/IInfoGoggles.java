package teamroots.embers.api.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public interface IInfoGoggles {
    boolean shouldDisplayInfo(EntityPlayer player, ItemStack stack, EntityEquipmentSlot slot);
}
