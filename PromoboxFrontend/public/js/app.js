var apiEndpoint = "http://46.182.31.101:8080/service/";
//var apiEndpoint = "http://192.168.1.52:8080/backend/service/";

var app = angular.module('promobox', ['ngRoute', 'ngSanitize', 'ui.bootstrap', 'pascalprecht.translate', 'promobox.services', 'angularFileUpload', 'toaster', 'ui.router', 'ui.sortable', 'ui.select', 'angularMoment', 'ui.bootstrap.datetimepicker', 'checklist-model']);

var adminView = function(contentController, contentTemplate) {
    return {
        "topView": { controller: 'TopMenuController', templateUrl: '/views/top_menu.html' },
        "leftMenuView": {controller: 'LeftMenuController', templateUrl: '/views/left_menu.html' },
        "contentView": { controller: contentController, templateUrl: contentTemplate },
        "footerView": {controller: 'FooterController', templateUrl: '/views/footer.html'}
    }
};

app.config(['$routeProvider','$stateProvider','$urlRouterProvider', function ($routeProvider, $stateProvider, $urlRouterProvider) {
    $urlRouterProvider.otherwise('/');
    $stateProvider
        .state('login', {
            url: "/",
            views: {
                "topView": { controller: 'TopMenuController',templateUrl: '/views/top_clean_menu.html' },
                "contentView": { controller: 'LoginController',templateUrl: '/views/login.html' },
                "footerView": {controller: 'FooterController', templateUrl: '/views/footer.html'}
            }
        })
        .state('registration', {
            url: "/registration",
            views: {
                "topView": { controller: 'TopMenuController',templateUrl: '/views/top_clean_menu.html' },
                "contentView": { controller: 'RegistrationController',templateUrl: '/views/register.html' },
                "footerView": {controller: 'FooterController', templateUrl: '/views/footer.html'}
            }
        })
        .state('list', {
            url: "/list",
            views: adminView('CampaignsController', '/views/list.html') 
        })
        .state('campaign_edit', {
            url: "/campaign/edit/:cId",
            views: adminView('CampaignEditController', '/views/campaign_edit.html')
        })
        .state('device', {
            url: "/device",
            views: adminView('DevicesController', '/views/device.html')
        })
        .state('setting_account', {
            url: "/setting/account",
            views: adminView('SettingAccountController', '/views/settings/account.html')
        })
        .state('setting_campaign', {
            url: "/setting/campaign",
            views: adminView('SettingCampaignController', '/views/settings/usersPermissions.html')
        })
        .state('setting_device', {
            url: "/setting/device",
            views: adminView('SettingDeviceController', '/views/settings/usersPermissions.html')
        })
        .state('setting_payment', {
            url: "/setting/payment",
            views: adminView('SettingPaymentController', '/views/settings/payments.html')
        })
        .state('setting_user', {
            url: "/setting/user",
            views: adminView('SettingUserController', '/views/settings/users.html')
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

app.filter('humanLength', function() {
    return function(sec) {
        var hours = Math.floor(sec / 3600);
        sec -= hours * 3600;
        var min = Math.floor(sec / 60);

        return hours + " : " + min;
    }
 });

app.filter('timeConvert', function() {
    return function(time) {
        var timeConvert = moment(time).unix();
        timeConvert = moment(timeConvert, 'X').format('DD.MM.YYYY h:mm');
        return timeConvert;
    }
});

app.filter('dateConvert', function() {
    return function(time) {
        var timeConvert = moment(time).unix();
        timeConvert = moment(timeConvert, 'X').format('DD.MM.YYYY');
        return timeConvert;
    }
});

app.controller('Exit', ['token',
    function (token) {
        token.remove();
    }]);

app.controller('LeftMenuController', ['$scope', '$location', '$http', 'token', 'Clients', '$rootScope', '$translate',
    function ($scope, $location, $http, token, Clients, $rootScope) {
        Clients.getClient({token: token.get()}, function(response) {
            $scope.admin = response.admin;
            $rootScope.admin = response.admin;
        });
    }]);


app.controller('TopMenuController', ['$scope', '$location', '$http', 'token', 'Clients', '$rootScope', '$translate',
    function ($scope, $location, $http, token, Clients, $rootScope, $translate) {
        $rootScope.bodyClass = 'content_bg';
        $rootScope.left_menu_active = '';
        
        Clients.getClient({token: token.get()}, function(response) {
            $scope.compName = response.compName;
        });

        $scope.openAccount = function() {
            $location.path('/setting/account');
        }
    }]);

app.controller('FooterController', ['$scope', '$location', '$http', 'token', '$rootScope', '$translate', '$filter','$locale',
    function ($scope, $location, $http, token, $rootScope, $translate, $filter, $locale) {

        $scope.lang = {};
        $scope.langs = ["en", "et", "lv", "ru"];
        $scope.lang.code = $translate.use();

        $scope.change_language = function(lang) {
            $translate.use(lang);
        };

        if (! $translate.use() ){
            switch ($locale.id.substring(0,2)){
                case 'et':
                    $scope.lang.code = "et";
                    break;
                case 'ru' :
                    $scope.lang.code = "ru";
                    break;
                case 'lv' :
                    $scope.lang.code = "lv";
                    break;
                default :
                    $scope.lang.code = "en";
                    break;
            }
            $scope.change_language($scope.lang.code);
        }



        //setTimeout(function(){jQuery('input[type="checkbox"], input[type="radio"],select').styler();}, 50);

    }]);

//Update When Create new Design
app.controller('LoginController', ['$scope', '$location', '$http', 'token', '$rootScope', 'sysMessage', '$filter','facade',
    function ($scope, $location, $http, token, $rootScope, sysMessage, $filter, facade) {
        $rootScope.bodyClass = 'main_bg';
        if (!token.check()) {
            $scope.loginForm = { email: '', password: '', remember: true};

            $scope.login = function () {
                $http.post(apiEndpoint + "user/login",
                    $.param({
                        email: $scope.loginForm.email,
                        password: $scope.loginForm.password
                    }))
                    .success(function (data) {
                        if (data.response == 'OK') {
                            token.put(data.token, $scope.loginForm.remember);
                            $location.path('/list');
                            return true;
                        } else {
                            sysMessage.login_failed($filter('translate')('system_thenameorpassworddonotmatch'))
                        }
                    });
            };

            $scope.forgot_password = function(){
                    facade.getModal().open({
                        templateUrl: '/views/modal/info.html',
                        controller: 'ModalInfoController',
                        windowClass: 'info-dialog',
                        resolve: {
                            model: function() {
                                return {
                                    titleText : 'modal_forgot_pswd',
                                    bodyText  : 'modal_forgot_pswd_body',
                                    okBtnText : 'modal_forgot_pswd_ok_btn'
                                }
                            }
                        }
                    });
            }

        } else {
            $location.path('/list');
        }
    }]);

//Update When Create new Design
app.controller('RegistrationController', ['$scope', '$http', 'token', 'sysLocation', '$rootScope', 'sysMessage', '$filter', 'Clients', 
    function ($scope, $http, token, sysLocation, $rootScope, sysMessage, $filter, Clients) {
        $rootScope.bodyClass = 'main_bg';

        $scope.register = function() {
            Clients.register({
                firstname: $scope.register_form.firstname,
                surname: $scope.register_form.surname,
                companyName: $scope.register_form.companyName,
                email: $scope.register_form.email
            }, function(response) {
                if(response.response == "ERROR") {
                    if (response.reason == "invalidEmail") {  
                        sysMessage.error($filter('translate')('login_form_register') + ' ' + $filter('translate')('registration_form_invalid_email'));
                    } else if (response.reason == "emailExist") {
                        sysMessage.error($filter('translate')('login_form_register') + ' ' + $filter('translate')('registration_form_email_exists'));
                    }
                    
                } else {
                    sysMessage.registration_success($filter('translate')('registration_form_success'))
                }
            });
        }
    }]);

app.controller('CampaignNewController', ['token', 'Campaign', 'sysLocation',
    function (token, Campaign, sysLocation) {
        Campaign.create_new_campaign({token: token.get()}, function(response){
            sysLocation.goLink('/campaign/edit/' + response.id)
        });
    }]);

app.controller('CampaignEditController', ['$scope', '$stateParams', 'token', 'Campaign', '$location', '$http', 'toaster',
    'Files','sysMessage', 'sysLocation', 'FileUploader', '$rootScope', '$filter', 'orderByFilter', '$timeout', 'browser', 'facade',
    function ($scope, $stateParams, token, Campaign, $location, $http, toaster,
              Files, sysMessage, sysLocation, FileUploader, $rootScope, $filter, orderByFilter, $timeout, browser, facade) {
        $rootScope.left_menu_active = 'campaign';
        $scope.filesArray = [];
        $scope.checkedDays = [];
        $scope.checkedHours = [];
        $scope.apiEndpoint = apiEndpoint;
        
        Campaign.get_campaigns({token: token.get(), id: $stateParams.cId}, function (response) {
            
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
                                    campaign_finish_time: timeToDataTime($scope.campaign.finish)
                                    };
            $scope.campaign_stat = {campaign_count_files: $scope.campaign.countFiles,
                                    campaign_count_images: $scope.campaign.countImages,
                                    campaign_count_audios: $scope.campaign.countAudios,
                                    campaign_count_videos: $scope.campaign.countVideos,
                                    campaign_audio_length: $scope.campaign.audioLength,
                                    campaign_video_length: $scope.campaign.videoLength
                                    };
            
            console.log($scope.campaign_form);

            if (!$scope.campaign.files) {
                $scope.campaign.files = [];
            }
            
        });

        $scope.isWebmVideo = function() {
            var browserName = browser.detectBrowser();
            return browserName == "chrome" || browserName == "firefox" || browserName == "opera";
        };

        $scope.getFileExt = function(file) {
            var browserName = browser.detectBrowser();
            var webm =  browserName == "chrome" || browserName == "firefox" || browserName == "opera";
            var ext = ".png";

            if (file.fileType == 2) {
                ext = ".mp3";
            } else if (file.fileType == 3) {
                if (webm) {
                    ext = ".webm";
                } else {
                    ext = ".mp4";
                }
            }

            return ext;
        };

        $scope.isFileConverting = function (file) {
            return file.status == 0 || file.status == 4;
        };
        
        $scope.getFileThumb = function(file){
            var src = apiEndpoint + "files/thumb/" + file.id + "?t=" + file.t;
            return src;
        };
        
        var extstsConvertingFiles = function(id) {
            for (var i = 0; file = $scope.campaign.files[i]; i++) {
                return $scope.isFileConverting(file);
            }
            
            return false;
        };
        
        var findFileById = function(id) {
            for (var i = 0; file = $scope.campaign.files[i]; i++) {
                if (id == file.id) {
                    return file;
                }
            }
            
            return null;
        } ;
        
        var refreshFileStatuses = function() {
            var files = [];
            for (var i = 0; i < $scope.campaign.files.length; i++) {
                if ($scope.campaign.files[i].status == 0 || 
                        $scope.campaign.files[i].status == 4) {
                    files.push($scope.campaign.files[i].id);
                }
            }
            if (files.length > 0) {
                Campaign.refresh_files_status({token: token.get(), files: files}, function(responce) {
                    for (var i = 0; i < responce.files.length; i++) {
                        var f = responce.files[i];

                        var file = findFileById(f.id);
                        file.t= new Date().getTime();
                        file.status = f.status;
                     }
                });
            }
            
            if (extstsConvertingFiles()) {
                $timeout(refreshFileStatuses, 15000);
            }
        };

        $scope.workdays = ['mo', 'tu', 'we', 'th', 'fr', 'sa', 'su'];
        $scope.toggleCheckDays = function (day) {
            if ($scope.checkedDays.indexOf(day) === -1) {
                $scope.checkedDays.push(day);
            } else {
                $scope.checkedDays.splice($scope.checkedDays.indexOf(day), 1);
            }
        };
        

        $scope.workhours = [];
        for (var i = 0; i < 24; i++) {
              $scope.workhours.push(i + "");
        }
        
        $scope.addWorkhours = [];
        $scope.removeWorkhours = [];

        $scope.campaignTimes = [5, 15, 30, 60, 300, 600];
        $scope.formatCampaignTime = function(time) {
            if (time <= 30) {
                return time + ' ' + $filter('translate')('campaign_edit_sec'); 
            } else {
                return time/60 + ' ' + $filter('translate')('campaign_edit_min'); 
            }
        };
        
        $scope.campaignOrders = [1, 2];
        $scope.orderName = function(order) {
            if (order == 1) {
                return $filter('translate')('campaign_edit_streak');
            } else {
                return $filter('translate')('campaign_edit_accident');
            }
        };
        
        
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
        };
        
        $scope.removeWorkingHours = function () {
            for (i = 0; i < $scope.removeWorkhours.length; i++) {
                $scope.checkedHours.splice($scope.checkedHours.indexOf($scope.removeWorkhours[i]), 1);
            }
            $scope.removeWorkhours = [];
        };
        
        $scope.formatWorkingHours = function (hour) {
            if (hour) {
                if (hour.indexOf(":") == -1) {
                    return hour + ":00";
                }
                
                return hour;
            }
        };
        $scope.sanitizeWorkingHours = function(hour) {
            if (hour) {
                return hour.replace(":00", "").replace(":0", "").replace(":", "");
            }
        };
        

        var timeToData = function(time) {
            var dateTime = new Date(time);
            return new Date(dateTime.getFullYear(), dateTime.getMonth(), dateTime.getDate(), 0, 0, 0, 0);
        };
        
        var timeToDataTime = function(time) {
            var date = new Date(time);
            
            var h = date.getHours();
            //var m = date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes();
            return  h + ":00";
        };
 
        var dataToTime = function(data, time) {
            var timePart = $scope.formatWorkingHours(time).split(":");
            return data.getTime() + (timePart[0] * 60 * 60 + timePart[1] * 60) * 1000;
        };
        $scope.edit_company = function () {
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
                hours: $scope.checkedHours}, function(response) {
                    if (response.WARN) {
                        sysMessage.warning($filter('translate')('system_device') + ' ' + $filter('translate')('device_time_intersection'));
                    }
                    
                    sysMessage.update_s($filter('translate')('campaign_updated'));
                    
            });
        };
        
        $scope.close_settings = function () {
            sysLocation.goList();
        };

        var openConfirmDialog = function(toDo, confirmationText) {
            facade.getModal().open({
                templateUrl: '/views/modal/confirmation.html',
                controller: 'ModalConfirmController',
                windowClass: 'confirmation-dialog',
                resolve: {
                    facade: function() {
                        return facade;
                    },
                    model: function() {
                        return {
                            toDo: toDo,
                            confirmationText : confirmationText
                        }
                    }
                }
            });
        };
        
        $scope.remove_company = function () {
            var toDo = function(){
                Campaign.delete_campaigns({token: token.get(), id: $scope.campaign.id}, function (response) {
                    if (response.error) {
                        sysMessage.error($filter('translate')('system_device') + ' ' + $filter('translate')('campaign_edit_in_use'));
                    } else {
                        sysLocation.goList();
                    }
                });
            };
            openConfirmDialog(toDo, 'modal_confirm_campaign_deletion');
        };
       
        $scope.fileSort = {
            stop: function(e, ui) {
                update_files_order_id();
            }
        };
        
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
        };
        
        $scope.reorder_files = function() {
            Files.reorderFiles({
                token: token.get(),
                id: $scope.campaign.id,
                filesOrder: $scope.campaign.files});
        };

        $scope.openPlayer = function(key) {
            var index = key;
            for (var i = 0; file = $scope.campaign.files[i]; i++) {
                if (i >= key) {
                    break;
                }

                if ($scope.isFileConverting(file)) {
                    index = index - 1;
                }
            }
            FWDRL.show('playlist', index);
        };

        $scope.inArchive = function (id) {
            Files.arhiveFiles({token: token.get(), id: id}, function(response){
                sysMessage.delete_s($filter('translate')('system_filewasdeleted'));
                refreshFilesModel();
            });
        };
        
        $scope.playNextFile = function (id) {
            Campaign.play_next_file({token: token.get(), id: $stateParams.cId, file: id}, function(responce) {
                if (responce.error == "no_device") {
                    sysMessage.error($filter('translate')('system_device') + ' ' + $filter('translate')('campaign_no_active_device'));
                } else {
                    sysMessage.update_s($filter('translate')('system_playnextfile'));
                }
            });
        };
        
        $scope.rotateFile = function (id, angle) {
            Campaign.rotate_file({token: token.get(), id: $stateParams.cId, file: id, angle: angle}, function(responce) {
                sysMessage.update_s($filter('translate')('system_rotatefile'));
                refreshFilesModel();
            });
        };

        $scope.saveLink = function(httpLink){
            if(httpLink && httpLink != ""){
                Files.addLink({token: token.get(), id: $scope.campaign.id, link: httpLink }, function(response){
                    if (response.result == "ERROR"){
                        if (response.reason == "UNKNOWN_PROTOCOL"){
                            sysMessage.error($filter('translate')('system_link_unknown_protocol'));
                        } else {
                            sysMessage.error(response.reason);
                        }
                        return;
                    } else if (response.WARN == "NO_PROTOCOL"){
                        sysMessage.warning("No protocol was defined, setting HTTP");
                    }
                    refreshFilesModel();
                });
            } else {
                sysMessage.warning($filter('translate')('system_rotatefile'));
            }
        };
        
        $scope.settingsVisible = false;
        $scope.showSettings = function(show) {
            $scope.settingsVisible = show;
        };
        
        var refreshFilesModel = function () {
            Campaign.get_campaigns({token: token.get(), id: $stateParams.cId}, function (response) {
                $scope.campaign.files = response.files;
                $scope.campaign_form.filesArray = $scope.campaign.files;
                
                for (var i = 0; i < $scope.campaign.files.length; i++) {
                    if ($scope.campaign.files[i].status == 0 || 
                            $scope.campaign.files[i].status == 4) {
                        $scope.campaign.files[i].t =  new Date().getTime();
                    }
                }
                
                $timeout(refreshFileStatuses, 15000);
            });
        };

        var file_upload_url = apiEndpoint + 'token/' + token.get() + '/campaigns/' + $stateParams.cId + '/files/';

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
        
        //setTimeout(function(){jQuery('.styler').styler();}, 800);
    }]);

app.controller('CampaignsController', ['$scope', 'token', 'Campaign', 'DevicesGroups', 'Device', 'sysMessage', '$rootScope', '$filter',
    function ($scope, token, Campaign, DevicesGroups, Device, sysMessage, $rootScope, $filter) {
        if (token.check()) {
            $rootScope.left_menu_active = 'campaign';
            $scope.currentCampaign = {};
            Campaign.get_all_campaigns({token: token.get()}, function (response) {
                $scope.campaigns = response.campaigns;
                
                if ($scope.campaigns.length > 0) {
                    $scope.currentCampaign = $scope.campaigns[0];
                }
            });
            $scope.show_statistics = function(campaign) {
                $scope.currentCampaign = campaign;
            };
            $scope.remove = function (campaign) {
                $scope.campaigns.splice($scope.campaigns.indexOf(campaign), 1);
                Campaign.delete_campaigns({token: token.get(), id: campaign.id}, function (response) {
                    Campaign.get_all_campaigns({token: token.get()}, function (response) {
                        $scope.campaigns = response.campaigns;
                        sysMessage.delete_s($filter('translate')('system_campaign') + ' ' + campaign.name + ' ' + $filter('translate')('system_theremoved'));
                    });
                });
            };

            $scope.addToGroup = function(campaign){
                var devicesCampaigns = [];

                Device.devicesCampaigns({token: token.get()}, function (response) {
                    devicesCampaigns = response.devices;

                    DevicesGroups.list({token: token.get()}, function (response) {

                        var deviceContainsCampaign = function(devicesCampaigns, deviceId, campaignId){
                            for(var i = 0; i < devicesCampaigns.length; i++){
                                var device = devicesCampaigns[i];
                                if(device.id == deviceId){
                                    for(var j = 0; j < device.campaigns.length; j++){
                                        if(device.campaigns[j].id == campaignId) { return true; }
                                    }
                                    return false;
                                }
                            }
                            return false;
                        };

                        var removeDeviceFromDevices = function(devices, deviceId){
                            for (var i = 0; i < devices.length; i++){
                                if ( devices[i].id == deviceId){
                                    devices.splice(i,1);
                                    return;
                                }
                            }
                        };

                        var findDeviceWithId = function(devices,deviceId){
                            for (var i = 0; i < devices.length; i++){
                                if ( devices[i].id == deviceId){
                                    return devices[i];
                                }
                            }
                        };

                        $scope.groups = response.groups;
                        $scope.devices = response.devices;
                        var unGroupedDevices = response.devices.slice();
                        for(var i = 0; i < $scope.groups.length; i++){
                            var group = $scope.groups[i];
                            var devices = [];
                            for(var j = 0; j < group.devices.length; j++){
                                var device = group.devices[j];
                                if(device.contains){
                                    device = findDeviceWithId($scope.devices,device.id);
                                    devices.splice(0,0,device);
                                    removeDeviceFromDevices(unGroupedDevices,device.id);
                                }
                            }
                            group.devices = devices;
                        }

                        var ungoupedGroup = {};
                        ungoupedGroup.id = 0;
                        ungoupedGroup.name = $filter('translate')('modal_group_ungrouped_devices');
                        ungoupedGroup.devices = unGroupedDevices;
                        $scope.groups.splice(0,0,ungoupedGroup);

                        for(var i = 0; i < $scope.devices.length; i++){
                            var device = $scope.devices[i];
                            var campaignIsSet = deviceContainsCampaign(devicesCampaigns, device.id, campaign.id);
                            device.initialSelected = campaignIsSet;
                            device.selected = campaignIsSet;
                        }

                    });
                });

                $scope.currentGroupId = -1;

                $scope.selectGroup = function(group){
                    for(var i=0; i < group.devices.length; i++){
                        group.devices[i].selected = group.selected;
                    }
                };

                $scope.selectDevice = function(device,group){
                    device.selected = !device.selected;
                    group.selected = false;
                };

                $scope.confirm = function(){
                    var changedDevices = [];
                    for(var i = 0; i < $scope.devices.length; i++){
                        var device = $scope.devices[i];
                        if(device.initialSelected != device.selected){
                            changedDevices.push(device);
                        }
                    }
                    var devicesUpdate = {token: token.get(),
                        devices : changedDevices,
                        campaignId : campaign.id
                    };
                    if (changedDevices.length == 0) {
                        sysMessage.warning($filter('translate')('system_no_changes'));
                        return;
                    }
                    Device.delete_and_set_devices_campaign(devicesUpdate, function (response) {
                        if (response.result != "OK"){
                            sysMessage.error($filter('translate')('system_error'));
                        } else {
                            var devicesNames = "";
                            for(var i = 0; i < changedDevices.length; i++){
                                devicesNames += changedDevices[i].name;
                                devicesNames += (i < changedDevices.length-1) ? ", ": ".";
                            }
                            sysMessage.update_s($filter('translate')('system_devices_updated') + devicesNames);
                        }
                    });

                };
            };

            $scope.copyCampaign = function(campaign){
                Campaign.create_campaign_copy({token: token.get(),id: campaign.id}, function(response){
                    var indexOfCampaign = $scope.campaigns.indexOf(campaign);
                    $scope.campaigns.splice(indexOfCampaign, 0, response.campaign);
                });
            };
        }
    }]);

app.controller('DevicesController', ['$scope', 'token', 'Device', 'DevicesGroups', 'sysMessage', '$rootScope', '$filter',
    function ($scope, token, Device, DevicesGroups, sysMessage, $rootScope, $filter) {
        if (token.check()) {
            $scope.currentGroupId = 0;
            $rootScope.left_menu_active = 'device';
            Device.get_data({token: token.get()}, function (response) {
                console.log(response);
                
                $scope.devices = response.devices;
                $scope.campaigns = response.campaigns;
                
                $scope.currentDevice = null;
                $scope.tmpCampaignIds = [];
                if ($scope.devices.length > 0) {
                    $scope.currentDevice = $scope.devices[0];
                }
                //setTimeout(function(){jQuery('.styler').styler();}, 800);
                loadGroups();
            });

            $scope.changeCurrentGroupId = function(id){
                console.log($scope.currentGroupId);
                if ($scope.currentGroupId == id){
                    $scope.currentGroupId = -1;
                } else{
                    $scope.currentGroupId = id;
                }
            };

             var loadGroups = function(){
                 DevicesGroups.list({token: token.get()}, function (response) {
                     //$scope.currentGroup = {};
                     $scope.groups = [];

                     var findDeviceById = function(deviceId){
                         for (var i=0; i<$scope.devices.length; i++){
                             if ($scope.devices[i].id == deviceId){
                                 return $scope.devices[i];
                             }
                         }
                     };

                     for (var i=0; i < response.groups.length; i++){
                         var respGroup = response.groups[i];
                         var group = {};
                         group.id = respGroup.id;
                         group.name = respGroup.name;
                         group.devices = [];
                         for (var j=0; j < respGroup.devices.length; j++){
                             var respDevice = respGroup.devices[j];
                             if ( respDevice.contains){
                                 var foundDevice = findDeviceById(respDevice.id);
                                 if (foundDevice){
                                     group.devices.push(foundDevice);
                                 }
                             }
                         }
                         $scope.groups.push(group);
                     }
                     var group = {};
                     group.id = 0;
                     group.name = $filter('translate')('device_group_all_devices');
                     group.devices = $scope.devices;
                     $scope.groups.push(group);

                 });
             };
            
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
            $scope.filter_device_campaigns = function(value, index) {
                if (!$scope.currentDevice) {
                    $scope.currentDevice = {campaignIds: []};
                }
                
                if (!$scope.currentDevice.campaignIds) {
                    $scope.currentDevice.campaignIds = [];
                }
                
                return $scope.currentDevice.campaignIds.indexOf(value.id) == -1;
            };
            $scope.toggle_campaign = function(campaignId) {
                var index = $scope.tmpCampaignIds.indexOf(campaignId);
                if (index == -1) {
                    $scope.tmpCampaignIds.push(campaignId);
                } else {
                    $scope.tmpCampaignIds.splice(index, 1);
                }
            };
            $scope.add_campaigns = function() {
                for (var index in $scope.tmpCampaignIds) {
                    var campId = $scope.tmpCampaignIds[index];
                    
                    if ($scope.currentDevice.campaignIds.indexOf(campId) == -1) {
                        $scope.currentDevice.campaignIds.push(campId);
                    }
                }
            };
            
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
                };
                
                for (index in $scope.workdays) {
                    var day = $scope.workdays[index];
                    deviceUpdate[day] = device[day];
                }
                
                Device.update(deviceUpdate, function (response) {
                    if (response.WARN) {
                        sysMessage.warning($filter('translate')('system_device') + ' ' + $filter('translate')('device_time_intersection') + ' ' +
                            response.name);
                    }
                    
                    sysMessage.update_s($filter('translate')('system_device') + ' ' + $filter('translate')('system_updated'));
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
                    for (var i=0; i< $scope.groups.length; i++){
                        var group = $scope.groups[i];
                        var index = group.devices.indexOf(device);
                        if(index != -1){
                            group.devices.splice(index,1);
                            DevicesGroups.removeDevice({token: token.get(),groupId: group.id, deviceId: device.id});
                        }
                    }

                    Device.get_data({token: token.get()}, function (response) {
                        $scope.devices = response.devices;
                        sysMessage.update_s($filter('translate')('system_device') + ' ' + device.uuid + ' ' + $filter('translate')('system_removed'));
                    });
                });
            };
            $scope.add_device = function() {
                var findGroupWithId = function(groupId){
                    for (var i=0; i<$scope.groups.length; i++){
                        if ($scope.groups[i].id == groupId){
                            return $scope.groups[i];
                        }
                    }
                };

                Device.add({token: token.get()}, function (response){
                    Device.get_data({token: token.get()}, function (response) {
                        $scope.devices = response.devices;
                        var allDevicesGroup = findGroupWithId(0);
                        if (allDevicesGroup) {
                            allDevicesGroup.devices = response.devices;
                        }
                        sysMessage.update_s($filter('translate')('system_deviceadded'));

                    });
                });

            };
            
            $scope.clearCache =  function(deviceId) {
                Device.clearCache({token: token.get() , id: deviceId}, function (response) {
                    sysMessage.update_s($filter('translate')('system_device_cachecleared'));
                });
            };

            $scope.openApp =  function(deviceId) {
                Device.openApp({token: token.get() , id: deviceId}, function (response) {
                    sysMessage.update_s($filter('translate')('system_device_ontop'));
                });
            };
            
            $scope.workdays = ['mo', 'tu', 'we', 'th', 'fr', 'sa', 'su'];
            $scope.toggleWorkDay = function(device, day) {
                device[day] = !device[day];
            };
            $scope.workhours = [];
            for (var i = 0; i < 24; i++) {
                 $scope.workhours.push(i + ":00");
                 $scope.workhours.push(i + ":30");
            }

            $scope.deviceOrientations = [1, 2, 3];
            $scope.orientationName = function(orientation) {
                if (orientation == 1) {
                    return $filter('translate')('device_horizontalorientation');
                } else if (orientation == 2) {
                    return $filter('translate')('device_verticalorientation');
                } else {
                    return $filter('translate')('device_verticalorientation_emu');
                }
            };

            $scope.deviceAudioOuts = [1, 2];
            $scope.audioOutName = function(audioOut) {
                if (audioOut == 1) {
                    return 'HDMI';
                } else if (audioOut == 2) {
                    return 'Mini jack';
                }
            };

            $scope.visibleDeviceSettings = 0;
            $scope.showDeviceSettings = function(id) {
                $scope.visibleDeviceSettings = id;
            };
        }
    }]);
