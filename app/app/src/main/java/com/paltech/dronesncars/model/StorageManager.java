package com.paltech.dronesncars.model;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class StorageManager {

    private ContentResolver content_resolver;

    @Inject
    public StorageManager(@ApplicationContext Context context) {
        this.content_resolver = context.getContentResolver();
    }



    public void save_string_to_file(String file_name, String content) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Uri downloads = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            String relative_location = "Download/DronesNCars";

            ContentValues content_details = new ContentValues();
            content_details.put(MediaStore.Downloads.DISPLAY_NAME, file_name);
            content_details.put(MediaStore.Downloads.RELATIVE_PATH, relative_location);
            content_details.put(MediaStore.Downloads.IS_PENDING, 1);

            Uri content_uri = content_resolver.insert(downloads, content_details);
            if (content_uri != null) {
                copy_file_data(content_uri, content);

                content_details.clear();
                content_details.put(MediaStore.Downloads.IS_PENDING, 0);
                content_resolver.update(content_uri, content_details, null, null);
            }
        }
    }

    private void copy_file_data(Uri destination_content_uri, String string_to_write) {
        ParcelFileDescriptor file_descriptor;
        try {
            file_descriptor = content_resolver.openFileDescriptor(destination_content_uri, "w");
            new ParcelFileDescriptor.AutoCloseOutputStream(file_descriptor).write(string_to_write.getBytes());
        } catch (FileNotFoundException file_not_found_exception) {
            file_not_found_exception.printStackTrace();
            Log.d("DEV_ERROR", "FileNotFoundException while receiving ParcelFileDescriptor");
        } catch (IOException io_exception) {
            io_exception.printStackTrace();
            Log.d("DEV_ERROR", "IOException while trying to write the string to the file");
        }

    }
}
