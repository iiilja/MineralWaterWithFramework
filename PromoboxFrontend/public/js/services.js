/**
 * Created by Maxim on 22.07.2014.
 */
var services = angular.module('promobox.services', ['ngResource']);

services.factory('Campaign', ['$resource', 'token',
    function ($resource, token) {
        return $resource(apiEndpoint + '/token/:token/campaign/:id/',
            {"token": token.value, id: '@id'},
            {
                all: {method: 'GET', url: apiEndpoint + '/token/:token/campaigns/'}
            });
    }]);


services.factory("token", function () {
    return {"value": ''};
});