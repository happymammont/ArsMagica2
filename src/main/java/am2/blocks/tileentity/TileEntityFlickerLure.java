package am2.blocks.tileentity;

import java.util.List;

import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.api.flickers.FlickerGenerationPool;
import am2.defs.ItemDefs;
import am2.entity.EntityFlicker;
import am2.power.PowerNodeRegistry;
import am2.power.PowerTypes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityFlickerLure extends TileEntityAMPower{
	
	private int ticksExisted = 0;

	public TileEntityFlickerLure(){
		super(200);
	}

	@Override
	public boolean canRelayPower(PowerTypes type){
		return false;
	}

	@Override
	public int getChargeRate(){
		return 5;
	}

	@Override
	public void update(){
		if (worldObj.isRemote)
			return;
		super.update();

		if (worldObj.isBlockIndirectlyGettingPowered(pos) > 0){
			if (worldObj.rand.nextDouble() < 0.05f && PowerNodeRegistry.For(worldObj).checkPower(this, 20)){
				EntityFlicker flicker = new EntityFlicker(worldObj);
				flicker.setPosition(pos.getX() + 0.5f, pos.getY() + 1.5f, pos.getZ() + 0.5f);
				flicker.setFlickerType(FlickerGenerationPool.INSTANCE.getWeightedAffinity());
				worldObj.spawnEntityInWorld(flicker);
				PowerNodeRegistry.For(worldObj).consumePower(this, PowerNodeRegistry.For(worldObj).getHighestPowerType(this), 20);
			}
		}
		
		if (ticksExisted % 200 == 0) {
			TileEntity eastTile = worldObj.getTileEntity(pos.east());
			TileEntity westTile = worldObj.getTileEntity(pos.west());
			TileEntity southTile = worldObj.getTileEntity(pos.south());
			TileEntity northTile = worldObj.getTileEntity(pos.north());
			List<EntityFlicker> flickers = worldObj.getEntitiesWithinAABB(EntityFlicker.class, new AxisAlignedBB(pos).expandXyz(5));
			for (EntityFlicker flicker : flickers) {
				IInventory inventory = null;
				if (northTile != null && northTile instanceof IInventory)
					inventory = (IInventory) northTile;
				else if (southTile != null && southTile instanceof IInventory)
					inventory = (IInventory) southTile;
				else if (eastTile != null && eastTile instanceof IInventory)
					inventory = (IInventory) eastTile;
				else if (westTile != null && westTile instanceof IInventory)
					inventory = (IInventory) westTile;
				if (inventory != null) {
					ItemStack jar = null;
					for (int i = 0; i < inventory.getSizeInventory(); i++) {
						ItemStack is = inventory.getStackInSlot(i);
						if (is == null || is.getItem() != ItemDefs.flickerJar || is.getItemDamage() != ArsMagicaAPI.getAffinityRegistry().getId(Affinity.NONE)) continue;
						is.stackSize--;
						if (is.stackSize <= 0)
							is = null;
						inventory.setInventorySlotContents(i, is);
						jar = new ItemStack(ItemDefs.flickerJar, 1, 0);
						ItemDefs.flickerJar.setFlickerJarTypeFromFlicker(jar, flicker);
						break;
					}
					if (jar != null) {
						boolean placed = false;
						for (int i = 0; i < inventory.getSizeInventory(); i++) {
							ItemStack is = inventory.getStackInSlot(i);
							if (is == null) {
								inventory.setInventorySlotContents(i, jar);
								placed = true;
							} else if (is.isItemEqual(jar) && is.stackSize < 64) {
								is.stackSize++;
								placed = true;
							}
							if (placed) break;
						}
						if (!placed) {
							EntityItem item = new EntityItem(worldObj, pos.getX() + 0.5D, pos.getY() + 1.5D, pos.getZ() + 0.5D, jar);
							worldObj.spawnEntityInWorld(item);
						}
						flicker.setDead();
					}
				}
			}
		}
		ticksExisted++;
	}
}
