(function (){
    var app = angular.module('promoboxAdminApp', []);
    
    app.controller('userControl',[ '$http','$scope', function($http, $scope){
        $scope.page = this.page;
        $scope.page = 0;
        

        this.selectLink = function(link) {
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
              var language = this;
              $http.get("http://localhost:8383/MonitorSites/core/json/en.json").success(function (s_data) {
                  language.text = s_data.login_form;
              }).error(function (e_data){
                  alert('Error');
              });
          },
          controllerAs: 'login'
        };
    });
    
    app.directive('registerForm', function(){
        return {
          restrict: 'E',
          templateUrl: 'register-form.html',
          controller: function($http,$scope) {
              var language = this;
              $http.get("http://localhost:8383/MonitorSites/core/json/en.json").success(function (s_data) {
                  language.text = s_data.registration_form;
              }).error(function (e_data){
                  alert('Error');
              });
          },
          controllerAs: 'register'
        };
    });
    
})();