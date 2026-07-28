package ui;

import chess.ChessBoard;
import client.ResponseException;
import client.ServerFacade;
import model.PublicGameData;
import result.ListGamesResult;
import result.LoginResult;
import result.RegisterResult;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Repl {
    private final Scanner scanner;
    private final ServerFacade facade;
    private String authToken;
    private HashMap<Integer, Integer> gameIDs;

    public Repl(int port){
        this.scanner = new Scanner(System.in);
        this.facade = new ServerFacade(port);

        this.authToken = null;
        this.gameIDs = new HashMap<>();
    }

    private void printBold(String message){
        System.out.print(SET_TEXT_BOLD);
        System.out.print(message);
        System.out.print(RESET_TEXT_BOLD_FAINT);
    }

    //private
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
        String[][] gamePlayMenu = {{"quit", "return to previous menu"}};

        System.out.println("Sorry - game play/observation is currently unsupported.");
        System.out.println("Come back soon to play/observe!");

        printFormattedMenu(gamePlayMenu);
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

    private void drawChessBoard(String playerColor){
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        ChessBoard board = new ChessBoard();
        board.resetBoard();

        DrawChessGame.drawChessBoard(out, board, playerColor);
    }

    public void preLoginRepl(){
        printBold(WHITE_QUEEN + "  Welcome to 240 Chess!  " + WHITE_QUEEN + "\n");
        preloginHelpMenu();

        while (true){
            printUserPrompt("LOGGED_OUT");

            String line = scanner.nextLine();
            var info = line.split(" ");

            String command = info[0].toLowerCase();

            if (command.equals("register")){
                if (info.length != 4){
                    printErrorReport("Please provide a username, password, and email to register an account.");
                    continue;
                }

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
                } catch (Exception e){
                    printErrorReport("Something went wrong here. Check your server connection and try again.");
                }

            } else if (command.equals("login")){
                if (info.length != 3){
                    printErrorReport("Please provide a username and password to login to an existing account.");
                    continue;
                }

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
                } catch (Exception e){
                    printErrorReport("Something went wrong here. Check your server connection and try again.");
                }

            } else if (command.equals("quit")){
                System.out.println("Are you sure you want to quit the application? <y/n>");
                String answer = scanner.nextLine();
                if (answer.equalsIgnoreCase("y")){
                    System.out.println("Thanks for playing!");
                    break;
                }
            } else if (command.equals("help")){
                preloginHelpMenu();
            } else if (command.equals("clear")){
                try {
                    facade.clear();
                } catch (Exception e) {}
            } else {
                printErrorReport("Please choose a valid option. Type help for option information.");
            }
        }
    }

    public boolean postLoginRepl(){
        postloginHelpMenu();

        boolean quitApplication = false;

        while (true){
            printUserPrompt("LOGGED_IN");

            String line = scanner.nextLine();
            var info = line.split(" ");

            String command = info[0].toLowerCase();

            if (command.equals("create")){
                if (info.length != 2){
                    printErrorReport("Please provide a game name to create a new game. Use underscores instead of spaces (ex. my_chess_game).");
                    continue;
                }

                String gameName = info[1] ;

                try {
                    facade.createGame(authToken, gameName);
                    printBold("Successfully created game: " + gameName + "\n");
                } catch (ResponseException e){
                    printErrorReport(e.getMessage());
                } catch (Exception e){
                    printErrorReport("Something went wrong here. Check your server connection and try again.");
                }
            } else if (command.equals("list")){
                if (info.length != 1){
                    printErrorReport("Type list to list games.");
                    continue;
                }

                try {
                    ListGamesResult listGamesResult = facade.listGames(authToken);
                    printListGames(listGamesResult.games());
                } catch (ResponseException e){
                    printErrorReport(e.getMessage());
                } catch (Exception e){
                    printErrorReport("Something went wrong here. Check your server connection and try again.");
                }
            } else if (command.equals("join")){
                if (info.length != 3){
                    printErrorReport("Please provide the game ID and player color for the game you would like to join.");
                    continue;
                }

                int gameNum;
                try {
                    gameNum = Integer.parseInt(info[1]);
                } catch (Exception e){
                    printErrorReport("Game ID not valid.");
                    continue;
                }
                Integer gameID = gameIDs.get(gameNum);
                if (gameID == null){
                    printErrorReport("Game " + gameNum + " does not exist.");
                    continue;
                }

                String playerColor = info[2].toUpperCase();

                try {
                    facade.joinGame(authToken, playerColor, gameID);
                    printBold("Successfully joined game " + gameNum + " as " + playerColor.toLowerCase() + "\n");
                    drawChessBoard(playerColor);
                    gamePlay();
                } catch (ResponseException e){
                    printErrorReport(e.getMessage());
                } catch (Exception e){
                    printErrorReport("\"Something went wrong here. Check your server connection and try again.\"");
                }

            } else if (command.equals("observe")){
                if (info.length != 2){
                    printErrorReport("Please specify the game ID of the game you would like to observe.");
                    continue;
                }

                int gameNum;
                try {
                    gameNum = Integer.parseInt(info[1]);
                } catch (Exception e){
                    printErrorReport("Game ID not valid.");
                    continue;
                }
                Integer gameID = gameIDs.get(gameNum);
                if (gameID == null){
                    printErrorReport("Game " + gameNum + " does not exist.");
                    continue;
                }

                drawChessBoard("WHITE");
                gamePlay();

            } else if (command.equals("logout")){
                System.out.println("Are you sure you would like to logout (and return to the previous menu)? <y/n>");
                String answer = scanner.nextLine();

                if (answer.equalsIgnoreCase("y")){
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
                System.out.println("Are you sure you want to quit (logout and quit the application)? <y/n>");
                String answer = scanner.nextLine();
                if (answer.equalsIgnoreCase("y")){
                    quitApplication = true;
                    return quitApplication;
                }
            } else if (command.equals("help")){
                postloginHelpMenu();
            } else {
                System.out.println("Please choose a valid option. Type help for option information.");
            }
        }
        return quitApplication;
    }

    public void gamePlay(){
        gamePlayHelpMenu();

        while (true){
            printUserPrompt("IN_GAME");

            String line = scanner.nextLine();
            var info = line.split(" ");


            String command = info[0].toLowerCase();

            if (command.equals("quit")){
                System.out.println("Are you sure you want to quit playing/observing this chess game (and return to the previous menu)? <y/n>");
                String answer = scanner.nextLine();

                if (answer.equalsIgnoreCase("y")){
                    postloginHelpMenu();
                    return;
                }
            } else {
                System.out.println("Please choose a valid option. Type help for option information.");
            }
        }
    }
}
