package ssk.project.Practice.reddit.comments;

import java.util.LinkedList;

import android.os.AsyncTask;

import com.andrewshu.android.reddit.comments.CommentsListActivity;
import com.andrewshu.android.reddit.markdown.Markdown;
import com.andrewshu.android.reddit.things.ThingInfo;

public class ProcessCommentsTask extends AsyncTask<Void, Integer, Void>{

	private static final String TAG = "ProcessCommentsTask";
	
	private final CommentsListActivity mActivity;
	private final Markdown markdown = new Markdown();
	
	private final LinkedList<DeferredCommentProcessing> mDeferredProcessingList = new LinkedList<DeferredCommentProcessing>();
	private final LinkedList<DeferredCommentProcessing> mDeferredProcessingHighPriorityList = new LinkedList<DeferredCommentProcessing>();
	private final LinkedList<DeferredCommentProcessing> mDeferredProcessingLowPriorityList = new LinkedList<DeferredCommentProcessing>();
	
	public static class DeferredCommentProcessing {
		public int commentIndex;
		public ThingInfo comment;
		public DeferredCommentProcessing(ThingInfo comment, int commentIndex) {
			this.comment = comment;
			this.commentIndex = commentIndex;
		}
	}
	
	public ProcessCommentsTask(CommentsListActivity commentsListActivity) {
		mActivity = commentsListActivity;
	}
	
	public void addDeferred(DeferredCommentProcessing deferredCommentProcessing) {
		mDeferredProcessingList.add(deferredCommentProcessing);
	}
	
	public void addDeferredHighPriority(DeferredCommentProcessing deferredCommentProcessing) {
		mDeferredProcessingHighPriorityList.add(deferredCommentProcessing);
	}
	
	public void addDeferredLowPriority(DeferredCommentProcessing deferredCommentProcessing) {
		mDeferredProcessingLowPriorityList.add(deferredCommentProcessing);
	}
	
	public void moveHighPriorityOverflowToLowPriority(int highPriorityMaxSize) {
		if (mDeferredProcessingHighPriorityList.size() > highPriorityMaxSize) {
			DeferredCommentProcessing overflow = mDeferredProcessingHighPriorityList.removeFirst();
			mDeferredProcessingLowPriorityList.add(overflow);
		}
	}
	
	
	@Override
	protected Void doInBackground(Void... params) {
		return null;
	}

	
}
