/*
 * Copyright (c) 2018, Tomas Slusny <slusnucky@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.fheGroundItem;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class fheGroundItemOverlay extends Overlay
{
    public List<TileInfo> colourTiles = new ArrayList<>();
    public List<TileDropInfo> myDroppedTiles = new ArrayList<>();
    public List<TileDropInfo> othersDroppedTiles = new ArrayList<>();

    public static class TileDropInfo{
        LocalPoint location;
        Integer itemId;
        Tile tile;

        TileDropInfo(LocalPoint location, Integer itemId, Tile tile){
            this.location = location;
            this.itemId = itemId;
            this.tile = tile;
        }
    }

    public static class TileInfo{
        LocalPoint location;
        Color colour;
        LocalDateTime dateAdded;
        Integer itemId;
        Tile tile;

        TileInfo(LocalPoint location, Color colour, Integer itemId, Tile tile){
            this.location = location;
            this.colour = colour;
            this.itemId = itemId;
            this.tile = tile;

            dateAdded = LocalDateTime.now();
        }
    }

    private final Client _client;
    private final fheGroundItemConfig _config;

    @Inject
    private fheGroundItemOverlay(Client client, fheGroundItemConfig config)
    {
        _client = client;
        _config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(PRIORITY_MED);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        for(TileDropInfo myTile : myDroppedTiles) {
            if (myTile.tile.getGroundItems() == null) {
                myDroppedTiles.remove(myTile);
                continue;
            }
            if (myTile.tile.getGroundItems().isEmpty()) {
                myDroppedTiles.remove(myTile);
                continue;
            }
            if (myTile.tile.getGroundItems().stream().noneMatch(x -> x.getId() == myTile.itemId)) {
                    myDroppedTiles.remove(myTile);
                    continue;
            }
        }

        for(TileDropInfo othersTile : othersDroppedTiles){
            if (othersTile.tile.getGroundItems() == null){
                othersDroppedTiles.remove(othersTile);
                continue;
            }
            if (othersTile.tile.getGroundItems().isEmpty()) {
                othersDroppedTiles.remove(othersTile);
                continue;
            }
            if (othersTile.tile.getGroundItems().stream().noneMatch(x -> x.getId() == othersTile.itemId)){
                othersDroppedTiles.remove(othersTile);
                continue;
            }
            renderTile(graphics, othersTile.location, Color.RED, 2, new Color(255, 0, 0, 40));
        }

        for(TileInfo tile : colourTiles){
            if (tile.tile.getGroundItems() == null){
                colourTiles.remove(tile);
                continue;
            }

            if (tile.tile.getGroundItems().isEmpty()) {
                colourTiles.remove(tile);
                continue;
            }

            if (tile.tile.getGroundItems().stream().noneMatch(x -> x.getId() == tile.itemId)){
                colourTiles.remove(tile);
                continue;
            }

            if(tile.dateAdded.isBefore(LocalDateTime.now().minusMinutes(1))){
                colourTiles.remove(tile);
                continue;
            }

            renderTile(graphics, tile.location, tile.colour, 2, new Color(0, 0, 0, 0));
        }

        return null;
    }

    private void renderTile(final Graphics2D graphics, final LocalPoint dest, final Color color, final double borderWidth, final Color fillColor)
    {
        if (dest == null)
        {
            return;
        }

        final Polygon poly = Perspective.getCanvasTilePoly(_client, dest);

        if (poly == null)
        {
            return;
        }

        OverlayUtil.renderPolygon(graphics, poly, color, fillColor, new BasicStroke((float) borderWidth));
    }
}
