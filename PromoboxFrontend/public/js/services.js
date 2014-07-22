/**
 * Created by Maxim on 22.07.2014.
 */
var services = angular.module('promobox.services', ['ngResource']);

services.factory('Campaign', ['$resource',
    function ($resource) {
        return $resource('/campaign/:id', {id: '@id'});
    }]);


services.factory('MultiCampgaignLoader', ['Campaign', '$q',
    function (Campaign, $q) {
        return function () {
            var delay = $q.defer();

            Campaign.query(function (campaigns) {
                delay.resolve(campaigns);
            }, function () {
                delay.reject('Unable to fetch campaigns');
            });
            return delay.promise;
        };
    }]);


services.factory('CampaignLoader', ['Campaign', '$route', '$q',
    function (Campaign, $route, $q) {
        return function () {
            var delay = $q.defer();

            Campaign.get({id: $route.current.params.recipeId}, function (campaign) {
                delay.resolve(campaign);
            }, function () {
                delay.reject('Unable to fetch campaign ' + $route.current.params.campaignId);
            });
            return delay.promise;
        };
    }]);