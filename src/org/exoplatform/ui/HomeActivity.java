package org.exoplatform.ui;

import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;
import greendroid.widget.LoaderActionBarItem;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.controller.home.HomeController;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ChatServiceHelper;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.ui.social.SocialActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.HomeSocialItem;
import org.exoplatform.widget.MyActionBar;
import org.exoplatform.widget.ShaderImageView;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class HomeActivity extends MyActionBar {
  private static final String SERVER_SETTING_HELPER = "SERVER_SETTING_HELPER";

  private static final String ACCOUNT_SETTING       = "ACCOUNT_SETTING";

  private TextView            activityButton;

  private TextView            documentButton;

  private TextView            appsButton;

  private String              newsTitle;

  private String              documentTitle;

  private String              appsTitle;

  private Resources           resource;

  private HomeController      homeController;

  private ShaderImageView     homeUserAvatar;

  private TextView            homeUserName;

  private int                 dafault_avatar        = R.drawable.default_avatar;

  private ViewFlipper         viewFlipper;

  private LoaderActionBarItem loaderItem;

  public static HomeActivity  homeActivity;

  @Override
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setActionBarContentView(R.layout.home_layout);

    super.getActionBar().setType(greendroid.widget.ActionBar.Type.Dashboard);
    addActionBarItem(Type.Refresh);
    getActionBar().getItem(0).setDrawable(R.drawable.action_bar_icon_refresh);
    addActionBarItem();
    getActionBar().getItem(1).setDrawable(R.drawable.action_bar_logout_button);
    homeActivity = this;
    if (bundle != null) {
      AccountSetting accountSetting = bundle.getParcelable(ACCOUNT_SETTING);
      AccountSetting.getInstance().setInstance(accountSetting);
      ServerSettingHelper settingHelper = bundle.getParcelable(SERVER_SETTING_HELPER);
      ServerSettingHelper.getInstance().setInstance(settingHelper);
      ArrayList<String> cookieList = AccountSetting.getInstance().cookiesList;
      ExoConnectionUtils.setCookieStore(ExoConnectionUtils.cookiesStore, cookieList);
    }
    init();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelable(SERVER_SETTING_HELPER, ServerSettingHelper.getInstance());
    outState.putParcelable(ACCOUNT_SETTING, AccountSetting.getInstance());

  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    getContentView().removeAllViews();
    setActionBarContentView(R.layout.home_layout);
    init();
  }

  @Override
  protected void onResume() {
    super.onResume();
    setInfo();
    loaderItem = (LoaderActionBarItem) getActionBar().getItem(0);
    startSocialService(loaderItem);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.clear();
    menu.add(0, 1, 0, getResources().getString(R.string.Settings))
        .setIcon(R.drawable.optionsettingsbutton);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    int selectedItemIndex = item.getItemId();

    if (selectedItemIndex == 1) {
      Intent next = new Intent(HomeActivity.this, SettingActivity.class);
      next.putExtra(ExoConstants.SETTING_TYPE, 0);
      startActivity(next);
    }
    return false;
  }

  @Override
  public void onBackPressed() {
    onFinish();
    // new LogoutDialog(HomeActivity.this, homeController).show();
  }

  private void init() {
    activityButton = (TextView) findViewById(R.id.home_btn_activity);
    // activityButton.setOnClickListener(this);
    documentButton = (TextView) findViewById(R.id.home_btn_document);
    // documentButton.setOnClickListener(this);
    appsButton = (TextView) findViewById(R.id.home_btn_apps);
    // appsButton.setOnClickListener(this);
    homeUserAvatar = (ShaderImageView) findViewById(R.id.home_user_avatar);
    homeUserAvatar.setDefaultImageResource(dafault_avatar);
    homeUserName = (TextView) findViewById(R.id.home_textview_name);
    viewFlipper = (ViewFlipper) findViewById(R.id.home_social_flipper);
    // viewFlipper.setOnClickListener(this);
    setInfo();
  }

  private void setInfo() {
    resource = getResources();
    changeLanguage();
    activityButton.setText(newsTitle);
    documentButton.setText(documentTitle);
    appsButton.setText(appsTitle);
    if (SocialServiceHelper.getInstance().userProfile != null) {
      setProfileInfo(SocialServiceHelper.getInstance().userProfile);
    }

    if (SocialServiceHelper.getInstance().socialInfoList != null) {
      setSocialInfo(SocialServiceHelper.getInstance().socialInfoList);
    }
  }

  private void startSocialService(LoaderActionBarItem loader) {
    homeController = new HomeController(this);
    if (SocialServiceHelper.getInstance().activityService == null) {
      homeController.launchNewsService(loader);
    }
  }

  public void setProfileInfo(String[] profile) {
    homeUserAvatar.setUrl(profile[0]);
    homeUserName.setText(profile[1]);
  }

  /*
   * Set Social Information and start animation
   */
  public void setSocialInfo(ArrayList<SocialActivityInfo> list) {
    HomeSocialItem socialItem = null;
    viewFlipper.removeAllViews();
    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    /*
     * Display maximum 10 activities
     */
    int listSize = list.size();
    int maxChild = (listSize > ExoConstants.HOME_SOCIAL_MAX_NUMBER) ? ExoConstants.HOME_SOCIAL_MAX_NUMBER
                                                                   : listSize;
    for (int i = 0; i < maxChild; i++) {
      socialItem = new HomeSocialItem(this, list.get(i));
      viewFlipper.addView(socialItem, params);
    }
    viewFlipper.startFlipping();
    viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.home_anim_right_to_left));
    viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.home_anim_left_out));

  }

  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {

    case -1:
      break;
    case 0:
      loaderItem = (LoaderActionBarItem) item;
      if (SocialServiceHelper.getInstance().activityService == null) {
        homeController.launchNewsService(loaderItem);
      } else {
        homeController.onLoad(ExoConstants.NUMBER_OF_ACTIVITY, loaderItem);
      }
      break;
    case 1:
      // new LogoutDialog(HomeActivity.this, homeController).show();
      onFinish();
      break;
    }
    return true;

  }

  private void onFinish() {
    if (ExoConnectionUtils.httpClient != null) {
      ExoConnectionUtils.httpClient.getConnectionManager().shutdown();
      ExoConnectionUtils.httpClient = null;
    }

    AccountSetting.getInstance().cookiesList = null;
    /*
     * Clear all social service data
     */
    SocialServiceHelper.getInstance().clearData();
    homeController.finishService();
    homeActivity = null;
    finish();
  }

  public void onNewsClick(View view) {
    if (ExoConnectionUtils.isNetworkAvailableExt(this)) {
      launchNewsService();
    } else {
      new ConnectionErrorDialog(this).show();
    }
  }

  public void onDocumentClick(View view) {

    if (ExoConnectionUtils.isNetworkAvailableExt(this)) {
      launchDocumentApp();
    } else {
      new ConnectionErrorDialog(this).show();
    }
  }

  public void onDashboardClick(View view) {

    if (ExoConnectionUtils.isNetworkAvailableExt(this)) {
      launchDashboardApp();
    } else {
      new ConnectionErrorDialog(this).show();
    }
  }

  // @Override
  // public void onClick(View view) {
  // if (ExoConnectionUtils.isNetworkAvailableExt(this)) {
  // if (view.equals(activityButton) || view.equals(viewFlipper)) {
  // launchNewsService();
  // }
  // if (view.equals(documentButton)) {
  // launchDocumentApp();
  // }
  // if (view.equals(appsButton)) {
  // launchDashboardApp();
  // }
  // } else {
  // new ConnectionErrorDialog(this).show();
  // }
  //
  // }

  private void launchNewsService() {
    Intent next = new Intent(this, SocialActivity.class);
    startActivity(next);
  }

  private void launchDocumentApp() {

    Intent next = new Intent(this, DocumentActivity.class);
    startActivity(next);
  }

  private void launchDashboardApp() {
    Intent intent = new Intent(this, DashboardActivity.class);
    startActivity(intent);
  }

  private void changeLanguage() {
    newsTitle = resource.getString(R.string.ActivityStream);
    documentTitle = resource.getString(R.string.Documents);
    appsTitle = resource.getString(R.string.Dashboard);

  }

}
