package com.dtalk.dd.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dtalk.dd.DB.entity.UserEntity;
import com.dtalk.dd.R;
import com.dtalk.dd.config.DBConstant;
import com.dtalk.dd.config.IntentConstant;
import com.dtalk.dd.http.base.BaseClient;
import com.dtalk.dd.http.base.BaseResponse;
import com.dtalk.dd.http.friend.FriendClient;
import com.dtalk.dd.utils.IMUIHelper;
import com.dtalk.dd.imservice.event.UserInfoEvent;
import com.dtalk.dd.imservice.service.IMService;
import com.dtalk.dd.ui.activity.DetailPortraitActivity;
import com.dtalk.dd.imservice.support.IMServiceConnector;
import com.dtalk.dd.ui.widget.IMBaseImageView;
import com.dtalk.dd.utils.Logger;
import com.dtalk.dd.utils.ThemeUtils;
import com.dtalk.dd.utils.ViewUtils;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * 1.18 添加currentUser变量
 */
public class UserInfoFragment extends MainFragment {

	private View curView = null;
    private IMService imService;
    private UserEntity currentUser;
    private int currentUserId;
    private IMServiceConnector imServiceConnector = new IMServiceConnector(){
        @Override
        public void onIMServiceConnected() {
            Logger.d("detail#onIMServiceConnected");

            imService = imServiceConnector.getIMService();
            if (imService == null) {
                Logger.e("detail#imService is null");
                return;
            }
			EventBus.getDefault().registerSticky(UserInfoFragment.this);
            if(currentUserId == 0){
                Logger.e("detail#intent params error!!");
                return;
            }
            currentUser = imService.getContactManager().findContact(currentUserId);
            if(currentUser != null) {
                initBaseProfile();
                initDetailProfile();
            }
//            ArrayList<Integer> userIds = new ArrayList<>(1);
//            //just single type
//            userIds.add(currentUserId);
            imService.getContactManager().reqGetDetailUser(currentUserId+"");
        }

		@Override
		public void onServiceDisconnected() {
			if(EventBus.getDefault().isRegistered(UserInfoFragment.this)){
				EventBus.getDefault().unregister(UserInfoFragment.this);
			}
		}

    };

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if(EventBus.getDefault().isRegistered(UserInfoFragment.this)){
			EventBus.getDefault().unregister(UserInfoFragment.this);
		}
		imServiceConnector.disconnect(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		currentUserId = getActivity().getIntent().getIntExtra(IntentConstant.KEY_PEERID,0);
		imServiceConnector.connect(getActivity());
		if (null != curView) {
			((ViewGroup) curView.getParent()).removeView(curView);
			return curView;
		}
		curView = inflater.inflate(R.layout.tt_fragment_user_detail, topContentView);
		super.init(curView);
		showProgressBar();
		initRes();
		return curView;
	}

	@Override
	public void onResume() {
		Intent intent = getActivity().getIntent();
		if (null != intent) {
			String fromPage = intent.getStringExtra(IntentConstant.USER_DETAIL_PARAM);
			setTopLeftText(fromPage);
		}
		super.onResume();
	}

	/**
	 * @Description 初始化资源
	 */
	private void initRes() {
		// 设置标题栏
		setTopTitle(getActivity().getString(R.string.page_user_detail));
		setTopLeftButton(R.drawable.tt_top_back);
		topLeftContainerLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				getActivity().finish();
			}
		});
		setTopLeftText(getResources().getString(R.string.top_left_back));
	}

	@Override
	protected void initHandler() {
	}

    public void onEventMainThread(UserInfoEvent event){
        switch (event){
            case USER_INFO_UPDATE:
				currentUser  = imService.getContactManager().findContact(currentUserId);
//                if(entity !=null && currentUser.equals(entity)){
                    initBaseProfile();
                    initDetailProfile();
//                }
                break;
        }
    }


	private void initBaseProfile() {
		Logger.d("detail#initBaseProfile");
        IMBaseImageView portraitImageView = (IMBaseImageView) curView.findViewById(R.id.user_portrait);

		setTextViewContent(R.id.nickName, currentUser.getMainName());
		setTextViewContent(R.id.userName, currentUser.getRealName());
        //头像设置
        portraitImageView.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
        portraitImageView.setCorner(8);
        portraitImageView.setImageResource(R.drawable.tt_default_user_portrait_corner);
        portraitImageView.setImageUrl(currentUser.getAvatar());

		portraitImageView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), DetailPortraitActivity.class);
				intent.putExtra(IntentConstant.KEY_AVATAR_URL, currentUser.getAvatar());
				intent.putExtra(IntentConstant.KEY_IS_IMAGE_CONTACT_AVATAR, true);
				
				startActivity(intent);
			}
		});

		// 设置界面信息
		Button chatBtn = (Button) curView.findViewById(R.id.chat_btn);
		if (currentUserId == imService.getLoginManager().getLoginId()) {
			chatBtn.setVisibility(View.GONE);
		}else{
			if (currentUser.getIsFriend() == 0) {
				chatBtn.setText("添加好友");
			}
			else {
				chatBtn.setText(R.string.chat);
			}
            chatBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
					if (currentUser.getIsFriend() == 0) {
						applyFriend();
					}
					else {
						IMUIHelper.openChatActivity(getActivity(), currentUser.getSessionKey());
						getActivity().finish();
					}
                }
            });

        }
	}

	private void initDetailProfile() {
		Logger.d("detail#initDetailProfile");
		hideProgressBar();
//        DepartmentEntity deptEntity = imService.getContactManager().findDepartment(currentUser.getDepartmentId());
//		setTextViewContent(R.id.department,deptEntity.getDepartName());
//		setTextViewContent(R.id.telno, currentUser.getPhone());
//		setTextViewContent(R.id.email, currentUser.getEmail());
//
//		View phoneView = curView.findViewById(R.id.phoneArea);
//        View emailView = curView.findViewById(R.id.emailArea);
//		IMUIHelper.setViewTouchHightlighted(phoneView);
//        IMUIHelper.setViewTouchHightlighted(emailView);
//
//        emailView.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
//                if (currentUserId == IMLoginManager.instance().getLoginId())
//                    return;
//                IMUIHelper.showCustomDialog(getActivity(), View.GONE, String.format(getString(R.string.confirm_send_email), currentUser.getEmail()), new IMUIHelper.dialogCallback() {
//					@Override
//					public void callback() {
//						Intent data = new Intent(Intent.ACTION_SENDTO);
//						data.setData(Uri.parse("mailto:" + currentUser.getEmail()));
//						data.putExtra(Intent.EXTRA_SUBJECT, "");
//						data.putExtra(Intent.EXTRA_TEXT, "");
//						startActivity(data);
//					}
//				});
//            }
//        });
//
//		phoneView.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (currentUserId == IMLoginManager.instance().getLoginId())
//					return;
//                IMUIHelper.showCustomDialog(getActivity(),View.GONE,String.format(getString(R.string.confirm_dial),currentUser.getPhone()),new IMUIHelper.dialogCallback() {
//                    @Override
//                    public void callback() {
//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                IMUIHelper.callPhone(getActivity(), currentUser.getPhone());
//                            }
//                        },0);
//                    }
//                });
//			}
//		});

		setSex(currentUser.getGender());
	}

	private void setTextViewContent(int id, String content) {
		TextView textView = (TextView) curView.findViewById(id);
		if (textView == null) {
			return;
		}

		textView.setText(content);
	}

	private void setSex(int sex) {
		if (curView == null) {
			return;
		}

		TextView sexTextView = (TextView) curView.findViewById(R.id.sex);
		if (sexTextView == null) {
			return;
		}

		int textColor = Color.rgb(255, 138, 168); //xiaoxian
		String text = getString(R.string.sex_female_name);

		if (sex == DBConstant.SEX_MAILE) {
			textColor = Color.rgb(144, 203, 1);
			text = getString(R.string.sex_male_name);
		}

		sexTextView.setVisibility(View.VISIBLE);
		sexTextView.setText(text);
		sexTextView.setTextColor(textColor);
	}

	private void applyFriend() {
		FriendClient.applyFriend(currentUserId + "", "", new BaseClient.ClientCallback() {
			@Override
			public void onPreConnection() {
				ViewUtils.createProgressDialog(getActivity(), "", ThemeUtils.getThemeColor()).show();
			}

			@Override
			public void onCloseConnection() {
				ViewUtils.dismissProgressDialog();
			}

			@Override
			public void onSuccess(Object data) {
				BaseResponse response = (BaseResponse) data;
				Toast.makeText(getActivity(), response.getMsg(), Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onFailure(String message) {
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onException(Exception e) {
				Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}

}
