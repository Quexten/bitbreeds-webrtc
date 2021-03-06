package com.bitbreeds.webrtc.sctp.impl.buffer;

import com.bitbreeds.webrtc.model.sctp.SCTPPayloadProtocolId;
import com.bitbreeds.webrtc.model.sctp.SackUtil;
import com.bitbreeds.webrtc.common.SetUtil;
import com.bitbreeds.webrtc.model.webrtc.Deliverable;
import com.bitbreeds.webrtc.sctp.impl.model.ReceivedData;
import com.bitbreeds.webrtc.sctp.model.SCTPOrderFlag;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Copyright (c) 20/02/2018, Jonas Waage
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
public class ReceiveBufferFragmentationTest {

    private ReceivedData makeFrag(long tsn, byte[] data, SCTPOrderFlag flag) {
        return new ReceivedData(tsn,
                0,
                0,
                flag,
                SCTPPayloadProtocolId.WEBRTC_BINARY,data);
    }

    @Test
    public void testReceiveFragmented() {
        ReceiveBuffer buffer = new ReceiveBuffer(6,100);

        buffer.setInitialTSN(1);

        ReceivedData start = makeFrag(1,new byte[]{0,1,2},SCTPOrderFlag.UNORDERED_START_FRAGMENT);
        ReceivedData mid = makeFrag(2,new byte[]{0,1,2},SCTPOrderFlag.UNORDERED_MIDDLE_FRAGMENT);
        ReceivedData mid2 = makeFrag(3,new byte[]{0,1,2},SCTPOrderFlag.UNORDERED_MIDDLE_FRAGMENT);
        ReceivedData end = makeFrag(4,new byte[]{0,1,2},SCTPOrderFlag.UNORDERED_END_FRAGMENT);

        buffer.store(start);
        buffer.store(mid2);

        List<Deliverable> del = buffer.getMessagesForDelivery();
        assertEquals(Collections.emptyList(),del);

        SackData sack = buffer.getSackDataToSend();
        assertEquals(1,sack.getCumulativeTSN());
        assertEquals(sack.getTsns(), SackUtil.getGapAckList(SetUtil.newHashSet(3L)));
        assertEquals(sack.getDuplicates(),Collections.emptyList());

        buffer.store(mid);

        List<Deliverable> del2 = buffer.getMessagesForDelivery();
        assertEquals(Collections.emptyList(),del2);

        SackData sack2 = buffer.getSackDataToSend();
        assertEquals(3,sack2.getCumulativeTSN());
        assertEquals(sack2.getTsns(), Collections.emptyList());
        assertEquals(sack2.getDuplicates(),Collections.emptyList());

        buffer.store(end);

        SackData sack3 = buffer.getSackDataToSend();
        List<Deliverable> del3 = buffer.getMessagesForDelivery();
        assertEquals(4,sack3.getCumulativeTSN());
        assertEquals(1,del3.size());
    }


    @Test
    public void wrappedFragmedted() {
        ReceiveBuffer buffer = new ReceiveBuffer(6,100);

        buffer.setInitialTSN(1);

        ReceivedData uf = makeFrag(1,new byte[]{0,1,2},SCTPOrderFlag.UNORDERED_UNFRAGMENTED);
        ReceivedData uf1 = makeFrag(2,new byte[]{0,1,2},SCTPOrderFlag.UNORDERED_UNFRAGMENTED);
        ReceivedData uf2 = makeFrag(3,new byte[]{0,1,2},SCTPOrderFlag.UNORDERED_UNFRAGMENTED);

        ReceivedData start = makeFrag(4,new byte[]{0},SCTPOrderFlag.UNORDERED_START_FRAGMENT);
        ReceivedData mid = makeFrag(5,new byte[]{1},SCTPOrderFlag.UNORDERED_MIDDLE_FRAGMENT);
        ReceivedData mid2 = makeFrag(6,new byte[]{2},SCTPOrderFlag.UNORDERED_MIDDLE_FRAGMENT);
        ReceivedData end = makeFrag(7,new byte[]{3},SCTPOrderFlag.UNORDERED_END_FRAGMENT);

        buffer.store(uf);
        buffer.store(uf1);
        buffer.store(uf2);

        buffer.getMessagesForDelivery();
        buffer.getSackDataToSend();

        buffer.store(start);
        buffer.store(mid);
        buffer.store(mid2);
        buffer.store(end);

        List<Deliverable> del = buffer.getMessagesForDelivery();
        assertEquals(1,del.size());
        assertArrayEquals(new byte[]{0,1,2,3},del.get(0).getData());
    }




}
