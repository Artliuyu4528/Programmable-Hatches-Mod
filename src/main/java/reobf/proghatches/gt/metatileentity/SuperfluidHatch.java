package reobf.proghatches.gt.metatileentity;

import java.util.ArrayList;

import com.google.common.collect.ImmutableMap;

import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidTank;

import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.extensions.ArrayExt;
import reobf.proghatches.main.Config;

public class SuperfluidHatch extends BufferedDualInputHatch {

	public SuperfluidHatch(String mName, byte mTier, String[] mDescriptionArray, ITexture[][][] mTextures,
			boolean mMultiFluid, int bufferNum) {
		super(mName, mTier, 4 + 1, mDescriptionArray, mTextures, mMultiFluid, bufferNum);

	}

	@Override
	public int fluidSlotsPerRow() {

		return 2;
	}

	@Override
	public int slotTierOverride(int mTier) {

		return 1;// 4 slots
	}

	public SuperfluidHatch(int id, String name, String nameRegional, int tier, boolean mMultiFluid, int bufferNum) {
		super(id, name, nameRegional, tier, 4 + 1, mMultiFluid, bufferNum,
				reobf.proghatches.main.Config.get("SH", ImmutableMap.of())

		);

	}

	@Override
	public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

		return new SuperfluidHatch(mName, mTier, mDescriptionArray, mTextures, mMultiFluid, bufferNum);

	}

	public int fluidLimit() {
		return 10_000_000;

	}

	public int itemLimit() {
		return 64;
	}

	public void initTierBasedField() {

		ArrayList<FluidTank> arr = new ArrayList<>();
		for (int i = 0; i < 24; i++) {
			arr.add(new ListeningFluidTank((int) (1000 * Math.pow(2, mTier)), this));

		}

		mStoredFluid = arr.toArray(new ListeningFluidTank[0]);

		return;
	}
}
