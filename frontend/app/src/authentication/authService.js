angular
    .module('eArkPlatform')
    .config(config)
    .factory('httpTicketInterceptor', httpTicketInterceptor)
    .factory('authService', authService);

function config($httpProvider) {
    $httpProvider.interceptors.push('httpTicketInterceptor');
    $httpProvider.defaults.headers.common.Authorization = undefined;
    $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
}

function httpTicketInterceptor($injector, $translate, $window, $q, sessionService, BRIDGE_URI) {
    return {
        request: request,
        response: response,
        responseError: responseError
    };

    function request(config) {
        config.url = prefixAlfrescoServiceUrl(config.url);
        if (sessionService.getUserInfo()) {
            config.params = config.params || {};
            config.params.alf_ticket = sessionService.getUserInfo().ticket;
        }
        return config;
    }

    function prefixAlfrescoServiceUrl(url) {
        if (url.indexOf("/webapi") == 0) {
         return BRIDGE_URI.serviceProxy + url;
         }
        return url;
    }

    function response(response) {
        if (response.status == 401 && typeof $window._omsSessionExpired === 'undefined') {
            sessionExpired();
        }
        return response || $q.when(response);
    }

    function responseError(rejection) {
        //Prevent from popping up the message on failed SSO attempt
        if (rejection.status == 401) {
            console.log('==> Authentication failure ');
            sessionExpired();
        }
        return $q.reject(rejection);
    }

    function sessionExpired() {
        if (typeof $window._omsSessionExpired !== 'undefined')
            return;

        $window._omsSessionExpired = true;
        sessionService.setUserInfo(null);
        var $mdDialog = $injector.get('$mdDialog'),
            notificationUtilsService = $injector.get('notificationUtilsService');
        $mdDialog.cancel();
        sessionService.retainCurrentLocation();
        $window.location = "/#/login";
        notificationUtilsService.notify($translate.instant('LOGIN.SESSION_TIMEOUT'));
        delete $window._omsSessionExpired;
    }
}

function authService($http, $window, sessionService, userService, $q) {
    var service = {
        login: login,
        logout: logout,
        loggedin: loggedin,
        changePassword: changePassword,
        isAuthenticated: isAuthenticated,
        isAuthorized: isAuthorized,
        getUserInfo: getUserInfo,
        ssoLogin: ssoLogin
    };

    return service;

    function getUserInfo() {
        return sessionService.getUserInfo();
    }

    function ssoLogin() {
        return $http.get("/touch").then(function (response) {
            if (response.status == 401 || authFailedSafari(response)) {
                return response;
            }
            sessionService.setUserInfo({});
            return revalidateUser();
        });
    }

    function login(username, password) {
        var userInfo = {};
        return $http.post('/webapi/authentication/login', {userName: username, password: password}).then(function (response) {
                //sessionService.setUserInfo(response.data);
                return addUserAndParamsToSession(username);
            }, function (reason) {
                console.log(reason);
                return reason;
            });
    }

    function logout() {
        var userInfo = sessionService.getUserInfo();


        if (userInfo) {
            //return $http.post('/webapi/logout').then(function (response) {
                sessionService.clearUserInfo();
                sessionService.clearRetainedLocation();
                //return response;
            //});

        }
        return $q.resolve(true);
    }

    function loggedin() {
        return sessionService.getUserInfo();
    }

    /**
     * Accepts a user email (which should be unique) bound to a unique user name, recreates a password for the user
     * and emails the user with the details required to login to the system.
     * @param email
     * @returns {*}
     */
    function changePassword(email) {
        return $http.post("/webapi/reset-user-password", {email: email}).then(function (response) {
            return response;
        });
    }

    function isAuthenticated() {
        return sessionService.getUserInfo();
    }

    function isAuthorized(authorizedRoles) {
        var userInfo = sessionService.getUserInfo();
        if (typeof userInfo === 'undefined') {
            return false;
        }
        if (!angular.isArray(authorizedRoles)) {
            authorizedRoles = [authorizedRoles];
        }
        return userInfo.user.role == 'ADMIN' ||
            (authorizedRoles.length > 0 && authorizedRoles.indexOf('standard') > -1);
    }

    function addUserAndParamsToSession(username) {
        return userService.getPerson(username).then(function (user) {
            delete $window._omsSessionExpired;
            var userInfo = sessionService.getUserInfo();
            userInfo['user'] = user;
            sessionService.setUserInfo(userInfo);
            return user;
        });
    }
}