angular.module('eArkPlatform.administration', ['ngMaterial', 'pascalprecht.translate'])
    .config(config);

function config(modulesMenuServiceProvider, $stateProvider, languageFilesProvider, $translateProvider, USER_ROLES) {

    /**
     * Inject the modules translation files
     */
    languageFilesProvider.addFile('app/src/admin/i18n/','-erms.json');

    $stateProvider.state('settings', {
        parent: 'site',
        url: '/settings',
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
    });

    $translateProvider.forceAsyncReload(true);
}