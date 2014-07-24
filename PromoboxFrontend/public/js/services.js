/**
 * Created by Maxim on 22.07.2014.
 */
var services = angular.module('promobox.services', ['ngResource']);

services.factory('Campaign', ['$resource', 'token',
    function ($resource, token) {
        return $resource(apiEndpoint + '/token/:t/campaign/:id/', {t:token.value, id: '@id'});
    }]);


services.factory("token", function () {
    return {"value": ''};
});