package DB;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import utils.Logger;

/**
 * Database
 */
public class DB {

  // create a hash map to storage key value pairs
  private final Map<String, String> db;

  public DB() throws IOException {
    this.db = new HashMap<>();
    populateMap();
  }

  private void populateMap() throws IOException {
    Properties prop = new Properties();
    InputStream in = new FileInputStream("map.properties");
    prop.load(in);
    in.close();

    Set<Object> set = prop.keySet();

    for(Object o: set){
      String key = (String)o;
      db.put(key, prop.getProperty(key));
    }
  }

  /**
   * Get a value by a key.
   *
   * @param key the key to be found
   * @return the value of the key if the key is available, otherwise null
   */
  public synchronized String get(String key) {
    return db.get(key);
  }

  /**
   * Insert or update a key with its value.
   *
   * @param key   the key to be inserted or updated
   * @param value the value of the key
   */
  public synchronized void put(String key, String value) {
    db.put(key, value);
  }

  /**
   * Delete a key value pair from the storage.
   *
   * @param key the key to be deleted
   */
  public synchronized void delete(String key) {
    db.remove(key);
  }

  /**
   * Determine if the database has the key
   *
   * @param key key
   * @return true if it has, otherwise false
   */
  public synchronized boolean contains(String key) {
    return db.containsKey(key);
  }

  public void saveMapToFile() throws IOException {
    Logger.errorLog("here");
    Properties prop = new Properties();
    InputStream in = new FileInputStream("map.properties");
    prop.load(in);
    in.close();

    for(String key: db.keySet()) {
      prop.setProperty(key, db.get(key));
    }
    prop.store(new FileOutputStream("map.properties"), null);
  }

}

