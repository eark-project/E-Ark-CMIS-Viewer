angular
    .module('eArkPlatform')
    .controller('AuthController', AuthController);

function AuthController($state, $stateParams, $mdDialog, $window, authService, errorService, sessionService, modulesMenuService, base64) {
    var vm = this;
    var loginErrorMessage = angular.fromJson($stateParams.error);

    vm.login = login;
    vm.logout = logout;
    vm.loggedin = loggedin;
    vm.getUserInfo = getUserInfo;
    vm.errorMsg = loginErrorMessage ? loginErrorMessage : "";
    vm.showForgotDialog = showForgotDialog;
    vm.updateValidator = updateValidator;

    if ($stateParams.nosso !== "true" && !authService.isAuthenticated()) {
        authService.ssoLogin().then(function (response) {
            if (response.status == 401) {
                return;
            }
           /* if (response.uid) {
                userService.getPerson(response.uid).then(function (response) {
                    vm.user = response;
                    restoreLocation();
                });
            }*/
        });
    }

    function login(credentials) {
        authService.login(credentials.username, base64.encode(credentials.password) ).then(function (response) {
            modulesMenuService.fixPerms(sessionService.getUserInfo().user);

            // Logged in
            if (sessionService.getUserInfo().user) {
                    vm.user = sessionService.getUserInfo().user;
                    restoreLocation();
            }

            // If incorrect values            
            if (response.status == 401) {
                debugger;
                errorService.displayErrorMsg( response );
            } else if (response.status == 500) {
                errorService.displayErrorMsg( response );
            }

        });
    }

    function restoreLocation() {
        var retainedLocation = sessionService.getRetainedLocation();
        if (!retainedLocation || retainedLocation === undefined) {
            $state.go('repositoryView');
        } else {
            $window.location = retainedLocation;
        }
    }

    function logout() {
        $state.go('login');
        authService.logout().then(function () {
            delete vm.user;

        });
    }

    function loggedin() {
        return authService.loggedin();
    }

    function updateValidator() {
        if (vm.form.password.$error.loginFailure)
            vm.form.password.$setValidity("loginFailure", true);
    }

    function forgotPasswordCtrl($scope, $mdDialog) {
        var dlg = this;
        dlg.emailSent = false;

        dlg.cancel = function () {
            return $mdDialog.cancel();
        };

        dlg.updateValidators = function () {
            if (dlg.form.email.$error.emailNotExists)
                dlg.form.email.$setValidity("emailNotExists", true);
        };

        dlg.forgotPassword = function () {
            if (!dlg.email) return;

            authService.changePassword(dlg.email).then(
                    function success(response) {
                        dlg.emailSent = true;
                   },
                    function onError(response) {
                        // If email doesn't exist in system
                        if (response.status !== 200)
                            dlg.form.email.$setValidity("emailNotExists", false);
                    }
            );
        };
    };

    function showForgotDialog(ev) {
        $mdDialog.show({
            controller: forgotPasswordCtrl,
            controllerAs: 'dlg',
            templateUrl: 'app/src/authentication/view/forgotPasswordDialog.html',
            parent: angular.element(document.body),
            targetEvent: ev,
            clickOutsideToClose: true
        });
    }

    function getUserInfo() {
        var userInfo = authService.getUserInfo();
        return userInfo;
    }
}