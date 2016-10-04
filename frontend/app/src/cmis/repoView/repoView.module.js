angular
    .module('eArkPlatform.cmis.repoView', [])
    .config(config);

function config($stateProvider, languageFilesProvider, USER_ROLES, modulesMenuServiceProvider){
    
    modulesMenuServiceProvider.addItem({
        templateUrl: 'app/src/cmis/repoView/view/moduleMenuItem.html',
        order: 1,
        authorizedRole: USER_ROLES.enduser
    });

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