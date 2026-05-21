# modbone

This is a simple utility to keep very detailed daily download numbers for your Minecraft mods on Modrinth and CurseForge. This comes from a frustration that i've had where you cannot see which versions of your mod get how many downloads as instead they aggregate download numbers, so i wasn't able to see which versions remained popular after their release or a later update to Minecraft.

modbone is currently in alpha and is missing export functionality.

# Using

modbone requires manual setup, because it is currently under heavy development. A pre-made SQLite3 database is required, and the following SQLite DDL is used to model such:

```sql
CREATE TABLE TABLE IF NOT EXISTS mod_id (
    "id" INTEGER NOT NULL PRIMARY KEY autoincrement,
    "name" TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS records (
    "date" TEXT(10) NOT NULL,
    "mod_id" INTEGER NOT NULL,
    "mod_version" TEXT NOT NULL,
    "modrinth_downloads" INTEGER,
    "curseforge_downloads" INTEGER,
    PRIMARY KEY ("date", "mod_id", "mod_version"),
    CONSTRAINT "record_mod_id" FOREIGN KEY ("mod_id") REFERENCES "mods" ("id")
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);
```

modbone also requires the mods to be configured in a JSON file with the following format:

```JSON
[
  {
    "name": "<your project's name>",
    "modrinth": {
        "projectId": "<the project's id or slug on Modrinth>",
        "versionIds": {
            "<human readable version number, same as CF's equivalent>": "<this verion's id on Modrinth>",
            "1.1.0": "A1b2C3d4",
            "1.0.0": "5E6f7G8h"
        }
    },
    "curseforge": {
        "modId": "<the mod's numerical id on CurseForge>",
        "fileIds": {
            "<human readable version number, same as MR's equivalent>": "<this file's id on CurseForge>",
            "1.1.0": "1235670",
            "1.0.0": "1234560"
        }
    }
  },
  { ... },
  ...
]
```

Additionally, in order to properly monitor download metrics from CurseForge, you will need a valid CurseForge API key. To use it, you must pass it as the `CURSEFORGE_API_KEY` environment variable.

modbone does not stay resident and terminates upon finishing its counting routine. To get updated numbers daily, please attach modbone to a hook that fires everyday, such as a cronjob.

For more information, please run `java -jar modbone.jar --help`.

## Roadmap

modbone is under development, and the following features are planned:

- CSV export
- configuration validation and example
- more flexible configuration

## Miscellaneous information

modbone is licensed under MIT.
