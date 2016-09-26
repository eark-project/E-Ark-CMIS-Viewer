angular.module('eArkPlatform.administration', ['ngMaterial', 'pascalprecht.translate'])
    .config(config);

function config(modulesMenuServiceProvider, $stateProvider, languageFilesProvider, $translateProvider, USER_ROLES) {
    /**
     * Inject a menuItem into the platform header area
     */
    modulesMenuServiceProvider.addItem({
        templateUrl: 'app/src/admin/view/menuItem.html',
        order: 10
    });

    /**
     * Inject the modules translation files
     */
    languageFilesProvider.addFile('app/src/admin/i18n/','-erms.json');

    $stateProvider.state('administration', {
        parent: 'site',
        url: '/admin',
        views: {
            'content@': {
                templateUrl: 'app/src/admin/view/admin.html',
                controller: 'AdminController',
                controllerAs: 'vm'
            }
        },
        data: {
            authorizedRoles: [USER_ROLES.admin],
            selectedTab: 0
        }
    }).state('administration.users', {
        url: '/users',
        data: {
            authorizedRoles: [USER_ROLES.admin],
            selectedTab: 0
        },
        views: {
            'users': {
                templateUrl: 'app/src/users/view/users.html',
                controller: 'UsersController',
                controllerAs: 'vm'
            }
        }
    });

    $translateProvider.forceAsyncReload(true);
}