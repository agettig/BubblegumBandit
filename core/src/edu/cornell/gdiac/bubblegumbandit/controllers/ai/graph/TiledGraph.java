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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.bubblegumbandit.view.GameCanvas;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import edu.cornell.gdiac.physics.obstacle.Obstacle;

public class TiledGraph implements IndexedGraph<TiledNode>{
	protected int width;

	protected int height;

	private int boardOffset;

	private Vector2 scale;

	protected TiledNode[] nodes;

	private float debugSize;


	public static final int GRAVITY_UP_UNRESTRICTED = 1;

	public static final int GRAVITY_UP_RESTRICRED = 2;

	private static final int GRAVITY_DOWN_UNRESTRICTED = 3;

	public static final int GRAVITY_DOWN_RESTRICTED = 4;


	public TiledGraph(JsonValue boardJson, int boardIdOffset, Vector2 scale, float debugSize) {
		this.scale = scale;
		this.height = boardJson.getInt("height");
		this.width = boardJson.getInt("width");
		this.nodes = new TiledNode[width * height];
		this.boardOffset = boardIdOffset;
		this.debugSize = debugSize;

		int[] jsonTiles = boardJson.get("data").asIntArray();
		int x = 0;
		int y = height - 1;
		for (int i = 0; i < jsonTiles.length; i++) {
			int type = 0;
			if (jsonTiles[i] != 0) {
				type = jsonTiles[i] - boardIdOffset + 1;
			}
			nodes[y * width + x] = new TiledNode(x, y, type );
			x++;
			if (x == width) {
				y--;
				x = 0;
			}
		}


		for (int j = 0; j < height; j++) {
			int idx = j * width;
			for (int k = 0; k < width; k++) {
				TiledNode n = nodes[idx + k];
				if (k > 0) addConnection(n, -1, 0);
				if (j > 0) addConnection(n, 0, -1);
				if (k < width - 1) addConnection(n, 1, 0);
				if (j < height - 1) addConnection(n, 0, 1);
			}
		}
	}

	public TiledNode getNode (int x, int y) {
		return nodes[y * width + x];
	}

	public TiledNode getNode (int index) {
		return nodes[index];
	}

	public int getIndex (TiledNode node) {
		return node.getX() * height + node.getY();
	}

	public int getNodeCount () {
		return nodes.length;
	}

	public Array<Connection<TiledNode>> getConnections (TiledNode fromNode) {
		return fromNode.getConnections();
	}

	private void addConnection (TiledNode n, int xOffset, int yOffset) {
		int a = n.getX() + xOffset;
		int b = n.getY() + yOffset;

		TiledNode target = getNode(n.getX() + xOffset, n.getY() + yOffset);
		if (target.getType() != 0){
			n.getConnections().add(new TiledEdge(this, n, target));
		}
	}

	public void drawGraph(GameCanvas canvas) {
		PolygonShape s = new PolygonShape();
		s.setAsBox(this.scale.x*debugSize, this.scale.y*debugSize); //smaller than grid squares
		float margin = this.scale.x*1/2;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int val = nodes[j * width + i].getType();
				if (val != 0) {
					Color color;
					if (val == GRAVITY_DOWN_UNRESTRICTED) color = Color.BLUE;
					else if (val == GRAVITY_UP_UNRESTRICTED) {
						color = Color.RED;
					} else if (val == GRAVITY_UP_RESTRICRED){
						color = Color.GREEN;
					}
					else{
						color = Color.PURPLE;
					}
					canvas.drawPhysics(s, color, i * scale.x + margin, j * scale.y + margin);
				}
			}
		}
	}

}
