package ui;

import chess.ChessBoard;
import chess.ChessGame;
import client.ResponseException;
import client.ServerFacade;
import client.WebSocketFacade;
import model.PublicGameData;
import result.CreateGameResult;
import result.ListGamesResult;
import result.LoginResult;
import result.RegisterResult;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Repl implements NotificationHandler {
    private final Scanner scanner;
    private final ServerFacade facade;
    private final WebSocketFacade webFacade;
    private String authToken;
    private String currentPlayerColor;
    private ChessBoard currentBoard;
    private HashMap<Integer, Integer> gameIDs;

    public Repl(int port) throws ResponseException {
        this.scanner = new Scanner(System.in);
        this.facade = new ServerFacade(port);
        this.webFacade = new WebSocketFacade(port, this);

        this.authToken = null;
        this.currentPlayerColor = "";
        this.currentBoard = null;
        this.gameIDs = new HashMap<>();
    }

    private void printBold(String message){
        System.out.print(SET_TEXT_BOLD);
        System.out.print(message);
        System.out.print(RESET_TEXT_BOLD_FAINT);
    }

    private void printUserPrompt(String status){
        System.out.print(SET_TEXT_COLOR_LIGHT_GREY);
        System.out.print("[" + status + "] " + ">>> ");
        System.out.print(RESET_TEXT_COLOR);
    }

    private void printErrorReport(String message){
        System.out.print(SET_TEXT_ITALIC);
        System.out.println(message);
        System.out.print(RESET_TEXT_ITALIC);
    }

    private void printNotification(String message){
        System.out.print(SET_TEXT_ITALIC);
        System.out.print(SET_TEXT_COLOR_GREEN);
        System.out.println(message);
        System.out.print(RESET_TEXT_COLOR);
        System.out.print(RESET_TEXT_ITALIC);
    }

    private void printServerErrorReport(){
        printErrorReport("Something went wrong here. Check your server connection and try again.");
    }

    private void printInvalidOptionReport(){
        printErrorReport("Please choose a valid option. Type help for option information.");
    }

    private void printFormattedMenu(String[][] menu){
        for (String[] menuLine : menu){
            String command = menuLine[0];
            String description = menuLine[1];

            System.out.println("\t" + SET_TEXT_COLOR_PINK + command + RESET_TEXT_COLOR + " - " + SET_TEXT_COLOR_LIGHT_PINK + description);
        }
        System.out.print(RESET_TEXT_COLOR);
    }

    private void preloginHelpMenu(){
        String[][] preLoginMenu = {{"register <USERNAME> <PASSWORD> <EMAIL>", "to create an account"},
                {"login <USERNAME> <PASSWORD>", "to play chess"},
                {"quit", "the application"},
                {"help", "with possible commands"}};

        printFormattedMenu(preLoginMenu);
    }

    private void postloginHelpMenu(){
        String[][] postloginMenu = {{"create <NAME>", "a new game"},
            {"list", "games"},
            {"join <ID> [WHITE|BLACK]", "a game"},
            {"observe <ID>", "a game"},
            {"logout", "when you are done"},
            {"quit", "the application"},
            {"help", "with possible commands"}};

        printFormattedMenu(postloginMenu);
    }

    private void gamePlayHelpMenu(){
        String[][] gamePlayMenu = {{"move", "make a move in the current game"},
                {"redraw", "the current board state"},
                {"highlight", "legal moves"},
                {"resign", "and forfeit the current match"},
                {"leave", "the current game"},
                {"quit", "return to previous menu"},
                {"help", "with possible commands"}};

        printFormattedMenu(gamePlayMenu);
    }

    private boolean validateArgCount(String[] args, int expectedNum, String message){
        if (args.length != expectedNum){
            printErrorReport(message);
            return false;
        }
        return true;
    }

    private int validateGameID(String[] args){
        int gameNum;
        try {
            gameNum = Integer.parseInt(args[1]);
        } catch (Exception e){
            printErrorReport("Game ID not valid.");
            return -1;
        }
        Integer gameID = gameIDs.get(gameNum);
        if (gameID == null){
            printErrorReport("Game " + gameNum + " does not exist.");
            return -1;
        }
        return gameID;
    }

    private boolean quit(String message){
        System.out.println(message);
        String answer = scanner.nextLine();
        if (answer.equalsIgnoreCase("y")){
            System.out.println("Thanks for playing!");
            return true;
        }
        return false;
    }

    private void printListGames(Collection<PublicGameData> games){
        gameIDs.clear();

        if (games.isEmpty()){
            System.out.println("No games found. When you create a game, it will show up here.");
            return;
        }
        String format = "%-12s %-15s %-25s %-25s%n";
        System.out.print(SET_TEXT_BOLD);
        System.out.print(SET_TEXT_COLOR_PINK);
        System.out.printf(format, "Game ID", "Game Name", "White Player Username", "Black Player Username");

        int gameNum = 1;

        System.out.print(RESET_TEXT_BOLD_FAINT);
        System.out.print(RESET_TEXT_COLOR);
        for (PublicGameData game : games){
            String whiteUsername = game.whiteUsername();
            String blackUsername = game.blackUsername();

            if (whiteUsername == null) {
                whiteUsername = "---";
            }
            if (blackUsername == null){
                blackUsername = "---";
            }
            System.out.printf(format, gameNum, game.gameName(), whiteUsername, blackUsername);

            gameIDs.put(gameNum, game.gameID());
            gameNum++;
        }
        System.out.print(RESET_TEXT_COLOR);
    }

    private void drawChessBoard(ChessBoard board, String playerColor){
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        DrawChessGame.drawChessBoard(out, board, playerColor);
    }

    public void loadGame(LoadGameMessage message){
        ChessGame game = message.getGame();
        ChessBoard board = game.getBoard();
        currentBoard = board;

        drawChessBoard(currentBoard, currentPlayerColor.toUpperCase());
    }

    public void notificationMessage(NotificationMessage notification){
        String message = notification.getMessage();
        printNotification(message);
    }

    public void error(ErrorMessage errorMessage){
        String message = errorMessage.getErrorMessage();
        printErrorReport(message);
    }

    public void preLoginRepl(){
        printBold(WHITE_QUEEN + "  Welcome to 240 Chess!  " + WHITE_QUEEN + "\n");
        preloginHelpMenu();

        while (true){
            printUserPrompt("LOGGED_OUT");

            String line = scanner.nextLine().trim();
            var info = line.split("\\s+");

            String command = info[0].toLowerCase();

            if (command.equals("register")){
                if(!validateArgCount(info, 4, "Please provide a username, password, and email to register an account.")) { continue; }

                String username = info[1];
                String password = info[2];
                String email = info[3];

                try {
                    RegisterResult registerResult = facade.register(username, password, email);
                    authToken = registerResult.authToken();

                    printBold("Successfully registered!\n");
                    System.out.print("Logged in as " + username + "\n");

                    boolean quitApplication = postLoginRepl();
                    if (quitApplication == true){
                        System.out.println("Thanks for playing!");
                        break;
                    }
                } catch (ResponseException e) {
                    printErrorReport(e.getMessage());
                } catch (Exception e){ printServerErrorReport(); }

            } else if (command.equals("login")){
                if (!validateArgCount(info, 3, "Please provide a username and password to login to an existing account.")) { continue; }

                String username = info[1];
                String password = info[2];

                try {
                    LoginResult loginResult = facade.login(username, password);
                    authToken = loginResult.authToken();

                    printBold("Welcome back " + username + "!\n");

                    boolean quitApplication = postLoginRepl();
                    if (quitApplication == true){
                        System.out.println("Thanks for playing!");
                        break;
                    }
                } catch (ResponseException e){
                    printErrorReport(e.getMessage());
                } catch (Exception e){printServerErrorReport();}

            } else if (command.equals("quit")){
                if (quit("Are you sure you want to quit the application? <y/n>")) { break; }
            } else if (command.equals("help")){
                preloginHelpMenu();
            } else if (command.equals("clear")){
                try {
                    facade.clear();
                } catch (Exception e) {}
            } else { printInvalidOptionReport(); }
        }
    }

    public boolean postLoginRepl(){
        postloginHelpMenu();
        boolean quitApplication = false;

        while (true){
            printUserPrompt("LOGGED_IN");

            String line = scanner.nextLine().trim();
            var info = line.split("\\s+");

            String command = info[0].toLowerCase();

            if (command.equals("create")){
                if (!validateArgCount(info, 2, "Please provide a game name to create a new game. Use underscores instead of spaces.")) { continue; }
                try {
                    String gameName = info[1];

                    CreateGameResult createGameResult = facade.createGame(authToken, gameName);
                    int newGameNum = gameIDs.size() + 1;
                    gameIDs.put(newGameNum, createGameResult.gameID());
                    printBold("Successfully created game: " + gameName + "\n");
                } catch (ResponseException e){
                    printErrorReport(e.getMessage());
                } catch (Exception e){ printServerErrorReport(); }
            } else if (command.equals("list")){
                if (!validateArgCount(info, 1, "Type list to list games.")) { continue; }

                try {
                    ListGamesResult listGamesResult = facade.listGames(authToken);
                    printListGames(listGamesResult.games());
                } catch (ResponseException e){
                    printErrorReport(e.getMessage());
                } catch (Exception e){ printServerErrorReport();}
            } else if (command.equals("join")){
                if (!validateArgCount(info, 3, "Please provide the game ID and player color for the game you would like to join.")) { continue; }

                int gameID = validateGameID(info);
                if (gameID == -1) { continue; }

                try {
                    currentPlayerColor = info[2].toUpperCase();

                    facade.joinGame(authToken, currentPlayerColor, gameID);
                    printBold("Successfully joined game as " + currentPlayerColor.toLowerCase() + "\n");
                    webFacade.connect(authToken, gameID);
                    gamePlay(gameID);
                } catch (ResponseException e){
                    printErrorReport(e.getMessage());
                } catch (Exception e){ printServerErrorReport();}
            } else if (command.equals("observe")){
                if(!validateArgCount(info, 2, "Please specify the game ID of the game you would like to observe.")) { continue; }

                int gameID = validateGameID(info);
                if (gameID == -1) { continue; }

                try {
                    currentPlayerColor = "WHITE";
                    webFacade.connect(authToken, gameID);
                } catch (ResponseException e) {
                    printErrorReport(e.getMessage());
                }
                gamePlay(gameID);
            } else if (command.equals("logout")){
                if (quit("Are you sure you would like to logout (and return to the previous menu)? <y/n>")){
                    try {
                        facade.logout(authToken);
                        authToken = null;
                        preloginHelpMenu();
                        break;
                    } catch (ResponseException e){
                        printErrorReport(e.getMessage());
                    } catch (Exception e){
                        printErrorReport("Something went wrong here. Check your server connection and try again.");
                    }
                }
            } else if (command.equals("quit")){
                if (quit("Are you sure you want to quit (logout and quit the application)? <y/n>")){
                    quitApplication = true;
                    return quitApplication;
                }
            } else if (command.equals("help")){
                postloginHelpMenu();
            } else {printInvalidOptionReport();}
        }
        return quitApplication;
    }

    public void gamePlay(int gameID){
        gamePlayHelpMenu();

        while (true){
            printUserPrompt("IN_GAME");

            String line = scanner.nextLine().trim();
            var info = line.split("\\s+");

            String command = info[0].toLowerCase();
            if (command.equals("move")){

            }  else if (command.equals("redraw")){
                if(!validateArgCount(info, 1, "Type redraw to redraw the current chess board;")) { continue; }

                drawChessBoard(currentBoard, currentPlayerColor);
            } else if (command.equals("highlight")){

            } else if (command.equals("resign")) {
                if(!validateArgCount(info, 1, "Type resign to resign.")) { continue; }

                System.out.println("Are you sure you want to resign? (No more moves can be made. You may view the final board state until you leave). <y/n>");
                String answer = scanner.nextLine();

                if (answer.equalsIgnoreCase("y")) {
                    try {
                        webFacade.resign(authToken, gameID);
                    } catch (ResponseException e) {
                        printErrorReport(e.getMessage());
                    }
                }
            } else if (command.equals("leave")){
                if (!validateArgCount(info, 1, "Type leave to leave this chess game.")) { continue; }

                System.out.println("Are you sure you want to leave this chess game (and return to the previous menu)? <y/n>");
                String answer = scanner.nextLine();

                if (answer.equalsIgnoreCase("y")){
                    try {
                        webFacade.leave(authToken, gameID);
                        postloginHelpMenu();
                        return;
                    } catch (ResponseException e) {
                        printErrorReport(e.getMessage());
                    }
                }
            } else if (command.equals("help")){
                gamePlayHelpMenu();
            } else {printInvalidOptionReport(); }
        }
    }
}
