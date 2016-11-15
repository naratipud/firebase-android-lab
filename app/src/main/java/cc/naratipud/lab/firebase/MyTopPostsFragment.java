package cc.naratipud.lab.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class MyTopPostsFragment extends PostListFragment {

    public MyTopPostsFragment() {
        // Required empty public constructor
    }

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // My top posts by number of stars
        return databaseReference.child("user-posts").child(getUid()).orderByChild("starCount");
    }

}
