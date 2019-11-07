package com.yocn.meida.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import com.yocn.meida.base.BaseApplication;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Scanner;

import androidx.annotation.NonNull;

import static android.os.Environment.MEDIA_MOUNTED;

/**
 * 获取手机上的所有存储设备，包括内置SD卡和外置SD卡的路径、label</br> 大部分中高端设备使用内置SD卡</br>
 * 通过android标准接口Environment
 * .getExternalStorageDirectory()获取的路径，有可能是机身内存，并非外置SD卡</br>
 *
 * @author danny
 */
public class StorageUtil {
    private final static String TAG = "StorageUtil";
    private static final String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";

    private static final String DEV_MOUNT = "dev_mount";
    private static final String SYSTEM_ETC_VOLD_FSTAB = "/system/etc/vold.fstab";
    private static final String DEV_BLOCK_VOLD = "/dev/block/vold/";
    private static final String PROC_MOUNTS = "/proc/mounts";

    /**
     * SDCARD的绝对路径。 need permission , android api level 23
     */
    @NonNull
    public static String getDefaultExternalStoragePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static String getExternalStorageFilesDir() {
        return BaseApplication.getAppContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
    }

    public static String getExternalFilesDir() {
        File file = BaseApplication.getAppContext().getFilesDir(); //内置
        if (file == null) {
            file = BaseApplication.getAppContext().getExternalFilesDir("");//外置
            if (file == null) {
                return "";
            }
        }
        return file.getAbsolutePath();
    }

    private static String sDefaultExternalStoragePathPrefix = null;

    public static String getDefaultExternalStoragePathPrefix() {
        try {
            if (sDefaultExternalStoragePathPrefix == null) {
                sDefaultExternalStoragePathPrefix = Environment.getExternalStorageDirectory().getAbsolutePath().substring(0, 8);
            }
            return sDefaultExternalStoragePathPrefix;
        } catch (Exception e) {
            LogUtil.e(TAG, e);
        }

        return "/storage";
    }

    private static boolean isSdcardWritable() {
        String status = Environment.getExternalStorageState();
        return status.equals(Environment.MEDIA_MOUNTED) || status.equals(Environment.MEDIA_CHECKING);
    }

    private static boolean isSdcardMounted() {
        String status = Environment.getExternalStorageState();
        return status.equals(Environment.MEDIA_MOUNTED) || status.equals(Environment.MEDIA_CHECKING)
                || status.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
    }

    /**
     * 是否mount外置存储
     *
     * @return
     */
    private static boolean isMount() {
        String status = Environment.getExternalStorageState();
        return status.equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 检查SD卡剩余空间
     *
     * @return
     */
    @SuppressWarnings("deprecation")
    public static long freeSpace() {
        return getSDAvailableSize();
    }

    /**
     * SD卡剩余空间的百分比
     *
     * @return
     */
    public static double freePercentage() {
        long mCapacity = 0;
        long mAvailCapacity = 0;
        try {
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            //FIXME: After API level 18, use getTotalBytes() and getAvailableBytes() instead
            long blockSize;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = stat.getBlockSizeLong();
                mCapacity = blockSize * stat.getBlockCountLong();
                mAvailCapacity = blockSize * stat.getAvailableBlocksLong();
                return (double) mAvailCapacity / (double) mCapacity * 100;
            } else {
                blockSize = stat.getBlockSize();
                mCapacity = blockSize * stat.getBlockCount();
                mAvailCapacity = blockSize * stat.getAvailableBlocks();
                return (double) mAvailCapacity / (double) mCapacity * 100;
            }
        } catch (Exception e) {
            LogUtil.e(TAG, e);
        }
        return 0;
    }

    private static class SizePair {
        long mCapacity = 0;
        long mAvailCapacity = 0;
    }

    /**
     * 获取手机内部剩余存储空间
     *
     * @return
     */
    public static long getAvailableInternalMemorySize() {
        SizePair sizePair = getFilePathSize(Environment.getDataDirectory());
        return sizePair.mAvailCapacity;
    }

    /**
     * 获取手机内部总的存储空间
     *
     * @return
     */
    public static long getTotalInternalMemorySize() {
        SizePair sizePair = getFilePathSize(Environment.getDataDirectory());
        return sizePair.mCapacity;
    }

    public static long getSDTotalSize() {
        SizePair sizePair = getFilePathSize(Environment.getExternalStorageDirectory());
        return sizePair.mCapacity;
    }

    public static long getSDAvailableSize() {
        SizePair sizePair = getFilePathSize(Environment.getExternalStorageDirectory());
        return sizePair.mAvailCapacity;
    }

    private static SizePair getFilePathSize(File file) {
        SizePair sizePair = new SizePair();
        long mCapacity;
        long mAvailCapacity;
        try {
            StatFs stat = new StatFs(file.getPath());
            //After API level 18, use getTotalBytes() and getAvailableBytes() instead by danny
            long blockSize;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = stat.getBlockSizeLong();
                mCapacity = blockSize * stat.getBlockCountLong();
                mAvailCapacity = blockSize * stat.getAvailableBlocksLong();
            } else {
                blockSize = stat.getBlockSize();
                mCapacity = blockSize * stat.getBlockCount();
                mAvailCapacity = blockSize * stat.getAvailableBlocks();
            }
            LogUtil.v(TAG, " Path: " + file.getPath() + "blockSize: " + blockSize + " mCapacity" + mCapacity + "  mAvailCapacity:" + mAvailCapacity);
            sizePair.mAvailCapacity = mAvailCapacity;
            sizePair.mCapacity = mCapacity;
        } catch (Exception e) {
            LogUtil.e(TAG, e);
        }
        return sizePair;
    }

    /**
     * 扫描/proc/mounts文件，查找/dev/block/vold并解析出SD卡路径 /dev/block/vold/179:1
     * /mnt/sdcard vfat
     * rw,dirsync,nosuid,nodev,noexec,relatime,uid=1000,gid=1015
     * ,fmask=0602,dmask
     * =0602,allow_utime=0020,codepage=cp437,iocharset=iso8859-1
     * ,shortname=mixed,utf8,errors=remount-ro 0 0
     */
    private static ArrayList<String> getPathsFromMount() {
        ArrayList<String> paths = new ArrayList<String>();
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(PROC_MOUNTS));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.startsWith(DEV_BLOCK_VOLD)) {
                    line = line.replace('\t', ' ');
                    String[] lineElements = line.split(" ");
                    if (lineElements.length >= 2) {
                        String element = lineElements[1];
                        paths.add(element);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }

        return paths;
    }

    /**
     * 扫描/system/etc/vold.fstab，找到dev_mount并解析，得到挂载设备 dev_mount sdcard
     * /mnt/sdcard 1 /devices/platform/s3c-sdhci.0/mmc_host/mmc0
     */
    private static ArrayList<String> getPathsFromVoldFile() {
        File file = new File(SYSTEM_ETC_VOLD_FSTAB);
        if (!file.exists()) {
            return null;
        }

        ArrayList<String> paths = new ArrayList<String>();
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.startsWith(DEV_MOUNT)) {
                    line = line.replace('\t', ' ');
                    String[] lineElements = line.split(" ");
                    String element = lineElements[2];

                    if (element.contains(":")) {
                        element = element.substring(0, element.indexOf(":"));
                    }
                    paths.add(element);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }

        return paths;
    }

    private static ArrayList<String> getSamePaths(ArrayList<String> mounts, ArrayList<String> volds) {
        ArrayList<String> paths = new ArrayList<String>();
        for (int i = 0; i < mounts.size(); i++) {
            String mount = mounts.get(i);
            if (volds.contains(mount)) {
                paths.add(mount);
            }
        }
        return paths;
    }

    public static File getCacheDirectory(Context context, boolean preferExternal) {
        File appCacheDir = null;
        String externalStorageState;
        try {
            externalStorageState = Environment.getExternalStorageState();
        } catch (NullPointerException e) { // (sh)it happens (Issue #660)
            externalStorageState = "";
        }

        if (preferExternal && MEDIA_MOUNTED.equals(externalStorageState)) {
            appCacheDir = getExternalCacheDir(context);
        }

        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }

        if (appCacheDir == null) {
            String cacheDirPath = "/data/data/" + context.getPackageName() + "/cache/";
            //LogUtils.w("Can't define system cache directory! '%s' will be used.", cacheDirPath);
            appCacheDir = new File(cacheDirPath);
        }
        return appCacheDir;
    }

    private static File getOwnCacheDirectory(Context context, String cacheDir) {
        File appCacheDir = null;
        if (MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && hasExternalStoragePermission(context)) {
            appCacheDir = new File(Environment.getExternalStorageDirectory(), cacheDir);
        }
        if (appCacheDir == null || (!appCacheDir.exists() && !appCacheDir.mkdirs())) {
            appCacheDir = context.getCacheDir();
        }
        return appCacheDir;
    }

    private static File getExternalCacheDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                LogUtil.w(TAG, "Unable to create external cache directory");
                return null;
            }
            try {
                new File(appCacheDir, ".nomedia").createNewFile();
            } catch (IOException e) {
                LogUtil.i(TAG, "Can't create .nomedia file in application external cache directory");
            }
        }
        return appCacheDir;
    }

    private static boolean hasExternalStoragePermission(Context context) {
        int perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION);
        return perm == PackageManager.PERMISSION_GRANTED;
    }

    public static ArrayList<String> getStoragePaths(Context context) {
        if (context == null)
            return null;

        ArrayList<String> paths = new ArrayList<String>();
        return getStoragePathsFromStorageVolume(context);
    }

    private static ArrayList<String> getStoragePathsFromStorageVolume(
            Context context) {
        Object manager = context.getSystemService(context.STORAGE_SERVICE);
        ArrayList<String> paths = new ArrayList<>();
        if (manager == null) {
            paths.add(getDefaultExternalStoragePath());
            return paths;
        }

        ArrayList<String> internalList = new ArrayList<>();
        ArrayList<String> externalList = new ArrayList<>();

        try {
            Class<?> clsStorageVolume = Class.forName("android.os.storage.StorageVolume");
            Method mthdGetVolumeList = manager.getClass().getMethod("getVolumeList");
            Method mthdGetVolumeState = manager.getClass().getMethod("getVolumeState", String.class);
            Method mthdIsRemovable = clsStorageVolume.getMethod("isRemovable");
            Method mthdGetPath = clsStorageVolume.getMethod("getPath");

            Object[] volumes = (Object[]) mthdGetVolumeList.invoke(manager);

            boolean removable = false;
            for (int i = 0; i < volumes.length; i++) {
                String path = (String) mthdGetPath.invoke(volumes[i]);
                removable = (Boolean) mthdIsRemovable.invoke(volumes[i]);

                if (path != null && !path.equals("")) {
                    String state = (String) mthdGetVolumeState.invoke(manager, path);
                    if (state != null && state.equals("mounted")) {
                        if (removable) {
                            externalList.add(path);
                        } else {
                            internalList.add(path);
                        }
                    }
                }
            }

            paths.addAll(internalList);
            paths.addAll(externalList);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return paths;
    }


}