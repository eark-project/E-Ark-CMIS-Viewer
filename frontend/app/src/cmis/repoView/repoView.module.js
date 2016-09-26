angular
    .module('eArkPlatform.cmis.repoView', [])
    .config(config);

function config($stateProvider, languageFilesProvider, USER_ROLES){

    $stateProvider.state('repositoryView', {
        parent: 'site',
        url: '/repository',
        views: {
            'content@': {
                templateUrl : 'app/src/cmis/repoView/view/repoView.html',
                controller : 'RepoViewController',
                controllerAs: 'rvc'
            }
        },
        data: {
            authorizedRoles: [USER_ROLES.standard]
        }
    });

    languageFilesProvider.addFile('app/src/cmis/repoView/i18n/','-repoView.json');

}