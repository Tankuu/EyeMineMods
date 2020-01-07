/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.messages;

import javax.xml.ws.handler.MessageContext;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

public class AddItemToHotbar implements IMessage {
    
    private ItemStack item;
    private int slotId = -1;

    public AddItemToHotbar() { }

    public AddItemToHotbar(ItemStack item, int id) {
        this.item = item;
        this.slotId = id;
    }

    public AddItemToHotbar(ItemStack item) {
        this.item = item;
    }
    
    @Override
    public void fromBytes(ByteBuf buf) {
        item = ByteBufUtils.readItemStack(buf);
        slotId = ByteBufUtils.readVarInt(buf, 5);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, item);
        ByteBufUtils.writeVarInt(buf, slotId, 5);
    }

    public static class Handler implements IMessageHandler<AddItemToHotbar, IMessage> {        
    	@Override
        public IMessage onMessage(final AddItemToHotbar message,final MessageContext ctx) {
            IThreadListener mainThread = (WorldServer) ctx.getServerHandler().playerEntity.world; // or Minecraft.getInstance() on the client
            mainThread.addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    PlayerEntity player = ctx.getServerHandler().playerEntity;
                    PlayerInventory inventory = player.inventory;
                    
                    if (message.slotId < 0) {
                    	message.slotId = inventory.getBestHotbarSlot();
                    }
            		inventory.setInventorySlotContents(message.slotId, message.item);
            		inventory.currentItem = message.slotId;
                }
            });
            return null; // no response in this case
        }
    }
}