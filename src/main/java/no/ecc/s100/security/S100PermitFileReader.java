package no.ecc.s100.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import no.ecc.s100.S100ProductSpecification;

/**
 * PERMIT.XML 파일을 읽어서 S100PermitFile을 사용하는 유틸리티 클래스
 */
public class S100PermitFileReader {

    private final S100PermitFile permitFile;
    private final Map<String, S100Manufacturer> manufacturerMap;

    /**
     * PERMIT.XML 파일을 읽어서 S100PermitFile을 생성합니다.
     * 
     * @param permitXmlFile PERMIT.XML 파일 경로
     * @param manufacturerMap Manufacturer ID를 키로 하는 Manufacturer 맵
     * @throws IOException 파일 읽기 오류 시
     * @throws XMLStreamException XML 파싱 오류 시
     */
    public S100PermitFileReader(File permitXmlFile, Map<String, S100Manufacturer> manufacturerMap)
            throws IOException, XMLStreamException {
        this.manufacturerMap = new HashMap<>(manufacturerMap);
        S100ManufacturerLookup lookup = new MapBasedManufacturerLookup(this.manufacturerMap);
        
        try (FileInputStream fis = new FileInputStream(permitXmlFile)) {
            this.permitFile = new S100PermitFile(lookup, fis);
        }
    }

    /**
     * PERMIT.XML 파일을 읽어서 S100PermitFile을 생성합니다.
     * 
     * @param permitXmlPath PERMIT.XML 파일 경로 (문자열)
     * @param manufacturerMap Manufacturer ID를 키로 하는 Manufacturer 맵
     * @throws IOException 파일 읽기 오류 시
     * @throws XMLStreamException XML 파싱 오류 시
     */
    public S100PermitFileReader(String permitXmlPath, Map<String, S100Manufacturer> manufacturerMap)
            throws IOException, XMLStreamException {
        this(new File(permitXmlPath), manufacturerMap);
    }

    /**
     * InputStream에서 PERMIT.XML을 읽어서 S100PermitFile을 생성합니다.
     * 
     * @param inputStream PERMIT.XML 입력 스트림
     * @param manufacturerMap Manufacturer ID를 키로 하는 Manufacturer 맵
     * @throws IOException 파일 읽기 오류 시
     * @throws XMLStreamException XML 파싱 오류 시
     */
    public S100PermitFileReader(InputStream inputStream, Map<String, S100Manufacturer> manufacturerMap)
            throws IOException, XMLStreamException {
        this.manufacturerMap = new HashMap<>(manufacturerMap);
        S100ManufacturerLookup lookup = new MapBasedManufacturerLookup(this.manufacturerMap);
        this.permitFile = new S100PermitFile(lookup, inputStream);
    }

    /**
     * 읽어온 S100PermitFile 인스턴스를 반환합니다.
     * 
     * @return S100PermitFile 인스턴스
     */
    public S100PermitFile getPermitFile() {
        return permitFile;
    }

    /**
     * 모든 데이터 permit을 반환합니다.
     * 
     * @return 데이터 permit 컬렉션
     */
    public Collection<S100DataPermit> getAllDataPermits() {
        return permitFile.getDataPermits();
    }

    /**
     * 특정 제품 사양에 대한 데이터 permit을 반환합니다.
     * 
     * @param productSpecification 제품 사양
     * @return 데이터 permit 컬렉션
     */
    public Collection<S100DataPermit> getDataPermits(S100ProductSpecification productSpecification) {
        return permitFile.get(productSpecification);
    }

    /**
     * User Permit String을 반환합니다.
     * 
     * @return User Permit String
     */
    public String getUserPermitString() {
        return permitFile.getUserPermitString();
    }

    /**
     * Map 기반의 S100ManufacturerLookup 구현
     */
    private static class MapBasedManufacturerLookup implements S100ManufacturerLookup {
        private final Map<String, S100Manufacturer> manufacturerMap;

        public MapBasedManufacturerLookup(Map<String, S100Manufacturer> manufacturerMap) {
            this.manufacturerMap = manufacturerMap;
        }

        @Override
        public S100Manufacturer manufacturerForMId(String mId) {
            return manufacturerMap.get(mId);
        }
    }

}

