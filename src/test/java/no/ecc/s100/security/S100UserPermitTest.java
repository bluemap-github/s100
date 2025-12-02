package no.ecc.s100.security;

import junit.framework.TestCase;

public class S100UserPermitTest extends TestCase {

    public void testUserPermit() throws Exception {
        // example from S-100 Ed 4.0.0 15-7.3
        String mId = "859868";
        String mKey = "4D5A79677065774A7343705272664F72";
        S100Manufacturer manufacturer = new S100Manufacturer(mId, mKey);

        // HW_ID is different than the one in S-100 Ed 4.0.0 15-7.3.1.1, but HW_ID
        // encrypted and user permit are the same. The HW_ID in S-100 Ed 4.0.0
        // 15-7.3.1.1 seem to have illegal length.
        String hwId = "40384B45B54596201114FE9904220101";
        String hwIdEncrypted = manufacturer.encrypt(hwId);
        assertEquals("AD1DAD797C966EC9F6A55B66ED982815", hwIdEncrypted);
        S100UserPermit userPermit = new S100UserPermit(hwIdEncrypted, mId);
        assertEquals("AD1DAD797C966EC9F6A55B66ED98281599B3C7B1859868", userPermit.getUserPermitString());
    }

}
