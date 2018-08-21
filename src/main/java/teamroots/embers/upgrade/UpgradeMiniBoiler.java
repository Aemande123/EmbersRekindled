package teamroots.embers.upgrade;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import teamroots.embers.api.tile.IMechanicallyPowered;
import teamroots.embers.api.upgrades.IUpgradeProvider;
import teamroots.embers.api.upgrades.UpgradeUtil;
import teamroots.embers.tileentity.TileEntityCatalyticPlug;
import teamroots.embers.tileentity.TileEntityMiniBoiler;
import teamroots.embers.util.DefaultUpgradeProvider;

import java.util.HashSet;
import java.util.List;

public class UpgradeMiniBoiler extends DefaultUpgradeProvider {
    public UpgradeMiniBoiler(TileEntity tile) {
        super("mini_boiler", tile);
    }

    boolean active;
    double heat;

    @Override
    public int getLimit(TileEntity tile) {
        return tile instanceof IMechanicallyPowered ? 0 : 4;
    }

    @Override
    public double transformEmberConsumption(TileEntity tile, double ember) {
        setHeat(ember);
        return ember;
    }

    @Override
    public double transformEmberProduction(TileEntity tile, double ember) {
        setHeat(ember);
        return ember;
    }

    public void setHeat(double heat) {
        this.heat = heat;
        this.active = true;
    }

    @Override
    public boolean doWork(TileEntity tile, List<IUpgradeProvider> upgrades) {
        if(active) {
            if (this.tile instanceof TileEntityMiniBoiler)
                ((TileEntityMiniBoiler) this.tile).boil(heat);
            active = false;
        }

        return false; //No cancel
    }
}
