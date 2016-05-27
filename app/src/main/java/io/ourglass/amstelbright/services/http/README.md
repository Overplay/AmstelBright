NanoHTTP Implementation Notes
=============================

To test NanoHTTP while running in an emulator:

`adb forward tcp:9090 tcp:9090`

You can then get to NanoHTTP by GET localhost:9090. The first tcp: is
the PC port, the second is the emulator port.

You could also do

`adb forward tcp:80 tcp:9090`

but this would probably be a bad idea and make your PC webserver upset :).


Resource Loading
----------------

NanoHTTPD does a bunch of reource loading based off the classpath like so:

`SomeClass.class.getResourceAsStream("blubbie.mp3")`

This doesn't work in Android since it wants resoources in res/raw
or assets. There are hackarounds (compiling into a JAR), but I'm going
to just rewrite those when I fine them.

The first was MimeType. Nano used a translation file to go from extension
to MimeType. Android has a built in call, so I just used that.



