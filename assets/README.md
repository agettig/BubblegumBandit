# Using Tiled for this project

### Important: In Tiled Preferences, check "Embed Tilesets" (this is a new change as of 3/28) and "Resolve object types and properties" under "Export Options". Otherwise, you will get errors.

## Making a new level
To make a new level, you can either copy the existing one and use as a template or create a new map.

It's easier to start with the map size as Infinite and change it to Fixed in Map Properties later.
Map properties: map size should be Fixed (once you export), Width 64px, Height 64px
Map layers: Objects (object layer), Terrain (tile layer), and Board (tile layer). 

## Editing a level
Add tiles for the terrain in the Terrain layer. Then press Map -> Automap, or command M to make them look nice.

For enemy ai, we need two board layers. Title the first "BoardGravityUp", and the second "BoardGravityDown". "Up" gravity refers to when the gravity is flipped, while "down" gravity is regular gravity.
In these board layers, create the enemy paths according to the corresponding gravity. Use red tiles for all paths the enemy is able to traverse, including upward jumps (we currently have it so enemies can only jump up four tiles). Use green tiles to show "falling" paths; paths the robot can fall down, but can not climb back up. Use red tiles for the traversals of both gravityUp and gravityDown.

Add all other objects in the Objects layer. Each of them should be a Tile Object - click the "orange sunset" button in the navigation bar, then just click the asset you want and click on the stage. You shouldn't need to do anything else.

## Exporting a level
To add the level to the game, click Export as JSON and put the level in the jsons folder for the game, then add a new level json value in assets.json. If you aren't replacing an existing level, increase NUM_LEVELS in GameController.

If you added a new object type, you will also have to export the tileset as a json and replace objects.json with it in the jsons folder.

Before exporting, make sure the following Map Properties are set in Custom Properties:
- gravity (standard: -15)
- timer (time to escape after getting orb in seconds) (default: 60)

## Adding new objects
To add a new type of object, just go to the objects tileset (objects.tsx) and click "add tile". Add the image, then set the "class" field to the object's name in the asset directory. 

To get the new object working in the game, you will need to add to the parsing code in LevelModel.populate() to support a new object type. You can check the object's name, and if it matches, create a new object and initialize it.

## Using camera control
Camera tiles set the camera properties after the player passes through them. The "green" values are what the camera becomes if the player ends up on the green side of the tile, and the "pink" values are what the camera becomes if the player ends up on the pink side.

Add a camera tile as an object.

For vertical camera tiles, define the camera position of the top by adding the following custom properties for the tile and setting them: "greenx1", 'greeny1", "greenx2", and "greeny2", where (x1, y1) is the top left and (x2, y2) is the bottom right of the camera view. Define the camera position of the bottom by setting "pinkx1", 'pinky1", "pinkx2", and "pinky2". Don't worry about aspect ratio - the camera will keep the same aspect ratio and zoom to fit the whole area.

For horizontal camera tiles, do the same but where green is left and pink is right. 

Not setting a value will lead to tracking the player instead of changing the camera to a fixed location.  You can also only set just one dimension (x or y) to lock scrolling on one axis.

Camera tiles don't have to be one tile big - they can be any size. 
