package com.romanpulov.jutilscore.storage;

import com.romanpulov.jutilscore.io.FileUtils;
import com.romanpulov.jutilscore.io.ZipFileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.util.List;

/**
 * Backup and restore for file
 * Created by romanpulov on 19.12.2016.
 */

public class BackupUtils {

    /**
     * Returns folder name from given folder name with ending slash
     * @param folderName input folder name
     * @return folder name with slash
     */
    public static String normalizeFolderName(String folderName) {
        if (folderName.endsWith(File.separator))
            return folderName;
        else
            return folderName + File.separator;
    }

    /**
     * Returns backup file name
     * @return file name
     */
    private static String getFullFileName(String folderName, String fileName) {
        return normalizeFolderName(folderName) + fileName;
    }

    /**
     * Created local backup
     * @return archived file name if successful
     */
    private static String createLocalBackup(String dataFileName, String backupFolderName, String backupFileName) {
        String backupFullFileName = getFullFileName(backupFolderName, backupFileName);

        //init backup folder
        File backupFolder = new File(normalizeFolderName(backupFolderName));
        if (!backupFolder.exists()) {
            if (!backupFolder.mkdir()) {
                return null;
            }
        }

        //write file
        if (!backupFullFileName.equals(dataFileName)) {
            if (!FileUtils.copy(dataFileName, backupFullFileName))
                return null;
        }

        //archive file
        return ZipFileUtils.zipFile(normalizeFolderName(backupFolderName), backupFileName);
    }

    /**
     * Restores backup from archive
     * @return restored file name if successful
     */
    public static String restoreBackup(String dataFileName, String backupFolderName, String backupFileName) {
        String backupFullFileName = getFullFileName(backupFolderName, backupFileName);

        //check backup availability
        File zipFile = new File(ZipFileUtils.getZipFileName(backupFullFileName));
        if (!zipFile.exists())
            return null;

        //extract backup
        if (!ZipFileUtils.unZipFile(normalizeFolderName(backupFolderName), ZipFileUtils.getZipFileName(backupFileName)))
            return null;

        //check restored file availability
        File file = new File(backupFullFileName);
        if (!file.exists())
            return null;

        if (!backupFileName.equals(dataFileName)) {
            //replace source file
            if (!FileUtils.copy(backupFullFileName, dataFileName))
                return null;

            //delete and ignore any errors
            file.delete();
        }

        return dataFileName;
    }

    public static String createRollingStreamBackup(
            InputStream dataFileStream,
            String backupFolderName,
            String backupFileName,
            List<String> backupFileNames,
            FileUtils.FileProcessor copyFileProcessor

    ) {
        //get file names
        String fileName = getFullFileName(backupFolderName, backupFileName);
        String zipFileName = ZipFileUtils.getZipFileName(fileName);

        if (!FileUtils.processListCopies(backupFileNames, copyFileProcessor)) {
            return null;
        }

        return null;
    }

    /**
     * Created rolling backup
     * @return archived file if successful
     */
    public static String createRollingLocalBackup(String dataFileName, String backupFolderName, String backupFileName) {
        //get file names
        String fileName = getFullFileName(backupFolderName, backupFileName);
        String zipFileName = ZipFileUtils.getZipFileName(fileName);

        //roll copies of data: first try rename, then copy
        if (!FileUtils.renameCopies(zipFileName))
            if ((!FileUtils.saveCopies(zipFileName)))
                return null;

        //create backup
        String result = createLocalBackup(dataFileName, backupFolderName, backupFileName);

        //delete non zipped file, ignore any errors
        if (result != null)
            FileUtils.delete(fileName);

        return result;
    }

    public static class BackupFileFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            String lowAbsolutePath = pathname.getAbsolutePath().toLowerCase();
            return lowAbsolutePath.endsWith(ZipFileUtils.ZIP_EXT) || lowAbsolutePath.matches("\\S*" +ZipFileUtils.ZIP_EXT + "." + FileUtils.BAK_EXT + "" + "[0-9]{2}");
        }
    }

    /**
     * Returns backup files from backup folder
     * @return File list
     */
    public static File[] getBackupFiles(String backupFolderName) {
        File folder = new File(normalizeFolderName(backupFolderName));
        return folder.listFiles(new BackupFileFilter());
    }
}
