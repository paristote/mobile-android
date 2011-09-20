package org.exoplatform.singleton;

import java.util.ArrayList;

import org.exoplatform.proxy.ServerObj;

/*
 * This class for storing the list of server url and the index of the selected 
 *  which is used for adding/repairing/deleting function in setting   
 */

public class ServerSettingHelper {
  //The index of server url was selected in setting
  private int                        selectedServerIndex;               
  //if this server url is new url
  private boolean                    isNewServer;                       

  private String                     version;
  // List of server url
  private ArrayList<ServerObj>       serverInfoList;                   

  private static ServerSettingHelper helper = new ServerSettingHelper();

  private ServerSettingHelper() {

  }

  public static ServerSettingHelper getInstance() {
    return helper;
  }

  public void setSelectedServerIndex(int index) {
    selectedServerIndex = index;
  }

  public int getSelectedServerIndex() {
    return selectedServerIndex;
  }

  public void setIsNewServer(boolean is) {
    isNewServer = is;
  }

  public boolean getIsNewServer() {
    return isNewServer;
  }

  public void setVersion(String ver) {
    version = ver;
  }

  public String getVersion() {
    return version;
  }

  public void setServerInfoList(ArrayList<ServerObj> list) {
    serverInfoList = list;
  }

  public ArrayList<ServerObj> getServerInfoList() {
    return serverInfoList;
  }

}