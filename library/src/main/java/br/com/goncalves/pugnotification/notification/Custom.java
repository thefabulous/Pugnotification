package br.com.goncalves.pugnotification.notification;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Looper;
import android.support.annotation.DrawableRes;
import android.support.v4.app.NotificationCompat;
import android.text.Spanned;
import android.widget.RemoteViews;

import br.com.goncalves.pugnotification.R;
import br.com.goncalves.pugnotification.interfaces.ImageLoader;
import br.com.goncalves.pugnotification.interfaces.OnImageLoadingCompleted;

@TargetApi(android.os.Build.VERSION_CODES.JELLY_BEAN)
public class Custom extends Builder implements OnImageLoadingCompleted {

    private RemoteViews mRemoteView;
    private String mTitle;
    private String mMessage;
    private Spanned mMessageSpanned;
    private String mUri;
    private int mSmallIcon;
    private int mBackgroundResId;
    private int mPlaceHolderResourceId;
    private ImageLoader mImageLoader;
    private final boolean mUseSpanForCustomNotification;

    public Custom(NotificationCompat.Builder builder,
                  int identifier,
                  String title,
                  String message,
                  Spanned messageSpanned,
                  int smallIcon,
                  String tag,
                  boolean useSpanForCustomNotification
    ) {
        super(builder, identifier, tag);
        this.mUseSpanForCustomNotification = useSpanForCustomNotification;
        this.mRemoteView = new RemoteViews(PugNotification.singleton.context.getPackageName(), R.layout.pugnotification_custom);
        this.mTitle = title;
        this.mMessage = message;
        this.mMessageSpanned = messageSpanned;
        this.mSmallIcon = smallIcon;
        this.mPlaceHolderResourceId = R.drawable.pugnotification_ic_placeholder;
        this.init();
    }

    private void init() {
        this.setTitle();
        this.setMessage();
        this.setSmallIcon();
    }

    private void setTitle() {
        mRemoteView.setTextViewText(R.id.notification_text_title, mTitle);
    }

    private void setMessage() {
        if (mMessageSpanned != null) {
            CharSequence message = mUseSpanForCustomNotification ? this.mMessageSpanned : this.mMessageSpanned.toString();
            mRemoteView.setTextViewText(R.id.notification_text_message, message);
        } else {
            mRemoteView.setTextViewText(R.id.notification_text_message, mMessage);
        }
    }

    private void setSmallIcon() {
        if (mSmallIcon <= 0) {
            mRemoteView.setImageViewResource(R.id.notification_img_icon, R.drawable.pugnotification_ic_launcher);
        }
        mRemoteView.setImageViewResource(R.id.notification_img_icon, mSmallIcon);
    }

    public Custom background(@DrawableRes int resource) {
        if (resource == 0) {
            throw new IllegalArgumentException("Resource ID Should Not Be Zero!");
        }

        if (mUri != null) {
            throwBackgroundAlreadySetException();
        }

        this.mBackgroundResId = resource;
        return this;
    }

    private void throwBackgroundAlreadySetException() {
        throw new IllegalStateException("Background Already Set!");
    }

    public Custom setPlaceholder(@DrawableRes int resource) {
        if (resource == 0) {
            throw new IllegalArgumentException("Resource ID Should Not Be Zero!");
        }

        this.mPlaceHolderResourceId = resource;
        return this;
    }

    public Custom setImageLoader(ImageLoader imageLoader) {
        this.mImageLoader = imageLoader;
        return this;
    }

    public Custom background(String uri) {
        if (mBackgroundResId > 0) {
            throwBackgroundAlreadySetException();
        }

        if (mUri != null) {
            throwBackgroundAlreadySetException();
        }

        if (uri == null) {
            throw new IllegalArgumentException("Path Must Not Be Null!");
        }
        if (uri.trim().length() == 0) {
            throw new IllegalArgumentException("Path Must Not Be Empty!");
        }

        if (mImageLoader == null) {
            throw new IllegalStateException("You have to set an ImageLoader!");
        }

        this.mUri = uri;
        return this;
    }

    @Override
    public void build() {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            throw new IllegalStateException("Method call should happen from the main thread.");
        }

        super.build();
        setBigContentView(mRemoteView);
        loadImageBackground();
    }

    private void loadImageBackground() {
        mRemoteView.setImageViewResource(R.id.notification_img_background, mPlaceHolderResourceId);
        if (mUri != null) {
            mImageLoader.load(mUri, this);
        } else {
            mImageLoader.load(mBackgroundResId, this);
        }
    }

    @Override
    public void imageLoadingCompleted(Bitmap bitmap) {
        if (bitmap == null) {
            throw new IllegalArgumentException("bitmap cannot be null");
        }
        mRemoteView.setImageViewBitmap(R.id.notification_img_background, bitmap);
        super.notificationNotify();
    }
}
