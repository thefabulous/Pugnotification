package br.com.goncalves.pugnotification.notification;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.text.Spanned;
import android.view.View;
import android.widget.RemoteViews;

import br.com.goncalves.pugnotification.R;
import br.com.goncalves.pugnotification.interfaces.ImageLoader;
import br.com.goncalves.pugnotification.interfaces.OnImageLoadingCompleted;
import br.com.goncalves.pugnotification.utils.ResourceUtils;

@TargetApi(android.os.Build.VERSION_CODES.JELLY_BEAN)
public class Custom extends Builder implements OnImageLoadingCompleted {

    private RemoteViews remoteViews;

    private String title;
    private String message;
    private Spanned messageSpanned;
    private String uri;
    private @DrawableRes int smallIconId;
    private @DrawableRes int backgroundResId;
    private @DrawableRes int placeHolderResourceId;

    private final boolean useSpanForCustomNotification;

    private ImageLoader imageLoader;

    public Custom(NotificationCompat.Builder builder,
                  int identifier,
                  String title,
                  String message,
                  Spanned messageSpanned,
                  @DrawableRes int smallIconId,
                  String tag,
                  boolean useSpanForCustomNotification
    ) {
        super(builder, identifier, tag);
        this.useSpanForCustomNotification = useSpanForCustomNotification;
        this.remoteViews = new RemoteViews(PugNotification.singleton.context.getPackageName(), R.layout.pugnotification_custom);
        this.title = title;
        this.message = message;
        this.messageSpanned = messageSpanned;
        this.smallIconId = smallIconId;
        this.placeHolderResourceId = R.drawable.pugnotification_ic_placeholder;
        this.init();
    }

    private void init() {
        this.setTitle();
        this.setMessage();
        this.setSmallIcon();
    }

    private void setTitle() {
        remoteViews.setTextViewText(R.id.notification_text_title, title);
    }

    private void setMessage() {
        if (messageSpanned != null) {
            CharSequence targetMessage = useSpanForCustomNotification ? this.messageSpanned : this.messageSpanned.toString().trim();
            remoteViews.setTextViewText(R.id.notification_text_message, targetMessage);
        } else {
            remoteViews.setTextViewText(R.id.notification_text_message, message);
        }
    }

    private void setSmallIcon() {
        if (ResourceUtils.isValid(smallIconId)) {
            remoteViews.setImageViewResource(R.id.notification_img_icon, smallIconId);
        } else {
            remoteViews.setImageViewResource(R.id.notification_img_icon, R.drawable.pugnotification_ic_launcher);
        }
    }

    public Custom textBackground(@ColorInt int color, boolean castShadow) {
        remoteViews.setInt(R.id.notification_content_information, "setBackgroundColor", color);

        if (castShadow) {
            this.remoteViews.setViewVisibility(R.id.notification_img_bottom_shadow, View.VISIBLE);
            this.remoteViews.setImageViewBitmap(R.id.notification_img_bottom_shadow, createShadowBitmap(color));
        } else {
            this.remoteViews.setViewVisibility(R.id.notification_img_bottom_shadow, View.GONE);
        }

        return this;
    }

    public Custom background(@DrawableRes int resource) {
        ResourceUtils.assertResouceValid(resource);

        if (uri != null) {
            throwBackgroundAlreadySetException();
        }

        this.backgroundResId = resource;
        return this;
    }

    private void throwBackgroundAlreadySetException() {
        throw new IllegalStateException("Background Already Set!");
    }

    public Custom setPlaceholder(@DrawableRes int resource) {
        ResourceUtils.assertResouceValid(resource);

        this.placeHolderResourceId = resource;
        return this;
    }

    public Custom setImageLoader(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
        return this;
    }

    public Custom background(String uri) {

        if (ResourceUtils.isValid(backgroundResId)) {
            throwBackgroundAlreadySetException();
        }

        if (this.uri != null) {
            throwBackgroundAlreadySetException();
        }

        if (uri == null) {
            throw new IllegalArgumentException("Path Must Not Be Null!");
        }
        if (uri.trim().length() == 0) {
            throw new IllegalArgumentException("Path Must Not Be Empty!");
        }

        if (imageLoader == null) {
            throw new IllegalStateException("You have to set an ImageLoader!");
        }

        this.uri = uri;
        return this;
    }

    @Override
    public void build() {
        if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
            throw new IllegalStateException("Method call should happen from the main thread.");
        }

        super.build();
        setBigContentView(remoteViews);
        loadImageBackground();
    }

    private void loadImageBackground() {
        remoteViews.setImageViewResource(R.id.notification_img_background, placeHolderResourceId);
        if (uri != null) {
            imageLoader.load(uri, this);
        } else {
            imageLoader.load(backgroundResId, this);
        }
    }

    @Override
    public void imageLoadingCompleted(Bitmap bitmap) {
        if (bitmap == null) {
            throw new IllegalArgumentException("bitmap cannot be null");
        }

        remoteViews.setImageViewBitmap(R.id.notification_img_background, bitmap);
        super.notificationNotify();
    }

    @NonNull
    private Bitmap createShadowBitmap(@ColorInt int color) {
        int width = 1;      //width of the bitmap doesn't matter as it will be stretched by ImageView
        int height = ((int) getResources().getDimension(R.dimen.pugnotification_shadow_height));

        Shader shader = new LinearGradient(0, 0, 0, height, Color.TRANSPARENT, color, Shader.TileMode.CLAMP);

        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        Paint paint = new Paint();
        paint.setShader(shader);

        c.drawRect(0, 0, width, height, paint);
        return b;
    }

    private Resources getResources() {
        return PugNotification.singleton.context.getResources();
    }
}
