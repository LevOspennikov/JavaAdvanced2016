package ru.ifmo.ctddev.ospennikov.walk;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
public class FileWalker extends SimpleFileVisitor<Path> {

    private BufferedWriter os;
    static final private String errorCheckSum = "00000000000000000000000000000000";
    static final private int ARRSIZE = 2048;
    FileWalker(BufferedWriter outputStream) {
        super();
        os = outputStream;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String hash = getMD5Checksum(file);
        os.write(hash + ' ' + file.toString());
        os.newLine();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        if (exc != null) {
            System.err.println(exc);
        }
        String hash = errorCheckSum;
        try {
            os.write(hash + ' ' + file.toString());
            os.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return FileVisitResult.CONTINUE;
    }

    public static String getMD5Checksum(Path file) throws IOException {
        try (FileInputStream is = new FileInputStream(file.toString())) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            String digest = getDigest(is, md, ARRSIZE);
            return digest;
        } catch (IOException e) {
            e.printStackTrace();
            return errorCheckSum;
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
            return errorCheckSum;
        }
    }

    private static String getDigest(InputStream is, MessageDigest md, int byteArraySize)
            throws IOException {
        md.reset();
        byte[] bytes = new byte[byteArraySize];
        int count;
        while ((count = is.read(bytes)) != -1) {
            md.update(bytes, 0, count);
        }
        byte[] digest = md.digest();
        String result = "";
        for (int i = 0; i < digest.length; i++) {
            result += Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result.toUpperCase();
    }
}
