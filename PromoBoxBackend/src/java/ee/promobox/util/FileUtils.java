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

    public static final int FILE_TYPE_JPG = 1;
    public static final int FILE_TYPE_PNG = 2;
    public static final int FILE_TYPE_MP3 = 3;
    public static final int FILE_TYPE_MP4 = 4;
    public static final int FILE_TYPE_AAC = 5;
    public static final int FILE_TYPE_AVI = 6;
    public static final int FILE_TYPE_MOV = 7;
    public static final int FILE_TYPE_PDF = 8;

    public static String renameFile(String filename) {

        return filename;
    }

    public static int determineFileTypeNumber(String filetype) {
        // if file type is not legal then 0 is returned
        switch (filetype.toUpperCase()) {
            case "JPG":
                return FILE_TYPE_JPG;
            case "JPEG":
                return FILE_TYPE_JPG;
            case "PNG":
                return FILE_TYPE_PNG;
            case "MP3":
                return FILE_TYPE_MP3;
            case "MP4":
                return FILE_TYPE_MP4;
            case "AAC":
                return FILE_TYPE_AAC;
            case "AVI":
                return FILE_TYPE_AVI;
            case "MOV":
                return FILE_TYPE_MOV;
            case "PDF":
                return FILE_TYPE_PDF;
            default:
                return 0;
        }
    }

    public static void convertFile(File f) {
        // path to converter
        ImageOP converter = new ImageOP("C:\\Program Files\\ImageMagick-6.8.9-Q16\\convert.exe");
        // input file into conveter
        converter.input(f);

        // appropriate convert is chosen depending on file type      
        switch (f.getName().split("\\.")[1].toUpperCase()) {
            case "JPG":
                break;
            case "JPEG":
                break;
            case "PNG":
                converter.resize(1920, 1080);
                // give correct name to file for output
                converter.processToFile(new File(f.getAbsolutePath().replace(".png", "_output.png")));
                break;
            case "MP3":
                break;
            case "MP4":
                break;
            case "AAC":
                break;
            case "AVI":
                break;
            case "MOV":
                break;
            case "PDF":
                converter.density(300);
                converter.resize(25, null, true);
                // give correct name to file for output
                converter.processToFile(new File(f.getAbsolutePath().replace(".pdf", ".png")));
                break;
            default:
                break;
        }
    }

    public static void createThumbnail() {

    }
}
