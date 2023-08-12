package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class ServerListFetcher {
  static Properties prop;

  public static Map<String, String> fetchServers() throws IOException {
    prop = new Properties();
    InputStream in = new FileInputStream("servers.properties");
    prop.load(in);
    in.close();

    Map<String, String> serverList = new HashMap<>();
    Set<Object> set = prop.keySet();

    for(Object o: set){
      String key = (String)o;
      serverList.put(key, prop.getProperty(key));
    }

    return serverList;
  }

}
