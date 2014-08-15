var apiEndpoint = "http://api.dev.promobox.ee/service/";

var app = angular.module('promobox', ['ngRoute', 'ui.bootstrap', 'pascalprecht.translate', 'promobox.services', 'angularFileUpload', 'toaster', 'ui.router']);


app.config(['$routeProvider','$stateProvider','$urlRouterProvider', function ($routeProvider, $stateProvider, $urlRouterProvider) {
    $urlRouterProvider.otherwise('/');
    $stateProvider
        .state('login', {
            url: "/",
            views: {
                "contentView": { controller: 'LoginController',templateUrl: '/views/login.html' }
            }
        })
        .state('registration', {
            url: "/registration",
            views: {
                "contentView": { controller: 'RegistrationController',templateUrl: '/views/register.html' }
            }
        })
        .state('list', {
            url: "/list",
            views: {
                "topView": { controller: 'TopMenuController',templateUrl: '/views/top_menu.html' },
                "contentView": { controller: 'CampaignsController',templateUrl: '/views/list.html' }
            }
        })
        .state('campaign_edit', {
            url: "/campaign/edit/:cId",
            views: {
                "contentView": { controller: "CampaignEditController", templateUrl: '/views/campaign_edit.html' }
            }
        })
        .state('device', {
            url: "/device",
            views: {
                "topView": { controller: 'TopMenuController',templateUrl: '/views/top_menu.html' },
                "contentView": { controller: 'DevicesController',templateUrl: '/views/device.html' }
            }
        })
        .state('campaign_new', {
            url: "/campaign/new",
            views: {
                "contentView": { controller: 'CampaignNewController', template: '' }
            }
        })
        .state('exit', {
            url: "/exit",
            views: {
                "contentView": { controller: 'Exit', template: '' }
            }
        });
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

app.controller('Exit', ['token',
    function (token) {
        token.remove();
    }]);

app.controller('TopMenuController', ['$scope', '$location', '$http', 'token', '$rootScope',
    function ($scope, $location, $http, token, $rootScope) {
        $rootScope.bodyClass = 'content_bg';
        $rootScope.top_link_active_list = '';
        $rootScope.top_link_active_device = '';
    }]);

//Update When Create new Design
app.controller('LoginController', ['$scope', '$location', '$http', 'token', '$rootScope',
    function ($scope, $location, $http, token, $rootScope) {
        $rootScope.bodyClass = 'main_bg';
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
                            $location.path('/list');
                        }
                    });
            };
        } else {
            $location.path('/list');
        }
    }]);

//Update When Create new Design
app.controller('RegistrationController', ['$scope', '$http', 'token', 'sysLocation', '$rootScope',
    function ($scope, $http, token, sysLocation, $rootScope) {
        $rootScope.bodyClass = 'main_bg';
    }]);

app.controller('CampaignNewController', ['token', 'Campaign', 'sysLocation',
    function (token, Campaign, sysLocation) {
        Campaign.create_new_campaignts({token: token.get()}, function(response){
            sysLocation.goLink('/campaign/edit/' + response.id)
        });
    }]);

app.controller('CampaignEditController', ['$scope', '$stateParams', 'token', 'Campaign', '$upload', '$location', '$http', 'toaster', 'Files','sysMessage', 'sysLocation',
    function ($scope, $stateParams, token, Campaign, $upload, $location, $http, toaster, Files, sysMessage, sysLocation) {
        $scope.filesArray = [];
        Campaign.get_campaigns({token: token.get(), id: $stateParams.cId}, function (response) {
            $scope.campaign = response;
            $scope.campaign_form = {campaign_status: $scope.campaign.status, filesArray: $scope.campaign.files, campaign_name: $scope.campaign.name, campaign_time: $scope.campaign.duration, campaign_order: $scope.campaign.sequence, campaign_start: $scope.campaign.start, campaign_finish: $scope.campaign.finish};
        });
        $scope.inArchive = function (id) {
            Files.arhiveFiles({token: token.get(), id: id}, function(response){
                sysMessage.delete_s('Файл удалён')
                refreshFilesModel();
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
            Files.getFiles({id: $stateParams.cId, token: token.get()}, function (response) {
                Campaign.get_campaigns({token: token.get(), id: $stateParams.cId}, function (response) {
                    $scope.files = response;
                    $scope.campaign_form.filesArray = $scope.files.files;
                });
            });
        };
        var dataToTime = function(data) {
            return new Date(data).getTime() + 15*60*1000;
        };
        $scope.edit_company = function () {
            Campaign.edit_campaigns({token: token.get(), id: $scope.campaign.id, status: $scope.campaign_form.campaign_status, name: $scope.campaign_form.campaign_name, sequence: $scope.campaign_form.campaign_order, start: dataToTime($scope.campaign_form.campaign_start), finish: dataToTime($scope.campaign_form.campaign_finish), duration: $scope.campaign_form.campaign_time}, function(response){
                sysLocation.goList();
            });
        };

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

app.controller('CampaignsController', ['$scope', 'token', 'Campaign', 'sysMessage', '$rootScope',
    function ($scope, token, Campaign, sysMessage, $rootScope) {
        if (token.check()) {
            $rootScope.top_link_active_list = 'top_link_active';
            Campaign.get_all_campaigns({token: token.get()}, function (response) {
                $scope.campaigns = response.campaigns;
            });
            $scope.remove = function (campaign) {
                $scope.campaigns.splice($scope.campaigns.indexOf(campaign), 1);
                Campaign.delete_campaigns({token: token.get(), id: campaign.id}, function (response) {
                    Campaign.get_all_campaigns({token: token.get()}, function (response) {
                        $scope.campaigns = response.campaigns;
                        sysMessage.delete_s('Кампания ' + campaign.name + ' удаленна');
                    });
                });
            };
        }
    }]);

app.controller('DevicesController', ['$scope', 'token', 'Device', 'sysMessage', '$rootScope',
    function ($scope, token, Device, sysMessage, $rootScope) {
        if (token.check()) {
            $rootScope.top_link_active_device = 'top_link_active';
            Device.get_data({token: token.get()}, function (response) {
                $scope.devices = response.devices;
            });
            $scope.change_device = function(device) {
                Device.update({token: token.get(), id: device.id, orientation: parseInt(device.orientation), resolution: parseInt(device.resolution), campaignId: parseInt(device.campaignId) }, function (response) {
                    sysMessage.update_s('Устройство ' + device.uuid + ' обновленно');
                });
            }
        }
    }]);