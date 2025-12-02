package no.ecc.s100.utility;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class Zip {
    /**
     * ZIP 파일을 안전하게 압축 해제하는 메소드
     */
    public static void extractZip(String zipFilePath, String extractDir) throws IOException {
        // 압축 해제할 디렉토리 생성
        Path extractPath = Paths.get(extractDir);
        Files.createDirectories(extractPath);

        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;

            while ((entry = zipIn.getNextEntry()) != null) {
                String fileName = entry.getName();
                System.out.println("압축 해제 중: " + fileName);

                // 보안: 경로 조작 공격 방지 (Zip Slip 방지)
                Path extractPathAbs = extractPath.toAbsolutePath().normalize();
                Path entryPath = extractPathAbs.resolve(fileName).normalize();
                if (!entryPath.startsWith(extractPathAbs)) {
                    throw new IOException("잘못된 ZIP 엔트리: " + fileName);
                }

                if (entry.isDirectory()) {
                    // 디렉토리 생성
                    Files.createDirectories(entryPath);
                } else {
                    // 파일 압축 해제
                    Files.createDirectories(entryPath.getParent());

                    try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(entryPath))) {

                        byte[] buffer = new byte[4096];
                        int read;

                        while ((read = zipIn.read(buffer)) != -1) {
                            bos.write(buffer, 0, read);
                        }
                    }

                    System.out.println("완료: " + fileName + " (" + Files.size(entryPath) + " bytes)");
                }
                zipIn.closeEntry();
            }
        }
    }

    public static void extractZip(byte[] zipBytes, String extractDir) throws IOException {
        // 압축 해제할 디렉토리 생성
        Path extractPath = Paths.get(extractDir);
        Files.createDirectories(extractPath);

        try (ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;

            while ((entry = zipIn.getNextEntry()) != null) {
                String fileName = entry.getName();
                System.out.println("압축 해제 중: " + fileName);

                // 보안: 경로 조작 공격 방지 (Zip Slip 방지)
                Path extractPathAbs = extractPath.toAbsolutePath().normalize();
                Path entryPath = extractPathAbs.resolve(fileName).normalize();
                if (!entryPath.startsWith(extractPathAbs)) {
                    throw new IOException("잘못된 ZIP 엔트리: " + fileName);
                }

                if (entry.isDirectory()) {
                    // 디렉토리 생성
                    Files.createDirectories(entryPath);
                } else {
                    // 파일 압축 해제
                    Files.createDirectories(entryPath.getParent());

                    try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(entryPath))) {
                        byte[] buffer = new byte[4096];
                        int read;
                        while ((read = zipIn.read(buffer)) != -1) {
                            bos.write(buffer, 0, read);
                        }
                    }

                    System.out.println("완료: " + fileName + " (" + Files.size(entryPath) + " bytes)");
                }
                zipIn.closeEntry();
            }
        }
    }

    /**
     * 파일 또는 디렉토리를 ZIP 파일로 압축하는 메소드
     * @param sourcePath 압축할 파일 또는 디렉토리 경로
     * @param zipFilePath 생성할 ZIP 파일 경로
     * @throws IOException 파일 입출력 오류 발생 시
     */
    public static void compressZip(String sourcePath, String zipFilePath) throws IOException {
        Path source = Paths.get(sourcePath);
        if (!Files.exists(source)) {
            throw new IOException("압축할 경로가 존재하지 않습니다: " + sourcePath);
        }

        Path zipPath = Paths.get(zipFilePath);
        boolean overwriteSameFile = source.toAbsolutePath().normalize().equals(zipPath.toAbsolutePath().normalize());
        
        System.out.println("압축 시작: " + sourcePath + " -> " + zipFilePath);
        System.out.println("소스 타입: " + (Files.isDirectory(source) ? "디렉토리" : "파일"));
        if (overwriteSameFile) {
            System.out.println("경고: 원본 파일을 덮어쓰기 모드로 압축합니다.");
        }
        
        if (Files.isRegularFile(source)) {
            System.out.println("소스 파일 크기: " + Files.size(source) + " bytes");
        }

        // 같은 파일을 덮어쓰는 경우 임시 파일 사용
        Path tempZipPath = overwriteSameFile ? 
            Paths.get(zipFilePath + ".tmp") : zipPath;

        try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(tempZipPath));
             ZipOutputStream zos = new ZipOutputStream(bos)) {
            
            if (Files.isDirectory(source)) {
                // 디렉토리인 경우 재귀적으로 압축
                compressDirectory(source, source, zos);
            } else {
                // 파일인 경우 압축
                compressFile(source, source.getFileName().toString(), zos);
            }
            
            // ZIP 파일 완료 처리
            zos.finish();
            // 버퍼를 명시적으로 flush
            zos.flush();
            bos.flush();
        }
        
        // 같은 파일을 덮어쓰는 경우 원본 삭제 후 임시 파일을 원본 이름으로 변경
        if (overwriteSameFile) {
            Files.deleteIfExists(zipPath);
            Files.move(tempZipPath, zipPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("원본 파일을 ZIP 파일로 덮어썼습니다.");
        }
        
        Path finalZipFile = Paths.get(zipFilePath);
        if (Files.exists(finalZipFile)) {
            System.out.println("ZIP 파일 생성 완료: " + zipFilePath + " (크기: " + Files.size(finalZipFile) + " bytes)");
        } else {
            System.out.println("경고: ZIP 파일이 생성되지 않았습니다: " + zipFilePath);
        }
    }

    /**
     * 파일 또는 디렉토리를 ZIP으로 압축하여 바이트 배열로 반환하는 메소드
     * @param sourcePath 압축할 파일 또는 디렉토리 경로
     * @return 압축된 ZIP 파일의 바이트 배열
     * @throws IOException 파일 입출력 오류 발생 시
     */
    public static byte[] compressZip(String sourcePath) throws IOException {
        Path source = Paths.get(sourcePath);
        if (!Files.exists(source)) {
            throw new IOException("압축할 경로가 존재하지 않습니다: " + sourcePath);
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(baos))) {
            
            if (Files.isDirectory(source)) {
                // 디렉토리인 경우 재귀적으로 압축
                compressDirectory(source, source, zos);
            } else {
                // 파일인 경우 압축
                compressFile(source, source.getFileName().toString(), zos);
            }
            
            zos.finish();
            return baos.toByteArray();
        }
    }

    /**
     * 디렉토리를 재귀적으로 압축하는 헬퍼 메소드
     */
    private static void compressDirectory(Path root, Path directory, ZipOutputStream zos) throws IOException {
        int fileCount = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    // 디렉토리인 경우 엔트리 추가 후 재귀 호출
                    String entryName = root.relativize(path).toString().replace('\\', '/') + "/";
                    zos.putNextEntry(new ZipEntry(entryName));
                    zos.closeEntry();
                    compressDirectory(root, path, zos);
                } else {
                    // 파일인 경우 압축
                    fileCount++;
                    String entryName = root.relativize(path).toString().replace('\\', '/');
                    compressFile(path, entryName, zos);
                }
            }
        }
        if (fileCount == 0) {
            System.out.println("경고: 디렉토리에 파일이 없습니다: " + directory);
        }
    }

    /**
     * 파일을 압축하는 헬퍼 메소드
     */
    private static void compressFile(Path file, String entryName, ZipOutputStream zos) throws IOException {
        long fileSize = Files.size(file);
        System.out.println("압축 시작: " + entryName + " (크기: " + fileSize + " bytes)");
        
        ZipEntry zipEntry = new ZipEntry(entryName);
        zipEntry.setSize(fileSize);
        zos.putNextEntry(zipEntry);
        
        long totalWritten = 0;
        try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(file))) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = bis.read(buffer)) != -1) {
                zos.write(buffer, 0, read);
                totalWritten += read;
            }
        }
        
        zos.closeEntry();
        System.out.println("압축 완료: " + entryName + " (쓰기: " + totalWritten + " bytes)");
    }
}
