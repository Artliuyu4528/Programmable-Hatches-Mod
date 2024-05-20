package reobf.proghatches.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemEditableBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;

public class ItemBookTutorial extends ItemEditableBook {

	@SideOnly(Side.CLIENT)
	public String getItemStackDisplayName(ItemStack p_77653_1_) {
		if (p_77653_1_.hasTagCompound()) {
			NBTTagCompound nbttagcompound = p_77653_1_.getTagCompound();
			String s = nbttagcompound.getString("title");

			if (!StringUtils.isNullOrEmpty(s)) {
				return StatCollector.translateToLocal(s);
			}
		}

		return super.getItemStackDisplayName(p_77653_1_);
	}

}
