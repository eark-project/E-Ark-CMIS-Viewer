angular
    .module('eArkPlatform.users')
    .controller('UsersController', UsersController);

/**
 * Main Controller for the system users module
 * @param $scope
 * @constructor
 */
function UsersController($scope, $mdDialog, $mdToast, userService, $translate, sessionService) {
    var vm = this;

    vm.createUser = createUser;
    vm.deleteUser = deleteUser;
    vm.editUser = editUser;
    vm.userExists = false;

    //For the search control filter
    vm.selectOptions = [
        {optionLabel: $translate.instant('USER.USERNAME'), optionValue: "userName"},
        {optionLabel: $translate.instant('USER.FIRST_NAME'), optionValue: "firstName"},
        {optionLabel: $translate.instant('USER.LAST_NAME'), optionValue: "lastName"}
    ];

    vm.filterCallback = function (query) {
        console.log(query);
        getAllSystemUsers(query);
    };

    populateUsersList();
    function populateUsersList() {
        getAllSystemUsers();
    }

    function createUser(ev) {
        console.log('Creating a new user');
        return showUserDialog(ev, null);
    }

    function editUser(ev, user) {
        console.log('Editing user');
        return showUserDialog(ev, user);
    }

    function deleteUser(ev, user) {
        console.log('Deleting user');

        var confirm = $mdDialog.confirm()
            .title($translate.instant('COMMON.CONFIRM'))
            .textContent($translate.instant('USER.ARE_YOU_SURE_YOU_WANT_TO_DELETE_USER', {
                user: user.firstName + " " + user.lastName + " (" + user.userName + ")"
            }))
            .ariaLabel('')
            .targetEvent(ev)
            .ok($translate.instant('COMMON.YES'))
            .cancel($translate.instant('COMMON.CANCEL'));

        var warning = $mdDialog.confirm()
            .title($translate.instant('COMMON.WARNING'))
            .textContent($translate.instant('USER.CAN_NOT_DELETE_ADMIN_USER'))
            .ariaLabel('')
            .targetEvent(ev)
            .ok($translate.instant('COMMON.OK'));

        if (user.userName != "admin") {
            $mdDialog.show(confirm).then(function () {
                userService.deleteUser(user.userName).then(function (response) {
                    var responseMessage = (Object.keys(response).length == 0) ? $translate.instant('USER.DELETE_USER_SUCCESS') : $translate.instant('USER.DELETE_USER_FAILURE');
                    getAllSystemUsers();
                    $mdToast.show(
                        $mdToast.simple()
                            .content(responseMessage)
                            .position('top right')
                            .hideDelay(3000)
                    );
                })
            });
        } else {
            $mdDialog.show(warning);
        }

    }

    function showUserDialog(ev, user) {
        $mdDialog.show({
            controller: 'UserDialogController',
            controllerAs: 'ucd',
            locals: {
                user: user
            },
            templateUrl: 'app/src/users/view/userCrudDialog.html',
            parent: angular.element(document.body),
            targetEvent: ev,
            clickOutsideToClose: true
        }).then(function onUpdateOrCreate() {
                vm.allSystemUsers = [];
                getAllSystemUsers();
        }, function onCancel() {
            // Do nothing
        });
    }

    function getAllSystemUsers(query) {
        console.log('getting users using this query: ', query);
        return userService.getPersons(query).then(function (response) {
            vm.allSystemUsers = response.users;
            return response;
        });
    }

}