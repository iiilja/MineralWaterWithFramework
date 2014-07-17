(function (){
    var app = angular.module('promoboxAdminApp', []);
    
    app.controller('userControl',[ '$http','$scope', function($http, $scope){
        $scope.page = 0;
        this.title = 'PromoBox';

        $scope.selectLink = function(link) {
            $scope.page = link;
        };
        
        this.pageSelected= function(selected) {
          return $scope.page === selected;  
        };
        
    }]);
    
    app.directive('loginForm', function(){
        return {
          restrict: 'E',
          templateUrl: 'login-form.html',
          controller: function($http,$scope) {
              var login = this;
              
              $http.get("http://localhost:8383/MonitorSites/core/json/en.json").success(function (s_data) {
                  login.text = s_data.login_form;
              }).error(function (e_data){
                  alert('Error');
              });
              
              $scope.submitLogin = function () {
                if ($scope.loginForm.$valid) {
                    var email = $scope.loginForm.email.$modelValue;
                    var password = $scope.loginForm.password.$modelValue;
                    var remember = $scope.loginForm.remember.$modelValue;
                    
                    $http.get("http://localhost:8383/MonitorSites/core/json/user_data.json").success(function (s_data) {
                        if(s_data.return) {
                            $scope.id = s_data.id;
                            $scope.login = s_data.login;
                            
                            $scope.selectLink(2);
                        }
                    }).error(function (e_data){
                        alert('Error');
                    });
		}
              };
          },
          controllerAs: 'login'
        };
    });
    
    app.directive('registerForm', function(){
        return {
          restrict: 'E',
          templateUrl: 'register-form.html',
          controller: function($http,$scope) {
              var register = this;
              
              $http.get("http://localhost:8383/MonitorSites/core/json/en.json").success(function (s_data) {
                  register.text = s_data.registration_form;
              }).error(function (e_data){
                  alert('Error');
              });
          },
          controllerAs: 'register'
        };
    });
    
    app.directive('mainpage', function(){
        return {
          restrict: 'E',
          templateUrl: 'mainpage.html',
          controller: function($http,$scope) {
              
          },
          controllerAs: 'mainpage'
        };
    });
    
})();