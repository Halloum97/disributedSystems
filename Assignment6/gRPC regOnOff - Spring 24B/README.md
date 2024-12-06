### GRPC Services and Registry

#### Description
This project demonstrates a network of nodes using gRPC services and a registry for service discovery. The project includes clients and nodes that register themselves with a central registry, allowing clients to discover and interact with available services dynamically. The project fulfills the following requirements:
- Registers nodes with a central registry.
- Allows clients to discover and interact with registered services.
- Provides robust error handling to ensure the client does not crash easily.

#### Running the Program
To run the program, follow these steps:

1. **Run the Registry Server:**
 
   gradle runRegistryServer


2. **Run a Node with Services:**

   gradle runNode
 

3. **Run the Client:**

   gradle runClient2 


#### Working with the Program
1. **Client Interaction:**
   - The client will fetch available services from the registry.
   - It will display the list of available services.
   - The user can select a service by entering the corresponding number.
   - The client will then interact with the selected service, prompting the user for any necessary input.

2. **Expected Inputs:**
   - When prompted to select a service, enter the number corresponding to the desired service.
   - For services like "Echo," you will be prompted to enter a message.

#### Requirements Fulfilled
- **Task 3.1: Register things locally**
  - Created `Client2.java` to connect to the registry and fetch available services.
  - Ensured the client can choose between all registered services.
  - Implemented robust error handling to prevent crashes.

- **Task 3.2: Putting your node online**
  - Provided commands to run the node and client with the necessary parameters.

#### Screencast
[Watch the screencast](https://somup.com/cZl1VLJRXf) (NOTE: PLEASE TURN OFF THE VOLUME) to see the program in action and understand what has been accomplished.

#### Running Tests
To run the automated tests, use the following command:
```sh
gradle runClientAuto -Phost=localhost -Pport=8000 -PregHost=localhost -PregPort=9002 -Pmessage="Hello" -PregOn=false
```

#### Explanation of `library.proto`
The `library.proto` file defines the protocol buffer schema for the Library service. It includes messages and service definitions for adding books, listing books, and other library-related operations. This schema is used to generate the gRPC code for the Library service, enabling clients and servers to communicate using the defined messages and methods.