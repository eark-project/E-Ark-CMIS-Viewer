angular
        .module('openeApp.systemsettings', [ 'ngMaterial', 'pascalprecht.translate'])
        .config(config);

function config(systemSettingsPagesServiceProvider, $stateProvider, USER_ROLES){
    systemSettingsPagesServiceProvider.addPage('ADMIN.SYS_SETTINGS.GENERAL.GENERAL_CONFIG', 'administration.systemsettings.general');

    $stateProvider.state('administration.systemsettings', {
        url: '/system-settings',
        data: {
            authorizedRoles: [USER_ROLES.admin],
            selectedTab: 4
        },
        views: {
            'systemsettings': {
                templateUrl: 'app/src/system_settings/menu/system_settings.html',
                controller: 'SystemSettingsController',
                controllerAs: 'vm'
            }
        }
    }).state('administration.systemsettings.general', {
        url: '/general-configuration',
        data: {
            authorizedRoles: [USER_ROLES.admin]
        },
        views: {
            'systemsetting-view': {
                templateUrl: 'app/src/system_settings/general_configuration/view/generalConfiguration.html',
                controller: 'GeneralConfigurationController',
                controllerAs: 'vm'
            }
        }
    });
}