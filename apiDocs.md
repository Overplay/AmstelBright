## AmstelBright HTTP Endpoints ##
----------
#### OGNanolets.java (contains base routes) ####

```java
public class OGNanolets extends OGRouterNanoHTTPD {

    private static final int PORT = 9090;
    private  HTTPDService mHTTPServer;

    /**
     * Create the server instance
     */
    public OGNanolets(HTTPDService httpServer) throws IOException {
        super(PORT);
        if(OGConstants.USE_HTTPS) {
            File f = new File(OGConstants.SSL_KEYSTORE);

            System.setProperty("javax.net.ssl.trustStore", f.getAbsolutePath());
            System.setProperty("javax.net.ssl.trustStorePassword", OGConstants.SSL_KEY_PASSWORD);
            this.makeSecure(NanoHTTPD.makeSSLSocketFactory("/" + f.getName(), OGConstants.SSL_KEY_PASSWORD.toCharArray()), null);
            this.setServerSocketFactory(new SecureServerSocketFactory(NanoHTTPD.makeSSLSocketFactory("/" + f.getName(), OGConstants.SSL_KEY_PASSWORD.toCharArray()), null));
        }
        mHTTPServer = httpServer;
        addMappings();
        start();
    }

   ...

    /**
     * Add the routes Every route is an absolute path Parameters starts with ":"
     * Handler class should implement @UriResponder interface If the handler not
     * implement UriResponder interface - toString() is used
     */
    @Override
    public void addMappings() {
        super.addMappings();

        // Real OG Routes
        addRoute("/api/appdata/:appid", JSONAppDataHandler.class);X
        addRoute("/api/system/:command", JSONSystemHandler.class);
        addRoute("/api/channel/favorite/:channel", JSONChannelFavoriteHandler.class);
        addRoute("/api/program/:command", JSONPGSHandler.class);
        addRoute("/api/tv/change/:newchannel", JSONTVControlHandler.class);
        addRoute("/api/tv/currentgrid", JSONTVControlHandler.class);
        addRoute("/api/app/:appid/:command", JSONAppCommandsHandler.class);

        // TODO: add source to route e.g."twitter"
        addRoute("/api/scrape/:appid", JSONAppScrapeHandler.class);
        addRoute("/user/jwt", JSONJWTHandler.class);

        addRoute("/api/stb/:command", JSONSTBHandler.class);

        addRoute("/api/ad/", JSONAdHandler.class);

        // Aqui files
        File f = new File("/mnt/sdcard"+ OGConstants.PATH_TO_ABWL).getAbsoluteFile();
        addRoute("/www/(.)+", StaticPageTestHandler.class, f.getAbsoluteFile());

        // Serve an experimental branch of Aqui via wwwx/...
        File fExperimental = new File("/mnt/sdcard"+
		    OGConstants.PATH_TO_ABWL+"experimental").getAbsoluteFile();
        addRoute("/wwwx/(.)+", StaticPageTestHandler.class, 
	        fExperimental.getAbsoluteFile());

        // Ads
        File mediaFileDir = new 
	        File("/data/data/io.ourglass.amstelbright2/media").getAbsoluteFile();
        addRoute("/api/admedia/(.)+", StaticPageTestHandler.class, 
	        mediaFileDir.getAbsoluteFile());

        // Examples
	    ...
	    }
	}
```

-----

| Call | `GET /api/appdata/:appid`|
|:---:|:---:|
| Successful return JSON | unrestriced JSON Object (defined by `POST /api/appdata/:appid`) |   
| Query Params | `appid` - unique name of the installed app, defined in info.json  |
| JSON Body Params| NONE |
| Possible Errors | NONE |
| Check to make sure error paths return JSON (initials & comment) | No visible Error Paths `JEA` |
-----

| Call | `POST /api/appdata/:appid`|
|:---:|:---:|
| Successful return JSON | unrestriced JSON Object (same as the body of the POST) |   
| Query Params | `appid` - unique name of the installed app, defined in info.json  |
| JSON Body Params| JSON object representing the data that you want associated with the given app |
| Possible Errors | `500 - Internal Error` |
| Check to make sure error paths return JSON (initials & comment) | Only Exception makes call to makeErrorJSON - `JEA` |
-----

| Call | `GET /api/sytem/apps`|
|:---:|:---:|
| Successful return JSON | Compact JSON array containing JSON representation of all installed apps |   
| Query Params | NONE  |
| JSON Body Params| JSON object representing the data that you want associated with the given app |
| Possible Errors | NONE |
| Check to make sure error paths return JSON (initials & comment) | No error paths visible - `JEA` |
-----

| Call | `GET /api/sytem/device`|
|:---:|:---:|
| Successful return JSON | {`name`: String (System name), `locationWithinVenue`: String, `randomFactoid`: String (TODO probably should go?), `wifiMacAddress`: String, `isPairedToSTB`: Boolean, `pairedSTBIP`: String (IP address), `channel`(only if paired): String, `title`(only if paired): String, `outputRes`: String (in the format `<width>x<height>`, `abVersionName`: String, `abVersionCode`: Integer, `osVersion`: String, `osApiLevel`: Integer, `venue`: String, `udid`: String, `lastGuideSync`: utcISOTimeString }|   
| Query Params | NONE |
| JSON Body Params| NONE |
| Possible Errors | `500 - Internal Error` |
| Check to make sure error paths return JSON (initials & comment) | Only Exception makes call to makeErrorJSON - `JEA` |
-----

| Call | `GET /api/sytem/channel`|
|:---:|:---:|
| Successful return JSON | { `channel`: String, `programId`: String, `programTitle`: String } |   
| Query Params | NONE |
| JSON Body Params| NONE |
| Possible Errors | NONE |
| Check to make sure error paths return JSON (initials & comment) | No possible error paths, may return an incomplete or empty JSON object - `JEA`|
-----

| Call | `GET /api/sytem/logs`|
|:---:|:---:|
| Successful return JSON | [{`logType`: String, `message`: JSONObject, `loggedAt`: long (date), `uploadedAt`: long (date), `deviceUniqueId`: String (UUID), `deviceId`: String}, ...] |   
| Query Params | NONE  |
| JSON Body Params| NONE |
| Possible Errors | NONE |
| Check to make sure error paths return JSON (initials & comment) | No possible error paths, may return an incomplete or empty JSON Object - `JEA` |
-----

**The method that handles this request seems too unstable to document right now**
| Call | `POST/PUT /api/sytem/device`|
|:---:|:---:|
| Successful return JSON | |   
| Query Params | |
| JSON Body Params|  |
| Possible Errors |  |
| Check to make sure error paths return JSON (initials & comment) | |
-----

**The method that handles this request seems too unstable to document right now**
| Call | `POST/PUT /api/sytem/regcode`|
|:---:|:---:|
| Successful return JSON | |   
| Query Params | |
| JSON Body Params|  |
| Possible Errors |  |
| Check to make sure error paths return JSON (initials & comment) | |
-----

| Call | `POST/PUT /api/channel/favorite/:channel`|
|:---:|:---:|
| Successful return JSON | Channel representing the newly bookmarked station: {<br> `channelNumber`: Integer, <br>`stationId`: Integer,<br>`name`: String,<br>`callsign`: String,<br>`network`: String,<br>`Twitter`: String,<br>`stationHd`: Boolean,<br>`defaultBestPositionCrawler`: Integer,<br>`defaultBestPositionWidget`: Integer,<br>`favorite`: Boolean,<br>`displayWeight`: Integer<br>} |   
| Query Params | `channel`: int representing the channe|
| JSON Body Params| `clear`(optional): presence will clear bookmark |
| Possible Errors | Missing JWT returns `401 UNAUTHORIZED`.<br> Not high enough authority level returns `401 UNAUTHORIZED`.<br> Station is invalid returns `406 NOT_ACCEPTABLE`  |
| Check to make sure error paths return JSON (initials & comment) | Was returning an empty string for Unauthorized added JSON. JEA|
-----

| Call | `GET /api/program/grid4channel`|
|:---:|:---:|
| Successful return JSON | `channel`: JSONObject representing channel, <br> `listings`: JSONArray with all upcoming listings|   
| Query Params | NONE|
| JSON Body Params| `channel`: Integer |
| Possible Errors | Missing channel parameter - returns `406 NOT_ACCEPTABLE`|
| Check to make sure error paths return JSON (initials & comment) | Error paths seem in order. `JEA` |
-----

| Call | `GET /api/program/grid`|
|:---:|:---:|
| Successful return JSON |  |   
| Query Params | |
| JSON Body Params| |
| Possible Errors | |
| Check to make sure error paths return JSON (initials & comment) | |
-----

| Call | `GET /api/program/channels`|
|:---:|:---:|
| Successful return JSON |  |   
| Query Params | |
| JSON Body Params| |
| Possible Errors | |
| Check to make sure error paths return JSON (initials & comment) | |
-----

























