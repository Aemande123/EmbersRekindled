package teamroots.embers.power;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public interface IEmberPacketProducer {
	public void setTargetPosition(BlockPos pos, EnumFacing side);
}
