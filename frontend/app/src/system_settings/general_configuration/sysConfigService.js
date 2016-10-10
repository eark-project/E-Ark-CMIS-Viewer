
angular
        .module('eArkPlatform.systemsettings')
        .factory('sysConfigService', sysConfigService);

function sysConfigService($http) {
    return {
        getRepoDetails: getRepoDetails,
        saveRepoDetails: saveRepoDetails,
    };

    function getRepoDetails() {
        return $http.get('/webapi/repository/details').then(function(response) {
            return response.data;
        });
    }

    function saveRepoDetails(repoParams) {
        return $http.put('/webapi/repository/update', repoParams)
                .then(function(response) {
                    return response.data;
                });
    }
}