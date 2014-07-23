/**
 * Created by Maxim on 22.07.2014.
 */
var services = angular.module('promobox.services', ['ngResource']);

services.factory('Campaign', ['$resource', 'token',
    function ($resource, token) {
        return $resource('http://:url/token/:token/campaign/:id/', {url:apiEndpoint, 'token':token.value, id: '@id'});
    }]);


