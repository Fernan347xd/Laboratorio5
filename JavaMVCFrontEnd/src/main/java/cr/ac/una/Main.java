package cr.ac.una;

import Presentation.Controllers.LoginController;
import Presentation.Views.LoginView;
import Services.AuthService;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args){

            LoginView loginView = new LoginView();
            AuthService authService = new AuthService("localhost", 7000);
            LoginController loginController = new LoginController(loginView, authService);
            loginController.addObserver(loginView);

            loginView.setVisible(true);
    }
}

