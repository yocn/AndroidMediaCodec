package com.yocn.meida.util;

import android.app.Activity;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Video;
import android.text.TextUtils;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;


public class FileUtils {
    /**
     * KB
     */
    public static final long ONE_KB = 1024;
    /**
     * MB
     */
    public static final long ONE_MB = ONE_KB * ONE_KB;
    /**
     * GB
     */
    public static final long ONE_GB = ONE_KB * ONE_MB;
    /**
     * 后缀名分隔符
     */
    public static final char EXTENSION_SEPARATOR = '.';

    // -------------------- 获得文件的md5等hash值---------------------//
    public final static String HASH_TYPE_MD5 = "MD5";
    public final static String HASH_TYPE_SHA1 = "SHA1";
    public final static String HASH_TYPE_SHA1_256 = "SHA-256";
    public final static String HASH_TYPE_SHA1_384 = "SHA-384";
    public final static String HASH_TYPE_SHA1_512 = "SHA-512";
    public static char[] hexChar = {'0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    // 处理lossless音频格式的文件。
    // zhouhenlei add .2010-10-14
    // 音频格式
    public static final String[] AUDIO_EXTS = {".flac", ".FLAC", ".ape",
            ".APE", ".wv", ".WV", ".mpc", ".MPC", "m4a", "M4A", ".wav", ".WAV",
            ".mp3", ".MP3", ".wma", ".WMA", ".ogg", ".OGG", ".3gpp", ".3GPP",
            ".aac", ".AAC"};
    // 无损音频格式
    public static final String[] LOSSLESS_EXTS = {".flac", ".FLAC", ".ape",
            ".APE", ".wv", ".WV", ".mpc", ".MPC", "m4a", "M4A", ".wav", ".WAV"};
    // 有损音频格式
    public static final String[] LOSS_EXTS = {".mp3", ".MP3", ".wma", ".WMA",
            ".ogg", ".OGG", ".3gpp", ".3GPP", ".aac", ".AAC"};
    // 用在ape,flac的cue后缀
    public static final String CUE_EXT = ".cue";
    // 自定义的播放列表后缀，暂时不用。
    public static final String CUSTOM_PLAYLIST_EXT = ".playlist";
    // 列表格式
    public static final String[] PLAYLIST_EXTS = {CUSTOM_PLAYLIST_EXT, ".m3u",
            ".M3U", ".pls", ".PLS"};
    // 自定义书签到后缀
    public static final String BOOKMARK_EXT = ".bmark";

    private static final String TAG = "FileUtils";
    /**
     * Unix路径分隔符
     */
    private static final char UNIX_SEPARATOR = '/';
    /**
     * Windows路径分隔符
     */
    private static final char WINDOWS_SEPARATOR = '\\';
    public static final int BUF_SIZE = 4096;


    public static boolean writeToFile(byte[] bytes, String fullfilename, boolean isAppend) {
        if (bytes == null || TextUtils.isEmpty(fullfilename)) {
            return false;
        }
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(fullfilename, isAppend));
            out.write(bytes);
        } catch (IOException ioe) {
            LogUtil.e(TAG, "IOException : " + ioe.getMessage());
            return false;
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (IOException ioe) {
                LogUtil.e(TAG, ioe);
                return false;
            }
        }
        return true;
    }

    /**
     * 将文件大小的long值转换为可读的文字
     *
     * @param size
     * @param scale 保留几位小数
     * @return 10KB或10MB或1GB
     */
    public static String byteCountToDisplaySize(long size, int scale) {
        String displaySize;
        if (size / ONE_GB > 0) {
            float d = (float) size / ONE_GB;
            displaySize = getOffsetDecimal(d, scale) + " GB";
        } else if (size / ONE_MB > 0) {
            float d = (float) size / ONE_MB;
            displaySize = getOffsetDecimal(d, scale) + " MB";
        } else if (size / ONE_KB > 0) {
            float d = (float) size / ONE_KB;
            displaySize = getOffsetDecimal(d, scale) + " KB";
        } else {
            displaySize = String.valueOf(size) + " bytes";
        }
        return displaySize;
    }

    /**
     * 将文件大小的long值转换为可读的文字
     *
     * @param size
     * @param scale 保留几位小数
     * @return 10MB
     */
    public static String byteCountToDisplayMBSize(long size, int scale) {
        String displaySize = "0.00M";
        if (size / ONE_MB >= 0) {
            float d = (float) size / ONE_MB;
            if (d < 0.3) {
                d = 0;
            }
            displaySize = getOffsetDecimal(d, scale) + "M";
        }
        return displaySize;
    }

    public static String getOffsetDecimal(float ft, int scale) {
        int roundingMode = 4;//表示四舍五入，可以选择其他舍值方式，例如去尾，等等.
        BigDecimal bd = new BigDecimal(ft);
        bd = bd.setScale(scale, roundingMode);
        ft = bd.floatValue();
        return "" + ft;
    }

    public static boolean delAllFileWithoutDir(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                //temp.delete();
                deleteFileSafely(temp);
            }
            if (temp.isDirectory()) {
                delAllFileWithoutDir(path + "/" + tempList[i]);// 先删除文件夹里面的文件
                flag = true;
            }
        }
        return flag;
    }

    /**
     * read file to a string
     *
     * @param file
     * @return
     */
    public static String loadString(File file) {
        if (null == file || !file.exists()) {
            return "";
        }
        FileInputStream fis = null;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            fis = new FileInputStream(file);
            int restSize = fis.available();
            int bufSize = restSize > BUF_SIZE ? BUF_SIZE : restSize;
            byte[] buf = new byte[bufSize];
            while (fis.read(buf) != -1) {
                baos.write(buf);
                restSize -= bufSize;

                if (restSize <= 0) {
                    break;
                }
                if (restSize < bufSize) {
                    bufSize = restSize;
                }
                buf = new byte[bufSize];
            }
        } catch (FileNotFoundException e) {
            LogUtil.e(TAG, e);
        } catch (SecurityException e) {
            LogUtil.e(TAG, e);
        } catch (IOException e) {
            LogUtil.e(TAG, e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    LogUtil.e(TAG, e);
                }
            }
        }

        return baos.toString();
    }

    public static long getFolderSize(File folder)
            throws IllegalArgumentException {
        // Validate
        if (folder == null || !folder.isDirectory())
            throw new IllegalArgumentException("Invalid   folder ");
        String list[] = folder.list();
        if (list == null || list.length < 1)
            return 0;

        // Get size
        File object = null;
        long folderSize = 0;
        for (int i = 0; i < list.length; i++) {
            object = new File(folder, list[i]);
            if (object.isDirectory())
                folderSize += getFolderSize(object);
            else if (object.isFile())
                folderSize += object.length();
        }
        return folderSize;
    }

    /**
     * 检查文件是否存在于某目录下, 3.3.0
     *
     * @param filePathName
     * @return
     */
    public static String checkFile(String filePathName) {
        // 添加入口参数检查
        if (filePathName == null) {
            return null;
        }
        // 在图片存储目录里检查
        File file = new File(filePathName);
        if (file.exists() || file.isFile()) {
            try {
                return file.getCanonicalPath();
            } catch (IOException e) {
                LogUtil.e(TAG, e);
            }
        }
        return null;
    }

    public static boolean checkValidFile(String filePathName) {
        // 添加入口参数检查
        if (filePathName == null) {
            return false;
        }
        File file = new File(filePathName);
        if (file.exists() && file.isFile() && file.length() > 0) {
            return true;
        }
        return false;
    }

    public static long getFileLength(String filePathName) {
        if (filePathName == null) {
            return 0;
        }
        File file = new File(filePathName);
        if (file.exists() && file.isFile()) {
            return file.length();
        }
        return 0;
    }


    public static String checkJpgFile(String filePathName) {
        // 添加入口参数检查
        if (filePathName == null) {
            return null;
        }
        // 在图片存储目录里检查
        File file = new File(filePathName);
        if (file.exists() || file.isFile()) {
            try {
                if (file.length() < 9) {
                    return null;
                }
                return file.getCanonicalPath();
            } catch (IOException e) {
                LogUtil.e(TAG, e);
            }
        }
        return null;
    }

    /**
     * 获取文件编码
     *
     * @param sourceFile
     * @return
     */
    public static String getFileCharset(String sourceFile) {
        LogUtil.d(TAG, "getFileCharset, sourceFile=" + sourceFile);
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        BufferedInputStream bis = null;
        try {
            boolean checked = false;
            bis = new BufferedInputStream(new FileInputStream(sourceFile));
            bis.mark(3);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1) {
                return charset; // 文件编码为 ANSI
            } else if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                charset = "UTF-16LE"; // 文件编码为 Unicode
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF) {
                charset = "UTF-16BE"; // 文件编码为 Unicode big endian
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1] == (byte) 0xBB && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF-8"; // 文件编码为 UTF-8
                checked = true;
            }
            bis.reset();
            if (!checked) {
                while ((read = bis.read()) != -1) {
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)
                            // (0x80
                            // - 0xBF),也可能在GB编码内
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) {// 也有可能出错，但是几率较小
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.e(TAG, e);
        } finally {
            if (bis != null)
                try {
                    bis.close();
                } catch (IOException e) {
                    LogUtil.e(TAG, e);
                }
        }
        LogUtil.d(TAG, "getFileCharset, charset=" + charset);
        return charset;
    }

    public static String getName(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfLastSeparator(filename);
        return filename.substring(index + 1);
    }

    public static int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        }
        int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
        int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
        return Math.max(lastUnixPos, lastWindowsPos);
    }

    /**
     * 流拷贝
     *
     * @param is
     * @param os
     * @throws IOException
     */
    public static void copyStream(InputStream is, OutputStream os) throws IOException {
        byte buffer[] = new byte[BUF_SIZE];
        int len = -1;
        while ((len = is.read(buffer, 0, BUF_SIZE)) != -1) {
            os.write(buffer, 0, len);
        }
    }

    /**
     * 流拷贝
     *
     * @param is
     * @param os
     * @throws IOException
     */
    public static void copyStream(InputStream is, OutputStream os, int firstOffest) throws IOException {
        byte buffer[] = new byte[BUF_SIZE];
        byte offestBuffer[] = new byte[firstOffest];
        int len = -1;
//        long size = is.skip(firstOffest);
//        LogUtil.d("yocn size->" + size);
        is.read(offestBuffer);
        while ((len = is.read(buffer, 0, BUF_SIZE)) != -1) {
            os.write(buffer, 0, len);
        }
    }

    public static byte[] byteMerger(byte[] byte_1) {
        byte[] byte_2 = new byte[byte_1.length * 2];
        for (int i = 0; i < byte_1.length; i++) {
            if (i % 2 == 0) {
                byte_2[2 * i] = byte_1[i];
                byte_2[2 * i + 1] = byte_1[i + 1];
            } else {
                byte_2[2 * i] = byte_1[i - 1];
                byte_2[2 * i + 1] = byte_1[i];
            }
        }
        return byte_2;
    }

    static int index = 0;

    /**
     * 两个字节使用一个字节
     *
     * @param doubleBytes 两个字节的数组
     * @return
     */
    public static byte[] byteDivider(byte[] doubleBytes) {
        byte[] output = new byte[doubleBytes.length / 2];
        int outputIndex = 0;
        for (int n = 0; n < doubleBytes.length; n += 4) {
            // copy in the first 16 bit sample
            output[outputIndex++] = doubleBytes[n];
            output[outputIndex++] = doubleBytes[n + 1];
        }
        return output;
    }

    /**
     * 流拷贝
     *
     * @param is
     * @param os
     * @throws IOException
     */
    public static void copyStreamChannel(InputStream is, OutputStream os) throws IOException {
        byte buffer[] = new byte[BUF_SIZE];
        int len = -1;
        while ((len = is.read(buffer, 0, BUF_SIZE)) != -1) {
            os.write(byteMerger(buffer), 0, len * 2);
        }
    }

    /**
     * 流拷贝
     *
     * @param is
     * @param os
     * @throws IOException
     */
    public static void copyStreamFromDoubleChannel2SingleChannel(InputStream is, OutputStream os) throws IOException {
        int bits = 16;
        byte buffer[] = new byte[bits];
        int len = -1;
        while ((len = is.read(buffer, 0, bits)) != -1) {
            os.write(byteDivider(buffer), 0, len / 2);
        }
    }

    private static void writeInteger(final OutputStream stream, final int size, final int value) throws IOException {
        for (int i = 0; i < size; i++) {
            stream.write((value >>> (i * 8)) & 0xFF);
        }
    }

    /**
     * 拷贝文件
     *
     * @param src
     * @param desc
     * @throws IOException
     */
    public static void copyChannelFile(File src, File desc) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(src);
            os = new FileOutputStream(desc);
            copyStreamChannel(is, os);
        } finally {
            if (is != null)
                is.close();
            if (os != null)
                os.close();
        }
    }

    /**
     * 拷贝文件 双声道到单声道
     *
     * @param src
     * @param desc
     * @throws IOException
     */
    public static void copyChannelFileFromDoubleChannel2Single(File src, File desc) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(src);
            os = new FileOutputStream(desc);
            copyStreamFromDoubleChannel2SingleChannel(is, os);
        } finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }


    /**
     * 拷贝流到文件中
     *
     * @param is
     * @param desc
     * @throws IOException
     */
    public static void copyStream2File(InputStream is, File desc) throws IOException {
        OutputStream os = null;
        try {
            os = new FileOutputStream(desc);
            copyStream(is, os);
        } finally {
            if (os != null)
                os.close();
        }
    }

    /**
     * 拷贝文件
     *
     * @param src
     * @param desc
     * @throws IOException
     */
    public static void copyFile(File src, File desc) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(src);
            os = new FileOutputStream(desc);
            copyStream(is, os);
        } finally {
            if (is != null)
                is.close();
            if (os != null)
                os.close();
        }
    }

    /**
     * 拷贝src的前limit个字节到dest
     *
     * @param srcPath  源文件路径
     * @param destPath 目标文件路径
     * @param limit    limit
     */
    public static boolean copyFileWithLimit(String srcPath, String destPath, int limit) {
        boolean isSuccess = true;
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(srcPath);
            os = new FileOutputStream(destPath);

            byte[] buffer = new byte[1024];
            int byteRead = 0;
            int byteReadTotal = 0;
            while ((byteRead = is.read(buffer)) > 0) {
                byteReadTotal += byteRead;
                if (byteReadTotal >= limit) {
                    byte[] temp = new byte[byteReadTotal - limit];
                    System.arraycopy(buffer, 0, temp, 0, temp.length);
                    break;
                }
                os.write(buffer);
            }
        } catch (IOException e) {
            isSuccess = false;
            LogUtil.d("yocn ->" + e.toString());
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                isSuccess = false;
                e.printStackTrace();
                LogUtil.d("yocn ->" + e.toString());
            }
        }
        return isSuccess;
    }

    /**
     * 拷贝文件
     *
     * @param src
     * @param desc
     * @throws IOException
     */
    public static void copyFileWithOffest(File src, File desc, int firstOffest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(src);
            os = new FileOutputStream(desc);
            copyStream(is, os, firstOffest);
        } finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }

    public static void copyFile(String srcPath, String destPath) throws IOException {
        copyFile(new File(srcPath), new File(destPath));
    }

    /**
     * 获得文件简单名称
     *
     * @param file
     * @return
     */
    public static String getSimpleName(String file) {
        return getSimpleName(new File(file));
    }

    /**
     * 获得文件简单名称
     *
     * @param file
     * @return
     */
    public static String getSimpleName(File file) {
        if (file == null) return "";

        if (file.isDirectory()) {
            return file.getName();
        }

        String filePath = file.getName();
        int index = filePath.lastIndexOf(".");

        if (index != -1) {
            return filePath.substring(0, index);
        }

        return filePath;
    }

    /**
     * 根据文件URI判断是否为媒体文件
     *
     * @param uri
     * @return
     */
    public static boolean isMediaUri(String uri) {
        if (uri.startsWith(Audio.Media.INTERNAL_CONTENT_URI.toString())
                || uri.startsWith(Audio.Media.EXTERNAL_CONTENT_URI.toString())
                || uri.startsWith(Video.Media.INTERNAL_CONTENT_URI.toString())
                || uri.startsWith(Video.Media.EXTERNAL_CONTENT_URI.toString())) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean copyfile(File src, File desc) throws Exception {
        if (src == null || desc == null) {
            return false;
        }
        InputStream is = null;
        OutputStream os = null;
        try {
            if (!src.isFile() || !src.exists()) {
                return false;
            }

            if (!checkFile(desc)) {
                return false;
            }
            is = new FileInputStream(src);
            os = new FileOutputStream(desc);

            byte[] buf = new byte[BUF_SIZE];
            int len;
            while ((len = is.read(buf)) > 0) {
                os.write(buf, 0, len);
            }
        } catch (FileNotFoundException e) {
            LogUtil.e(TAG, e);
            throw e;
        } catch (IOException e) {
            LogUtil.e(TAG, e);
            throw e;
        } catch (Exception e) {
            LogUtil.e(TAG, e);
            throw e;
        } finally {
            if (is != null)
                is.close();
            if (os != null)
                os.close();
        }
        return true;
    }

//    public static String getFileName(String path) {
//        if (path == null) {
//            return null;
//        }
//        String retStr = "";
//        if (path.indexOf(File.separator) > 0) {
//            retStr = path.substring(path.lastIndexOf(File.separator) + 1);
//        } else {
//            retStr = path;
//        }
//        return retStr;
//    }

    public static String getFileName(String path) {
        if (StringUtils.isEmpty(path)) {
            return null;
        }

        int separatorIndex = path.lastIndexOf("/");
        return (separatorIndex < 0) ? path : path.substring(separatorIndex + 1, path.length());
    }

    public static String getFileParent(String path) {
        if (StringUtils.isEmpty(path)) {
            return null;
        }

        File file = new File(path);
        if (file != null) {
            return file.getParent();
        }
        return null;
    }

    public static String getFileNameNoPostfix(String path) {
        if (path == null) {
            return null;
        }
        return path.substring(path.lastIndexOf(File.separator) + 1);
    }

    /**
     * 根据文件URI得到文件扩展名
     *
     * @param uri 文件路径标识
     * @return
     */
    public static String getExtension(String uri) {
        if (uri == null)
            return "";

        int extensionIndex = uri.lastIndexOf(EXTENSION_SEPARATOR);
        int lastUnixIndex = uri.lastIndexOf(UNIX_SEPARATOR);
        int lastWindowsIndex = uri.lastIndexOf(WINDOWS_SEPARATOR);
        int index = Math.max(lastUnixIndex, lastWindowsIndex);
        if (index > extensionIndex || extensionIndex < 0)
            return "";
        return uri.substring(extensionIndex + 1);
    }

    /**
     * 判断是否为本地文件
     *
     * @param uri
     * @return
     */
    public static boolean isLocal(String uri) {
        if (uri != null && !uri.startsWith("http://")) {
            return true;
        }
        return false;
    }

    /**
     * 判断文件是否为视频文件
     *
     * @param filename
     * @return
     */
    public static boolean isVideo(String filename) {
        String mimeType = getMimeType(filename);
        if (mimeType != null && mimeType.startsWith("video/")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断文件是否为音频文件
     *
     * @param filename
     * @return
     */
    public static boolean isAudio(String filename) {
        String mimeType = getMimeType(filename);
        if (mimeType != null && mimeType.startsWith("audio/")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 根据文件名得到文件的mimetype 简单判断,考虑改为xml文件配置关联
     *
     * @param filename
     * @return
     */
    public static String getMimeType(String filename) {
        String mimeType = null;

        if (filename == null) {
            return mimeType;
        }
        if (filename.endsWith(".3gp")) {
            mimeType = "video/3gpp";
        } else if (filename.endsWith(".mid")) {
            mimeType = "audio/mid";
        } else if (filename.endsWith(".mp3")) {
            mimeType = "audio/mpeg";
        } else if (filename.endsWith(".xml")) {
            mimeType = "text/xml";
        } else {
            mimeType = "";
        }
        return mimeType;
    }

    /**
     * 将文件大小的long值转换为可读的文字
     *
     * @param size
     * @return 10KB或10MB或1GB
     */
    public static String byteCountToDisplaySize(long size) {
        String displaySize;

        if (size / ONE_GB > 0) {
            displaySize = String.valueOf(size / ONE_GB) + " GB";
        } else if (size / ONE_MB > 0) {
            displaySize = String.valueOf(size / ONE_MB) + " MB";
        } else if (size / ONE_KB > 0) {
            displaySize = String.valueOf(size / ONE_KB) + " KB";
        } else {
            displaySize = String.valueOf(size) + " bytes";
        }
        return displaySize;
    }

    public static boolean isDirectory(File file) {
        return file.exists() && file.isDirectory();
    }

    public static boolean isDirectory(String name) {
        File file = new File(name);
        return file.exists() && file.isDirectory();
    }

    public static boolean isFile(File file) {
        return file.exists() && file.isFile();
    }

    public static boolean createNewDirectory(File file) {
        if (file.exists() && file.isDirectory()) {
            return false;
        }
        return file.mkdirs();
    }

    public static boolean mvFile(String srcFileName, String destFileName) {
        File srcFile = new File(srcFileName);
        File destFile = new File(destFileName);

        if ((!srcFile.exists()) || (destFile.exists()))
            return false;

        return srcFile.renameTo(destFile);
    }

    public static boolean deleteFile(String filePath) {
        if (filePath == null || filePath.length() < 1)
            return true;
        File file = new File(filePath);
        return deleteFileSafely(file);
    }

    public static boolean deleteFile(File file) {
        if (!file.exists())
            return true;
        boolean flag = false;
        if (file.isFile())
            flag = file.delete();
        return flag;
    }

    public static void delDirectory(String directoryPath) {
        try {
            delAllFile(directoryPath); // 删除完里面所有内容
            File myFilePath = new File(directoryPath);
            myFilePath.delete(); // 删除空文件夹
        } catch (Exception e) {
            LogUtil.e(TAG, e);
        }
    }

    public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
                delDirectory(path + "/" + tempList[i]);// 再删除空文件夹
                flag = true;
            }
        }
        return flag;
    }

    public static String getHash(String fileName, String hashType)
            throws Exception {
        InputStream fis;
        fis = new FileInputStream(fileName);
        byte[] buffer = new byte[BUF_SIZE];
        MessageDigest md5 = MessageDigest.getInstance(hashType);
        int numRead = 0;
        while ((numRead = fis.read(buffer)) > 0) {
            md5.update(buffer, 0, numRead);
        }
        fis.close();
        return toHexString(md5.digest());
    }

    public static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(hexChar[(b[i] & 0xf0) >>> 4]);
            sb.append(hexChar[b[i] & 0x0f]);
        }
        return sb.toString();
    }

    // true :lossless
    // false:loss
    public static boolean isLosslessSupported(File f) {
        String s = f.toString();
        if (s.endsWith(".flac") || s.endsWith(".FLAC"))
            return true;
        else if (s.endsWith(".ape") || s.endsWith(".APE"))
            return true;
        else if (s.endsWith(".wav") || s.endsWith(".WAV"))
            return true;
        else if (s.endsWith(".wv") || s.endsWith(".WV"))
            return true;
        else if (s.endsWith(".mpc") || s.endsWith(".MPC"))
            return true;
        else if (s.endsWith(".m4a") || s.endsWith(".M4A"))
            return true;
        else
            return false;
    }

    /**
     * 清除目录dirPath下以prefix为前缀的文件
     *
     * @param dirPath
     * @param prefix
     */
    public static void clearFilesWithPrefix(String dirPath, String prefix) {
        if (TextUtils.isEmpty(dirPath) || TextUtils.isEmpty(prefix))
            return;
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory())
            return;
        String filename = null;
        for (File file : dir.listFiles()) {
            filename = file.getName();
            if (filename.startsWith(prefix))
                file.delete();
        }
    }

    public static void clearInfoForFile(String fileName) throws IOException {
        File file = new File(fileName);
        if (file.exists()) {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
        }
    }

    /**
     * 清除目录dirPath下以prefix为前缀的文件 ,除了exceptPrefix前缀的文件
     *
     * @param dirPath
     * @param prefix
     * @param exceptPrefix 除过的文件前缀名
     */
    public static void clearFilesWithPrefix(String dirPath, String prefix, String exceptPrefix) {
        if (TextUtils.isEmpty(dirPath) || TextUtils.isEmpty(prefix))
            return;
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory())
            return;
        String filename = null;
        for (File file : dir.listFiles()) {
            filename = file.getName();
            if (filename.startsWith(prefix) && !filename.startsWith(exceptPrefix))
                file.delete();
        }
    }

    /**
     * 清除目录dirPath下后缀名为suffix的文件
     *
     * @param dirPath
     * @param suffix
     */
    public static void clearFiles(String dirPath, String suffix) {
        if (TextUtils.isEmpty(dirPath) || TextUtils.isEmpty(suffix))
            return;
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory())
            return;
        String filename = null;
        for (File file : dir.listFiles()) {
            filename = file.getName();
            if (filename.endsWith(suffix))
                file.delete();
        }
    }

    public static String writeToFile(InputStream is, String filepath, boolean isAppend) throws IOException {
        if (is == null || TextUtils.isEmpty(filepath)) {
            return null;
        }
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try {
            LogUtil.d(TAG, "write to file : " + filepath);

            File file = new File(filepath);
            checkDir(file.getParent());
            in = new BufferedInputStream(is);
            out = new BufferedOutputStream(new FileOutputStream(filepath, isAppend));
            byte[] buffer = new byte[BUF_SIZE];
            int l;
            while ((l = in.read(buffer)) != -1) {
                out.write(buffer, 0, l);
            }
        } catch (IOException e) {
            LogUtil.e(TAG, e);
            throw e;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                is.close();
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (IOException ioe) {
                LogUtil.e(TAG, ioe);
            }
        }
        return filepath;
    }

    /**
     * 输入忽略is的前firstOffest个字节
     *
     * @param is
     * @param filepath
     * @param isAppend
     * @param firstOffest
     * @return
     * @throws IOException
     */
    public static String writeToFile(InputStream is, String filepath, boolean isAppend, int firstOffest) throws IOException {
        if (is == null || TextUtils.isEmpty(filepath)) {
            return null;
        }
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try {
            LogUtil.d(TAG, "yocn write to firstOffest : " + firstOffest);

            File file = new File(filepath);
            checkDir(file.getParent());
            in = new BufferedInputStream(is);
            out = new BufferedOutputStream(new FileOutputStream(filepath, isAppend));
            byte[] buffer = new byte[BUF_SIZE];
            int l;
            in.skip(firstOffest);
            while ((l = in.read(buffer)) != -1) {
                out.write(buffer, 0, l);
            }
        } catch (IOException e) {
            LogUtil.e(TAG, e);
            throw e;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                is.close();
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (IOException ioe) {
                LogUtil.e(TAG, ioe);
            }
        }
        return filepath;
    }

    /**
     * 将输入流is指定的数据写入filepath指定的文件中
     *
     * @param is
     * @param filepath
     * @return
     * @throws java.io.IOException
     */
    public static String writeToFile(InputStream is, String filepath) throws IOException {
        return writeToFile(is, filepath, false);
    }

    /**
     * 将输入流is指定数据同步写入filepath指定位置
     *
     * @param is
     * @param filepath
     * @return
     * @throws java.io.IOException
     */
    synchronized public static String writeToFileSync(InputStream is,
                                                      String filepath) throws IOException {
        return writeToFile(is, filepath);
    }


    public static byte[] readFileToBytes(File file) {
        if (null == file)
            return null;
        FileInputStream fileInput = null;

        try {
            fileInput = new FileInputStream(file);
            byte[] buf = new byte[(int) file.length()];
            fileInput.read(buf);
            return buf;
        } catch (Exception e) {
            LogUtil.e(TAG, e);
        } finally {
            if (fileInput != null) {
                try {
                    fileInput.close();
                } catch (IOException e) {
                    LogUtil.e(TAG, e);
                }
            }
        }
        return null;
    }

    public static String readFileToString(File file) {
        if (null == file)
            return "";
        FileInputStream fileInput = null;
        StringBuffer strBuf = new StringBuffer();

        try {
            fileInput = new FileInputStream(file);
            byte[] buf = new byte[BUF_SIZE];
            while (fileInput.read(buf) != -1) {
                strBuf.append(new String(buf));
                buf = new byte[BUF_SIZE];
            }
        } catch (Exception e) {
            LogUtil.e(TAG, e);
        } finally {
            if (fileInput != null) {
                try {
                    fileInput.close();
                } catch (IOException e) {
                    LogUtil.e(TAG, e);
                }
            }
        }
        return strBuf.toString();
    }

    /**
     * list all files from current path
     *
     * @param path
     * @return
     */
    public static File[] listFiles(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();
        }
        try {
            return dir.listFiles();
        } catch (SecurityException ex) {
            return null;
        }
    }

    /**
     * get a file lines
     *
     * @return
     */
    public static int getFileLines(File file) {
        if (null == file)
            return 0;
        BufferedReader bufReader = null;
        int count = 0;
        try {
            bufReader = new BufferedReader(new FileReader(file));

            while ((bufReader.readLine()) != null)
                count++;
        } catch (FileNotFoundException e) {
            LogUtil.e(TAG, e);
            count = 0;
        } catch (IOException e) {
            LogUtil.e(TAG, e);
        } finally {
            if (bufReader != null) {
                try {
                    bufReader.close();
                } catch (IOException e) {
                    LogUtil.e(TAG, e);
                }
            }
        }
        return count;
    }

    /**
     * 检查目录是否存在，如果不存在创建之
     *
     * @return 目录是否存在
     */
    public static boolean checkDir(String dirPath) {
        if (TextUtils.isEmpty(dirPath))
            return false;
        File dir = new File(dirPath);
        if (dir.exists() && dir.isDirectory())
            return true;
        if (dir.exists())
            dir.delete();
        return dir.mkdirs();
    }

    /**
     * 获取目录所占空间大小(包括子目录文件)
     *
     * @param dirPath 目录路径
     * @return
     */
    public static long getDirLength(String dirPath) {
        if (TextUtils.isEmpty(dirPath))
            return 0;
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory())
            return 0;
        long length = 0;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                length += file.length();
            else if (file.isDirectory())
                length += getDirLength(file.getAbsolutePath());
        }
        return length;
    }

    public static int readData(byte[] data, int sizeInBytes, RandomAccessFile file) {
        int count = 0;
        try {
            int readSize = sizeInBytes;
            while (count < sizeInBytes) {
                int ret = file.read(data, count, readSize);
                count += ret;
                if (count <= 0 || ret <= 0) break;
                if (sizeInBytes - count > sizeInBytes) {
                    readSize = sizeInBytes;
                } else {
                    readSize = sizeInBytes - count;
                }
            }
        } catch (IOException e) {
            LogUtil.e(TAG, e);
        }
        return count;
    }

    /**
     * 从指定目录清除最久未使用的文件，被清除的文件大小总和需要大于length
     *
     * @param dirPath 目录路径
     * @param length  要清除掉的文件大小总和
     * @return 被清除的文件数
     */
    public static int removeOldFiles(String dirPath, long length) {
        if (TextUtils.isEmpty(dirPath) || length <= 0)
            return 0;
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory())
            return 0;
        File[] files = getFilesByLastModified(dir);
        long l = 0;
        int count = 0;
        for (File file : files) {
            l = file.length();
            if (file.delete()) {
                count++;
                length -= l;
                if (length <= 0)
                    break;
            }
        }
        return count;
    }

    /**
     * 按照最后修改时间排序，获取目录dir下文件列表
     *
     * @param dir
     * @return
     */
    public static File[] getFilesByLastModified(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return null;
        File[] files = dir.listFiles();

        try {
            //jdk7 此算法有修改，有可能报错，设置属性，使其使用旧版本排序
            System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File lhs, File rhs) {
                    return (int) (lhs.lastModified() - rhs.lastModified());
                }
            });
        } catch (Exception e) {
            LogUtil.e(TAG, e);
        }

        return files;
    }

    /**
     * SDCard是否可用
     */
    public static boolean isSDCardAvailable() {
        String status = Environment.getExternalStorageState();
        return status.equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 清除目录下的所以文件
     *
     * @param dirPath 目录路径
     * @return 是否清除成功
     */
    public static boolean clearDir(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory())
            return false;
        for (File file : dir.listFiles()) {
            if (!file.exists())
                continue;
            if (file.isFile())
                file.delete();
            if (file.isDirectory()) {
                clearDir(file.getAbsolutePath());
            }
        }
        File[] files = dir.listFiles();
        return files == null || files.length == 0;
    }

    /**
     * 检查文件是否存在，不存在时创建相应文件及所在目录
     *
     * @param file
     * @return
     * @throws java.io.IOException
     */
    public static boolean checkFile(File file) throws IOException {
        if (file == null) {
            return false;
        }
        if (file.isFile() && file.exists()) {
            return true;
        }
        if (file.exists() && !file.isFile()) {
            file.delete();
        }
        if (file.getParentFile() == null) {
            return false;
        }
        file.getParentFile().mkdirs();
        return file.createNewFile();
    }

    /**
     * 过滤文件名，保证过滤后的文件名为合法文件名<br/>
     * 非法字符将被替换成下划线_
     *
     * @param filename 需要过滤的文件名(不包括父目录路径)
     * @return 过滤后合法的文件名
     */
    public static String filterFileName(String filename) {
        if (TextUtils.isEmpty(filename)) {
            return filename;
        }
        filename = filename.replace(' ', '_');
        filename = filename.replace('"', '_');
        filename = filename.replace('\'', '_');
        filename = filename.replace('\\', '_');
        filename = filename.replace('/', '_');
        filename = filename.replace('<', '_');
        filename = filename.replace('>', '_');
        filename = filename.replace('|', '_');
        filename = filename.replace('?', '_');
        filename = filename.replace(':', '_');
        filename = filename.replace(',', '_');
        filename = filename.replace('*', '_');
        return filename;
    }

    /**
     * 获取SD卡可用大小
     *
     * @return
     */
    @SuppressWarnings("deprecation")
    public static long getSDCardAvailableSpace() {
        File file = Environment.getExternalStorageDirectory(); // 取得sdcard文件路径
        StatFs stat = new StatFs(file.getPath());

        long blockSize = stat.getBlockSize();

        long availableBlocks = stat.getAvailableBlocks();

        return availableBlocks * blockSize;
    }

    /**
     * 检查SD卡可用空间是否满足需要
     *
     * @return
     */
    public static boolean checkSDCardHasEnoughSpace(long size) {
        return getSDCardAvailableSpace() > size;
    }

    /**
     * 获取文件的字节数组
     *
     * @param filePath 文件路径
     * @return
     */
    public static byte[] getBytesFromFile(String filePath) {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(new File(filePath), "rw");
            byte[] buffer = new byte[(int) file.length()];
            file.read(buffer);

            return buffer;
        } catch (Exception e) {
            LogUtil.e(TAG, "read file error!");
        } finally {
            if (file != null)
                try {
                    file.close();
                } catch (Exception e2) {
                    LogUtil.e(TAG, e2);
                }
        }

        return null;
    }

    /**
     * check if file exists by creating a File type with path, file name can be included in the path
     *
     * @param path the file path
     * @return whether the path exists
     */
    public static boolean fileExists(String path) {
        File file = new File(path);
        return file.exists();
//        if (file.exists()) {
//            return true;
//        } else {
//            return false;
//        }
    }

    /**
     * create a new folder : createDirectoryAtPath
     *
     * @param path the folder path
     * @return if create success, else return false
     */
    public static boolean createDirectoryAtPath(String path) {
        boolean result = true;
        if (!isFilePathExist(path)) {
            File file = new File(path);
            result = file.mkdirs();
        }
        return result;
    }

    // 复制文件夹
    public static void copyDirectiory(String sourceDir, String targetDir) throws IOException {
        // 新建目标目录
        (new File(targetDir)).mkdirs();
        // 获取源文件夹当前下的文件或目录
        File[] file = (new File(sourceDir)).listFiles();
        for (int i = 0; i < file.length; i++) {
            if (file[i].isFile()) {
                // 源文件
                File sourceFile = file[i];
                // 目标文件
                File targetFile = new File(new File(targetDir).getAbsolutePath() + File.separator + file[i].getName());
                copyFile(sourceFile, targetFile);
            }
            if (file[i].isDirectory()) {
                // 准备复制的源文件夹
                String dir1 = sourceDir + "/" + file[i].getName();
                // 准备复制的目标文件夹
                String dir2 = targetDir + "/" + file[i].getName();
                copyDirectiory(dir1, dir2);
            }
        }
    }

    /**
     * 给定根目录，返回一个相对路径所对应的实际文件名.
     *
     * @param baseDir     指定根目录
     * @param absFileName 相对路径名，来自于ZipEntry中的name
     * @return java.io.File 实际的文件
     */
    public static File getRealFileName(String baseDir, String absFileName) {
        String[] dirs = absFileName.split("/");
        File ret = new File(baseDir);

        if (!ret.exists())
            ret.mkdirs();

        String substr = null;
        if (dirs.length > 1) {
            for (int i = 0; i < dirs.length - 1; i++) {
                substr = dirs[i];
                ret = new File(ret.getAbsolutePath(), substr);
            }
            if (!ret.exists())
                ret.mkdirs();
            substr = dirs[dirs.length - 1];
            ret = new File(ret, substr);
            return ret;
        } else if (dirs.length == 1) {
            ret = new File(ret, absFileName);
        }
        return ret;
    }

    public static boolean createFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            return true;
        } else {
            try {
                return file.createNewFile();
            } catch (IOException e) {
                LogUtil.e(TAG, e);
            }
        }

        return false;
    }

    //将json保存到文件中
    public synchronized static void saveJson(JSONObject jsonObject, String path) {
        if (jsonObject == null)
            return;
        if (path == null || path.equalsIgnoreCase(""))
            return;

        String content = jsonObject.toString();
        BufferedOutputStream os = null;
        try {
            if (!FileUtils.fileExists(path))
                FileUtils.createFile(path);

            os = new BufferedOutputStream(new DataOutputStream(new FileOutputStream(new File(path))));
            byte[] bytes = content.getBytes();
            os.write(bytes, 0, bytes.length);
        } catch (Exception e) {
            LogUtil.v(TAG, "path:" + path);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e2) {
                    LogUtil.e(TAG, e2);
                }
            }
        }
    }

    //将json保存到文件中
    public synchronized static void saveString(String content, String path) {
        if (content == null)
            return;
        if (path == null || path.equalsIgnoreCase(""))
            return;

        BufferedOutputStream os = null;
        try {
            if (!FileUtils.fileExists(path))
                FileUtils.createFile(path);

            os = new BufferedOutputStream(new DataOutputStream(new FileOutputStream(new File(path))));
            byte[] bytes = content.getBytes();
            os.write(bytes, 0, bytes.length);
        } catch (Exception e) {
            LogUtil.v(TAG, "path:" + path);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e2) {
                    LogUtil.e(TAG, e2);
                }
            }
        }
    }

    /**
     * 读取流
     *
     * @param inStream
     * @return 字节数组
     * @throws Exception
     */
    public static byte[] readStream(InputStream inStream) throws IOException {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUF_SIZE];
        int len;
        while ((len = inStream.read(buffer)) != -1) {
            outSteam.write(buffer, 0, len);
        }
        outSteam.close();
        inStream.close();
        return outSteam.toByteArray();
    }

    /**
     * 读取流
     *
     * @param inStream
     * @return 字节数组
     * @throws Exception
     */
    public static String readStreamToString(InputStream inStream) throws IOException {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUF_SIZE];
        int len;
        while ((len = inStream.read(buffer)) != -1) {
            outSteam.write(buffer, 0, len);
        }
        outSteam.close();
        inStream.close();
        return outSteam.toString();
    }

    public static boolean isFilePathExist(String pathString) { // path is exists or not
        if (StringUtils.isEmpty(pathString)) {
            return false;
        }
        return new File(pathString).exists();
    }

    /**
     * 文件是否存在并且是文件 非文件夹
     *
     * @param path
     * @return
     */
    public static boolean isFileAndExist(String path) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        if (isFile(file)) {
            return true;
        }
        return false;
    }


    /**
     * 安全删除文件.
     *
     * @param file
     * @return
     */
    public static void deleteAllFilesSafely(File file) {
        if (file.isFile()) {
            deleteFileSafely(file);
            return;
        }
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                deleteFileSafely(file);
                return;
            }
            for (int i = 0; i < childFiles.length; i++) {
                deleteAllFilesSafely(childFiles[i]);
            }
            deleteFileSafely(file);
        }
    }

    private static boolean deleteFileSafely(File file) {
        if (file != null) {
            String tmpPath = file.getParent() + File.separator + System.currentTimeMillis();
            File tmp = new File(tmpPath);
            file.renameTo(tmp);
            return tmp.delete();
        }
        return false;
    }

    public static boolean copyAssetsFile2Phone(Activity activity, String fileName, String targetPath) {
        try {
            InputStream inputStream = activity.getAssets().open(fileName);
            //getFilesDir() 获得当前APP的安装路径 /data/data/包名/files 目录
            File file = new File(targetPath);
            if (!file.exists() || file.length() == 0) {
                FileOutputStream fos = new FileOutputStream(file);
                int len = -1;
                byte[] buffer = new byte[1024];
                while ((len = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                fos.flush();//刷新缓存区
                inputStream.close();
                fos.close();
            } else {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
