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
                delete_campaigns: {
                    method: 'DELETE', 
                    url: apiEndpoint + 'token/:token/campaigns/:id/', 
                    params: {
                        token: '@token',
                         id: '@id'
                    }
                },
                listPermissions: {
                    method: 'GET',
                    url: apiEndpoint + 'token/:token/campaign/permissions',
                    params: {
                        token: '@token'
                    }
                },
                updatePermissions: {
                    method: 'PUT',
                    url: apiEndpoint + 'token/:token/users/:userId/permissions/campaign/:entityId',
                    params: {
                        token: '@token',
                        userId: '@userId',
                        entityId: '@entityId'
                    }
                },
                deletePermissions: {
                    method: 'DELETE',
                    url: apiEndpoint + 'token/:token/users/:userId/permissions/campaign/:entityId',
                    params: {
                        token: '@token',
                        userId: '@userId',
                        entityId: '@entityId'
                    }
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
                delete_device_campaign: {method: 'DELETE', url:apiEndpoint + 'token/:token/devices/:id/campaign/:campaignId', params: {token: '@token', id: '@id', campaignId: '@campaignId'}},
                listPermissions: {
                    method: 'GET',
                    url: apiEndpoint + 'token/:token/device/permissions',
                    params: {
                        token: '@token'
                    }
                },
                updatePermissions: {
                    method: 'PUT',
                    url: apiEndpoint + 'token/:token/users/:userId/permissions/device/:entityId',
                    params: {
                        token: '@token',
                        userId: '@userId',
                        entityId: '@entityId'
                    }
                },
                deletePermissions: {
                    method: 'DELETE',
                    url: apiEndpoint + 'token/:token/users/:userId/permissions/device/:entityId',
                    params: {
                        token: '@token',
                        userId: '@userId',
                        entityId: '@entityId'
                    }
                }
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
                url: apiEndpoint + 'user/data/:token',
                params: {
                    token: '@token'
                }
            },
            list: {
                method: 'GET',
                url: apiEndpoint + 'token/:token/users',
                params: {
                    token: '@token'
                }
            },
            add: {
                method: 'POST',
                url: apiEndpoint + 'token/:token/users',
                params: {
                    token: '@token'
                }
            },
            update: {
                method: 'PUT', 
                url: apiEndpoint + 'token/:token/users/:id',
                params: {
                    token: '@token',
                    id: '@id'
                }
            },
            remove: {
                method: 'DELETE', 
                url: apiEndpoint + 'token/:token/users/:id',
                params: {
                    token: '@token',
                    id: '@id'
                }
            },
            register: {
                method: 'POST',
                url: apiEndpoint + "user/register",
                params: {}
            }
        });
    }]);

services.factory("DevicesGroups", ['$resource',
    function($resource) {
        return $resource('',{},{
            list: {
                method: 'GET',
                url: apiEndpoint + 'token/:token/groups',
                params: {
                    token: '@token'
                }
            },
            create: {
                method: 'PUT',
                url: apiEndpoint + 'token/:token/groups',
                params: {
                    token: '@token'
                }
            },
            delete: {
                method: 'DELETE',
                url: apiEndpoint + 'token/:token/groups/:groupId',
                params: {
                    token: '@token',
                    groupId: '@groupId'
                }
            },
            addDevice: {
                method: 'PUT',
                url: apiEndpoint + 'token/:token/groups/:groupId/devices/:deviceId',
                params: {
                    token: '@token',
                    groupId: '@groupId',
                    deviceId: '@deviceId'
                }
            },
            removeDevice: {
                method: 'DELETE',
                url: apiEndpoint + 'token/:token/groups/:groupId/devices/:deviceId',
                params: {
                    token: '@token',
                    groupId: '@groupId',
                    deviceId: '@deviceId'
                }
            }
        });
    }]);

services.config(['$cookiesProvider', function ($cookiesProvider){
    var d = new Date();
    d.setMilliseconds(d.getMilliseconds() + 4*60*60*1000);
    $cookiesProvider.defaults.expires = d;
}]);

services.factory("token", ['$cookies', '$location', function ($cookies, $location) {
    var token = $cookies.get('token');

    return {
        check: function () {
            if (!token) {
                $location.path('/');
                return false;
            }

            return true;
        },

        put: function (value, forever) {
            token = value;
            if (forever) {
                $cookies.put('token', value, {'expires' : undefined});
            } else {
                $cookies.put('token', value, $cookies.defaults);
            }
        },

        get: function () {
            this.check();
            return token;
        },

        remove: function () {
            token = undefined;
            $cookies.remove('token');
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
        registration_success: function(message) {
            toaster.pop('success', $filter('translate')('login_form_register'), message);
        },
        warning: function (message) {
            toaster.pop('warning', $filter('translate')('system_warning'), message);
        },
        error: function (message) {
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

services.factory('facade', function(token, sysMessage, Clients, Device, Campaign, DevicesGroups, $location, $filter, $modal) {

     return {

        getToken: function() {
            return token;
        },
        getSysMessage: function() {
           return sysMessage;
        }, 
        getClients: function() {
            return Clients;
        },
        getDevices: function() {
            return Device;
        },
        getCampaigns: function() {
            return Campaign;
        },
        getDevicesGroups: function(){
            return DevicesGroups;
        },
        getLocation: function() {
            return $location;
        },
        getFilter: function() {
            return $filter;
        },
        getModal: function() {
            return $modal;
        }

    }
});