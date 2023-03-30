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
import com.badlogic.gdx.utils.Array;

/** A node for a {@link TiledGraph}.
 * 
 * @author davebaol */
public class TiledNode {

	private int x;

	private int y;

	private int type;

	// Getters
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getType(){
		return type;
	}

	private Array<Connection<TiledNode>> connections;

	public TiledNode(int x, int y, int type) {
		this.x = x;
		this.y = y;
		this.type = type;
		this.connections =  new Array<>(4);
	}

	public Array<Connection<TiledNode>> getConnections () {
		return this.connections;
	}
}
