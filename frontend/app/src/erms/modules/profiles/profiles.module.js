angular
    .module('eArkPlatform.erms.profile', ['ngMaterial', 'pascalprecht.translate'])
    .config(config);

function config($stateProvider, languageFilesProvider, ermsServiceProvider ){

    $stateProvider.state('erms.repos', {
        parent: 'erms',
        url: '/repositories',
        views: {
            'erms': {
                templateUrl : 'app/src/erms/modules/profiles/view/profilesView.html',
                controller : 'ErmsProfilesController',
                controllerAs: 'empc'
            }
        }
    });

    languageFilesProvider.addFile('app/src/erms/modules/profiles/i18n/','-profiles.json');
    ermsServiceProvider.addERMSModule('ERMS_PROFILES.PROFILES.MENU.LABEL.PROFILES','erms.profiles','list');

}