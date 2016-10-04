angular
    .module('eArkPlatform.search', ['ngMaterial', 'pascalprecht.translate'])
    .config(config);

function config($stateProvider, modulesMenuServiceProvider, USER_ROLES) {
    
    modulesMenuServiceProvider.addItem({
        templateUrl: 'app/src/search/view/menuItem.html',
        order: 2
    });
    
    $stateProvider.state('search', {
        parent: 'site',
        url: '/search',
        views: {
            'content@': {
                templateUrl : 'app/src/search/view/search.html',
                controller : 'SearchController',
                controllerAs: 'sctrl'
            }
        },
        data: {
            authorizedRoles: [USER_ROLES.standard]
        }
    });
    
}