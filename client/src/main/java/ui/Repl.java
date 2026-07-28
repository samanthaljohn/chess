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

    private void printPreloginHelpMenu(){
        System.out.println("register <USERNAME> <PASSWORD> <EMAIL> - to create an account");
        System.out.println("login <USERNAME> <PASSWORD> - to play chess");
        System.out.println("quit - playing chess");
        System.out.println("help - with possible commands");
        System.out.println();
    }

    private void printPostloginHelpMenu(){
        System.out.println("create <NAME>");
        System.out.println("list - games");
        System.out.println("join <ID> [WHITE][BLACK] - a game");
        System.out.println("observe <ID> - a game");
        System.out.println("logout - when you are done");
        System.out.println("quit - playing chess");
        System.out.println("help - with possible commands");
        System.out.println();
    }

    private void printGamePlayHelpMenu(){
        System.out.println("Sorry - game play/observation is currently unsupported.");
        System.out.println("Come back soon to play/observe!");
        System.out.println("quit - return to previous menu");
        System.out.println();
    }

    private void printListGames(Collection<PublicGameData> games){
        gameIDs.clear();

        if (games.isEmpty()){
            System.out.println("No games found to list.");
            return;
        }

        System.out.println("Game Number\tGame Name\tWhite Player\tBlack Player");

        int count = 1;
        for (PublicGameData game : games){
            String output = count + "\t" + game.gameName() + "\t";
            if (game.whiteUsername() != null){
                output += game.whiteUsername() + "\t";
            } else {
                output += "---\t";
            }

            if (game.blackUsername() != null){
                output += game.blackUsername();
            } else {
                output += "---";
            }
            System.out.println(output);

            gameIDs.put(count, game.gameID());
        }
    }

    private void drawChessBoard(int gameID){
        System.out.println("INSERT SOME REPRESENTATION OF A CHESS BOARD HERE");
    }

    public void preLoginRepl(){
        System.out.println("Welcome to 240 Chess! Type help to get started.");
        System.out.println();

        while (true){
            String line = scanner.nextLine();
            var info = line.split(" ");

            String command = info[0].toLowerCase();

            if (command.equals("register")){
                if (info.length != 4){
                    System.out.println("Incorrect number of arguments to register!");
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
                    System.out.println("Looks like something went wrong here... try again later");
                }

            } else if (command.equals("login")){
                if (info.length != 3){
                    System.out.println("Incorrect number of arguments to login!");
                    continue;
                }

                String username = info[1];
                String password = info[2];

                try {
                    LoginResult loginResult = facade.login(username, password);
                    authToken = loginResult.authToken();

                    System.out.print("Logged in as " + username + "\n");

                    postLoginRepl();
                } catch (ResponseException e){
                    System.out.println(e.getMessage());
                } catch (Exception e){
                    System.out.println("Looks like something went wrong here... try again later");
                }

            } else if (command.equals("quit")){
                System.out.println("Are you sure you want to quit? <y/n>");
                String answer = scanner.nextLine();
                if (answer.equalsIgnoreCase("y")){
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
            String line = scanner.nextLine();
            var info = line.split(" ");

            String command = info[0].toLowerCase();

            if (command.equals("create")){
                if (info.length != 2){
                    System.out.println("Incorrect number of arguments to create a game!");
                    continue;
                }

                String gameName = info[1] ;

                try {
                    CreateGameResult createGameResult = facade.createGame(authToken, gameName);
                    int gameID = createGameResult.gameID();
                    System.out.println("Created game: " + gameName + "!");
                    System.out.println("Type join <" + gameID + "> [WHITE|BLACK] to join" + gameName + ".");
                } catch (ResponseException e){
                    System.out.println(e.getMessage());
                } catch (Exception e){
                    System.out.println("Something went wrong. Try again later.");
                }
            } else if (command.equals("list")){
                if (info.length != 1){
                    System.out.println("Incorrect number of arguments to list games!");
                    continue;
                }

                try {
                    ListGamesResult listGamesResult = facade.listGames(authToken);
                    printListGames(listGamesResult.games());
                } catch (ResponseException e){
                    System.out.print(e.getMessage());
                } catch (Exception e){
                    System.out.println("Something went wrong here, try again.");
                }
            } else if (command.equals("join")){
                if (info.length != 3){
                    System.out.println("Please specify the game ID and player color for the game you would like to join.");
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
                    drawChessBoard(gameID);
                    gamePlay();
                } catch (ResponseException e){
                    System.out.println(e.getMessage());
                } catch (Exception e){
                    System.out.println("Something went wrong here, try again.");
                }

            } else if (command.equals("observe")){
                if (info.length != 2){
                    System.out.println("Please specify only the game ID of the game you would like to observe.");
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
                System.out.println("All done with chess for the day? <y/n>");
                String answer = scanner.nextLine();

                if (answer.equalsIgnoreCase("y")){
                    authToken = null;
                    return;
                }

            } else if (command.equals("quit")){
                System.out.println("Are you sure you want to quit (and return to the login menu)? <y/n>");
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
            String line = scanner.nextLine();
            var info = line.split(" ");


            String command = info[0].toLowerCase();

            if (command.equals("quit")){
                System.out.println("Are you sure you want to quit playing chess (and return to the previous menu)? <y/n>");
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
