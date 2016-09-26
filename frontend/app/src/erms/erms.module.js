angular
    .module('eArkPlatform.erms', ['ngMaterial', 'pascalprecht.translate'])
    .config(config)
    .factory('httpTicketInterceptor', httpTicketInterceptor);

function config(modulesMenuServiceProvider, $stateProvider, languageFilesProvider, $translateProvider, $httpProvider) {
    /**
     * Inject a menuItem into the platform header area
     */
    modulesMenuServiceProvider.addItem({
        templateUrl: 'app/src/erms/view/menuItem.html',
        order: 2
    });

    /**
     * Inject the modules translation files
     */
    languageFilesProvider.addFile('app/src/erms/i18n/','-erms.json');

    $stateProvider.state('erms', {
        parent: 'site',
        url: '/erms',
        views: {
            'content@': {
                templateUrl: 'app/src/erms/view/erms.html',
                controller: 'ErmsMenuController',
                controllerAs: 'emc'
            }
        }
    });

    $translateProvider.forceAsyncReload(true);

    $httpProvider.interceptors.push('httpTicketInterceptor');
}

function httpTicketInterceptor() {
    return {
        request: request
    };

    function request(config) {

        config.url = prefixServiceUrl(config.url);


        return config;

    }

    function prefixServiceUrl(url) {
        if (url.indexOf("/webapi/") == 0 ) {
            return "http://localhost:9090" + url;
        }
        return url;
    }
}