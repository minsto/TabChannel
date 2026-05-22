package com.mickdev.tabchannel;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class ChannelSavedData extends SavedData {

	/** Identifiant SavedData sur disque ({@code world/data/&lt;id&gt;.dat} pour l’Overworld). */
	public static final String DATA_ID = "tabchannel_chat_channels";

	public ChannelSavedData() {
		ChatManager.resetForNewServer();
	}

	public static ChannelSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
		ChatManager.resetForNewServer();

		ListTag channelsList = tag.getList("channels", 10);
		for (int i = 0; i < channelsList.size(); i++) {
			CompoundTag channelTag = channelsList.getCompound(i);

			String id = channelTag.getString("id");
			String displayName = channelTag.getString("displayName");
			ChannelVisibility visibility = ChannelVisibility.valueOf(channelTag.getString("visibility"));
			UUID owner = channelTag.getUUID("owner");

			ChatChannel channel = new ChatChannel(id, displayName, visibility, owner);
			channel.setOriginalGlobal(channelTag.getBoolean("originalGlobal"));
			channel.setAntiSwear(channelTag.getBoolean("antiSwear"));
			channel.setRules(channelTag.getString("rules"));
			if (channelTag.contains("tabColor")) {
				channel.setTabColor(channelTag.getString("tabColor"));
			}

			ListTag membersList = channelTag.getList("members", 10);
			for (int j = 0; j < membersList.size(); j++) {
				CompoundTag memberTag = membersList.getCompound(j);
				UUID playerId = memberTag.getUUID("player");
				ChannelRole role = ChannelRole.valueOf(memberTag.getString("role"));
				channel.addMember(playerId, role);
			}

			ChatManager.addLoadedChannel(channel);
		}

		return new ChannelSavedData();
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
		ListTag channelsList = new ListTag();

		for (ChatChannel channel : ChatManager.getChannels()) {
			if (channel.isOriginalGlobal()) {
				continue;
			}

			CompoundTag channelTag = new CompoundTag();
			channelTag.putString("id", channel.getId());
			channelTag.putString("displayName", channel.getDisplayName());
			channelTag.putString("visibility", channel.getVisibility().name());
			channelTag.putUUID("owner", channel.getOwner());
			channelTag.putBoolean("originalGlobal", channel.isOriginalGlobal());
			channelTag.putBoolean("antiSwear", channel.isAntiSwear());
			channelTag.putString("rules", channel.getRules());
			channelTag.putString("tabColor", channel.getTabColor());

			ListTag membersList = new ListTag();
			for (Map.Entry<UUID, ChannelMemberData> entry : channel.getMembers().entrySet()) {
				CompoundTag memberTag = new CompoundTag();
				memberTag.putUUID("player", entry.getKey());
				memberTag.putString("role", entry.getValue().getRole().name());
				membersList.add(memberTag);
			}

			channelTag.put("members", membersList);
			channelsList.add(channelTag);
		}

		tag.put("channels", channelsList);
		return tag;
	}

	/**
	 * Persistance des canaux : toujours l’Overworld pour un seul fichier cohérent
	 * ({@code world/data/}{@link #DATA_ID}{@code .dat}), quel que soit le niveau courant du joueur.
	 */
	public static ChannelSavedData get(ServerLevel level) {
		ServerLevel overworld = level.getServer().overworld();

		return overworld.getDataStorage().computeIfAbsent(
				new SavedData.Factory<>(
						ChannelSavedData::new,
						ChannelSavedData::load,
						null
				),
				DATA_ID
		);
	}
}
