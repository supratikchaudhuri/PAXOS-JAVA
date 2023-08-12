package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Util class to get list of servers to communicate with.
 */
public class ServerListFetcher {
  static Properties prop;

  /**
   * Fetches al list of all the servers that user want the server and client to communicate with.
   * These are the addresses of servers that will participate in the paxos fault tolerance algorithm.
   * Additionally, the client can send request to any of these servers.
   *
   * @return Map of participating server names
   * @throws IOException in case of accessing servers.properties file
   */
  public static Map<String, String> fetchServers() throws IOException {
    prop = new Properties();
    InputStream in = new FileInputStream("servers.properties");
    prop.load(in);
    in.close();

    Map<String, String> serverList = new HashMap<>();
    Set<Object> set = prop.keySet();

    for (Object o : set) {
      String key = (String) o;
      serverList.put(key, prop.getProperty(key));
    }

    return serverList;
  }

}
