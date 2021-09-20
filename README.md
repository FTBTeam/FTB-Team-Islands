# FTB Team Islands

## FTB Teams

This mod relies heavily on FTB Teams(teams), you must have teams installed. Teams is used as the core of how the Islands
-> Team system works. The Islands map works by using the teams UUID as it's key and having an Island Object as it's
value. This allows us to have quick and easy access to islands at the same time as providing a very simple, and low
effort, way of moving players from one island to another by allowing teams to change their teams UUID.

## Commands

### `/ftbteamislands create`

Creates an Island for your part, this requires you to be part of a FTB Team Party. You can also not use this command if
you're already part of an island. You will need to leave your team before you can create a new island

### `/ftbteamislands list`

Lists all the islands (including inactive islands) with a clickable (in purple) chat item that'll autofill
the `/ftbteamislands islands <team>` command. You can also see the `Spawn Pos` of the island from this command.

You can only use this command if you have a permission level of 2 or more.

### `/ftbteamislands lobby`

Will take you back to the worlds lobby if one exists.

### `/myisland`

Will take you back to your islands spawn point. Don't delete the block under it or you're in for a long fall! This
command can be disabled from the config.

### `/ftbteamislands islands <team>`

Will take an admin, or a user with a permissions level of 2 or more, to a teams island. The teams id can be quickly
fetched from the `list` command or using the commands auto complete.

### `/ftbteamislands delete`

**WARNING!** DELETES REGION FILES!

This command will remove and unclaimed / in-active islands. An island is marked as unused / unclaimed when the last user
leaves a team, and the team is deleted. The config contains a value for how far apart islands will spawn from each
other. This command will use that radius (default as 3) to delete each region file within the islands max distance from
another island. For example: if the radius is set to 3 regions, it will delete a 3x3 region sized area (512 * 9) around
the teams island. Hopefully removing all trace of that island.

The region files are deleted upon a game restart (clients) or on a server restart. If you use this command accidentally,
go to your worlds `region` files and back them all up, restart the server, stop the server and put the backed up region
files back in their original place.

### `/ftbteamislands reload-islands-json`

This command will reload the `config/ftbteamislands/islands.json` file in real time allowing you to make changes without
having to restart / reload the game / map.

## FTB Chunks

We also support chunks, you can enable the radius of how many chunks a player can auto-claim upon island creation
through the `common config`. Simply use `-1` to disable FTB Chunks integration.

## Prebuilt Islands

"Prebuilts" are a simple concept of being able to use prebuilt islands that can be added to the mod through `data packs`
, `kubejs` (like mods), and raw files.

Upon first start up using the mod, a `ftbteamislands` folder will be created in the `config`directory of your game
instance or server instance. In there you will find a `structures` directory, and an empty `islands.json` file. You can
use the `islands.json` (show in the below example) to create a list of prebuilt islands that a user can pick from using
an in-game gui. You can provide an image, description, author, and a structure file for our mod to load automatically.
We typically fail softly so be sure to keep an eye on the logs during world load.

```json
[
  {
    "name": "Main Islands",
    "author": "FTB Team",
    "desc": "The team behind all the packs?",
    "islands": [
      {
        "name": "Hay look, an Island",
        "desc": "This is an island",
        "structure": "island-one.nbt",
        "image": "ftbteamislands:textures/screens/foldericon.png"
      },
      {
        "name": "And another!",
        "desc": "Another island. Woooooooow",
        "structure": "island-two.nbt",
        "image": "ftbteamislands:textures/screens/default-island.png",
        "yOffset": -2
      }
    ]
  }
]
```

As you can see from the `json` above, we support multiple groups of "prebuilts" and you can set custom names,
descriptions, and images for all of them. The structure file must be in the `structures` folder. The image is a fully
quantified `resource location` and thus you can use any common method to add an image for our mod to use.

You can use the `yOffset` to control the islands spawn location on the `Y` axis.

## Using without "prebuilts"

So you want to use the mod without "prebuilts"? Well... I'm sad but sure, I guess you can if you really want to.

We currently support two types of `default islands`, you can, through the config, set a default `lobby` and a
default `island`. The lobby is used upon the first team creation and is used as the global spawn point of the world.
This island is set via the config option `defaultIslands`. This is also a fully quantified `resource location` and thus
can be added from any common methods. The lobby is done very similarly via the `lobbyStructureFile` config option.

You can also provide a `Y offset` for the default island to spawn at if our generated Y level does not work for you. The
lobby can be controlled via the `height` config option used for all islands upon island creation.

No images are supported for this method of using the mod.

As well as changing those config options, you can also override the data files in our mod using a data pack. Our files
are called
`default_lobby.nbt` and `teamislands_island.nbt`. They are held in the `resources.data.ftbteamislands.structures`.

### Spawn point

Each island either prebuilts or set via the config will require a single structure block where you wish the player to
spawn. This spawn point is set via the Metadata of the structure block by putting `SPAWN_POINT` on the structure block.
