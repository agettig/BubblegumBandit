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

/** A connection for a {@link TiledGraph}.
 * 
 * @author davebaol */
public class TiledEdge implements Connection<TiledNode>{

	private TiledNode fromNode;

	private TiledNode toNode;
	TiledGraph worldMap;

	public TiledEdge(TiledGraph worldMap, TiledNode fromNode, TiledNode toNode) {
		this.worldMap = worldMap;
		this.fromNode = fromNode;
		this.toNode = toNode;
	}

	public float getCost () {
		return 1.0f;
	}

	@Override
	public TiledNode getFromNode() {
		return fromNode;
	}

	@Override
	public TiledNode getToNode() {
		return toNode;
	}
}
