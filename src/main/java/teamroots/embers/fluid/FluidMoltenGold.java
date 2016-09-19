package teamroots.embers.fluid;

import java.awt.Color;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import teamroots.embers.Embers;
import teamroots.embers.RegistryManager;

public class FluidMoltenGold extends Fluid {
	public FluidMoltenGold() {
		super("moltenGold",new ResourceLocation(Embers.MODID+":blocks/moltenGoldStill"),new ResourceLocation(Embers.MODID+":blocks/moltenGoldFlowing"));
		setViscosity(6000);
		setDensity(2000);
		setLuminosity(15);
		setTemperature(900);
		setBlock(RegistryManager.blockMoltenGold);
		setUnlocalizedName("moltenGold");
	}
	
	@Override
	public int getColor(){
		return Color.WHITE.getRGB();
	}
}
