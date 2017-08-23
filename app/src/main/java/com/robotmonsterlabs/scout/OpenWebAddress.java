package com.robotmonsterlabs.scout;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by joduplessis on 2015/07/14.
 */
public class OpenWebAddress {
    public void OpenWebAddress() {
    }
    public void open(Context context, String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(browserIntent);
    }
}
