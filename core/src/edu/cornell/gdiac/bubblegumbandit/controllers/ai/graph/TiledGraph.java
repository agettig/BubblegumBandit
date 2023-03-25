/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package edu.cornell.gdiac.bubblegumbandit.controllers.ai.graph;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;

public class FlatTiledGraph {
	protected int width;

	protected int height;

	private Vector2 scale;

	protected Array<TiledNode> nodes;

	public boolean diagonal;
	public TiledNode startNode;


	private static final int GRAVITY_DOWN_TILE = 1;

	private static final int GRAVITY_UP_TILE = 3;

	private static final int BOTH_GRAVITY_TILE = 2;


	public FlatTiledGraph (JsonValue boardJson, int boardIdOffset, Vector2 scale) {
		this.scale = scale;
		this.height = boardJson.getInt("height");
		this.width = boardJson.getInt("width");
		this.nodes = new Array<TiledNode>(width * height);
		this.diagonal = false;
		this.startNode = null;

		for (int i = 0; i < width; i++){
			for (int j = 0; j < height; j++){
				nodes.add(new TiledNode(i, j, , 4));
			}
		}
	}

	public void init (int roomCount, int roomMinSize, int roomMaxSize, int squashIterations) {
//		int map[][] = DungeonUtils.generate(sizeX, sizeY, roomCount, roomMinSize, roomMaxSize, squashIterations);
//		for (int x = 0; x < sizeX; x++) {
//			for (int y = 0; y < sizeY; y++) {
//				nodes.add(new FlatTiledNode(x, y, map[x][y], 4));
//			}
//		}
//
//		// Each node has up to 4 neighbors, therefore no diagonal movement is possible
//		for (int x = 0; x < sizeX; x++) {
//			int idx = x * sizeY;
//			for (int y = 0; y < sizeY; y++) {
//				FlatTiledNode n = nodes.get(idx + y);
//				if (x > 0) addConnection(n, -1, 0);
//				if (y > 0) addConnection(n, 0, -1);
//				if (x < sizeX - 1) addConnection(n, 1, 0);
//				if (y < sizeY - 1) addConnection(n, 0, 1);
//			}
//		}
	}

	public TiledNode getNode (int x, int y) {
		return nodes.get(x * sizeY + y);
	}

	public TiledNode getNode (int index) {
		return nodes.get(index);
	}

	public int getIndex (TiledNode node) {
		return node.getIndex();
	}

	public int getNodeCount () {
		return nodes.size;
	}

	public Array<Connection<TiledNode>> getConnections (TiledNode fromNode) {
		return fromNode.getConnections();
	}

	private void addConnection (TiledNode n, int xOffset, int yOffset) {
		TiledNode target = getNode(n.x + xOffset, n.y + yOffset);
		if (target.type == TiledNode.TILE_FLOOR) n.getConnections().add(new FlatTiledConnection(this, n, target));
	}

}
