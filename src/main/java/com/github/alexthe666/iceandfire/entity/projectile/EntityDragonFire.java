package com.github.alexthe666.iceandfire.entity.projectile;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.IceAndFireConfig;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.explosion.FireExplosion;
import com.github.alexthe666.iceandfire.entity.util.IDragonProjectile;
import com.github.alexthe666.iceandfire.entity.util.DragonUtils;
import com.github.alexthe666.iceandfire.enums.EnumParticle;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityDragonFire extends EntityFireball implements IDragonProjectile {

	public EntityDragonFire(World worldIn) {
		super(worldIn);
	}

	public EntityDragonFire(World worldIn, double posX, double posY, double posZ, double accelX, double accelY, double accelZ) {
		super(worldIn, posX, posY, posZ, accelX, accelY, accelZ);
	}

	public EntityDragonFire(World worldIn, EntityDragonBase shooter, double accelX, double accelY, double accelZ) {
		super(worldIn, shooter, accelX, accelY, accelZ);
		this.setSize(0.5F, 0.5F);
		double d0 = (double) MathHelper.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ);
		this.accelerationX = accelX / d0 * (0.1D * (shooter.isFlying() ? 4 * shooter.getDragonStage() : 1));
		this.accelerationY = accelY / d0 * (0.1D * (shooter.isFlying() ? 4 * shooter.getDragonStage() : 1));
		this.accelerationZ = accelZ / d0 * (0.1D * (shooter.isFlying() ? 4 * shooter.getDragonStage() : 1));
	}

	public void setSizes(float width, float height) {
		this.setSize(width, height);
	}

	protected boolean isFireballFiery() {
		return false;
	}

	@Override
	public boolean canBeCollidedWith() {
		return false;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if(this.world.isRemote) {
			for (int i = 0; i < 6; ++i) {
				IceAndFire.PROXY.spawnParticle(EnumParticle.DRAGON_FIRE, world, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
			}
		}
		if (ticksExisted > 160) {
			setDead();
		}
		if (this.isInWater()) {
			setDead();
		}
	}

	@Override
	protected void onImpact(RayTraceResult movingObject) {
		boolean flag = this.world.getGameRules().getBoolean("mobGriefing");

		if (!this.world.isRemote) {
			if (movingObject.entityHit instanceof IDragonProjectile) {
				return;
			}
			if (DragonUtils.isOwner(movingObject.entityHit, shootingEntity) || DragonUtils.hasSameOwner(movingObject.entityHit, shootingEntity)) {
				this.setDead();
				return;
			}
			if (movingObject.entityHit == null || !(movingObject.entityHit instanceof IDragonProjectile) && this.shootingEntity != null && this.shootingEntity instanceof EntityDragonBase && movingObject.entityHit != shootingEntity) {
				if (this.shootingEntity != null && this.shootingEntity instanceof EntityDragonBase && IceAndFireConfig.DRAGON_SETTINGS.dragonGriefing != 2) {
					FireExplosion explosion = new FireExplosion(world, shootingEntity, this.posX, this.posY, this.posZ, ((EntityDragonBase) this.shootingEntity).getDragonStage() * 2.5F, flag);
					explosion.doExplosionA();
					explosion.doExplosionB(true);
				} else if (movingObject.entityHit != null) {
					movingObject.entityHit.setFire(5);
				}
				this.setDead();
			}
			if (movingObject.entityHit != null && !(movingObject.entityHit instanceof IDragonProjectile) && !movingObject.entityHit.isEntityEqual(shootingEntity)) {
				if (this.shootingEntity != null && this.shootingEntity instanceof EntityDragonBase) {
					if (movingObject.entityHit instanceof EntityLivingBase && ((EntityLivingBase) movingObject.entityHit).getHealth() == 0) {
						((EntityDragonBase) this.shootingEntity).attackDecision = true;
					}
				}
				this.applyEnchantments(this.shootingEntity, movingObject.entityHit);
			}
		}
		this.setDead();
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		return false;
	}

	public void setAim(Entity fireball, Entity entity, float p_184547_2_, float p_184547_3_, float p_184547_4_, float p_184547_5_, float p_184547_6_) {
		float f = -MathHelper.sin(p_184547_3_ * 0.017453292F) * MathHelper.cos(p_184547_2_ * 0.017453292F);
		float f1 = -MathHelper.sin(p_184547_2_ * 0.017453292F);
		float f2 = MathHelper.cos(p_184547_3_ * 0.017453292F) * MathHelper.cos(p_184547_2_ * 0.017453292F);
		fireball.motionX = entity.motionX;
		fireball.motionZ = entity.motionZ;
		if (!entity.onGround) {
			fireball.motionY = entity.motionY;
		}
		this.setThrowableHeading(fireball, (double) f, (double) f1, (double) f2, p_184547_5_, p_184547_6_);
	}

	public void setThrowableHeading(Entity fireball, double x, double y, double z, float velocity, float inaccuracy) {
		x = x + this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
		y = y + this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
		z = z + this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
		x = x * (double) velocity;
		y = y * (double) velocity;
		z = z * (double) velocity;
		fireball.motionX = x;
		fireball.motionY = y;
		fireball.motionZ = z;
		float f1 = MathHelper.sqrt(x * x + z * z);
		fireball.rotationYaw = (float) (MathHelper.atan2(x, z) * (180D / Math.PI));
		fireball.rotationPitch = (float) (MathHelper.atan2(y, (double) f1) * (180D / Math.PI));
		fireball.prevRotationYaw = fireball.rotationYaw;
		fireball.prevRotationPitch = fireball.rotationPitch;
	}

	public float getCollisionBorderSize() {
		return 1F;
	}

}