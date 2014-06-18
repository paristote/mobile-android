/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.mobile.tests;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.robolectric.Robolectric.shadowOf;

import org.exoplatform.R;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.ui.DashboardActivity;
import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.ui.HomeActivity;
import org.exoplatform.ui.login.LoginActivity;
import org.exoplatform.ui.social.SocialTabsActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.widget.ShaderImageView;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ExoShadowResources;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowSupportMenuInflater;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
/**
 * Created by The eXo Platform SAS
 * Author : Philippe Aristote
 *          paristote@exoplatform.com
 * Apr 16, 2014  
 */
@RunWith(ExoRobolectricTestRunner.class)
@Config(qualifiers = "v10", reportSdk = 10, shadows = ExoShadowResources.class)
public class HomeActivityTest extends ExoActivityTestUtils<HomeActivity> {

  ViewFlipper flipper;
  TextView activitiesTV, documentsTV, appsTV, userNameTV;
  LinearLayout activitiesBtn, documentsBtn, appsBtn;
  ShaderImageView userAvatar;
  ActionBar actionBar;
  Menu actionBarMenu;
  
  private void init() {
    flipper = (ViewFlipper)activity.findViewById(R.id.home_social_flipper);
    activitiesTV = (TextView)activity.findViewById(R.id.home_btn_activity);
    documentsTV = (TextView)activity.findViewById(R.id.home_btn_document);
    appsTV = (TextView)activity.findViewById(R.id.home_btn_apps);
    userNameTV = (TextView)activity.findViewById(R.id.home_textview_name);
    userAvatar = (ShaderImageView)activity.findViewById(R.id.home_user_avatar);
    
    activitiesBtn = (LinearLayout)activitiesTV.getParent();
    documentsBtn = (LinearLayout)documentsTV.getParent();
    appsBtn = (LinearLayout)appsTV.getParent();
    
    actionBar = activity.getSupportActionBar();
    
  }
  
  @Override
  @Before
  public void setup() {
    controller = Robolectric.buildActivity(HomeActivity.class);
    
    // mock response for HTTP GET /rest/api/social/version/latest.json
    Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_SOCIAL_VERSION_LATEST), getResponseOKForRequest(REQ_SOCIAL_VERSION_LATEST));
    // mock response for HTTP GET /rest/private/api/social/v1-alpha3/portal/identity/organization/{testuser}.json
    Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_SOCIAL_IDENTITY), getResponseOKForRequest(REQ_SOCIAL_IDENTITY));
    // mock response for HTTP GET /rest/private/api/social/v1-alpha3/portal/identity/{testidentity}.json
    Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_SOCIAL_IDENTITY_2), getResponseOKForRequest(REQ_SOCIAL_IDENTITY_2));
    // mock response for HTTP GET /rest/private/api/social/v1-alpha3/portal/activity_stream/feed.json
    Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_SOCIAL_NEWS), getResponseOKForRequest(REQ_SOCIAL_NEWS));
  }
  
  @Override
  @After
  public void teardown() {
    // empty to avoid call to controller.destroy()
  }
  
  @Test
  public void verifyDefaultLayout() {
    
    create();
    init();
    
    // Adding a call to Robolectric.runUiThreadTasksIncludingDelayedTasks() just prior to the assertions ensures that the async tasks execute
    Robolectric.runUiThreadTasksIncludingDelayedTasks();
    
    assertThat(userNameTV).containsText(TEST_USER_NAME); // text field is filled by data returned in RESP_SOCIAL_IDENTITY
    
    
    assertThat(flipper).hasChildCount(1); // should have only 1 activity in the flipper since RESP_SOCIAL_NEWS contains just 1 activity
    
    assertThat(activitiesTV).containsText(R.string.ActivityStream);
    assertThat(documentsTV).containsText(R.string.Documents);
    assertThat(appsTV).containsText(R.string.Dashboard);
    
    // because it's a greendroid actionbar, the number of items is not exposed
    // we use a trick to verify that only 2 items are present
    assertNotNull(actionBarMenu.getItem(0)); // refresh button, must not be null
    assertNotNull(actionBarMenu.getItem(1)); // sign-out button, must not be null
    assertNull(actionBarMenu.getItem(2));    // ActionBar.getItem() returns null when there is no item at the given position
    
    
  }
  
  @Test
  public void shouldOpenNewsActivity() {
    create();
    init();
    
    Robolectric.clickOn(activitiesBtn);
    
    ShadowActivity sActivity = shadowOf(activity);
    Intent welcomeIntent = sActivity.getNextStartedActivity();
    ShadowIntent sIntent = shadowOf(welcomeIntent);
    
    assertThat(sIntent.getComponent().getClassName(), equalTo(SocialTabsActivity.class.getName()));
  }
  
  @Test
  public void shouldOpenNewsActivityFromFlipper() {
    create();
    init();
    
    Robolectric.clickOn(flipper);
    
    ShadowActivity sActivity = shadowOf(activity);
    Intent welcomeIntent = sActivity.getNextStartedActivity();
    ShadowIntent sIntent = shadowOf(welcomeIntent);
    
    assertThat(sIntent.getComponent().getClassName(), equalTo(SocialTabsActivity.class.getName()));
  }
  
  @Test
  public void shouldOpenDocumentsActivity() {
    create();
    init();
    
    Robolectric.clickOn(documentsBtn);
    
    ShadowActivity sActivity = shadowOf(activity);
    Intent welcomeIntent = sActivity.getNextStartedActivity();
    ShadowIntent sIntent = shadowOf(welcomeIntent);
    
    assertThat(sIntent.getComponent().getClassName(), equalTo(DocumentActivity.class.getName()));
    
  }
  
  @Test
  public void shouldOpenDashboardActivity() {
    create();
    init();
    
    Robolectric.clickOn(appsBtn);
    
    ShadowActivity sActivity = shadowOf(activity);
    Intent welcomeIntent = sActivity.getNextStartedActivity();
    ShadowIntent sIntent = shadowOf(welcomeIntent);
    
    assertThat(sIntent.getComponent().getClassName(), equalTo(DashboardActivity.class.getName()));
  }
  
  @Test
  public void shouldSignOutAndOpenLoginActivity() {
    create();
    init();
    

    actionBarMenu.performIdentifierAction(1, 0);
//    Robolectric.clickOn(actionBarMenu.getItem(1)); // sign out button
    
    ShadowActivity sActivity = shadowOf(activity);
    Intent welcomeIntent = sActivity.getNextStartedActivity();
    ShadowIntent sIntent = shadowOf(welcomeIntent);
    
    assertNull(ExoConnectionUtils.httpClient);
    assertNull(AccountSetting.getInstance().cookiesList);
    assertNull(SocialServiceHelper.getInstance().userIdentity);
    assertNull(SocialServiceHelper.getInstance().activityService);
    assertNull(SocialServiceHelper.getInstance().identityService);
    assertNull(SocialServiceHelper.getInstance().socialInfoList);
    assertNull(SocialServiceHelper.getInstance().userProfile);
    assertNull(SocialServiceHelper.getInstance().myConnectionsList);
    assertNull(SocialServiceHelper.getInstance().mySpacesList);
    assertNull(SocialServiceHelper.getInstance().myStatusList);
    
    assertThat(sIntent.getComponent().getClassName(), equalTo(LoginActivity.class.getName()));
    
  }
  
  @Test
  public void shouldRefreshFlipper() {
    /* 
     * FIXME current impl refresh activities in the activity stream (SocialTabsActivity)
     * HomeActivity.onHandleActionBarItemCLick() {  
     *    homeController.onLoad(ExoConstants.HOME_SOCIAL_MAX_NUMBER, SocialTabsActivity.ALL_UPDATES);
     * }
     * 
     * TODO it should refresh in the flipper view instead
     * HomeActivity.onHandleActionBarItemCLick() {
     *    homeController.onLoad(ExoConstants.HOME_SOCIAL_MAX_NUMBER, HomeController.FLIPPER_VIEW);
     * }
     */
  }

}
