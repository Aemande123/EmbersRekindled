package teamroots.embers.tileentity;

import net.minecraft.util.EnumFacing;
import teamroots.embers.util.EnumPipeConnection;

public interface IEmberPipeConnectable {
    EnumPipeConnection getConnection(EnumFacing facing);
}
