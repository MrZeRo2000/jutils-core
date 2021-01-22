package com.romanpulov.jutilscore.storage;

import com.romanpulov.jutilscore.io.FileUtils;
import com.romanpulov.jutilscore.io.ZipFileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.util.ArrayList;
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

    private static boolean prepareBackupFolder(String backupFolderName) {

        //init backup folder
        File backupFolder = new File(normalizeFolderName(backupFolderName));

        return backupFolder.exists() || backupFolder.mkdir();

    }

    /**
     * Created local backup
     * @return archived file name if successful
     */
    private static String createLocalBackup(String dataFileName, String backupFolderName, String backupFileName) {
        String backupFullFileName = getFullFileName(backupFolderName, backupFileName);

        if (backupFullFileName.equals(dataFileName) || !prepareBackupFolder(backupFolderName)) {
            return null;
        } else {
            String zipFileName  = ZipFileUtils.getZipFileName(backupFullFileName);
            if (FileUtils.copyWithZip(backupFileName, dataFileName, zipFileName)) {
                return backupFileName;
            } else {
                return null;
            }
        }
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

        //roll old backups
        List<String> fileNames = getBackupFileNames(backupFolderName);
        if (fileNames != null && !FileUtils.renameListCopies(fileNames) && !FileUtils.saveListCopies(fileNames)) {
            return null;
        }

        //create backup and return result
        return createLocalBackup(dataFileName, backupFolderName, backupFileName);
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

    /**
     * Returns backup file names from backup folder
     * @return File list
     */
    public static List<String> getBackupFileNames(String backupFolderName) {
        File[] fileList = getBackupFiles(backupFolderName);

        if (fileList == null) {
            return null;
        } else {
            List<String> result = new ArrayList<>(fileList.length);
            for (File f : fileList) {
                result.add(f.getAbsolutePath());
            }

            return result;
        }
    }

}
