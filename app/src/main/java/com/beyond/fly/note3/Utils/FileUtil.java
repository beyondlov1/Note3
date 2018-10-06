/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.beyond.fly.note3.Utils;

import java.io.File;

import android.content.Context;

public class FileUtil {
    public static File getSaveFile(Context context) {
        File file = new File(context.getFilesDir(), "pic.jpg");
        return file;
    }
}
