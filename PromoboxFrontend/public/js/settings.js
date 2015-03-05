angular.module('promobox.services').controller('SettingAccountController', ['$scope', '$location', '$http', 'token', 'Clients', '$rootScope', '$translate', 'sysMessage', '$filter',
    function ($scope, $location, $http, token, Clients, $rootScope, $translate, sysMessage, $filter) {
        $rootScope.left_menu_active = 'setting_account';

        Clients.getClient({token: token.get()}, function(response) {

            $scope.userId = response.userId;
            $scope.firstname = response.firstname;
            $scope.surname = response.surname;
            $scope.admin =response.admin;
            $scope.email = response.email;
            $scope.compName = response.compName;
        });

        $scope.save = function() {

            var passLength = 0;

            if ($scope.password) {
                passLength = $scope.password.trim().length;
            }

            if ($scope.password && $scope.password != $scope.passwordRepeat) {
                console.log("password: " + $scope.password);
                console.log("password 2: " + $scope.passwordRepeat);

                // Show error message and remove logs
            } else if (passLength > 0 && passLength < 6)  {

                // Show short password error message
            } else {
                Clients.update({
                    id: $scope.userId,
                    token: token.get(),
                    firstname: $scope.firstname,
                    surname: $scope.surname,
                    companyName: $scope.compName,
                    password: $scope.password,
                    email: $scope.email
                }, function(response) {
                    if(response.response == "ERROR") {
                        if (response.reason == "invalidEmail") {
                            console.log("invalidEmail");  
                            sysMessage.error($filter('translate')('login_form_register') + ' ' + $filter('translate')('registration_form_invalid_email'));
                        } else if (response.reason == "emailExist") {
                            sysMessage.error($filter('translate')('login_form_register') + ' ' + $filter('translate')('registration_form_email_exists'));
                        }
                    } else {
                        // show success message
                    }
                });
            }
        }
    }]);

angular.module('promobox.services').controller('SettingCampaignController', ['$scope', '$location', '$http', 'token', 'Clients', '$rootScope', '$translate', 'sysMessage', '$filter',
    function ($scope, $location, $http, token, Clients, $rootScope, $translate, sysMessage, $filter) {
        $rootScope.left_menu_active = 'setting_campaign';

        Clients.list({token: token.get()}, function(response) {
            console.log(response);

            $scope.users = response.users;
        });

        $scope.selectUser = function(user) {
            $scope.selectedUser = user;
        }

        $scope.openEditUser = function() {
            $scope.firstname = $scope.selectedUser.firstname;
            $scope.surname = $scope.selectedUser.surname;
            $scope.email = $scope.selectedUser.email;
        }

        $scope.openEditUser = function() {
            $scope.firstname = $scope.selectedUser.firstname;
            $scope.surname = $scope.selectedUser.surname;
            $scope.email = $scope.selectedUser.email;
        }
    }]);

angular.module('promobox.services').controller('SettingDeviceController', ['$scope', '$location', '$http', 'token', 'Clients', '$rootScope', '$translate', 'sysMessage', '$filter',
    function ($scope, $location, $http, token, Clients, $rootScope, $translate, sysMessage, $filter) {
        $rootScope.left_menu_active = 'setting_device';
    }]);

angular.module('promobox.services').controller('SettingPaymentController', ['$scope', '$location', '$http', 'token', 'Clients', '$rootScope', '$translate', 'sysMessage', '$filter',
    function ($scope, $location, $http, token, Clients, $rootScope, $translate, sysMessage, $filter) {
        $rootScope.left_menu_active = 'setting_payment';
    }]);

angular.module('promobox.services').controller('SettingUserController', ['$scope', '$location', '$http', 'token', 'Clients', '$rootScope', '$translate', 'sysMessage', '$filter',
    function ($scope, $location, $http, token, Clients, $rootScope, $translate, sysMessage, $filter) {
        $rootScope.left_menu_active = 'setting_user';
    }]);