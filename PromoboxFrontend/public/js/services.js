/**
 * Created by Maxim on 22.07.2014.
 */
var services = angular.module('promobox.services', ['ngResource', 'ngCookies']);

services.factory('Campaign', ['$resource',
    function ($resource) {
        return $resource(apiEndpoint + '/token/:token/campaign/:id/',
            {"token": '@token', id: '@id'},
            {
                all: {method: 'GET', url: apiEndpoint + '/token/:token/campaigns/'}
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
            return token;
        }}
}]);