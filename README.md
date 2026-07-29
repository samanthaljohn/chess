# ♕ Chess — A Multiplayer Chess Server & CLI Client

A full-stack multiplayer chess application written in Java: a chess rules engine built from scratch, a REST API server backed by MySQL, and a command-line client that communicates with it over HTTP. Developed for BYU's CS 240 (Advanced Software Construction), where each development phase was required to pass a comprehensive provided test suite before progressing to the next.

## Overview

- **Chess engine** (`shared/chess`) — board representation, legal move generation for every piece, and check/checkmate/stalemate detection, including castling and en passant. Fully covered by the test suite (`shared/src/test`).
- **REST API server** (`server`) — built with Spark, backed by MySQL for persistent users, auth tokens, and games, with an in-memory data access implementation available for local testing. Supports registration, login/logout, and game creation, listing, and joining, with request validation and structured error handling using custom exception classes.
- **Command-line client** (`client/ui/Repl`) — a REPL that communicates with the server through a `ServerFacade` HTTP client. Supports registering, logging in, creating and listing games, and joining or observing them.

**Current scope:** live in-game play over WebSockets is not yet implemented. Joining or observing a game currently renders only a generic starting board; move-by-move gameplay is the next phase of development.

## Design Notes

A few decisions worth highlighting:

- **Separation of concerns** across the `shared`, `server`, and `client` modules — the chess engine has no dependency on networking code, and the server has no dependency on client rendering logic.
- **Test coverage at every layer** — the chess engine, service layer, and API endpoints are all covered by JUnit tests, including a dedicated suite for edge-case rules such as castling and en passant.

## Architecture

The application is organized into three Maven modules:

- **`shared`** — chess rules and game state (`ChessBoard`, `ChessGame`, `ChessPiece`, move validation), along with request/response models shared by client and server.
- **`server`** — the Spark-based REST API: request handlers for auth, user, and game endpoints, a service layer, and a `DataAccess` layer with both MySQL and in-memory implementations.
- **`client`** — the CLI: a `ServerFacade` for HTTP communication with the server, and a `Repl` that drives the interactive session.

[Sequence diagram of the full client/server flow](https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=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-1ASUa4N6hnIMRgc2DwjRE9OxpY3VsY8-SarHs0ao41e7V8TdW1Uc8AAT8Az1nUTMmRsy4Wx+zybx9ddYAtNmC4pxxEDVnNqHGDdT44KUpW8f5hs-oVxrn3oEk02AtDvzDVRjRs4CtFqDLKK9l7hquaVVTfLhXDCebq++dz2S0mmdtUy7m9RSPEaDCUspzKK3UmWf8eLA5YFQheRuJBesAheCPSOsdS3-SIDuLAYA2AD2EDyAUX9RCZMkMds7V29ljB-qZmWqka7+XcDwCKh7UAJXhuoxZ68rWmNUw23gYs3ov48aTcCTb-GgfMZB39lrPH2tScHnm3rkOXsluG+WsLoDxsUSbVN+WLygA)

## Running Locally

```sh
mvn package
mvn -pl server exec:java   # starts the server on port 8080
mvn -pl client exec:java   # starts the CLI client
```

Or, after building an uber jar:

```sh
java -jar client/target/client-jar-with-dependencies.jar
```

Additional Maven commands:

| Command                    | Description                              |
| --------------------------- | ---------------------------------------- |
| `mvn compile`               | Build the code                           |
| `mvn test`                  | Run all tests                            |
| `mvn -pl shared test`       | Run chess engine tests only              |
| `mvn package -DskipTests`   | Build an uber jar without running tests  |

## Roadmap

Real-time gameplay over WebSockets: moves, resignations, and live board updates pushed to both players and observers as they occur.