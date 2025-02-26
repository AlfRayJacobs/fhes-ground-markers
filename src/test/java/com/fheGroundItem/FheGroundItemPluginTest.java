package com.fheGroundItem;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class FheGroundItemPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(fheGroundItemPlugin.class);
		RuneLite.main(args);
	}
}