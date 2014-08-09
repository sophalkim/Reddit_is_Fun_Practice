package ssk.project.Practice.reddit.comments;

import java.util.regex.Pattern;

import android.app.ListActivity;
import android.view.View;

import com.andrewshu.android.reddit.common.Constants;

public class CommentsListActivity extends ListActivity 
		implements View.OnCreateContextMenuListener {

	private static final String TAG = "CommentsListActivity";
	
	private final Pattern COMMENT_PATH_PATTERN = Pattern.compile(Constants.COMMENT_PATH_PATTERN_STRING);
	private final Pattern COMMENT_CONTEXT_PATTERN = Pattern.compile("context=(\\d+)");
	
}
