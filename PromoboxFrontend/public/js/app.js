var apiEndpoint = "http://46.182.31.101:8080/service/";

var app = angular.module('promobox', ['ngRoute', 'ui.bootstrap', 'pascalprecht.translate', 'promobox.services', 'angularFileUpload', 'toaster', 'ui.router', 'ui.sortable', 'angularMoment', 'ui.bootstrap.datetimepicker', 'checklist-model']);


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

app.controller('TopMenuController', ['$scope', '$location', '$http', 'token', 'Clients', '$rootScope', '$translate',
    function ($scope, $location, $http, token, Clients, $rootScope, $translate) {
        $rootScope.bodyClass = 'content_bg';
        $rootScope.top_link_active_list = '';
        $rootScope.top_link_active_device = '';
        
        Clients.getClient({token: token.get()}, function(response) {
            console.log(response);
            $scope.compName = response.compName;
        });

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

app.controller('CampaignEditController', ['$scope', '$stateParams', 'token', 'Campaign', '$location', '$http', 'toaster', 'Files','sysMessage', 'sysLocation', 'FileUploader', '$rootScope', '$filter', 'orderByFilter',
    function ($scope, $stateParams, token, Campaign, $location, $http, toaster, Files, sysMessage, sysLocation, FileUploader, $rootScope, $filter, orderByFilter) {
        $rootScope.top_link_active_list = 'top_link_active';
        $scope.filesArray = [];
        $scope.checkedDays = [];
        $scope.checkedHours = [];
        $scope.apiEndpoint = apiEndpoint;
        
        Campaign.get_campaigns({token: token.get(), id: $stateParams.cId}, function (response) {
            console.log(response);
            $scope.campaign = response;
            $scope.checkedDays = $scope.campaign.days;
            $scope.checkedHours = $scope.campaign.hours;
            $scope.campaign_form = {campaign_status: $scope.campaign.status, 
                                    filesArray: $scope.campaign.files, 
                                    campaign_name: $scope.campaign.name, 
                                    campaign_time: $scope.campaign.duration, 
                                    campaign_order: $scope.campaign.sequence, 
                                    campaign_start: timeToData($scope.campaign.start), 
                                    campaign_finish: timeToData($scope.campaign.finish),
                                    campaign_start_time: timeToDataTime($scope.campaign.start),
                                    campaign_finish_time: timeToDataTime($scope.campaign.finish),
                                    };
            $scope.campaign_stat = {campaign_count_files: $scope.campaign.countFiles,
                                    campaign_count_images: $scope.campaign.countImages,
                                    campaign_count_audios: $scope.campaign.countAudios,
                                    campaign_count_videos: $scope.campaign.countVideos,
                                    campaign_audio_length: humanLength($scope.campaign.audioLength),
                                    campaign_video_length: humanLength($scope.campaign.videoLength)};
                                
            if (!$scope.campaign.files) {
                $scope.campaign.files = [];
            }
            
            //$scope.$watchCollection('campaign.files', function () {
            //    $scope.campaign.files = orderByFilter($scope.campaign.files, ['orderId','name']);
            //});
        });

        $scope.workdays = ['mo', 'tu', 'we', 'th', 'fr', 'sa', 'su'];
        $scope.toggleCheckDays = function (day) {
            if ($scope.checkedDays.indexOf(day) === -1) {
                $scope.checkedDays.push(day);
            } else {
                $scope.checkedDays.splice($scope.checkedDays.indexOf(day), 1);
            }
        };
        
        

        $scope.workhours = ['7', '7:30',
                            '8', '8:30',
                            '9', '9:30',
                            '10', '10:30',
                            '11', '11:30',
                            '12', '12:30',
                            '13', '13:30',
                            '14', '14:30',
                            '15', '15:30',
                            '16', '16:30',
                            '17', '17:30',
                            '18', '18:30',
                            '19', '19:30',
                            '20', '20:30',
                            '21', '21:30',
                            '22', '22:30',
                            '23', '23:30',
                            '0'];
        
        $scope.addWorkhours = [];
        $scope.removeWorkhours = [];
        
        
        
        $scope.toggleWorkHours = function (ar, hour) {
            if (ar.indexOf(hour) === -1) {
                ar.push(hour);
            } else {
                ar.splice(ar.indexOf(hour), 1);
            }
        };
        
        $scope.addWorkingHours = function () {
            for (i = 0; i < $scope.addWorkhours.length; i++) {
                $scope.checkedHours.push($scope.addWorkhours[i]);
            }
            $scope.addWorkhours = [];
        }
        
        $scope.removeWorkingHours = function () {
            for (i = 0; i < $scope.removeWorkhours.length; i++) {
                $scope.checkedHours.splice($scope.checkedHours.indexOf($scope.removeWorkhours[i]), 1);
            }
            $scope.removeWorkhours = [];
        }
        
        $scope.formatWorkingHours = function (hour) {
            if (hour.indexOf(":") == -1) {
                return hour + ":00";
            }
            
            return hour;
        }
        
        var humanLength = function(sec) {
            var hours = sec % 3600;
            sec -= hours * 3600;
            var min = sec % 60;
            
            return hours + " : " + min
        }

        var timeToData = function(time) {
            return new Date(time);
        };
        
        var timeToDataTime = function(time) {
            var date = new Date(time);
            
            var h = date.getHours();
            var m = date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes();
            var result =  h + ":" + m;
            
            console.log(result);
            
            return result;
        };
 
        var dataToTime = function(data, time) {
            var timePart = time.split(":");
            return data.getTime() + (timePart[0] * 60 * 60 + timePart[1] * 60) * 1000;
        };
        $scope.edit_company = function () {
            console.log($scope.checkedHours);
            console.log($scope.checkedDays);
            console.log($scope.campaign_form);

            Campaign.edit_campaigns({
                token: token.get(), 
                id: $scope.campaign.id, 
                status: $scope.campaign_form.campaign_status, 
                name: $scope.campaign_form.campaign_name, 
                sequence: $scope.campaign_form.campaign_order, 
                start: dataToTime($scope.campaign_form.campaign_start, $scope.campaign_form.campaign_start_time), 
                finish: dataToTime($scope.campaign_form.campaign_finish, $scope.campaign_form.campaign_finish_time), 
                duration: $scope.campaign_form.campaign_time, 
                days: $scope.checkedDays, 
                hours: $scope.checkedHours}, function(response){
                sysLocation.goList();
            });
        };
        
        $scope.close_settings = function () {
            sysLocation.goList();
        }
        
        $scope.remove_company = function () {
            Campaign.delete_campaigns({token: token.get(), id: $scope.campaign.id}, function (response) {
                sysLocation.goList();
            });
        };
       
        $scope.fileSort = {
            stop: function(e, ui) {
                update_files_order_id();
            }
        }
        
        $scope.selectedFile = null;
        $scope.file_sort_select_file = function(file) { 
            $scope.selectedFile = file;
        };
        
        $scope.file_sort_up = function () {
            var index = $scope.campaign_form.filesArray.indexOf($scope.selectedFile);

            if (index != -1 && index != 0 ) {
                change_files_order(index, index - 1);
            }
        };
        
        $scope.file_sort_down = function () {
            var index = $scope.campaign_form.filesArray.indexOf($scope.selectedFile);

            if (index != -1 && index != $scope.campaign_form.filesArray.length - 1) {
                change_files_order(index, index + 1);
            }
        };
        
        $scope.file_sort_top = function () {
            var index = $scope.campaign_form.filesArray.indexOf($scope.selectedFile);

            if (index != -1 && index != 0 ) {
                change_files_order(index, 0);
            }
        };
        
        $scope.file_sort_bottom = function () {
            var index = $scope.campaign_form.filesArray.indexOf($scope.selectedFile);

            if (index != -1 && index != $scope.campaign_form.filesArray.length - 1) {
                change_files_order(index, $scope.campaign_form.filesArray.length - 1);
            }
        };
        
        
        var change_files_order = function(i1, i2) {
            var f1 = $scope.campaign_form.filesArray[i1];
            $scope.campaign_form.filesArray.splice(i1, 1);
            $scope.campaign_form.filesArray.splice(i2, 0, f1);
            
            update_files_order_id();
        };
        
        var update_files_order_id = function() {
            for (var index in $scope.campaign.files) {
                $scope.campaign.files[index].orderId = index;
            }
        }
        
        $scope.reorder_files = function() {
            Files.reorderFiles({
                token: token.get(),
                id: $scope.campaign.id,
                filesOrder: $scope.campaign.files});
        };

        $scope.openPlayer = function(key) {
            FWDRL.show('playlist', key);
        };

        $scope.inArchive = function (id) {
            Files.arhiveFiles({token: token.get(), id: id}, function(response){
                sysMessage.delete_s($filter('translate')('system_filewasdeleted'));
                refreshFilesModel();
            });
        };
        
        $scope.playNextFile = function (id) {
            Campaign.play_next_file({token: token.get(), id: $stateParams.cId, file: id}, function(responce) {
                sysMessage.update_s($filter('translate')('system_playnextfile'));
            });
        };
        
        $scope.rotateFile = function (id, angle) {
            Campaign.rotate_file({token: token.get(), id: $stateParams.cId, file: id, angle: angle}, function(responce) {
                sysMessage.update_s($filter('translate')('system_rotatefile'));
            });
        };
        
        $scope.settingsVisible = false;
        $scope.showSettings = function(show) {
            $scope.settingsVisible = show;
        }
        
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
                return this.queue.length < 50;
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
        
        setTimeout(function(){jQuery('.styler').styler();}, 800);
        //jQuery(function(){jQuery('.styler').styler()});
        console.log("styler")
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
            $scope.currentCampaign = {};
            Campaign.get_all_campaigns({token: token.get()}, function (response) {
                console.log(response);
                
                $scope.campaigns = response.campaigns;
                
                if ($scope.campaigns.length > 0) {
                    $scope.currentCampaign = $scope.campaigns[0];
                }
            });
            $scope.show_statistics = function(campaign) {
                $scope.currentCampaign = campaign;
            }
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
                $scope.campaigns = response.campaigns;
                
                $scope.currentDevice = null;
                $scope.tmpCampaignIds = [];
                if ($scope.devices.length > 0) {
                    $scope.currentDevice = $scope.devices[0];
                }
                setTimeout(function(){jQuery('.styler').styler();}, 800); 
                console.log("styler")
            });
            
            $scope.open_add_campaign = function(device) {
                $scope.currentDevice = device;
                
                if (!$scope.currentDevice.campaignIds) {
                    $scope.currentDevice.campaignIds = [];
                }
                              
                $scope.tmpCampaignIds = [];
                for (var index in device.campaignIds) {
                    $scope.tmpCampaignIds.push(device.campaignIds[index]);
                }
            };
            $scope.filter_device_campigns = function(value, index) {
                if (!$scope.currentDevice) {
                    $scope.currentDevice = {campaignIds: []};
                }
                
                if (!$scope.currentDevice.campaignIds) {
                    $scope.currentDevice.campaignIds = [];
                }
                
                return $scope.currentDevice.campaignIds.indexOf(value.id) == -1;
            }
            $scope.toggle_campaign = function(campaignId) {
                var index = $scope.tmpCampaignIds.indexOf(campaignId);
                if (index == -1) {
                    $scope.tmpCampaignIds.push(campaignId);
                } else {
                    $scope.tmpCampaignIds.splice(index, 1);
                }
            }
            $scope.add_campaigns = function() {
                for (var index in $scope.tmpCampaignIds) {
                    var campId = $scope.tmpCampaignIds[index];
                    
                    if ($scope.currentDevice.campaignIds.indexOf(campId) == -1) {
                        $scope.currentDevice.campaignIds.push(campId);
                    }
                }
            }
            
            $scope.change_device = function(device) {
                var deviceUpdate = {token: token.get(), 
                    id: device.id, 
                    orientation: parseInt(device.orientation), 
                    resolution: parseInt(device.resolution), 
                    campaignIds: device.campaignIds, 
                    description: device.description,
                    audioOut: device.audioOut,
                    workStartAt: device.workStartAt,
                    workEndAt: device.workEndAt
                }
                
                for (index in $scope.workdays) {
                    var day = $scope.workdays[index];
                    deviceUpdate[day] = device[day];
                }
                
                Device.update(deviceUpdate, function (response) {
                    if (response.ERROR) {
                        sysMessage.error($filter('translate')('system_device') + ' ' + $filter('translate')('time_intersection'));
                    } else {
                        sysMessage.update_s($filter('translate')('system_device') + ' ' + $filter('translate')('system_updated'));
                    }
                });
            };
            
            $scope.delete_device_campaign = function (device, campaignId) {
                Device.delete_device_campaign(
                        {token: token.get(), id: device.id, campaignId: campaignId}, function(response) {
                        var index = device.campaignIds.indexOf(campaignId);
                        device.campaignIds.splice(index, 1);
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
            
            $scope.clearCache =  function(deviceId) {
                Device.clearCache({token: token.get() , id: deviceId}, function (response) {
                    sysMessage.update_s($filter('translate')('system_device_cachecleared'));
                });
            }
            
            $scope.workdays = ['mo', 'tu', 'we', 'th', 'fr', 'sa', 'su'];
            $scope.toggleWorkDay = function(device, day) {
                device[day] = !device[day];
            }
            $scope.workhours = [];
            for (var i = 0; i < 24; i++) {
                 $scope.workhours.push(i + ":00");
            }
            
            $scope.visibleDeviceSettings = 0;
            $scope.showDeviceSettings = function(id) {
                $scope.visibleDeviceSettings = id;
            };
            
        };
    }]);
