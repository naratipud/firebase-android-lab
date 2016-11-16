package cc.naratipud.lab.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;

public class PostActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPager pager = (ViewPager) findViewById(R.id.container);
        pager.setAdapter(new PostPagerAdapter(getSupportFragmentManager()));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(pager);

        // Button launches NewPostActivity
        FloatingActionButton newPostButton = (FloatingActionButton) findViewById(R.id.fab_new_post);
        newPostButton.setOnClickListener(v -> startActivity(new Intent(PostActivity.this, NewPostActivity.class)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private class PostPagerAdapter extends FragmentPagerAdapter {

        private final Fragment[] mFragments = new Fragment[]{
                new RecentPostsFragment(),
                new MyPostsFragment(),
                new MyTopPostsFragment(),
        };
        private final String[] mFragmentNames = new String[]{
                getString(R.string.tab_title_recent),
                getString(R.string.tab_title_my_posts),
                getString(R.string.tab_title_my_top_posts)
        };

        PostPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments[position];
        }

        @Override
        public int getCount() {
            return mFragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentNames[position];
        }
    }

}
