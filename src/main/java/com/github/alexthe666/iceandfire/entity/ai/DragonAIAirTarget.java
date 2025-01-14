package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.api.IEntityEffectCapability;
import com.github.alexthe666.iceandfire.api.InFCapabilities;
import com.github.alexthe666.iceandfire.entity.util.DragonUtils;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class DragonAIAirTarget extends EntityAIBase {
	private EntityDragonBase dragon;

	public DragonAIAirTarget(EntityDragonBase dragon) {
		this.dragon = dragon;
	}

	public boolean shouldExecute() {
		if (dragon != null) {
			if (!dragon.isFlying() && !dragon.isHovering() || dragon.onGround) {
				return false;
			}
			if (dragon.isSleeping()) {
				return false;
			}
			if (dragon.isChild()) {
				return false;
			}
			if (dragon.getOwner() != null && dragon.getPassengers().contains(dragon.getOwner())) {
				return false;
			}
			if (dragon.airTarget != null && (dragon.isTargetBlocked(new Vec3d(dragon.airTarget)))) {
				dragon.airTarget = null;
			}

			if (dragon.airTarget != null) {
				return false;
			} else {
				Vec3d vec = this.findAirTarget();

				if (vec == null) {
					return false;
				} else {
					dragon.airTarget = new BlockPos(vec.x, vec.y, vec.z);
					return true;
				}
			}
		}
		return false;
	}

	public boolean continueExecuting() {
		IEntityEffectCapability capability = InFCapabilities.getEntityEffectCapability(dragon);
		if (!dragon.isFlying() && !dragon.isHovering()) {
			return false;
		}
		if (dragon.isSleeping()) {
			return false;
		}
		if (dragon.isChild()) {
			return false;
		}
		if (capability != null && capability.isStoned()) {
			return false;
		}
		return dragon.airTarget != null;
	}

	public Vec3d findAirTarget() {
		return new Vec3d(getNearbyAirTarget());
	}

	public BlockPos getNearbyAirTarget() {
		if (dragon.getAttackTarget() == null) {
			BlockPos pos = DragonUtils.getBlockInView(dragon);
			if (pos != null && dragon.world.getBlockState(pos).getMaterial() == Material.AIR) {
				return pos;
			}
		} else {
			return new BlockPos((int) dragon.getAttackTarget().posX, (int) dragon.getAttackTarget().posY, (int) dragon.getAttackTarget().posZ);
		}
		return dragon.getPosition();
	}
}