package com.xsl.servicebestpractice;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * User:xsl
 * Date:2017/7/21
 * Time:10:19
 */

public class DownLoadTask extends AsyncTask<String, Integer, Integer> {

    private static final int TYPE_SUCCESS = 0;
    private static final int TYPE_FAILED = 1;
    private static final int TYPE_PAUSED = 2;
    private static final int TYPE_CANCELED = 3;

    private DownLoadListener downLoadListener;
    private boolean isCanceled = false;
    private boolean isPaused = false;
    private int lastProgress;

    public DownLoadTask(DownLoadListener loadListener) {
        this.downLoadListener = loadListener;
    }

    @Override
    protected Integer doInBackground(String... parms) {
        InputStream is = null;
        RandomAccessFile saveFile = null;
        File file = null;
        try {
            long downLoadLength = 0;
            String downLoadUrl = parms[0];
            String fileName = downLoadUrl.substring(downLoadUrl.lastIndexOf("/"));
            String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            file = new File(directory + fileName);
            if (file.exists()) {
                downLoadLength = file.length();
            }

            long contentLength = getContentLength(downLoadUrl);
            if (contentLength == 0) {
                return TYPE_FAILED;
            } else if (contentLength == downLoadLength) {
                return TYPE_SUCCESS;
            }
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .addHeader("RANGE", "bytes=" + downLoadLength + "-")
                    .url(downLoadUrl)
                    .build();

            Response response = client.newCall(request).execute();
            if (response != null) {
                is = response.body().byteStream();
                saveFile = new RandomAccessFile(file, "rw");
                saveFile.seek(downLoadLength);
                byte[] b = new byte[1024];
                int total = 0;
                int len;
                while ((len = is.read(b)) != -1) {
                    if (isCanceled) {
                        return TYPE_CANCELED;
                    } else if (isPaused) {
                        return TYPE_PAUSED;
                    } else {
                        total += len;
                        saveFile.write(b, 0, len);
                        int progress = (int) ((total + downLoadLength) * 100 / contentLength);
                        publishProgress(progress);
                    }
                }
                response.body().close();
                return TYPE_SUCCESS;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (saveFile != null) {
                    saveFile.close();
                }
                if (isCanceled && file != null) {
                    file.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return TYPE_FAILED;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress > lastProgress) {
            downLoadListener.onProgress(progress);
            lastProgress = progress;
        }
    }

    @Override
    protected void onPostExecute(Integer states) {
        switch (states) {
            case TYPE_SUCCESS:
                downLoadListener.onSuccess();
                break;
            case TYPE_FAILED:
                downLoadListener.onFailed();
                break;
            case TYPE_PAUSED:
                downLoadListener.onPaused();
                break;
            case TYPE_CANCELED:
                downLoadListener.onCanceled();
            default:
                break;
        }
    }

    public void pauseDownLoad() {
        isPaused = true;
    }

    public void cancelDownLoad() {
        isCanceled = true;
    }

    private long getContentLength(String downLoadUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(downLoadUrl)
                .build();
        Response response = client.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            long contentLength = response.body().contentLength();
            response.close();
            return contentLength;
        }
        return 0;
    }


}
