#AmstelBright REST API

##App Data [ /api/appdata/:appid ]

GET returns the data object for a given app id.

Example:

`GET /api/appdata/io.ourglass.shuffleboard`

[ PASTE EXAMPLE RESPONSE ]

Errors: 406 (no such app), 500 (error converting data to JSON string)

POST sets the app data. Must be a JSON Object ("{}") not an array.

`POST /api/appdata/io.ourglass.shuffleboard { red: 23, blue: 45 }`

[ PASTE EXAMPLE RESPONSE ]

##System Calls [ /api/system/:command ]


### /api/system/apps

GET all installed apps and their status.

Example:

`GET /api/system/apps`

[ PASTE EXAMPLE RESPONSE ]

Errors: ???

POST not supported (will generate error)

### /api/system/device

Gets/Sets basic device settings.

Example:

`GET /api/system/device`

[ PASTE EXAMPLE RESPONSE ]

Errors: ???

POST to set location and name:

`POST /api/system/device { name: "Over Bar", locationWithinVenue: "First Floor" }`

[ PASTE EXAMPLE RESPONSE ]





#BACKUP

      // Real OG Routes
      addRoute("/api/appdata/:appid", JSONAppDataHandler.class);
      addRoute("/api/system/:command", JSONSystemHandler.class);
      addRoute("/api/app/:appid/:command", JSONAppCommandsHandler.class);
      // TODO: add source to route e.g."twitter"
      addRoute("/api/scrape/:appid", JSONAppScrapeHandler.class);
     
      File f = new File("/mnt/sdcard"+ OGConstants.PATH_TO_ABWL).getAbsoluteFile();
     
      // Static pages (AmstelBrightWithLime)
      addRoute("/www/(.)+", StaticPageTestHandler.class, f.getAbsoluteFile());