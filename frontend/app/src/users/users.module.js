angular.module('eArkPlatform.users', ['ngMaterial', 'pascalprecht.translate'])
    .config(config);

function config($stateProvider, USER_ROLES) {

    $stateProvider.state('usersView', {
        parent: 'site',
        url: '/users',
        views: {
            'content@': {
                templateUrl: 'app/src/users/view/users.html',
                controller: 'UsersController',
                controllerAs: 'vm'
            }
        },
        data: {
            authorizedRoles: [USER_ROLES.admin]
        }
    });

}