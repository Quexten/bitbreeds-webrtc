package com.bitbreeds.webrtc.signaling;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.sdp.SdpFactory;
import javax.sdp.SessionDescription;
import java.net.InetAddress;

/**
 * Copyright (c) 26/04/16, Jonas Waage
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/**
 * Process browser JSON contains SDP or candidates
 */
public class ProcessSignals implements Processor {

    private Gson gson = new Gson();
    private final static Logger logger = LoggerFactory.getLogger(ProcessSignals.class);

    public void process(Exchange exchange) throws Exception {
        String ex = (String)exchange.getIn().getBody();
        JsonObject el = gson.fromJson(ex,JsonObject.class);

        SdpFactory factory = SdpFactory.getInstance();

        if(el.get("type") != null) {
            if("offer".equalsIgnoreCase(el.get("type").getAsString())) {
                SessionDescription sdp = factory.createSessionDescription(el.get("sdp").getAsString());
                logger.info("SDP" + sdp);
                exchange.getIn().setBody(new Offer(sdp));
                return;
            }
            else if("answer".equalsIgnoreCase(el.get("type").getAsString())) {
                SessionDescription sdp = factory.createSessionDescription(el.get("sdp").getAsString());
                logger.info("SDP" + sdp);
                exchange.getIn().setBody(new Answer(sdp));
                return;
            }
        }
        else if(el.get("candidate") != null) {

            String iceCandidate = el.get("candidate").getAsString();
            String[] ice = iceCandidate.split(" ");

            IceCandidate can = new IceCandidate(
                    Integer.valueOf(ice[5]),
                    InetAddress.getByName(ice[4]),
                    Long.valueOf(ice[3]));
            exchange.getIn().setBody(can);
            return;

        }
        else {
            throw new UnsupportedOperationException("unknown type");
        }

        factory.createSessionDescription(ex);
    }


}
