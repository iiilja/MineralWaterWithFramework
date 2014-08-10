/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ee.promobox.util;

import java.io.File;

/**
 *
 * @author Dan
 */
public class FileUtils {

    public static final int INVALID_FILE_TYPE = 0;

    public static final int FILE_TYPE_IMAGE = 1;
    public static final int FILE_TYPE_AUDIO = 2;
    public static final int FILE_TYPE_VIDEO = 3;
    public static final int FILE_TYPE_HTNL = 4;
    public static final int FILE_TYPE_SWF = 5;


    public static String renameFile(String filename) {

        return filename;
    }

    public static int determineFileTypeNumber(String filetype) {
        // if file type is not legal then 0 is returned
        switch (filetype.toUpperCase()) {
            case "JPG":
                return FILE_TYPE_IMAGE;
            case "JPEG":
                return FILE_TYPE_IMAGE;
            case "PNG":
                return FILE_TYPE_IMAGE;
            case "MP3":
                return FILE_TYPE_AUDIO;
            case "MP4":
                return FILE_TYPE_VIDEO;
            case "AAC":
                return FILE_TYPE_AUDIO;
            case "AVI":
                return FILE_TYPE_VIDEO;
            case "MOV":
                return FILE_TYPE_VIDEO;
            case "PDF":
                return FILE_TYPE_IMAGE;
            default:
                return 0;
        }
    }

    

    public static void createThumbnail() {

    }
}
