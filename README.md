bitbreeds-webrtc
----------------

### Goal
The goal for bitbreeds-webrtc is to eventually make a simple Java peer for
talking directly to one or several browsers or
other WebRTC peers through an unordered/unreliable or 
unordered/reliable DataChannel.

At the moment the goal is to only allow it to be used as a server
(the browser must initiate the WebRTC connection), though extending
it to be able to act as a client should not be that hard, but 
it is not something I am interested in writing, since I do not need it at this moment.

### Maturity
bitbreeds webrtc is __experimental__ and not even close to complete and __not__ ready for
any kind serious use.

### How to run
#### Run locally.

Main class `SimpleSignalingExample` will start a websocket server on port 8443.

Then run `./web/index.html` in firefox to connect to the server, share candicates and 
using WebRTC. If it works it should say _ONMESSAGE_

#### Run on a server
If you build webrtc-example, you can start the _webrtc-example-1.0-SNAPSHOT-capsule.jar_ like this (make sure you point _-Dcom.bitbreeds.keystore_ to a keystore that exists):

```
java -Dcom.bitbreeds.keystore=./ws2.jks -Dcom.bitbreeds.keystore.alias=websocket -Dcom.bitbreeds.keystore.pass=websocket -Dcom.bitbreeds.ip="192.168.1.5" -jar webrtc-example-1.0-SNAPSHOT-capsule.jar
```

If the server has problems finding its own public ip, you can supply the
ip to send as a candidate manually like this.

```
-Dcom.bitbreeds.ip=192.168.1.5
```

The keystore parameters are pretty self explanatory (you need to make an RSA cert though):
```
-Dcom.bitbreeds.keystore=./ws2.jks
-Dcom.bitbreeds.keystore.alias=websocket
-Dcom.bitbreeds.keystore.pass=websocket
```

#### Run a complete selenium test
class `BrowserTestChrome/Firefox` runs a full test against a browser.
It will start the server, open the browser and connect. Then end
once it has opened the WebRTC connection, or sent a bunch of messages over the 
peerconnection.


### Debug
To start firefox with logging, take a look in ./firefox_osx_webrtc_logging.sh: 
```
#!/bin/bash
dat=`echo ~`
path="$dat/webrtc_firefox.log"
trace="$dat/webrtc_trace.log"
echo $path
export WEBRTC_TRACE_FILE=$trace
export MOZ_LOG_FILE=$path
export MOZ_LOG='timestamp,sync,jsep:5,rtplogger:5,SCTP:5,signaling:5,mtransport:5,MediaManager:5,webrtc_trace:5'
export R_LOG_LEVEL=9
export R_LOG_VERBOSE=1
open /Applications/Firefox.app/
```
That log will contain a lot of information needed to debug eventual issues.
On the server side setting levels in logback-test.xml control logging.

Chrome also has chrome://webrtc-internals, which is great for debugging.


