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
                    token.value = data.token;
                    console.log("Login success: " + token.value);
                    $location.path('/main/');
                });
        };
    }]);


app.controller('CampaignEditController', ['$scope', '$routeParams', '$http', 'token',
    function ($scope, $routeParams, $http, token) {

        $http.post(apiEndpoint + "campaign/data",
            $.param({
                'token': $scope.token,
                'id': $routeParams.cId
            }))
            .success(function (data) {
                $scope.campaign = data.campaign;
            });

    }]);


app.controller('MainController', ['$scope', '$location', '$http', 'token',
    function ($scope, $location, $http, token) {
        $scope.token = token.value;

        $scope.remove = function(campaign) {
            $scope.campaigns.splice($scope.campaigns.indexOf(campaign), 1);
        }

        $http.post(apiEndpoint + "user/data",
            $.param({
                'token': $scope.token
            }))
            .success(function (data) {
                $scope.campaigns = data.campaigns;
            });
    }]);
