/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */

package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.Ride;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Broadcast;

/**
 * Support for /mount command.  
 * @author Tempy
 */
public class Mount implements IUserCommandHandler
{
    private static final int[] COMMAND_IDS = { 61 }; 
	
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#useUserCommand(int, net.sf.l2j.gameserver.model.L2PcInstance)
     */
    public synchronized boolean useUserCommand(int id, L2PcInstance activeChar)
    {
        if (id != COMMAND_IDS[0]) return false;
        
        L2Summon pet = activeChar.getPet();

        if (pet != null && pet.isMountable() && !activeChar.isMounted()) 
        {
            if (activeChar.isDead())
            {
                // A strider cannot be ridden when player is dead.
                SystemMessage msg = new SystemMessage(SystemMessage.STRIDER_CANT_BE_RIDDEN_WHILE_DEAD);
                activeChar.sendPacket(msg);
            }
            else if (pet.isDead())
            {   
                // A dead strider cannot be ridden.
                SystemMessage msg = new SystemMessage(SystemMessage.DEAD_STRIDER_CANT_BE_RIDDEN);
                activeChar.sendPacket(msg);
            }
            else if (pet.isInCombat())
            {
                // A strider in battle cannot be ridden.
                SystemMessage msg = new SystemMessage(SystemMessage.STRIDER_IN_BATLLE_CANT_BE_RIDDEN);
                activeChar.sendPacket(msg);
            }
            else if (activeChar.isInCombat())
            {
                // A pet cannot be ridden while player is in battle.
                SystemMessage msg = new SystemMessage(SystemMessage.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
                activeChar.sendPacket(msg);                        
            }                   
            else if (activeChar.isSitting() || activeChar.isMoving())
            {
                // A strider can be ridden only when player is standing.
                SystemMessage msg = new SystemMessage(SystemMessage.STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING);
                activeChar.sendPacket(msg);
            }
            else if (!pet.isDead() && !activeChar.isMounted())
            {
                // Don't allow ride with a cursed weapon equiped
                if (activeChar.isCursedWeaponEquiped()) return false;

                // Unequip the weapon
                L2ItemInstance wpn = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
                if (wpn == null) wpn = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
                if (wpn != null)
                {
                	if (wpn.isWear())
                		return false;
                	
                    L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
                    InventoryUpdate iu = new InventoryUpdate();
                    for (int i = 0; i < unequiped.length; i++)
                        iu.addModifiedItem(unequiped[i]);
                    activeChar.sendPacket(iu);

                    activeChar.abortAttack();
                    activeChar.refreshExpertisePenalty();
                    activeChar.broadcastUserInfo();

                    // this can be 0 if the user pressed the right mousebutton twice very fast
                    if (unequiped.length > 0)
                    {
                        if (unequiped[0].isWear())
                            return false;
                        
                        SystemMessage sm = null;
                        if (unequiped[0].getEnchantLevel() > 0)
                        {
                            sm = new SystemMessage(SystemMessage.EQUIPMENT_S1_S2_REMOVED);
                            sm.addNumber(unequiped[0].getEnchantLevel());
                            sm.addItemName(unequiped[0].getItemId());
                        }
                        else
                        {
                            sm = new SystemMessage(SystemMessage.S1_DISARMED);
                            sm.addItemName(unequiped[0].getItemId());
                        }
                        activeChar.sendPacket(sm);
                    }
                }
                
                // Unequip the shield
                L2ItemInstance sld = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
                if (sld != null)
                {
                	if (sld.isWear())
                		return false;
                	
                    L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInBodySlotAndRecord(sld.getItem().getBodyPart());
                    InventoryUpdate iu = new InventoryUpdate();
                    for (int i = 0; i < unequiped.length; i++)
                        iu.addModifiedItem(unequiped[i]);
                    activeChar.sendPacket(iu);

                    activeChar.abortAttack();
                    activeChar.refreshExpertisePenalty();
                    activeChar.broadcastUserInfo();

                    // this can be 0 if the user pressed the right mousebutton twice very fast
                    if (unequiped.length > 0)
                    {
                        SystemMessage sm = null;
                        if (unequiped[0].getEnchantLevel() > 0)
                        {
                            sm = new SystemMessage(SystemMessage.EQUIPMENT_S1_S2_REMOVED);
                            sm.addNumber(unequiped[0].getEnchantLevel());
                            sm.addItemName(unequiped[0].getItemId());
                        }
                        else
                        {
                            sm = new SystemMessage(SystemMessage.S1_DISARMED);
                            sm.addItemName(unequiped[0].getItemId());
                        }
                        activeChar.sendPacket(sm);
                    }
                }
            	
                Ride mount = new Ride(activeChar.getObjectId(), Ride.ACTION_MOUNT, pet.getTemplate().npcId);
                Broadcast.toSelfAndKnownPlayersInRadius(activeChar, mount, 810000/*900*/);
                activeChar.setMountType(mount.getMountType());
                activeChar.setMountObjectID(pet.getControlItemId());
                pet.unSummon(activeChar);
            }
        }
        else if (activeChar.isRentedPet())
        {
        	activeChar.stopRentPet();
        }
        else if (activeChar.isMounted())
        {
            // Unequip the weapon
            L2ItemInstance wpn = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
            if (wpn == null) wpn = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
            if (wpn != null)
            {
            	if (wpn.isWear())
            		return false;
            	
                L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
                InventoryUpdate iu = new InventoryUpdate();
                for (int i = 0; i < unequiped.length; i++)
                    iu.addModifiedItem(unequiped[i]);
                activeChar.sendPacket(iu);

                activeChar.abortAttack();
                activeChar.refreshExpertisePenalty();
                activeChar.broadcastUserInfo();

                // this can be 0 if the user pressed the right mousebutton twice very fast
                if (unequiped.length > 0)
                {
                    SystemMessage sm = null;
                    if (unequiped[0].getEnchantLevel() > 0)
                    {
                        sm = new SystemMessage(SystemMessage.EQUIPMENT_S1_S2_REMOVED);
                        sm.addNumber(unequiped[0].getEnchantLevel());
                        sm.addItemName(unequiped[0].getItemId());
                    }
                    else
                    {
                        sm = new SystemMessage(SystemMessage.S1_DISARMED);
                        sm.addItemName(unequiped[0].getItemId());
                    }
                    activeChar.sendPacket(sm);
                }
            }
            
            // Unequip the shield
            L2ItemInstance sld = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
            if (sld != null)
            {
            	if (sld.isWear())
            		return false;
            	
                L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInBodySlotAndRecord(sld.getItem().getBodyPart());
                InventoryUpdate iu = new InventoryUpdate();
                for (int i = 0; i < unequiped.length; i++)
                    iu.addModifiedItem(unequiped[i]);
                activeChar.sendPacket(iu);

                activeChar.abortAttack();
                activeChar.refreshExpertisePenalty();
                activeChar.broadcastUserInfo();

                // this can be 0 if the user pressed the right mousebutton twice very fast
                if (unequiped.length > 0)
                {
                    SystemMessage sm = null;
                    if (unequiped[0].getEnchantLevel() > 0)
                    {
                        sm = new SystemMessage(SystemMessage.EQUIPMENT_S1_S2_REMOVED);
                        sm.addNumber(unequiped[0].getEnchantLevel());
                        sm.addItemName(unequiped[0].getItemId());
                    }
                    else
                    {
                        sm = new SystemMessage(SystemMessage.S1_DISARMED);
                        sm.addItemName(unequiped[0].getItemId());
                    }
                    activeChar.sendPacket(sm);
                }
            }
        	
        	if (activeChar.isFlying())activeChar.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
			Ride dismount = new Ride(activeChar.getObjectId(), Ride.ACTION_DISMOUNT, 0);
			Broadcast.toSelfAndKnownPlayers(activeChar, dismount);
            activeChar.setMountType(0);
            activeChar.setMountObjectID(0);
        }
        
        return true;
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#getUserCommandList()
     */
    public int[] getUserCommandList()
    {
        return COMMAND_IDS;
    }
}