var apiEndpoint = "http://api.dev.promobox.ee/service/";

var app = angular.module('promobox', ['ngRoute', 'ui.bootstrap', 'pascalprecht.translate', 'promobox.services', 'angularFileUpload']);


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
        .when('/exit', {
            controller: 'Exit',
            template: ''
        })
        .otherwise({redirectTo: '/'});
}]);


app.factory('AuthInterceptor', ['$q', 'token' ,function ($q, token) {
    return {
        responseError: function (response) {
            if(response.status === 401){
                token.remove();
                return $q.reject(response);
            } else {
                return $q.reject(response);
            }
        }
    };
}]);

app.config(function ($httpProvider) {
    $httpProvider.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded; charset=UTF-8';
    $httpProvider.defaults.headers.put['Content-Type'] = 'application/x-www-form-urlencoded; charset=UTF-8';
    $httpProvider.defaults.headers.common['x-requested-with'] = 'promobox.ee';

    $httpProvider.interceptors.push([
        '$injector',
        function ($injector) {
            return $injector.get('AuthInterceptor');
        }
    ]);

});

app.config(function ($translateProvider) {
    $translateProvider.useStaticFilesLoader({
        prefix: '/json/',
        suffix: '.json'
    });

    $translateProvider.preferredLanguage('en');

});



app.controller('Exit', ['$scope', '$location', '$http', 'token',
    function ($scope, $location, $http, token) {
        token.remove();
    }]);

app.controller('LoginController', ['$scope', '$location', '$http', 'token',
    function ($scope, $location, $http, token) {

        if (!token.check()) {

            $scope.login_form = {email: '', password: '', remember: false};

            $scope.login = function () {
                $http.post(apiEndpoint + "user/login",
                    $.param({
                        email: $scope.login_form.email,
                        password: $scope.login_form.password
                    }))
                    .success(function (data) {
                        if (data.response == 'OK') {
                            token.put(data.token);
                            $location.path('/main/');
                        }
                    });
            };
        } else {
            $location.path('/main/');
        }
    }]);

app.controller('RegistrationController', ['$scope', '$location', '$http', 'token',
    function ($scope, $location, $http, token) {
        console.log(!token.check());
        if (!token.check()) {

        } else {
            $location.path('/');
        }
    }]);

app.controller('CampaignEditController', ['$scope', '$routeParams', 'token', 'Campaign', '$upload', '$location', '$http',
    function ($scope, $routeParams, token, Campaign, $upload, $location, $http) {

        $scope.campaign = Campaign.get({id: $routeParams.cId, token: token.get()}, function (response) {
            $scope.campaign = response;
            $scope.campaign_form = {campaign_name: $scope.campaign.name, campaign_time: $scope.campaign.duration, campaign_order: $scope.campaign.sequence, campaign_start: $scope.campaign.start, campaign_finish: $scope.campaign.finish};
        });

        $scope.onFileSelect = function ($files) {

            for (var i = 0; i < $files.length; i++) {
                var file = $files[i];

                $scope.upload = $upload.upload({
                    url: apiEndpoint + 'token/' + token.get() + '/campaigns/' + $scope.campaign.id + '/files/', //upload.php script, node.js route, or servlet url
                    method: 'POST',
                    file: file
                }).progress(function (evt) {
                    console.log('percent: ' + parseInt(100.0 * evt.loaded / evt.total));
                }).success(function (data, status, headers, config) {
                    console.log(data);
                });

            }

        };

        $scope.edit_company = function () {
            $http.put(apiEndpoint + "token/" + token.get() + "/campaigns/" + $scope.campaign.id,
                {
                    "status": "0",
                    "sequence": $scope.campaign_form.campaign_order,
                    "start": $scope.campaign_form.campaign_start,
                    "finish": $scope.campaign_form.campaign_finish,
                    "duration": $scope.campaign_form.campaign_time})
                .success(function (data) {
                    if (data.response == 'OK') {
                        console.log(data);
                        $location.path('/main/');
                    }
                });
        };


    }]);

app.controller('DatepickerCtrl', ['$scope',
    function ($scope) {
        $scope.today = function () {
            $scope.campaign_form.campaign_start = new Date();
            $scope.campaign_form.campaign_finish = new Date();
        };
//        $scope.today();

        $scope.clear = function () {
            $scope.campaign_form.campaign_start = null;
            $scope.campaign_form.campaign_finish = null;
        };

        // Disable weekend selection
        $scope.disabled = function (date, mode) {
            return ( mode === 'day' && ( date.getDay() === 0 || date.getDay() === 6 ) );
        };

        $scope.toggleMin = function () {
            $scope.minDate = $scope.minDate ? null : new Date();
        };
        $scope.toggleMin();

        $scope.open = function ($event) {
            $event.preventDefault();
            $event.stopPropagation();

            $scope.opened = true;
        };

        $scope.dateOptions = {
            formatYear: 'yy',
            startingDay: 1
        };

        $scope.initDate = new Date('2016-15-20');
        $scope.formats = ['dd-MMMM-yyyy', 'yyyy/MM/dd', 'dd.MM.yyyy', 'shortDate'];
        $scope.format = $scope.formats[2];
    }]);

app.controller('MainController', ['$scope', '$location', '$http', 'token', 'Campaign',
    function ($scope, $location, $http, token, Campaign) {
        if (token.check()) {

            $scope.token = token.get();

            $scope.remove = function (campaign) {
                $scope.campaigns.splice($scope.campaigns.indexOf(campaign), 1);
            };

            Campaign.all({token: token.get()}, function (response) {
                if (response.response == 'OK') {
                    $scope.campaigns = response.campaigns;
                }
            });
        }

    }]);
