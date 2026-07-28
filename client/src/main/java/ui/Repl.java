package ui;

import client.ResponseException;
import client.ServerFacade;
import result.CreateGameResult;
import result.ListGamesResult;
import result.LoginResult;
import result.RegisterResult;

import java.util.Scanner;

public class Repl {
    private Scanner scanner;
    private ServerFacade facade;
    private String authToken;

    public Repl(int port){
        this.scanner = new Scanner(System.in);
        this.facade = new ServerFacade(port);

        this.authToken = null;
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
        System.out.print("quit - playing chess");
        System.out.print("help - with possible commands");
        System.out.println();
    }

    public void preLoginRepl(){
        System.out.println("Welcome to 240 Chess! Type Help to get started.");

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
                if (answer.toLowerCase().equals("y")){
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
                System.out.println("Hmm... Seems like you didn't select one of the options, maybe try again?");
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
                }

                try {
                    ListGamesResult listGamesResult = facade.listGames(authToken);
                    System.out.print(listGamesResult);
                } catch (ResponseException e){
                    System.out.print(e.getMessage());
                } catch (Exception e){
                    System.out.println("Something went wrong here, try again later.");
                }
            } else if (command.equals("join")){

            } else if (command.equals("observe")){

            } else if (command.equals("logout")){

            } else if (command.equals("quit")){

            } else if (command.equals("help")){
                printPostloginHelpMenu();
            } else {
                System.out.println("Hmm... Seems like you didn't select one of the options, maybe try again?");
            }
        }
    }

    public void gameplay(){

    }

    public static void main(String[] args){
        Repl repl = new Repl(8080);
        repl.preLoginRepl();
    }
}
