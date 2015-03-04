angular.module('promobox.services').controller('SettingAccountController', ['$scope', '$location', '$http', 'token', 'Clients', '$rootScope', '$translate',
    function ($scope, $location, $http, token, Clients, $rootScope, $translate) {
        $rootScope.left_menu_active = 'setting_account';
    }]);

angular.module('promobox.services').controller('SettingCampaignController', ['$scope', '$location', '$http', 'token', 'Clients', '$rootScope', '$translate',
    function ($scope, $location, $http, token, Clients, $rootScope, $translate) {
        $rootScope.left_menu_active = 'setting_campaign';
    }]);

angular.module('promobox.services').controller('SettingDeviceController', ['$scope', '$location', '$http', 'token', 'Clients', '$rootScope', '$translate',
    function ($scope, $location, $http, token, Clients, $rootScope, $translate) {
        $rootScope.left_menu_active = 'setting_device';
    }]);

angular.module('promobox.services').controller('SettingPaymentController', ['$scope', '$location', '$http', 'token', 'Clients', '$rootScope', '$translate',
    function ($scope, $location, $http, token, Clients, $rootScope, $translate) {
        $rootScope.left_menu_active = 'setting_payment';
    }]);

angular.module('promobox.services').controller('SettingUserController', ['$scope', '$location', '$http', 'token', 'Clients', '$rootScope', '$translate',
    function ($scope, $location, $http, token, Clients, $rootScope, $translate) {
        $rootScope.left_menu_active = 'setting_user';
    }]);