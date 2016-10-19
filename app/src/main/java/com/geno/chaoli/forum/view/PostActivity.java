package com.geno.chaoli.forum.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.databinding.ObservableBoolean;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.geno.chaoli.forum.R;
import com.geno.chaoli.forum.databinding.PostActivityBinding;
import com.geno.chaoli.forum.meta.Constants;
import com.geno.chaoli.forum.utils.ConversationUtils;
import com.geno.chaoli.forum.meta.DividerItemDecoration;
import com.geno.chaoli.forum.viewmodel.BaseViewModel;
import com.geno.chaoli.forum.viewmodel.PostActivityViewModel;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostActivity extends BaseActivity implements ConversationUtils.IgnoreAndStarConversationObserver
{
	public static final String TAG = "PostActivity";

	private static final int POST_NUM_PER_PAGE = 20;

	private final Context mContext = this;

	@BindView(R.id.reply)
	public FloatingActionButton reply;

	public static SharedPreferences sp;
	public SharedPreferences.Editor e;

	int mConversationId;

	String mTitle;
	int mPage;

	@BindView(R.id.postList)
	RecyclerView postListRv;
	@BindView(R.id.swipyRefreshLayout)
	SwipyRefreshLayout swipyRefreshLayout;

	LinearLayoutManager mLinearLayoutManager;

	PostActivityViewModel viewModel;

	public static final int menu_settings = 0;
	public static final int menu_share = 1;
	public static final int menu_author_only = 2;
	public static final int menu_star = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setViewModel(new PostActivityViewModel());
		ButterKnife.bind(this);

		Bundle data = getIntent().getExtras();
		mConversationId = data.getInt("conversationId");
		viewModel.setConversationId(mConversationId);
		mTitle = data.getString("conversationTitle", "");
		setTitle(mTitle);
		viewModel.setTitle(mTitle);
		mPage = data.getInt("page", 1);
		viewModel.setPage(mPage);
		viewModel.setAuthorOnly(data.getBoolean("isAuthorOnly", false));
		sp = getSharedPreferences(Constants.postSP + mConversationId, MODE_PRIVATE);

		configToolbar(mTitle);

		swipyRefreshLayout.setDirection(SwipyRefreshLayoutDirection.BOTH);
		swipyRefreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh(SwipyRefreshLayoutDirection direction) {
				viewModel.loadMore();
			}
		});

		mLinearLayoutManager = new LinearLayoutManager(mContext);
        postListRv.setLayoutManager(mLinearLayoutManager);
		postListRv.addItemDecoration(new DividerItemDecoration(mContext));

		viewModel.getList(0);

		viewModel.goToReply.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
			@Override
			public void onPropertyChanged(Observable observable, int i) {
				if (((ObservableBoolean) observable).get()) {
					Intent toReply = new Intent(mContext, ReplyAction.class);
					toReply.putExtra("conversationId", viewModel.conversationId);
					startActivityForResult(toReply, viewModel.REPLY_CODE);
				}
			}
		});

		viewModel.showToast.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
			@Override
			public void onPropertyChanged(Observable observable, int i) {
				if (((ObservableBoolean) observable).get()) {
					showToast(viewModel.toastContent.get());
				}
			}
		});

	}

	/*class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.PostViewHolder> {
		List<Post> mPosts;
		Context mContext;
		public PostListAdapter(Context context, List<Post> posts) {
			mContext = context;
			mPosts = posts;
		}

		public void setPosts(List<Post> posts) {
			this.mPosts = posts;
		}
		public List<Post> getPosts() {
			return mPosts;
		}

		@Override
		public int getItemCount() {
			return mPosts.size();
		}

		@Override
		public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new PostViewHolder(LayoutInflater.from(mContext).inflate(R.layout.post_view, parent, false));
		}

		@Override
		public void onBindViewHolder(final PostViewHolder holder, final int position) {
			final Post post = mPosts.get(position);
			holder.avatar.update(post.getAvatarFormat(), post.getMemberId(), post.getUsername());
			holder.avatar.scale(35);
			holder.usernameAndSignature.setText(post.signature == null ? post.username : getString(R.string.comma, post.username, post.signature));
			holder.floor.setText(String.format(Locale.getDefault(), "%d", post.getFloor()));

			if (post.deleteMemberId != 0)
			{
				holder.itemView.setBackgroundColor(0xFF808080);
				holder.avatar.setVisibility(View.GONE);
				//signature.setVisibility(GONE);
				holder.content.setVisibility(View.GONE);
			} else {
				holder.itemView.setBackgroundColor(Color.WHITE);
				holder.avatar.setVisibility(View.VISIBLE);
				holder.content.setVisibility(View.VISIBLE);
			}

			holder.avatar.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.i(TAG, "click");
					Intent intent = new Intent(mContext, HomepageActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("username", holder.avatar.getUsername());
					bundle.putInt("userId", holder.avatar.getUserId());
					bundle.putString("avatarSuffix", holder.avatar.getImagePath());
					bundle.putString("signature", post.signature);
					intent.putExtras(bundle);
					mContext.startActivity(intent);
				}
			});
			holder.content.init(mContext);
			holder.content.setPost(post);
			holder.content.setConversationId(mConversationId);
			for (int i = 0; i < holder.content.getChildCount(); i++) {
				View child = holder.content.getChildAt(i);
				if (child instanceof OnlineImgTextView) {
					child.setOnLongClickListener(new View.OnLongClickListener() {
						@Override
						public boolean onLongClick(View view) {
							new AlertDialog.Builder(mContext).setTitle("请选择")
									.setItems(new String[]{getString(R.string.reply)}, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialogInterface, int i) {
											switch (i){
												case 0:
													Intent toReply = new Intent(PostActivity.this, ReplyAction.class);
													toReply.putExtra("conversationId", mConversationId);
													toReply.putExtra("postId", post.getShowingPostId());
													toReply.putExtra("replyTo", post.getUsername());
													toReply.putExtra("replyMsg", PostUtils.removeQuote(post.getContent()));
													Log.d(TAG, "onClick: content = " + post.getContent() + ", replyMsg = " + PostUtils.removeQuote(post.getContent()));
													startActivityForResult(toReply, REPLY_CODE);
													break;
											}
										}
									})
									.show();
							return true;
						}
					});
				}
			}
			for (int i = 0; i < ((LinearLayout) holder.itemView).getChildCount(); i++) {
				View child = ((LinearLayout) holder.itemView).getChildAt(i);
				if(child instanceof ImageView) {
					((LinearLayout) holder.itemView).removeViewAt(i);
				}
			}
		}

		class PostViewHolder extends RecyclerView.ViewHolder {
			@BindView(R.id.avatar)
			AvatarView avatar;
			@BindView(R.id.usernameAndSignature)
			TextView usernameAndSignature;
			@BindView(R.id.floor)
			TextView floor;
			@BindView(R.id.content)
			PostContentView content;
			PostViewHolder(View view){
				super(view);
				ButterKnife.bind(this, view);
			}
			
		}
	}
	*/

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		viewModel.replyComplete(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		menu.add(Menu.NONE, Menu.NONE, menu_settings, R.string.settings).setIcon(android.R.drawable.ic_menu_manage);
		menu.add(Menu.NONE, Menu.NONE, menu_share, R.string.share).setIcon(android.R.drawable.ic_menu_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add(Menu.NONE, Menu.NONE, menu_author_only, viewModel.isAuthorOnly() ? R.string.cancel_author_only : R.string.author_only).setIcon(android.R.drawable.ic_menu_view);
		menu.add(Menu.NONE, Menu.NONE, menu_star, R.string.star).setIcon(R.drawable.ic_menu_star).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		switch (item.getOrder())
		{
			case menu_settings:
				CharSequence[] settingsMenu = {getString(R.string.ignore_this), getString(R.string.mark_as_unread)};
				AlertDialog.Builder ab = new AlertDialog.Builder(this)
						.setTitle(R.string.settings)
						.setCancelable(true)
						.setItems(settingsMenu, new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								switch (which)
								{
									case 0:
										ConversationUtils.ignoreConversation(mContext, viewModel.conversationId, (PostActivity) mContext);
										break;
									case 1:
										showToast(R.string.mark_as_unread);
										break;
								}
							}
						});
				ab.show();
				break;
			case menu_share:
				share();
				break;
			case menu_author_only:
				/*finish();
				Intent author_only = new Intent(this, PostActivity.class);
				author_only.putExtra("conversationId", viewModel.conversationId);
				author_only.putExtra("page", viewModel.isAuthorOnly ? "" : "?author=lz");
				author_only.putExtra("title", viewModel.title);
				author_only.putExtra("isAuthorOnly", !isAuthorOnly);
				startActivity(author_only);*/
				break;
			case menu_star:
				// TODO: 16-3-28 2201 Star light
				ConversationUtils.starConversation(this, viewModel.conversationId, this);
				break;
		}

		return true;
	}

	@Override
	public void onIgnoreConversationSuccess(Boolean isIgnored)
	{
		Toast.makeText(PostActivity.this, isIgnored ? R.string.ignore_this_success : R.string.ignore_this_cancel_success, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onIgnoreConversationFailure(int statusCode)
	{
		Toast.makeText(PostActivity.this, getString(R.string.failed, statusCode), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onStarConversationSuccess(Boolean isStarred)
	{
		Toast.makeText(PostActivity.this, isStarred ? R.string.star_success : R.string.star_cancel_success, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onStarConversationFailure(int statusCode)
	{
		Toast.makeText(PostActivity.this, getString(R.string.failed, statusCode), Toast.LENGTH_SHORT).show();
	}

	private void share() {
		Intent shareIntent = new Intent();
		shareIntent.setAction(Intent.ACTION_SEND);
		shareIntent.putExtra(Intent.EXTRA_TEXT, Constants.postListURL + viewModel.conversationId);
		shareIntent.setType("text/plain");
		startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
	}
	@Override
	public void setViewModel(BaseViewModel viewModel) {
		this.viewModel = (PostActivityViewModel) viewModel;
		PostActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.post_activity);
		binding.setViewModel(this.viewModel);
	}
}
