package no.ecc.s100.security;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import no.ecc.s100.S100ProductSpecification;

/**
 * S100PermitFile을 사용하여 PRIMAR_PERMIT.XML (S100 네임스페이스 형식) 파일을 생성하는 클래스
 */
public class S100PermitFileWriter {

    private static final String S100_NAMESPACE = "http://www.iho.int/s100/se/5.2";
    private static final String S100_PREFIX = "s100";
    
    private static final String PERMIT_ELEMENT = "Permit";
    private static final String HEADER_ELEMENT = "header";
    private static final String ISSUE_DATE_ELEMENT = "issueDate";
    private static final String DATA_SERVER_NAME_ELEMENT = "dataServerName";
    private static final String DATA_SERVER_IDENTIFIER_ELEMENT = "dataServerIdentifier";
    private static final String VERSION_ELEMENT = "version";
    private static final String USERPERMIT_ELEMENT = "userpermit";
    private static final String PRODUCTS_ELEMENT = "products";
    private static final String PRODUCT_ELEMENT = "product";
    private static final String ID_ATTRIBUTE = "id";
    private static final String DATASET_PERMIT_ELEMENT = "datasetPermit";
    private static final String FILENAME_ELEMENT = "filename";
    private static final String EDITION_NUMBER_ELEMENT = "editionNumber";
    private static final String EXPIRY_ELEMENT = "expiry";
    private static final String ENCRYPTED_KEY_ELEMENT = "encryptedKey";

    private static final String ISSUE_DATE_FORMAT = "yyyy-MM-dd";
    private static final String EXPIRY_DATE_FORMAT = "yyyy-MM-dd";
    private static final String DEFAULT_VERSION = "5.2.0";

    /**
     * S100PermitFile을 PRIMAR_PERMIT.XML 파일로 저장합니다.
     * 
     * @param permitFile 저장할 S100PermitFile 인스턴스
     * @param outputFile 출력 파일
     * @throws IOException 파일 쓰기 오류 시
     * @throws XMLStreamException XML 생성 오류 시
     */
    public static void write(S100PermitFile permitFile, File outputFile) throws IOException, XMLStreamException {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            write(permitFile, fos);
        }
    }

    /**
     * S100PermitFile을 PRIMAR_PERMIT.XML 파일로 저장합니다.
     * 
     * @param permitFile 저장할 S100PermitFile 인스턴스
     * @param outputPath 출력 파일 경로
     * @throws IOException 파일 쓰기 오류 시
     * @throws XMLStreamException XML 생성 오류 시
     */
    public static void write(S100PermitFile permitFile, String outputPath) throws IOException, XMLStreamException {
        write(permitFile, new File(outputPath));
    }

    /**
     * S100PermitFile을 OutputStream에 PRIMAR_PERMIT.XML (S100 네임스페이스) 형식으로 씁니다.
     * 
     * @param permitFile 저장할 S100PermitFile 인스턴스
     * @param outputStream 출력 스트림
     * @throws IOException 파일 쓰기 오류 시
     * @throws XMLStreamException XML 생성 오류 시
     */
    public static void write(S100PermitFile permitFile, OutputStream outputStream)
            throws IOException, XMLStreamException {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = null;
        try {
            writer = outputFactory.createXMLStreamWriter(outputStream, "UTF-8");
            writer.writeStartDocument("UTF-8", "1.0");
            
            // s100:Permit 루트 요소 (네임스페이스 선언)
            writer.setPrefix(S100_PREFIX, S100_NAMESPACE);
            writer.writeStartElement(S100_PREFIX, PERMIT_ELEMENT, S100_NAMESPACE);
            writer.writeNamespace(S100_PREFIX, S100_NAMESPACE);
            writer.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            writer.writeAttribute("http://www.w3.org/2001/XMLSchema-instance", "standalone", "yes");

            // s100:header 섹션
            writer.writeStartElement(S100_PREFIX, HEADER_ELEMENT, S100_NAMESPACE);
            
            // s100:issueDate
            writer.writeStartElement(S100_PREFIX, ISSUE_DATE_ELEMENT, S100_NAMESPACE);
            SimpleDateFormat issueDateFormat = new SimpleDateFormat(ISSUE_DATE_FORMAT);
            writer.writeCharacters(issueDateFormat.format(permitFile.getDate()));
            writer.writeEndElement();
            
            // s100:dataServerName
            writer.writeStartElement(S100_PREFIX, DATA_SERVER_NAME_ELEMENT, S100_NAMESPACE);
            writer.writeCharacters(permitFile.getDataserver());
            writer.writeEndElement();
            
            // s100:dataServerIdentifier (기본값 사용)
            writer.writeStartElement(S100_PREFIX, DATA_SERVER_IDENTIFIER_ELEMENT, S100_NAMESPACE);
            writer.writeCharacters("urn:mrn:iho:KR00:00280"); // 기본값
            writer.writeEndElement();
            
            // s100:version
            writer.writeStartElement(S100_PREFIX, VERSION_ELEMENT, S100_NAMESPACE);
            writer.writeCharacters(DEFAULT_VERSION);
            writer.writeEndElement();
            
            // s100:userpermit
            writer.writeStartElement(S100_PREFIX, USERPERMIT_ELEMENT, S100_NAMESPACE);
            writer.writeCharacters(permitFile.getUserPermitString());
            writer.writeEndElement();
            
            writer.writeEndElement(); // s100:header

            // s100:products 섹션
            writer.writeStartElement(S100_PREFIX, PRODUCTS_ELEMENT, S100_NAMESPACE);
            
            // product 요소들 (제품 사양별로 그룹화)
            Set<S100ProductSpecification> productSpecs = new TreeSet<>(
                    permitFile.getDataPermits().stream()
                            .map(S100DataPermit::getProductSpecification)
                            .collect(java.util.stream.Collectors.toSet()));

            for (S100ProductSpecification productSpec : productSpecs) {
                writer.writeStartElement(S100_PREFIX, PRODUCT_ELEMENT, S100_NAMESPACE);
                writer.writeAttribute(ID_ATTRIBUTE, productSpec.toString());

                Collection<S100DataPermit> permits = permitFile.get(productSpec);
                for (S100DataPermit permit : permits) {
                    // s100:datasetPermit 요소
                    writer.writeStartElement(S100_PREFIX, DATASET_PERMIT_ELEMENT, S100_NAMESPACE);

                    // s100:filename
                    writer.writeStartElement(S100_PREFIX, FILENAME_ELEMENT, S100_NAMESPACE);
                    writer.writeCharacters(permit.getFileName());
                    writer.writeEndElement();

                    // s100:editionNumber
                    writer.writeStartElement(S100_PREFIX, EDITION_NUMBER_ELEMENT, S100_NAMESPACE);
                    writer.writeCharacters(String.valueOf(permit.getEdtn()));
                    writer.writeEndElement();

                    // s100:expiry
                    writer.writeStartElement(S100_PREFIX, EXPIRY_ELEMENT, S100_NAMESPACE);
                    SimpleDateFormat expiryDateFormat = new SimpleDateFormat(EXPIRY_DATE_FORMAT);
                    writer.writeCharacters(expiryDateFormat.format(permit.getPermitEndDate()));
                    writer.writeEndElement();

                    // s100:encryptedKey
                    writer.writeStartElement(S100_PREFIX, ENCRYPTED_KEY_ELEMENT, S100_NAMESPACE);
                    writer.writeCharacters(permit.getEncryptedDataKey());
                    writer.writeEndElement();

                    writer.writeEndElement(); // s100:datasetPermit
                }

                writer.writeEndElement(); // s100:product
            }
            
            writer.writeEndElement(); // s100:products

            writer.writeEndElement(); // s100:Permit
            writer.writeEndDocument();
            writer.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

}

