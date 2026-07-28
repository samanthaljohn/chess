package ui;

import client.ResponseException;
import client.ServerFacade;
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
        System.out.println("Welcome to 240 Chess! Type Help to get started.");
        System.out.println("register <USERNAME> <PASSWORD> <EMAIL> - to create an account");
        System.out.println("login <USERNAME> <PASSWORD> - to play chess");
        System.out.println("quit - playing chess");
        System.out.println("help - with possible commands");
    }

    public void preLoginRepl(){
        printPreloginHelpMenu();

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

                    System.out.print("Successfully registered!");
                    System.out.print("Logged in as " + username);

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

                    System.out.print("Logged in as " + username);

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
            } else {
                System.out.println("Hmm... Seems like you didn't select one of the options, maybe try again?");
            }
        }
    }

    public void postLoginRepl(){

    }

    public void gameplay(){

    }

    public void main(){
        preLoginRepl();
    }
}
