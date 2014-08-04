var apiEndpoint = "http://api.dev.promobox.ee/service/";

var app = angular.module('promobox', ['ngRoute', 'ui.bootstrap', 'pascalprecht.translate', 'promobox.services', 'angularFileUpload', 'toaster']);


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
        .when('/campaign/new', {
            controller: 'CampaignNewController',
            template: ''
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

app.controller('CampaignEditController', ['$scope', '$routeParams', 'token', 'Campaign', '$upload', '$location', '$http', 'Showfiles',
    function ($scope, $routeParams, token, Campaign, $upload, $location, $http, Showfiles) {

        $scope.filesArray = [];
        $scope.campaign = Campaign.get({id: $routeParams.cId, token: token.get()}, function (response) {
            $scope.campaign = response;
            $scope.campaign_form = {campaign_status: $scope.campaign.status, filesArray: $scope.campaign.files, campaign_name: $scope.campaign.name, campaign_time: $scope.campaign.duration, campaign_order: $scope.campaign.sequence, campaign_start: $scope.campaign.start, campaign_finish: $scope.campaign.finish};
        });

        $scope.inArchive = function (id) {
            $http.put(apiEndpoint + "token/" + token.get() + "/files/archive/" + id + "/")
                .success(function (data) {
                    if (data.response == 'OK') {
                        refreshFilesModel();
                    }
                });
        };

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
                    refreshFilesModel();
                });

            }

        };

        var refreshFilesModel = function () {
            $scope.files = Showfiles.get({id: $routeParams.cId, token: token.get()}, function (response) {
                $scope.files = response;
                $scope.campaign_form.filesArray = $scope.files.campaignfiles;
            });
        }

        var dataToTime = function(data) {
            return new Date(data).getTime() + 15*60*1000;
        }

        $scope.edit_company = function () {
            $http.put(apiEndpoint + "token/" + token.get() + "/campaigns/" + $scope.campaign.id,
                {
                    "status": $scope.campaign_form.campaign_status,
                    "name": $scope.campaign_form.campaign_name,
                    "sequence": $scope.campaign_form.campaign_order,
                    "start": dataToTime($scope.campaign_form.campaign_start),
                    "finish": dataToTime($scope.campaign_form.campaign_finish),
                    "duration": $scope.campaign_form.campaign_time})
                .success(function (data) {
                    if (data.response == 'OK') {
                        $location.path('/main/');
                    }
                });
        };




    }]);

app.controller('CampaignNewController', ['$scope', '$routeParams', 'token', '$location', '$http',
    function ($scope, $routeParams, token, $location, $http) {
        $http.post(apiEndpoint + "token/" + token.get() + "/campaigns/")
            .success(function (data) {
                    $location.path('/campaign/edit/' + data.id);
            });
    }]);

app.controller('DatepickerCtrl', ['$scope',
    function ($scope) {
        $scope.open = function ($event) {
            $event.preventDefault();
            $event.stopPropagation();

            $scope.opened = true;
        };

        $scope.dateOptions = {
            formatYear: 'yy',
            startingDay: 1
        };
    }]);

app.controller('MainController', ['$scope', '$location', '$http', 'token', 'Campaign', 'toaster', 'Device',
    function ($scope, $location, $http, token, Campaign, toaster, Device) {
        if (token.check()) {

            $scope.token = token.get();

            $scope.remove = function (campaign) {
                $scope.campaigns.splice($scope.campaigns.indexOf(campaign), 1);
                $http.delete(apiEndpoint + "token/" + token.get() + "/campaigns/" + campaign.id)
                    .success(function (data) {
                        toaster.pop('success', "Delete", campaign.name +  " deleted");
                    });
            };

            Campaign.all({token: token.get()}, function (response) {
                if (response.response == 'OK') {
                    $scope.campaigns = response.campaigns;
                }
            });

            $scope.devices = Device.get({token: token.get()}, function (response) {
                $scope.devices = response.devices;
                console.log(response.devices);
            });
        }

    }]);
