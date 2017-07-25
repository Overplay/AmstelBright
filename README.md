AMSTEL BRIGHT: OBSOLETE
=======================

Amstel Bright is the Ourglass Android servers and TV UI. It consists of the following components:

- UDP Beaconing Server
- HTTP Web Server
- Mutlicast UDP Audio Server (not currently in this version)
- OG Core central business logic
- Upgrade package de-archiver
- App installation logic (not currently implemented)
- The "Mainframe" UI designed for 720p TVs (due to limitations of the 
  Tronsmart box)
  
Organization and Startup Flow
-----------------------------

The main UI is located in the `io.ourglass.amstelbright.tvui` package, specifically
`MainframeActivity`. This activity is the first thing kicked off in this version
of AB. 

AmstelBrightService (ABS)
-------------------------

`MainframeActivity` starts the parent service, `AmstelBrightService` (ABS). ABS does
the following (in order):
1. Checks to see if there is a new `www.zip` package in the `assets` for the app,
and if so, extracts it to `/mnt/sdcard/www`. We will be adding a check of a central
repository in future versions.
2. Starts the UDP Beacon service
3. Starts the HTTP Service

As ABS starts up, it fires off Broadcast Intents that the UI can catch to update
the boot screen.

ABS also includes some crufties/test code that was used to determine which service
communication strategy should be used (binding, broadcat intents, etc.). Leave this
code in for now incase we change gears and want some example code.

ABS is in `io.ourglass.amstelbright.services.amstelbright`.

UDPBeaconService (UBS)
----------------------

The `UDPBeaconService` is used to advertise the Ourglass Player (OGP) on the local
network. It periodically fires off a UDP packet with information about the OGP. These
packets are received by the mobile app to discover OGPs.

UBS is in `io.ourglass.amstelbright.services.udp` .

HTTPDService (HTTPD)
--------------------

The `HTTPDService` is a thin wrapper around a modified version of `NanoHTTPD`. NanoHTTPD is 
an open source web server that is fairly popular for Android. The main modification made
are:
1. Stock NanoHTTPD uses resources in the Classpath to determin mimetype. This is not allowed
in Android, so I modified it to use native Android mimetype lookups (near top of `OGRouterNanoHTTPD`).
2. Modified the `Nanolets` example to create `OGNanolets` main server.

The majority of the code we will write will be in the `OGNanolets` file (where routings are defined) and
in the handlers themselves. If you find yourself modifying soemthing deeper in the inheritance than
that, you are probably doing it wrong!

There is a separate README in the `io.ourglass.amstelbright.services.http` package with details on 
the implementation.

AudioStreamingService (ASS...we need a better name!)
----------------------------------------------------
TBD, Alyssa needs to move this over and document here.


User Interface
--------------

The UI is written in native Android and currently provides the following:

- A crawler webview that is 80px high and the full width of the screen. The crawlwer can be
positioned at the top or bottom of the screen.
- A widget WebView that is 200px x 400px (actually dip) that can be in any of the 4 corners
  of the screen.
- A popup menu to start/stop apps.
- A listener for broadcast commands from OGCore (launch, move, kill, etc.)
- Animations for operations

The goal is to keep the UI as dumb as possible and do as much work as possible on OGCore.


IMPLEMENTATION NOTES
====================

Realm
-----

The Realm ORM is used for persistent storage (Google it). Realm models are in `io.ourglass.amstelbright.realm`.
