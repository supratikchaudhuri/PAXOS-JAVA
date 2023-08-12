change Message class to Request



# Project 4

### Fault-Tolerant Replicated KV-Store using Paxos

To set up a connectionless client server communication, please follow the steps in order:

**Step 1**: Start the coordinator with the following code in the terminal, with **IP** & **PORT** as a command line argument:
>`java -jar coordinator.jar <COORDINATOR_IP> <COORDINATOR_PORT>`

**Step 2**: Start the client with the following code in the terminal and pass **IP** and **PORT** pairs of all the servers you want to interact with as a command line argument
>`java -jar client.jar <IP1> <PORT1> <IP2> <PORT2> ... <IPn> <PORTn>`

**Step 3:** Pass request from the client by following the instructions listed in the CLI.

### Points to remember while starting the program
- Please make sure you have the `map.properties` file in the same folder as the executable jar files.
- In case a serving participants fail, please restart the whole program from step 1. (Could have implemented a feature to remove server from coordinators participant list, but it was out of scope for this project... maybe in the future).

---

### Interaction Rules with the server

1. The **GET** operation accepts a `string` as an input and returns the value associated with it in the map. If no value is associated, it returns `NULL`.
2. The **PUT** operation accepts 2 arguments, name a key and a value as `string`, and stores them in the map. In case the key already exits, it rewrites the value with the latest passed argument.
3. The **DELETE** operation deletes a key-value pair from the map. If the key does not exist, the map remains unchanged.
4. The **Change Server** operation selects a particular sever to interact with from a list of all servers user provided as cli arguments.
5. The **Save & Exit** operation permanently stores changes in the external file. If client is closes without this option, changes made would not persist.

---

### Map characteristics:

1. key-value pairs are case sensitive.
2. key-value pairs are stored as`string`
3. keys and values are trimmed of trailing spaces before storing in the map.
4. At the start of the program, following 5 key-value pairs are initially added:

| Key                | Value                                |   
|--------------------|--------------------------------------|
| MS                 | Computer Science                     |
| Firstname Lastname | John Doe                             |
| hello              | world                                |
| CS6650             | Building Scalable Distributed System |
| BTC                | Bitcoin                              |


