package am2.affinity.abilities;

import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.extensions.AffinityData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class AbilitySolidBones extends AbstractAffinityAbility {

	public AbilitySolidBones() {
		super(new ResourceLocation("arsmagica2", "solidbones"));
	}

	@Override
	protected float getMinimumDepth() {
		return 0.5f;
	}

	@Override
	protected Affinity getAffinity() {
		return Affinity.EARTH;
	}

	@Override
	public void applyTick(EntityPlayer player) {
		if (player.isInWater()) {
			float earthDepth = AffinityData.For(player).getAffinityDepth(Affinity.EARTH);
			if (player.motionY > -0.3f) {
				player.addVelocity(0, -0.01f * earthDepth, 0);
			}
		}
	}
	
	@Override
	public void applyFall(EntityPlayer player, LivingFallEvent event) {
		float earthDepth = AffinityData.For(player).getAffinityDepth(Affinity.EARTH);
		event.setDistance((float) (event.getDistance() + (1.25 * (earthDepth))));
	}
	
	@Override
	public void applyHurt(EntityPlayer player, LivingHurtEvent event, boolean isAttacker) {
		if (!isAttacker) {
			float earthDepth = AffinityData.For(player).getAffinityDepth(Affinity.EARTH);
			float reduction = 0.1f * earthDepth;
			event.setAmount(event.getAmount() - (event.getAmount() * reduction));
		}
	}
}