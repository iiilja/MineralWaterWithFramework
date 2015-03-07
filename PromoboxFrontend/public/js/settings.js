angular.module('promobox.services').controller('SettingAccountController', function($scope, $rootScope, facade) {
        $rootScope.left_menu_active = 'setting_account';
        $rootScope.showSettings = true;

        var $filter = facade.getFilter();

        facade.getClients().getClient({token: facade.getToken().get()}, function(response) {

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
                facade.getClients().update({
                    id: $scope.userId,
                    token: facade.getToken().get(),
                    firstname: $scope.firstname,
                    surname: $scope.surname,
                    companyName: $scope.compName,
                    password: $scope.password,
                    email: $scope.email
                }, function(response) {
                    if(response.response == "ERROR") {
                        if (response.reason == "invalidEmail") {
                            facade.getSysMessage().error($filter('translate')('login_form_register') + ' ' + $filter('translate')('registration_form_invalid_email'));
                        } else if (response.reason == "emailExist") {
                            facade.getSysMessage().error($filter('translate')('login_form_register') + ' ' + $filter('translate')('registration_form_email_exists'));
                        }
                    } else {
                        // show success message
                    }
                });
            }
        }
    });

angular.module('promobox.services').controller('SettingCampaignController', function($scope, $rootScope, facade) {
        $rootScope.left_menu_active = 'setting_campaign';
        $rootScope.showSettings = true;
    });

angular.module('promobox.services').controller('SettingDeviceController', function($scope, $rootScope, facade) {
        $rootScope.left_menu_active = 'setting_device';
        $rootScope.showSettings = true;
    });

angular.module('promobox.services').controller('SettingPaymentController', function($scope, $rootScope, facade) {
        $rootScope.left_menu_active = 'setting_payment';
        $rootScope.showSettings = true;
    });

angular.module('promobox.services').controller('SettingUserController', function($scope, $rootScope, facade) {
        $rootScope.left_menu_active = 'setting_user';
        $rootScope.showSettings = true;

         var $filter = facade.getFilter();

        facade.getClients().list({token: facade.getToken().get()}, function(response) {
            $scope.users = response.users;
        });

        $scope.selectUser = function(user) {
            $scope.selectedUser = user;
        }

        var openEditDialog = function(editMode) {
            var modalInstanse = facade.getModal().open({
                templateUrl: '/views/modal/editUser.html',
                controller: 'ModalUserController',
                resolve: {
                    facade: function() {
                        return facade;
                    },
                    model: function() {
                        return { 
                            selectedUser: $scope.selectedUser,
                            editMode: editMode
                        }
                    }
                }
            });

            modalInstanse.result.then(function(model) {
                console.log(model);
                if (!model.editUser) {
                    $scope.users.push(model.selectedUser);
                }
            });
        }
        $scope.openEditUser = function() {
            if ($scope.selectedUser) {
                openEditDialog(true);
            }
        }

        $scope.openAddUser = function() {
            openEditDialog(false);
        }

        $scope.deleteUser = function() {
            facade.getClients().remove({
                token: facade.getToken().get(),
                id: $scope.selectedUser.id
            }, function(response) {
                if(response.response == "ERROR") {
                    // show error message
                } else {
                    // show success message
                    $scope.selectedUser.active = false;
                    $scope.selectedUser = null;
                }
            });
        }
    });

angular.module('promobox.services').controller('ModalUserController', function ($scope, $modalInstance, facade, model) {
        var $filter = facade.getFilter();

        $scope.data = {};
        if (model.editMode) {
            $scope.data.firstname = model.selectedUser.firstname;
            $scope.data.surname = model.selectedUser.surname;
            $scope.data.email = model.selectedUser.email;
            $scope.data.password = "";
        } else {
            $scope.data.firstname = "";
            $scope.data.surname = "";
            $scope.data.email = "";
            $scope.data.password = "";
        }
        $scope.editMode = model.editMode;

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };

        $scope.editUser = function() {
            facade.getClients().update({
                id: model.selectedUser.id,
                token: facade.getToken().get(),
                firstname: $scope.data.firstname,
                surname: $scope.data.surname,
                password: $scope.data.password,
                email: $scope.data.email
            }, function(response) {
                if(response.response == "ERROR") {
                    if (response.reason == "invalidEmail") {
                        facade.getSysMessage().error($filter('translate')('login_form_register') + ' ' + $filter('translate')('registration_form_invalid_email'));
                    } else if (response.reason == "emailExist") {
                        facade.getSysMessage().error($filter('translate')('login_form_register') + ' ' + $filter('translate')('registration_form_email_exists'));
                    }
                } else {
                    // show success messagee
                    model.selectedUser.firstname = $scope.data.firstname;
                    model.selectedUser.surname = $scope.data.surname;
                    model.selectedUser.email = $scope.data.email;
                    model.selectedUser.active = true;

                    $modalInstance.close({
                        editMode: model.editMode,
                        selectedUser: model.selectedUser
                    });
                }
            });
        }

        $scope.addUser = function() {
            facade.getClients().add({
                token: facade.getToken().get()
            }, function(response) {
                model.selectedUser = response.user;
                $scope.editUser();
            });
        }

    });