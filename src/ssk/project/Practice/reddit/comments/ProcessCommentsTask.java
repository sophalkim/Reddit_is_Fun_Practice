package ssk.project.Practice.reddit.comments;

import java.util.LinkedList;

import ssk.project.Practice.util.Util;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.andrewshu.android.reddit.R;
import com.andrewshu.android.reddit.comments.CommentsListActivity;
import com.andrewshu.android.reddit.common.Constants;
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
	
	public void mergeHighPriorityListToMainList() {
		mDeferredProcessingList.addAll(0, mDeferredProcessingHighPriorityList);
		mDeferredProcessingHighPriorityList.clear();
	}
	
	public void mergeLowPriorityListToMainList() {
		mDeferredProcessingList.addAll(0, mDeferredProcessingLowPriorityList);
		mDeferredProcessingLowPriorityList.clear();
	}
	
	
	@Override
	protected Void doInBackground(Void... v) {
		for (final DeferredCommentProcessing deferredCommentProcessing : mDeferredProcessingList) {
			processCommentSlowSteps(deferredCommentProcessing.comment);
			publishProgress(deferredCommentProcessing.commentIndex);
		}
		cleanupQueues();
		return null;
	}
	
	private void cleanupQueues() {
		mDeferredProcessingList.clear();
		mDeferredProcessingHighPriorityList.clear();
		mDeferredProcessingLowPriorityList.clear();
	}
	
	@Override
	public void onProgressUpdate(Integer... commentsToShow) {
		for (Integer commentIndex : commentsToShow) {
			refreshDeferredCommentIfVisibleUI(commentIndex);
		}
	}
	
	private void processCommentSlowSteps(ThingInfo comment) {
		if (comment.getBody_html() != null) {
			CharSequence spanned = createSpanned(comment.getBody_html());
			comment.setSpannedBody(spanned);
		}
		markdown.getURLs(comment.getBody(), comment.getUrls());
	}
	
	private CharSequence createSpanned(String bodyHtml) {
		try {
			bodyHtml = Html.fromHtml(bodyHtml).toString();
			bodyHtml = Util.convertHtmlTags(bodyHtml);
			
			Spanned body = Html.fromHtml(bodyHtml);
			if (body.length() > 2) {
				return body.subSequence(0, body.length() - 2);
			} else {
				return "";
			}
		} catch (Exception e) {
			if (Constants.LOGGING) Log.e(TAG, "created Spanned failed", e);
			return null;
		}
	}

	private void refreshDeferredCommentIfVisibleUI(final int commentIndex) {
		if (isPositionVisibleUI(commentIndex))
			refreshCommentUI(commentIndex);
	}
	
	private void refreshCommentUI(int commentIndex) {
		refreshCommentBodyTextViewUI(commentIndex);
		refreshCommentSubmitterUI(commentIndex);
	}
	
	private void refreshCommentBodyTextViewUI(int commentIndex) {
		int positionOnScreen = commentIndex - mActivity.getListView().getFirstVisiblePosition();
		View v = mActivity.getListView().getChildAt(positionOnScreen);
		if (v != null) {
			View bodyTextView = v.findViewById(R.id.body);
			if (bodyTextView != null) {
				((TextView) bodyTextView).setText(mActivity.mCommentsList.get(commentIndex).getSpannedBody());
			}
		}
	}
}
