var apiEndpoint = "http://api.dev.promobox.ee/service/";

var app = angular.module('promobox', ['ngRoute', 'ui.bootstrap', 'pascalprecht.translate', 'promobox.services', 'angularFileUpload']);


app.config(['$routeProvider', function ($routeProvider) {
    $routeProvider
        .when('/', {
            controller: 'LoginController',
            templateUrl: '/views/login.html'
        })
        .when('/registration', {
            controller: 'RegistrationController',
            templateUrl: '/views/register.html'
        })
        .when('/main', {
            controller: 'MainController',
            templateUrl: '/views/main.html'
        })
        .when('/campaign/edit/:cId', {
            controller: 'CampaignEditController',
            templateUrl: '/views/campaign_edit.html'
        })
        .when('/exit', {
            controller: 'Exit',
            template: ''
        })
        .otherwise({redirectTo: '/'});
}]);

app.config(function ($httpProvider) {
    $httpProvider.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded; charset=UTF-8';
});

app.config(function ($translateProvider) {
    $translateProvider.useStaticFilesLoader({
        prefix: '/json/',
        suffix: '.json'
    });

    $translateProvider.preferredLanguage('en');

});

app.controller('Exit', ['$scope', '$location', '$http', 'token',
    function($scope, $location, $http, token) {
        token.put(undefined);
        token.check();
    }]);

app.controller('LoginController', ['$scope', '$location', '$http', 'token',
    function ($scope, $location, $http, token) {
        console.log(!token.check());
        if (!token.check()) {

            $scope.login_form = {email: '', password: '', remember: false};

            $scope.login = function () {
                $http.post(apiEndpoint + "user/login",
                    $.param({
                        email: $scope.login_form.email,
                        password: $scope.login_form.password
                    }))
                    .success(function (data) {
                        if (data.response == 'OK') {
                            token.put(data.token);

                            console.log(data);

                            $location.path('/main/');
                        }
                    });
            };
        } else {
            $location.path('/main/');
        }
    }]);

app.controller('RegistrationController', ['$scope', '$location', '$http', 'token',
    function ($scope, $location, $http, token) {
        console.log(!token.check());
        if (!token.check()) {

        } else {
            $location.path('/');
        }
    }]);

app.controller('CampaignEditController', ['$scope', '$routeParams', 'token', 'Campaign', '$upload','$location', '$http',
    function ($scope, $routeParams, token, Campaign, $upload, $location, $http) {
       if (token.check()) {
           $scope.campaign = Campaign.get({id: $routeParams.cId, token: token.get()});
           console.log($scope.campaign);

           $scope.campaign_form = {campaign_name: $scope.campaign.name, campaign_time: $scope.campaign.duration, campaign_order: $scope.campaign.sequence, campaign_start: $scope.campaign.start, campaign_finish: $scope.campaign.finish};


           $scope.onFileSelect = function($files) {
               //$files: an array of files selected, each file has name, size, and type.
               for (var i = 0; i < $files.length; i++) {
                   var file = $files[i];
                   console.log(file);
                   $scope.upload = $upload.upload({
                       url: apiEndpoint+'token/'+token.get()+'/files/'+$scope.campaign.id, //upload.php script, node.js route, or servlet url
                       //method: 'POST' or 'PUT',
                       method: 'POST',
                       //headers: {'header-key': 'header-value'},
                       //withCredentials: true,
                       //data: {myObj: $scope.myModelObj},
                       file: file // or list of files ($files) for html5 only
                       //fileName   : 'doc.jpg' or ['1.jpg', '2.jpg', ...] // to modify the name of the file(s)
                       // customize file formData name ('Content-Desposition'), server side file variable name.
                       //fileFormDataName: myFile, //or a list of names for multiple files (html5). Default is 'file'
                       // customize how data is added to formData. See #40#issuecomment-28612000 for sample code
                       //formDataAppender: function(formData, key, val){}
                   }).progress(function(evt) {
                       console.log('percent: ' + parseInt(100.0 * evt.loaded / evt.total));
                   }).success(function(data, status, headers, config) {
                       // file is uploaded successfully
                       console.log(data);
                   });
                   //.error(...)
                   //.then(success, error, progress);
                   // access or attach event listeners to the underlying XMLHttpRequest.
                   //.xhr(function(xhr){xhr.upload.addEventListener(...)})
               }
               /* alternative way of uploading, send the file binary with the file's content-type.
                Could be used to upload files to CouchDB, imgur, etc... html5 FileReader is needed.
                It could also be used to monitor the progress of a normal http post/put request with large data*/
               // $scope.upload = $upload.http({...})  see 88#issuecomment-31366487 for sample code.
           };

           $scope.edit_company = function () {
                console.log($scope.campaign_form);
               $http.post(apiEndpoint + "token/"+token.get()+"/campaigns/"+$scope.campaign.id,
                   $.param({
                       "status":"0",
                       "sequence":$scope.campaign_form.campaign_order,
                       "start":$scope.campaign_form.campaign_start,
                       "finish":$scope.campaign_form.campaign_finish,
                       "duration":$scope.campaign_form.campaign_time}))
                   .success(function (data) {
                       if (data.response == 'OK') {
                           console.log(data);
                           $location.path('/main/');
                       }
                   });
           };



       }
    }]);

app.controller('DatepickerCtrl', ['$scope',
    function ($scope) {
        $scope.today = function() {
            $scope.campaign_form.campaign_start = new Date();
            $scope.campaign_form.campaign_finish = new Date();
        };
//        $scope.today();

        $scope.clear = function () {
            $scope.campaign_form.campaign_start = null;
            $scope.campaign_form.campaign_finish = null;
        };

        // Disable weekend selection
        $scope.disabled = function(date, mode) {
            return ( mode === 'day' && ( date.getDay() === 0 || date.getDay() === 6 ) );
        };

        $scope.toggleMin = function() {
            $scope.minDate = $scope.minDate ? null : new Date();
        };
        $scope.toggleMin();

        $scope.open = function($event) {
            $event.preventDefault();
            $event.stopPropagation();

            $scope.opened = true;
        };

        $scope.dateOptions = {
            formatYear: 'yy',
            startingDay: 1
        };

        $scope.initDate = new Date('2016-15-20');
        $scope.formats = ['dd-MMMM-yyyy', 'yyyy/MM/dd', 'dd.MM.yyyy', 'shortDate'];
        $scope.format = $scope.formats[0];
    }]);

app.controller('MainController', ['$scope', '$location', '$http', 'token', 'Campaign',
    function ($scope, $location, $http, token, Campaign) {
        if (token.check()) {

            $scope.token = token.get();

            $scope.remove = function (campaign) {
                $scope.campaigns.splice($scope.campaigns.indexOf(campaign), 1);
            };

            Campaign.all({token: token.get()},function (response) {
                if (response.response == 'OK') {
                    $scope.campaigns = response.campaigns;
                }
            });
        }

    }]);
