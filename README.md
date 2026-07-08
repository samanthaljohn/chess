# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[Seqeunce Diagram](https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=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-1ASUa4N6hnIMRgc2DwjRE9OxpY3VsY8-SarHs0ao41e7V8TdW1Uc8AAT8Az1nUTMmRsy4Wx+zybx9ddYAtNmC4pxxEDVnNqHGDdT44KUpW8f5hs-oVxrn3oEk02AtDvzDVRjRs4CtFqDLKK9l7hquaVVTfLhXDCebq++dz2S0mmdtUy7m9RSPEaDCUspzKK3UmWf8eLA5YFQheRuJBesAheCPSOsdS3-SIDuLAYA2AD2EDyAUX9RCZMkMds7V29ljB-qZmWqka7+XcDwCKh7UAJXhuoxZ68rWmNUw23gYs3ov48aTcCTb-GgfMZB39lrPH2tScHnm3rkOXsluG+WsLoDxsUSbVN+WLygA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```
