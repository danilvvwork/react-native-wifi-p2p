package io.wifi.p2p;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static io.wifi.p2p.FileTransferService.copyFile;

/**
 * Created by kiryl on 18.7.18.
 * A simple server socket that accepts connection and writes some data on
 * the stream.
 */
public class FileServerAsyncTask extends AsyncTask<Void, Void, String> {
    private Context context;
    /**
     * @param context
     */
    public FileServerAsyncTask(Context context) {
        this.context = context;
    }
    @Override
    protected String doInBackground(Void... params) {
        try {
            ServerSocket serverSocket = new ServerSocket(8988);
            System.out.println("Server: Socket opened");
            Socket client = serverSocket.accept();
            System.out.println("Server: connection done");
            String downloadPath = "Download" + "/wifip2pboxroom-" + System.currentTimeMillis()
                    + ".jpg";
            final File f = new File(Environment.getExternalStorageDirectory() + "/"
                    + downloadPath);
            File dirs = new File(f.getParent());
            if (!dirs.exists())
                dirs.mkdirs();
            f.createNewFile();
            System.out.println("server: copying files " + f.toString());
            InputStream inputstream = client.getInputStream();
            copyFile(inputstream, new FileOutputStream(f));
            serverSocket.close();
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(context);
            }
            builder.setTitle("Download Success")
                    .setMessage("File downloaded to " + downloadPath + ".")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

            return f.getAbsolutePath();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            System.out.println("File copied - " + result);
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + result), "image/*");
            context.startActivity(intent);
        }
    }
    /*
     * (non-Javadoc)
     * @see android.os.AsyncTask#onPreExecute()
     */
    @Override
    protected void onPreExecute() {
        System.out.println("Opening a server socket");
    }
}
