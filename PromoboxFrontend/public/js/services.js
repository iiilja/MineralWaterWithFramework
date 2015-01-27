/**
 * Created by Maxim on 22.07.2014.
 * https://medium.com/opinionated-angularjs/techniques-for-authentication-in-angularjs-applications-7bbf0346acec
 */
var services = angular.module('promobox.services', ['ngResource', 'ngCookies']);

services.factory('Campaign', ['$resource',
    function ($resource) {
        return $resource('',{},{
                create_new_campaignts: {method: 'POST', url: apiEndpoint + 'token/:token/campaigns/', params: {token: '@token'}},
                edit_campaigns: {method: 'PUT', url: apiEndpoint + 'token/:token/campaigns/:id', params: {token: '@token', id: '@id'}},
                get_campaigns: {method: 'GET', url: apiEndpoint + 'token/:token/campaigns/:id/', params: {token: '@token', id: '@id'}},
                get_all_campaigns: {method: 'GET', url: apiEndpoint + 'token/:token/campaigns/', params: {token: '@token'}},
                refrehs_files_stautes: {
                    method: 'GET',
                    url: apiEndpoint + 'token/:token/files/status',
                    params: {
                        token: '@token'
                    }
                },
                play_next_file: {
                    method: 'PUT',
                    url: apiEndpoint + 'token/:token/campaigns/:id/nextFile/:file',
                    params: {
                        token: '@token',
                        id: '@id',
                        file: '@file'
                    }
                },
                rotate_file: {
                    method: 'PUT',
                    url: apiEndpoint + "token/:token/campaigns/:id/files/:file/rotate/:angle",
                    params: {
                        token: '@token',
                        id: '@id',
                        file: '@file',
                        angle: '@angle'
                    }
                },
                delete_campaigns: {method: 'DELETE', url: apiEndpoint + 'token/:token/campaigns/:id/', params: {token: '@token', id: '@id'}
                    /*,
                    interceptor: {
                        response: function(response) {
                            console.log(response);
                            console.debug('Tags: ', response.data.tags);
                            response.data = response.data.tags;
                            return response;
                        }}*/
                    }
                
            });}]);

services.factory('Device', ['$resource',
    function ($resource) {
        return $resource('',{},{
                get_data: {method: 'GET', url: apiEndpoint + 'token/:token/devices/', params: {token: '@token'}},
                update: {method: 'PUT', url:apiEndpoint + 'token/:token/devices/:id', params: {token: '@token', id: '@id'}},
                delete: {method: 'DELETE', url:apiEndpoint + 'token/:token/devices/:id', params: {token: '@token', id: '@id'}},
                add: {method: 'POST', url:apiEndpoint + 'token/:token/devices/', params: {token: '@token'}},
                clearCache: {method: 'PUT', url:apiEndpoint + 'token/:token/devices/:id/clearcache', params: {token: '@token', id: '@id'}},
                openApp: {method: 'PUT', url:apiEndpoint + 'token/:token/devices/:id/openapp', params: {token: '@token', id: '@id'}},
                delete_device_campaign: {method: 'DELETE', url:apiEndpoint + 'token/:token/devices/:id/campaign/:campaignId', params: {token: '@token', id: '@id', campaignId: '@campaignId'}}
            });}]);

services.factory('Files', ['$resource',
    function ($resource) {
        return $resource('',{},{
            getFiles: {method: 'GET', url: apiEndpoint + 'token/:token/campaigns/:id/files/', params: {token: '@token', id: '@id'}},
            arhiveFiles: {method: 'PUT', url: apiEndpoint + 'token/:token/files/archive/:id/', params: {token: '@token', id: '@id'}},
            reorderFiles: {method: 'PUT', url: apiEndpoint + 'token/:token/campaigns/:id/files/order', params: {token: '@token', id: '@id'}}
        });
    }]);

services.factory("Clients", ['$resource',
    function($resource) {
        return $resource('',{},{
            getClient: {
                method: 'GET',
                url: apiEndpoint + "/user/data/:token",
                params: {
                    token: '@token'
                }
            }
        });
    }]);

services.factory("token", ['$cookies', '$location', function ($cookies, $location) {
    var token = '';
    token = $cookies.token;

    return {
        check: function () {
            if (!token) {
                $location.path('/');
                return false;
            }

            return true;
        },

        put: function (value) {
            token = value;
            $cookies.token = value;
        },

        get: function () {
            this.check();
            return token;
        },

        remove: function () {
            token = undefined;
            delete $cookies["token"];
            $location.path('/');
        }
    }
}]);

services.factory("sysMessage", ['toaster','$filter', function (toaster, $filter) {
    return {
        add_s: function (message) {
            toaster.pop('success', $filter('translate')('system_adding'), message);
        },
        update_s: function (message) {
            toaster.pop('success', $filter('translate')('system_update'), message);
        },
        delete_s: function (message) {
            toaster.pop('success', $filter('translate')('system_deleting'), message);
        },
        login_failed: function (message) {
            toaster.pop('error', $filter('translate')('system_error'), message);
        },
        error: function (message) {
            toaster.pop('error', $filter('translate')('system_error'), message);
        },
    }
}]);

services.factory("sysLocation", ['$location', function ($location) {
    return {
        goHome: function () {
            $location.path('/');
        },
        goList: function () {
            $location.path('/list/');
        },
        goLink: function (link) {
            $location.path(link);
        }
    }
}]);

services.factory('browser', ['$window', function($window) {

     return {

        detectBrowser: function() {

            var userAgent = $window.navigator.userAgent;

            var browsers = {
                chrome: /chrome/i, 
                safari: /safari/i, 
                firefox: /firefox/i, 
                ie: /internet explorer/i, 
                opera: /opera/i
            };

            for(var key in browsers) {
                if (browsers[key].test(userAgent)) {
                    return key;
                }
           };

           return 'unknown';
       }
    }

}]);