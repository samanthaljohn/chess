package ui;

import client.ResponseException;
import client.ServerFacade;
import model.PublicGameData;
import result.CreateGameResult;
import result.ListGamesResult;
import result.LoginResult;
import result.RegisterResult;

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

    //private
    private void printUserPrompt(String status){
        System.out.print(SET_TEXT_COLOR_LIGHT_GREY);
        System.out.print("[" + status + "] " + ">>> ");
        System.out.print(SET_TEXT_COLOR_WHITE);
    }

    private void printPreloginHelpMenu(){
        System.out.println("\tregister <USERNAME> <PASSWORD> <EMAIL> - to create an account");
        System.out.println("\tlogin <USERNAME> <PASSWORD> - to play chess");
        System.out.println("\tquit - playing chess");
        System.out.println("\thelp - with possible commands");
    }

    private void printPostloginHelpMenu(){
        System.out.println("\tcreate <NAME>");
        System.out.println("\tlist - games");
        System.out.println("\tjoin <ID> [WHITE][BLACK] - a game");
        System.out.println("\tobserve <ID> - a game");
        System.out.println("\tlogout - when you are done");
        System.out.println("\tquit - playing chess");
        System.out.println("\thelp - with possible commands");
    }

    private void printGamePlayHelpMenu(){
        System.out.println("Sorry - game play/observation is currently unsupported.");
        System.out.println("Come back soon to play/observe!");
        System.out.println("\tquit - return to previous menu");
    }

    private void printListGames(Collection<PublicGameData> games){
        gameIDs.clear();

        if (games.isEmpty()){
            System.out.println("No games found. When you create a game, it will show up here.");
            return;
        }
        String format = "%-12s %-15s %-25s %-25s%n";
        System.out.printf(format, "Game ID", "Game Name", "White Player Username", "Black Player Username");

        int gameNum = 1;

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
    }

    private void drawChessBoard(int gameID){
        System.out.println("INSERT SOME REPRESENTATION OF A CHESS BOARD HERE");
    }

    public void preLoginRepl(){
        System.out.println("Welcome to 240 Chess! Type help to get started.");

        while (true){
            printUserPrompt("LOGGED_OUT");

            String line = scanner.nextLine();
            var info = line.split(" ");

            String command = info[0].toLowerCase();

            if (command.equals("register")){
                if (info.length != 4){
                    System.out.println("Please provide a username, password, and email to register an account.");
                    continue;
                }

                String username = info[1];
                String password = info[2];
                String email = info[3];

                try {
                    RegisterResult registerResult = facade.register(username, password, email);
                    authToken = registerResult.authToken();

                    System.out.print("Successfully registered!\n");
                    System.out.print("Logged in as " + username + "\n");

                    postLoginRepl();
                } catch (ResponseException e) {
                    System.out.println(e.getMessage());
                } catch (Exception e){
                    System.out.println("Something went wrong here. Try again.");
                }

            } else if (command.equals("login")){
                if (info.length != 3){
                    System.out.println("Please provide a username and password to login to an existing account.");
                    continue;
                }

                String username = info[1];
                String password = info[2];

                try {
                    LoginResult loginResult = facade.login(username, password);
                    authToken = loginResult.authToken();

                    System.out.print("Welcome back " + username + "!\n");

                    postLoginRepl();
                } catch (ResponseException e){
                    System.out.println(e.getMessage());
                } catch (Exception e){
                    System.out.println("Something went wrong here. Try again.");
                }

            } else if (command.equals("quit")){
                System.out.println("Are you sure you want to quit the application? <y/n>");
                String answer = scanner.nextLine();
                if (answer.equalsIgnoreCase("y")){
                    System.out.println("Thanks for playing!");
                    break;
                }
            } else if (command.equals("help")){
                printPreloginHelpMenu();
            } else if (command.equals("clear")){
                try {
                    facade.clear();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            } else {
                System.out.println("Please choose a valid option.");
            }
        }
    }

    public void postLoginRepl(){
        printPostloginHelpMenu();

        while (true){
            printUserPrompt("LOGGED_IN");

            String line = scanner.nextLine();
            var info = line.split(" ");

            String command = info[0].toLowerCase();

            if (command.equals("create")){
                if (info.length != 2){
                    System.out.println("Please provide a game name to create a new game. Use underscores instead of spaces (ex. my_chess_game).");
                    continue;
                }

                String gameName = info[1] ;

                try {
                    facade.createGame(authToken, gameName);
                    System.out.println("Successfully created game: " + gameName);
                } catch (ResponseException e){
                    System.out.println(e.getMessage());
                } catch (Exception e){
                    System.out.println("Something went wrong here. Try again.");
                }
            } else if (command.equals("list")){
                if (info.length != 1){
                    System.out.println("Type list to list games.");
                    continue;
                }

                try {
                    ListGamesResult listGamesResult = facade.listGames(authToken);
                    printListGames(listGamesResult.games());
                } catch (ResponseException e){
                    System.out.print(e.getMessage());
                } catch (Exception e){
                    System.out.println("Something went wrong here. Try again.");
                }
            } else if (command.equals("join")){
                if (info.length != 3){
                    System.out.println("Please provide the game ID and player color for the game you would like to join.");
                    continue;
                }

                int gameNum;
                try {
                    gameNum = Integer.parseInt(info[1]);
                } catch (Exception e){
                    System.out.println("Game ID not valid.");
                    continue;
                }
                Integer gameID = gameIDs.get(gameNum);
                if (gameID == null){
                    System.out.println("Game " + gameNum + " does not exist.");
                    continue;
                }

                String playerColor = info[2].toUpperCase();

                try {
                    facade.joinGame(authToken, playerColor, gameID);
                    System.out.println("Successfully joined game " + gameNum + " as " + playerColor.toLowerCase() + "\n");
                    drawChessBoard(gameID);
                    gamePlay();
                } catch (ResponseException e){
                    System.out.println(e.getMessage());
                } catch (Exception e){
                    System.out.println("Something went wrong here. Try again.");
                }

            } else if (command.equals("observe")){
                if (info.length != 2){
                    System.out.println("Please specify the game ID of the game you would like to observe.");
                    continue;
                }

                int gameNum;
                try {
                    gameNum = Integer.parseInt(info[1]);
                } catch (Exception e){
                    System.out.println("Game ID not valid.");
                    continue;
                }
                Integer gameID = gameIDs.get(gameNum);
                if (gameID == null){
                    System.out.println("Game " + gameNum + " does not exist.");
                    continue;
                }

                drawChessBoard(gameID);
                gamePlay();

            } else if (command.equals("logout")){
                System.out.println("Are you sure you would like to logout (and return to the login menu)? <y/n>");
                String answer = scanner.nextLine();

                if (answer.equalsIgnoreCase("y")){
                    authToken = null;
                    printPreloginHelpMenu();
                    return;
                }

            } else if (command.equals("quit")){
                System.out.println("Are you sure you want to quit playing chess (and logout)? <y/n>");
                String answer = scanner.nextLine();
                if (answer.equalsIgnoreCase("y")){
                    printPreloginHelpMenu();
                    return;
                }
            } else if (command.equals("help")){
                printPostloginHelpMenu();
            } else {
                System.out.println("Please choose a valid option.");
            }
        }
    }

    public void gamePlay(){
        printGamePlayHelpMenu();

        while (true){
            printUserPrompt("IN_GAME");

            String line = scanner.nextLine();
            var info = line.split(" ");


            String command = info[0].toLowerCase();

            if (command.equals("quit")){
                System.out.println("Are you sure you want to quit playing/observing this chess game (and return to the previous menu)? <y/n>");
                String answer = scanner.nextLine();

                if (answer.equalsIgnoreCase("y")){
                    printPostloginHelpMenu();
                    return;
                }
            } else {
                System.out.println("Please choose a valid option.");
            }
        }
    }

    public static void main(String[] args){
        Repl repl = new Repl(8080);
        repl.preLoginRepl();
    }
}
