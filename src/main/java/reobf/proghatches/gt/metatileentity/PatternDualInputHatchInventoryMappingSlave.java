package reobf.proghatches.gt.metatileentity;

import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_PIPE_IN;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import com.glodblock.github.common.item.ItemFluidPacket;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.api.forge.ItemStackHandler;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.SyncedWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IInterfaceViewable;
import appeng.core.AppEng;
import appeng.core.sync.GuiBridge;
import appeng.helpers.ICustomNameObject;
import appeng.items.misc.ItemEncodedPattern;
import appeng.items.tools.quartz.ToolQuartzCuttingKnife;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import gregtech.GT_Mod;
import gregtech.api.GregTech_API;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Textures;
import gregtech.api.gui.modularui.GT_UIInfos;
import gregtech.api.gui.modularui.GT_UITextures;
import gregtech.api.interfaces.IConfigurationCircuitSupport;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.modularui.IAddUIWidgets;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.interfaces.tileentity.IHasWorldObjectAndCoords;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_InputBus;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_TieredMachineBlock;
import gregtech.api.render.TextureFactory;
import gregtech.common.tileentities.machines.IDualInputHatch;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import reobf.proghatches.block.BlockIOHub;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch.DualInvBuffer;
import reobf.proghatches.gt.metatileentity.DualInputHatch.Net;
import reobf.proghatches.gt.metatileentity.util.IMultiplePatternPushable;
import reobf.proghatches.gt.metatileentity.util.ISkipStackSizeCheck;
import reobf.proghatches.gt.metatileentity.util.MappingItemHandler;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;
import reobf.proghatches.net.MasterSetMessage;
import reobf.proghatches.net.UpgradesMessage;

public class PatternDualInputHatchInventoryMappingSlave<T extends DualInputHatch & IDualInputHatch & IMetaTileEntity>
		extends GT_MetaTileEntity_TieredMachineBlock implements IAddUIWidgets, ICraftingMedium, ICustomNameObject,
		IGridProxyable, IInterfaceViewable, IPowerChannelState, IActionHost, ICraftingProvider
		,IMultiplePatternPushable
		{
	private T master; // use getMaster() to access
	public int masterX, masterY, masterZ;
	public boolean masterSet = false; // indicate if values of masterX,
										// masterY, masterZ are valid

	public PatternDualInputHatchInventoryMappingSlave(int aID, String aName, String aNameRegional, int aTier) {
		super(aID, aName, aNameRegional, aTier, 0,

				Config.get("PDIHIMS", ImmutableMap.of()));
		Registration.items.add(new ItemStack(GregTech_API.sBlockMachines, 1, aID));
	}

	public PatternDualInputHatchInventoryMappingSlave(String aName, int aTier, int aInvSlotCount, String[] aDescription,
			ITexture[][][] aTextures) {
		super(aName, aTier, aInvSlotCount, aDescription, aTextures);

	}

	@Override
	public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

		return new PatternDualInputHatchInventoryMappingSlave<>(mName, mTier, 0, mDescriptionArray, mTextures);
	}

	@Override
	public void saveNBTData(NBTTagCompound aNBT) {
		aNBT.setBoolean("additionalConnection", additionalConnection);
		NBTTagCompound tag = new NBTTagCompound();// aNBT.getCompoundTag("patternSlots");

		for (int i = 0; i < pattern.length; i++) {
			final int ii = i;
			Optional.ofNullable(pattern[i]).map(s -> s.writeToNBT(new NBTTagCompound()))
					.ifPresent(s -> tag.setTag("i" + ii, s));
		}
		aNBT.setTag("patternSlots", tag);
		Optional.ofNullable(customName).ifPresent(s -> aNBT.setString("customName", s));
		getProxy().writeToNBT(aNBT);

		if (masterSet) {
			NBTTagCompound masterNBT = new NBTTagCompound();
			masterNBT.setInteger("x", masterX);
			masterNBT.setInteger("y", masterY);
			masterNBT.setInteger("z", masterZ);
			aNBT.setTag("master", masterNBT);
		}

	}

	@Override
	public void loadNBTData(NBTTagCompound aNBT) {
		if(aNBT.hasKey("x")==false)return;
		additionalConnection = aNBT.getBoolean("additionalConnection");
		NBTTagCompound tag = aNBT.getCompoundTag("patternSlots");
		if (tag != null)
			for (int i = 0; i < pattern.length; i++) {
				pattern[i] = Optional.ofNullable(tag.getCompoundTag("i" + i)).map(ItemStack::loadItemStackFromNBT)
						.orElse(null);
			}
		customName = aNBT.getString("customName");

		getProxy().readFromNBT(aNBT);

		updateValidGridProxySides();

		if (aNBT.hasKey("master")) {
			NBTTagCompound masterNBT = aNBT.getCompoundTag("master");
			masterX = masterNBT.getInteger("x");
			masterY = masterNBT.getInteger("y");
			masterZ = masterNBT.getInteger("z");
			masterSet = true;
		}
	}

	@Override
	public boolean shouldDropItemAt(int index) {
		return false;
	}

	@Override
	public boolean isAccessAllowed(EntityPlayer aPlayer) {

		return true;
	}

	@Override
	public ITexture[] getTexture(IGregTechTileEntity baseMetaTileEntity, ForgeDirection side, ForgeDirection facing,
			int colorIndex, boolean active, boolean redstoneLevel) {

		if (side != facing)

			return new ITexture[] { Textures.BlockIcons.MACHINE_CASINGS[mTier][colorIndex + 1] };
		else
			return new ITexture[] { Textures.BlockIcons.MACHINE_CASINGS[mTier][colorIndex + 1],
					TextureFactory.of(MyMod.iohub, BlockIOHub.magicNO_inv_me_slave) };

	}

	@Override
	public ITexture[][][] getTextureSet(ITexture[] aTextures) {

		return new ITexture[0][0][0];
	}
	long lastSync;
	@Override
	public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTimer) {
		super.onPostTick(aBaseMetaTileEntity, aTimer);
		if (aTimer % 100 == 0 && masterSet && getMaster() == null) {
			trySetMasterFromCoord(masterX, masterY, masterZ);
		}
		if (getBaseMetaTileEntity().isServerSide()) {
			if (needPatternSync && aTimer > lastSync + 100) {
				needPatternSync = !postMEPatternChange();
				lastSync = aTimer;
			}
			if (aTimer % 20 == 0) {
				getBaseMetaTileEntity().setActive(isActive());
				}
			}
			
	}

	@SuppressWarnings("unchecked")
	public IDualInputHatch trySetMasterFromCoord(int x, int y, int z) {
		TileEntity tileEntity = getBaseMetaTileEntity().getWorld().getTileEntity(x, y, z);
		if (tileEntity == null)
			return null;
		if (!(tileEntity instanceof IGregTechTileEntity))
			return null;
		IMetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
		if (!(metaTileEntity instanceof IDualInputHatch))
			return null;

		if (!(metaTileEntity instanceof reobf.proghatches.gt.metatileentity.DualInputHatch))
			return null;

		masterX = x;
		masterY = y;
		masterZ = z;
		masterSet = true;
		master = (T) metaTileEntity;
		return master;
	}

	private boolean tryLinkDataStick(EntityPlayer aPlayer) {
		ItemStack dataStick = aPlayer.inventory.getCurrentItem();

		if (!ItemList.Tool_DataStick.isStackEqual(dataStick, false, true)) {
			return false;
		}
		if (dataStick.hasTagCompound() && dataStick.stackTagCompound.getString("type").equals("CraftingInputBuffer")) {
			aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.gt.slave.compat"));
			return false;
		}
		if (!dataStick.hasTagCompound()
				|| !dataStick.stackTagCompound.getString("type").equals("ProgHatchesDualInput")) {
			return false;
		}

		NBTTagCompound nbt = dataStick.stackTagCompound;
		int x = nbt.getInteger("x");
		int y = nbt.getInteger("y");
		int z = nbt.getInteger("z");
		if (trySetMasterFromCoord(x, y, z) != null) {
			aPlayer.addChatMessage(new ChatComponentText("Link successful"));
			return true;
		}
		aPlayer.addChatMessage(new ChatComponentText("Link failed"));
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * gregtech.api.metatileentity.MetaTileEntity#onRightclick(gregtech.api.
	 * interfaces.tileentity.IGregTechTileEntity,
	 * net.minecraft.entity.player.EntityPlayer,
	 * net.minecraftforge.common.util.ForgeDirection, float, float, float)
	 */
	@Override
	public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer, ForgeDirection side,
			float aX, float aY, float aZ) {
		if (!(aPlayer instanceof EntityPlayerMP)) {
			return false;
		}
		final ItemStack is = aPlayer.inventory.getCurrentItem();
		if (is != null && is.getItem() instanceof ToolQuartzCuttingKnife) {
			if (ForgeEventFactory.onItemUseStart(aPlayer, is, 1) <= 0)
				return false;
			IGregTechTileEntity te = getBaseMetaTileEntity();
			aPlayer.openGui(AppEng.instance(), GuiBridge.GUI_RENAMER.ordinal() << 5 | (side.ordinal()), te.getWorld(),
					te.getXCoord(), te.getYCoord(), te.getZCoord());
			return true;
		}
		if (tryLinkDataStick(aPlayer)) {
			return true;
		}

		if (getMaster() != null) {
			if (!aBaseMetaTileEntity.isClientSide()) {
				/*
				 * TileEntity m= (TileEntity)
				 * getMaster().getBaseMetaTileEntity(); NBTTagCompound
				 * nbttagcompound = new NBTTagCompound();
				 * m.writeToNBT(nbttagcompound); S35PacketUpdateTileEntity pa=
				 * new S35PacketUpdateTileEntity(m.xCoord, m.yCoord, m.zCoord,
				 * m.getBlockMetadata() , nbttagcompound);
				 * 
				 * ((EntityPlayerMP)
				 * aPlayer).playerNetServerHandler.sendPacket(pa);
				 */

				MyMod.net.sendTo(new MasterSetMessage(aBaseMetaTileEntity.getXCoord(), aBaseMetaTileEntity.getYCoord(),
						aBaseMetaTileEntity.getZCoord(), this), (EntityPlayerMP) aPlayer);

			}
			GT_UIInfos.openGTTileEntityUI(aBaseMetaTileEntity, aPlayer);
			return true;
		} else {

			GT_UIInfos.openGTTileEntityUI(aBaseMetaTileEntity, aPlayer);
		}
		return false;

	}

	public T getMaster() {
		if (master == null)
			return null;
		if (((IMetaTileEntity) master).getBaseMetaTileEntity() == null) { // master
																			// disappeared
			master = null;
		}
		return master;
	}

	public ForgeDirection getMasterFront() {
		if (master == null)
			return this.getBaseMetaTileEntity().getFrontFacing();// throw new
																	// RuntimeExcpetion()?
		// do not check master, becasue it's always called after getMaster()
		return master.getBaseMetaTileEntity().getFrontFacing();
	}

	@Override
	public void getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
			IWailaConfigHandler config) {
		NBTTagCompound tag = accessor.getNBTData();
		currenttip.add((tag.getBoolean("linked") ? "Linked" : "Not linked"));

		if (tag.hasKey("masterX")) {
			currenttip.add("Bound to " + tag.getInteger("masterX") + ", " + tag.getInteger("masterY") + ", "
					+ tag.getInteger("masterZ"));
		}

		if (tag.hasKey("masterName")) {
			currenttip.add(EnumChatFormatting.GOLD + tag.getString("masterName") + EnumChatFormatting.RESET);
		}

		super.getWailaBody(itemStack, currenttip, accessor, config);
	}

	@Override
	public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
			int z) {

		tag.setBoolean("linked", getMaster() != null);
		if (masterSet) {
			tag.setInteger("masterX", masterX);
			tag.setInteger("masterY", masterY);
			tag.setInteger("masterZ", masterZ);
		}
		if (getMaster() != null)
			tag.setString("masterName", getNameOf(getMaster()));
		/*
		 * if (getMaster() != null) tag.setString("masterName",
		 * getMaster().getnam);
		 */

		super.getWailaNBTData(player, tile, tag, world, x, y, z);
	}

	public String getNameOf(T tg) {

		if (tg instanceof ICustomNameObject) {
			ICustomNameObject iv = (ICustomNameObject) tg;
			if (iv.hasCustomName())
				return iv.getCustomName();

		}

		StringBuilder name = new StringBuilder();
		if (tg instanceof ICraftingMedium && ((ICraftingMedium) tg).getCrafterIcon() != null) {
			name.append(((ICraftingMedium) tg).getCrafterIcon().getDisplayName());
		} else {
			name.append(tg.getLocalName());
		}

		return name.toString();
	}

	@Override
	public boolean isFacingValid(ForgeDirection facing) {

		return true;
	}

	@Override
	public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
			ItemStack aStack) {

		return false;
	}

	@Override
	public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
			ItemStack aStack) {

		return false;
	}

	@Override
	public void addUIWidgets(Builder builder, UIBuildContext buildContext) {
		if (masterSet)
			trySetMasterFromCoord(masterX, masterY, masterZ);
		if (getMaster() instanceof IAddUIWidgets) {
			builder.widget(new SyncedWidget() {
				@Override
				public void detectAndSendChanges(boolean init) {
					if (getMaster() == null || getMaster().getBaseMetaTileEntity() == null || !getMaster().isValid()) {
						buildContext.getPlayer().closeScreen();
					}
				}

				@Override
				public void readOnClient(int id, PacketBuffer buf) throws IOException {

				}

				@Override
				public void readOnServer(int id, PacketBuffer buf) throws IOException {

				}

			});
			((IAddUIWidgets) getMaster()).addUIWidgets(builder, buildContext);
			buildContext.addSyncedWindow(989898, this::createPatternWindow);

			builder.widget(new ButtonWidget().setOnClick((clickData, widget) -> {
				if (widget.getContext().isClient() == false)
					widget.getContext().openSyncedWindow(989898);
			}).setPlayClickSound(true)
					.setBackground(GT_UITextures.BUTTON_STANDARD, GT_UITextures.OVERLAY_BUTTON_PLUS_LARGE)
					.addTooltips(ImmutableList
							.of(LangManager.translateToLocalFormatted("programmable_hatches.gt.pattern.mapping")))
					.setSize(16, 16)
					// .setPos(10 + 16 * 9, 3 + 16 * 2)
					.setPos(new Pos2d(getGUIWidth() - 18 - 3, 5 + 16 + 2 + 16 + 2 + 18 + 24)));

		} else if (getMaster() == null) {
			builder.widget(TextWidget.localised("hatch.dualinput.slave.inv.mapping.me.missing").setPos(5, 5)

			);
			buildContext.addSyncedWindow(989898, this::createPatternWindow);

			builder.widget(new ButtonWidget().setOnClick((clickData, widget) -> {
				if (widget.getContext().isClient() == false)
					widget.getContext().openSyncedWindow(989898);
			}).setPlayClickSound(true)
					.setBackground(GT_UITextures.BUTTON_STANDARD, GT_UITextures.OVERLAY_BUTTON_PLUS_LARGE)
					.addTooltips(ImmutableList
							.of(LangManager.translateToLocalFormatted("programmable_hatches.gt.pattern.mapping")))
					.setSize(16, 16)
					// .setPos(10 + 16 * 9, 3 + 16 * 2)
					.setPos(new Pos2d(getGUIWidth() - 18 - 3, 5 + 16 + 2 + 16 + 2 + 18 + 24)));
		}

	}

	@Override
	public boolean useModularUI() {

		return true;
	}

	private AENetworkProxy gridProxy;

	@Override
	public AENetworkProxy getProxy() {
		if (gridProxy == null) {
			gridProxy = new AENetworkProxy(this, "proxy",
					new ItemStack(GregTech_API.sBlockMachines, 1, this.getBaseMetaTileEntity().getMetaTileID()), true);
			gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
			updateValidGridProxySides();
			if (getBaseMetaTileEntity().getWorld() != null)
				gridProxy.setOwner(getBaseMetaTileEntity().getWorld()
						.getPlayerEntityByName(getBaseMetaTileEntity().getOwnerName()));
		}

		return this.gridProxy;
	}

	@Override
	public IGridNode getGridNode(ForgeDirection dir) {
		return getProxy().getNode();
	}

	@Override
	public void securityBreak() {

	}

	@Override
	public DimensionalCoord getLocation() {

		return new DimensionalCoord(getTileEntity());
	}

	boolean additionalConnection;

	private void updateValidGridProxySides() {
		if (additionalConnection) {
			getProxy().setValidSides(EnumSet.complementOf(EnumSet.of(ForgeDirection.UNKNOWN)));
		} else {
			getProxy().setValidSides(EnumSet.of(getBaseMetaTileEntity().getFrontFacing()));
		}
		// getProxy().setValidSides(EnumSet.of(getBaseMetaTileEntity().getFrontFacing()));

	}

	@Override
	public IGridNode getActionableNode() {

		return this.getGridNode(ForgeDirection.UP);
	}

	@Override
	public boolean isPowered() {
		return getProxy() != null && getProxy().isPowered();
	}

	@Override
	public boolean isActive() {
		return getProxy() != null && getProxy().isActive();
	}

	@Override
	public int rows() {
		return 4;
	}

	@Override
	public int rowSize() {
		return 9;
	}

	@Override
	public IInventory getPatterns() {

		return patternMapper;
	}

	@Override
	public String getName() {
		if (hasCustomName()) {
			return getCustomName();
		}
		StringBuilder name = new StringBuilder();
		if (getCrafterIcon() != null) {
			name.append(getCrafterIcon().getDisplayName());
		} else {
			name.append(getInventoryName());
		}

		/*
		 * if (mInventory[SLOT_CIRCUIT] != null) { name.append(" - ");
		 * name.append(mInventory[SLOT_CIRCUIT].getItemDamage()); } if
		 * (mInventory[SLOT_MANUAL_START] != null) { name.append(" - ");
		 * name.append(mInventory[SLOT_MANUAL_START].getDisplayName()); }
		 */// TODO
		return name.toString();
	}

	@Override
	public TileEntity getTileEntity() {
		return (TileEntity) getBaseMetaTileEntity();
	}

	@Override
	public boolean shouldDisplay() {

		return true;
	}

	ItemStack[] pattern = new ItemStack[36];
	IInventory patternMapper = new IInventory() {

		@Override
		public int getSizeInventory() {

			return pattern.length;
		}

		@Override
		public ItemStack getStackInSlot(int slotIn) {

			return pattern[slotIn];
		}

		@Override
		public ItemStack decrStackSize(int index, int count) {

			try {
				if (pattern[index] != null) {
					ItemStack itemstack;

					if (pattern[index].stackSize <= count) {
						itemstack = pattern[index];
						pattern[index] = null;
						this.markDirty();
						return itemstack;
					} else {
						itemstack = pattern[index].splitStack(count);

						if (pattern[index].stackSize == 0) {
							pattern[index] = null;
						}

						this.markDirty();
						return itemstack;
					}
				} else {
					return null;
				}
			} finally {

				onPatternChange();
			}
		}

		@Override
		public ItemStack getStackInSlotOnClosing(int index) {

			return null;
		}

		@Override
		public void setInventorySlotContents(int index, ItemStack stack) {
			pattern[index] = stack;
			onPatternChange();
		}

		@Override
		public String getInventoryName() {

			return "";
		}

		@Override
		public boolean hasCustomInventoryName() {

			return false;
		}

		// @Override
		// public int stack

		@Override
		public void markDirty() {

		}

		@Override
		public boolean isUseableByPlayer(EntityPlayer player) {

			return true;
		}

		@Override
		public void openInventory() {
		}

		@Override
		public void closeInventory() {
		}

		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack) {

			return true;
		}

		@Override
		public int getInventoryStackLimit() {

			return 1;
		}
	};
	boolean needPatternSync;
	private String customName;

	private void onPatternChange() {
		if (!getBaseMetaTileEntity().isServerSide())
			return;

		needPatternSync = true;
	}

	@Override
	public void gridChanged() {
		needPatternSync = true;
	}

	@Override
	public String getCustomName() {

		return customName;
	}

	@Override
	public void setCustomName(String name) {
		customName = name;

	}

	@Override
	public boolean hasCustomName() {

		return customName != null && (!customName.equals(""));
	}

	public boolean isInputEmpty(T master) {

		for (FluidTank f : master.mStoredFluid) {
			if (f.getFluidAmount() > 0) {
				return false;
			}
		}
		for (ItemStack i : master.mInventory) {

			if (i != null && i.stackSize > 0) {
				return false;
			}
		}
		return true;
	}

	public void clearInv(T master) {

		for (FluidTank f : master.mStoredFluid) {
			f.setFluid(null);
		}
		for (int i = 0; i < master.mInventory.length; i++) {

			if (master.isValidSlot(i)) {
				mInventory[i] = null;
			}
		}

	}

	@Override
	public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
		T master = getMaster();
		if (getMaster() instanceof PatternDualInputHatch) {
			PatternDualInputHatch dih = ((PatternDualInputHatch) getMaster());
			try{
			dih.skipActiveCheck=true;
			return dih.pushPattern(patternDetails, table);
			}finally{dih.skipActiveCheck=false;}
		}
		if (master != null) {
			if (!isInputEmpty(master)) {
				return false;
			}

			int i = 0;
			int f = 0;
			int ilimit = master.getInventoryStackLimit();
			int flimit = master.getInventoryFluidLimit();
			boolean isplit = master.disableLimited;
			boolean fsplit = master.fluidLimit==0;
			for (int index = 0; index < table.getSizeInventory(); index++) {
				ItemStack is = (table.getStackInSlot(index));
				if (is == null)
					continue;
				is = is.copy();
				if (is.getItem() instanceof ItemFluidPacket) {
					FluidStack fs = ItemFluidPacket.getFluidStack(is);
					if (fs == null) {
						continue;
					}
					while (fs.amount > 0) {
						if (f >= master.mStoredFluid.length) {
							clearInv(master);
							return false;
						}
						int tosplit = Math.min(fs.amount, flimit);
						fs.amount -= tosplit;
						if ((!fsplit) && fs.amount > 0) {
							clearInv(master);
							return false;
						}
						FluidStack splitted = new FluidStack(fs.getFluid(), tosplit);
						master.mStoredFluid[f].setFluidDirect(splitted);
						f++;
					}

				} else {
					while (is.stackSize > 0) {
						if (master.isValidSlot(i) == false) {
							clearInv(master);
							return false;
						}
						ItemStack splitted = is.splitStack(Math.min(is.stackSize, ilimit));
						if ((!isplit) && is.stackSize > 0) {
							clearInv(master);
							return false;
						}
						master.mInventory[i] = splitted;
						i++;
					}
				}

			}
			if(master instanceof BufferedDualInputHatch){
				((BufferedDualInputHatch) master).classifyForce();
			}
			return true;// hoo ray
		}

		return false;
	}

	@Override
	public boolean isBusy() {
		T master = getMaster();
		if (master instanceof PatternDualInputHatch) {
			return ((PatternDualInputHatch) getMaster()).isBusy();
		}
		if (master != null) {

			if (!isInputEmpty(master)) {
				return true;
			}

		}

		return false;
	}
	private boolean postMEPatternChange() {
		// don't post until it's active
		if (!getProxy().isActive())
			return false;
		try {
			getProxy().getGrid().postEvent(new MENetworkCraftingPatternChange(this, getProxy().getNode()));
		} catch (GridAccessException ignored) {
			return false;
		}
		return true;
	}
	@Override
	public void provideCrafting(ICraftingProviderHelper craftingTracker) {

		if (!isActive())
			return;

		for (ItemStack slot : pattern) {
			if (slot == null)
				continue;
			ICraftingPatternDetails details = null;
			try {
				details = ((ICraftingPatternItem) slot.getItem()).getPatternForItem(slot,
						this.getBaseMetaTileEntity().getWorld());

			} catch (Exception e) {

			}
			if (details == null) {
				GT_Mod.GT_FML_LOGGER.warn("Found an invalid pattern at " + getBaseMetaTileEntity().getCoords()
						+ " in dim " + getBaseMetaTileEntity().getWorld().provider.dimensionId);
				continue;
			}
			craftingTracker.addCraftingOption(this, details);
		}
	}

	protected ModularWindow createPatternWindow(final EntityPlayer player) {
		final int WIDTH = 18 * 4 + 6;
		final int HEIGHT = 18 * 9 + 6;
		final int PARENT_WIDTH = getGUIWidth();
		final int PARENT_HEIGHT = getGUIHeight();

		ModularWindow.Builder builder = ModularWindow.builder(WIDTH, HEIGHT);
		builder.setBackground(GT_UITextures.BACKGROUND_SINGLEBLOCK_DEFAULT);
		builder.setGuiTint(getGUIColorization());
		builder.setDraggable(true);
		builder.setPos((a, b) -> new Pos2d(PARENT_WIDTH + b.getPos().getX(), PARENT_HEIGHT * 0 + b.getPos().getY()));
		MappingItemHandler shared_handler = new MappingItemHandler(pattern, 0, 36);
		// use shared handler
		// or shift clicking a pattern in pattern slot will just transfer it to
		// another pattern slot
		// instead of player inventory!
		for (int i = 0; i < 36; i++) {

			BaseSlot bs;

			builder.widget(new SlotWidget(bs = new BaseSlot(shared_handler, i)

			) {

				@Override
				protected ItemStack getItemStackForRendering(Slot slotIn) {
					ItemStack stack = slotIn.getStack();
					if (stack == null || !(stack.getItem() instanceof ItemEncodedPattern)) {
						return stack;
					}
					ItemStack output = ((ItemEncodedPattern) stack.getItem()).getOutput(stack);
					return output != null ? output : stack;

				}
			}.setShiftClickPriority(-1).setFilter(itemStack -> itemStack.getItem() instanceof ICraftingPatternItem)
					.setChangeListener(() -> {
						onPatternChange();
					}).setPos((i % 4) * 18 + 3, (i / 4) * 18 + 3)
					.setBackground(getGUITextureSet().getItemSlot(), GT_UITextures.OVERLAY_SLOT_PATTERN_ME));

		}

		return builder.build();
	}

	@Override
	public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
		super.onFirstTick(aBaseMetaTileEntity);
		getProxy().onReady();
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection forgeDirection) {
		return isOutputFacing(forgeDirection) ? AECableType.SMART : AECableType.NONE;
	}

	@Override
	public void onFacingChange() {
		updateValidGridProxySides();
	}

	@Override
	public ItemStack getCrafterIcon() {
		ItemStack is = this.getMachineCraftingIcon();
		return is == null ? new ItemStack(GregTech_API.sBlockMachines, 1, getBaseMetaTileEntity().getMetaTileID()) : is;
	}

	@Override
	public int getGUIHeight() {

		return super.getGUIHeight() + 20;
	}
	  @Override
	    public boolean onWireCutterRightClick(ForgeDirection side, ForgeDirection wrenchingSide, EntityPlayer aPlayer,
	        float aX, float aY, float aZ) {
	        additionalConnection = !additionalConnection;
	        updateValidGridProxySides();
	        aPlayer.addChatComponentMessage(
	            new ChatComponentTranslation("GT5U.hatch.additionalConnection." + additionalConnection));
	        return true;
	    }

	@Override
	public int pushPatternMulti(ICraftingPatternDetails patternDetails, InventoryCrafting table, int maxTodo) {
		if(Config.fastPatternDualInput==false)return 0;
		
		if (getMaster() instanceof PatternDualInputHatch) {
			PatternDualInputHatch dih = ((PatternDualInputHatch) getMaster());
			try{
			dih.skipActiveCheck=true;
			return dih.pushPatternMulti(patternDetails, table, maxTodo);
			}finally{dih.skipActiveCheck=false;}
		}
		int suc=0;
		if (master != null) {
			if (!isInputEmpty(master)) {
				return 0;
			}

			int i = 0;
			int f = 0;
			int ilimit = master.getInventoryStackLimit();
			int flimit = master.getInventoryFluidLimit();
			boolean isplit = master.disableLimited;
			boolean fsplit = master.fluidLimit==0;
			for (int index = 0; index < table.getSizeInventory(); index++) {
				ItemStack is = (table.getStackInSlot(index));
				if (is == null)
					continue;
				is = is.copy();
				if (is.getItem() instanceof ItemFluidPacket) {
					FluidStack fs = ItemFluidPacket.getFluidStack(is);
					if (fs == null) {
						continue;
					}
					while (fs.amount > 0) {
						if (f >= master.mStoredFluid.length) {
							clearInv(master);
							return 0;
						}
						int tosplit = Math.min(fs.amount, flimit);
						fs.amount -= tosplit;
						if ((!fsplit) && fs.amount > 0) {
							clearInv(master);
							return 0;
						}
						FluidStack splitted = new FluidStack(fs.getFluid(), tosplit);
						master.mStoredFluid[f].setFluidDirect(splitted);
						f++;
					}

				} else {
					while (is.stackSize > 0) {
						if (master.isValidSlot(i) == false) {
							clearInv(master);
							return 0;
						}
						ItemStack splitted = is.splitStack(Math.min(is.stackSize, ilimit));
						if ((!isplit) && is.stackSize > 0) {
							clearInv(master);
							return 0;
						}
						master.mInventory[i] = splitted;
						i++;
					}
				}

			}
			suc++;maxTodo--;
			
			if(master instanceof BufferedDualInputHatch){
			DualInvBuffer theBuffer=((BufferedDualInputHatch) master).classifyForce();
			
			if(theBuffer!=null){
				int todo=Math.min(theBuffer.space(),maxTodo);
				
				if(todo>0){
					for(int ix=0;ix<theBuffer.i;ix++){
						if(theBuffer.mStoredItemInternalSingle[ix]!=null){
							if(theBuffer.mStoredItemInternal[ix]==null){
								theBuffer.mStoredItemInternal[ix]=theBuffer.mStoredItemInternalSingle[ix].copy();
								theBuffer.mStoredItemInternal[ix].stackSize=0;//circuit?
							}
							theBuffer.mStoredItemInternal[ix].stackSize+=theBuffer.mStoredItemInternalSingle[ix].stackSize*todo;
						}}
					
					
					for(int ix=0;ix<theBuffer.f;ix++){
						if(theBuffer.mStoredFluidInternalSingle[ix].getFluidAmount()>0){
							if(theBuffer.mStoredFluidInternal[ix].getFluidAmount()<=0){
								theBuffer.mStoredFluidInternal[ix].setFluid(theBuffer.mStoredFluidInternalSingle[ix].getFluid().copy());
								theBuffer.mStoredFluidInternal[ix].getFluid().amount=0;
							}
							theBuffer.mStoredFluidInternal[ix].getFluid().amount+=theBuffer.mStoredFluidInternalSingle[ix].getFluidAmount()*todo;
						}}
					
					suc+=todo;
				}
				
				
			}
			
			
		}
		
		
		
		
		
		
		
		
		
		
	}
	return suc;
	

}
}
