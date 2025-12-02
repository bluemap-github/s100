package no.ecc.s100.security;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.crypto.SecretKey;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

//import com.google.common.io.Files;

import no.ecc.s100.S100ProductSpecification;
import no.ecc.s100.utility.FileUtils;
import no.ecc.s100.utility.Hex;
import no.ecc.s100.utility.Zip;

public class PermitTest {

    private static String mId = "859868";
    private static String mKey = "4D5A79677065774A7343705272664F72";
    private static String hwId = "40384B45B54596201114FE9904220101";
    private static String UPN = "AD1DAD797C966EC9F6A55B66ED98281599B3C7B1859868";

    private static S100Manufacturer manufacturer = new S100Manufacturer(mId, mKey);

    @Test
    public void testS100DataPermit() throws Exception {
        
        String dataKey = "1C81DFAB4053D04803FFDC87EF92FDD1";
                         
        // encrypt permit
        S100DataPermit dp = S100DataPermit.create("101NO12345678.000", 1, new Date(), dataKey, hwId,
                new S100ProductSpecification(101));
        assertEquals("172019407CDA6B8C1F545CCDB11B7297", dp.getEncryptedDataKey());
        
        // decrypt permit
        S100Crypt crypt = new S100Crypt.EmptyIVNoPadding(hwId);
        assertEquals(dataKey, Hex.toString(crypt.decrypt(Hex.fromString(dp.getEncryptedDataKey()))));
    }

    @Test
    public void testS100KeyGenerator() throws Exception {
        SecretKey key = S100KeyGenerator.generateKey();
        assertEquals(16, key.getEncoded().length);

        // key를 hex 문자열로 변환
        String keyHex = Hex.toString(key.getEncoded());
        assertEquals(32, keyHex.length());
    }

    @Test
    public void testGenerateKey() throws Exception {
        // ..\\data 폴더 안에있는 확장자가 .000인 파일명을 하위 폴더까지 다 찾아와서 키 쌍을 생성하여 csv로 저장
        List<File> files = new ArrayList<>();
        File dataFolder = new File("..\\data");
        findFiles(dataFolder, files);
        for (File file : files) {
            if (file.getName().endsWith(".000")) {
                String fileName = file.getName();
                String key = S100KeyGenerator.generateKeyHex();
                System.out.println(fileName + "," + key);
            }
        }
        // csv 파일로 저장
        File csvFile = new File("..\\data\\key.csv");
        BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));
        for (File file : files) {
            if (file.getName().endsWith(".000")) {
                String fileName = file.getName();
                String key = S100KeyGenerator.generateKeyHex();
                writer.write(fileName + "," + key);
                writer.newLine();
            }
        }
        writer.close();
    }

    // key.csv를 다시 읽어와서 Map에 저장
    private static Map<String, String> readKeyCsv() throws Exception {
        Map<String, String> keyMap = new HashMap<>();
        File csvFile = new File("..\\data\\key.csv");
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] key = line.split(",");
                keyMap.put(key[0], key[1]);
            }
        }
        return keyMap;
    }

    private void findFiles(File dataFolder, List<File> files) {
        File[] folderFiles = dataFolder.listFiles();
        if (folderFiles != null) {
            for (File file : folderFiles) {
                if (file.isDirectory()) {
                    findFiles(file, files);
                } else {
                    files.add(file);
                }   
            }
        }
    }

    @Test
    public void testGeneratePermit() throws Exception {
        Map<String, String> keyMap = readKeyCsv();
        for (Map.Entry<String, String> entry : keyMap.entrySet()) {
            String fileName = entry.getKey();
            String key = entry.getValue();
            System.out.println(fileName + "," + key);
        }

        // S100PermitFile 생성
        S100PermitFile permitFile = new S100PermitFile("KHRA", hwId, UPN);
        //permitFile.add("101NO12345678.000", 1, new Date(), dataKey, productSpec);

        // keyMap 순환
        for (Map.Entry<String, String> entry : keyMap.entrySet()) {
            String fileName = entry.getKey();
            String key = entry.getValue();
            // 2026년 6월 말 까지
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, 2026);
            calendar.set(Calendar.MONTH, Calendar.JUNE);
            calendar.set(Calendar.DAY_OF_MONTH, 30);
            Date permitEndDate = calendar.getTime();
            permitFile.add(fileName, 1, permitEndDate, key, new S100ProductSpecification(101));
        }

        // PERMIT.XML 파일로 저장
        S100PermitFileWriter.write(permitFile, "..\\data\\PERMIT.XML");
    }

    // 암호화 테스트
    @Test
    public void testEncS101() throws Exception {
        Map<String, String> keyMap = readKeyCsv();
        for (Map.Entry<String, String> entry : keyMap.entrySet()) {
            String fileName = entry.getKey();
            String key = entry.getValue();
            
            String filePath = FileUtils.getFullPath("..\\data\\S100_ROOT", fileName);
            if (filePath != null) {
                // 원본 파일을 ZIP 파일로 덮어쓰기
                String zipFilePath = filePath;
                Zip.compressZip(filePath, zipFilePath);

                try (InputStream fileIn = Files.newInputStream(Paths.get(zipFilePath))) {
                    byte[] inputFileByte = fileIn.readAllBytes();
                    
                    S100Crypt crypt =  new S100Crypt.RandomIV(key);
                    
                    

                    byte[] decrypteFileByte = crypt.encrypt(inputFileByte);
                    // write to file
                    Files.write(Paths.get(zipFilePath), decrypteFileByte);
                    //Files.write(Paths.get(zipFilePath), inputFileByte);
                }
            }
        }

        // assert
        assertEquals(true, true);
    }

    @Test
    public void genEncHWID() throws Exception {
        S100Crypt crypt = new S100Crypt.EmptyIVNoPadding(hwId);
        String encHWID = Hex.toString(crypt.encrypt(Hex.fromString(hwId)));
        System.out.println(encHWID);
         // assert
         assertEquals(true, true);
    }
}
