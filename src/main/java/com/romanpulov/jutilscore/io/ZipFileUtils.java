package com.romanpulov.jutilscore.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Helper class for zipping and unzipping operations
 * Created by romanpulov on 19.12.2016.
 */

public class ZipFileUtils {
    public static final String ZIP_EXT = ".zip";

    public static String getZipFileName(String fileName){
        int extensionPos = fileName.lastIndexOf(".");
        int pathPos = fileName.lastIndexOf(File.separator);

        if ((extensionPos == -1) || (pathPos > extensionPos))
            return fileName + ZIP_EXT;
        else
            return fileName.substring(0, extensionPos) + ZIP_EXT;
    }

    /**
     * ZIPs the stream to the output stream
     * @param entryName file name
     * @param inputStream input stream to zip
     * @param outputStream output stream to write to
     * @throws IOException exception in case of errors working with streams
     */
    public static void zipStream(String entryName, InputStream inputStream, OutputStream outputStream) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)){
            //next entry
            zipOutputStream.putNextEntry(new ZipEntry(entryName));

            //write
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                zipOutputStream.write(buffer, 0, len);
            }

            //complete entry
            zipOutputStream.closeEntry();
        }
    }

    /**
     * ZIPs the file to the same path with zip extension
     * @param filePath path to file
     * @param fileName file name
     * @return true if successful
     */
    public static String zipFile(String filePath, String fileName) {
        File sourceFile = new File(filePath + fileName);
        if (!sourceFile.exists())
            return null;

        File zipFile = new File(filePath + getZipFileName(fileName));

        try (InputStream inputStream = new FileInputStream(sourceFile);
             OutputStream outputStream = new FileOutputStream(zipFile)) {
            //init streams and entry

            zipStream(fileName, inputStream, outputStream);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return zipFile.getPath();
    }

    /**
     * unZIPs from input stream to output stream
     * @param inputStream input stream with ZIP content
     * @param outputStream output stream
     * @return zip entry
     * @throws IOException in case of errors with streams
     */
    public static String unZipStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry entry = zipInputStream.getNextEntry();

            if (entry != null) {
                if (zipInputStream.available() > 0) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zipInputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, len);
                    }
                }
                return entry.getName();
            } else {
                return null;
            }
        }
    }

    /**
     * Unzips from archive to the same path with original zipped name
     * @param filePath path to zipped file
     * @param fileName zipped file name
     * @return true if successful
     */
    public static boolean unZipFile(String filePath, String fileName) {
        try (ZipFile zipFile = new ZipFile(filePath + fileName)) {

            if (zipFile.entries().hasMoreElements()) {
                ZipEntry zipEntry = zipFile.entries().nextElement();

                try (InputStream inputStream = zipFile.getInputStream(zipEntry);
                     OutputStream outputStream = new FileOutputStream(filePath + zipEntry.getName())
                     ) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, len);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
