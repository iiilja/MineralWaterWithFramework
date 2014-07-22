var app = angular.module('promobox', ['ngRoute', 'ui.bootstrap', 'pascalprecht.translate', 'promobox.services']);

app.config(['$routeProvider', function ($routeProvider) {
    $routeProvider
        .when('/', {
            controller: 'LoginController',
            templateUrl: '/views/login.html'
        })
        .when('/main', {
            controller: 'MainController',
            resolve: {
                campaigns: function (MultiCampgaignLoader) {
                    return MultiCampgaignLoader();
                }
            },
            templateUrl: '/views/main.html'
        }).otherwise({redirectTo: '/'});
}]);

app.config(function($translateProvider) {
    $translateProvider.useStaticFilesLoader({
        prefix: '/json/',
        suffix: '.json'
    });

    $translateProvider.preferredLanguage('en');

});


app.controller('LoginController', ['$scope', '$location',
    function ($scope, $location) {
        $scope.login_form = {email:'', password:'', remember:false};

        $scope.login = function () {
            $location.path('/main/');
        };
    }]);


app.controller('MainController', ['$scope', '$location', 'campaigns',
    function ($scope, $location, campaigns) {

    }]);
