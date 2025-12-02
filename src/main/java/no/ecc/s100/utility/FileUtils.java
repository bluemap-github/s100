package no.ecc.s100.utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

public class FileUtils {

    /**
     * Return the basename of the given filename.
     */
    public static String getBaseName(String fileNameWithPath) {
        String tmp = fileNameWithPath;
        int i = tmp.lastIndexOf('/');
        if (i == -1) {
            i = tmp.lastIndexOf('\\');
        }
        if (i != -1) {
            tmp = tmp.substring(i + 1);
        }
        return tmp;
    }

    /**
     * Return the basename of the given filename without suffix. "a/bc/file.txt" ->
     * "file".
     */
    public static String getFileNameWithoutSuffix(String fileName) {
        String s = getBaseName(fileName);
        int p = s.indexOf('.');
        if (p > 0) {
            s = s.substring(0, p);
        }
        return s;
    }

    public static String getFullPath(String targetFolder, String fileName) {
        Path start = Paths.get(targetFolder);

        try (Stream<Path> stream = Files.walk(start)) {  // try-with-resources
            Optional<Path> result = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().equals(fileName))
                    .findFirst();

            return result.map(Path::toAbsolutePath)
                    .map(Path::toString)
                    .orElse(null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String AddDecToFileName(String fullPath) {
        Path path = Paths.get(fullPath);
        String fileName = path.getFileName().toString();

        int dotIndex = fileName.lastIndexOf('.');
        String newFileName;

        if (dotIndex != -1) {
            // 확장자가 있는 경우
            String namePart = fileName.substring(0, dotIndex);
            String extPart = fileName.substring(dotIndex); // 포함된 점(.)부터
            newFileName = namePart + "_dec" + extPart;
        } else {
            // 확장자가 없는 경우
            newFileName = fileName + "_dec";
        }

        // 부모 디렉토리가 없으면 (상대 경로만 있을 때)
        if (path.getParent() == null) {
            return newFileName;
        } else {
            return path.getParent().resolve(newFileName).toString();
        }
    }
}