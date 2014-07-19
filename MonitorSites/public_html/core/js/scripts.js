(function (){
    var app = angular.module('promoboxAdminApp', ['ngCookies','ui.bootstrap']);
    
    app.controller('userControl',[ '$http','$scope','$cookies', function($http, $scope, $cookies){
        $scope.page = 0;
        this.title = 'PromoBox';
        
        $scope.email = $cookies.email;
        $scope.password = $cookies.password;
        $scope.remember = $cookies.remember;
        
        console.log($scope.email + ' ' + $scope.password + ' ' + $scope.remember);
        


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
          controller: function($http,$scope,$cookies) {
              var login = this;
              
              $http.get("http://localhost:8383/MonitorSites/core/json/en.json").success(function (s_data) {
                  login.text = s_data.login_form;
              }).error(function (e_data){
                  alert('Error');
              });
              
              $scope.submitLogin = function () {
                if ($scope.loginForm.$valid) {
                    $cookies.email = $scope.loginForm.email.$modelValue;
                    $cookies.password = $scope.loginForm.password.$modelValue;
                    if($scope.loginForm.remember.$modelValue === undefined) {
                        $cookies.remember = "false";
                    } else {
                        $cookies.remember = $scope.loginForm.remember.$modelValue;
                    }
                    
                    $scope.getUserData();
		}
              };
              
              $scope.getUserData = function () {
                  $http.get("http://localhost:8383/MonitorSites/core/json/user_data.json").success(function (s_data) {
                        if(s_data.return) {
                            $scope.id = s_data.id;
                            $scope.login = s_data.login;
                            
                            $scope.selectLink(2);
                        }
                    }).error(function (e_data){
                        alert('Error');
                    });
              };
          },
          controllerAs: 'login'
        };
    });
    
    app.directive('registerForm', function(){
        return {
          restrict: 'E',
          templateUrl: 'register-form.html',
          controller: function($http,$scope,$cookies) {
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
          controller: function($http,$scope,$cookies) {
              var mainpage = this;
              
              $http.get("http://localhost:8383/MonitorSites/core/json/en.json").success(function (s_data) {
                  mainpage.text = s_data.mainpage;
              }).error(function (e_data){
                  alert('Error');
              });
          },
          controllerAs: 'mainpage'
        };
    });
    
    app.directive('companyView', function(){
        return {
          restrict: 'E',
          templateUrl: 'company-view.html',
          controller: function($http,$scope,$cookies) {
              var companyview = this;
              
              $http.get("http://localhost:8383/MonitorSites/core/json/company.json").success(function (s_data) {
                  console.log(s_data);
                  companyview.company_data = s_data;
              }).error(function (e_data){
                  alert('Error');
              });
          },
          controllerAs: 'companyview'
        };
    });
    
})();