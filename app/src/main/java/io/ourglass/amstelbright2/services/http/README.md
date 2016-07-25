NanoHTTP Implementation Notes
=============================

To test NanoHTTP while running in an emulator:

`adb forward tcp:9090 tcp:9090`

You can then get to NanoHTTP by GET localhost:9090. The first tcp: is
the PC port, the second is the emulator port.

You could also do

`adb forward tcp:80 tcp:9090`

but this would probably be a bad idea and make your PC webserver upset :).


Adding Routes
-------------

Routes are added near the bottom of OGNanolets:

        /**
         * Add the routes Every route is an absolute path Parameters starts with ":"
         * Handler class should implement @UriResponder interface If the handler not
         * implement UriResponder interface - toString() is used
         */
        @Override
        public void addMappings() {
            super.addMappings();
    
            // Real OG Routes
            addRoute("/api/appdata/:appid", JSONAppDataHandler.class);
            addRoute("/api/system/:command", JSONSystemHandler.class);
            addRoute("/api/app/:appid/:command", JSONAppCommandsHandler.class);
    
            // Static pages (AmstelBrightWithLime)
            addRoute("/www/(.)+", StaticPageTestHandler.class, new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/www").getAbsoluteFile());
            
            }

The syntax is pretty obvious since it uses slugs (":appid") like most route libraries. You pass the route,
then the handler. All of our JSON handlers inherit from the abstract class `JSONHandler`. `JSONHandler` does
 al the common work needed for any JSON response so only the unique work needs to be implemented.

Resource Loading Changes from Stock NanoHTTP
--------------------------------------------

NanoHTTPD does a bunch of reource loading based off the classpath like so:

`SomeClass.class.getResourceAsStream("blubbie.mp3")`

This doesn't work in Android since it wants resoources in res/raw
or assets. There are hackarounds (compiling into a JAR), but I'm going
to just rewrite those when I fine them.

The first was MimeType. Nano used a translation file to go from extension
to MimeType. Android has a built in call, so I just used that.



