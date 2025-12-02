package no.ecc.s100.security;
//import org.springframework.core.io.ClassPathResource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collection;

import org.junit.Test;

import no.ecc.s100.utility.FileUtils;
import no.ecc.s100.utility.Hex;
import no.ecc.s100.utility.Zip;
import javax.xml.stream.XMLStreamException;

public class LocalTest {
    // 복호화 테스트
    @Test
    public void testS100DataPermit() throws Exception {
        String mId = "859868";
        String mKey = "4D5A79677065774A7343705272664F72";
        String hwId = "40384B45B54596201114FE9904220101";
        //String encryptedHwId = "EC715CF845C2921583B7C1A18133ED9C";
        //String encryptedKey = "EE25C38189E9F6AD04DCF10BF102DC71";
        String permitPath = "..\\data\\PERMIT.XML";
        //String permitPath = "..\\data\\PERMIT.XML";
        // 미리 준비된 ManufacturerLookup 인스턴스
        S100ManufacturerLookupImpl manufacturerLookup = new S100ManufacturerLookupImpl();
        manufacturerLookup.Set(mId, mKey);
        try (InputStream in = new FileInputStream(permitPath)) {
            // PERMIT.XML 파일을 읽어서 객체 생성
            S100PermitFile permitFile = new S100PermitFile(manufacturerLookup, in);
            // UserPermit 출력
            System.out.println("UserPermit: " + permitFile.getUserPermitString());

            Collection<S100DataPermit> dataPermits = permitFile.getDataPermits();
            
            // 디버깅: dataPermits 상태 확인
            System.out.println("dataPermits가 null인가? " + (dataPermits == null));
            if (dataPermits != null) {
                System.out.println("dataPermits 크기: " + dataPermits.size());
            }

            // 데이터 퍼밋들 출력
            if (dataPermits != null && !dataPermits.isEmpty()) {
                dataPermits.forEach(dp -> {
                    System.out.println("forEach 내부 진입 - 파일명: " + dp.getFileName());
                    String filePath = FileUtils.getFullPath("..\\data\\S100_ROOT", dp.getFileName());
                    if (filePath != null) {
                        try (InputStream fileIn = Files.newInputStream(Paths.get(filePath))) {
                            
                            // 암호화된 파일
                            byte[] inputFileByte = fileIn.readAllBytes();

                            // hwId가 키인 암호화 객체 생성
                            S100Crypt cryptHWID = new S100Crypt.EmptyIVNoPadding(hwId);

                            // 복호화된 키가 키인 암호화 객체 생성
                            String encryptedDataKey = dp.getEncryptedDataKey();
                            String decryptedDataKey = Hex.toString(cryptHWID.decrypt(Hex.fromString(encryptedDataKey)));
                            S100Crypt cryptEK =  new S100Crypt.RandomIV(decryptedDataKey);
                            byte[] decrypteFileByte = cryptEK.decrypt(inputFileByte);
                            Path path = Paths.get(filePath);
                            Path parent = path.getParent();
                            if (parent != null) {
                                Zip.extractZip(decrypteFileByte, parent.toString());
                            } else {
                                // parent가 null인 경우 현재 디렉토리 사용
                                Zip.extractZip(decrypteFileByte, ".");
                            }

                            Zip.extractZip(decrypteFileByte, ".");

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (GeneralSecurityException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    System.out.println("파일명: " + dp.getFileName());
                    //System.out.println("만료일: " + dp.getExpiryDate());
                    //System.out.println("제품: " + dp.getProductSpecification().getId());
                    //System.out.println("----------------------");
                });
            } else {
                System.out.println("경고: dataPermits가 null이거나 비어있습니다.");
            }
        } catch (IOException | XMLStreamException e) {
           throw new RuntimeException(e);
        }
    }
}
