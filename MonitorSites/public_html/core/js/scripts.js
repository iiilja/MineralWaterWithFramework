(function (){
    var app = angular.module('promoboxAdminApp', []);
    
    app.controller('userControl',[ '$http', function($http){
        this.page = 0;

        this.selectLink = function(link) {
            this.page = link;
        };
        
        this.pageSelected= function(selected) {
          return this.page === selected;  
        };
        
//        var lenguage = this;
//        $http.get('/json/en.json').seccess(function (data){
//        lenguage.text = data;
//        });
    }]);
    
    app.directive('loginForm', function(){
        return {
          restrict: 'E',
          templateUrl: 'login-form.html',
          controller: function() {
              this.text = lenguage.login_form;
          },
          controllerAs: 'login'
        };
    });
    
    app.directive('registerForm', function(){
        return {
          restrict: 'E',
          templateUrl: 'register-form.html',
          controller: function() {
              this.text = lenguage.registration_form;
          },
          controllerAs: 'register'
        };
    });
    
    var lenguage = {
        'login_form': {
            'heder': "PromoBox Login",
            'email': "Email",
            'password': "Password",
            'remember': "Remember me",
            'sign_in': "Sign In",
            'register': "Register"
        },
        'registration_form': {
            'heder': "PromoBox Registration",
            'firstname': "Firstname",
            'lastname': "Lastname",
            'company': "Company",
            'email': "Email",
            'remember': "Remember me",
            'register': "Register",
            'sign_in': "Sign In"
        }
    };
    
})();