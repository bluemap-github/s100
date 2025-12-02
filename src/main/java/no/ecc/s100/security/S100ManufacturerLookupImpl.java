package no.ecc.s100.security;

public class S100ManufacturerLookupImpl implements S100ManufacturerLookup {

    public String m_id;
    public String m_key;

    public void Set(String id, String Key) {
        m_id = id;
        m_key = Key;
    }

    @Override
    public S100Manufacturer manufacturerForMId(String mId) {
        return new S100Manufacturer(m_id, m_key);
    }
}
