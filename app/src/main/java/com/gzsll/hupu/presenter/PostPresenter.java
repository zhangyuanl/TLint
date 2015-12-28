package com.gzsll.hupu.presenter;

import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.mobileconnectors.s3.transfermanager.Upload;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.gzsll.hupu.Constants;
import com.gzsll.hupu.api.thread.ThreadApi;
import com.gzsll.hupu.support.storage.UserStorage;
import com.gzsll.hupu.support.storage.bean.AddReplyResult;
import com.gzsll.hupu.support.storage.bean.BaseResult;
import com.gzsll.hupu.support.storage.bean.UploadInfo;
import com.gzsll.hupu.support.utils.ConfigHelper;
import com.gzsll.hupu.support.utils.FileHelper;
import com.gzsll.hupu.support.utils.SecurityHelper;
import com.gzsll.hupu.view.PostView;

import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by sll on 2015/3/10.
 */
public class PostPresenter extends Presenter<PostView> {

    Logger logger = Logger.getLogger("PostPresenterImpl");


    @Inject
    UserStorage mUserStorage;
    @Inject
    ThreadApi mThreadApi;


    private ArrayList<String> paths = new ArrayList<>();


    int uploadCount = 0;

    /**
     * 评论或者引用
     *
     * @param content 内容
     */
    public void comment(final String tid, final String fid, final String pid, final String content) {
        view.showLoading();
        if (paths != null && !paths.isEmpty()) {
            uploadCount = 0;
            final List<String> images = new ArrayList<>();
            for (int i = 0; i < paths.size(); i++) {
                final UploadInfo uploadInfo = new UploadInfo();
                uploadInfo.position = i;
                uploadInfo.uploadPath = paths.get(i);
                uploadFile(uploadInfo, new ProgressListener() {
                    @Override
                    public void progressChanged(ProgressEvent progressEvent) {
                        if (progressEvent.getEventCode() == ProgressEvent.COMPLETED_EVENT_CODE || progressEvent.getEventCode() == ProgressEvent.FAILED_EVENT_CODE) {
                            uploadCount++;
                        }
                        if (progressEvent.getEventCode() == ProgressEvent.COMPLETED_EVENT_CODE) {
                            new File(uploadInfo.uploadPath).delete();
                            images.add(uploadInfo.url);
                        }
                        if (uploadCount == paths.size()) {
                            addReply(tid, fid, pid, content, images);
                        }
                    }
                });
            }

        } else {
            addReply(tid, fid, pid, content, null);
        }


    }

    private void addReply(String tid, String fid, String pid, String content, List<String> imgs) {
        StringBuffer buffer = new StringBuffer(content);
        if (imgs != null) {
            for (String url : imgs) {
                buffer.append("<br><br><img src=\"" + url + "\"><br><br>");
            }
        }
        mThreadApi.addReplyByApp(tid, fid, pid, buffer.toString(), new Callback<AddReplyResult>() {
            @Override
            public void success(AddReplyResult result, Response response) {
                view.hideLoading();
                if (result != null) {
                    view.showToast(result.getMsg());
                    if (result.getStatus() == 200) {
                        view.postSuccess();
                    }
                } else {
                    view.showToast("您的网络有问题，请检查后重试");
                }
            }

            @Override
            public void failure(RetrofitError error) {
                view.hideLoading();
                view.showToast("您的网络有问题，请检查后重试");
            }
        });
    }


    public void post(final String fid, final String content, final String title) {
        view.showLoading();
        if (paths != null && !paths.isEmpty()) {
            uploadCount = 0;
            final List<String> images = new ArrayList<>();
            for (int i = 0; i < paths.size(); i++) {
                final UploadInfo uploadInfo = new UploadInfo();
                uploadInfo.position = i;
                uploadInfo.uploadPath = paths.get(i);
                uploadFile(uploadInfo, new ProgressListener() {
                    @Override
                    public void progressChanged(ProgressEvent progressEvent) {
                        if (progressEvent.getEventCode() == ProgressEvent.COMPLETED_EVENT_CODE || progressEvent.getEventCode() == ProgressEvent.FAILED_EVENT_CODE) {
                            uploadCount++;
                        }
                        if (progressEvent.getEventCode() == ProgressEvent.COMPLETED_EVENT_CODE) {
                            new File(uploadInfo.uploadPath).delete();
                            images.add(uploadInfo.url);
                        }
                        if (uploadCount == paths.size()) {
                            addPost(fid, content, title, images);
                        }
                    }
                });
            }

        } else {
            addPost(fid, content, title, null);
        }
    }

    private void addPost(String fid, String content, String title, List<String> imgs) {
        StringBuffer buffer = new StringBuffer(content);
        if (imgs != null) {
            for (String url : imgs) {
                buffer.append("<br><br><img src=\"" + url + "\"><br><br>");
            }
        }
        mThreadApi.addGroupThread(title, buffer.toString(), fid, new Callback<BaseResult>() {
            @Override
            public void success(BaseResult result, Response response) {
                view.hideLoading();
                if (result != null) {
                    view.showToast(result.getMsg());
                    if (result.getStatus() == 200) {
                        view.postSuccess();
                    }
                } else {
                    view.showToast("您的网络有问题，请检查后重试");
                }
            }

            @Override
            public void failure(RetrofitError error) {
                view.hideLoading();
                view.showToast("您的网络有问题，请检查后重试");
            }
        });
    }


    @Inject
    TransferManager mTransferManager;
    @Inject
    SecurityHelper mSecurityHelper;
    @Inject
    FileHelper mFileHelper;
    @Inject
    ConfigHelper mConfigHelper;


    private List<PutObjectRequest> requests = new ArrayList<>();

    public Upload uploadFile(UploadInfo uploadInfo, ProgressListener progressListener) {
        File file = new File(uploadInfo.uploadPath);
        StringBuilder builder = new StringBuilder();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(uploadInfo.uploadPath, options);
        int width = options.outWidth;
        int height = options.outHeight;

        String uid = mUserStorage.getUid();
        if (!TextUtils.isEmpty(uid)) {
            builder.append(uid);
            builder.append("_");
        }
        builder.append("byte");
        builder.append(mFileHelper.length(file.length()));
        builder.append("_");
        builder.append(mSecurityHelper.getMd5ByteByFile(file));
        builder.append("_hupu_android_w");
        builder.append(width);
        builder.append("h");
        builder.append(height);
        if (uploadInfo.uploadPath.endsWith(".gif")) {
            builder.append(".gif");
        } else {
            builder.append(".png");
        }
        File uploadFile = new File(mConfigHelper.getUploadPath() + builder.toString());
        mFileHelper.copy(file, uploadFile);
        PutObjectRequest withGeneralProgressListener = new PutObjectRequest(Constants.BOX_BUCKET_NAME, uploadFile.getName(), uploadFile).withCannedAcl(CannedAccessControlList.PublicRead).withGeneralProgressListener(progressListener);
        uploadInfo.url = Constants.BOX_END_POINT_NEW + uploadFile.getName();
        Upload upload = mTransferManager.upload(withGeneralProgressListener);
        requests.add(withGeneralProgressListener);
        return upload;
    }


    public void parse(ArrayList<String> paths) {
        this.paths = paths;
    }


    @Override
    public void initialize() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {
        paths.clear();
    }
}
