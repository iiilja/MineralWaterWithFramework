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
                delete_campaigns: {method: 'DELETE', url: apiEndpoint + 'token/:token/campaigns/:id/', params: {token: '@token', id: '@id'},
                    interceptor: {
                        response: function(response) {
                            console.log(response);
                            console.debug('Tags: ', response.data.tags);
                            response.data = response.data.tags;
                            return response;
                        }}}
            });}]);

services.factory('Device', ['$resource',
    function ($resource) {
        return $resource('',{},{
                get_data: {method: 'GET', url: apiEndpoint + 'token/:token/devices/', params: {token: '@token'}},
                update: {method: 'PUT', url:apiEndpoint + 'token/:token/devices/:id', params: {token: '@token', id: '@id'}},
                delete: {method: 'DELETE', url:apiEndpoint + 'token/:token/devices/:id', params: {token: '@token', id: '@id'}},
                add: {method: 'POST', url:apiEndpoint + 'token/:token/devices/', params: {token: '@token'}}
            });}]);

services.factory('Files', ['$resource',
    function ($resource) {
        return $resource('',{},{
            getFiles: {method: 'GET', url: apiEndpoint + 'token/:token/campaigns/:id/files/', params: {token: '@token', id: '@id'}},
            arhiveFiles: {method: 'PUT', url: apiEndpoint + 'token/:token/files/archive/:id/', params: {token: '@token', id: '@id'}},
            reorderFiles: {method: 'PUT', url: apiEndpoint + 'token/:token/campaigns/:id/files/order', params: {token: '@token', id: '@id'}}
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
        }
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