(function (){
    var app = angular.module('promoboxAdminApp', []);

    app.controller('userControl', function(){
        this.text = lenguage;
    });
    
    var lenguage = {
        login_form: {
            heder: "PromoBox Login",
            email: "Email",
            password: "Password",
            remember: "Remember me",
            sign_in: "Sign In",
            register: "Register",
        },
        registration_form: {
            heder: "PromoBox Registration",
            firstname: "Firstname",
            lastname: "Lastname",
            company: "Company",
            email: "Email",
            remember: "Remember me",
            register: "Register",
            sign_in: "Sign In",
        }
    };
    
})();