angular
    .module('eArkPlatform')
    .factory('sessionService', sessionService);

function sessionService($window) {
    var userInfo = {};
    var service = {
        getUserInfo: getUserInfo,
        setUserInfo: setUserInfo,
        clearUserInfo: clearUserInfo,
        isArchivist: isArchivist,
        retainCurrentLocation: retainCurrentLocation,
        getRetainedLocation: getRetainedLocation,
        clearRetainedLocation: clearRetainedLocation
    };

    init();

    return service;

    function init() {
        if ($window.sessionStorage.getItem('userInfo')) {
            userInfo = angular.fromJson($window.sessionStorage.getItem('userInfo'));
        }
    }

    function getUserInfo() {
        return userInfo;
    }

    function setUserInfo(info) {
        userInfo = info;
        $window.sessionStorage.setItem('userInfo', angular.toJson(userInfo));
    }

    function clearUserInfo(){
        userInfo = {};
        $window.sessionStorage.clear('userInfo');
    }

    function isArchivist() {
        if (userInfo == null || userInfo == undefined) {
            return false;
        }
        return userInfo.user.role == 'archivist';
    }

    function retainCurrentLocation() {
        this.clearRetainedLocation();
        var location = $window.location.hash;
        if (location == '#/login') {
            return;
        }
        $window.sessionStorage.setItem('retainedLocation', location);
    }

    function getRetainedLocation() {
        return $window.sessionStorage.getItem('retainedLocation');
    }

    function clearRetainedLocation() {
        $window.sessionStorage.setItem('retainedLocation', "");
    }

}