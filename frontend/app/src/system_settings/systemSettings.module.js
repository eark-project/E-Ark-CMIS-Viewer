angular
    .module('eArkPlatform.systemsettings', [ 'ngMaterial', 'pascalprecht.translate'])
    .config(config);

function config( $stateProvider, languageFilesProvider, USER_ROLES){

    /**
     * Inject the modules translation files
     */
    languageFilesProvider.addFile('app/src/system_settings/i18n/','-cmis.json');

    $stateProvider.state('systemsettings', {
        parent: 'site',
        url: '/system-settings',
        data: {
            authorizedRoles: [USER_ROLES.admin],
        },
        views: {
            'content@': {
                templateUrl: 'app/src/system_settings/general_configuration/view/generalConfiguration.html',
                controller: 'GeneralConfigurationController',
                controllerAs: 'vm'
            }
        }
    });
}