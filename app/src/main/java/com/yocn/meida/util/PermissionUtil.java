package com.yocn.meida.util;

import android.app.Activity;

//import pub.devrel.easypermissions.EasyPermissions;

/**
 * @Author yocn
 * @Date 2019/8/2 5:54 PM
 * @ClassName PermissionUtil
 */
public class PermissionUtil {
    public static boolean checkPermission(Activity context, String[] perms) {
//        return EasyPermissions.hasPermissions(context, perms);
        return true;
    }

    public static void requestPermission(Activity context, String tip, int requestCode, String[] perms) {
//        EasyPermissions.requestPermissions(context, tip, requestCode, perms);
    }
}
