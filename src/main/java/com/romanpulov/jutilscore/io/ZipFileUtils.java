package com.romanpulov.jutilscore.io;;

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
    public static String ZIP_EXT = ".zip";

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
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

        try {
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
        } finally {
            zipOutputStream.flush();
            zipOutputStream.close();
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

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            //init streams and entry
            inputStream = new FileInputStream(sourceFile);
            outputStream = new FileOutputStream(zipFile);

            zipStream(fileName, inputStream, outputStream);

        } catch (IOException e) {
            return null;
        } finally {
            //close input
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //close output
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return zipFile.getPath();
    }

    public static String unZipStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
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

    /**
     * Unzips from archive to the same path with original zipped name
     * @param filePath path to zipped file
     * @param fileName zipped file name
     * @return true if successful
     */
    public static boolean unZipFile(String filePath, String fileName) {
        ZipFile zipFile = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            zipFile = new ZipFile(filePath + fileName);
            if (zipFile.entries().hasMoreElements()) {
                ZipEntry zipEntry = zipFile.entries().nextElement();

                inputStream = zipFile.getInputStream(zipEntry);
                outputStream = new FileOutputStream(filePath + zipEntry.getName());

                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
            }

        } catch (IOException e) {
            return false;

        } finally {
            //zip file
            if (zipFile != null)
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            //input stream
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //output stream
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }
}
