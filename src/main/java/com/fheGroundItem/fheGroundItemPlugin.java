package com.fheGroundItem;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@PluginDescriptor(
		name = "Fhe's Extra Ground Markers",
		description = "Set different ground items to different colours. Highlight special items (brews/restores/overloads) not dropped by you",
		tags = {"loot"}
)

@Slf4j
public class fheGroundItemPlugin extends Plugin
{
	@Inject
	private OverlayManager overlayManager;

	@Inject
	private fheGroundItemOverlay overlay;

	@Inject
	private fheGroundItemConfig _config;

	@Inject
	private Client client;

	private Map<Integer, Color> _configColourSets;

	public fheGroundItemPlugin(){
		if(_config == null)
			return;

		_configColourSets = parseColorString(_config.IdColourPairs());
	}

	@Provides
	fheGroundItemConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(fheGroundItemConfig.class);
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned itemSpawned)
	{
		var tile = itemSpawned.getTile();
		var item = itemSpawned.getItem();
		//Brews & restores
		var notableIds = new Integer[] { 3024, 3026, 3028, 3030, 6685, 6687, 6689, 6691 };

		//Xerics aids, revitalisations, prayer enhances & overloads
		if((item.getId() >= 20949 && item.getId() <= 20996) || (Arrays.stream(notableIds).anyMatch(x -> x == item.getId()))) {
			if(tile.getWorldLocation().getX() == client.getLocalPlayer().getWorldLocation().getX() &&
				tile.getWorldLocation().getY() == client.getLocalPlayer().getWorldLocation().getY()) {
				overlay.myDroppedTiles.add(new fheGroundItemOverlay.TileDropInfo(tile.getLocalLocation(), item.getId(), tile));
			}
			else
			{
				overlay.othersDroppedTiles.add(new fheGroundItemOverlay.TileDropInfo(tile.getLocalLocation(), item.getId(), tile));
			}
		}

		var configColourEntry = _configColourSets.get(item.getId());
		if (configColourEntry == null)
			return;

		overlay.colourTiles.add(new fheGroundItemOverlay.TileInfo(tile.getLocalLocation(), configColourEntry, item.getId(), tile));
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned itemDespawned){
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		_configColourSets = parseColorString(_config.IdColourPairs());
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		_configColourSets = parseColorString(_config.IdColourPairs());

		System.out.println("Startup completed");
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
	}

	public static Map<Integer, Color> parseColorString(String input) {
		Map<Integer, Color> colorDictionary = new HashMap<>();
		String[] pairs = input.split(",");

		for (String pair : pairs) {
			String[] parts = pair.split(":");
			int id = Integer.parseInt(parts[0]);

			String colorHex = parts[1];
			if (!colorHex.startsWith("#")) {
				colorHex = "#" + colorHex;
			}

			Color color = Color.decode(colorHex);
			colorDictionary.put(id, color);
		}

		return colorDictionary;
	}
}
