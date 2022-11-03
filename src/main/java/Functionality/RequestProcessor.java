package Functionality;

import Entities.Login;
import Entities.Request;
import Entities.Response;
import Entities.User;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Objects;

@ApplicationScoped
public class RequestProcessor {
    public HashMap<String, User> allUsers = new HashMap();
    public HashMap<String, Login> activeAccounts = new HashMap<>();

    @Incoming("requests") // Kafka topic this will "read" from
    @Outgoing("responses") // Kafka topic this will "write" to
    public Response takeRequest(Request request) throws InterruptedException {
        System.out.println("Request received in processor! \n");

        if (Objects.equals(request.id, "createAccount")) {
            return createUser(request);
        } else if (Objects.equals(request.id, "login")) {
            return login(request);
        } else if (Objects.equals(request.id, "logout")) {
            return logout(request);
        } else if (Objects.equals(request.id, "deleteAccount")) {
            return deleteAccount(request);
        }

        return new Response("ID Inputted: " + request.getId(), "ERROR: Bad request", "No function matches the " +
                "request id \"" + request.getId() + "\" \n\n Please try again, normal calls include (but are not limited to) " +
                "\"login\", \"logout\", \"createAccount\" ETC...");
    }

    public Response createUser(Request request) {
        System.out.println("Entering createUser!");

        // Separates "dataString" from the request, so we can find the username, password and email!
        String[] specData = request.getDataString().split(",");

        // IF username (account-name) is already occupied, inform user
        if (allUsers.containsKey(specData[1])) {
            return new Response(request.getId(), "Account creation failed", "This account-name is taken, " +
                    "please choose another one\nAlready have an account? Try logging in instead!");
        }

        // Otherwise create the new account
        else {
            User user = new User(specData[1], specData[3], specData[5]);

            allUsers.put(user.getUsername(), user);
            System.out.println("Added " + user.getUsername() + " to DB - PROCESSOR");

            return new Response(request.getId(), "SUCCESSFUL USER CREATION!", "Added user " + user.getUsername()
                    + " to database!");
        }
    }

    public Response login(Request request) {
        System.out.println("Entering login!");

        // Separates "dataString" from the request, so we can find the login username and password!
        String[] specData = request.getDataString().split(",");

        // IF user is not already logged in OR if the account does not exist, inform user of the issue
        if (activeAccounts.containsKey(specData[1]) || !allUsers.containsKey(specData[1])) {
            return new Response(request.getId(), "Login failed",
                    "This account is either already logged in OR it does not exist, verify inputs!");
        }


        // ELSE log the account in
        else {
            Login login = new Login(specData[1], specData[3]);
            activeAccounts.put(login.getUsername(), login);

            System.out.println("User " + login.getUsername() + " logged into system");
            return new Response(request.getId(), "Successful login!", "User " + login.getUsername() + " logged into system");
        }
    }

    public Response logout(Request request) {
        System.out.println("Entering logout!");

        // Separates "dataString" from the request, so we can find the username of the account to be logged out
        String[] specData = request.getDataString().split(",");
        String username = specData[1];

        // IF account is logged in (can be found in activeAccounts) Remove account from map and send appropriate response
        if (activeAccounts.containsKey(username)) {
            activeAccounts.remove(username);
            return new Response(request.getId(), "Logout successful", "User " + username + " was logged out from system!");
        }


        // ELSE inform the user that the account is not logged in
        else {
            return new Response(request.getId(), "Logout failed...", "This account is not currently logged into the system.");
        }
    }

    public Response deleteAccount(Request request) {
        System.out.println("Entering delete account!");

        // Separates "dataString" from the request, so we can find the username and password of the account to be deleted
        String[] specData = request.getDataString().split(",");
        String username = specData[1];
        String password = specData[3];

        //CASE 1) Ensures no null pointer exception is caused in next IF-case in case no account matches users inputted username
        if (allUsers.get(username) == null){
            return new Response(request.getId(), "Deletion unsuccessful...", "This account doesn't exist");
        }



        /*
        CASE 2) IF account exists (username can be found in allUsers) AND the password of said account is the same as users input,
        Remove the account from allUsers thereby deleting it permanently
        (This if-case also checks if the user was logged in during deletion, if they were, delete them from "activeAccounts" also)
        */
        if (Objects.equals(allUsers.get(username).getPassword(), password)) {
            allUsers.remove(username);

            // *********************** Removes user from activeAccounts IF user was logged in *************************
            if (activeAccounts.get(username) == null){
                System.out.println("No need to remove anything from activeAccounts, user " + username + " was NOT logged in");}
            else {activeAccounts.remove(username);}
            // ********************************************************************************************************


            return new Response(request.getId(), "Successful account deletion", "The former account " +
                    username + " has now been permanently removed from our system per your request");
        }



        // CASE 3) ELSE if account CAN be found but the password is incorrect, inform user of their bad grammar
        else {
            return new Response(request.getId(), "Deletion unsuccessful...",
                    "Account DOES exist BUT, your inputted password is incorrect. Double-check your inputs and try again");
        }
    }



}