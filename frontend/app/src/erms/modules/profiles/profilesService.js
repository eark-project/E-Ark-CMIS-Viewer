angular
    .module('eArkPlatform.erms.profile')
    .factory('ermsProfilesService', ermsProfilesService);

function ermsProfilesService($q, $http){
    return {
        getProfiles     : getProfiles,
        createProfile   : createProfile,
        deleteProfile   : deleteProfile,
        updateProfile   : updateProfile,
        addProfileRepo  : addProfileRepo,
        removeProfileRepo : removeProfileRepo
    };

    function getProfiles(){
        return $http.get('/webapi/profile/getProfiles').then(_returnResult);
    }
    function createProfile(profile){
        return $http.post('/webapi/profile/addProfile', profile).then(_returnResult);
    }
    function updateProfile(profile){
        return $http.put('/webapi/profile/updateProfile', profile).then(_returnResult);
    }

    function deleteProfile(profileName){
        return $http.delete('/webapi/profile/delete/'+ profileName).then(_returnResult);
    }

    function addProfileRepo(profileName, repo){
        return $http.put('/webapi/profile/add/'+profileName+'/repository/'+repo).then(_returnResult);
    }

    function removeProfileRepo(profileName, repo){
        return $http.delete('/webapi/profile/remove/'+profileName+'/repository/'+repo).then(_returnResult);
    }

    /**
     * Generic function for returning the results from the profile as promise
     * @param response
     * @returns {*}
     * @private
     */
    function _returnResult(response){
        var defer = $q.defer();

        try {
        var profileObjects = response.data;
            defer.resolve(profileObjects);
        }
        catch (err) {
            defer.reject('unable to retrieve profiles because: ' + err.message);
        }
        return defer.promise;
    }
}
