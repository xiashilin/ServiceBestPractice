package com.xsl.servicebestpractice;

/**
 * Created by xsl on 2017/7/21.
 */

public interface DownLoadListener {
    void onProgress(int progress);

    void onSuccess();

    void onFailed();

    void onPaused();

    void onCanceled();
}
