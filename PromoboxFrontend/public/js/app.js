var apiEndpoint = "http://api.dev.promobox.ee/service/";

var app = angular.module('promobox', ['ngRoute', 'ui.bootstrap', 'pascalprecht.translate', 'promobox.services']);


app.config(['$routeProvider', function ($routeProvider) {
    $routeProvider
        .when('/', {
            controller: 'LoginController',
            templateUrl: '/views/login.html'
        })
        .when('/registration', {
            controller: 'RegistrationController',
            templateUrl: '/views/register.html'
        })
        .when('/main', {
            controller: 'MainController',
            templateUrl: '/views/main.html'
        })
        .when('/campaign/edit/:cId', {
            controller: 'CampaignEditController',
            templateUrl: '/views/campaign_edit.html'
        })
        .otherwise({redirectTo: '/'});
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
                    token.put(data.token);
                    console.log(data);
                    console.log("Login success: " + token.get());
                    $location.path('/main/');
                });
        };
    }]);

app.controller('RegistrationController', ['$scope', '$location', '$http', 'token',
    function ($scope, $location, $http, token) {

    }]);

app.controller('CampaignEditController', ['$scope', '$routeParams', 'token', 'Campaign',
    function ($scope, $routeParams, token, Campaign) {
       token.check();

       $scope.campaign = Campaign.get({id: $routeParams.cId});

    }]);


app.controller('MainController', ['$scope', '$location', '$http', 'token', 'Campaign',
    function ($scope, $location, $http, token, Campaign) {
        if (token.check()) {

            $scope.token = token.get();

            $scope.remove = function (campaign) {
                $scope.campaigns.splice($scope.campaigns.indexOf(campaign), 1);
            };

            Campaign.all({token: token.get()},function (response) {
                $scope.campaigns = response.campaigns;
            });
        }

    }]);
