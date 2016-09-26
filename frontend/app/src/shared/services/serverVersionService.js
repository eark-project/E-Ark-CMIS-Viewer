angular
    .module('eArkPlatform')
    .factory('serverVersionService', ServerVersionService);

function ServerVersionService($http) {
    return {
        getGitDetails: getGitDetails
    };

    function getGitDetails() {
        //point to a rest interface to retrieve repository information details
        return $http.get("/api/some/endpoint//getversion").then(function (response) {
            return {
                gitCommitId: response.data.scmCommitId,
                gitBranch: response.data.scmBranchName
            };
        });
    }

}