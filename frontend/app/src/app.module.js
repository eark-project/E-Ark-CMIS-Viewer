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
        'eArkPlatform.users',
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

    $mdThemingProvider.theme('default')
        .primaryPalette('blue')
        .accentPalette('yellow')
        .warnPalette('deep-orange');

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