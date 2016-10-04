angular
    .module('eArkPlatform.systemsettings', [ 'ngMaterial', 'pascalprecht.translate'])
    .config(config);

function config(systemSettingsPagesServiceProvider, $stateProvider, USER_ROLES){

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