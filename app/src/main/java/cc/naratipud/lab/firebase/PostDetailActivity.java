package cc.naratipud.lab.firebase;

import android.os.Bundle;

public class PostDetailActivity extends BaseActivity {

    private static final String TAG = PostDetailActivity.class.getSimpleName();
    public static final String EXTRA_POST_KEY = "post_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
    }

}
