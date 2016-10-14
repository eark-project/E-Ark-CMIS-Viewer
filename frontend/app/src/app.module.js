angular
    .module('eArkPlatform', [
        'ngSanitize',
        'ngMaterial',
        'ngMessages',
        'material.wizard',
        'ui.router',
        'rt.encodeuri',
        'ngResource',
        'pdf',
        'swfobject',
        'isteven-multi-select',
        'eArkPlatform.init',
        'eArkPlatform.translations.init',
        'eArkPlatform.header',
        'eArkPlatform.administration',
        'eArkPlatform.systemsettings',
        'eArkPlatform.users',
        'eArkPlatform.search',
        //'eArkPlatform.dashboard',
        'eArkPlatform.cmis.repoView',
        'eArkPlatform.common.directives',
        'eArkPlatform.common.directives.filter',
        'dcbImgFallback',
        /*DO NOT REMOVE MODULES PLACEHOLDER!!!*/ //openDesk-modules
        /*LAST*/ 'eArkPlatform.translations'])// TRANSLATIONS IS ALWAYS LAST!
    .config(config)
    .run(function ($rootScope, $state, $mdDialog, authService, sessionService, APP_CONFIG) {
        angular.element(window.document)[0].title = APP_CONFIG.appName;
        $rootScope.appName = APP_CONFIG.appName;

        $rootScope.$on('$stateChangeStart', function (event, next, params) {
            $rootScope.toState = next;
            $rootScope.toStateParams = params;
            if (next.data.authorizedRoles.length === 0) {
                return;
            }

            if (authService.isAuthenticated() && authService.isAuthorized(next.data.authorizedRoles)) {
                //We do nothing. Attempting to transition to the actual state results in call stack exception
            } else {
                event.preventDefault();
                sessionService.retainCurrentLocation();
                $state.go('login');
            }

            // If we got any open dialogs, close them before route change
            $mdDialog.cancel();
        });
    });

function config($mdThemingProvider, $stateProvider, $urlRouterProvider, $httpProvider) {
        //$httpProvider.interceptors.push('httpTicketInterceptor');
        //$httpProvider.defaults.headers.common.Authorization = undefined;
        $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';

    $mdThemingProvider.definePalette('earkStyle', {
        '50': 'FFB49F',
        '100': 'FF9885',
        '200': 'FF7C6B',
        '300': 'ED6053',
        '400': 'CD453B',
        '500': 'AD2624',
        '600': '7A3332',
        '700': '472C2C',
        '800': '333333',
        '900': '000000',
        'A100': 'FFC7B8',
        'A200': 'FFAD9E',
        'A400': 'FF9385',
        'A700': 'AD3736',
        'contrastDefaultColor': 'light', // whether, by default, text (contrast) on this palette should be dark or light
        'contrastDarkColors': ['50', '100', '200', '300', '400', 'A100'] //hues which contrast should be 'dark' by default
    });
    
    $mdThemingProvider.theme('default')
        .primaryPalette('earkStyle')
        .accentPalette('grey')
        .warnPalette('amber');

    $urlRouterProvider
        .otherwise('/login');

    $stateProvider.state('site', {
        abstract: true,
        resolve: {
            authorize: ['authService', function (authService) {}]
        },
        views: {
            'footer@': {
                templateUrl: 'app/src/footer/view/footer.html',
                controller: 'FooterController'
            },
            'header@': {
                templateUrl: 'app/src/header/view/header.html',
                controller: 'HeaderController',
                controllerAs: 'vm'
            }
        }
    }).state('login', {
            parent: 'site',
            url: '/login?error&nosso',
            views: {
                'content@': {
                    templateUrl: 'app/src/authentication/view/login.html',
                    controller: 'AuthController',
                    controllerAs: 'vm'
                },
                'header@': {},
                'footer@': {}
            },
            data: {
                authorizedRoles: []
            }
        });
}