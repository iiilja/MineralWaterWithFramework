var apiEndpoint = "http://api.dev.promobox.ee/service/";

var app = angular.module('promobox', ['ngRoute', 'ui.bootstrap', 'pascalprecht.translate', 'promobox.services']);

app.value('token', {value: ''});

app.config(['$routeProvider', function ($routeProvider) {
    $routeProvider
        .when('/', {
            controller: 'LoginController',
            templateUrl: '/views/login.html'
        })
        .when('/main', {
            controller: 'MainController',
            templateUrl: '/views/main.html'
        }).otherwise({redirectTo: '/'});
}]);

app.config(function ($httpProvider) {
    $httpProvider.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded; charset=UTF-8';
});

app.config(function ($translateProvider) {
    $translateProvider.useStaticFilesLoader({
        prefix: '/json/',
        suffix: '.json'
    });

    $translateProvider.preferredLanguage('en');

});


app.controller('LoginController', ['$scope', '$location', '$http', 'token',
    function ($scope, $location, $http, token) {

        $scope.login_form = {email: '', password: '', remember: false};

        $scope.login = function () {
            $http.post(apiEndpoint + "user/login",
                $.param({
                    email: $scope.login_form.email,
                    password: $scope.login_form.password
                }))
                .success(function (data) {
                    token.value = data.token;
                    console.log("Login success: " + token.value);
                    $location.path('/main/');
                });
        };
    }]);


app.controller('MainController', ['$scope', '$location', 'token',
    function ($scope, $location, token) {
        $scope.token = token.value;
        console.log("Main token: " + token.value);
    }]);
