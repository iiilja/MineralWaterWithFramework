var apiEndpoint = "http://api.dev.promobox.ee/service/";

var app = angular.module('promobox', ['ngRoute', 'ui.bootstrap', 'pascalprecht.translate', 'promobox.services', 'angularFileUpload', 'toaster', 'ui.router', 'angularMoment', 'ui.bootstrap.datetimepicker', 'checklist-model']);


app.config(['$routeProvider','$stateProvider','$urlRouterProvider', function ($routeProvider, $stateProvider, $urlRouterProvider) {
    $urlRouterProvider.otherwise('/');
    $stateProvider
        .state('login', {
            url: "/",
            views: {
                "topView": { controller: 'TopMenuController',templateUrl: '/views/top_clean_menu.html' },
                "contentView": { controller: 'LoginController',templateUrl: '/views/login.html' }
            }
        })
        .state('registration', {
            url: "/registration",
            views: {
                "topView": { controller: 'TopMenuController',templateUrl: '/views/top_clean_menu.html' },
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
                "topView": { controller: 'TopMenuController',templateUrl: '/views/top_menu.html' },
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

    $translateProvider.preferredLanguage('et');

});

app.filter('bytes', function() {
    return function(bytes, precision) {
        if (isNaN(parseFloat(bytes)) || !isFinite(bytes)) return '-';
        if (typeof precision === 'undefined') precision = 1;
        var units = ['bytes', 'kB', 'MB', 'GB', 'TB', 'PB'],
            number = Math.floor(Math.log(bytes) / Math.log(1024));
        return (bytes / Math.pow(1024, Math.floor(number))).toFixed(precision) + ' ' + units[number];
    }
});


app.controller('Exit', ['token',
    function (token) {
        token.remove();
    }]);

app.controller('TopMenuController', ['$scope', '$location', '$http', 'token', '$rootScope', '$translate',
    function ($scope, $location, $http, token, $rootScope, $translate) {
        $rootScope.bodyClass = 'content_bg';
        $rootScope.top_link_active_list = '';
        $rootScope.top_link_active_device = '';

        $scope.change_language = function(lang) {
            $translate.use(lang);
        }

    }]);

//Update When Create new Design
app.controller('LoginController', ['$scope', '$location', '$http', 'token', '$rootScope', 'sysMessage', '$filter',
    function ($scope, $location, $http, token, $rootScope, sysMessage, $filter) {
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
                        } else {
                            sysMessage.login_failed($filter('translate')('system_thenameorpassworddonotmatch'))
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

app.controller('CampaignEditController', ['$scope', '$stateParams', 'token', 'Campaign', '$location', '$http', 'toaster', 'Files','sysMessage', 'sysLocation', 'FileUploader', '$rootScope', '$filter',
    function ($scope, $stateParams, token, Campaign, $location, $http, toaster, Files, sysMessage, sysLocation, FileUploader, $rootScope, $filter) {
        $rootScope.top_link_active_list = 'top_link_active';
        $scope.filesArray = [];

        $scope.workdays = ['mo', 'tu', 'we', 'th', 'fr', 'sa', 'su'];
        $scope.checkedDays = [];
        $scope.toggleCheckDays = function (day) {
            if ($scope.checkedDays.indexOf(day) === -1) {
                $scope.checkedDays.push(day);
            } else {
                $scope.checkedDays.splice($scope.checkedDays.indexOf(day), 1);
            }
        };

        $scope.workhours = ['7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23', '0'];
        $scope.checkedHours = [];
        $scope.toggleCheckHours = function (hour) {
            if ($scope.checkedHours.indexOf(hour) === -1) {
                $scope.checkedHours.push(hour);
            } else {
                $scope.checkedHours.splice($scope.checkedHours.indexOf(hour), 1);
            }
        };

        Campaign.get_campaigns({token: token.get(), id: $stateParams.cId}, function (response) {
            console.log(response);
            $scope.campaign = response;
            $scope.campaign_form = {campaign_status: $scope.campaign.status, filesArray: $scope.campaign.files, campaign_name: $scope.campaign.name, campaign_time: $scope.campaign.duration, campaign_order: $scope.campaign.sequence, campaign_start: timeToData($scope.campaign.start), campaign_finish: timeToData($scope.campaign.finish)};
        });

        var timeToData = function(time) {
            return new Date(time);
        };

        var dataToTime = function(data) {
            return data.getTime();
        };
        $scope.edit_company = function () {
            console.log($scope.checkedHours);
            console.log($scope.checkedDays);
            console.log($scope.campaign_form);

            Campaign.edit_campaigns({token: token.get(), id: $scope.campaign.id, status: $scope.campaign_form.campaign_status, name: $scope.campaign_form.campaign_name, sequence: $scope.campaign_form.campaign_order, start: dataToTime($scope.campaign_form.campaign_start), finish: dataToTime($scope.campaign_form.campaign_finish), duration: $scope.campaign_form.campaign_time, days: $scope.checkedDays, hours: $scope.checkedHours}, function(response){
                sysLocation.goList();
            });
        };

        $scope.openPlayer = function(key) {
            FWDRL.show('playlist', key);
        }

        $scope.inArchive = function (id) {
            Files.arhiveFiles({token: token.get(), id: id}, function(response){
                sysMessage.delete_s($filter('translate')('system_filewasdeleted'))
                refreshFilesModel();
            });
        };

        var refreshFilesModel = function () {
            Campaign.get_campaigns({token: token.get(), id: $stateParams.cId}, function (response) {
                console.log(response);
                $scope.files = response;
                $scope.campaign_form.filesArray = $scope.files.files;
            });
        };

        var file_upload_url = apiEndpoint + 'token/' + token.get() + '/campaigns/' + $stateParams.cId + '/files/';
        console.log(file_upload_url);

        var uploader = $scope.uploader = new FileUploader({
            url: file_upload_url
        });

        // FILTERS

        uploader.filters.push({
            name: 'customFilter',
            fn: function(item /*{File|FileLikeObject}*/, options) {
                return this.queue.length < 10;
            }
        });

        // CALLBACKS

        uploader.onWhenAddingFileFailed = function(item /*{File|FileLikeObject}*/, filter, options) {
            console.info('onWhenAddingFileFailed', item, filter, options);
        };
        uploader.onAfterAddingFile = function(fileItem) {
            console.info('onAfterAddingFile', fileItem);
        };
        uploader.onAfterAddingAll = function(addedFileItems) {
            console.info('onAfterAddingAll', addedFileItems);
        };
        uploader.onBeforeUploadItem = function(item) {
            console.info('onBeforeUploadItem', item);
        };
        uploader.onProgressItem = function(fileItem, progress) {
            console.info('onProgressItem', fileItem, progress);
        };
        uploader.onProgressAll = function(progress) {
            console.info('onProgressAll', progress);
        };
        uploader.onSuccessItem = function(fileItem, response, status, headers) {
            console.info('onSuccessItem', fileItem, response, status, headers);
        };
        uploader.onErrorItem = function(fileItem, response, status, headers) {
            console.info('onErrorItem', fileItem, response, status, headers);
        };
        uploader.onCancelItem = function(fileItem, response, status, headers) {
            console.info('onCancelItem', fileItem, response, status, headers);
        };
        uploader.onCompleteItem = function(fileItem, response, status, headers) {
            console.info('onCompleteItem', fileItem, response, status, headers);
            refreshFilesModel();
        };
        uploader.onCompleteAll = function() {
            console.info('onCompleteAll');
            uploader.clearQueue();
            refreshFilesModel();
        };

        console.info('uploader', uploader);
    }]);

app.controller('CampaignsController', ['$scope', 'token', 'Campaign', 'sysMessage', '$rootScope', '$filter',
    function ($scope, token, Campaign, sysMessage, $rootScope, $filter) {
        if (token.check()) {
            $scope.timeconvert = function(time) {
                var timeConvert = moment(time).unix();
                timeConvert = moment(timeConvert, 'X').format('DD.MM.YYYY');
                return timeConvert;
            };

            $rootScope.top_link_active_list = 'top_link_active';
            Campaign.get_all_campaigns({token: token.get()}, function (response) {
                $scope.campaigns = response.campaigns;
            });
            $scope.remove = function (campaign) {
                $scope.campaigns.splice($scope.campaigns.indexOf(campaign), 1);
                Campaign.delete_campaigns({token: token.get(), id: campaign.id}, function (response) {
                    Campaign.get_all_campaigns({token: token.get()}, function (response) {
                        $scope.campaigns = response.campaigns;
                        sysMessage.delete_s($filter('translate')('system_campaign') + ' ' + campaign.name + ' ' + $filter('translate')('system_theremoved'));
                    });
                });
            };
        }
    }]);

app.controller('DevicesController', ['$scope', 'token', 'Device', 'sysMessage', '$rootScope', '$filter',
    function ($scope, token, Device, sysMessage, $rootScope, $filter) {
        if (token.check()) {
            $scope.timeconvert = function(time) {
                var timeConvert = moment(time).unix();
                timeConvert = moment(timeConvert, 'X').format('DD.MM.YYYY');
                return timeConvert;
            };

            $rootScope.top_link_active_device = 'top_link_active';
            Device.get_data({token: token.get()}, function (response) {
                console.log(response);
                $scope.devices = response.devices;
            });
            $scope.change_device = function(device) {
                Device.update({token: token.get(), id: device.id, orientation: parseInt(device.orientation), resolution: parseInt(device.resolution), campaignId: parseInt(device.campaignId), description: device.description }, function (response) {
                    sysMessage.update_s($filter('translate')('system_device') + ' ' + $filter('translate')('system_updated'));
                });
            };
            $scope.delete_device = function(device) {
                Device.delete({token: token.get(), id: device.id}, function (response){
                    Device.get_data({token: token.get()}, function (response) {
                        $scope.devices = response.devices;
                        sysMessage.update_s($filter('translate')('system_device') + ' ' + device.uuid + ' ' + $filter('translate')('system_removed'));
                    });
                });
            };
            $scope.add_device = function() {
                Device.add({token: token.get()}, function (response){
                    Device.get_data({token: token.get()}, function (response) {
                        $scope.devices = response.devices;
                        sysMessage.update_s($filter('translate')('system_deviceadded'));

                    });
                });
            };
        };
    }]);