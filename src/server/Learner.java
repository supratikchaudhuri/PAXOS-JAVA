package server;

import java.io.IOException;

import DB.DB;
import utils.KeyValuePacket;

/**
 * Learner class to commit requests to modify the key value store.
 */
public class Learner extends DB {

  /**
   * Constructor to initialize database, i.e. key value store
   *
   * @throws IOException exception
   */
  public Learner() throws IOException {
    super();
  }

  /**
   * Used for committing key value pair, or modify the storage.
   *
   * @param request the request to be committed
   * @return response
   */
  public String commit(KeyValuePacket request) {
    String res;
    switch (request.getType()) {
      case GET:
        if (contains(request.getKey())) {
          res = "key: " + request.getKey() + ", value: " + get(request.getKey());
        } else {
          res = "key: " + request.getKey() + " not found in map";
        }
        return res;

      case PUT:
        if (contains(request.getKey())) {
          res = "key: " + request.getKey() + ", with value: " + get(request.getKey()) + " already exists";
        } else {
          put(request.getKey(), request.getValue());
          res = "key: " + request.getKey() + ", value: " + request.getValue() + " stored in Key-Value store";
        }
        return res;

      case DELETE:
        if (!contains(request.getKey())) {
          res = "key: " + request.getKey() + " doesn't exist";
        } else {
          delete(request.getKey());
          res = "key: " + request.getKey() + " has been deleted";
        }
        return res;

      default:
        return null;
    }
  }
}

