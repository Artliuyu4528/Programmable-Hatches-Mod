package reobf.proghatches.gt.metatileentity;

import java.lang.reflect.Field;
import java.util.Arrays;

import com.google.common.collect.ImmutableMap;

import gregtech.api.GregTech_API;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_InputBus;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.common.tileentities.machines.IRecipeProcessingAwareHatch;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import reobf.proghatches.main.registration.Registration;

public class RecipeCheckResultDetector extends GT_MetaTileEntity_Hatch_InputBus
implements IRecipeProcessingAwareHatch
{

	public RecipeCheckResultDetector(int id, String name, String nameRegional, int tier
			) {
		super(id, name, nameRegional, tier, 1, 
				
				reobf.proghatches.main.Config.get("RCRD", ImmutableMap.of()));
		Registration.items.add(new ItemStack(GregTech_API.sBlockMachines, 1, id));
	
	}   public RecipeCheckResultDetector(String aName, int aTier, String[] aDescription,
	        ITexture[][][] aTextures) {
	        super(aName, aTier, 1, aDescription, aTextures);
	    }
	@Override
	public void updateSlots() {
	
		//no-op
	}
	@Override
	public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
	
		return new RecipeCheckResultDetector(mName, mTier, mDescriptionArray, mTextures);
	}
	@Override
	public void startRecipeProcessing() {}
	/**0 no_recipe
	 * 1 running fine
	 * 2 other fail
	 * */
	private int lastSuccess;
	private int check(CheckRecipeResult crr){
		if(crr.wasSuccessful())return 1;
		if(crr==CheckRecipeResultRegistry.NO_RECIPE)return 0;
		return 2;
	}
	private static Field f;
	private int pulses;
	
	@Override
	public void loadNBTData(NBTTagCompound aNBT) {
		
		super.loadNBTData(aNBT);
		lastSuccess=aNBT.getInteger("lastSuccess");
		pulses=aNBT.getInteger("pulses");
	
	}
	@Override
	public void saveNBTData(NBTTagCompound aNBT) {
		
		super.saveNBTData(aNBT);
		aNBT.setInteger("lastSuccess", lastSuccess);
		aNBT.setInteger("pulses", pulses);
	}
	
	static {
		f= Arrays.stream(GT_MetaTileEntity_MultiBlockBase.class.getDeclaredFields())
		.filter(s->s.getType()==CheckRecipeResult.class)
		.findAny()
		.get();
		f.setAccessible(true);
		
	}
	public void start(int lastSuccess,int newSuccess){
		
		if(lastSuccess==1&&newSuccess==0){
			pulses++;
		}
	}
	@Override
	public boolean allowGeneralRedstoneOutput() {
		
		return true;
	}
	
	@Override
	public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTimer) {
	super.onPostTick(aBaseMetaTileEntity, aTimer);
		if(pulses>0){
			pulses--;
			BaseMetaTileEntity j;
			
			aBaseMetaTileEntity.setInternalOutputRedstoneSignal(aBaseMetaTileEntity.getFrontFacing(), (byte) 15);
			
		}else{
			
			aBaseMetaTileEntity.setInternalOutputRedstoneSignal(aBaseMetaTileEntity.getFrontFacing(), (byte) 0);
			
			
			
		}
	}
	
	@Override
	public CheckRecipeResult endRecipeProcessing(GT_MetaTileEntity_MultiBlockBase controller) {
		try {
			CheckRecipeResult res=(CheckRecipeResult) f.get(controller);
			int newSuccess=check(res);
			start(lastSuccess,newSuccess);
			lastSuccess=newSuccess;
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		return CheckRecipeResultRegistry.SUCCESSFUL;
	}

}
